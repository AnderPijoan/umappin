package models;

import com.google.code.morphia.annotations.Entity;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;

import java.util.HashMap;
import java.util.List;
@Entity
public class Map extends Item {

	private static final long serialVersionUID = 1L;

    public ObjectId ownerId;
    public List<HashMap<String, String>> features;

    public ObjectId getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(ObjectId userId) {
        this.ownerId = userId;
    }

    public List<HashMap<String, String>> getFeatures() {
        return features;
    }

    public void setFeatures(List<HashMap<String, String>> features) {
        this.features = features;
    }


    /** ------------ Map model needs special ObjectIds handling ------------- **/
    @Override
    public JsonNode toJson() {
        JsonNode json = super.toJson();
        ((ObjectNode)json).put("ownerId", ownerId != null ? ownerId.toString() : null);
        return json;
    }

    public static Map mapFromJson(JsonNode srcJson) {
        JsonNode json = srcJson;
        if (json.has("id") && !json.findValue("id").isNull())
            ((ObjectNode)json).putPOJO("id", new ObjectId(json.findValue("id").asText()));
        JsonNode jtemp = json.findValue("ownerId");
        if (jtemp != null && !jtemp.isNull())
            ((ObjectNode)json).putPOJO("ownerId", new ObjectId(jtemp.asText()));
        return Json.fromJson(json, Map.class);
    }

}
