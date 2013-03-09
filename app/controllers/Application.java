package controllers;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import models.User;
import play.Routes;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.*;
import play.mvc.Http.Session;
import play.mvc.Result;
import providers.MyUsernamePasswordAuthProvider;
import providers.MyUsernamePasswordAuthProvider.MyLogin;
import providers.MyUsernamePasswordAuthProvider.MySignup;

import views.html.*;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.user.AuthUser;

public class Application extends Controller {

	public static final String FLASH_MESSAGE_KEY = "message";
	public static final String FLASH_ERROR_KEY = "error";
	public static final String USER_ROLE = "user";
	
	public static Result index() {
        //return ok(index.render());
        return redirect("/assets/index.html");
	}

    public static Result tokenSuccess() {
        return redirect("/assets/tokenSuccess.html");
    }

    public static Result tokenFail() {
        return redirect("/assets/tokenFail.html");
    }

	public static User getLocalUser(final Session session) {
		final AuthUser currentAuthUser = PlayAuthenticate.getUser(session);
		final User localUser = User.findByAuthUserIdentity(currentAuthUser);
		return localUser;
	}

	@Restrict(@Group(Application.USER_ROLE))
	public static Result profile() {
		final User localUser = getLocalUser(session());
		return ok(profile.render(localUser));
	}

	public static Result login() {
		return ok(login.render(MyUsernamePasswordAuthProvider.LOGIN_FORM));
	}

	public static Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MyLogin> filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {
			// User did not fill everything properly
            Collection<List<ValidationError>> errors = filledForm.errors().values();
            StringBuffer sb = new StringBuffer();
            for (List<ValidationError> vel : errors)
                for (ValidationError ve : vel)
                    sb.append("<li>").append(ve.key()).append(": ").append(ve.message()).append("</li>");
			return badRequest("<ul>" + sb.toString() + "</ul>");// "Fill the fields properly!");//login.render(filledForm));
		} else {
			// Everything was filled
			return UsernamePasswordAuthProvider.handleLogin(ctx());
		}
	}

	public static Result signup() {
		return ok(signup.render(MyUsernamePasswordAuthProvider.SIGNUP_FORM));
	}

	public static Result jsRoutes() {
		return ok(
            Routes.javascriptRouter(
                "jsRoutes",
                controllers.routes.javascript.Signup.forgotPassword()
            )
        )
        .as("text/javascript");
	}

	public static Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache(response());
		final Form<MySignup> filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM
				.bindFromRequest();
		if (filledForm.hasErrors()) {

            // User did not fill everything properly
            Collection<List<ValidationError>> errors = filledForm.errors().values();
            StringBuffer sb = new StringBuffer();
            for (List<ValidationError> vel : errors)
                for (ValidationError ve : vel)
                    sb.append("<li>").append(ve.key()).append(": ").append(ve.message()).append("</li>");
            return badRequest("<ul>" + sb.toString() + "</ul>");// "Fill the fields properly!");//signup.render(filledForm));

		} else {
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return UsernamePasswordAuthProvider.handleSignup(ctx());
		}
	}

	public static String formatTimestamp(final long t) {
		return new SimpleDateFormat("yyyy-dd-MM HH:mm:ss").format(new Date(t));
	}


    @Restrict(@Group(Application.USER_ROLE))
    public static Result editmap() {
        final User localUser = getLocalUser(session());
        return ok(editmap.render(localUser));
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result messages() {
        final User localUser = getLocalUser(session());
        return ok(messages.render(localUser));
    }

}
