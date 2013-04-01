package models;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

@Entity
public class UserAward {
	@Id
	public ObjectId id;
	
	public String userId;
	
	public ObjectId award;
	
	public Date timeStamp;
	
	public boolean isNew;
	
	// Save the award.
	public String save() {
		timeStamp = new Date();
		isNew = true;
		MorphiaObject.datastore.save(this);
		return this.userId;
	}
	
	// Search for all the awards of an user.
	public static List<UserAward> findByUserId(String userId) {
		return	MorphiaObject.datastore.find(UserAward.class).
				field("userId").equal(userId).order("-timeStamp").asList();
	}
	
	// Search for all the non-read awards of an user.
	public static List<UserAward> findNewByUserId(String userId) {
		return	MorphiaObject.datastore.find(UserAward.class).
				field("userId").equal(userId).
				field("isNew").equal(true).order("-timeStamp").asList();
	}
	
	// TODO: Method to update the non-read awards.
}
