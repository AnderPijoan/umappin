package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;

import org.bson.types.ObjectId;
import play.Logger;

import java.util.*;

@Entity
public class Follows {

	private static final long serialVersionUID = 1L;

	@Id
	public String id;

	public String userId;

    public List<String> follows;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFollows() {
        return follows;
    }

    public void setFollows(List<String> follows) {
        this.follows = follows;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    // Creation methods
    public Follows save() {
        MorphiaObject.datastore.save(this);
        return this;
    }


    public static Follows create(String userId) {
        final Follows follows = new Follows();
        follows.userId = userId;
        follows.follows = new ArrayList<String>();
        // Fix - Manually create an ObjectID and get its String UUID
        follows.id = new ObjectId().toString();
        follows.save();
        return follows;
    }


    // Search methods
    public static List<Follows> all() {
        if (MorphiaObject.datastore != null) {
            List<Follows> res = MorphiaObject.datastore.find(Follows.class).asList();
            if (res != null)
                return res;
        }
        return new ArrayList<Follows>();
    }

    public static Follows findById(String id) {
        return MorphiaObject.datastore.find(Follows.class).field("_id").equal(id).get();
    }

    public static Follows findByUserId(String userId) {
        return MorphiaObject.datastore.find(Follows.class).field("userId").equal(userId).get();
    }


    // Delete methods
    public Follows delete() {
        MorphiaObject.datastore.delete(this);
        return this;
    }

    public static Follows delete(String userId) {
        Follows follows = findByUserId(userId);
        if (follows != null)
            return follows.delete();
        return null;
    }


    // Update methods
    public Follows update(List<String> follows) {
        this.setFollows(follows);
        return this.save();
    }

    public static Follows update(String userId, List<String> followList) {
        Follows follows = findByUserId(userId);
        if (follows != null)
            return follows.update(followList);
        return null;
    }



    // TODO Overview this!!!

    public Follows follow(User user) {
        if (this.follows == null)
            this.follows = new ArrayList<String>();
        this.follows.add(user.id);
        this.save();
        return this;
    }

    public static Follows follow(User follower, User followed) {
        Follows follows = MorphiaObject.datastore.find(Follows.class).field("userId").equal(follower.id).get();
        if (follows == null)
            follows = Follows.create(follower.id);
        return follows.follow(followed);
    }

    public Follows unfollow(User followed) {
        //Followed toUnfollow = MorphiaObject.datastore.find(Followed.class).field("userId").equal(followed.id).get();
        if (followed != null) {
            this.follows.remove(followed.id);
            this.save();
        }
        return this;
    }

    public static Follows unfollow(User follower, User followed) {
        Follows follows = MorphiaObject.datastore.find(Follows.class).field("userId").equal(follower.id).get();
        if (follows != null)
            return follows.unfollow(followed);
        else return null;
    }

}
