package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Item {

	@Id
	public ObjectId id;

	public String itemName;

	public String itemDesc;

	public static List<Item> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Item.class).asList();
		} else {
			return new ArrayList<Item>();
		}
	}

	public static Item findById(String id) {
		return MorphiaObject.datastore.get(Item.class, new ObjectId(id));
	}

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public void delete() {
        MorphiaObject.datastore.delete(this);
    }

    public static Item create() {
       Item item = new Item();
       item.save();
       return item;
    }

	public JsonNode toJson() {
        JsonNode json = Json.toJson(this);
        ((ObjectNode)json).put("id", this.id.toString());
        return json;
	}

    public static Item fromJson(JsonNode json) {
        if (json.findValue("id") != null)
            ((ObjectNode)json).putPOJO("id", new ObjectId(json.findValue("id").asText()));
        return  Json.fromJson(json, Item.class);
    }

}