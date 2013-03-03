package controllers;

import models.User;

import org.codehaus.jackson.JsonNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthUser;

import providers.MyUsernamePasswordAuthProvider.MySignup;

import java.util.List;

public class UserREST extends Controller {

    public static Result getUsers() {
        List<User> users =  User.all();
        if (users.size() == 0) {
            return badRequest("No users");
        } else {
            return ok(Json.toJson(users));
        }
    }

    public static Result getUser(String user) {
        User usr =  User.findByName(user);
        if (usr == null) {
            return badRequest("User does not exist");
        } else {
            return ok(Json.toJson(usr));
        }
    }

    public static Result addUser() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            MySignup signup= new MySignup();
            signup.name = json.findPath("name").getTextValue();
            signup.password = json.findPath("password").getTextValue();
            signup.email = json.findPath("email").getTextValue();
            User.create(new MyUsernamePasswordAuthUser(signup));
            return ok("New user " + signup.name + " created");
        }
    }

    public static Result getSessionUser() {
        final User localUser = Application.getLocalUser(session());
        return ok(Json.toJson(localUser));
    }

}
