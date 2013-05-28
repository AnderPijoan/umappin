package models;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Json;
import scala.util.parsing.json.JSONArray;
import scala.util.parsing.json.JSONObject;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


@Entity
public class RouteLike extends Item {

	private static final long serialVersionUID = 1L;

    public ObjectId routeId;
    public ObjectId userId;
    public String comment;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getRouteId() {
        return routeId;
    }

    public void setRouteId(ObjectId routeId) {
        this.routeId = routeId;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public JsonNode toJson() {
        JsonNode json = super.toJson();
        ((ObjectNode)json).put("routeId", this.routeId.toString());
        ((ObjectNode)json).put("userId", this.userId.toString());
        return json;
    }

    /*
    public static <T extends Item> T fromJson(JsonNode json, Class<T> klass) {
        return Json.fromJson(fromJson(json), klass);
    }
    */

    public static JsonNode fromJson(JsonNode json) {
        if (json.has("id"))
            ((ObjectNode)json).putPOJO("id", new ObjectId(json.findValue("id").asText()));
        if (json.has("routeId"))
            ((ObjectNode)json).putPOJO("routeId", new ObjectId(json.findValue("routeId").asText()));
        if (json.has("userId"))
            ((ObjectNode)json).putPOJO("userId", new ObjectId(json.findValue("userId").asText()));
        return json;
    }

    public static JsonNode listToJson(List<RouteLike> list) {
        ArrayNode aux = new ArrayNode(JsonNodeFactory.instance);
        for (RouteLike rl : list)
            aux.add(rl.toJson());
        return aux;
    }

    public static List<RouteLike> findRouteUserRouteLike(String idr, String idu) {
            return MorphiaObject.datastore.find(RouteLike.class)
                    .field("routeId").equal(new ObjectId(idr))
                    .field("userId").equal(new ObjectId(idu)).asList();
    }

}
