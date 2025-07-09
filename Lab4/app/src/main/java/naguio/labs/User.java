package naguio.labs;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class User extends RealmObject {

    @PrimaryKey
    private String uuid;

    @Required
    private String name;

    @Required
    private String password;

    private String imageFilename;

    public User() {
    }

    public User(String uuid, String name, String password, String imageFilename) {
        this.uuid = uuid;
        this.name = name;
        this.password = password;
        this.imageFilename = imageFilename;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    @Override
    public String toString() {
        return "User{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", imageFilename='" + imageFilename + '\'' +
                '}';
    }
}
