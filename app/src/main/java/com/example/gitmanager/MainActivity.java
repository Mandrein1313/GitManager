package com.example.gitmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.*;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity implements BuildTaskManager.BuildListener {

    private EditText etUser, etEmail, etToken;
    private TextView tvStatus;
    private Spinner spinnerProjects;
    private String rootPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. ขอสิทธิ์เข้าถึงไฟล์ทั้งหมด (สำหรับ Android 11+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        // 2. กำหนด Path ให้อยู่ข้างนอกที่หน้าหลักของเครื่อง
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyGitProjects";
        File folder = new File(rootPath);
        if (!folder.exists()) folder.mkdirs();

        // 3. เชื่อมต่อ UI
        etUser = findViewById(R.id.edit_username);
        etEmail = findViewById(R.id.edit_email);
        etToken = findViewById(R.id.edit_token);
        tvStatus = findViewById(R.id.text_status);
        spinnerProjects = findViewById(R.id.spinner_projects);
        
        // 4. โหลดค่า
        SharedPreferences pref = getSharedPreferences("GitHubPrefs", MODE_PRIVATE);
        etUser.setText(pref.getString("username", ""));
        etEmail.setText(pref.getString("email", ""));
        etToken.setText(pref.getString("token", ""));

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            pref.edit().putString("username", etUser.getText().toString())
                       .putString("email", etEmail.getText().toString())
                       .putString("token", etToken.getText().toString()).apply();
            Toast.makeText(this, "บันทึกข้อมูลแล้ว", Toast.LENGTH_SHORT).show();
            loadProjectList();
        });

        findViewById(R.id.btn_push).setOnClickListener(v -> {
            if (spinnerProjects.getSelectedItem() == null) {
                tvStatus.setText("ยังไม่มีโปรเจกต์ในโฟลเดอร์ MyGitProjects ครับ");
                return;
            }
            String fullPath = rootPath + "/" + spinnerProjects.getSelectedItem().toString();
            new BuildTaskManager(this, fullPath, this).executePush();
        });

        loadProjectList();
    }

    private void loadProjectList() {
        File root = new File(rootPath);
        File[] files = root.listFiles(File::isDirectory);
        ArrayList<String> projectNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) { projectNames.add(file.getName()); }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, projectNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProjects.setAdapter(adapter);
    }

    @Override
    public void onLogAppend(String text, int color) { runOnUiThread(() -> tvStatus.append("\n" + text)); }
    
    @Override
    public void onFinished(boolean success) { runOnUiThread(() -> Toast.makeText(this, success ? "สำเร็จ!" : "ผิดพลาด", Toast.LENGTH_SHORT).show()); }
}
