package naguio.labs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import io.realm.Realm;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextPassword, editTextConfirmPassword;
    private ImageView imageViewProfile;
    private Button buttonSave, buttonCancel;

    private Realm realm;
    private SharedPreferences prefs;
    private File imageDir;

    private boolean imageHasBeenTaken = false;
    private byte[] jpeg;
    private String uuidCompact;

    public static final int REQUEST_CODE_IMAGE_SCREEN = 1234;
    public static final String TEMP_IMAGE_FILENAME = "temp_image";
    public static final String IMAGE_EXTENSION = ".jpeg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();
        prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);
        imageDir = getExternalCacheDir();

        initViews();
    }

    private void initViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        imageViewProfile = findViewById(R.id.editNewImage);
        buttonSave = findViewById(R.id.buttonSave);
        buttonCancel = findViewById(R.id.buttonCancel);

        imageViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageActivity.class);
            startActivityForResult(intent, REQUEST_CODE_IMAGE_SCREEN);
        });

        buttonSave.setOnClickListener(v -> saveUser());
        buttonCancel.setOnClickListener(v -> finish());

        // Load placeholder or previously taken image
        File tempImageFile = new File(imageDir, TEMP_IMAGE_FILENAME + IMAGE_EXTENSION);
        if (tempImageFile.exists()) {
            Picasso.get()
                    .load(tempImageFile)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(imageViewProfile);
        }
    }

    private void saveUser() {
        String username = editTextName.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Username must not be blank!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password must not be blank!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (realm.where(User.class).equalTo("name", username).findFirst() != null) {
            Toast.makeText(this, "User already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uuid = UUID.randomUUID().toString();
        uuidCompact = uuid.replace("-", "");

        realm.beginTransaction();
        User user = realm.createObject(User.class, uuid);
        user.setName(username);
        user.setPassword(password);
        user.setImageFilename(uuidCompact);
        realm.commitTransaction();

        // Save image permanently
        if (imageHasBeenTaken && jpeg != null) {
            try {
                File outFile = new File(imageDir, uuidCompact + IMAGE_EXTENSION);
                FileOutputStream fos = new FileOutputStream(outFile);
                fos.write(jpeg);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long count = realm.where(User.class).count();
        Toast.makeText(this, "New User saved. Total: " + count, Toast.LENGTH_SHORT).show();
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_SCREEN && resultCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN) {
            imageHasBeenTaken = true;
            jpeg = data.getByteArrayExtra("rawJpeg");

            if (jpeg != null) {
                try {
                    File tempFile = new File(imageDir, TEMP_IMAGE_FILENAME + IMAGE_EXTENSION);
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(jpeg);
                    fos.close();

                    Picasso.get()
                            .load(tempFile)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(imageViewProfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
