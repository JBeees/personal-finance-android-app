package com.financeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

public class PinActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 6;
    private StringBuilder pinInput = new StringBuilder();
    private View[] dots;
    private TextView tvError;
    private DatabaseHelper db;

    // Mode: "verify", "setup_confirm", or "action_verify"
    private String mode = "verify";
    private String firstPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        db = DatabaseHelper.getInstance(this);
        mode = getIntent().getStringExtra("mode") != null ?
                getIntent().getStringExtra("mode") : "verify";
        firstPin = getIntent().getStringExtra("first_pin") != null ?
                getIntent().getStringExtra("first_pin") : "";

        initViews();
        setupKeypad();
    }

    private void initViews() {
        tvError = findViewById(R.id.tvError);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);

        if ("setup_confirm".equals(mode)) {
            tvTitle.setText("KONFIRMASI PIN");
            tvSubtitle.setText("masukkan PIN sekali lagi");
        }

        dots = new View[]{
                findViewById(R.id.dot1), findViewById(R.id.dot2),
                findViewById(R.id.dot3), findViewById(R.id.dot4),
                findViewById(R.id.dot5), findViewById(R.id.dot6)
        };

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupKeypad() {
        int[] keyIds = {R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5,
                R.id.key6, R.id.key7, R.id.key8, R.id.key9, R.id.key0};
        String[] keys = {"1","2","3","4","5","6","7","8","9","0"};

        for (int i = 0; i < keyIds.length; i++) {
            final String key = keys[i];
            findViewById(keyIds[i]).setOnClickListener(v -> onKeyPressed(key));
        }

        findViewById(R.id.keyDel).setOnClickListener(v -> onDelete());
    }

    private void onKeyPressed(String key) {
        if (pinInput.length() >= PIN_LENGTH) return;
        pinInput.append(key);
        updateDots();
        if (pinInput.length() == PIN_LENGTH) {
            new Handler().postDelayed(this::verifyPin, 200);
        }
    }

    private void onDelete() {
        if (pinInput.length() > 0) {
            pinInput.deleteCharAt(pinInput.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < PIN_LENGTH; i++) {
            dots[i].setBackgroundResource(
                    i < pinInput.length() ? R.drawable.pin_dot_active : R.drawable.pin_dot_inactive
            );
        }
    }

    private void verifyPin() {
        String entered = pinInput.toString();

        if ("verify".equals(mode) || "action_verify".equals(mode)) {
            String savedPin = db.getPin();
            android.util.Log.d("PIN_DEBUG", "Saved PIN: " + savedPin);
            if (entered.equals(savedPin)) {
                if ("action_verify".equals(mode)) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    // Success — go to savings
                    Intent intent = new Intent(this, SavingsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            } else {
                showError("PIN salah, coba lagi");
                resetPin();
            }
        } else if ("setup_confirm".equals(mode)) {
            if (entered.equals(firstPin)) {
                // Save PIN
                db.setPin(entered);
                // Go to savings
                Intent intent = new Intent(this, SavingsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } else {
                showError("PIN tidak cocok, ulangi dari awal");
                resetPin();
            }
        }
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        // Shake animation on dots
        View llDots = findViewById(R.id.llPinDots);
        llDots.animate().translationX(10f).setDuration(50).withEndAction(() ->
                llDots.animate().translationX(-10f).setDuration(50).withEndAction(() ->
                        llDots.animate().translationX(6f).setDuration(50).withEndAction(() ->
                                llDots.animate().translationX(0f).setDuration(50).start()
                        ).start()
                ).start()
        ).start();
    }

    private void resetPin() {
        new Handler().postDelayed(() -> {
            pinInput.setLength(0);
            updateDots();
        }, 600);
    }
}
