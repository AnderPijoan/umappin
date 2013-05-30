package controllers;

import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;

import models.Message;
import models.Publication;
import models.User;
import models.Timeline;
import play.libs.Json;
import play.mvc.Result;

public class TimelineREST extends ItemREST {

	public static Result all() {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.TIMELINE_EMPTY.toString());
		}
		List<Publication> publications = timeline.all();
		if (publications.size() == 0) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		} else {
			// Return the response
			return ok(Json.toJson(Publication.publicationsToObjectNodes(publications)));
		}
	}
	

	public static Result getTimeline(String userId){
		final User user = User.findById(userId, User.class);
		if (user == null){
			return badRequest(Constants.USERS_EMPTY.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		System.out.println("aqui llega..");
		if (timeline == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		// Return the response
		return ok(Json.toJson(Timeline.timelineToShortObjectNode(timeline)));
	}


	public static Result getPublications() {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		List<Publication> publications = timeline.getPublications(0, 10);
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
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		List<Publication> publications = timeline.getPublications(0, amount);
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
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.TIMELINE_EMPTY.toString());
		}
		List<Publication> publications = timeline.getPublications(from, to);
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
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		Publication publication = timeline.findPublicationById(publicationId);
		if (publication == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		// Return the response
		return ok(Json.toJson(Publication.publicationToFullObjectNode(publication)));
	}


	public static Result getPublicationLikes(String publicationId) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		Publication publication = timeline.findPublicationById(publicationId);
		if (publication == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}

		// Return the response
		return ok(Json.toJson(User.usersToObjectNodes(publication.getLikes())));
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
		Timeline timeline = null;
		
		publication = new Publication();		// Create Publication
		publication.subject = json.findPath("subject").getTextValue();

		/*
		if (json.findPath("postPicture") != null){
			publication.postPicture = new ObjectId(json.findPath("postPicture").getTextValue());
		}*/

		publication.save();
		
		try {

			message = new Message();		// Create first message
			message.message = json.findPath("message").getTextValue();
			message.writerId = user.id;
			message.save();


			publication.firstMessage = message.id; // This is the publications first message
			publication.save();

			// Add publication to users timeline
			timeline = Timeline.findById(user.id, Timeline.class);

			// If is users first discussion, create new User2Discussion
			if (timeline == null){
				timeline = new Timeline();
				/****************************************/
				timeline.id = new ObjectId(user.id.toString()); // IMPORTANT
				/****************************************/
				timeline.save();
			}

			timeline.addPublication(publication);

		} catch (Exception e) {

			message.delete();
			publication.delete();

			return badRequest(Constants.JSON_EMPTY.toString());
		}
		message.save(); // Save Message
		publication.save(); // Save publication
		timeline.save(); // Save timeline

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
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.TIMELINE_EMPTY.toString());
		}
		Publication publication = timeline.findPublicationById(publicationId);
		if (publication == null){
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}

		timeline.removePublication(publication);
		publication.delete();

		return ok(Json.toJson(Publication.publicationToFullObjectNode(publication)));
	}


	public static Result getMessage(String messageId) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.TIMELINE_EMPTY.toString());
		}
		String publicationId = json.findPath("publication_id").getTextValue(); 
		Publication publication = timeline.findPublicationById(publicationId);
		if (publication == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		Message message = publication.findMessageById(messageId);
		if (message == null){
			return badRequest(Constants.MESSAGES_EMPTY.toString());
		}
		// Return a copy of the message
		return ok(Json.toJson(Message.messageToObjectNode(message)));
	}


	public static Result updateMessage(String messageId) {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.TIMELINE_EMPTY.toString());
		}
		String publicationId = json.findPath("publication_id").getTextValue(); 
		Publication publication = timeline.findPublicationById(publicationId);
		if (publication == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		Message message = publication.findMessageById(messageId);
		if (message == null){
			return badRequest(Constants.MESSAGES_EMPTY.toString());
		}

		message.message = json.findPath("message").getTextValue();
		message.save();

		// Return a copy of the message
		return ok(Json.toJson(Message.messageToObjectNode(message)));
	}


	public static Result reply() {
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.TIMELINE_EMPTY.toString());
		}
		String publicationId = json.findPath("publication_id").getTextValue(); 
		Publication publication = timeline.findPublicationById(publicationId);
		if (publication == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}

		Message message = new Message();
		message.message = json.findPath("message").getTextValue();
		message.writerId = user.id;
		message.save();

		publication.addMessage(message);
		publication.save(); // Save publication

		// Return a copy of the discussion
		return ok(Json.toJson(Message.messageToObjectNode(message)));
	}


	public static Result replyToMessage(String id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.TIMELINE_EMPTY.toString());
		}
		String publicationId = json.findPath("publication_id").getTextValue(); 
		Publication publication = timeline.findPublicationById(publicationId);
		if (publication == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}

		// If the message replying to its really from this publication
		Message msg = publication.findMessageById(id);
		if(msg != null){
			Message message = new Message();
			message.message = json.findPath("message").getTextValue();
			message.replyToMsg = msg.id;
			message.writerId = user.id;
			message.save(); // Save Message

			publication.addMessage(message); // Add message to its publication
			publication.save(); // Save Publication

			// Return a copy of the message
			return ok(Json.toJson(Message.messageToObjectNode(message)));
		}
		return badRequest(Constants.MESSAGES_EMPTY.toString());
	}


	public static Result deleteMessage(String id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		Timeline timeline = Timeline.findById(user.id, Timeline.class);
		if (timeline == null) {
			return badRequest(Constants.TIMELINE_EMPTY.toString());
		}
		String publicationId = json.findPath("publication_id").getTextValue(); 
		Publication publication = timeline.findPublicationById(publicationId);
		if (publication == null) {
			return badRequest(Constants.PUBLICATIONS_EMPTY.toString());
		}
		Message message = publication.findMessageById(id);
		if (message == null){
			return badRequest(Constants.MESSAGES_EMPTY.toString());
		}

		// If someone is trying to delete the first message from a publication
		// That cant be done
		if (message.id.equals(publication.firstMessage)){
			return forbidden(Constants.UNAUTHORIZED.toString());
		}

		publication.deleteMessage(message);
		publication.save();
		message.delete();

		return ok(Json.toJson(Message.messageToObjectNode(message)));
	}

}
