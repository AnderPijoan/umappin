package models;

import java.util.List;
import java.util.ArrayList;
import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;

import com.google.code.morphia.annotations.Entity;

@Entity
public class Follows extends Follow {

	public static Follows findByUserId(ObjectId oId) {
		return MorphiaObject.datastore.find(Follows.class).
			field("userId").equal(oId.toString()).get();
	}
}
