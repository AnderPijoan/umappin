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
    public Follows save() {
        MorphiaObject.datastore.save(this);
        return this;
    }


    public static Follows create(String userId) {
        final Follows follows = new Follows();
        follows.userId = userId;
        follows.follow = new ArrayList<String>();
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
        this.setFollow(follows);
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
        if (this.follow == null)
            this.follow = new ArrayList<String>();
        this.follow.add(user.id);
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
            this.follow.remove(followed.id);
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
