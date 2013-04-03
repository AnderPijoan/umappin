package controllers;

import models.Item;
import org.codehaus.jackson.JsonNode;

import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

public class ItemREST extends Controller {

    public static Result getItems() {
        List<Item> items =  Item.all();
        if (items.size() == 0) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            List<JsonNode> nodes = new ArrayList<JsonNode>();
            for (Item itm : items)
                nodes.add(itm.toJson());
            return ok(Json.toJson(nodes));
        }
    }

    public static Result getItem(String id) {
        Item item =  Item.findById(id);
        if (item == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else {
            return ok(item.toJson());
        }
    }

    public static Result addItem() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else {
            Item item = Item.fromJson(json);
            item.save();
            return ok(item.toJson());
        }
    }

    public static Result updateItem(String id) {
        JsonNode json = request().body().asJson();
        if(json == null || id == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else if (Item.findById(id) == null) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            ((ObjectNode)json).put("id", id);
            Item item = Item.fromJson(json);
            item.save();
            return ok(item.toJson());
        }
    }

    public static Result deleteItem(String id) {
        Item item = Item.findById(id);
        if (item == null) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            item.delete();
            return ok(item.toJson());
        }
    }

    public static Result addTestItem(String name) {
        if(name == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else {
            Item item = new Item();
            item.itemName = name;
            item.save();
            return ok(item.toJson());
        }
    }

}
