package jalil.prayertimes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityDialog extends AppCompatActivity {

    Button bOk, bShot;
    TextView tvMessage;
    LinearLayout linearLayout;

    final static String EXTRA_DIALOG_TITLE = "EXTRA_DIALOG_TITLE";
    final static String EXTRA_DIALOG_MESSAGE = "EXTRA_DIALOG_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        tvMessage = findViewById(R.id.dialog_message);
        bOk = findViewById(R.id.dialog_button_ok);
        bShot = findViewById(R.id.dialog_button_shot);
        linearLayout = findViewById(R.id.dialog_linear);

        Intent intent = this.getIntent();
        String message = intent.getStringExtra(EXTRA_DIALOG_MESSAGE);

        tvMessage.setText(message);

        bOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityDialog.this.finish();
            }
        });

        bShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = Utilities.createBitmapFromView(linearLayout);
                new AsyncSaveScreenshot(ActivityDialog.this, bitmap, "dialog").doInBackground();
            }
        });
    }
}
