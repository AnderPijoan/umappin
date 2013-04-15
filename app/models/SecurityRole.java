package models;

import be.objectify.deadbolt.core.models.Role;
import com.google.code.morphia.annotations.Entity;
import controllers.MorphiaObject;

@Entity
public class SecurityRole extends Item implements Role {

	private static final long serialVersionUID = 1L;

    /** ------------------------ Attributes ------------------------- **/

    public String name;
	public String roleName;

    /** ------------------------ Getters / Setters ------------------------- **/

    @Override
	public String getName() {
		return roleName;
	}

    /** ------------------------ Authentication methods -------------------------- **/

	public static SecurityRole findByRoleName(String roleName) {
        return MorphiaObject.datastore.find(SecurityRole.class)
                .field("roleName").equal(roleName).get();
	}
}
