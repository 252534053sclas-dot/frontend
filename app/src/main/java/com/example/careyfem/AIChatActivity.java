package com.example.careyfem;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private MessageAdapter adapter;
    private EditText etMessage;
    private ImageView btnSend, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        adapter = new MessageAdapter();
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        // Initial AI message
        addMessage("Hello! I'm Careyfem AI. How are you feeling today? 💕", "ai");

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = etMessage.getText().toString().trim();
                if (!text.isEmpty()) {
                    addMessage(text, "user");
                    etMessage.setText("");
                    getAIResponse(text);
                }
            }
        });
    }

    private void addMessage(String text, String sender) {
        Map<String, String> msg = new HashMap<>();
        msg.put("text", text);
        msg.put("sender", sender);
        msg.put("time", new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date()));
        adapter.addMessage(msg);
        rvChat.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void getAIResponse(String text) {
        // Retrofit call to Django Backend
        ApiInterface api = ApiClient.getClient(AIChatActivity.this).create(ApiInterface.class);
        Map<String, String> data = new HashMap<>();
        data.put("message", text);

        api.getAIResponse(data).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    addMessage(response.body().get("response"), "ai");
                } else {
                    addMessage("Sorry, I'm having trouble connecting to my brain. But I'm still here for you! 💕", "ai");
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                addMessage("I'm offline right now, but you're doing great! 💕", "ai");
            }
        });
    }
}
