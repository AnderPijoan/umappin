package models;

import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.mapping.Mapper;
import com.google.code.morphia.query.UpdateOperations;

import controllers.MorphiaObject;

@Entity
public class UserStatistics {
	@Id
	public String userId;

	public Map<String, Integer> statistics = new HashMap<String, Integer>();

	public static UserStatistics findByUserId(String userId) {
		return MorphiaObject.datastore.get(UserStatistics.class, new ObjectId(userId));
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
	
	public static UserStatistics init(String userId) {
		UserStatistics userStatistics = new UserStatistics();
		userStatistics.userId = userId;
		userStatistics.statistics = new HashMap<String, Integer>();
        //TODO: initialize the hashmap values to 0...
        return userStatistics;
    }
	
	public UserStatistics save() {
		MorphiaObject.datastore.save(this);
		return this;
	}
	
	public UserStatistics update() {
		UpdateOperations<UserStatistics> ops = 
				MorphiaObject.datastore.createUpdateOperations(UserStatistics.class)
					.set("statistics", this.statistics);

		//update, if not found create it
		MorphiaObject.datastore.updateFirst(MorphiaObject.datastore
				.createQuery(UserStatistics.class).field(Mapper.ID_KEY)
				.equal(new ObjectId(this.userId)), ops, true);
		return this;
	}
	
	public static ObjectNode userStatisticsToObjectNode (UserStatistics userStatistics){
		ObjectNode statisticsNode = Json.newObject();
		statisticsNode.put("id", userStatistics.userId);
		statisticsNode.put("statistics", Json.toJson(userStatistics.statistics));
		return statisticsNode;
	}
}
