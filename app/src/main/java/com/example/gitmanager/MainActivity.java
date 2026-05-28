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
    private String rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. ขอสิทธิ์จัดการไฟล์ทั้งหมด
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName())));
        }

        // 2. กำหนด Path อยู่หน้าหลักของเครื่อง
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GitProjects";
        new File(rootPath).mkdirs();

        // 3. เชื่อมต่อ UI
        etUser = findViewById(R.id.edit_username);
        etEmail = findViewById(R.id.edit_email);
        etToken = findViewById(R.id.edit_token);
        tvStatus = findViewById(R.id.text_status);
        spinnerProjects = findViewById(R.id.spinner_projects);

        loadPreferences();

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            getSharedPreferences("GitHubPrefs", MODE_PRIVATE).edit()
                    .putString("username", etUser.getText().toString())
                    .putString("email", etEmail.getText().toString())
                    .putString("token", etToken.getText().toString()).apply();
            Toast.makeText(this, "บันทึกแล้ว", Toast.LENGTH_SHORT).show();
            loadProjectList();
        });

        findViewById(R.id.btn_push).setOnClickListener(v -> {
            if (spinnerProjects.getSelectedItem() == null) {
                Toast.makeText(this, "กรุณาสร้างหรือย้ายโปรเจกต์เข้าโฟลเดอร์ GitProjects", Toast.LENGTH_SHORT).show();
                return;
            }
            new BuildTaskManager(this, rootPath + "/" + spinnerProjects.getSelectedItem().toString(), this).executePush();
        });

        loadProjectList();
    }

    private void loadProjectList() {
        File[] files = new File(rootPath).listFiles(File::isDirectory);
        ArrayList<String> names = new ArrayList<>();
        if (files != null) for (File f : files) names.add(f.getName());
        spinnerProjects.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names));
    }

    private void loadPreferences() {
        SharedPreferences pref = getSharedPreferences("GitHubPrefs", MODE_PRIVATE);
        etUser.setText(pref.getString("username", ""));
        etEmail.setText(pref.getString("email", ""));
        etToken.setText(pref.getString("token", ""));
    }

    @Override
    public void onLogAppend(String text, int color) { runOnUiThread(() -> tvStatus.append("\n" + text)); }
    @Override
    public void onFinished(boolean success) { runOnUiThread(() -> Toast.makeText(this, success ? "เสร็จสิ้น" : "ล้มเหลว", Toast.LENGTH_SHORT).show()); }
}
