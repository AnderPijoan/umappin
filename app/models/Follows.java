package models;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Follows extends Follow {
	
	public static Follows findByUserId(ObjectId oId) {
		return MorphiaObject.datastore.find(Follows.class).
			field("userId").equal(oId.toString()).field("follow").notEqual(null).get();
	}

    public static List<Follows> findRelatedByUserId(String id) {
        List<Follows> allfollows = MorphiaObject.datastore.find(Follows.class).asList();
        List<Follows> follows = new ArrayList<>();
        for (Follows f : allfollows)
            if (f.userId.equals(id)/* || (f.follow != null && f.follow.contains(id))*/)
                follows.add(f);
        return follows;
    }
	
}
