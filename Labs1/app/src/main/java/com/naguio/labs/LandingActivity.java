package com.naguio.labs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LandingActivity extends AppCompatActivity {

    TextView welcomeText;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
        initViews();
    }

    public void initViews() {
        welcomeText = findViewById(R.id.welcomeText);

        String name = getIntent().getStringExtra("username");
        boolean isRemembered = getIntent().getBooleanExtra("rememberMe", false);

        // fallback: load name from shared preferences in case intent fails
        if (name == null) {
            name = prefs.getString("username", "Guest");
        }

        String message = "Welcome " + name + "!!!";
        if (isRemembered) {
            message += "\nYou will be remembered.";
        }

        welcomeText.setText(message);
    }
}
