package controllers;

import models.Follow;
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

    // GET(ALL)
    @Restrict(@Group(Application.USER_ROLE))
    public static Result getAllFollows() {
        List<Follows> res = Follows.all();
        return ok(Json.toJson(res));
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static Result getAllFollowed() {
        List<Followed> res = Followed.all();
        return ok(Json.toJson(res));
    }

    // GET
    public static Result getFollows(String id) {
        return getFollow(Follows.findById(id));
    }

    public static Result getFollowed(String id) {
        return getFollow(Followed.findById(id));
    }

    public static Result getFollowedByUserId(String id) {
        System.out.println(id);
        return getFollow(Followed.findByUserId(id));
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static <T extends Follow> Result getFollow(T follow) {
        if (follow == null)
            return notFound("Follows not found");
        return ok(Json.toJson(follow));
    }

    // POST
    public static Result addFollows() {
        return addFollow(new Follows());
    }

    public static Result addFollowed() {
        return addFollow(new Followed());
    }

    @Restrict(@Group(Application.USER_ROLE))
    private static <T extends Follow> Result addFollow(T follow) {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            String userId = json.findPath("userId").getTextValue();
            follow.init(userId);
            List<String> flist = new ArrayList<String>();
            JsonNode listFollows = json.findPath("follow");
            for (JsonNode node:listFollows)
                flist.add(node.asText());
            follow.update(flist);
            return ok(Json.toJson(follow));
        }
    }

    // PUT
    public static Result updateFollows(String id) {
        return updateFollow(Follows.findById(id));
    }

    public static Result updateFollowed(String id) {
        return updateFollow(Followed.findById(id));
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static <T extends Follow> Result updateFollow(T follow) {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            if (follow == null)
                return  notFound("Invalid Index");
            List<String> flist = new ArrayList<String>();
            JsonNode listFollows = json.findPath("follow");
            for (JsonNode node:listFollows)
                flist.add(node.asText());
            follow.update(flist);
            return ok(Json.toJson(follow));
        }
    }

    // DELETE
    public static Result deleteFollows(String id) {
        return deleteFollow(Follows.findById(id));
    }

    public static Result deleteFollowed(String id) {
        return deleteFollow(Followed.findById(id));
    }

    @Restrict(@Group(Application.USER_ROLE))
    public static <T extends Follow> Result deleteFollow(T follow) {
        if (follow != null) {
            Follow res = follow.delete();
            if (res != null)
                return ok(Json.toJson(res));
        }
        return notFound("Follows not found");
    }

}
