package naguio.labs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.realm.Realm;

public class LandingActivity extends AppCompatActivity {

    TextView welcomeText;
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

        String uuid = prefs.getString("uuid", null);
        boolean isRemembered = prefs.getBoolean("rememberMe", false);

        if (uuid != null) {
            User user = realm.where(User.class)
                    .equalTo("uuid", uuid)
                    .findFirst();

            if (user != null) {
                String message = "Welcome " + user.getName() + "!!!";
                if (isRemembered) {
                    message += "\nYou will be remembered.";
                }
                welcomeText.setText(message);
                return;
            }
        }

        welcomeText.setText("Welcome Guest!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) realm.close();
    }
}
