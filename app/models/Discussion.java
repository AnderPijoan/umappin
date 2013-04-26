package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;


import play.data.format.Formats;
import play.libs.Json;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;

/**
 * User: a.pijoan
 * Date: 16/03/13
 * Time: 12:34
 */
@Entity
public class Discussion {

	@Id
	public ObjectId id;

	public String subject;

	public List<ObjectId> messageIds;
	
	public List<ObjectId> userIds;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date lastWrote = new Date();
	
	public static List<Discussion> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Discussion.class).asList();
		} else {
			return new ArrayList<Discussion>();
		}
	}
	
	public ObjectId save() {
		lastWrote = new Date();
		MorphiaObject.datastore.save(this);
		return id;
	}
	
	public static Discussion findById(String id) {
		Discussion discussion = MorphiaObject.datastore.get(Discussion.class, new ObjectId(id));
		if (discussion == null){
			return null;
		} else {
			return discussion;
		}
	}
	
	public static Discussion findById(ObjectId oid) {
		Discussion discussion = MorphiaObject.datastore.get(Discussion.class, oid);
		if (discussion == null){
			return null;
		} else {
			return discussion;
		}
	}
	
	public Message findMessageById(String id) {
		if (messageIds.contains(new ObjectId(id))){
			Message message = MorphiaObject.datastore.get(Message.class, new ObjectId(id));
			return message;
		} else {
			return null;
		}
	}
	
	public Message findMessageById(ObjectId oid) {
		if (messageIds.contains(oid)){
			Message message = MorphiaObject.datastore.get(Message.class, oid);
			return message;
		} else {
			return null;
		}
	}
	
	public List<Message> getMessages() {
		List<Message> messages = new ArrayList<Message>();
		for(ObjectId oid : messageIds){
			Message message = MorphiaObject.datastore.get(Message.class, oid);
			messages.add(message);
		}
		return messages;
	}
	
	public void addMessage(Message message) {
		if (!messageIds.contains(message.id))
			messageIds.add(message.id);
		lastWrote = new Date();
		this.save();
	}
	
	public void addUser(User user) {
		if (userIds != null && !userIds.contains(user.id))
			userIds.add(user.id);
		lastWrote = new Date();
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
		discussionNode.put("unread", "");
		discussionNode.put("subject", discussion.subject);
		discussionNode.put("users", Json.toJson(usersSmallInfo(discussion.userIds)));
		discussionNode.put("timeStamp", discussion.id.getTime());
		discussionNode.put("lastWrote", discussion.lastWrote.toString());
		return discussionNode;
	}
	
	/** Parses a discussion and prepares it for exporting to JSON
	 * @param discussion A discussion
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode discussionToFullObjectNode (Discussion discussion){
		ObjectNode discussionNode = Json.newObject();
		discussionNode.put("id", discussion.id.toString());
		discussionNode.put("unread", "");
		discussionNode.put("subject", discussion.subject);
		discussionNode.put("users", Json.toJson(usersSmallInfo(discussion.userIds)));
		discussionNode.put("timeStamp", discussion.id.getTime());
		discussionNode.put("lastWrote", discussion.lastWrote.toString());
		discussionNode.put("messages", Json.toJson(Message.messagesToObjectNodes(discussion.getMessages())));
		return discussionNode;
	}
	
	private static List<ObjectNode> usersSmallInfo(List<ObjectId> userIds){
		
		List<ObjectNode> users = new ArrayList<ObjectNode>();

		for(ObjectId oid : userIds){
			User user = MorphiaObject.datastore.get(User.class, oid);
			if (user != null){
				ObjectNode userNode = Json.newObject();
				userNode.put("id", user.id.toString());
				userNode.put("name", user.name);
				userNode.put("photo", "http://paginaspersonales.deusto.es/dipina/images/photo-txikia2.jpg");
				users.add(userNode);
			}
		}
		return users;
	}
}
