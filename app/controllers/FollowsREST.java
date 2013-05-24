package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.Result;

import models.Followed;
import models.Follows;
import models.User;


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
    
}
