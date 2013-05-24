package models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import controllers.MorphiaObject;
import controllers.routes;

public class Publication extends Post {

	// Neccessary for /news
	public ObjectId writerId;

	public ObjectId postPicture;

	public List<ObjectId> userLikesIds;

	@Override
	public void delete() {

		// TODO remove picture?

		if (repliesIds != null)
			for(ObjectId oid : repliesIds){
				Message message = Message.findById(oid, Message.class);
				if (message != null){
					message.delete();
				}
			}

		Message message = Message.findById(firstMessage, Message.class);
		if (message != null){
			message.delete();
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

		if (userLikesIds != null){
			Iterator<ObjectId> userIte = userLikesIds.iterator();
			while(userIte.hasNext()){
				User user = User.findById(userIte.next(), User.class);
				if (user != null){
					users.add(user);
				} else {
					userIte.remove();
				}
			}
		}
		return users;
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
		publicationNode.put("subject", publication.subject);
		publicationNode.put("postPicture", publication.postPicture != null ? routes.PhotosREST.getPhoto(publication.postPicture.toString()).toString() +"/content" : null);
		publicationNode.put("likes", Json.toJson(User.userIdsToShortObjectNode(publication.userLikesIds)));
		publicationNode.put("replies", publication.repliesIds.size());
		publicationNode.put("timeStamp", publication.id.getTime());
		publicationNode.put("lastWrote", publication.lastWrote.toString());

		Message message = Message.findById(publication.firstMessage, Message.class);
		if (message != null){
			publicationNode.put("content", message.message);
		} else {
			publication.delete();
		}

		return publicationNode;
	}


	/** Parses a publication and prepares it for exporting to JSON
	 * @param publication
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode publicationToFullObjectNode (Publication publication){
		ObjectNode publicationNode = Json.newObject();
		publicationNode.put("id", publication.id.toString());
		publicationNode.put("subject", publication.subject);
		publicationNode.put("postPicture", publication.postPicture != null ? routes.PhotosREST.getPhoto(publication.postPicture.toString()).toString() +"/content" : null);
		publicationNode.put("likes", Json.toJson(User.userIdsToShortObjectNode(publication.userLikesIds)));
		publicationNode.put("timeStamp", publication.id.getTime());
		publicationNode.put("lastWrote", publication.lastWrote.toString());
		publicationNode.put("messages", Json.toJson(Message.messagesToObjectNodes(publication.getMessages())));

		Message message = Message.findById(publication.firstMessage, Message.class);
		if (message != null){
			publicationNode.put("content", message.message);
		} else {
			publication.delete();
		}

		return publicationNode;
	}

}
