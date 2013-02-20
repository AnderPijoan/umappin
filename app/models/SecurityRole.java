/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package models;

//import javax.persistence.Entity;
//import javax.persistence.Id;

import be.objectify.deadbolt.core.models.Role;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class SecurityRole  implements Role {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public ObjectId id;

	public String roleName;

	//public static final Finder<Long, SecurityRole> find = new Finder<Long, SecurityRole>(Long.class, SecurityRole.class);

	@Override
	public String getName() {
		return roleName;
	}

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public static List<SecurityRole> all() {
        if (MorphiaObject.datastore != null) {
            return MorphiaObject.datastore.find(SecurityRole.class).asList();
        } else {
            return new ArrayList<SecurityRole>();
        }
    }

    public static void delete(String idToDelete) {
        SecurityRole toDelete = MorphiaObject.datastore.find(SecurityRole.class)
                                    .field("_id").equal(new ObjectId(idToDelete)).get();
        if (toDelete != null) {
            Logger.info("toDelete: " + toDelete);
            MorphiaObject.datastore.delete(toDelete);
        } else {
            Logger.debug("ID No Found: " + idToDelete);
        }
    }

	public static SecurityRole findByRoleName(String roleName) {
        return MorphiaObject.datastore.find(SecurityRole.class)
                .field("roleName").equal(roleName).get();
		//return find.where().eq("roleName", roleName).findUnique();
	}
}
