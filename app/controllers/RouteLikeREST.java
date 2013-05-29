package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.RouteLike;
import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.Result;

import models.Followed;
import models.Follows;
import models.User;


public class RouteLikeREST extends ItemREST {
	
	public static Result getAllRouteLikes() { return getItems(RouteLike.class); }
    public static Result getRouteLike(String id) { return getItem(id, RouteLike.class); }
    public static Result addRouteLike() { return addItem(RouteLike.class); }
    public static Result updateRouteLike(String id) { return updateItem(id, RouteLike.class); }
    public static Result deleteRouteLike(String id) { return deleteItem(id, RouteLike.class); }

    public static Result getUserRouteLikes(String id) {
        List<RouteLike> rl = RouteLike.findWhere("userId", new ObjectId(id), RouteLike.class);
        return ok(RouteLike.listToJson(rl));
    }

    public static Result getRouteRouteLikes(String id) {
        List<RouteLike> rl = RouteLike.findWhere("routeId", new ObjectId(id), RouteLike.class);
        return ok(RouteLike.listToJson(rl));
    }

    public static Result getRouteUserRouteLikes(String idr, String idu) {
        List<RouteLike> rl = RouteLike.findRouteUserRouteLike(idr, idu);
        return ok(String.valueOf(rl.size()));
    }
}
