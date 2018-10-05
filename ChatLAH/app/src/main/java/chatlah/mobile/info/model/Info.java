package chatlah.mobile.info.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Info {

    private Timestamp created_at;
    private Timestamp expires_on;
    private String description;
    private String photo_url;
    private String storage_path;
    private String title;

    // Item state
    private boolean expanded;

    public Info() {
    }

    public Info(Timestamp created_at, Timestamp expires_on, String description, String photo_url, String storage_path, String title) {
        this.created_at = created_at;
        this.expires_on = expires_on;
        this.description = description;
        this.photo_url = photo_url;
        this.storage_path = storage_path;
        this.title = title;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public Timestamp getExpires_on() {
        return expires_on;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public String getTitle() {
        return title;
    }

    public String getStorage_path() {
        return storage_path;
    }

    // Get item state
    public boolean isExpanded() {
        return expanded;
    }

    // Set item state
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
