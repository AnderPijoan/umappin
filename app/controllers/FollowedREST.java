package controllers;

import models.Followed;

import models.User;
import controllers.UserREST;

import play.mvc.Result;
import java.util.ArrayList;
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
    	if (userFollowed ==null){
    		return ok("[]");
    	}
    	List<ObjectNode> followedNode = new ArrayList<ObjectNode>();

    	for (String followId : userFollowed.follow){
    		followedNode.add(User.userToShortObjectNode(User.findById(followId,User.class)));
    	}

    	/*
        ((ObjectNode)json).put("id", this.id.toString())
        ((ObjectNode)json).put("email", this.email.toString());
        ((ObjectNode)json).put("name", this.name.toString());

        return json;*/
	
    	return ok(Json.toJson(followedNode));

    }

}