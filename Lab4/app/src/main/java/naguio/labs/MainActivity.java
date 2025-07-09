package naguio.labs;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    CheckBox rememberMeCheckbox;
    Button loginButton, clearButton, adminButton;
    SharedPreferences prefs;

    Realm realm;

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
        realm = Realm.getDefaultInstance();

        checkPermissions();
    }

    public void checkPermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                )
                .withListener(new BaseMultiplePermissionsListener() {
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            initViews();
                        } else {
                            toastRequirePermissions();
                        }
                    }
                })
                .check();
    }

    public void toastRequirePermissions() {
        Toast.makeText(this, "You must provide permissions for app to run", Toast.LENGTH_LONG).show();
        finish();
    }

    public void initViews() {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        loginButton = findViewById(R.id.loginButton);
        clearButton = findViewById(R.id.clearButton);
        adminButton = findViewById(R.id.adminButton);

        loginButton.setOnClickListener(v -> login());
        clearButton.setOnClickListener(v -> clearPrefs());
        adminButton.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));
    }

    public void login() {
        String name = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        User user = realm.where(User.class).equalTo("name", name).findFirst();

        if (user == null) {
            Toast.makeText(this, "No User found", Toast.LENGTH_SHORT).show();
        } else if (!user.getPassword().equals(password)) {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        } else {
            prefs.edit()
                    .putString("uuid", user.getUuid())
                    .putBoolean("rememberMe", rememberMeCheckbox.isChecked())
                    .apply();

            Intent intent = new Intent(this, LandingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void clearPrefs() {
        prefs.edit().clear().apply();
        usernameInput.setText("");
        passwordInput.setText("");
        rememberMeCheckbox.setChecked(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) realm.close();
    }
}
