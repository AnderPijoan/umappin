package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserREST extends ItemREST {

    public static Result getAll() {
        List<User> users =  User.all(User.class);
        if (users.size() == 0) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            List<JsonNode> nodes = new ArrayList<JsonNode>();
            for (User usr : users)
                if (usr.emailValidated)
                nodes.add(usr.toJson());
            return ok(Json.toJson(nodes));
        }
    }

    // Get all the user follows
    public static Result getAllRelated(){
        final User user = Application.getLocalUser(session());
        if (user == null)
            return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        List<Follows> userFollows = Follows.findRelatedByUserId(user.id.toString());
        List<Followed> userFollowed = Followed.findRelatedByUserId(user.id.toString());
        if (userFollows == null || userFollowed == null)
            return badRequest();
        Set<String> ids = new HashSet<>();
        for (Follows ufs : userFollows)
            ids.add(ufs.getUserId());
        for (Followed ufd : userFollowed)
            ids.add(ufd.getUserId());
        List<JsonNode> nodes = new ArrayList<JsonNode>();
        for (String id : ids) {
            User usr = User.findById(id, User.class);
            if (usr.emailValidated)
                nodes.add(usr.toJson());
        }
        return ok(Json.toJson(nodes));
    }

    public static Result getAllShortInfo() {
        List<User> users =  User.all(User.class);
        if (users.size() == 0) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            return ok(Json.toJson(User.usersToShortObjectNode(users)));
        }
    }

    public static Result get(String id) {
        User usr = User.findById(id, User.class);
        if (usr.emailValidated)
            return ok(usr.toJson());
        else
            return notFound(Constants.JSON_EMPTY.toString());
    }

    public static Result post() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else {
            MySignup signup= new MySignup();
            signup.name = json.findPath("name").getTextValue();
            signup.password = json.findPath("password").getTextValue();
            signup.email = json.findPath("email").getTextValue();
            User.create(new MyUsernamePasswordAuthUser(signup));
            return ok("New user " + signup.name + " created");
        }
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result put(String id) {
        JsonNode json = request().body().asJson();
        if(json == null || id == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else if (User.findById(id, User.class) == null) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            ((ObjectNode)json).put("id", id);
            User usr = User.userFromJson(json);
            usr.save();
            return ok(usr.toJson());
        }
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result putProfile(String id) {
        JsonNode json = request().body().asJson();
        if(json == null || id == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else {
            User currUsr = User.findById(id, User.class);
            if (currUsr == null) {
                return notFound(Constants.JSON_EMPTY.toString());
            } else {
                ((ObjectNode)json).put("id", id);
                User usr = User.userFromJson(json);
                currUsr.name = usr.name;
                currUsr.firstName = usr.firstName;
                currUsr.lastName = usr.lastName;
                currUsr.address = usr.address;
                currUsr.phone = usr.phone;
                currUsr.save();
                return ok(currUsr.toJson());
            }
        }
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result delete(String id) {
    	final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        }
		// Remove from all discussions
		User2Discussion user2disc = User2Discussion.findById(user.id, User2Discussion.class);
		if (user2disc != null){

			for(Discussion discussion : user2disc.all()){
				if (discussion != null){
					discussion.removeUser(user);
				}
			}
			user2disc.delete();
		}
		// Remove all its messages
		for(Message message : Message.findWhere("writerId", user.id, Message.class)){
			message.delete();
		}
		// Remove all publications and timeline
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline != null){

			for(Publication publication : timeline.all()){
				if (publication != null){
					publication.delete();
				}
			}
			timeline.delete();
		}
		
		// TODO REMOVE PHOTOS, AND MORE
		
		user.delete();
		return ok(user.toJson());
    }

    public static Result getSessionUser() {
        final User localUser = Application.getLocalUser(ctx().session());
        if (localUser != null)
            return ok(localUser.toJson());
        else
            return notFound(Constants.USER_NOT_LOGGED_IN.toString());
    }

    //added by Oscar, used in android
    
    public static Result update(){
        final User user = Application.getLocalUser(session());
        if (user == null){
            return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        }
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        }
        
        user.firstName = json.findPath("firstName").getTextValue();
        user.lastName = json.findPath("lastName").getTextValue();
        user.email = json.findPath("email").getTextValue();

        user.save();


        return ok(Json.toJson(User.userToShortObjectNode(user)));
    }
}
