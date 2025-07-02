package naguio.labs;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class UserAdapter extends RealmRecyclerViewAdapter<User, UserAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView usernameLabel;
        TextView passwordLabel;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameLabel = itemView.findViewById(R.id.usernameLabel);
            passwordLabel = itemView.findViewById(R.id.passwordLabel);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private AdminActivity activity;

    public UserAdapter(AdminActivity activity, @Nullable OrderedRealmCollection<User> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Create the raw view for this ViewHolder
        View v = activity.getLayoutInflater().inflate(R.layout.user_layout, parent, false);  // VERY IMPORTANT TO USE THIS STYLE

        // Assign view to the viewholder
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // This will give you the data object at the given position
        User user = getItem(position);
        if (user == null) return;

        // Copy all the values needed to the appropriate views
        holder.usernameLabel.setText(user.getName());
        holder.passwordLabel.setText(user.getPassword());

        // This will tag the user object to buttons
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
