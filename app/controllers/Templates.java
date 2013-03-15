package controllers;

import play.mvc.*;

import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;

import views.html.*;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class Templates extends Controller {

    /** PUBLIC **/
    public static Result home() { return ok("templates/home.html"); }
    public static Result login() { return ok("templates/login.html"); }
    public static Result logout() { return ok("templates/logout.html"); }
    public static Result signup() { return ok("templates/signup.html"); }
    public static Result forgotPassword() { return ok("templates/forgotPassword.html"); }
    public static Result changePassword() { return ok("templates/changePassword.html"); }

    /** RESTRICTED **/
    @Restrict(@Group(Application.USER_ROLE))
    public static Result userlist() { return ok("templates/userlist.html"); }
	@Restrict(@Group(Application.USER_ROLE))
	public static Result profile() { return ok("templates/profile.html"); }
    @Restrict(@Group(Application.USER_ROLE))
    public static Result editmap() { return ok("templates/editmap.html"); }
    @Restrict(@Group(Application.USER_ROLE))
    public static Result messages() { return ok("templates/messages.html"); }
    @Restrict(@Group(Application.USER_ROLE))
    public static Result _users() { return ok("templates/_users.html"); }

}
