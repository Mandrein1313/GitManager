package com.example.gitmanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;

public class ProjectListActivity extends AppCompatActivity {

    private ArrayList<String> projects = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        // 1. กำหนดโฟลเดอร์หลักไว้ที่หน้าหลักเหมือน AIDE/MiniStudio
        rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/GitProjects";
        File folder = new File(rootPath);
        if (!folder.exists()) folder.mkdirs();

        ListView listView = findViewById(R.id.projectListView);
        FloatingActionButton fab = findViewById(R.id.fabAddProject);

        refreshProjectList();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, projects);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("projectName", projects.get(position));
            startActivity(intent);
        });

        fab.setOnClickListener(v -> showCreateProjectDialog());
    }

    private void refreshProjectList() {
        projects.clear();
        File root = new File(rootPath);
        File[] files = root.listFiles(File::isDirectory);
        if (files != null) {
            for (File f : files) projects.add(f.getName());
        }
    }

    private void showCreateProjectDialog() {
        // ใช้โค้ด Dialog ดีไซน์พรีเมียมจากที่คุณให้มาได้เลย โดยในปุ่ม CREATE
        // ให้เรียกคำสั่ง: new File(rootPath + "/" + name).mkdirs();
        // แล้วสั่ง refreshProjectList() และ adapter.notifyDataSetChanged();
    }
}
