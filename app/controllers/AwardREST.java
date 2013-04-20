package controllers;

import models.Award;
import models.AwardTrigger;
import models.User;
import models.UserAward;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import static play.libs.Json.toJson;

public class AwardREST extends Controller{
	/*
	// GET
	public static Result all(){
		List<Award> awards = Award.all();
		return ok(toJson(awards));
	}

	// GET
	public static Result findById(String id){
		Award award = Award.findById(new ObjectId(id));
		return ok(toJson(award));
	}
	
	// GET
	public static Result findByUserId(String userId) {
		List<UserAward> userAwards = UserAward.findByUserId(userId);
		if(userAwards == null){
			return badRequest("No Awards Found");
		}else{
			return ok(Json.toJson(UserAward.userAwardsToObjectNodes(userAwards)));
		}
	}
	
	// TODO: 	Method to collect non-read User Awards. How should be the route??
	//			It also should update the non-read flags to read if the call is made
	//			by the awards owner.
	public static Result findNewByUserId(String userId) {
		List<UserAward> userAwards = UserAward.findNewByUserId(userId);
		if(userAwards == null){
			return badRequest("No Awards Found");
		}else{
			return ok(Json.toJson(UserAward.userAwardsToObjectNodes(userAwards)));
		}
	}
	
	// GET
	public static Result findByAwardTypeLimit(String triggerType, int limit) {
		Award awardWon = AwardTrigger.findByAwardTypeLimit(triggerType, limit);
		if(awardWon == null) {
			return badRequest("No Awards Found");
		}else{
			return ok(toJson(Award.awardToObjectNode(awardWon)));
		}
	}

	// POST
	public static Result saveUserAward(String userId) {
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest("No award received");
		}
		UserAward userAward = new UserAward();
		userAward.userId = userId;
		userAward.award = new ObjectId(json.findPath("id").asText());
		if(userAward.save() == null) {
			return badRequest("User Award Not Saved");
		}else {
			return ok("User Award Saved");
		}
	}*/
}


