package naguio.labs;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class UserAdapter extends RealmRecyclerViewAdapter<User, UserAdapter.ViewHolder> {

    private final AdminActivity activity;

    public UserAdapter(AdminActivity activity, @Nullable OrderedRealmCollection<User> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageUser;
        TextView usernameLabel;
        TextView passwordLabel;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageUser = itemView.findViewById(R.id.imageUser);
            usernameLabel = itemView.findViewById(R.id.usernameLabel);
            passwordLabel = itemView.findViewById(R.id.passwordLabel);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.user_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = getItem(position);
        if (user == null) return;

        holder.usernameLabel.setText(user.getName());
        holder.passwordLabel.setText(user.getPassword());

        // Load image from external cache with .jpeg extension
        if (user.getImageFilename() != null && !user.getImageFilename().isEmpty()) {
            File imageFile = new File(activity.getExternalCacheDir(), user.getImageFilename() + ".jpeg");
            if (imageFile.exists()) {
                Picasso.get()
                        .load(imageFile)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(holder.imageUser);
            } else {
                Log.w("UserAdapter", "Image file not found: " + imageFile.getAbsolutePath());
                holder.imageUser.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            holder.imageUser.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.editButton.setTag(user);
        holder.deleteButton.setTag(user);

        holder.editButton.setOnClickListener(v -> {
            User u = (User) v.getTag();
            Intent intent = new Intent(activity, EditActivity.class);
            intent.putExtra("uuid", u.getUuid());
            intent.putExtra("username_initial", u.getName());
            intent.putExtra("password_initial", u.getPassword());
            activity.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            User u = (User) v.getTag();
            activity.showDeleteDialog(u);
        });
    }
}

