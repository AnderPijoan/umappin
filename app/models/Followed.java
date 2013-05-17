package models;

import java.util.List;
import java.util.ArrayList;
import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;

import com.google.code.morphia.annotations.Entity;
@Entity
public class Followed extends Follow {
	public static Followed findByUserId(ObjectId oId) {
		return MorphiaObject.datastore.find(Followed.class).
			field("userId").equal(oId.toString()).field("follow").notEqual(null).get();
	}
}
