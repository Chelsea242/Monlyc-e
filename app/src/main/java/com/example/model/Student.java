package com.example.model;

import java.io.Serializable;

/**
 * Modèle de données représentant un élève du lycée.
 * Utilisé pour la sérialisation et la désérialisation avec Firebase Realtime Database.
 */
public class Student implements Serializable {

    private String id;              // Clé unique Firebase
    private String matricule;       // Numéro matricule unique (ex: LYC-2026-0042)
    private String nom;             // Nom de famille
    private String prenom;          // Prénom(s)
    private String dateNaissance;   // Date de naissance (JJ/MM/AAAA)
    private String lieuNaissance;   // Lieu de naissance (ex: Brazzaville)
    private String sexe;            // Sexe : "Masculin" ou "Féminin"
    private String ville;           // Ville de résidence
    private String quartier;        // Quartier
    private String adresse;         // Adresse complète
    private String telephone;       // Numéro de téléphone de contact
    private String niveau;          // Niveau : "Seconde", "Première", "Terminale"
    private String classe;          // Classe spécifique : "2nde A", "1ère D", "Tle C", etc.
    private String serie;           // Série : "A", "C", "D", "TI", etc.
    private long dateInscription;   // Timestamp de l'inscription

    /**
     * Constructeur par défaut sans argument indispensable pour Firebase Realtime Database.
     */
    public Student() {
        this.dateInscription = System.currentTimeMillis();
    }

    /**
     * Constructeur complet avec tous les champs requis.
     */
    public Student(String id, String matricule, String nom, String prenom,
                   String dateNaissance, String lieuNaissance, String sexe,
                   String ville, String quartier, String adresse,
                   String telephone, String niveau, String classe, String serie) {
        this.id = id;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.lieuNaissance = lieuNaissance;
        this.sexe = sexe;
        this.ville = ville;
        this.quartier = quartier;
        this.adresse = adresse;
        this.telephone = telephone;
        this.niveau = niveau;
        this.classe = classe;
        this.serie = serie;
        this.dateInscription = System.currentTimeMillis();
    }

    // --- Getters et Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public long getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(long dateInscription) {
        this.dateInscription = dateInscription;
    }

    /**
     * Retourne le nom complet formaté (NOM Prénom).
     */
    public String getNomComplet() {
        String n = nom != null ? nom.toUpperCase() : "";
        String p = prenom != null ? prenom : "";
        return (n + " " + p).trim();
    }

    /**
     * Génère les initiales pour l'avatar visuel (ex: "MJ" pour Moukassa Jean).
     */
    public String getInitiales() {
        StringBuilder sb = new StringBuilder();
        if (nom != null && !nom.trim().isEmpty()) {
            sb.append(nom.trim().charAt(0));
        }
        if (prenom != null && !prenom.trim().isEmpty()) {
            sb.append(prenom.trim().charAt(0));
        }
        return sb.length() > 0 ? sb.toString().toUpperCase() : "EL";
    }
}
