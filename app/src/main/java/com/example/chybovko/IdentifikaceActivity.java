package com.example.chybovko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IdentifikaceActivity extends AppCompatActivity {
    private EditText incidentEditText;
    private TextView pracovisteTextView;
    private TextView zakazkaTextView;
    private EditText komponentaEditText;
    private EditText pocetNefunknichKusuEditText;
    private Spinner oblastSpinner;
    private EditText popisVadyEditText;
    private SwitchCompat zdrojVadySwitch;
    private AutoCompleteTextView dodavatelAutoCompleteText;
    private EditText znackaDodavateleEditText;
    private Spinner charakterVadySpinner;
    private Spinner druhVadySpinner;
    private Button zrusitButton;
    private Button pokracovatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_identifikace);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        incidentEditText = findViewById(R.id.incidentEditText);
        pracovisteTextView = findViewById(R.id.pracovisteTextView);
        zakazkaTextView = findViewById(R.id.zakazkaTextView);
        komponentaEditText = findViewById(R.id.komponentaEditText);
        pocetNefunknichKusuEditText = findViewById(R.id.pocetNefunknichKusuEditText);
        oblastSpinner = findViewById(R.id.oblastSpinner);
        popisVadyEditText = findViewById(R.id.popisVadyEditText);
        zdrojVadySwitch = findViewById(R.id.zdrojVadySwitch);
        dodavatelAutoCompleteText = findViewById(R.id.dodavatelAutoCompleteText);
        znackaDodavateleEditText = findViewById(R.id.znackaDodavateleEditText);
        charakterVadySpinner = findViewById(R.id.charakterVadySpinner);
        druhVadySpinner = findViewById(R.id.druhVadySpinner);
        zrusitButton = findViewById(R.id.zrusitButton);
        pokracovatButton = findViewById(R.id.pokracovatBbutton);

        zrusitButton.setOnClickListener(v -> zrus());
        pokracovatButton.setOnClickListener(v -> bezNaDalsi());


    }

    private void ulozHodnotyDoChybovka() {

    }

    private void bezNaDalsi() {
        ulozHodnotyDoChybovka();
        Intent intent = new Intent(this, FotkyActivity.class);
        startActivity(intent);
    }

    private void zrus() {
        Toast.makeText(this, "Zrušil jsi všechno...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}