package models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;

/**
 * User: a.pijoan
 * Date: 16/03/13
 * Time: 12:34
 */
@Entity
public class User2Discussion extends Item {

	/** ------------------------ Attributes ------------------------- **/

	///////////////////////////////////////////////////////////////////////////////
	// THE OBJECTID OF USER2DISCUSSION IS THE SAME AS THE USERS, TO GET IT DIRECTLY
	///////////////////////////////////////////////////////////////////////////////
	
	public List<ObjectId> discussionIds;

	public List<ObjectId> unread; // Only unread discussions ids are stored

	/** ------------------------- Methods -------------------------- **/

	public List<Discussion> all() {
		if (MorphiaObject.datastore != null) {
			List<Discussion> result = new ArrayList<Discussion>();
			for (ObjectId oid : discussionIds){
				Discussion discussion = MorphiaObject.datastore.get(Discussion.class, oid);
				if (discussion != null){
					result.add(discussion);
				} else {
					discussionIds.remove(oid);
				}
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
				Discussion discussion = Discussion.findById(oid, Discussion.class);
				if (discussion != null){
					result.add(discussion);
				} else {
					unread.remove(oid);
					discussionIds.remove(oid);
				}
			}
			return result;
		} else {
			return new ArrayList<Discussion>();
		}
	}

	public static User2Discussion findById(String id) {
		return MorphiaObject.datastore.get(User2Discussion.class, new ObjectId(id));
	}

	public static User2Discussion findById(ObjectId oid) {
		return MorphiaObject.datastore.get(User2Discussion.class, oid);
	}

	public Discussion findDiscussionById(String id) {
		if (discussionIds != null && discussionIds.contains(new ObjectId(id))){
			Discussion discussion = Discussion.findById(new ObjectId(id), Discussion.class);
			if (discussion != null){
				setRead(discussion, true); // We set the discussion to read
				discussion.save();
				return discussion;
			} else {
				discussionIds.remove(new ObjectId(id));
			}
		} 
		return null;
	}

	public Discussion findDiscussionById(ObjectId oid) {
		if (discussionIds != null && discussionIds.contains(oid)){
			Discussion discussion = Discussion.findById(oid, Discussion.class);
			if (discussion != null){
				setRead(discussion, true); // We set the discussion to read
				discussion.save();
				return discussion;
			} else {
				discussionIds.remove(oid);
			}
		}
		return null;
	}

	/** Adds a new discussionId to the list
	 * @param id
	 */
	public void addDiscussion(Discussion discussion){
		if (discussionIds == null){
			discussionIds = new ArrayList<ObjectId>();
		}
		
		if (!discussionIds.contains(discussion.id)){
			discussionIds.add(discussion.id);
			this.save();
		}
	}
	
	public void removeDiscussion(Discussion discussion){
		if (discussionIds != null){
			discussionIds.remove(discussion.id);
			
			if (discussionIds.isEmpty())
				discussionIds = null;
			
			this.save();
		}
	}

	/** Sets if the discussion is read entirely
	 * @param id
	 * @param read
	 */
	public void setRead(Discussion discussion, boolean read){
		if (!read && unread == null){
			unread = new ArrayList<ObjectId>();
		}
		
		if (unread != null && read){
			unread.remove(discussion.id);
		} else if (unread != null && !unread.contains(discussion.id)) {
			unread.add(discussion.id);
		}
		this.save();
	}

}
