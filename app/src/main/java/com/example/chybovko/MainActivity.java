package com.example.chybovko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    public static ChyboveHlaseni chyboveHlaseni;
    private Button noveChybovkoButton;
    private Button nactiChybovkoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        noveChybovkoButton = findViewById(R.id.noveChybovkoButton);
        nactiChybovkoButton = findViewById(R.id.nactiChybovkoButton);

        noveChybovkoButton.setOnClickListener(v -> vytvorNoveChybovko());
        nactiChybovkoButton.setOnClickListener(v -> nactiChybovko());

    }

    public static ChyboveHlaseni getChyboveHlaseni() {
        return chyboveHlaseni;
    }

    private void vytvorNoveChybovko() {
        chyboveHlaseni = new ChyboveHlaseni();
        Intent intent = new Intent(this, IdentifikaceActivity.class);
        startActivity(intent);
    }

    private void nactiChybovko() {
        Toast.makeText(this, "Ještě nic nedělám...", Toast.LENGTH_SHORT).show();
    }

}