package com.example.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.R;
import com.example.model.Student;
import com.google.android.material.button.MaterialButton;

/**
 * Boîte de dialogue animée affichant la fiche détaillée complète d'un élève.
 */
public class StudentDetailDialog {

    public interface OnDetailActionListener {
        void onEdit(Student student);
        void onDelete(Student student);
    }

    public static void show(Context context, Student student, OnDetailActionListener listener) {
        if (student == null) return;

        final Dialog dialog = new Dialog(context, R.style.CustomDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_student_detail, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        // Vues de la fiche
        TextView tvAvatar = view.findViewById(R.id.tv_detail_avatar);
        TextView tvFullName = view.findViewById(R.id.tv_detail_full_name);
        TextView tvMatricule = view.findViewById(R.id.tv_detail_matricule);
        TextView tvNiveau = view.findViewById(R.id.tv_detail_niveau);
        TextView tvSerie = view.findViewById(R.id.tv_detail_serie);
        TextView tvClasse = view.findViewById(R.id.tv_detail_classe);
        TextView tvBirth = view.findViewById(R.id.tv_detail_birth);
        TextView tvSexe = view.findViewById(R.id.tv_detail_sexe);
        TextView tvLocation = view.findViewById(R.id.tv_detail_location);
        TextView tvAddress = view.findViewById(R.id.tv_detail_address);
        TextView tvPhone = view.findViewById(R.id.tv_detail_phone);

        ImageButton btnClose = view.findViewById(R.id.btn_dialog_close);
        MaterialButton btnCall = view.findViewById(R.id.btn_dialog_call);
        MaterialButton btnEdit = view.findViewById(R.id.btn_dialog_edit);
        MaterialButton btnDelete = view.findViewById(R.id.btn_dialog_delete);

        // Remplissage des données
        tvAvatar.setText(student.getInitiales());
        tvFullName.setText(student.getNomComplet());
        tvMatricule.setText("Matricule : " + (student.getMatricule() != null ? student.getMatricule() : "—"));
        tvNiveau.setText(student.getNiveau() != null ? student.getNiveau() : "Lycée");
        tvSerie.setText("Série " + (student.getSerie() != null ? student.getSerie() : "—"));
        tvClasse.setText("Classe : " + (student.getClasse() != null ? student.getClasse() : "—"));

        String dateNais = student.getDateNaissance() != null && !student.getDateNaissance().isEmpty() ? student.getDateNaissance() : "Non précisée";
        String lieuNais = student.getLieuNaissance() != null && !student.getLieuNaissance().isEmpty() ? student.getLieuNaissance() : "Non précisé";
        tvBirth.setText("Né(e) le " + dateNais + " à " + lieuNais);

        tvSexe.setText("Sexe : " + (student.getSexe() != null ? student.getSexe() : "Non spécifié"));

        String ville = student.getVille() != null ? student.getVille() : "—";
        String quartier = student.getQuartier() != null ? student.getQuartier() : "—";
        tvLocation.setText("Ville : " + ville + " • Quartier : " + quartier);

        String adresse = student.getAdresse() != null && !student.getAdresse().isEmpty() ? student.getAdresse() : "Non précisée";
        tvAddress.setText("Adresse : " + adresse);

        String tel = student.getTelephone() != null ? student.getTelephone() : "Non renseigné";
        tvPhone.setText("Téléphone : " + tel);

        // Actions
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnCall.setOnClickListener(v -> {
            if (student.getTelephone() != null && !student.getTelephone().trim().isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + student.getTelephone().replaceAll("[^0-9+]", "")));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Impossible d'ouvrir l'application d'appel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Numéro de téléphone manquant", Toast.LENGTH_SHORT).show();
            }
        });

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onEdit(student);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) listener.onDelete(student);
        });

        dialog.show();
    }
}
