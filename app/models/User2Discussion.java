package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class User2Discussion extends Item {

    /** ------------------------ Attributes ------------------------- **/
	
	@Id
	public String userId;
	
	public List<String> discussionIds;
	
	public Map<String, Date> lastReadTimeStamp = new HashMap<String, Date>();
	
    /** ------------------------- Methods -------------------------- **/
	
	public List<Discussion> all() {
		if (MorphiaObject.datastore != null) {
			List<Discussion> result = new ArrayList<Discussion>();
			for (String id : this.discussionIds){
				result.add(MorphiaObject.datastore.get(Discussion.class, new ObjectId(id)));
			}
			return result;
		} else {
			return new ArrayList<Discussion>();
		}
	}
	
	public List<Discussion> unread() {
		if (MorphiaObject.datastore != null) {
			List<Discussion> result = new ArrayList<Discussion>();
			for (String id : this.discussionIds){
				
				Discussion discussion = MorphiaObject.datastore.get(Discussion.class, new ObjectId(id));
				if(discussion.lastWrote.after(lastReadTimeStamp.get(discussion.id.toString())))
					result.add(discussion);
			}
			return result;
		} else {
			return new ArrayList<Discussion>();
		}
	}
	
	public void save() {
		MorphiaObject.datastore.save(this);
	}
	
	public static User2Discussion findById(String id) {
		return MorphiaObject.datastore.get(User2Discussion.class, id);
	}
	
	public Discussion findDiscussionById(String id) {
		if (this.discussionIds.contains(id)){
			lastReadTimeStamp.put(id, new Date());
			return MorphiaObject.datastore.get(Discussion.class, new ObjectId(id));
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
	
	public void setReadTimeStamp(String id){
		lastReadTimeStamp.put(id, new Date());
	}
	
}
