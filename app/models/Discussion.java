package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;

/**
 * User: a.pijoan
 * Date: 16/03/13
 * Time: 12:34
 */
@Entity
public class Discussion extends Post {

	public List<ObjectId> userIds;

	@Override
	public void delete() {
		for (ObjectId oid : userIds){
			User2Discussion user2disc = User2Discussion.findById(oid, User2Discussion.class);
			user2disc.removeDiscussion(this);
		}

		if (repliesIds != null){
			for(ObjectId oid : repliesIds){
				Message message = Message.findById(oid, Message.class);
				message.delete();
			}
		}

		Message message = Message.findById(firstMessage, Message.class);
		if (message != null){
			message.delete();
		}

		MorphiaObject.datastore.delete(this);
	}


	public void addUser(User user) {
		if (userIds == null){
			userIds = new ArrayList<ObjectId>();
		}
		if (!userIds.contains(user.id)){
			userIds.add(user.id);
		}
		lastWrote = new Date();
		this.save();
	}

	public void removeUser(User user) {
		if (userIds != null){
			userIds.remove(user.id);
			if (userIds.isEmpty()){
				userIds = null;
				this.delete();
			}
		}
		this.save();
	}

	/** Parses a discussion list and prepares it for exporting to JSON
	 * @param dscs Discussion list
	 * @return List of ObjectNodes ready for use in toJson
	 */
	public static List<ObjectNode> discussionsToObjectNodes (List<Discussion> dscs){
		List<ObjectNode> discussions = new ArrayList<ObjectNode>();
		for(Discussion discussion : dscs){
			discussions.add(discussionToShortObjectNode(discussion));
		}
		return discussions;
	}

	/** Parses a discussion and prepares it for exporting to JSON
	 * @param discussion A discussion
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode discussionToShortObjectNode (Discussion discussion){
		ObjectNode discussionNode = Json.newObject();
		discussionNode.put("id", discussion.id.toString());
		discussionNode.put("unread", 0);
		discussionNode.put("subject", discussion.subject);
		discussionNode.put("users", Json.toJson(User.userIdsToShortObjectNode(discussion.userIds)));
		discussionNode.put("timeStamp", discussion.id.getTime());
		discussionNode.put("lastWrote", discussion.lastWrote.toString());

		Message message = Message.findById(discussion.firstMessage, Message.class);
		if (message != null){
			discussionNode.put("content", message.message);
		} else {
			discussion.delete();
		}

		return discussionNode;
	}

	/** Parses a discussion and prepares it for exporting to JSON
	 * @param discussion A discussion
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode discussionToFullObjectNode (Discussion discussion){
		ObjectNode discussionNode = Json.newObject();
		discussionNode.put("id", discussion.id.toString());
		discussionNode.put("unread", 0);
		discussionNode.put("subject", discussion.subject);
		discussionNode.put("users", Json.toJson(User.userIdsToShortObjectNode(discussion.userIds)));
		discussionNode.put("timeStamp", discussion.id.getTime());
		discussionNode.put("lastWrote", discussion.lastWrote.toString());
		discussionNode.put("messages", Json.toJson(Message.messagesToObjectNodes(discussion.getMessages())));

		Message message = Message.findById(discussion.firstMessage, Message.class);
		if (message != null){
			discussionNode.put("content", message.message);
		} else {
			discussion.delete();
		}

		return discussionNode;
	}
}
