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
	public UserAward(String userId, Award award) {
		this.userId = userId;
		this.award = award.id;
		this.timeStamp = new Date();
		this.isNew = true;
	}
	
	public void setRead() {
		this.isNew = false;
	}
	
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
