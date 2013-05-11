package controllers;

import play.mvc.Result;

import models.Follows;


public class FollowsREST extends ItemREST {
	
	public static Result getAllFollows() { return getItems(Follows.class); }
    public static Result getFollows(String id) { return getItem(id, Follows.class); }
    public static Result addFollows() { return addItem(Follows.class); }
    public static Result updateFollows(String id) { return updateItem(id, Follows.class); }
    public static Result deleteFollows(String id) { return deleteItem(id, Follows.class); }

    
}
