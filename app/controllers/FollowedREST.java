package controllers;

import models.Followed;
import play.mvc.Result;

public class FollowedREST extends ItemREST {
	
	public static Result getAllFollowed() { return getItems(Followed.class); }
    public static Result getFollowed(String id) { return getItem(id, Followed.class); }
    public static Result addFollowed() { return addItem(Followed.class); }
    public static Result updateFollowed(String id) { return updateItem(id, Followed.class); }
    public static Result deleteFollowed(String id) { return deleteItem(id, Followed.class); }

}