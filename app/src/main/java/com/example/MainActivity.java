package com.example;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.StudentAdapter;
import com.example.dialog.StudentDetailDialog;
import com.example.dialog.StudentFormDialog;
import com.example.firebase.FirebaseStudentManager;
import com.example.model.Student;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité principale gérant les onglets de l'application Lycée:
 * - Tableau de bord avec statistiques
 * - Liste complète et filtres dynamiques (Niveau & Série)
 * - Formulaire d'inscription directe avec validation
 * - Paramètres & Synchronisation Firebase
 */
public class MainActivity extends AppCompatActivity {

    private FrameLayout contentContainer;
    private FirebaseStudentManager firebaseManager;

    // Vues des onglets
    private View viewDashboard;
    private View viewStudents;
    private View viewRegistration;
    private View viewSettings;

    // Navigation items
    private LinearLayout navItemDashboard;
    private View navItemStudents;
    private LinearLayout navItemRegister;
    private LinearLayout navItemSettings;

    private ImageView navIconDashboard, navIconStudents, navIconRegister, navIconSettings;
    private TextView navLabelDashboard, navLabelStudents, navLabelRegister, navLabelSettings;
    private TextView navBadgeStudents;

    // Données des élèves
    private final List<Student> allStudents = new ArrayList<>();
    private final List<Student> filteredStudents = new ArrayList<>();
    private StudentAdapter studentAdapter;

    // Critères de filtrage
    private String selectedLevel = "Tous";
    private String selectedSerie = "Toutes";
    private String searchQuery = "";

    // Composants de l'écran des Inscrits
    private EditText etSearch;
    private ImageButton btnClearSearch;
    private TextView tvResultsCount;
    private View layoutEmptyState;

    // Composants du Tableau de bord
    private TextView tvTotalStudents, tvBoysCount, tvGirlsCount;
    private TextView tvCountSeconde, tvCountPremiere, tvCountTerminale;
    private TextView tvCountSerieA, tvCountSerieC, tvCountSerieD, tvCountSerieTI;
    private LinearProgressIndicator progSerieA, progSerieC, progSerieD, progSerieTI;

    // Index de l'onglet actif (0: Dashboard, 1: Students, 2: Register, 3: Settings)
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation du gestionnaire Firebase
        firebaseManager = FirebaseStudentManager.getInstance(this);

        initViews();
        setupNavigation();
        initTabViews();

        // Afficher l'onglet par défaut (Tableau de bord)
        selectTab(0, false);

