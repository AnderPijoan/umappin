package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AwardTrigger {

	@Id
	public ObjectId id;
	
	public String triggerType;
	
	public int limit;
	
	public ObjectId award;
	
	// Get all the award triggers.
	public static List<AwardTrigger> all() {
        if (MorphiaObject.datastore != null) {
            return MorphiaObject.datastore.find(AwardTrigger.class).asList();
        } else {
            return new ArrayList<AwardTrigger>();
        }
    }
	
	// Get one specific Award Trigger.
	public static AwardTrigger findByID(String id) {
		return MorphiaObject.datastore.get(AwardTrigger.class, new ObjectId(id));
	}
	
	// Search for the award Triggers of one specific type.
	public static List<AwardTrigger> find(String type) {
		return MorphiaObject.datastore.find(AwardTrigger.class).field("triggerType").
											equal(type).asList();
	}
	
	// Search for the awards of one specific type and one specific limit.
	public static Award findByAwardTypeLimit(String type, Integer limit) {
		AwardTrigger awardTrigger =  MorphiaObject.datastore.find(AwardTrigger.class)
											.field("triggerType").equal(type)
											.field("limit").equal(limit).get();
		return awardTrigger !=null ? Award.findById(awardTrigger.award) : null;
	}
}
