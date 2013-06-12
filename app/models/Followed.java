package models;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Followed extends Follow {
	public static Followed findByUserId(ObjectId oId) {
		return MorphiaObject.datastore.find(Followed.class).
			field("userId").equal(oId.toString()).field("follow").notEqual(null).get();
	}

    public static List<Followed> findRelatedByUserId(String id) {
        List<Followed> allfollowed = MorphiaObject.datastore.find(Followed.class).asList();
        Follows userFollows = Follows.findByUserId(new ObjectId(id));
        Followed userFollowed = Followed.findByUserId(new ObjectId(id));
        List<Followed> follows = new ArrayList<>();
        for (Followed f : allfollowed)
            if (f.userId.equals(id) || (userFollowed != null && userFollowed.follow.contains(f.userId)) || (userFollows != null && userFollows.follow.contains(f.userId)))
                follows.add(f);
        return follows;
    }
}
