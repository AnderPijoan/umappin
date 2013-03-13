package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;

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



    public void save() {
        //Logger.debug("Saving " + this.name +" to " + MorphiaObject.datastore.getDB());
        MorphiaObject.datastore.save(this);
    }

    public static Follows create(User user) {
        final Follows follows = new Follows();
        follows.userId = user.id;
        follows.follows = new ArrayList<String>();
        follows.save();
        return follows;
    }



    public static List<Follows> all() {
        if (MorphiaObject.datastore != null) {
            return MorphiaObject.datastore.find(Follows.class).asList();
        } else {
            return new ArrayList<Follows>();
        }
    }

    public static Follows findById(String id) {
        return MorphiaObject.datastore.find(Follows.class).field("_id").equal(id).get();
    }

    public static Follows findByUserId(String id) {
        return MorphiaObject.datastore.find(Follows.class).field("userId").equal(id).get();
    }


    public Follows delete() {
        MorphiaObject.datastore.delete(this);
        return this;
    }



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
            follows = Follows.create(follower);
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
