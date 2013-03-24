package controllers;

import play.mvc.*;

import play.mvc.Result;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class Templates extends Controller {

    /** PUBLIC **/
    public static Result home() { return ok("/assets/templates/home.html"); }
    public static Result login() { return ok("/assets/templates/login.html"); }
    public static Result logout() { return ok("/assets/templates/logout.html"); }
    public static Result signup() { return ok("/assets/templates/signup.html"); }
    public static Result forgotPassword() { return ok("/assets/templates/forgotPassword.html"); }
    public static Result changePassword() { return ok("/assets/templates/changePassword.html"); }

    /** RESTRICTED **/
	@Restrict(@Group(Application.USER_ROLE))
	public static Result profile() { return ok("/assets/templates/profile.html"); }
    @Restrict(@Group(Application.USER_ROLE))
    public static Result linkProvider() { return ok("/assets/templates/linkProvider.html"); }
    @Restrict(@Group(Application.USER_ROLE))
    public static Result editmap() { return ok("/assets/templates/editmap.html"); }
    @Restrict(@Group(Application.USER_ROLE))
    public static Result messages() { return ok("/assets/templates/messages.html"); }
    @Restrict(@Group(Application.USER_ROLE))
    public static Result account() { return ok("/assets/templates/account.html"); }

}
