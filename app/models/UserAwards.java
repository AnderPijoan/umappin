package models;
import com.google.code.morphia.annotations.Embedded;

import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embedded
public class UserAwards {
	
	public String userId;
	
	public String award;
	
	public Date timeStamp;
	
	public boolean isNew;
	
	// Save the award.
	public UserAwards(String userId, Award award) {
		this.userId = userId;
		this.award = award.id.toString();
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
	public static List<ObjectNode> userAwardsToObjectNodes (List<UserAwards> userAwardList){
		List<ObjectNode> userAwards = new ArrayList<ObjectNode>();
		for(UserAwards userAward : userAwardList){
			userAwards.add(userAwardToObjectNode(userAward));
		}
		return userAwards;
	}
	
	/** Parses an UserAward and prepares it for exporting to JSON
	 * @param UserAwards An User Award
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode userAwardToObjectNode (UserAwards userAward){
		ObjectNode awardNode = Json.newObject();
		awardNode.put("userId", Json.toJson(userAward.userId));
		//awardNode.put("award", Award.awardToObjectNode(Award.findById(userAward.award)));
		//awardNode.put("timeStamp", Json.toJson(userAward.timeStamp));
		//awardNode.put("isNew", userAward.isNew);
		return awardNode;
	}
}
