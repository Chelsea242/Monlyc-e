# MyLycée - Application de Gestion des Inscriptions Scolaires

Application Android native développée pour les lycées, permettant la gestion complète des élèves, des inscriptions et le suivi statistique.

## Fonctionnalités

- **Tableau de bord interactif** : Visualisation du nombre total d'élèves, répartition par filière/niveau, statut des paiements de scolarité.
- **Gestion des élèves** :
  - Inscription complète d'un nouvel élève (informations personnelles, classe, contact tuteur, etc.).
  - Liste filtrable et recherche rapide en temps réel.
  - Consultation de la fiche détaillée, modification et suppression.
- **Persistance hybride & Résilience** :
  - Support de synchronisation Firebase Realtime Database.
  - Mode local hors-ligne automatique (cache persistant) permettant un fonctionnement complet même sans connexion internet ou sans configuration Firebase immédiate.
- **Interface moderne** : Conçue selon les standards Material Design pour Android.

## Structure du projet

- `app/src/main/java/com/example/` :
  - `MainActivity.java` : Tableau de bord et navigation principale.
  - `SplashActivity.java` : Écran d'accueil avec transition fluide.
  - `StudentListActivity.java` / `StudentDetailActivity.java` : Consultation et gestion des élèves.
  - `AddStudentActivity.java` / `EditStudentActivity.java` : Formulaires d'ajout et d'édition.
  - `FirebaseStudentManager.java` : Couche de données (Firebase + cache local persistant).
- `app/src/main/res/` : Ressources UI, thèmes, layouts XML et icônes.

## Prérequis et compilation

- Android Studio Flamingo ou supérieur (recommandé).
- JDK 17+.
- Android SDK avec `compileSdk = 34`.

### Lancer le projet :
1. Cloner le dépôt :
   ```bash
   git clone https://github.com/VOTRE_UTILISATEUR/mylycee-android.git
   ```
2. Ouvrir le dossier dans Android Studio.
3. Synchroniser Gradle et cliquer sur **Run**.
