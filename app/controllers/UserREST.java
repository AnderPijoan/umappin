package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.User;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import java.util.ArrayList;
import java.util.List;

public class UserREST extends ItemREST {

    public static Result getAll() {
        List<User> users =  User.all(User.class);
        if (users.size() == 0) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            List<JsonNode> nodes = new ArrayList();
            for (User usr : users)
                if (usr.emailValidated)
                nodes.add(usr.toJson());
            return ok(Json.toJson(nodes));
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
    public static Result delete(String id) {
        User item = User.findById(id, User.class);
        if (item == null) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            item.delete();
            return ok(item.toJson());
        }
    }

    public static Result getSessionUser() {
        final User localUser = Application.getLocalUser(session());
        if (localUser != null)
            return ok(localUser.toJson());
        else
            return notFound(Constants.USER_NOT_LOGGED_IN.toString());
    }

}