        // Écouter les données en temps réel depuis Firebase
        observeFirebaseStudents();
    }

    private void initViews() {
        contentContainer = findViewById(R.id.main_content_container);

        navItemDashboard = findViewById(R.id.nav_item_dashboard);
        navItemStudents = findViewById(R.id.nav_item_students);
        navItemRegister = findViewById(R.id.nav_item_register);
        navItemSettings = findViewById(R.id.nav_item_settings);

        navIconDashboard = findViewById(R.id.nav_icon_dashboard);
        navIconStudents = findViewById(R.id.nav_icon_students);
        navIconRegister = findViewById(R.id.nav_icon_register);
        navIconSettings = findViewById(R.id.nav_icon_settings);

        navLabelDashboard = findViewById(R.id.nav_label_dashboard);
        navLabelStudents = findViewById(R.id.nav_label_students);
        navLabelRegister = findViewById(R.id.nav_label_register);
        navLabelSettings = findViewById(R.id.nav_label_settings);

        navBadgeStudents = findViewById(R.id.nav_badge_students);

        // Bouton Inscription rapide dans l'en-tête
        MaterialButton btnHeaderAdd = findViewById(R.id.btn_header_add);
        btnHeaderAdd.setOnClickListener(v -> {
            StudentFormDialog.show(MainActivity.this, null, (student, isEdit) -> {
                saveStudentToFirebase(student, false);
            });
        });
    }

    /**
     * Configuration des clics et des animations de la barre de navigation.
     */
    private void setupNavigation() {
        navItemDashboard.setOnClickListener(v -> animateAndSelectTab(0, navItemDashboard));
        navItemStudents.setOnClickListener(v -> animateAndSelectTab(1, navItemStudents));
        navItemRegister.setOnClickListener(v -> animateAndSelectTab(2, navItemRegister));
        navItemSettings.setOnClickListener(v -> animateAndSelectTab(3, navItemSettings));
    }

    /**
     * Déclenche une animation de rebond et de scale au clic sur un élément de navigation.
     */
    private void animateAndSelectTab(int index, View targetView) {
        if (currentTab == index) return;
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.nav_click_scale);
        targetView.startAnimation(bounceAnim);
        selectTab(index, true);
    }

    /**
     * Initialise les vues des 4 onglets principaux.
     */
    private void initTabViews() {
        LayoutInflater inflater = LayoutInflater.from(this);

        // 1. Tableau de bord
        viewDashboard = inflater.inflate(R.layout.layout_dashboard, contentContainer, false);
        setupDashboardView(viewDashboard);

        // 2. Liste des Inscrits
        viewStudents = inflater.inflate(R.layout.layout_students, contentContainer, false);
        setupStudentsView(viewStudents);

        // 3. Formulaire d'inscription
        viewRegistration = inflater.inflate(R.layout.layout_registration, contentContainer, false);
        setupRegistrationView(viewRegistration);

        // 4. Paramètres
        viewSettings = inflater.inflate(R.layout.layout_settings, contentContainer, false);
        setupSettingsView(viewSettings);

        // Ajout des vues dans le conteneur
        contentContainer.addView(viewDashboard);
        contentContainer.addView(viewStudents);
        contentContainer.addView(viewRegistration);
        contentContainer.addView(viewSettings);
    }

    /**
     * Bascule l'affichage entre les différents onglets avec transition animée.
     */
    private void selectTab(int index, boolean animate) {
        currentTab = index;

        // Visibilité des conteneurs
        viewDashboard.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        viewStudents.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        viewRegistration.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        viewSettings.setVisibility(index == 3 ? View.VISIBLE : View.GONE);

        if (animate) {
            View activeView = null;
            if (index == 0) activeView = viewDashboard;
            else if (index == 1) activeView = viewStudents;
            else if (index == 2) activeView = viewRegistration;
            else if (index == 3) activeView = viewSettings;

            if (activeView != null) {
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                activeView.startAnimation(fadeIn);
            }
        }

        // Mise à jour des couleurs et styles d'icônes
        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
        int mutedColor = ContextCompat.getColor(this, R.color.text_muted);

        navIconDashboard.setColorFilter(index == 0 ? primaryColor : mutedColor);
        navLabelDashboard.setTextColor(index == 0 ? primaryColor : mutedColor);

        navIconStudents.setColorFilter(index == 1 ? primaryColor : mutedColor);
        navLabelStudents.setTextColor(index == 1 ? primaryColor : mutedColor);

        navIconRegister.setColorFilter(index == 2 ? primaryColor : mutedColor);
        navLabelRegister.setTextColor(index == 2 ? primaryColor : mutedColor);

        navIconSettings.setColorFilter(index == 3 ? primaryColor : mutedColor);
        navLabelSettings.setTextColor(index == 3 ? primaryColor : mutedColor);
    }

    /**
     * Configuration des composants du Tableau de bord.
     */
    private void setupDashboardView(View root) {
        tvTotalStudents = root.findViewById(R.id.tv_dash_total_students);
        tvBoysCount = root.findViewById(R.id.tv_dash_boys);
        tvGirlsCount = root.findViewById(R.id.tv_dash_girls);

        tvCountSeconde = root.findViewById(R.id.tv_dash_count_seconde);
        tvCountPremiere = root.findViewById(R.id.tv_dash_count_premiere);
        tvCountTerminale = root.findViewById(R.id.tv_dash_count_terminale);

        tvCountSerieA = root.findViewById(R.id.tv_count_serie_a);
        tvCountSerieC = root.findViewById(R.id.tv_count_serie_c);
        tvCountSerieD = root.findViewById(R.id.tv_count_serie_d);
        tvCountSerieTI = root.findViewById(R.id.tv_count_serie_ti);

        progSerieA = root.findViewById(R.id.progress_serie_a);
        progSerieC = root.findViewById(R.id.progress_serie_c);
        progSerieD = root.findViewById(R.id.progress_serie_d);
        progSerieTI = root.findViewById(R.id.progress_serie_ti);

        // Clic sur les cartes de filtre rapide
        root.findViewById(R.id.card_filter_seconde).setOnClickListener(v -> {
            selectedLevel = "Seconde";
            applyFilters();
            selectTab(1, true);
        });

        root.findViewById(R.id.card_filter_premiere).setOnClickListener(v -> {
            selectedLevel = "Première";
            applyFilters();
            selectTab(1, true);
        });

        root.findViewById(R.id.card_filter_terminale).setOnClickListener(v -> {
            selectedLevel = "Terminale";
            applyFilters();
            selectTab(1, true);
        });

        // Boutons d'actions
        MaterialButton btnNewReg = root.findViewById(R.id.btn_dash_new_registration);
        btnNewReg.setOnClickListener(v -> selectTab(2, true));

        MaterialButton btnViewAll = root.findViewById(R.id.btn_dash_view_all);
        btnViewAll.setOnClickListener(v -> {
            selectedLevel = "Tous";
            selectedSerie = "Toutes";
            applyFilters();
            selectTab(1, true);
        });
    }

    /**
     * Configuration des composants de la liste des inscrits et des filtres.
     */
    private void setupStudentsView(View root) {
        etSearch = root.findViewById(R.id.et_search_student);
        btnClearSearch = root.findViewById(R.id.btn_clear_search);
        tvResultsCount = root.findViewById(R.id.tv_results_count);
        layoutEmptyState = root.findViewById(R.id.layout_empty_state);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_students);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentAdapter = new StudentAdapter(this, new StudentAdapter.OnStudentActionListener() {
            @Override
            public void onDetail(Student student) {
                StudentDetailDialog.show(MainActivity.this, student, new StudentDetailDialog.OnDetailActionListener() {
                    @Override
                    public void onEdit(Student s) {
                        StudentFormDialog.show(MainActivity.this, s, (updated, isEdit) -> {
                            saveStudentToFirebase(updated, isEdit);
                        });
                    }

                    @Override
                    public void onDelete(Student s) {
                        confirmDeleteStudent(s);
                    }
                });
            }

            @Override
            public void onCall(Student student) {
                if (student.getTelephone() != null && !student.getTelephone().isEmpty()) {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + student.getTelephone().replaceAll("[^0-9+]", "")));
                        startActivity(callIntent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Erreur appel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onEdit(Student student) {
                StudentFormDialog.show(MainActivity.this, student, (updated, isEdit) -> {
                    saveStudentToFirebase(updated, isEdit);
                });
            }

            @Override
            public void onDelete(Student student) {
                confirmDeleteStudent(student);
            }
        });

        recyclerView.setAdapter(studentAdapter);

        // Recherche en direct
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                btnClearSearch.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));

        // Filtre par Niveau
        ChipGroup chipGroupLevels = root.findViewById(R.id.chip_group_levels);
        chipGroupLevels.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedLevel = "Tous";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chip_level_seconde) selectedLevel = "Seconde";
                else if (id == R.id.chip_level_premiere) selectedLevel = "Première";
                else if (id == R.id.chip_level_terminale) selectedLevel = "Terminale";
                else selectedLevel = "Tous";
            }
            applyFilters();
        });

        // Filtre par Série
        ChipGroup chipGroupSeries = root.findViewById(R.id.chip_group_series);
        chipGroupSeries.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedSerie = "Toutes";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chip_serie_a) selectedSerie = "A";
                else if (id == R.id.chip_serie_c) selectedSerie = "C";
                else if (id == R.id.chip_serie_d) selectedSerie = "D";
                else if (id == R.id.chip_serie_ti) selectedSerie = "TI";
                else selectedSerie = "Toutes";
            }
            applyFilters();
        });
    }

    /**
     * Configuration du formulaire d'inscription dans l'onglet Inscription.
     */
    private void setupRegistrationView(View root) {
        final TextInputLayout tilNom = root.findViewById(R.id.til_nom);
        final TextInputEditText etNom = root.findViewById(R.id.et_nom);
        final TextInputLayout tilPrenom = root.findViewById(R.id.til_prenom);
        final TextInputEditText etPrenom = root.findViewById(R.id.et_prenom);
        final TextInputLayout tilDateNais = root.findViewById(R.id.til_date_naissance);
        final TextInputEditText etDateNais = root.findViewById(R.id.et_date_naissance);
        final TextInputLayout tilLieuNais = root.findViewById(R.id.til_lieu_naissance);
        final TextInputEditText etLieuNais = root.findViewById(R.id.et_lieu_naissance);
        final RadioGroup rgSexe = root.findViewById(R.id.rg_sexe);
        final RadioButton rbMasc = root.findViewById(R.id.rb_sexe_masculin);
        final RadioButton rbFem = root.findViewById(R.id.rb_sexe_feminin);
        final TextInputLayout tilVille = root.findViewById(R.id.til_ville);
        final TextInputEditText etVille = root.findViewById(R.id.et_ville);
        final TextInputLayout tilQuartier = root.findViewById(R.id.til_quartier);
        final TextInputEditText etQuartier = root.findViewById(R.id.et_quartier);
        final TextInputLayout tilAdresse = root.findViewById(R.id.til_adresse);
        final TextInputEditText etAdresse = root.findViewById(R.id.et_adresse);
        final TextInputLayout tilTelephone = root.findViewById(R.id.til_telephone);
        final TextInputEditText etTelephone = root.findViewById(R.id.et_telephone);

        final AutoCompleteTextView actNiveau = root.findViewById(R.id.act_niveau);
        final AutoCompleteTextView actSerie = root.findViewById(R.id.act_serie);
        final AutoCompleteTextView actClasse = root.findViewById(R.id.act_classe);

        final MaterialButton btnSubmit = root.findViewById(R.id.btn_submit_student);
        final MaterialButton btnReset = root.findViewById(R.id.btn_reset_form);

        final String[] niveaux = {"Seconde", "Première", "Terminale"};
        final String[] series = {"A", "C", "D", "TI"};
        final String[] classes = {
                "2nde A 1", "2nde A 2", "2nde C 1", "2nde C 2",
                "1ère A 1", "1ère A 2", "1ère C 1", "1ère D 1", "1ère D 2", "1ère TI",
                "Tle A 1", "Tle A 2", "Tle C 1", "Tle D 1", "Tle D 2", "Tle TI"
        };

        actNiveau.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, niveaux));
        actSerie.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, series));
        actClasse.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classes));

        actNiveau.setText("Seconde", false);
        actSerie.setText("A", false);
        actClasse.setText("2nde A 1", false);
        etVille.setText("Brazzaville");

        etDateNais.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int year = cal.get(java.util.Calendar.YEAR) - 16;
            int month = cal.get(java.util.Calendar.MONTH);
            int day = cal.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog dpd = new android.app.DatePickerDialog(this, (datePicker, y, m, d) -> {
                String formatted = String.format("%02d/%02d/%04d", d, (m + 1), y);
                etDateNais.setText(formatted);
                tilDateNais.setError(null);
            }, year, month, day);
            dpd.show();
        });

        btnReset.setOnClickListener(v -> {
            etNom.setText("");
            etPrenom.setText("");
            etDateNais.setText("");
            etLieuNais.setText("");
            etQuartier.setText("");
            etAdresse.setText("");
            etTelephone.setText("");
            rbMasc.setChecked(true);
            tilNom.setError(null);
            tilPrenom.setError(null);
            tilTelephone.setError(null);
        });

        btnSubmit.setOnClickListener(v -> {
            String nom = etNom.getText() != null ? etNom.getText().toString().trim() : "";
            String prenom = etPrenom.getText() != null ? etPrenom.getText().toString().trim() : "";
            String dateNais = etDateNais.getText() != null ? etDateNais.getText().toString().trim() : "";
            String lieuNais = etLieuNais.getText() != null ? etLieuNais.getText().toString().trim() : "";
            String villeVal = etVille.getText() != null ? etVille.getText().toString().trim() : "";
            String quartierVal = etQuartier.getText() != null ? etQuartier.getText().toString().trim() : "";
            String adresseVal = etAdresse.getText() != null ? etAdresse.getText().toString().trim() : "";
            String telephoneVal = etTelephone.getText() != null ? etTelephone.getText().toString().trim() : "";
            String niveauVal = actNiveau.getText().toString().trim();
            String serieVal = actSerie.getText().toString().trim();
            String classeVal = actClasse.getText().toString().trim();
            String sexeVal = rbFem.isChecked() ? "Féminin" : "Masculin";

            boolean hasError = false;
            if (nom.isEmpty()) {
                tilNom.setError("Le nom de famille est obligatoire");
                hasError = true;
            } else {
                tilNom.setError(null);
            }

            if (prenom.isEmpty()) {
                tilPrenom.setError("Le prénom est obligatoire");
                hasError = true;
            } else {
                tilPrenom.setError(null);
            }

            if (telephoneVal.isEmpty()) {
                tilTelephone.setError("Le téléphone est obligatoire");
                hasError = true;
            } else {
                tilTelephone.setError(null);
            }

            if (hasError) {
                Toast.makeText(this, "Veuillez renseigner les champs obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            Student s = new Student();
            s.setNom(nom);
            s.setPrenom(prenom);
            s.setDateNaissance(dateNais);
            s.setLieuNaissance(lieuNais);
            s.setSexe(sexeVal);
            s.setVille(villeVal);
            s.setQuartier(quartierVal);
            s.setAdresse(adresseVal);
            s.setTelephone(telephoneVal);
            s.setNiveau(niveauVal.isEmpty() ? "Seconde" : niveauVal);
            s.setSerie(serieVal.isEmpty() ? "A" : serieVal);
            s.setClasse(classeVal.isEmpty() ? "2nde A 1" : classeVal);

            saveStudentToFirebase(s, false);

            // Réinitialiser les champs et basculer sur la liste
            btnReset.performClick();
            selectTab(1, true);
        });
    }

    /**
     * Configuration de l'onglet Paramètres (thème sombre, purge, démo).
     */
    private void setupSettingsView(View root) {
        MaterialSwitch switchDarkMode = root.findViewById(R.id.switch_dark_mode);
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("is_dark_mode", false);
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("is_dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Statut Firebase & rafraîchissement
        TextView tvFirebaseStatus = root.findViewById(R.id.tv_settings_firebase_status);
        if (tvFirebaseStatus != null) {
            if (firebaseManager.isFirebaseAvailable()) {
                tvFirebaseStatus.setText(R.string.firebase_status_connected);
                tvFirebaseStatus.setTextColor(ContextCompat.getColor(this, R.color.success_color));
            } else {
                tvFirebaseStatus.setText("Mode local actif & persistant");
                tvFirebaseStatus.setTextColor(ContextCompat.getColor(this, R.color.colorSecondary));
            }
        }

        View btnRefresh = root.findViewById(R.id.btn_refresh_firebase);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                Animation spin = AnimationUtils.loadAnimation(this, R.anim.nav_click_scale);
                btnRefresh.startAnimation(spin);
                observeFirebaseStudents();
                Toast.makeText(this, "Données actualisées avec succès", Toast.LENGTH_SHORT).show();
            });
        }

        // Charger des élèves démos
        root.findViewById(R.id.layout_load_demo).setOnClickListener(v -> {
            firebaseManager.initSampleStudents();
            Toast.makeText(this, "Élèves exemples initialisés !", Toast.LENGTH_SHORT).show();
            selectTab(1, true);
        });

        // Purger les données
        root.findViewById(R.id.layout_clear_data).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Purger les inscriptions")
                    .setMessage("Voulez-vous supprimer toutes les inscriptions d'élèves ?")
                    .setPositiveButton("Purger", (dialog, which) -> {
                        firebaseManager.clearAllStudents(new FirebaseStudentManager.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(MainActivity.this, "Toutes les données ont été effacées", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String error) {}
                        });
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    /**
     * Écoute en temps réel les changements d'élèves depuis Firebase Realtime Database.
     */
    private void observeFirebaseStudents() {
        firebaseManager.addListener(new FirebaseStudentManager.OnStudentsChangedListener() {
            @Override
            public void onStudentsChanged(List<Student> students) {
                allStudents.clear();
                allStudents.addAll(students);
                applyFilters();
                updateDashboardStats();

                // Mise à jour de la pastille du bas de navigation
                if (allStudents.isEmpty()) {
                    navBadgeStudents.setVisibility(View.GONE);
                } else {
                    navBadgeStudents.setVisibility(View.VISIBLE);
                    navBadgeStudents.setText(String.valueOf(allStudents.size()));
                }
            }

            @Override
            public void onError(String errorMessage) {}
        });
    }

    /**
     * Applique les filtres en mémoire (Niveau, Série et Recherche texte).
     */
    private void applyFilters() {
        filteredStudents.clear();
        String query = searchQuery.toLowerCase();

        for (Student s : allStudents) {
            // Filtre par Niveau
            boolean levelMatch = "Tous".equalsIgnoreCase(selectedLevel)
                    || (s.getNiveau() != null && s.getNiveau().equalsIgnoreCase(selectedLevel));

            // Filtre par Série
            boolean serieMatch = "Toutes".equalsIgnoreCase(selectedSerie)
                    || (s.getSerie() != null && s.getSerie().equalsIgnoreCase(selectedSerie));

            // Filtre par recherche textuelle (Nom, Prénom, Classe, Matricule)
            boolean searchMatch = true;
            if (!query.isEmpty()) {
                String fullName = s.getNomComplet().toLowerCase();
                String classe = s.getClasse() != null ? s.getClasse().toLowerCase() : "";
                String matricule = s.getMatricule() != null ? s.getMatricule().toLowerCase() : "";
                searchMatch = fullName.contains(query) || classe.contains(query) || matricule.contains(query);
            }

            if (levelMatch && serieMatch && searchMatch) {
                filteredStudents.add(s);
            }
        }

        if (studentAdapter != null) {
            studentAdapter.updateList(filteredStudents);
        }

        if (tvResultsCount != null) {
            tvResultsCount.setText(filteredStudents.size() + " élève(s) inscrit(s)");
        }

        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(filteredStudents.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Met à jour les compteurs et jauges du Tableau de bord.
     */
    private void updateDashboardStats() {
        int total = allStudents.size();
        int boys = 0;
        int girls = 0;
        int countSec = 0, countPrem = 0, countTerm = 0;
        int serieA = 0, serieC = 0, serieD = 0, serieTI = 0;

        for (Student s : allStudents) {
            if ("Masculin".equalsIgnoreCase(s.getSexe())) {
                boys++;
            } else {
                girls++;
            }

            String niv = s.getNiveau() != null ? s.getNiveau() : "";
            if ("Seconde".equalsIgnoreCase(niv)) countSec++;
            else if ("Première".equalsIgnoreCase(niv)) countPrem++;
            else if ("Terminale".equalsIgnoreCase(niv)) countTerm++;

            String ser = s.getSerie() != null ? s.getSerie().toUpperCase() : "";
            if ("A".equals(ser)) serieA++;
            else if ("C".equals(ser)) serieC++;
            else if ("D".equals(ser)) serieD++;
            else serieTI++;
        }

        if (tvTotalStudents != null) tvTotalStudents.setText(String.valueOf(total));
        if (tvBoysCount != null) tvBoysCount.setText(String.valueOf(boys));
        if (tvGirlsCount != null) tvGirlsCount.setText(String.valueOf(girls));

        if (tvCountSeconde != null) tvCountSeconde.setText(String.valueOf(countSec));
        if (tvCountPremiere != null) tvCountPremiere.setText(String.valueOf(countPrem));
        if (tvCountTerminale != null) tvCountTerminale.setText(String.valueOf(countTerm));

        if (tvCountSerieA != null) tvCountSerieA.setText(String.valueOf(serieA));
        if (tvCountSerieC != null) tvCountSerieC.setText(String.valueOf(serieC));
        if (tvCountSerieD != null) tvCountSerieD.setText(String.valueOf(serieD));
        if (tvCountSerieTI != null) tvCountSerieTI.setText(String.valueOf(serieTI));

        int max = Math.max(total, 1);
        if (progSerieA != null) progSerieA.setProgress((int) ((serieA * 100.0) / max));
        if (progSerieC != null) progSerieC.setProgress((int) ((serieC * 100.0) / max));
        if (progSerieD != null) progSerieD.setProgress((int) ((serieD * 100.0) / max));
        if (progSerieTI != null) progSerieTI.setProgress((int) ((serieTI * 100.0) / max));
    }

    /**
     * Enregistre ou met à jour un élève dans Firebase.
     */
    private void saveStudentToFirebase(Student student, boolean isEdit) {
        if (isEdit) {
            firebaseManager.updateStudent(student, new FirebaseStudentManager.ActionCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, R.string.success_updated, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(MainActivity.this, "Erreur mise à jour: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            firebaseManager.registerStudent(student, new FirebaseStudentManager.ActionCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, R.string.success_registered, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(MainActivity.this, "Erreur enregistrement: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Boîte de dialogue de confirmation de suppression d'un élève.
     */
    private void confirmDeleteStudent(Student student) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage("Voulez-vous supprimer le dossier de " + student.getNomComplet() + " ?")
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    firebaseManager.deleteStudent(student.getId(), new FirebaseStudentManager.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this, R.string.success_deleted, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(MainActivity.this, "Erreur suppression: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}
