package controllers;

import static play.libs.Json.toJson;

import java.util.ArrayList;
import java.util.List;

import models.Discussion;
import models.Message;
import models.User;

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
			
			ObjectNode response = Json.newObject();
			response.put("discussions", Json.toJson(discussions));
			
			return ok(response);
		}
	}

	public static Result getDiscussion(String id) {
		Discussion discussion =  Discussion.findById(new ObjectId(id));
		if (discussion == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {

			ObjectNode discussionNode = Json.newObject();
			discussionNode.put("id", discussion.id.toString());
			discussionNode.put("subject", discussion.subject);
			discussionNode.put("messages", Json.toJson(discussion.getMessages()));
			
			ObjectNode response = Json.newObject();
			response.put("discussion", Json.toJson(discussionNode));
			
			return ok(response);
		}
	}
	
	public static Result getDiscussion(ObjectId id) {
		Discussion discussion =  Discussion.findById(id);
		if (discussion == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {

			ObjectNode discussionNode = Json.newObject();
			discussionNode.put("id", discussion.id.toString());
			discussionNode.put("subject", discussion.subject);
			discussionNode.put("messages", Json.toJson(discussion.getMessages()));
			
			ObjectNode response = Json.newObject();
			response.put("discussion", Json.toJson(discussionNode));
			
			return ok(response);
		}
	}

	public static Result getMessage(String id, String msgId) {
		Discussion discussion =  Discussion.findById(id);
		if (discussion == null) {
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
		discussion.subject = json.findPath("subject").getTextValue();
		
		Message message = new Message();
		message.message = json.findPath("body").getTextValue();
		message.writerId = user.id.toString();
		message.save(); // Save message
		
		discussion.addMessage(message); // Add message to discussion
		discussion.save(); // Save discussion
		
		return ok(Discussion.discussionToObjectNode(discussion));
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
		if (discussion == null){
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		}
		Message message = new Message();
		message.message = json.findPath("body").getTextValue();
		message.writerId = user.id.toString();
		message.save();
		discussion.addMessage(message);
		discussion.save();
		return ok(toJson(Discussion.discussionToObjectNode(discussion)));

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
		if (discussion == null){
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
			
			return ok(toJson(Discussion.discussionToObjectNode(discussion)));
		} else {
			return badRequest(Constants.MESSAGES_EMPTY.toString());
		}
	}
}
