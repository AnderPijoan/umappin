package controllers;

import models.Followed;
import models.Follows;
import org.codehaus.jackson.JsonNode;
import play.libs.Json;
import play.mvc.*;

import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import java.util.ArrayList;
import java.util.List;

public class FollowREST extends Controller {

    /** ---------------------------------- FOLLOWS --------------------------------------- **/
    
    // GET(ALL)
    @Restrict(@Group(Application.USER_ROLE))
    public static Result getAllFollows() {
        List<Follows> res = Follows.all();
        return ok(Json.toJson(res));
    }

    // GET
    @Restrict(@Group(Application.USER_ROLE))
    public static Result getFollows(String id) {
        Follows res = Follows.findById(id);
        if (res == null)
            return notFound("Follows not found");
        return ok(Json.toJson(res));
    }

    // POST
    @Restrict(@Group(Application.USER_ROLE))
    public static Result addFollows() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            String userId = json.findPath("userId").getTextValue();
            Follows follows = Follows.create(userId);
            List<String> flist = new ArrayList<String>();
            JsonNode listFollows = json.findPath("follow");
            for (JsonNode node:listFollows)
                flist.add(node.asText());
            follows.update(flist);
            return ok(Json.toJson(follows));
        }
    }

    // PUT
    @Restrict(@Group(Application.USER_ROLE))
    public static Result updateFollows(String id) {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            Follows follows = Follows.findById(id);
            if (follows == null)
                return  notFound("Invalid Index");
            List<String> flist = new ArrayList<String>();
            JsonNode listFollows = json.findPath("follow");
            for (JsonNode node:listFollows)
                flist.add(node.asText());
            follows.update(flist);
            return ok(Json.toJson(follows));
        }
    }

    // DELETE
    @Restrict(@Group(Application.USER_ROLE))
    public static Result deleteFollows(String id) {
        Follows follows = Follows.findById(id);
        if (follows != null) {
            Follows res = follows.delete();
            if (res != null)
                return ok(Json.toJson(res));
        }
        return notFound("Follows not found");
    }


    /** ---------------------------------- FOLLOWED --------------------------------------- **/

    // GET(ALL)
    @Restrict(@Group(Application.USER_ROLE))
    public static Result getAllFollowed() {
        List<Followed> res = Followed.all();
        return ok(Json.toJson(res));
    }

    // GET
    @Restrict(@Group(Application.USER_ROLE))
    public static Result getFollowed(String id) {
        Followed res = Followed.findById(id);
        if (res == null)
            return notFound("Followed not found");
        return ok(Json.toJson(res));
    }

    // POST
    @Restrict(@Group(Application.USER_ROLE))
    public static Result addFollowed() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            String userId = json.findPath("userId").getTextValue();
            Followed follows = Followed.create(userId);
            List<String> flist = new ArrayList<String>();
            JsonNode listFollowed = json.findPath("follow");
            for (JsonNode node:listFollowed)
                flist.add(node.asText());
            follows.update(flist);
            return ok(Json.toJson(follows));
        }
    }

    // PUT
    @Restrict(@Group(Application.USER_ROLE))
    public static Result updateFollowed(String id) {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            Followed follows = Followed.findById(id);
            if (follows == null)
                return  notFound("Invalid Index");
            List<String> flist = new ArrayList<String>();
            JsonNode listFollowed = json.findPath("follow");
            for (JsonNode node:listFollowed)
                flist.add(node.asText());
            follows.update(flist);
            return ok(Json.toJson(follows));
        }
    }

    // DELETE
    @Restrict(@Group(Application.USER_ROLE))
    public static Result deleteFollowed(String id) {
        Followed follows = Followed.findById(id);
        if (follows != null) {
            Followed res = follows.delete();
            if (res != null)
                return ok(Json.toJson(res));
        }
        return notFound("Followed not found");
    }
    

}
