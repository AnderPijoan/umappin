package controllers;

import static play.libs.Json.toJson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.Discussion;
import models.Message;
import models.User;
import models.User2Discussion;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class DiscussionREST extends Controller {

	public static Result getDiscussions() {
		List<Discussion> discussions =  Discussion.all();
		if (discussions.size() == 0) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {
			
			return ok(Json.toJson(Discussion.discussionsToObjectNodes(discussions)));
		}
	}

	public static Result getDiscussion(String id) {
		Discussion discussion =  Discussion.findById(new ObjectId(id));
		if (discussion == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {
			return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
		}
	}
	
	public static Result getDiscussion(ObjectId id) {
		Discussion discussion =  Discussion.findById(id);
		if (discussion == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {
			return ok(Json.toJson(Discussion.discussionToFullObjectNode(discussion)));
		}
	}

	public static Result getMessage(String id, String msgId) {
		Discussion discussion =  Discussion.findById(id);
		if (discussion == null || !discussion.messageIds.contains(msgId)) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {
			Message message = Message.findById(msgId);
			if (message == null){
				return badRequest(Constants.MESSAGES_EMPTY.toString());
			}
			return ok(Json.toJson(Message.messageToObjectNode(message)));
		}
	}

	public static Result addDiscussion() {
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}

		Discussion discussion = new Discussion();
		discussion.messageIds = new ArrayList<String>();
		discussion.userIds = new ArrayList<String>();
		discussion.subject = json.findPath("subject").getTextValue();
		
		Iterator<JsonNode> userIds = json.findPath("receiver_users").getElements();
		while(userIds.hasNext()){
			String userId = userIds.next().toString();
			discussion.userIds.add(userId);
		}
		
		Message message = new Message();
		message.message = json.findPath("body").getTextValue();
		message.writerId = user.id.toString();
		message.save(); // Save message
		
		discussion.addMessage(message); // Add message to discussion
		discussion.save(); // Save discussion
		
		return ok(Discussion.discussionToFullObjectNode(discussion));
	}

	public static Result reply(String id){
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Discussion discussion = Discussion.findById(id);
		if (discussion == null || !discussion.userIds.contains(user.id)){
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		Message message = new Message();
		message.message = json.findPath("body").getTextValue();
		message.writerId = user.id.toString();
		message.save();
		discussion.addMessage(message);
		discussion.save();
		return ok(toJson(Discussion.discussionToFullObjectNode(discussion)));

	}

	public static Result replyToMessage(String id, String msgId){
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Discussion discussion = Discussion.findById(id);
		if (discussion == null || !discussion.userIds.contains(user.id)){
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		Message msg = discussion.findMessageById(msgId);
		if(msg != null){
			Message message = new Message();
			message.message = json.findPath("body").getTextValue();
			message.replyToMsg = msg.id.toString();
			message.writerId = user.id.toString();
			message.save(); // Save message
			
			discussion.addMessage(message); // Add message to discussion
			discussion.save(); // Save discussion
			
			return ok(toJson(Discussion.discussionToFullObjectNode(discussion)));
		} else {
			return badRequest(Constants.MESSAGES_EMPTY.toString());
		}
	}
}
