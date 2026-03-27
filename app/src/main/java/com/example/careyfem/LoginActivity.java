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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvResetPassword = findViewById(R.id.tvResetPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty()) {
                     Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                     return;
                }
                
                // Disable button to prevent double-clicks
                btnLogin.setEnabled(false);
                
                Map<String, String> data = new HashMap<>();
                data.put("username", email); // Django LoginView expects 'username' (can be email)
                data.put("password", pass);

                ApiClient.getClient(LoginActivity.this).create(ApiInterface.class).login(data).enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, String> body = response.body();
                            String token = body.get("access");
                            String username = body.get("username");
                            String fullName = body.get("name");
                            
                            String displayName = (fullName != null && !fullName.isEmpty()) ? fullName : username;
                            
                            SessionManager session = new SessionManager(LoginActivity.this);
                            session.createSession(token, displayName);
                            
                            Toast.makeText(LoginActivity.this, "Login Successful! 🌸", Toast.LENGTH_SHORT).show();
                            
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            btnLogin.setEnabled(true); // Re-enable for retry
                            try {
                                String errorMsg = "Login Failed";
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    errorMsg = errorJson.contains("error") ? errorJson : "Invalid credentials";
                                }
                                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        btnLogin.setEnabled(true); // Re-enable for retry
                        Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        tvResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });
    }
}
