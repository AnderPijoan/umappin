package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * User: igarri
 * Date: 01/03/13
 * Time: 16.51
 */

@Entity
public class AwardTrigger {

	@Id
	public ObjectId id;
	
	public String triggerType;
	
	public int limit;
	
	public ObjectId award;
	
	public static List<AwardTrigger> all() {
        if (MorphiaObject.datastore != null) {
            return MorphiaObject.datastore.find(AwardTrigger.class).asList();
        } else {
            return new ArrayList<AwardTrigger>();
        }
    }
	
	public static AwardTrigger findByID(String id) {
		return MorphiaObject.datastore.get(AwardTrigger.class, new ObjectId(id));
	}
	
	public static List<AwardTrigger> find(String type) {
		return MorphiaObject.datastore.find(AwardTrigger.class).field("triggerType").equal(type).asList();
	}
	
	public static Award findByAwardTypeLimit(String type, Integer limit) {
		AwardTrigger awardTrigger =  MorphiaObject.datastore.find(AwardTrigger.class)
											.field("triggerType").equal(type)
											.field("limit").equal(limit).get();
		return awardTrigger !=null ? Award.findById(awardTrigger.award) : null;
	}
}
