package models;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;

@Entity
public class Follows extends Follow {
	
	public static Follows findByUserId(ObjectId oId) {
		return MorphiaObject.datastore.find(Follows.class).
			field("userId").equal(oId.toString()).field("follow").notEqual(null).get();
	}
	
}
