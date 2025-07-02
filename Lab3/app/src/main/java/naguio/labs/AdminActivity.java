package naguio.labs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button addButton;
    private Button clearButton;

    private SharedPreferences prefs;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        init();
    }

    public void init() {
        recyclerView = findViewById(R.id.usersList);
        addButton = findViewById(R.id.addButton);
        clearButton = findViewById(R.id.clearButton);
        realm = Realm.getDefaultInstance();
        prefs = getSharedPreferences("my_prefs", MODE_PRIVATE);

        // This is the setup for RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        RealmResults<User> allUsers = realm.where(User.class).findAll();
        UserAdapter adapter = new UserAdapter(this, allUsers, true);
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        clearButton.setOnClickListener(view -> {
            showClearDialog();
        });

        prefs.edit().putBoolean("user_deleted", true).apply();
    }

    // For some reason, executeTransactionAsync works and not when I tried begin and commit Transaction... Weird.
    private void showClearDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Users")
                .setMessage("Are you sure you want to delete all users?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    realm.executeTransactionAsync(r -> r.where(User.class).findAll().deleteAllFromRealm());
                        prefs.edit().putBoolean("user_deleted", true).apply();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // This is called from UserAdapter when the delete button is clicked
    public void showDeleteDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    realm.beginTransaction();
                    User userToDelete = realm.where(User.class)
                            .equalTo("uuid", user.getUuid())
                            .findFirst();
                    if (userToDelete != null) {
                        userToDelete.deleteFromRealm();
                        prefs.edit().putBoolean("user_deleted", true).apply();
                    }
                    realm.commitTransaction();
                })
                .setNegativeButton("No", null)
                .show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) {
            realm.close();
        }
    }
}
