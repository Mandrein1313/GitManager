package com.example.gitmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.*;
import androidx.documentfile.provider.DocumentFile;

public class MainActivity extends Activity {

    private EditText etUser, etEmail, etToken, etPath, etMsg;
    private TextView tvStatus;
    private static final int REQUEST_CODE_OPEN_DIR = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // เชื่อม UI
        etUser = findViewById(R.id.edit_username);
        etEmail = findViewById(R.id.edit_email);
        etToken = findViewById(R.id.edit_token);
        etPath = findViewById(R.id.edit_path);
        etMsg = findViewById(R.id.edit_message);
        tvStatus = findViewById(R.id.text_status);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnPush = findViewById(R.id.btn_push);

        // โหลดค่าเดิม
        SharedPreferences pref = getSharedPreferences("GitHubPrefs", MODE_PRIVATE);
        etUser.setText(pref.getString("user", ""));
        etEmail.setText(pref.getString("email", ""));
        etToken.setText(pref.getString("token", ""));

        // กดเพื่อเลือกโฟลเดอร์โปรเจกต์
        etPath.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_OPEN_DIR);
        });

        // บันทึกค่า
        btnSave.setOnClickListener(v -> {
            pref.edit().putString("user", etUser.getText().toString())
                       .putString("email", etEmail.getText().toString())
                       .putString("token", etToken.getText().toString()).apply();
            Toast.makeText(this, "บันทึกตั้งค่าแล้ว", Toast.LENGTH_SHORT).show();
        });

        // สั่ง Push
        btnPush.setOnClickListener(v -> {
            String path = etPath.getText().toString();
            String msg = etMsg.getText().toString();
            String user = etUser.getText().toString();
            String token = etToken.getText().toString();

            if (path.isEmpty()) {
                tvStatus.setText("กรุณาเลือกโฟลเดอร์โปรเจกต์ก่อนครับ");
                return;
            }

            tvStatus.setText("กำลัง Push...");
            new Thread(() -> {
                try {
                    GitManager.executePush(path, msg, user, token);
                    runOnUiThread(() -> tvStatus.setText("สถานะ: Push สำเร็จ!"));
                } catch (Exception e) {
                    runOnUiThread(() -> tvStatus.setText("Error: " + e.getMessage()));
                }
            }).start();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OPEN_DIR && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            // ดึง Path ที่แท้จริง (ต้องเป็นโฟลเดอร์ที่มี .git อยู่ข้างใน)
            String path = DocumentFile.fromTreeUri(this, treeUri).getUri().getPath();
            // หมายเหตุ: การแปลง Uri เป็น Path จริงใน Android 11+ มีความซับซ้อน 
            // แนะนำให้ลองใช้ Path จริงที่พบใน File Manager มากรอกถ้า Uri ตรงนี้มีปัญหา
            etPath.setText(treeUri.toString()); 
        }
    }
}
