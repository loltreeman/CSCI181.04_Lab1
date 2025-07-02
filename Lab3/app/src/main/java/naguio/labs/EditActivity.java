package naguio.labs;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.realm.Realm;

public class EditActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private EditText confirmPasswordField;

    private Realm realm;
    private User userToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        init();
    }

    private void init() {
        usernameField = findViewById(R.id.usernameEdit);
        passwordField = findViewById(R.id.passwordEdit);
        confirmPasswordField = findViewById(R.id.confirmNewPassword);

        realm = Realm.getDefaultInstance();

        // For the pre-populating fields when wanting to edit the profile
        String oldUsername = getIntent().getStringExtra("username_initial");
        String oldPassword = getIntent().getStringExtra("password_initial");

        usernameField.setText(oldUsername);
        passwordField.setText(oldPassword);
        confirmPasswordField.setText(oldPassword);

        // You want to get the current user from Realm
        userToEdit = realm.where(User.class)
                .equalTo("name", oldUsername)
                .findFirst();

        findViewById(R.id.saveButton).setOnClickListener(v -> saveUser());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
    }

    private void saveUser() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            StringBuilder message = new StringBuilder();
            if (username.isEmpty()) message.append("Username must not be blank!");
            if (password.isEmpty()) message.append("Password must not be blank!");
            Toast.makeText(this, message.toString().trim(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        User existingUser = realm.where(User.class)
                .equalTo("name", username)
                .findFirst();

        if (existingUser != null && !existingUser.getUuid().equals(userToEdit.getUuid())) {
            Toast.makeText(this, "User already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.beginTransaction();
        userToEdit.setName(username);
        userToEdit.setPassword(password);
        realm.commitTransaction();

        Toast.makeText(this, "User updated successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) {
            realm.close();
        }
    }
}
