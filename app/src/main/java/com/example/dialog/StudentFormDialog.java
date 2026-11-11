package com.example.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.R;
import com.example.model.Student;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * Boîte de dialogue animée pour l'inscription ou la modification d'un élève.
 * Utilise les animations d'entrée et de sortie personnalisées.
 */
public class StudentFormDialog {

    public interface OnFormSubmitListener {
        void onSubmit(Student student, boolean isEdit);
    }

    public static void show(Context context, final Student existingStudent, final OnFormSubmitListener listener) {
        final boolean isEdit = existingStudent != null;
        final Dialog dialog = new Dialog(context, R.style.CustomDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_registration, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        // Récupération des composants du formulaire
        TextView tvTitle = view.findViewById(R.id.tv_form_title);
        final TextInputLayout tilNom = view.findViewById(R.id.til_nom);
        final TextInputEditText etNom = view.findViewById(R.id.et_nom);
        final TextInputLayout tilPrenom = view.findViewById(R.id.til_prenom);
        final TextInputEditText etPrenom = view.findViewById(R.id.et_prenom);
        final TextInputLayout tilDateNais = view.findViewById(R.id.til_date_naissance);
        final TextInputEditText etDateNais = view.findViewById(R.id.et_date_naissance);
        final TextInputLayout tilLieuNais = view.findViewById(R.id.til_lieu_naissance);
        final TextInputEditText etLieuNais = view.findViewById(R.id.et_lieu_naissance);
        final RadioGroup rgSexe = view.findViewById(R.id.rg_sexe);
        final RadioButton rbMasc = view.findViewById(R.id.rb_sexe_masculin);
        final RadioButton rbFem = view.findViewById(R.id.rb_sexe_feminin);
        final TextInputLayout tilVille = view.findViewById(R.id.til_ville);
        final TextInputEditText etVille = view.findViewById(R.id.et_ville);
        final TextInputLayout tilQuartier = view.findViewById(R.id.til_quartier);
        final TextInputEditText etQuartier = view.findViewById(R.id.et_quartier);
        final TextInputLayout tilAdresse = view.findViewById(R.id.til_adresse);
        final TextInputEditText etAdresse = view.findViewById(R.id.et_adresse);
        final TextInputLayout tilTelephone = view.findViewById(R.id.til_telephone);
        final TextInputEditText etTelephone = view.findViewById(R.id.et_telephone);

        final AutoCompleteTextView actNiveau = view.findViewById(R.id.act_niveau);
        final AutoCompleteTextView actSerie = view.findViewById(R.id.act_serie);
        final AutoCompleteTextView actClasse = view.findViewById(R.id.act_classe);

        final MaterialButton btnSubmit = view.findViewById(R.id.btn_submit_student);
        final MaterialButton btnReset = view.findViewById(R.id.btn_reset_form);

        // Configuration des dropdowns
        final String[] niveaux = {"Seconde", "Première", "Terminale"};
        final String[] series = {"A", "C", "D", "TI"};
        final String[] classes = {
                "2nde A 1", "2nde A 2", "2nde C 1", "2nde C 2",
                "1ère A 1", "1ère A 2", "1ère C 1", "1ère D 1", "1ère D 2", "1ère TI",
                "Tle A 1", "Tle A 2", "Tle C 1", "Tle D 1", "Tle D 2", "Tle TI"
        };

        actNiveau.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, niveaux));
        actSerie.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, series));
        actClasse.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, classes));

        // Date picker interactif
        etDateNais.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR) - 16; // Lycéen ~16 ans par défaut
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(context, (datePicker, y, m, d) -> {
                String formatted = String.format("%02d/%02d/%04d", d, (m + 1), y);
                etDateNais.setText(formatted);
                tilDateNais.setError(null);
            }, year, month, day);
            dpd.show();
        });

        // Pré-remplissage en mode modification
        if (isEdit) {
            tvTitle.setText(R.string.title_edit_student);
            btnSubmit.setText(R.string.btn_save_changes);
            btnReset.setText(R.string.btn_cancel);

            etNom.setText(existingStudent.getNom());
            etPrenom.setText(existingStudent.getPrenom());
            etDateNais.setText(existingStudent.getDateNaissance());
            etLieuNais.setText(existingStudent.getLieuNaissance());
            if ("Féminin".equalsIgnoreCase(existingStudent.getSexe())) {
                rbFem.setChecked(true);
            } else {
                rbMasc.setChecked(true);
            }
            etVille.setText(existingStudent.getVille());
            etQuartier.setText(existingStudent.getQuartier());
            etAdresse.setText(existingStudent.getAdresse());
            etTelephone.setText(existingStudent.getTelephone());
            actNiveau.setText(existingStudent.getNiveau(), false);
            actSerie.setText(existingStudent.getSerie(), false);
            actClasse.setText(existingStudent.getClasse(), false);
        } else {
            // Valeurs par défaut pour une nouvelle inscription
            actNiveau.setText("Seconde", false);
            actSerie.setText("A", false);
            actClasse.setText("2nde A 1", false);
            etVille.setText("Brazzaville");
            btnReset.setText("Fermer");
        }

        // Clic sur annuler / fermer
        btnReset.setOnClickListener(v -> dialog.dismiss());

        // Validation et soumission du formulaire
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

            // Vérifications des champs obligatoires
            boolean hasError = false;
            if (TextUtils.isEmpty(nom)) {
                tilNom.setError("Le nom de famille est obligatoire");
                hasError = true;
            } else {
                tilNom.setError(null);
            }

            if (TextUtils.isEmpty(prenom)) {
                tilPrenom.setError("Le prénom est obligatoire");
                hasError = true;
            } else {
                tilPrenom.setError(null);
            }

            if (TextUtils.isEmpty(telephoneVal)) {
                tilTelephone.setError("Le numéro de téléphone est obligatoire");
                hasError = true;
            } else if (telephoneVal.length() < 6) {
                tilTelephone.setError("Numéro de téléphone trop court");
                hasError = true;
            } else {
                tilTelephone.setError(null);
            }

            if (TextUtils.isEmpty(niveauVal)) {
                niveauVal = "Seconde";
            }
            if (TextUtils.isEmpty(serieVal)) {
                serieVal = "A";
            }
            if (TextUtils.isEmpty(classeVal)) {
                classeVal = "2nde A 1";
            }

            if (hasError) {
                Toast.makeText(context, "Veuillez corriger les erreurs dans le formulaire", Toast.LENGTH_SHORT).show();
                return;
            }

            Student resultStudent;
            if (isEdit) {
                resultStudent = existingStudent;
            } else {
                resultStudent = new Student();
            }

            resultStudent.setNom(nom);
            resultStudent.setPrenom(prenom);
            resultStudent.setDateNaissance(dateNais);
            resultStudent.setLieuNaissance(lieuNais);
            resultStudent.setSexe(sexeVal);
            resultStudent.setVille(villeVal);
            resultStudent.setQuartier(quartierVal);
            resultStudent.setAdresse(adresseVal);
            resultStudent.setTelephone(telephoneVal);
            resultStudent.setNiveau(niveauVal);
            resultStudent.setSerie(serieVal);
            resultStudent.setClasse(classeVal);

            dialog.dismiss();
            if (listener != null) {
                listener.onSubmit(resultStudent, isEdit);
            }
        });

        dialog.show();
    }
}
