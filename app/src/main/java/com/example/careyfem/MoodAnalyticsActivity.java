package com.example.careyfem;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoodAnalyticsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTrendStatus, tvDayInsight, tvWeekFrequent, tvWeekInsight, tvMonthFrequent, tvMonthInsight, tvReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_analytics);

        btnBack = findViewById(R.id.btnBack);
        tvTrendStatus = findViewById(R.id.tvTrendStatus);
        tvDayInsight = findViewById(R.id.tvDayInsight);
        tvWeekFrequent = findViewById(R.id.tvWeekFrequent);
        tvWeekInsight = findViewById(R.id.tvWeekInsight);
        tvMonthFrequent = findViewById(R.id.tvMonthFrequent);
        tvMonthInsight = findViewById(R.id.tvMonthInsight);
        tvReminders = findViewById(R.id.tvReminders);

        btnBack.setOnClickListener(v -> finish());

        fetchAnalytics();
    }

    private void fetchAnalytics() {
        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        apiService.getMoodAnalytics().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(MoodAnalyticsActivity.this, "Failed to load analytics", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(MoodAnalyticsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Map<String, Object> data) {
        try {
            // Trend
            tvTrendStatus.setText((String) data.get("trend"));

            // Day View
            Map<String, Object> day = (Map<String, Object>) data.get("day");
            tvDayInsight.setText((String) day.get("insight"));

            // Week View
            Map<String, Object> week = (Map<String, Object>) data.get("week");
            String weekFreq = (String) week.get("most_frequent");
            tvWeekFrequent.setText("Most frequent: " + weekFreq);
            tvWeekInsight.setText((String) week.get("insight"));

            // Month View
            Map<String, Object> month = (Map<String, Object>) data.get("month");
            String monthFreq = (String) month.get("most_frequent");
            tvMonthFrequent.setText("Monthly pattern: " + monthFreq);
            tvMonthInsight.setText((String) month.get("insight"));

            // Reminders
            List<String> reminders = (List<String>) data.get("reminders");
            if (reminders != null) {
                StringBuilder sb = new StringBuilder();
                for (String r : reminders) {
                    sb.append("• ").append(r).append("\n");
                }
                tvReminders.setText(sb.toString().trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MoodAnalyticsActivity.this, "Data Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
