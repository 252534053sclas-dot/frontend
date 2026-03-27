package com.example.careyfem;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class JournalActivity extends AppCompatActivity {

    private Button btnNewEntry;
    private ImageView btnBack;
    private LinearLayout llJournalContainer;
    private TextView tvTotalJournals, tvTotalInsights;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        btnBack = findViewById(R.id.btnBack);
        btnNewEntry = findViewById(R.id.btnNewEntry);
        llJournalContainer = findViewById(R.id.llJournalContainer);
        tvTotalJournals = findViewById(R.id.tvTotalJournals);
        tvTotalInsights = findViewById(R.id.tvTotalInsights);

        btnBack.setOnClickListener(v -> finish());

        // Fetch history when activity starts
        fetchJournalHistory();

        btnNewEntry.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_journal, null);
            EditText input = dialogView.findViewById(R.id.etJournalContent);
            
            new AlertDialog.Builder(JournalActivity.this)
                .setTitle("New Journal Entry")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String content = input.getText().toString().trim();
                    if (!content.isEmpty()) {
                        saveJournalToBackend(content);
                    } else {
                        Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void fetchJournalHistory() {
        ApiClient.getClient(this).create(ApiInterface.class).getJournals().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayJournals(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // Keep default state
            }
        });
    }

    private void displayJournals(List<Map<String, Object>> journals) {
        llJournalContainer.removeAllViews();
        int insightCount = 0;

        tvTotalJournals.setText(String.valueOf(journals.size()));

        for (Map<String, Object> journal : journals) {
            String content = (String) journal.get("content");
            String insight = (String) journal.get("ai_insight");
            String timestamp = (String) journal.get("timestamp");

            if (insight != null && !insight.isEmpty()) {
                insightCount++;
            }

            addJournalToLayout(content, insight, timestamp);
        }
        tvTotalInsights.setText(String.valueOf(insightCount));
    }

    private void addJournalToLayout(String content, String insight, String timestamp) {
        View entryView = LayoutInflater.from(this).inflate(R.layout.item_journal_entry, llJournalContainer, false);
        
        TextView tvDate = entryView.findViewById(R.id.tvJournalDate);
        TextView tvContent = entryView.findViewById(R.id.tvJournalContent);
        TextView tvInsight = entryView.findViewById(R.id.tvAiInsight);
        LinearLayout llInsight = entryView.findViewById(R.id.llAiInsight);

        tvContent.setText(content);
        
        if (timestamp != null && timestamp.length() >= 10) {
            tvDate.setText(timestamp.substring(0, 10)); // YYYY-MM-DD
        }

        if (insight != null && !insight.isEmpty()) {
            tvInsight.setText(insight);
            llInsight.setVisibility(View.VISIBLE);
        } else {
            llInsight.setVisibility(View.GONE);
        }

        llJournalContainer.addView(entryView, 0); // Add at top
    }

    private void saveJournalToBackend(String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("content", content);

        ApiClient.getClient(JournalActivity.this).create(ApiInterface.class).saveJournal(data).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(JournalActivity.this, "Journal saved! 🌸", Toast.LENGTH_SHORT).show();
                    fetchJournalHistory(); // Refresh to show new entry and AI insight
                } else {
                    Toast.makeText(JournalActivity.this, "Failed to save", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(JournalActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
