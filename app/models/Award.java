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
 * Time: 16.44
 */

@Entity
public class Award {
	
	@Id
	public ObjectId id;

	public String name;
	
	public String description;

	public int coins;
	
	// public int points;

	public static List<Award> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Award.class).asList();
		} else {
			return new ArrayList<Award>();
		}
	}

	public static Award findById(ObjectId id) {
		return MorphiaObject.datastore.get(Award.class, id);
	}
}
