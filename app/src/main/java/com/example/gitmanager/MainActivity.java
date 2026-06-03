import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView txtHead, txtContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // แทน toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // หา id ของ textview
        txtHead = findViewById(R.id.txtHead);
        txtContent = findViewById(R.id.txtContent);

        // ตัวอย่างการเปลี่ยนข้อความ
        txtHead.setText("หัวข้อใหม่");
        txtContent.setText("เนื้อหาข้อมูลใหม่");

        // ตัวอย่างการแสดงข้อความเมื่อมีการคลิกที่ toolbar
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // โค้ดที่จะทำเมื่อมีการคลิกที่ toolbar
            }
        });
    }
}