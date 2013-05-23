package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import controllers.MorphiaObject;
import controllers.Post;
import controllers.routes;

public class Publication extends Post {

	public ObjectId postPicture;

	public List<ObjectId> userLikesIds;

	@Override
	public void delete() {

		// TODO remove picture?

		for(ObjectId oid : messageIds){
			Message message = Message.findById(oid, Message.class);
			if (message != null){
				message.delete();
			}
		}
		MorphiaObject.datastore.delete(this);
	}


	public void addLike(User user) {
		if (userLikesIds == null){
			userLikesIds = new ArrayList<ObjectId>();
		}
		if (!userLikesIds.contains(user.id)){
			userLikesIds.add(user.id);
		}
		lastWrote = new Date();
		this.save();
	}

	public void removeLike(User user) {
		if (userLikesIds != null){
			userLikesIds.remove(user.id);
			if (userLikesIds.isEmpty()){
				userLikesIds = null;
			}
		}
		this.save();
	}

	public List<User> getLikes(){
		List<User> users = new ArrayList<User>();

		Iterator<ObjectId> userIte = userLikesIds.iterator();
		while(userIte.hasNext()){
			User user = User.findById(userIte.next(), User.class);
			if (user != null){
				users.add(user);
			} else {
				userIte.remove();
			}
		}
		return users;
	}


	/** Parses a publication list and prepares it for exporting to JSON
	 * @param dscs Publication list
	 * @return List of ObjectNodes ready for use in toJson
	 */
	public static List<ObjectNode> publicationsIdsToObjectNodes(List<ObjectId> pbctId){
		List<Publication> publications = new ArrayList<Publication>();
		for(ObjectId oid : pbctId){
			Publication publication = Publication.findById(oid, Publication.class);
			if (publication != null){
				publications.add(publication);
			}
		}
		return publicationsToObjectNodes(publications);
	}

	/** Parses a publication list and prepares it for exporting to JSON
	 * @param dscs Publication list
	 * @return List of ObjectNodes ready for use in toJson
	 */
	public static List<ObjectNode> publicationsToObjectNodes (List<Publication> pbct){
		List<ObjectNode> publications = new ArrayList<ObjectNode>();
		for(Publication publication : pbct){
			publications.add(publicationToShortObjectNode(publication));
		}
		return publications;
	}


	/** Parses a publication and prepares it for exporting to JSON
	 * @param publication
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode publicationToShortObjectNode(Publication publication) {

		ObjectNode publicationNode = Json.newObject();
		publicationNode.put("id", publication.id.toString());
		publicationNode.put("unread", "fixme");
		publicationNode.put("subject", publication.subject);
		publicationNode.put("postPicture", publication.postPicture != null ? routes.PhotosREST.getPhoto(publication.postPicture.toString()).toString() +"/content" : null);
		publicationNode.put("likes", Json.toJson(User.usersSmallInfo(publication.userLikesIds)));
		publicationNode.put("replys", publication.messageIds.size()-1);
		publicationNode.put("timeStamp", publication.id.getTime());
		publicationNode.put("lastWrote", publication.lastWrote.toString());
		return publicationNode;
	}


	/** Parses a publication and prepares it for exporting to JSON
	 * @param publication
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode publicationToFullObjectNode (Publication publication){
		ObjectNode publicationNode = Json.newObject();
		publicationNode.put("id", publication.id.toString());
		publicationNode.put("unread", "fixme");
		publicationNode.put("subject", publication.subject);
		publicationNode.put("postPicture", publication.postPicture != null ? routes.PhotosREST.getPhoto(publication.postPicture.toString()).toString() +"/content" : null);
		publicationNode.put("likes", Json.toJson(User.usersSmallInfo(publication.userLikesIds)));
		publicationNode.put("timeStamp", publication.id.getTime());
		publicationNode.put("lastWrote", publication.lastWrote.toString());
		publicationNode.put("messages", Json.toJson(Message.messagesToObjectNodes(publication.getMessages())));
		return publicationNode;
	}

}
