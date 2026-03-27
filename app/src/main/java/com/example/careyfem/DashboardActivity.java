package com.example.careyfem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private CardView cardAI, cardMood, cardJournal, cardCycle, cardSos;
    private ImageView btnLogout;
    private TextView tvWelcomeName, tvDate, tvTodayMood;
    private LinearLayout llRecentMoodHistory;
    private SessionManager session;
    private MaterialButton btnSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new SessionManager(this);
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        tvDate = findViewById(R.id.tvDate);
        tvTodayMood = findViewById(R.id.tvTodayMood);
        llRecentMoodHistory = findViewById(R.id.llRecentMoodHistory);
        btnLogout = findViewById(R.id.btnLogout);
        cardAI = findViewById(R.id.cardAI);
        cardMood = findViewById(R.id.cardMood);
        cardJournal = findViewById(R.id.cardJournal);
        cardCycle = findViewById(R.id.cardCycle);
        cardSos = findViewById(R.id.cardSos);
        btnSubscribe = findViewById(R.id.btnSubscribe);

        // Set Welcome Name and Date
        tvWelcomeName.setText("Hello, " + session.getName() + "!");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        tvDate.setText(dateFormat.format(new Date()));

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.logout();
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        cardAI.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AIChatActivity.class)));
        cardMood.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MoodTrackerActivity.class)));
        cardJournal.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, JournalActivity.class)));
        cardCycle.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MoodAnalyticsActivity.class)));

        if (btnSubscribe != null) {
            btnSubscribe.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, SubscriptionActivity.class);
                startActivity(intent);
            });
        }

        View.OnClickListener sosClick = v -> startActivity(new Intent(DashboardActivity.this, SOSActivity.class));
        cardSos.setOnClickListener(sosClick);
        findViewById(R.id.btnSosQuick).setOnClickListener(sosClick);

        fetchMoodHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMoodHistory();
    }

    private void fetchMoodHistory() {
        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        apiService.getMoods().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> moods = response.body();
                    updateMoodUI(moods);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // Silently fail or show toast
            }
        });
    }

    private void updateMoodUI(List<Map<String, Object>> moods) {
        if (moods == null || moods.isEmpty()) {
            if (findViewById(R.id.tvEmptyMood) != null) {
                findViewById(R.id.tvEmptyMood).setVisibility(View.VISIBLE);
            }
            tvTodayMood.setText("How are you feeling today?");
            return;
        }

        if (findViewById(R.id.tvEmptyMood) != null) {
            findViewById(R.id.tvEmptyMood).setVisibility(View.GONE);
        }
        
        if (llRecentMoodHistory != null) {
            llRecentMoodHistory.removeAllViews();
            
            // Update Today's Mood Bar with latest
            Map<String, Object> latest = moods.get(0);
            String latestEmoji = (String) latest.get("mood_type");
            tvTodayMood.setText("Today's mood: " + latestEmoji);

            // Add latest 3 entries to history
            int count = 0;
            for (Map<String, Object> mood : moods) {
                if (count >= 3) break;
                addMoodItem(mood);
                count++;
            }
        }
    }

    private void addMoodItem(Map<String, Object> mood) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_mood_history, llRecentMoodHistory, false);
        
        TextView tvEmoji = view.findViewById(R.id.tvHistoryEmoji);
        TextView tvLabel = view.findViewById(R.id.tvHistoryMood);
        TextView tvNote = view.findViewById(R.id.tvHistoryNote);
        TextView tvHistoryDate = view.findViewById(R.id.tvHistoryDate);

        String type = (String) mood.get("mood_type");
        String note = (String) mood.get("note");
        String timestamp = (String) mood.get("timestamp"); 

        tvEmoji.setText(type);
        tvLabel.setText(getMoodLabel(type));
        tvNote.setText(note != null && !note.isEmpty() ? note : "No note added");
        
        try {
            if (timestamp != null) {
                // Typical Django timestamp: 2026-01-27T15:54:42.123Z
                String datePart = timestamp.split("T")[0];
                tvHistoryDate.setText(datePart);
            }
        } catch (Exception e) {
            tvHistoryDate.setText("");
        }

        llRecentMoodHistory.addView(view);
    }

    private String getMoodLabel(String emoji) {
        if (emoji == null) return "Unknown";
        switch (emoji) {
            case "😊": return "Happy";
            case "😐": return "Neutral";
            case "😔": return "Sad";
            case "😡": return "Angry";
            default: return "Feeling " + emoji;
        }
    }
}
