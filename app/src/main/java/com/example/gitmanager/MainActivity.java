package com.example.gitmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends Activity implements BuildTaskManager.BuildListener {

    private EditText etUser, etToken, etPath;
    private TextView tvStatus;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUser = findViewById(R.id.edit_username);
        etToken = findViewById(R.id.edit_token);
        etPath = findViewById(R.id.edit_path);
        tvStatus = findViewById(R.id.text_status);
        
        findViewById(R.id.btn_select_folder).setOnClickListener(v -> {
            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE);
        });

        findViewById(R.id.btn_push).setOnClickListener(v -> {
            String path = etPath.getText().toString();
            new BuildTaskManager(this, path, this).executePush();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // หมายเหตุ: การแปลง Uri เป็น path ของ java.io.File อาจต้องใช้เทคนิคเพิ่มเติมใน Android 11+
            etPath.setText(treeUri.getPath()); 
        }
    }

    @Override
    public void onLogAppend(String text, int color) { runOnUiThread(() -> tvStatus.append("\n" + text)); }
    @Override
    public void onFinished(boolean success) { Toast.makeText(this, "เสร็จสิ้น", Toast.LENGTH_SHORT).show(); }
}
