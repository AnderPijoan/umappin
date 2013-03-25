package controllers;

import models.Message;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import static play.libs.Json.toJson;

/**
 * User: a.digangi
 * Date: 23/02/13
 * Time: 23.23
 */
public class MessageREST extends Controller {

	public static Result getMessages(){
		List<Message> messages =  Message.all();
		if (messages.size() == 0) {
			return badRequest(Constants.MESSAGES_EMPTY.toString());
		} else {
			return ok(Json.toJson(messages));
		}
	}

	public static Result findById(String id){
		Message message = Message.findById(id);
		if (message == null){
			return badRequest(Constants.MESSAGES_EMPTY.toString());
		} else {
		return ok(toJson(message));
		}
	}
}
