package com.example.chybovko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReseniActivity extends AppCompatActivity {
    private SwitchCompat opravaDiluSwitch;
    private SwitchCompat opravaLakuSwitch;
    private Spinner okamziteOpatreniSpinner;
    private Spinner systemoveOpatreniSpinner;
    private SwitchCompat tiskStitkuSwitch;
    private Button predchoziButton;
    private Button odeslatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reseni);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        opravaDiluSwitch = findViewById(R.id.opravaDiluSwitch);
        opravaLakuSwitch = findViewById(R.id.opravaLakuSwitch);
        okamziteOpatreniSpinner = findViewById(R.id.okamziteOpatreniSpinner);
        systemoveOpatreniSpinner = findViewById(R.id.systemoveOpatreniSpinner);
        tiskStitkuSwitch = findViewById(R.id.tiskStitkuSwitch);
        predchoziButton = findViewById(R.id.predchoziButton);
        odeslatButton = findViewById(R.id.odeslatButton);

        predchoziButton.setOnClickListener(v -> bezNaPredchozi());
        odeslatButton.setOnClickListener(v -> odesliChyboveHlaseni());
    }

    private void bezNaPredchozi() {
        ulozHodnotyDoChybovka();
        Intent intent = new Intent(this, FotkyActivity.class);
        startActivity(intent);
    }

    private void ulozHodnotyDoChybovka() {

    }

    private void odesliChyboveHlaseni() {
        ulozHodnotyDoChybovka();
        ulozChybovkoDoXml(MainActivity.getChyboveHlaseni());
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    private void ulozChybovkoDoXml(ChyboveHlaseni chybovko) {
        Toast.makeText(this, "Gratuluji, odeslal jsi chybové hlášení", Toast.LENGTH_SHORT).show();
    }
}