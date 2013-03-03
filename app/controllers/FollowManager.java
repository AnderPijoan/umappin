package controllers;


import models.Follows;
import models.User;


import org.codehaus.jackson.JsonNode;
import play.libs.Json;
import play.mvc.Http.Request;
import play.mvc.*;

import play.mvc.Result;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class FollowManager extends Controller {

	@Restrict(@Group(Application.USER_ROLE))
	public static Result follow() {
		final User follower = Application.getLocalUser(session());
        String followedName =  request().body().asFormUrlEncoded().get("name")[0];
        Follows res = Follows.follow(follower, User.findByName(followedName));
        return res != null ? ok("") : notFound("Could not follow");
	}

    @Restrict(@Group(Application.USER_ROLE))
    public static Result unfollow() {
        final User follower = Application.getLocalUser(session());
        String followedName =  request().body().asFormUrlEncoded().get("name")[0];
        Follows res = Follows.unfollow(follower, User.findByName(followedName));
        return res != null ? ok("") : notFound("Could not unfollow");
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result getFollows() {
        final User follower = Application.getLocalUser(session());
        Follows res = Follows.findByUserId(follower.id);
        if (res == null)
            res = Follows.create(follower);
        return ok(Json.toJson(res));

    }

}
