package controllers;

import models.Message;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import static play.libs.Json.toJson;
import static play.mvc.Results.ok;

/**
 * User: a.digangi
 * Date: 23/02/13
 * Time: 23.23
 */
public class RestMessages extends Controller {

    public static Result all(){

        List<Message> messages = Message.all();
        return ok(toJson(messages));
    }

    public static Result findById(String id){

        Message message = Message.findById(id);
        return ok(toJson(message));

    }

}
