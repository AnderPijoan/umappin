package models;

import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Item {

	@Id
	public ObjectId id;

	public static <T extends Item> List<T> all(Class<T> klass) {
		if (MorphiaObject.datastore != null)
			return MorphiaObject.datastore.find(klass).asList();
		else
			return new ArrayList<T>();
	}

	public static <T extends Item> T findById(String id, Class<T> klass) {
		return MorphiaObject.datastore.get(klass, new ObjectId(id));
	}
	
	public static <T extends Item> T findById(ObjectId oid, Class<T> klass) {
		return MorphiaObject.datastore.get(klass, oid);
	}
	
	public static <T extends Item> List<T> findWhere(String field, Object equals, Class<T> klass) {
		return MorphiaObject.datastore.find(klass).field(field).equal(equals).asList();
	}
	
	public static <T extends Item> List<T> findWhere(String field, Object equals, int limit, Class<T> klass) {
		return MorphiaObject.datastore.find(klass).field(field).equal(equals).limit(limit).asList();
	}
	
	public static <T extends Item> List<T> findWhere(String field, Object equals, String order, Class<T> klass) {
		return MorphiaObject.datastore.find(klass).field(field).equal(equals).order(order).asList();
	}
	
	public static <T extends Item> List<T> findWhere(String field, Object equals, int from, int to, String order, Class<T> klass) {
		return MorphiaObject.datastore.find(klass).field(field).equal(equals).order(order).offset(from).limit(to-from).asList();
	}
	
	public static <T extends Item> List<T> findWhere(String field, Object equals, int limit, String order, Class<T> klass) {
		return MorphiaObject.datastore.find(klass).field(field).equal(equals).order(order).limit(limit).asList();
	}

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public void delete() {
        MorphiaObject.datastore.delete(this);
    }

    public static <T extends Item> T create(Class<T> klass) {
        T item = null;
        try {
            item = klass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        item.save();
        return item;
    }

	public JsonNode toJson() {
        JsonNode json = Json.toJson(this);
        ((ObjectNode)json).put("id", this.id.toString());
        return json;
	}

    public static <T extends Item> T fromJson(JsonNode json, Class<T> klass) {
        return Json.fromJson(fromJson(json), klass);
    }

    public static JsonNode fromJson(JsonNode json) {
        Iterator<JsonNode> it = json.getElements();
        while (it.hasNext()) {
            JsonNode jsn = it.next();
            if (jsn.isArray() || jsn.isContainerNode())
               jsn = fromJson(jsn);
        }
        if (json.has("id"))
            ((ObjectNode)json).putPOJO("id", new ObjectId(json.findValue("id").asText()));
        return json;
    }

}