package com.example.careyfem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnSignup;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> data = new HashMap<>();
                data.put("username", name); // Using name as username
                data.put("email", email);
                data.put("password", pass);
                
                ApiClient.getClient(SignupActivity.this).create(ApiInterface.class).signup(data).enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                             Map<String, String> body = response.body();
                             String accessToken = body.get("access");
                             String username = body.get("username");
                             
                             if (accessToken != null) {
                                  SessionManager session = new SessionManager(SignupActivity.this);
                                  session.createSession(accessToken, username);
                                  
                                  Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                  Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                  startActivity(intent);
                                  finish();
                             } else {
                                  Toast.makeText(SignupActivity.this, "Signup successful but token missing", Toast.LENGTH_SHORT).show();
                                  finish();
                             }
                        } else {
                             try {
                                 String error = response.errorBody().string();
                                 Toast.makeText(SignupActivity.this, "Signup Failed: " + error, Toast.LENGTH_SHORT).show();
                             } catch (Exception e) {
                                 Toast.makeText(SignupActivity.this, "Signup Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                             }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                         Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Goes back to LoginActivity
            }
        });
    }
}
