package models;

import java.util.ArrayList;
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
public class User2Discussion extends Item {

	/** ------------------------ Attributes ------------------------- **/

	@Id
	public ObjectId id; // Users ObjectId to fetch discussions instantly

	public List<ObjectId> discussionIds;

	public List<ObjectId> unread; // Only unread discussions ids are stored

	/** ------------------------- Methods -------------------------- **/

	public List<Discussion> all() {
		if (MorphiaObject.datastore != null) {
			List<Discussion> result = new ArrayList<Discussion>();
			for (ObjectId oid : discussionIds){
				result.add(MorphiaObject.datastore.get(Discussion.class, oid));
			}
			return result;
		} else {
			return new ArrayList<Discussion>();
		}
	}

	public List<Discussion> unread() {
		if (unread != null) {
			List<Discussion> result = new ArrayList<Discussion>();
			for (ObjectId oid : unread){
				Discussion discussion = MorphiaObject.datastore.get(Discussion.class, oid);
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
		return MorphiaObject.datastore.get(User2Discussion.class, new ObjectId(id));
	}

	public static User2Discussion findById(ObjectId oid) {
		return MorphiaObject.datastore.get(User2Discussion.class, oid);
	}

	public Discussion findDiscussionById(String id) {
		if (discussionIds != null && discussionIds.contains(new ObjectId(id))){
			Discussion discussion = MorphiaObject.datastore.get(Discussion.class, new ObjectId(id));
			if (discussion != null){
				setRead(discussion, true); // We set the discussion to read
				discussion.save();
				return discussion;
			}
		} 
		return null;
	}

	public Discussion findDiscussionById(ObjectId oid) {
		if (discussionIds != null && discussionIds.contains(oid)){
			Discussion discussion = MorphiaObject.datastore.get(Discussion.class, oid);
			if (discussion != null){
				setRead(discussion, true); // We set the discussion to read
				discussion.save();
				return discussion;
			}
		}
		return null;
	}

	/** Adds a new discussionId to the list and also to the unread list
	 * @param id
	 */
	public void addDiscussion(Discussion discussion){
		if (discussionIds != null && !discussionIds.contains(discussion.id)){
			discussionIds.add(discussion.id);
			this.save();
		}
	}
	
	public void removeDiscussion(Discussion discussion){
		if (discussionIds != null){
			discussionIds.remove(discussion.id);
			this.save();
		}
	}

	/** Sets the discussionId to read or unread
	 * @param id
	 * @param read
	 */
	public void setRead(Discussion discussion, boolean read){
		if (unread != null && read){
			unread.remove(discussion.id);
		} else if (unread != null && !unread.contains(discussion.id)) {
			unread.add(discussion.id);
		}
		this.save();
	}

}
