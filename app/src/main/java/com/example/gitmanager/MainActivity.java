package com.example.gitmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.*;
import androidx.documentfile.provider.DocumentFile; // ต้องเพิ่ม dependency: implementation 'androidx.documentfile:documentfile:1.0.1'

public class MainActivity extends Activity implements BuildTaskManager.BuildListener {

    private EditText etUser, etEmail, etToken, etPath;
    private TextView tvStatus;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUser = findViewById(R.id.edit_username);
        etEmail = findViewById(R.id.edit_email);
        etToken = findViewById(R.id.edit_token);
        etPath = findViewById(R.id.edit_path);
        tvStatus = findViewById(R.id.text_status);
        
        SharedPreferences pref = getSharedPreferences("GitHubPrefs", MODE_PRIVATE);
        etUser.setText(pref.getString("username", ""));
        etEmail.setText(pref.getString("email", ""));
        etToken.setText(pref.getString("token", ""));

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            pref.edit().putString("username", etUser.getText().toString())
                       .putString("email", etEmail.getText().toString())
                       .putString("token", etToken.getText().toString()).apply();
            Toast.makeText(this, "บันทึกข้อมูลแล้ว", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_select_folder).setOnClickListener(v -> {
            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE);
        });

        findViewById(R.id.btn_push).setOnClickListener(v -> {
            String path = etPath.getText().toString();
            if (path.isEmpty()) {
                tvStatus.setText("กรุณาเลือกโฟลเดอร์ก่อนครับ");
                return;
            }
            // ส่ง Path ที่แปลงแล้วไปให้ BuildTaskManager
            new BuildTaskManager(this, path, this).executePush();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            
            // แก้ไข: ใช้ DocumentFile เพื่อดึงข้อมูลโฟลเดอร์ที่ Git อ่านได้
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);
            etPath.setText(pickedDir.getUri().toString());
            
            // หมายเหตุ: หากยัง Error เรื่อง permission ให้ตรวจสอบว่าโฟลเดอร์ที่เลือกไม่มีไฟล์ .git เก่าที่ล็อกอยู่
        }
    }

    @Override
    public void onLogAppend(String text, int color) { runOnUiThread(() -> tvStatus.append("\n" + text)); }
    
    @Override
    public void onFinished(boolean success) { runOnUiThread(() -> Toast.makeText(this, "เสร็จสิ้น", Toast.LENGTH_SHORT).show()); }
}
