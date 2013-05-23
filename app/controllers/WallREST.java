package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;

import models.Discussion;
import models.Message;
import models.Publication;
import models.User;
import models.User2Discussion;
import models.Wall;
import play.libs.Json;
import play.mvc.Result;

public class WallREST extends ItemREST {

	public static Result all() {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Wall wall = Wall.findById(user.id, Wall.class);
		if (wall == null) {
			return badRequest(Constants.WALL_EMPTY.toString());
		}
		List<Publication> publications = wall.getPublications();
		if (publications.size() == 0) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Publication.publicationsToObjectNodes(publications)));
		}
	}

	public static Result getPublications() {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Wall wall = Wall.findById(user.id, Wall.class);
		if (wall == null) {
			return badRequest(Constants.WALL_EMPTY.toString());
		}
		List<Publication> publications = wall.getPublications(0, 10);
		if (publications.size() == 0) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Publication.publicationsToObjectNodes(publications)));
		}
	}


	public static Result getLastPublications(int amount) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Wall wall = Wall.findById(user.id, Wall.class);
		if (wall == null) {
			return badRequest(Constants.WALL_EMPTY.toString());
		}
		List<Publication> publications = wall.getPublications(0, amount);
		if (publications.size() == 0) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Publication.publicationsToObjectNodes(publications)));
		}
	}


	public static Result getRangePublications(int from, int to) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Wall wall = Wall.findById(user.id, Wall.class);
		if (wall == null) {
			return badRequest(Constants.WALL_EMPTY.toString());
		}
		List<Publication> publications = wall.getPublications(from, to);
		if (publications.size() == 0) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Publication.publicationsToObjectNodes(publications)));
		}
	}


	public static Result getPublication(String publicationId) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Wall wall = Wall.findById(user.id, Wall.class);
		if (wall == null) {
			return badRequest(Constants.WALL_EMPTY.toString());
		}
		Publication publication = wall.findPublicationById(publicationId);
		if (publication == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		// Return the response
		return ok(Json.toJson(Publication.publicationToFullObjectNode(publication)));
	}
	
	
	public static Result addPublication() {
		final User user = Application.getLocalUser(session());

		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}

		Publication publication = null;
		Message message = null;
		Wall wall = null;

		publication = new Publication();		// Create Publication
		publication.messageIds = new ArrayList<ObjectId>();
		publication.subject = json.findPath("subject").getTextValue();
		publication.save();
		
		try {

			message = new Message();		// Create first message
			message.message = json.findPath("message").getTextValue();
			message.writerId = user.id;
			message.save();

			publication.addMessage(message); // Add message to publication

			// Add publication to users wall
			wall = Wall.findById(user.id, Wall.class);

			// If is users first discussion, create new User2Discussion
			if (wall == null){
				wall = new Wall();
				/****************************************/
				wall.id = new ObjectId(user.id.toString()); // IMPORTANT
				/****************************************/
				wall.save();
			}
			
			wall.addPublication(publication);

		} catch (Exception e) {

			publication.delete();

			return badRequest(Constants.JSON_EMPTY.toString());
		}
		message.save(); // Save Message
		publication.save(); // Save publication
		wall.save();

		//Return a copy of the publication
		return ok(Json.toJson(Publication.publicationToFullObjectNode(publication)));
	}
	
	
	public static Result deletePublication(String publicationId){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		Wall wall = Wall.findById(user.id, Wall.class);
		if (wall == null) {
			return badRequest(Constants.WALL_EMPTY.toString());
		}
		Publication publication = wall.findPublicationById(publicationId);
		if (publication == null){
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}

		wall.removePublication(publication);
		publication.delete();

		return ok(Json.toJson(Publication.publicationToFullObjectNode(publication)));
	}

}
