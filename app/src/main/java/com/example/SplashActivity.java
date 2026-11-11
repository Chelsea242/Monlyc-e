package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebase.FirebaseStudentManager;
import com.google.android.material.card.MaterialCardView;

/**
 * Écran d'accueil (Splash Screen) avec animations élégantes sur le logo et les textes.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Récupération des vues animées
        MaterialCardView cardLogo = findViewById(R.id.card_splash_logo);
        TextView tvTitle = findViewById(R.id.tv_splash_title);
        TextView tvSubtitle = findViewById(R.id.tv_splash_subtitle);
        TextView tvBadge = findViewById(R.id.tv_splash_badge);
        LinearLayout layoutFooter = findViewById(R.id.layout_splash_footer);

        // Chargement des animations personnalisées
        Animation animLogo = AnimationUtils.loadAnimation(this, R.anim.splash_logo);
        Animation animText = AnimationUtils.loadAnimation(this, R.anim.splash_text);
        Animation animFade = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Application des animations
        cardLogo.startAnimation(animLogo);
        tvTitle.startAnimation(animText);
        tvSubtitle.startAnimation(animText);
        tvBadge.startAnimation(animText);
        layoutFooter.startAnimation(animFade);

        // Initialisation précoce en tâche de fond du gestionnaire Firebase
        FirebaseStudentManager.getInstance(this);

        // Transition fluide vers l'écran principal après l'animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2200);
    }
}
