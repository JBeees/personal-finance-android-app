package com.financeapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SetupPinActivity extends AppCompatActivity {

    private static final int PIN_LENGTH = 6;
    private StringBuilder pinInput = new StringBuilder();
    private View[] dots;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvSubtitle = findViewById(R.id.tvSubtitle);
        tvTitle.setText("BUAT PIN");
        tvSubtitle.setText("6 digit untuk mengamankan tabungan");

        tvError = findViewById(R.id.tvError);
        dots = new View[]{
                findViewById(R.id.dot1), findViewById(R.id.dot2),
                findViewById(R.id.dot3), findViewById(R.id.dot4),
                findViewById(R.id.dot5), findViewById(R.id.dot6)
        };

        setupKeypad();
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
            new Handler().postDelayed(this::goToConfirm, 200);
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

    private void goToConfirm() {
        Intent intent = new Intent(this, PinActivity.class);
        intent.putExtra("mode", "setup_confirm");
        intent.putExtra("first_pin", pinInput.toString());
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
