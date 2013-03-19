package controllers;

import static play.libs.Json.toJson;

import java.util.ArrayList;
import java.util.List;

import models.Discussion;
import models.Message;

import org.codehaus.jackson.JsonNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import scala.reflect.internal.Types.ConstantType;

public class DiscussionREST extends Controller {

	public static Result getDiscussions() {
		List<Discussion> discussions =  Discussion.all();
		if (discussions.size() == 0) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {
			return ok(Json.toJson(discussions));
		}
	}

	public static Result getDiscussion(String id) {
		Discussion discussion =  Discussion.findById(id);
		if (discussion == null) {
			return badRequest(Constants.DISCUSSIONS_EMPTY.toString());
		} else {
			return ok(Json.toJson(discussion));
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
			return ok(Json.toJson(message));
		}
	}

	public static Result addDiscussion() {
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		} else {
			Discussion discussion = new Discussion();
			discussion.messages = new ArrayList<Message>();
			discussion.subject = json.findPath("subject").getTextValue();
			Message message = new Message();
			message.body = json.findPath("body").getTextValue();
			message.save();
			discussion.addMessage(message);
			discussion.save();
			return ok("New discussion " + discussion.id + " created");
		}
	}

	public static Result reply(String id){
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		} else {
			Discussion discussion = Discussion.findById(id);
			Message message = new Message();
			message.body = json.findPath("body").getTextValue();
			discussion.addMessage(message);
			return ok(toJson(discussion));
		}
	}

	public static Result replyToMessage(String id, String msgId){
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		} else {
			Discussion discussion = Discussion.findById(id);
			Message msg = Message.findById(msgId);
			if(discussion.messages.contains(msg)){
				Message message = new Message();
				message.body = json.findPath("body").getTextValue();
				message.replyToMsg = msg.id;
				discussion.addMessage(message);
				return ok(toJson(discussion));
			} else {
				return badRequest(Constants.MESSAGES_EMPTY.toString());
			}
		}
	}
}
