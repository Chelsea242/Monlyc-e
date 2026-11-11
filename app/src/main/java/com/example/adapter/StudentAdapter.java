package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.model.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptateur RecyclerView pour l'affichage élégant des élèves inscrits.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentActionListener {
        void onDetail(Student student);
        void onCall(Student student);
        void onEdit(Student student);
        void onDelete(Student student);
    }

    private final Context context;
    private final List<Student> studentList = new ArrayList<>();
    private final OnStudentActionListener listener;

    public StudentAdapter(Context context, OnStudentActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateList(List<Student> newList) {
        studentList.clear();
        if (newList != null) {
            studentList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.bind(student, listener);
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {

        private final View cardRoot;
        private final TextView tvAvatarInitials;
        private final TextView tvStudentName;
        private final TextView tvStudentMatricule;
        private final TextView tvBadgeNiveau;
        private final TextView tvBadgeSerie;
        private final TextView tvBadgeClasse;
        private final TextView tvBadgeSexe;
        private final TextView tvStudentLocation;
        private final TextView tvStudentPhone;
        private final ImageButton btnQuickCall;
        private final ImageButton btnQuickDelete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.card_student_root);
            tvAvatarInitials = itemView.findViewById(R.id.tv_avatar_initials);
            tvStudentName = itemView.findViewById(R.id.tv_student_name);
            tvStudentMatricule = itemView.findViewById(R.id.tv_student_matricule);
            tvBadgeNiveau = itemView.findViewById(R.id.tv_badge_niveau);
            tvBadgeSerie = itemView.findViewById(R.id.tv_badge_serie);
            tvBadgeClasse = itemView.findViewById(R.id.tv_badge_classe);
            tvBadgeSexe = itemView.findViewById(R.id.tv_badge_sexe);
            tvStudentLocation = itemView.findViewById(R.id.tv_student_location);
            tvStudentPhone = itemView.findViewById(R.id.tv_student_phone);
            btnQuickCall = itemView.findViewById(R.id.btn_quick_call);
            btnQuickDelete = itemView.findViewById(R.id.btn_quick_delete);
        }

        public void bind(final Student student, final OnStudentActionListener listener) {
            tvAvatarInitials.setText(student.getInitiales());
            tvStudentName.setText(student.getNomComplet());
            tvStudentMatricule.setText("Matricule : " + (student.getMatricule() != null ? student.getMatricule() : "—"));

            // Niveau (Seconde, Première, Terminale)
            String niveau = student.getNiveau() != null ? student.getNiveau() : "Lycée";
            tvBadgeNiveau.setText(niveau);

            // Série (A, C, D, etc.)
            String serie = student.getSerie() != null ? student.getSerie() : "Générale";
            tvBadgeSerie.setText("Série " + serie);

            // Classe (2nde A, 1ère D, etc.)
            String classe = student.getClasse() != null ? student.getClasse() : "—";
            tvBadgeClasse.setText(classe);

            // Sexe
            boolean isMale = "Masculin".equalsIgnoreCase(student.getSexe());
            tvBadgeSexe.setText(isMale ? "♂ Masc." : "♀ Fém.");

            // Localisation
            String ville = student.getVille() != null ? student.getVille() : "";
            String quartier = student.getQuartier() != null ? student.getQuartier() : "";
            String loc = ville;
            if (!quartier.isEmpty()) {
                loc = loc.isEmpty() ? quartier : (loc + ", " + quartier);
            }
            tvStudentLocation.setText(loc.isEmpty() ? "Non renseignée" : loc);

            // Téléphone
            String tel = student.getTelephone() != null ? student.getTelephone() : "—";
            tvStudentPhone.setText(tel);

            // Clic sur toute la carte pour afficher la fiche complète
            cardRoot.setOnClickListener(v -> {
                if (listener != null) listener.onDetail(student);
            });

            // Clic sur l'appel rapide
            btnQuickCall.setOnClickListener(v -> {
                if (listener != null) listener.onCall(student);
            });

            // Clic sur la suppression rapide
            btnQuickDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(student);
            });
        }
    }
}
