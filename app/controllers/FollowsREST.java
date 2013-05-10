package controllers;

import models.Follows;
import models.User;
import controllers.UserREST;

import play.mvc.Result;
import java.util.ArrayList;
import java.util.List;
import play.libs.Json;
import org.codehaus.jackson.node.ObjectNode;



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

    	

    	Follows  userFollows= Follows.findByUserId(user.id);
    	if (userFollows ==null){
    		return badRequest("No follows".toString());
    	}
    	List<ObjectNode> followsNode = new ArrayList<ObjectNode>();
		
    	for (String followId : userFollows.follow){
    		followsNode.add(User.userToShortObjectNode(User.findById(followId,User.class)));

    	}




    	/*
        ((ObjectNode)json).put("id", this.id.toString())
        ((ObjectNode)json).put("email", this.email.toString());
        ((ObjectNode)json).put("name", this.name.toString());

        return json;*/
	
    	return ok(Json.toJson(followsNode));

    }
}
