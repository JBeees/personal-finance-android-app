package com.financeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvTagline = findViewById(R.id.tvTagline);
        View dividerLine = findViewById(R.id.dividerLine);

        // Animate: fade in title
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setFillAfter(true);
        tvAppName.startAnimation(fadeIn);
        tvAppName.setAlpha(1f);

        // Animate tagline with delay
        new Handler().postDelayed(() -> {
            AlphaAnimation fadeIn2 = new AlphaAnimation(0f, 1f);
            fadeIn2.setDuration(600);
            fadeIn2.setFillAfter(true);
            tvTagline.startAnimation(fadeIn2);
            tvTagline.setAlpha(1f);

            dividerLine.animate().alpha(1f).setDuration(600).start();
        }, 400);

        // Navigate after splash
        new Handler().postDelayed(() -> {
            DatabaseHelper db = DatabaseHelper.getInstance(this);
            Intent intent;
            if (!db.hasPin()) {
                // First time: Setup PIN
                intent = new Intent(this, SetupPinActivity.class);
            } else {
                // Already setup: Main screen
                intent = new Intent(this, MainActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1800);
    }
}
