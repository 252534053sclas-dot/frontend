package com.example.careyfem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class MoodTrackerActivity extends AppCompatActivity {

    private LinearLayout llHappy, llNeutral, llSad, llAngry, llHistory;
    private EditText etNote;
    private Button btnSave;
    private ImageView btnBack;
    private TextView tvEmptyHistory, tvTotalEntries, tvLatestMood;
    private String selectedMood = "";
    private int totalEntries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracker);

        llHappy = findViewById(R.id.llHappy);
        llNeutral = findViewById(R.id.llNeutral);
        llSad = findViewById(R.id.llSad);
        llAngry = findViewById(R.id.llAngry);
        llHistory = findViewById(R.id.llHistory);
        etNote = findViewById(R.id.etMoodNote);
        btnSave = findViewById(R.id.btnSaveMood);
        btnBack = findViewById(R.id.btnBack);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        tvTotalEntries = findViewById(R.id.tvTotalEntries);
        tvLatestMood = findViewById(R.id.tvLatestMood);

        btnBack.setOnClickListener(v -> finish());

        // Fetch history when activity starts
        fetchMoodHistory();

        View.OnClickListener moodClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMoodBgs();
                v.setBackgroundResource(R.drawable.mood_card_selected);
                
                if (v.getId() == R.id.llHappy) selectedMood = "Happy 😊";
                else if (v.getId() == R.id.llNeutral) selectedMood = "Neutral 😐";
                else if (v.getId() == R.id.llSad) selectedMood = "Sad 😔";
                else if (v.getId() == R.id.llAngry) selectedMood = "Angry 😡";
            }
        };

        llHappy.setOnClickListener(moodClick);
        llNeutral.setOnClickListener(moodClick);
        llSad.setOnClickListener(moodClick);
        llAngry.setOnClickListener(moodClick);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMood.isEmpty()) {
                    Toast.makeText(MoodTrackerActivity.this, "Please select a mood", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String note = etNote.getText().toString().trim();
                
                Map<String, Object> data = new HashMap<>();
                data.put("mood_type", selectedMood);
                data.put("note", note);

                ApiClient.getClient(MoodTrackerActivity.this).create(ApiInterface.class).saveMood(data).enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                             Toast.makeText(MoodTrackerActivity.this, "Mood saved! 🌸", Toast.LENGTH_SHORT).show();
                             etNote.setText("");
                             resetMoodBgs();
                             selectedMood = "";
                             fetchMoodHistory(); // Refresh history
                        } else {
                             Toast.makeText(MoodTrackerActivity.this, "Failed to save: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                         Toast.makeText(MoodTrackerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void fetchMoodHistory() {
        ApiClient.getClient(this).create(ApiInterface.class).getMoods().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> moods = response.body();
                    displayMoodHistory(moods);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // Keep default empty state or show toast
            }
        });
    }

    private void displayMoodHistory(List<Map<String, Object>> moods) {
        llHistory.removeAllViews();
        llHistory.setGravity(android.view.Gravity.LEFT);

        if (moods.isEmpty()) {
            llHistory.setGravity(android.view.Gravity.CENTER);
            llHistory.addView(tvEmptyHistory);
            tvEmptyHistory.setVisibility(View.VISIBLE);
            tvTotalEntries.setText("0");
            tvLatestMood.setText("-");
            return;
        }

        tvEmptyHistory.setVisibility(View.GONE);
        totalEntries = moods.size();
        tvTotalEntries.setText(String.valueOf(totalEntries));

        for (int i = 0; i < moods.size(); i++) {
            Map<String, Object> mood = moods.get(i);
            String moodType = (String) mood.get("mood_type");
            String note = (String) mood.get("note");
            String timestamp = (String) mood.get("timestamp");

            if (i == 0 && moodType != null && moodType.length() >= 2) {
                tvLatestMood.setText(moodType.substring(moodType.length() - 2));
            }

            addMoodToHistory(moodType, note, timestamp);
        }
    }

    private void resetMoodBgs() {
        llHappy.setBackgroundResource(R.drawable.mood_card_unselected);
        llNeutral.setBackgroundResource(R.drawable.mood_card_unselected);
        llSad.setBackgroundResource(R.drawable.mood_card_unselected);
        llAngry.setBackgroundResource(R.drawable.mood_card_unselected);
    }

    private void addMoodToHistory(String mood, String note, String dateStr) {
        View historyItem = LayoutInflater.from(this).inflate(R.layout.item_mood_history, llHistory, false);
        
        TextView tvEmoji = historyItem.findViewById(R.id.tvHistoryEmoji);
        TextView tvMoodName = historyItem.findViewById(R.id.tvHistoryMood);
        TextView tvNote = historyItem.findViewById(R.id.tvHistoryNote);
        TextView tvDate = historyItem.findViewById(R.id.tvHistoryDate);

        if (mood != null && mood.length() >= 2) {
            tvEmoji.setText(mood.substring(mood.length() - 2));
            tvMoodName.setText(mood.substring(0, mood.length() - 2).trim());
        }

        tvNote.setText(note == null || note.isEmpty() ? "No note added" : note);
        
        // Simple date formatting (e.g., from 2026-01-27T... to Jan 27)
        if (dateStr != null && dateStr.length() >= 10) {
            tvDate.setText(dateStr.substring(5, 10)); // Just MM-DD for simplicity
        }

        llHistory.addView(historyItem);
    }
}
