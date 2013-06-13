package controllers;

import java.util.List;

import models.Follows;
import models.Publication;
import models.User;
import play.libs.Json;
import play.mvc.Result;

public class newsREST extends ItemREST {

	public static Result all() {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Follows userFollows = Follows.findByUserId(user.id);
		if (userFollows == null){
			return notFound(Constants.USERS_EMPTY.toString());
		}
		
		// Super query
		List<Publication> publications = 
				MorphiaObject.datastore.find(Publication.class).field("writerId").hasAnyOf(userFollows.getFollowOids()).order("-_id").asList();

		if (publications != null && publications.size() == 0) {
			return notFound(Constants.PUBLICATIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Publication.publicationsWithUserToObjectNodes(publications)));
		}
	}


	public static Result getNews() {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Follows userFollows = Follows.findByUserId(user.id);
		if (userFollows == null){
			return notFound(Constants.USERS_EMPTY.toString());
		}
		
		// Super query
		List<Publication> publications = 
				MorphiaObject.datastore.find(Publication.class).field("writerId").hasAnyOf(userFollows.getFollowOids()).order("-_id").limit(20).asList();

		if (publications != null && publications.size() == 0) {
			return notFound(Constants.PUBLICATIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Publication.publicationsWithUserToObjectNodes(publications)));
		}
	}


	public static Result getNewsRange(int from, int to) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Follows userFollows = Follows.findByUserId(user.id);
		if (userFollows == null){
			return notFound(Constants.USERS_EMPTY.toString());
		}

		// Super query
		List<Publication> publications = 
				MorphiaObject.datastore.find(Publication.class).field("writerId").hasAnyOf(userFollows.getFollowOids()).order("-_id").offset(from).limit(to-from).asList();

		if (publications != null && publications.size() == 0) {
			return notFound(Constants.PUBLICATIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Publication.publicationsWithUserToObjectNodes(publications)));
		}
	}

}
