package com.example.careyfem;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class SubscriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        ImageView btnBack = findViewById(R.id.btnBackSubscription);
        btnBack.setOnClickListener(v -> finish());

        MaterialButton btnStartPremium = findViewById(R.id.btnStartPremium);
        btnStartPremium.setOnClickListener(v -> {
            Toast.makeText(this, "Premium features coming soon!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
