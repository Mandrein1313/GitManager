package com.example.gitmanager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI สำหรับตั้งค่า
        EditText etUser = findViewById(R.id.edit_username);
        EditText etEmail = findViewById(R.id.edit_email);
        EditText etToken = findViewById(R.id.edit_token);
        Button btnSave = findViewById(R.id.btn_save);

        // UI สำหรับ Push
        EditText etPath = findViewById(R.id.edit_path);
        EditText etMsg = findViewById(R.id.edit_message);
        Button btnPush = findViewById(R.id.btn_push);
        TextView tvStatus = findViewById(R.id.text_status);

        // โหลดข้อมูลเก่า
        SharedPreferences pref = getSharedPreferences("GitHubPrefs", MODE_PRIVATE);
        etUser.setText(pref.getString("user", ""));
        etEmail.setText(pref.getString("email", ""));
        etToken.setText(pref.getString("token", ""));

        // ปุ่มบันทึกตั้งค่า
        btnSave.setOnClickListener(v -> {
            pref.edit().putString("user", etUser.getText().toString())
                       .putString("email", etEmail.getText().toString())
                       .putString("token", etToken.getText().toString()).apply();
            Toast.makeText(this, "บันทึกตั้งค่าสำเร็จ", Toast.LENGTH_SHORT).show();
        });

        // ปุ่มสั่ง Push
        btnPush.setOnClickListener(v -> {
            String path = etPath.getText().toString();
            String msg = etMsg.getText().toString();
            String user = pref.getString("user", "");
            String token = pref.getString("token", "");

            tvStatus.setText("กำลังประมวลผล...");
            new Thread(() -> {
                try {
                    GitManager.executePush(path, msg, user, token);
                    runOnUiThread(() -> tvStatus.setText("สถานะ: สำเร็จ!"));
                } catch (Exception e) {
                    runOnUiThread(() -> tvStatus.setText("Error: " + e.getMessage()));
                }
            }).start();
        });
    }
}
