package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.data.format.Formats;
import play.libs.Json;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.query.UpdateOperations;

import controllers.MorphiaObject;

@Entity
public class UserStatistics {
	
	private static final int LEVEL_FACTOR = 500;
	
	@Id
	public ObjectId id;
	
	public String userId;
	
	public int coins = 0;
	
	public int points = 0;
	
	public int level = 1;
	
	public boolean newLevel = false;

	public Map<String, Integer> statistics = new HashMap<String, Integer>();

	@Embedded("userAwards")
	public List<UserAwards> userAwards = new ArrayList<UserAwards>();

	
	public static UserStatistics findByUserId(String userId) {
		return	MorphiaObject.datastore.find(UserStatistics.class).
				field("userId").equal(userId).get();
	}
	
	public void updateStatistic(String statistic, Integer addingValue) {
		Integer previousValue = this.statistics.get(statistic);
		if(previousValue == null) {
			previousValue = new Integer(0);
		}
		Integer newValue = previousValue+addingValue;
		this.statistics.put(statistic, newValue);
		updateAwards(statistic, previousValue, newValue);
	}
	
	private void updateAwards(String statistic, Integer previousValue, Integer newValue) {
		List<Award> achievedAwards = Award.findByAwardTypeLimit(statistic, previousValue, newValue);
		if(achievedAwards != null){
			for(Award award : achievedAwards) {
				this.userAwards.add(new UserAwards(award.getIdentifier()));
				this.coins	+= award.coins;
				this.points += award.points;
			}
			updateLevel();
		}
	}
	
	private void updateLevel() {
		int newLevel = this.calculateLevel();
		if(newLevel != this.level){
			this.newLevel = true;
			this.level = newLevel;
		}
	}
	
	private int calculateLevel() {
		return (this.points / LEVEL_FACTOR)+1;
	}
	
	public static UserStatistics init(String userId) {
		UserStatistics userStatistics = new UserStatistics();
		userStatistics.userId = userId;
		for (StatisticTypes std : StatisticTypes.values()) {
			userStatistics.statistics.put(std.name(), 0);
		}
        userStatistics.save();
        return userStatistics;
    }
	
	public void setRead() {
		this.newLevel = false;
		for(UserAwards userAward : this.userAwards) {
			userAward.setRead();
		}
	}
	
	public UserStatistics save() {
		MorphiaObject.datastore.save(this);
		return this;
	}
	
	public UserStatistics update() {
		UpdateOperations<UserStatistics> ops = 
				MorphiaObject.datastore.createUpdateOperations(UserStatistics.class)
					.set("coins", this.coins)
					.set("points", this.points)
					.set("level", this.level)
					.set("newLevel", this.newLevel)
					.set("statistics", this.statistics)
					.set("userAwards", this.userAwards);
		MorphiaObject.datastore.updateFirst(MorphiaObject.datastore
				.createQuery(UserStatistics.class).field("userId")
				.equal(this.userId), ops);
		return this;
	}
	
	public static ObjectNode userStatisticsToObjectNode (UserStatistics userStatistics){
		ObjectNode statisticsNode = Json.newObject();
		statisticsNode.put("id", userStatistics.userId);
		statisticsNode.put("userId", userStatistics.userId);
		statisticsNode.put("coins", userStatistics.coins);
		statisticsNode.put("points", userStatistics.points);
		statisticsNode.put("level", userStatistics.level);
		statisticsNode.put("newLevel", userStatistics.newLevel);
		statisticsNode.put("statistics", Json.toJson(userStatistics.statistics));
		statisticsNode.put("userAwards", Json.toJson(UserAwards.userAwardsToObjectNodes(userStatistics.userAwards)));
		return statisticsNode;
	}
	
	@Embedded
	public static class UserAwards {
		
		public String award;
		
		@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
		public Date timeStamp;
		
		public boolean isNew;
		
		public UserAwards() {}
		
		// Save the award.
		public UserAwards(String awardId) {
			this.award = awardId;
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
			if(userAward != null){
				awardNode.put("award", Award.awardToObjectNode(Award.findById(userAward.award)));
				awardNode.put("timeStamp", Json.toJson(userAward.timeStamp));
				awardNode.put("isNew", userAward.isNew);
			}
			return awardNode;
		}
	}
	
}

