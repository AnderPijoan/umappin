package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Award {
	
	@Id
	public ObjectId id;

	public String name;
	
	public String description;

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
}
