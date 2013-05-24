package models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import controllers.MorphiaObject;

public class Timeline extends Item {

	///////////////////////////////////////////////////////////////////////////////
	// THE OBJECTID OF WALL IS THE SAME AS THE USERS, TO GET IT DIRECTLY
	///////////////////////////////////////////////////////////////////////////////

	public ObjectId userId;

	public List<ObjectId> postIds;

	public List<Publication> all() {
		List<Publication> publications = new ArrayList<Publication>();

		Iterator<ObjectId> postIte = postIds.iterator();

		while(postIte.hasNext()){
			Publication publication = Publication.findById(postIte.next(), Publication.class);
			if (publication != null){
				publications.add(publication);
			} else {
				postIte.remove();
			}
		}
		return publications;
	}

	
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


	public Publication findPublicationById(String id) {
		if (postIds != null && postIds.contains(new ObjectId(id))){
			Publication publication = Publication.findById(new ObjectId(id), Publication.class);
			if (publication != null){
				return publication;
			} else {
				postIds.remove(new ObjectId(id));
			}
		} 
		return null;
	}
	
	
	public void addPublication(Publication publication) {
		if (postIds == null){
			postIds = new ArrayList<ObjectId>();
		}
		if (!postIds.contains(publication.id)){
			postIds.add(0, publication.id);
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


	public List<Publication> getPublications(int from, int to) {

		List<Publication> publications = new ArrayList<Publication>();

		Iterator<ObjectId> postIte = postIds.iterator();
		int pos = 0;

		while(postIte.hasNext() && pos < to){
			if (from <= pos && pos < to){
				Publication publication = Publication.findById(postIte.next(), Publication.class);
				if (publication != null){
					publications.add(publication);
				} else {
					postIte.remove();
				}
			}
		}
		return publications;
	}


	/** Parses a publication and prepares it for exporting to JSON
	 * @param publication
	 * @return ObjectNode ready for use in toJson
	 */
	public static ObjectNode timelineToShortObjectNode(Timeline timeline) {
		ObjectNode timelineNode = Json.newObject();
		timelineNode.put("id", timeline.id.toString());
		timelineNode.put("user", User.userToShortObjectNode(timeline.userId));
		timelineNode.put("publications", Json.toJson(Publication.publicationsToObjectNodes(timeline.getPublications(0, 10))));
		return timelineNode;
	}

}
