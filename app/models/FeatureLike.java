package models;

import com.google.code.morphia.annotations.Entity;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.util.List;


@Entity
public class FeatureLike extends Item {

	private static final long serialVersionUID = 1L;

    public long featureId;
    public ObjectId userId;
    public String comment;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public long getFeatureId() {
        return featureId;
    }

    public void setFeatureId(long featureId) {
        this.featureId = featureId;
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
        ((ObjectNode)json).put("featureId", this.featureId);
        ((ObjectNode)json).put("userId", this.userId.toString());
        return json;
    }

    public static JsonNode fromJson(JsonNode json) {
        if (json.has("id"))
            ((ObjectNode)json).putPOJO("id", new ObjectId(json.findValue("id").asText()));
        if (json.has("featureId"))
            ((ObjectNode)json).putPOJO("featureId", json.findValue("featureId").asLong());
        if (json.has("userId"))
            ((ObjectNode)json).putPOJO("userId", new ObjectId(json.findValue("userId").asText()));
        return json;
    }

    public static JsonNode listToJson(List<FeatureLike> list) {
        ArrayNode aux = new ArrayNode(JsonNodeFactory.instance);
        for (FeatureLike fl : list)
            aux.add(fl.toJson());
        return aux;
    }

    public static List<FeatureLike> findFeatureUserFeatureLike(String idf, String idu) {
            return MorphiaObject.datastore.find(FeatureLike.class)
                    .field("featureId").equal(Long.parseLong(idf))
                    .field("userId").equal(new ObjectId(idu)).asList();
    }

}
