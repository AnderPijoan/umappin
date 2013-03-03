package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: igarri
 * Date: 01/03/13
 * Time: 16.44
 */

@Entity
public class Award {
	
	@Id
	public String id;

	public String name;

	public int coins;
	
	public AwardTrigger trigger;  // Could an award be triggered by more than one Trigger? If yes, this should be a List.

	public static List<Award> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Award.class).asList();
		} else {
			return new ArrayList<Award>();
		}
	}

	public static Award findById(String id) {
		return MorphiaObject.datastore.get(Award.class, new ObjectId(id));
	}
}
