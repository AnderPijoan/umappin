package models;

//import javax.persistence.Entity;
//import javax.persistence.Id;

//import play.db.ebean.Model;
import be.objectify.deadbolt.core.models.Permission;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;


/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
public class UserPermission implements Permission {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public ObjectId id;

	public String value;

	//public static final Model.Finder<Long, UserPermission> find = new Model.Finder<Long, UserPermission>(Long.class, UserPermission.class);

	public String getValue() {
		return value;
	}

    public void save() {
        MorphiaObject.datastore.save(this);
    }

	public static UserPermission findByValue(String value) {
        return MorphiaObject.datastore.find(UserPermission.class)
                .field("value").equal(value).get();
		//return find.where().eq("value", value).findUnique();
	}
}
