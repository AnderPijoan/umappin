package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Followed {

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

    public void setFollow(List<String> follows) {
        this.follow = follows;
    }

    // Creation methods
    public Followed save() {
        MorphiaObject.datastore.save(this);
        return this;
    }


    public static Followed create(String userId) {
        final Followed follows = new Followed();
        follows.userId = userId;
        follows.follow = new ArrayList<String>();
        // Fix - Manually create an ObjectID and get its String UUID
        follows.id = new ObjectId().toString();
        follows.save();
        return follows;
    }


    // Search methods
    public static List<Followed> all() {
        if (MorphiaObject.datastore != null) {
            List<Followed> res = MorphiaObject.datastore.find(Followed.class).asList();
            if (res != null)
                return res;
        }
        return new ArrayList<Followed>();
    }

    public static Followed findById(String id) {
        return MorphiaObject.datastore.find(Followed.class).field("_id").equal(id).get();
    }

    public static Followed findByUserId(String userId) {
        return MorphiaObject.datastore.find(Followed.class).field("userId").equal(userId).get();
    }

    // Delete methods
    public Followed delete() {
        MorphiaObject.datastore.delete(this);
        return this;
    }

    public static Followed delete(String userId) {
        Followed follows = findByUserId(userId);
        if (follows != null)
            return follows.delete();
        return null;
    }


    // Update methods
    public Followed update(List<String> follows) {
        this.setFollow(follows);
        return this.save();
    }

    public static Followed update(String userId, List<String> followList) {
        Followed follows = findByUserId(userId);
        if (follows != null)
            return follows.update(followList);
        return null;
    }

}
