package controllers;

import java.util.*;
import java.util.Map;
import models.*;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import play.mvc.Result;

public class FeatureLikeREST extends ItemREST {
	
	public static Result getAllFeatureLikes() { return getItems(FeatureLike.class); }
    public static Result getFeatureLike(String id) { return getItem(id, FeatureLike.class); }

    public static Result addFeatureLike() {
        JsonNode json = request().body().asJson();
        String userId = json.findPath("userId").getTextValue();
        Map<String, Integer> stat = new HashMap<>();
        stat.put(StatisticTypes.FEATURELIKES.name(), 1);
        UserStatisticsREST.updateUserStatistics(userId, stat);
        return addItem(FeatureLike.class);
    }

    public static Result updateFeatureLike(String id) { return updateItem(id, FeatureLike.class); }
    public static Result deleteFeatureLike(String id) { return deleteItem(id, FeatureLike.class); }

    public static Result getUserFeatureLikes(String id) {
        List<FeatureLike> fl = FeatureLike.findWhere("userId", new ObjectId(id), FeatureLike.class);
        return ok(FeatureLike.listToJson(fl));
    }

    public static Result getFeatureFeatureLikes(String id) {
        List<FeatureLike> fl = FeatureLike.findWhere("featureId", new ObjectId(id), FeatureLike.class);
        return ok(FeatureLike.listToJson(fl));
    }

    public static Result getFeatureUserFeatureLikes(String idf, String idu) {
        List<FeatureLike> fl = FeatureLike.findFeatureUserFeatureLike(idf, idu);
        return ok(String.valueOf(fl.size()));
    }
}
