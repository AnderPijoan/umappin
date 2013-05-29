package controllers;


import java.util.*;
import java.util.Map;


import models.*;
import org.bson.types.ObjectId;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

import play.mvc.Result;


public class RouteLikeREST extends ItemREST {
	
	public static Result getAllRouteLikes() { return getItems(RouteLike.class); }
    public static Result getRouteLike(String id) { return getItem(id, RouteLike.class); }

    public static Result addRouteLike() {
        JsonNode json = request().body().asJson();
        String userId = json.findPath("userId").getTextValue();
        Map<String, Integer> stat = new HashMap<>();
        stat.put(StatisticTypes.ROUTELIKES.name(), 1);
        UserStatisticsREST.updateUserStatistics(userId, stat);
        return addItem(RouteLike.class);
    }

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
