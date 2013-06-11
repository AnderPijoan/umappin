package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.Result;

import models.Followed;
import models.Follows;
import models.User;
import org.bson.types.ObjectId;



public class FollowsREST extends ItemREST {
	
	public static Result getAllFollows() { return getItems(Follows.class); }
    public static Result getFollows(String id) { return getItem(id, Follows.class); }
    public static Result addFollows() { return addItem(Follows.class); }
    public static Result updateFollows(String id) { return updateItem(id, Follows.class); }
    public static Result deleteFollows(String id) { return deleteItem(id, Follows.class); }
    
    public static Result getAllFollowsInfo(){

    	final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
    	Follows userFollows = Follows.findByUserId(user.id);
    	if (userFollows == null){
    		return ok("[]");
    	}
    	List<ObjectNode> followsNode = new ArrayList<ObjectNode>();

    	Iterator<String> followsIte = userFollows.follow.iterator();
    	
    	while(followsIte.hasNext()){
    		
    		User usr = User.findById(followsIte.next() ,User.class);
    		if (usr != null){
    			followsNode.add(User.userToShortObjectNode(usr));
    		} else {
    			followsIte.remove();
    			userFollows.save();
    		}
    	}
    	return ok(Json.toJson(followsNode));
    }

    public static Result addfollows(String id){
        final User user = Application.getLocalUser(session());
        if (user == null){
            return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        }
        Follows  userFollows = Follows.findByUserId(user.id);
        if (userFollows == null){
            userFollows = new Follows();
            userFollows.setUserId(user.id.toString());

        }
        List<String> follows = userFollows.follow;
        follows.add(id);
        userFollows.save();

        //Update user followedInfo

        Followed usersFollowed = Followed.findByUserId(new ObjectId(id));
        if (usersFollowed == null){
            usersFollowed = new Followed();
            usersFollowed.setUserId(id);
        }
        List<String>followed = usersFollowed.follow;
        followed.add(user.id.toString());
        usersFollowed.save();
        return ok(Json.toJson(follows));

    }
    public static Result unfollow(String id){
        final User user = Application.getLocalUser(session());
        if (user == null){
            return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        }
        Follows  userFollows = Follows.findByUserId(user.id);
        if (userFollows == null){
            userFollows = new Follows();
            userFollows.setUserId(user.id.toString());

        }
        List<String> follows = userFollows.follow;
        follows.remove(id);
        userFollows.save();

        //Update user followedInfo

        Followed usersFollowed = Followed.findByUserId(new ObjectId(id));
        if (usersFollowed == null){
            usersFollowed = new Followed();
            usersFollowed.setUserId(id);
        }
        List<String>followed = usersFollowed.follow;
        followed.remove(user.id.toString());
        usersFollowed.save();
        return ok(Json.toJson(follows));

    }

    // Get all the user follows
    public static Result getAllUserFollows(){
        final User user = Application.getLocalUser(session());
        if (user == null)
            return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        List<Follows> userFollows = Follows.findRelatedByUserId(user.id.toString());
        if (userFollows == null)
            return badRequest();
        List<JsonNode> nodes = new ArrayList<JsonNode>();
        for (Follows uf : userFollows)
            nodes.add(uf.toJson());
        return ok(Json.toJson(nodes));
    }

    // GEt just the user follows
    public static Result getUserFollows(String userId){
        final User user = Application.getLocalUser(session());
        if (user == null)
            return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
        Follows userFollows = Follows.findByUserId(new ObjectId(userId));
        if (userFollows == null) {
            if (userFollows == null) {
                userFollows = new Follows();
                userFollows.setUserId(userId);
                userFollows.save();
            }
        }
        return ok(userFollows.toJson());
    }
}
