package com.example.gitmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends Activity implements BuildTaskManager.BuildListener {

    private EditText etUser, etEmail, etToken, etPath;
    private TextView tvStatus;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // เชื่อมต่อ UI ครบทุกช่อง
        etUser = findViewById(R.id.edit_username);
        etEmail = findViewById(R.id.edit_email); // เพิ่มช่อง Email
        etToken = findViewById(R.id.edit_token);
        etPath = findViewById(R.id.edit_path);
        tvStatus = findViewById(R.id.text_status);
        
        // โหลดข้อมูลเดิมที่เคยบันทึกไว้
        SharedPreferences pref = getSharedPreferences("GitHubPrefs", MODE_PRIVATE);
        etUser.setText(pref.getString("username", ""));
        etEmail.setText(pref.getString("email", ""));
        etToken.setText(pref.getString("token", ""));

        // ปุ่มบันทึกข้อมูล
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            pref.edit().putString("username", etUser.getText().toString())
                       .putString("email", etEmail.getText().toString())
                       .putString("token", etToken.getText().toString()).apply();
            Toast.makeText(this, "บันทึกข้อมูลแล้ว", Toast.LENGTH_SHORT).show();
        });

        // ปุ่มเลือกโฟลเดอร์
        findViewById(R.id.btn_select_folder).setOnClickListener(v -> {
            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE);
        });

        // ปุ่ม Push
        findViewById(R.id.btn_push).setOnClickListener(v -> {
            String path = etPath.getText().toString();
            if (path.isEmpty()) {
                tvStatus.setText("กรุณาเลือกโฟลเดอร์ก่อนครับ");
                return;
            }
            new BuildTaskManager(this, path, this).executePush();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // อย่าลืมใส่ super
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            etPath.setText(treeUri.toString()); // ใช้ toString เพื่อเก็บ URI เต็มรูปแบบ
        }
    }

    @Override
    public void onLogAppend(String text, int color) { 
        runOnUiThread(() -> {
            tvStatus.append("\n" + text);
        }); 
    }
    
    @Override
    public void onFinished(boolean success) { 
        runOnUiThread(() -> Toast.makeText(this, success ? "เสร็จสิ้น" : "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show()); 
    }
}
