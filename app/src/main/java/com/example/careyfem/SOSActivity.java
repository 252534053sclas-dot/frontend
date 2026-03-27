package com.example.careyfem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.view.LayoutInflater;
import android.widget.TextView;
import java.util.List;

public class SOSActivity extends AppCompatActivity {

    private Button btnActivate, btnAdd;
    private ImageView btnBack;
    private LinearLayout llContactContainer;
    private List<String> contactPhones = new ArrayList<>();
    private static final int PERMISSION_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnBack = findViewById(R.id.btnBack);
        btnActivate = findViewById(R.id.btnActivateSOS);
        btnAdd = findViewById(R.id.btnAddContact);
        llContactContainer = findViewById(R.id.llContactContainer);

        btnBack.setOnClickListener(v -> finish());

        // Fetch contacts when activity starts
        fetchContacts();

        btnActivate.setOnClickListener(v -> {
            checkPermissionsAndAction();
        });

        btnAdd.setOnClickListener(v -> {
            showAddContactDialog();
        });
    }

    private void checkPermissionsAndAction() {
        String[] permissions = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);
        } else {
            fetchLocationAndSendSOS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                fetchLocationAndSendSOS();
            } else {
                Toast.makeText(this, "Permissions Denied. SOS features limited.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchLocationAndSendSOS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            double lat = 13.0827; // Default Chennai if location null
            double lon = 80.2707;
            
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
            }
            sendSOSAlert(lat, lon);
        });
    }

    private void fetchContacts() {
        ApiClient.getClient(this).create(ApiInterface.class).getContacts().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayContacts(response.body());
                }
            }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    private void displayContacts(List<Map<String, Object>> contacts) {
        llContactContainer.removeAllViews();
        contactPhones.clear();
        for (Map<String, Object> contact : contacts) {
            String name = (String) contact.get("name");
            String phone = (String) contact.get("phone_number");
            
            contactPhones.add(phone);
            
            View contactView = LayoutInflater.from(this).inflate(R.layout.item_contact, llContactContainer, false);
            TextView tvName = contactView.findViewById(R.id.tvContactName);
            TextView tvPhone = contactView.findViewById(R.id.tvContactPhone);
            
            tvName.setText(name);
            tvPhone.setText(phone);
            
            llContactContainer.addView(contactView);
        }
    }

    private void sendSOSAlert(double lat, double lon) {
        Map<String, Object> data = new HashMap<>();
        data.put("latitude", lat);
        data.put("longitude", lon);

        ApiClient.getClient(SOSActivity.this).create(ApiInterface.class).sendSOS(data).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    sendSmsToContacts(lat, lon);
                    new AlertDialog.Builder(SOSActivity.this)
                        .setTitle("SOS Activated! 🚨")
                        .setMessage("Emergency alerts and your LIVE location have been sent to your contacts.")
                        .setPositiveButton("OK", null)
                        .show();
                } else {
                    Toast.makeText(SOSActivity.this, "Failed to send alert", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(SOSActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSmsToContacts(double lat, double lon) {
        if (contactPhones.isEmpty()) return;
        
        String message = "EMERGENCY! I need help. My real-time location: http://maps.google.com/maps?q=" + lat + "," + lon;
        
        try {
            SmsManager smsManager = SmsManager.getDefault();
            for (String phone : contactPhones) {
                smsManager.sendTextMessage(phone, null, message, null, null);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error sending SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Emergency Contact");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64, 32, 64, 32);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Name");
        nameInput.setBackgroundResource(R.drawable.edittext_bg);
        nameInput.setPadding(32, 32, 32, 32);
        layout.addView(nameInput);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(1, 24));
        layout.addView(spacer);

        final EditText phoneInput = new EditText(this);
        phoneInput.setHint("Phone Number");
        phoneInput.setBackgroundResource(R.drawable.edittext_bg);
        phoneInput.setPadding(32, 32, 32, 32);
        phoneInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(phoneInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            if (!name.isEmpty() && !phone.isEmpty()) {
                saveContact(name, phone);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveContact(String name, String phone) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone_number", phone);

        ApiClient.getClient(SOSActivity.this).create(ApiInterface.class).saveContact(data).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SOSActivity.this, "Contact added! 🌸", Toast.LENGTH_SHORT).show();
                    fetchContacts(); // Refresh list
                } else {
                    Toast.makeText(SOSActivity.this, "Failed to add contact", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(SOSActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
