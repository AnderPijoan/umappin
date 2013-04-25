package models;

import java.util.ArrayList;
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
	
	public List<String> unread = new ArrayList<String>(); // Only unread discussions ids are stored
	
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
			for (String id : this.unread){
				Discussion discussion = MorphiaObject.datastore.get(Discussion.class, new ObjectId(id));
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
			setRead(id, true); // We set the discussion to read
			return MorphiaObject.datastore.get(Discussion.class, new ObjectId(id));
		} else {
			return null;
		}
	}
	
	public Discussion findDiscussionById(ObjectId oid) {
		String id = oid.toString();
		if (this.discussionIds.contains(id)){
			setRead(id, true); // We set the discussion to read
			return MorphiaObject.datastore.get(Discussion.class, id);
		} else {
			return null;
		}
	}
	
	/** Adds a new discussionId to the list and also to the unread list
	 * @param id
	 */
	public void addDiscussion(String id){
		if (!this.discussionIds.contains(id)){
			this.discussionIds.add(id);
			setRead(id, false);
		}
	}
	
	/** Sets the discussionId to read or unread
	 * @param id
	 * @param read
	 */
	public void setRead(String id, boolean read){
		if (read){
			unread.remove(id);
		} else if (!unread.contains(id)) {
			unread.add(id);
		}
	}
	
}
