package com.example.gitmanager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends Activity implements BuildTaskManager.BuildListener {

    private EditText etUser, etEmail, etToken, etPath, etMsg;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. เชื่อมต่อ UI จากไฟล์ XML
        etUser = findViewById(R.id.edit_username);
        etEmail = findViewById(R.id.edit_email);
        etToken = findViewById(R.id.edit_token);
        etPath = findViewById(R.id.edit_path);
        etMsg = findViewById(R.id.edit_message);
        tvStatus = findViewById(R.id.text_status);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnPush = findViewById(R.id.btn_push);

        // 2. โหลดค่าที่เคยบันทึกไว้
        SharedPreferences pref = getSharedPreferences("GitHubPrefs", MODE_PRIVATE);
        etUser.setText(pref.getString("username", ""));
        etEmail.setText(pref.getString("email", ""));
        etToken.setText(pref.getString("token", ""));

        // 3. ปุ่มบันทึกข้อมูลตั้งค่า
        btnSave.setOnClickListener(v -> {
            pref.edit().putString("username", etUser.getText().toString())
                       .putString("email", etEmail.getText().toString())
                       .putString("token", etToken.getText().toString()).apply();
            Toast.makeText(this, "บันทึกตั้งค่าเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
        });

        // 4. ปุ่มสั่ง Push ขึ้น GitHub
        btnPush.setOnClickListener(v -> {
            String path = etPath.getText().toString();
            if (path.isEmpty()) {
                tvStatus.setText("❌ กรุณาระบุ Path ของโปรเจกต์ก่อนครับ");
                return;
            }

            // สั่งงานผ่าน BuildTaskManager
            tvStatus.setText("กำลังเตรียมการ...");
            BuildTaskManager taskManager = new BuildTaskManager(this, path, this);
            taskManager.executePush();
        });
    }

    // --- ส่วน Implement ของ BuildTaskManager.BuildListener ---

    @Override
    public void onLogAppend(String text, int color) {
        // ใช้ runOnUiThread เพื่อให้ UI อัปเดตจาก Thread อื่นได้
        runOnUiThread(() -> {
            tvStatus.append("\n" + text);
            tvStatus.setTextColor(color);
        });
    }

    @Override
    public void onFinished(boolean success) {
        runOnUiThread(() -> {
            if (success) {
                Toast.makeText(this, "ส่งข้อมูลขึ้น GitHub สำเร็จ!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "เกิดข้อผิดพลาดในการ Push", Toast.LENGTH_LONG).show();
            }
        });
    }
}
