package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Award {
	
	@Id
	public ObjectId id;

	public String name;
	
	public String description;
	
	// Award picture..

	public int coins;
	
	public int points;

	// Get all the awards.
	public static List<Award> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Award.class).asList();
		} else {
			return new ArrayList<Award>();
		}
	}

	// Get one spedific award.
	public static Award findById(ObjectId id) {
		return MorphiaObject.datastore.get(Award.class, id);
	}
	
	// Get one spedific award.
	public static Award findById(String id) {
		return MorphiaObject.datastore.get(Award.class, new ObjectId(id));
	}
	
	/** Parses an award list and prepares it for exporting to JSON
	 * @param awardList Award list
	 * @return List of ObjectNodes ready for use in toJson
	 */
	public static List<ObjectNode> awardsToObjectNodes (List<Award> awardList){
		List<ObjectNode> awards = new ArrayList<ObjectNode>();
		for(Award award : awardList){
			awards.add(awardToObjectNode(award));
		}
		return awards;
	}
	
	/** Parses an award and prepares it for exporting to JSON
	 * @param award An Award
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode awardToObjectNode (Award award){
		ObjectNode awardNode = Json.newObject();
		awardNode.put("id", award.id.toString());
		awardNode.put("name", award.name);
		awardNode.put("description", award.description);
		awardNode.put("coins", award.coins);
		awardNode.put("points", award.points);
		return awardNode;
	}
}
