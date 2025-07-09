package naguio.labs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.Realm;

public class LandingActivity extends AppCompatActivity {

    TextView welcomeText;
    ImageView userImage;
    SharedPreferences prefs;
    Realm realm;

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
        realm = Realm.getDefaultInstance();

        initViews();
    }

    public void initViews() {
        welcomeText = findViewById(R.id.welcomeText);
        userImage = findViewById(R.id.imageLanding);

        String uuid = prefs.getString("uuid", null);
        boolean isRemembered = prefs.getBoolean("rememberMe", false);

        if (uuid != null) {
            User user = realm.where(User.class).equalTo("uuid", uuid).findFirst();

            if (user != null) {
                String name = user.getName();
                String imageFilename = user.getImageFilename();
                String welcomeMsg = "Welcome " + name + "!!!";

                if (isRemembered) {
                    welcomeMsg += " You will be remembered.";
                }

                welcomeText.setText(welcomeMsg);

                File cacheDir = getExternalCacheDir();
                File imageFile;

                if (imageFilename != null && !imageFilename.isEmpty()) {
                    imageFile = new File(cacheDir, imageFilename + ".jpeg");
                } else {
                    imageFile = new File(cacheDir, uuid + ".jpeg");
                }

                if (imageFile.exists()) {
                    Picasso.get()
                            .load(imageFile)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(userImage);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
