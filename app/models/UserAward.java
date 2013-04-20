package models;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import java.util.ArrayList;
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
	/*	
	// Search for all the non-read awards of an user.
	public static List<UserAward> findNewByUserId(String userId) {
		return	MorphiaObject.datastore.find(UserAward.class).
				field("userId").equal(userId).
				field("isNew").equal(true).order("-timeStamp").asList();
	}
	*/
	// TODO: Method to update the non-read awards.
	
	/** Parses an user award list and prepares it for exporting to JSON
	 * @param userAwardList UserAward list
	 * @return List of ObjectNodes ready for use in toJson
	 */
	public static List<ObjectNode> userAwardsToObjectNodes (List<UserAward> userAwardList){
		List<ObjectNode> userAwards = new ArrayList<ObjectNode>();
		for(UserAward userAward : userAwardList){
			userAwards.add(userAwardToObjectNode(userAward));
		}
		return userAwards;
	}
	
	/** Parses an UserAward and prepares it for exporting to JSON
	 * @param UserAward An User Award
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode userAwardToObjectNode (UserAward userAward){
		ObjectNode awardNode = Json.newObject();
		awardNode.put("award", Award.awardToObjectNode(Award.findById(userAward.award)));
		awardNode.put("timeStamp", Json.toJson(userAward.timeStamp));
		awardNode.put("isNew", userAward.isNew);
		return awardNode;
	}
}
