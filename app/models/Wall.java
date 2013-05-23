package models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import controllers.MorphiaObject;

public class Wall extends Item {

	public ObjectId userId;
	
	public List<ObjectId> postIds;


	@Override
	public void delete() {
		Iterator<ObjectId> postIte = postIds.iterator();
		while(postIte.hasNext()){
			Publication publication = Publication.findById(postIte.next(), Publication.class);
			publication.delete();
			postIte.remove();
		}
		MorphiaObject.datastore.delete(this);
	}


	public void clear() {
		Iterator<ObjectId> postIte = postIds.iterator();
		while(postIte.hasNext()){
			Publication publication = Publication.findById(postIte.next(), Publication.class);
			publication.delete();
			postIte.remove();
		}
		postIds = null;
		this.save();
	}


	public void addPublication(Publication publication) {
		if (postIds == null){
			postIds = new ArrayList<ObjectId>();
		}
		if (!postIds.contains(publication.id)){
			postIds.add(publication.id);
		}
		this.save();
	}


	public void removePublication(Publication publication) {
		if (postIds != null){
			postIds.remove(publication.id);
			if (postIds.isEmpty()){
				postIds = null;
			}
		}
		this.save();
	}


	public List<Publication> getPublications(int amount) {

		List<Publication> publications = new ArrayList<Publication>();

		Iterator<ObjectId> postIte = postIds.iterator();
		int pos = 0;

		while(postIte.hasNext() && pos < amount){
			Publication publication = Publication.findById(postIte.next(), Publication.class);
			if (publication != null){
				publications.add(publication);
			} else {
				postIte.remove();
			}
		}
		return publications;
	}
	
	
	/** Parses a publication and prepares it for exporting to JSON
	 * @param publication
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode wallToShortObjectNode(Wall wall) {
		ObjectNode wallNode = Json.newObject();
		wallNode.put("id", wall.id.toString());
		wallNode.put("user", User.userSmallInfo(wall.userId));
		wallNode.put("publications", Json.toJson(Publication.publicationsIdsToObjectNodes(wall.postIds)));
		return wallNode;
	}

}
