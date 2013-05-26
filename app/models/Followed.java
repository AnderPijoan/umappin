package models;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;

@Entity
public class Followed extends Follow {
	public static Followed findByUserId(ObjectId oId) {
		return MorphiaObject.datastore.find(Followed.class).
			field("userId").equal(oId.toString()).field("follow").notEqual(null).get();
	}
}
