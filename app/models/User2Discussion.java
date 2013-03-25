package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;

/**
 * User: a.pijoan
 * Date: 16/03/13
 * Time: 12:34
 */
@Entity
public class User2Discussion {

	@Id
	public String userId;
	
	public List<String> discussionIds;
	
	
	public List<Discussion> all() {
		if (MorphiaObject.datastore != null) {
			List<Discussion> result = new ArrayList<Discussion>();
			for (String id : this.discussionIds){
				result.add(MorphiaObject.datastore.get(Discussion.class, id));
			}
			return result;
		} else {
			return new ArrayList<Discussion>();
		}
	}
	
	public String save() {
		MorphiaObject.datastore.save(this);
		return this.userId;
	}
	
	public static User2Discussion findById(String id) {
		return MorphiaObject.datastore.get(User2Discussion.class, id);
	}
	
	public Discussion findDiscussionById(String id) {
		if (this.discussionIds.contains(id)){
			return MorphiaObject.datastore.get(Discussion.class, id);
		} else {
			return null;
		}
	}
	
	public Discussion findDiscussionById(ObjectId oid) {
		String id = oid.toString();
		if (this.discussionIds.contains(id)){
			return MorphiaObject.datastore.get(Discussion.class, id);
		} else {
			return null;
		}
	}
	
}
