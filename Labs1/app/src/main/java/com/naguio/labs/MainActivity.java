package com.naguio.labs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    CheckBox rememberMeCheckbox;
    Button loginButton, clearButton, registerButton;
    SharedPreferences prefs;

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

        prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
        initViews();
    }

    public void initViews() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        loginButton = findViewById(R.id.loginButton);
        clearButton = findViewById(R.id.clearButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> login());
        clearButton.setOnClickListener(v -> clearPrefs());
        registerButton.setOnClickListener(v -> openRegister());

        // This is for when "remember me" is checked, to autofill the saved credentials
        if (prefs.getBoolean("rememberMe", false)) {
            usernameInput.setText(prefs.getString("username", ""));
            passwordInput.setText(prefs.getString("password", ""));
            rememberMeCheckbox.setChecked(true);
        }
    }

    public void login() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        String savedUser = prefs.getString("username", null);
        String savedPass = prefs.getString("password", null);

        if (savedUser == null || savedPass == null) {
            Toast.makeText(this, "Nothing saved", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.equals(savedUser) && password.equals(savedPass)) {
            prefs.edit().putBoolean("rememberMe", rememberMeCheckbox.isChecked()).apply();

            Intent intent = new Intent(this, LandingActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("rememberMe", rememberMeCheckbox.isChecked());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearPrefs() {
        prefs.edit().clear().apply();

        usernameInput.setText("");
        passwordInput.setText("");
        rememberMeCheckbox.setChecked(false);
    }

    public void openRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
