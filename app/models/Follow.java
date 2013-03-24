package models;

import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public abstract class Follow {

	private static final long serialVersionUID = 1L;

	@Id
	public String id;
    public String userId;
    public List<String> follow;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getFollow() {
        return follow;
    }

    public void setFollow(List<String> follow) {
        this.follow = follow;
    }

    // Creation method
    public Follow save() {
        MorphiaObject.datastore.save(this);
        return this;
    }

    public Follow init(String userId) {
        this.userId = userId;
        this.follow = new ArrayList<String>();
        // Fix - Manually create an ObjectID and get its String UUID
        this.id = new ObjectId().toString();
        this.save();
        return this;
    }

    // Delete method
    public Follow delete() {
        MorphiaObject.datastore.delete(this);
        return this;
    }

    // Update method
    public Follow update(List<String> follow) {
        this.setFollow(follow);
        return this.save();
    }

}
