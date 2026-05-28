package com.example.gitmanager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.gitmanager.R;

public class MainActivity extends Activity {

    private EditText editPath, editMessage;
    private Button btnPush;
    private TextView textStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // เชื่อม UI จาก XML
        editPath = findViewById(R.id.edit_path);
        editMessage = findViewById(R.id.edit_message);
        btnPush = findViewById(R.id.btn_push);
        textStatus = findViewById(R.id.text_status);

        // ตั้งค่าเหตุการณ์เมื่อกดปุ่ม
        btnPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = editPath.getText().toString();
                String msg = editMessage.getText().toString();

                if (path.isEmpty()) {
                    textStatus.setText("กรุณาระบุ Path ของโปรเจกต์");
                    return;
                }

                textStatus.setText("กำลังดำเนินการ...");

                // รัน Git Push ใน Thread แยก เพื่อไม่ให้แอปค้าง
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GitManager.executePush(path, msg);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textStatus.setText("สถานะ: สำเร็จ!");
                                    Toast.makeText(MainActivity.this, "Push ขึ้น GitHub เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (final Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textStatus.setText("ผิดพลาด: " + e.getMessage());
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }
}
