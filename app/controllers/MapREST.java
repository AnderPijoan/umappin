package controllers;

import models.Follows;
import models.Map;
import models.MapFeature;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.mvc.Result;

public class MapREST extends ItemREST {
	
	public static Result getAllMaps() { return getItems(Map.class); }

    public static Result getMap(String id) { return getItem(id, Map.class); }

    public static Result addMap() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else {
            ((ObjectNode)json).remove("id");
            Map map = Map.mapFromJson(json);
            map.save();
            return ok(map.toJson());
        }
    }

    public static Result updateMap(String id) {
        JsonNode json = request().body().asJson();
        if(json == null || id == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else if (Map.findById(id, Map.class) == null) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            ((ObjectNode)json).put("id", id);
            Map map = Map.mapFromJson(json);
            map.save();
            return ok(map.toJson());
        }
    }

    public static Result deleteMap(String id) { return deleteItem(id, Map.class); }

}
