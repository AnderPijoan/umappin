package controllers;

import models.SubItem;
import play.mvc.Result;

public class SubItemREST extends ItemREST {

    public static Result getAll() { return getItems(SubItem.class); }
    public static Result get(String id) { return getItem(id, SubItem.class); }
    public static Result post() { return addItem(SubItem.class); }
    public static Result put(String id) { return updateItem(id, SubItem.class); }
    public static Result delete(String id) { return deleteItem(id, SubItem.class); }

}
