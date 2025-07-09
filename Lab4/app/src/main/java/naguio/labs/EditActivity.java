package naguio.labs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class EditActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private ImageView userImage;

    private Realm realm;
    private User userToEdit;

    private boolean imageUpdated = false;
    private byte[] jpeg;

    public static final int REQUEST_CODE_IMAGE_SCREEN = 1001;

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
        userImage = findViewById(R.id.editImage);

        realm = Realm.getDefaultInstance();

        // Pre-populate fields
        String oldUsername = getIntent().getStringExtra("username_initial");
        String oldPassword = getIntent().getStringExtra("password_initial");

        usernameField.setText(oldUsername);
        passwordField.setText(oldPassword);
        confirmPasswordField.setText(oldPassword);

        // Retrieve the current user from Realm
        userToEdit = realm.where(User.class)
                .equalTo("name", oldUsername)
                .findFirst();

        // Use the stored image filename to load the image
        if (userToEdit != null && userToEdit.getImageFilename() != null) {
            loadImageFromCache(userToEdit.getImageFilename());
        }

        findViewById(R.id.saveButton).setOnClickListener(v -> saveUser());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
        userImage.setOnClickListener(v -> takePhoto());
    }

    private void saveUser() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            if (username.isEmpty())
                msg.append("Username must not be blank!\n");
            if (password.isEmpty())
                msg.append("Password must not be blank!");
            Toast.makeText(this, msg.toString().trim(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        User existingUser = realm.where(User.class).equalTo("name", username).findFirst();
        if (existingUser != null && !existingUser.getUuid().equals(userToEdit.getUuid())) {
            Toast.makeText(this, "User already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        realm.beginTransaction();
        userToEdit.setName(username);
        userToEdit.setPassword(password);
        realm.commitTransaction();

        if (imageUpdated && jpeg != null) {
            try {
                realm.beginTransaction();
                // Set image filename if it hasn't been set yet
                if (userToEdit.getImageFilename() == null) {
                    userToEdit.setImageFilename(userToEdit.getUuid()); // ✅ use UUID as filename base
                }
                realm.commitTransaction();

                saveImageToCache(userToEdit.getImageFilename(), jpeg); // ✅ now it's not null
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Toast.makeText(this, "User updated successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void takePhoto() {
        Intent i = new Intent(this, ImageActivity.class);
        startActivityForResult(i, REQUEST_CODE_IMAGE_SCREEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_SCREEN &&
                resultCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN) {

            jpeg = data.getByteArrayExtra("rawJpeg");
            imageUpdated = true;

            try {
                // Save as a temporary file and display it
                File tempFile = saveImageToCache("temp_" + userToEdit.getUuid(), jpeg);
                loadImage(tempFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Changed parameter: use filename (without extension) from the user's imageFilename
    private void loadImageFromCache(String filename) {
        File file = new File(getExternalCacheDir(), filename + ".jpeg");
        if (file.exists()) {
            loadImage(file);
        }
    }

    private void loadImage(File file) {
        Picasso.get()
                .load(file)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(userImage);
    }

    // Save image under a given filename (without extension)
    private File saveImageToCache(String filename, byte[] jpeg) throws IOException {
        File file = new File(getExternalCacheDir(), filename + ".jpeg");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(jpeg);
        fos.close();
        return file;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) {
            realm.close();
        }
    }
}
