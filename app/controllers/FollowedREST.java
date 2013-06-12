package controllers;

import models.Followed;
import models.Follows;

import models.User;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import play.mvc.Result;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import play.libs.Json;
import org.codehaus.jackson.node.ObjectNode;

public class FollowedREST extends ItemREST {
	
	public static Result getAllFollowed() { return getItems(Followed.class); }
    public static Result getFollowed(String id) { return getItem(id, Followed.class); }
    public static Result addFollowed() { return addItem(Followed.class); }
    public static Result updateFollowed(String id) { return updateItem(id, Followed.class); }
    public static Result deleteFollowed(String id) { return deleteItem(id, Followed.class); }

    public static Result getAllFollowedInfo(){

    	final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
    	Followed  userFollowed = Followed.findByUserId(user.id);
		if (userFollowed == null){
			return ok("[]");
		}
    	
    	List<ObjectNode> followedNode = new ArrayList<ObjectNode>();

    	Iterator<String> followsIte = userFollowed.follow.iterator();
    	
    	while(followsIte.hasNext()){
    		
    		User usr = User.findById(followsIte.next() ,User.class);
    		if (usr != null){
    			followedNode.add(User.userToShortObjectNode(usr));
    		} else {
    			followsIte.remove();
    			userFollowed.save();
    		}
    	}
    	return ok(Json.toJson(followedNode));

    }

    // Get all the user followed
    public static Result getAllUserFollowed(){
        final User user = Application.getLocalUser(session());
        if (user == null)
            return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        List<Followed> userFollowed = Followed.findRelatedByUserId(user.id.toString());
        if (userFollowed == null)
            return badRequest();
        List<JsonNode> nodes = new ArrayList<JsonNode>();
        for (Followed uf : userFollowed)
            nodes.add(uf.toJson());
        return ok(Json.toJson(nodes));

    }

    // Get just the user followed
    public static Result getUserFollowed(String userId){
        final User user = Application.getLocalUser(session());
        if (user == null)
            return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        Followed userFollowed = Followed.findByUserId(new ObjectId(userId));
        if (userFollowed == null) {
            userFollowed = new Followed();
            userFollowed.setUserId(userId);
            userFollowed.save();
        }
        return ok(userFollowed.toJson());
    }
}