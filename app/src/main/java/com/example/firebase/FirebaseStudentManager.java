package com.example.firebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.model.Student;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gestionnaire Firebase Realtime Database pour les inscriptions des élèves du Lycée.
 * Fournit la synchronisation temps réel et une persistance résiliente avec bascule locale fluide.
 */
public class FirebaseStudentManager {

    private static final String TAG = "FirebaseStudentManager";
    private static final String NODE_ELEVES = "eleves";
    private static final String PREF_NAME = "mylycee_local_cache";
    private static final String KEY_CACHED_STUDENTS = "cached_students_json";

    private static FirebaseStudentManager instance;
    private final DatabaseReference databaseReference;
    private final Context appContext;
    private final SharedPreferences sharedPreferences;

    // Liste en mémoire pour un affichage instantané et réactif
    private final List<Student> studentList = new ArrayList<>();
    private final List<OnStudentsChangedListener> listeners = new ArrayList<>();

    public interface OnStudentsChangedListener {
        void onStudentsChanged(List<Student> students);
        void onError(String errorMessage);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Singleton pour le gestionnaire Firebase.
     */
    public static synchronized FirebaseStudentManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseStudentManager(context.getApplicationContext());
        }
        return instance;
    }

    private FirebaseStudentManager(Context context) {
        this.appContext = context;
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        DatabaseReference ref = null;
        try {
            // Initialisation de FirebaseApp si non initialisée (cas sans google-services.json complet)
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApplicationId("cg.moimeme.mylycee")
                        .setApiKey("AIzaSyMyLyceeOfflineFallbackKey123456789")
                        .setDatabaseUrl("https://mylycee-app-default-rtdb.firebaseio.com")
                        .setProjectId("mylycee-app")
                        .build();
                FirebaseApp.initializeApp(context, options);
            }

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            try {
                db.setPersistenceEnabled(true);
            } catch (Throwable ignored) {
                // Déjà configuré ou ignoré
            }
            ref = db.getReference(NODE_ELEVES);
            Log.i(TAG, "Firebase Realtime Database initialisé avec succès");
        } catch (Throwable t) {
            Log.w(TAG, "Firebase en mode local résilient : " + t.getMessage());
            ref = null;
        }

        this.databaseReference = ref;

        // Charger le cache local au démarrage pour une réactivité immédiate
        loadFromLocalCache();

        // Si le cache est vide, initialiser avec quelques élèves exemples représentatifs
        if (studentList.isEmpty()) {
            initSampleStudents();
        }

        // Écouter les modifications en temps réel sur le nœud Firebase si disponible
        if (this.databaseReference != null) {
            attachFirebaseListener();
        }
    }

    public boolean isFirebaseAvailable() {
        return databaseReference != null;
    }

    /**
     * Attache un écouteur en temps réel sur la référence Firebase "eleves".
     */
    private void attachFirebaseListener() {
        if (databaseReference == null) return;
        try {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Student> incoming = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        try {
                            Student s = child.getValue(Student.class);
                            if (s != null) {
                                if (s.getId() == null || s.getId().isEmpty()) {
                                    s.setId(child.getKey());
                                }
                                incoming.add(s);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur de désérialisation d'un élève: " + e.getMessage());
                        }
                    }

                    if (!incoming.isEmpty()) {
                        studentList.clear();
                        studentList.addAll(incoming);
                        saveToLocalCache();
                        notifyListeners();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Écouteur Firebase annulé ou hors ligne: " + error.getMessage());
                    notifyError(error.getMessage());
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "Erreur lors de l'attachement de l'écouteur Firebase: " + e.getMessage());
        }
    }

    /**
     * Enregistre un nouvel élève dans Firebase Realtime Database et le cache local.
     */
    public void registerStudent(Student student, ActionCallback callback) {
        try {
            if (student.getId() == null || student.getId().isEmpty()) {
                String newKey = null;
                if (databaseReference != null) {
                    try {
                        newKey = databaseReference.push().getKey();
                    } catch (Throwable ignored) {}
                }
                if (newKey == null) {
                    newKey = "el_" + System.currentTimeMillis();
                }
                student.setId(newKey);
            }

            if (student.getMatricule() == null || student.getMatricule().isEmpty()) {
                student.setMatricule(generateMatricule());
            }

            final Student finalStudent = student;
            updateOrAddLocal(finalStudent);

            if (databaseReference != null) {
                try {
                    databaseReference.child(finalStudent.getId()).setValue(finalStudent)
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Échec de l'écriture en ligne, sauvegardé localement: " + e.getMessage());
                                if (callback != null) callback.onSuccess();
                            });
                    return;
                } catch (Throwable t) {
                    Log.w(TAG, "Erreur push Firebase: " + t.getMessage());
                }
            }

            if (callback != null) callback.onSuccess();
        } catch (Throwable e) {
            Log.e(TAG, "Exception lors de l'inscription: " + e.getMessage());
            updateOrAddLocal(student);
            if (callback != null) callback.onSuccess();
        }
    }

    /**
     * Met à jour les informations d'un élève existant.
     */
    public void updateStudent(Student student, ActionCallback callback) {
        if (student == null || student.getId() == null) {
            if (callback != null) callback.onFailure("Identifiant d'élève invalide");
            return;
        }

        updateOrAddLocal(student);

        if (databaseReference != null) {
            try {
                databaseReference.child(student.getId()).setValue(student)
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onSuccess();
                        });
                return;
            } catch (Throwable ignored) {}
        }

        if (callback != null) callback.onSuccess();
    }

    /**
     * Supprime un élève de Firebase Realtime Database et du cache local.
     */
    public void deleteStudent(String studentId, ActionCallback callback) {
        if (studentId == null || studentId.isEmpty()) {
            if (callback != null) callback.onFailure("ID manquant");
            return;
        }

        removeLocal(studentId);

        if (databaseReference != null) {
            try {
                databaseReference.child(studentId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onSuccess();
                        });
                return;
            } catch (Throwable ignored) {}
        }

        if (callback != null) callback.onSuccess();
    }

    /**
     * Purge toutes les données.
     */
    public void clearAllStudents(ActionCallback callback) {
        if (databaseReference != null) {
            try {
                databaseReference.removeValue();
            } catch (Throwable ignored) {}
        }
        studentList.clear();
        sharedPreferences.edit().remove(KEY_CACHED_STUDENTS).apply();
        notifyListeners();
        if (callback != null) callback.onSuccess();
    }

    /**
     * Génère un numéro matricule officiel pour le lycée (ex: LYC-2026-1042).
     */
    public static String generateMatricule() {
        int randomNum = 1000 + new Random().nextInt(9000);
        return "LYC-2026-" + randomNum;
    }

    private void updateOrAddLocal(Student student) {
        boolean found = false;
        for (int i = 0; i < studentList.size(); i++) {
            if (studentList.get(i).getId().equals(student.getId())) {
                studentList.set(i, student);
                found = true;
                break;
            }
        }
        if (!found) {
            studentList.add(0, student);
        }
        saveToLocalCache();
        notifyListeners();
    }

    private void removeLocal(String studentId) {
        for (int i = 0; i < studentList.size(); i++) {
            if (studentList.get(i).getId().equals(studentId)) {
                studentList.remove(i);
                break;
            }
        }
        saveToLocalCache();
        notifyListeners();
    }

    public List<Student> getStudents() {
        return new ArrayList<>(studentList);
    }

    public void addListener(OnStudentsChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            listener.onStudentsChanged(new ArrayList<>(studentList));
        }
    }

    public void removeListener(OnStudentsChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        List<Student> copy = new ArrayList<>(studentList);
        for (OnStudentsChangedListener l : listeners) {
            l.onStudentsChanged(copy);
        }
    }

    private void notifyError(String err) {
        for (OnStudentsChangedListener l : listeners) {
            l.onError(err);
        }
    }

    /**
     * Initialise quelques élèves de test pour une expérience immédiate riche.
     */
    public void initSampleStudents() {
        if (!studentList.isEmpty()) return;

        addDemoStudent("MOUKASSA", "Jean-Pierre", "14/05/2008", "Brazzaville",
                "Masculin", "Brazzaville", "Bacongo", "15 Rue des Manguiers",
                "+242 06 612 34 56", "Terminale", "Tle D 1", "D");

        addDemoStudent("NGOUABI", "Grace Eliane", "22/11/2009", "Pointe-Noire",
                "Féminin", "Brazzaville", "Moungali", "04 Avenue de la Paix",
                "+242 05 523 88 12", "Première", "1ère C 2", "C");

        addDemoStudent("BOUITY", "Armel Nathan", "03/02/2010", "Dolisie",
                "Masculin", "Brazzaville", "Poto-Poto", "78 Rue Dahomey",
                "+242 06 441 90 23", "Seconde", "2nde A 3", "A");

        addDemoStudent("MASSAMBA", "Syntyche Diane", "19/08/2008", "Brazzaville",
                "Féminin", "Brazzaville", "Ouenze", "12 Rue Mbochis",
                "+242 05 777 45 10", "Terminale", "Tle A 2", "A");

        addDemoStudent("KIMBEMBE", "Christ-David", "30/01/2009", "Brazzaville",
                "Masculin", "Brazzaville", "Talangaï", "33 Boulevard Denis Sassou",
                "+242 06 911 22 33", "Première", "1ère D 1", "D");

        addDemoStudent("ITOUA", "Mireille Sarah", "11/09/2010", "Owando",
                "Féminin", "Brazzaville", "Makélékélé", "09 Rue Matsoua",
                "+242 05 333 66 99", "Seconde", "2nde C 1", "C");

        saveToLocalCache();
        notifyListeners();
    }

    private void addDemoStudent(String nom, String prenom, String dateNais, String lieuNais,
                                String sexe, String ville, String quartier, String adresse,
                                String tel, String niveau, String classe, String serie) {
        String key = "demo_" + System.nanoTime();
        Student s = new Student(key, generateMatricule(), nom, prenom, dateNais, lieuNais,
                sexe, ville, quartier, adresse, tel, niveau, classe, serie);
        studentList.add(s);
        if (databaseReference != null) {
            try {
                databaseReference.child(key).setValue(s);
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Sauvegarde la liste dans les préférences locales pour l'accès instantané.
     */
    private void saveToLocalCache() {
        try {
            JSONArray array = new JSONArray();
            for (Student s : studentList) {
                JSONObject obj = new JSONObject();
                obj.put("id", s.getId());
                obj.put("matricule", s.getMatricule());
                obj.put("nom", s.getNom());
                obj.put("prenom", s.getPrenom());
                obj.put("dateNaissance", s.getDateNaissance());
                obj.put("lieuNaissance", s.getLieuNaissance());
                obj.put("sexe", s.getSexe());
                obj.put("ville", s.getVille());
                obj.put("quartier", s.getQuartier());
                obj.put("adresse", s.getAdresse());
                obj.put("telephone", s.getTelephone());
                obj.put("niveau", s.getNiveau());
                obj.put("classe", s.getClasse());
                obj.put("serie", s.getSerie());
                obj.put("dateInscription", s.getDateInscription());
                array.put(obj);
            }
            sharedPreferences.edit().putString(KEY_CACHED_STUDENTS, array.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Erreur sauvegarde cache local: " + e.getMessage());
        }
    }

    /**
     * Charge les élèves depuis le cache local.
     */
    private void loadFromLocalCache() {
        try {
            String json = sharedPreferences.getString(KEY_CACHED_STUDENTS, null);
            if (json != null && !json.isEmpty()) {
                JSONArray array = new JSONArray(json);
                studentList.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Student s = new Student(
                            obj.optString("id"),
                            obj.optString("matricule"),
                            obj.optString("nom"),
                            obj.optString("prenom"),
                            obj.optString("dateNaissance"),
                            obj.optString("lieuNaissance"),
                            obj.optString("sexe"),
                            obj.optString("ville"),
                            obj.optString("quartier"),
                            obj.optString("adresse"),
                            obj.optString("telephone"),
                            obj.optString("niveau"),
                            obj.optString("classe"),
                            obj.optString("serie")
                    );
                    s.setDateInscription(obj.optLong("dateInscription", System.currentTimeMillis()));
                    studentList.add(s);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lecture cache local: " + e.getMessage());
        }
    }
}
