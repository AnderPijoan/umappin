package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.query.QueryResults;
import com.google.code.morphia.query.UpdateOperations;

import controllers.MorphiaObject;

@Entity
public class UserStatistics {
	
	private static final int LEVEL_FACTOR = 500;
	
	@Id
	public ObjectId id;
	
	public String userId;
	
	public int points = 0;
	
	public int level = 1;
	
	public boolean newLevel = false;

	public Map<String, Integer> statistics = new HashMap<String, Integer>();
	
	@Embedded("UserAwards")
	public List<UserAwards> userAwards = new ArrayList<UserAwards>();

	public static UserStatistics findByUserId(String userId) {
		QueryResults<UserStatistics> aux =	MorphiaObject.datastore.find(UserStatistics.class).
				field("userId").equal(userId);
		System.out.println("Elmts: "+aux.get().userAwards.size());
		return aux.get();
	}
	
	public static UserStatistics updateByUserId(String userId, Map<String, Integer> newStatistics) {
		UserStatistics userStatistics = findByUserId(userId);
		if(userStatistics == null) {
			userStatistics = UserStatistics.init(userId);
		}
		//TODO: update each item
        userStatistics.update();
        return userStatistics;
    }
	
	public void updateStatistic(String statistic, Integer addingValue) {
		Integer previousValue = this.statistics.get(statistic);
		Integer newValue = previousValue+addingValue;
		this.statistics.put(statistic, newValue);
		updateAwards(statistic, previousValue, newValue);
	}
	
	private void updateAwards(String statistic, Integer previousValue, Integer newValue) {
		List<Award> achievedAwards = Award.findByAwardTypeLimit(statistic, previousValue, newValue);
		if(achievedAwards != null){
			for(Award award : achievedAwards) {
				this.userAwards.add(new UserAwards(statistic, award));
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
		return this.points / LEVEL_FACTOR;
	}
	
	public static UserStatistics init(String userId) {
		UserStatistics userStatistics = new UserStatistics();
		userStatistics.userId = userId;
        //TODO: initialize the hashmap values to 0...
        return userStatistics;
    }
	
	public void setRead() {
		this.newLevel = false;
		for(UserAwards userAward : this.userAwards) {
			//userAward.setRead();
		}
	}
	
	public UserStatistics save() {
		MorphiaObject.datastore.save(this);
		return this;
	}
	
	public UserStatistics update() {
		UpdateOperations<UserStatistics> ops = 
				MorphiaObject.datastore.createUpdateOperations(UserStatistics.class)
					.set("points", this.points)
					.set("level", this.level)
					.set("newLevel", this.newLevel)
					.set("statistics", this.statistics)
					.set("userAwards", this.userAwards);

		//update, if not found create it
		MorphiaObject.datastore.updateFirst(MorphiaObject.datastore
				.createQuery(UserStatistics.class).field(Mapper.ID_KEY)
				.equal(new ObjectId(this.userId)), ops, true);
		return this;
	}
	
	public static ObjectNode userStatisticsToObjectNode (UserStatistics userStatistics){
		ObjectNode statisticsNode = Json.newObject();
		statisticsNode.put("id", userStatistics.userId);
		statisticsNode.put("points", userStatistics.points);
		statisticsNode.put("level", userStatistics.level);
		statisticsNode.put("newLevel", userStatistics.newLevel);
		statisticsNode.put("statistics", Json.toJson(userStatistics.statistics));
		statisticsNode.put("userAwards", Json.toJson(UserAwards.userAwardsToObjectNodes(userStatistics.userAwards)));
		return statisticsNode;
	}
}

