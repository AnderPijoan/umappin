package controllers;

import models.Award;
import models.AwardTrigger;
import models.UserAward;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.node.ObjectNode;

import static play.libs.Json.toJson;

/**
 * User: igarri
 * Date: 01/03/13
 * Time: 17.25
 */

public class RestAwards extends Controller{
	
	public static Result all(){
		List<Award> awards = Award.all();
		return ok(toJson(awards));
	}

	public static Result findById(String id){
		Award award = Award.findById(new ObjectId(id));
		return ok(toJson(award));
	}
	
	public static Result findByUserId(String userId) {
		List<UserAward> userAwards = UserAward.findByUserId(userId);
		if(userAwards == null){
			return badRequest("No Awards Found");
		}else{
			ObjectNode userAwardNode;
			UserAward userAward;
			List<ObjectNode> userAwardList = new ArrayList<ObjectNode>();
			Iterator<UserAward> i = userAwards.iterator();
			while(i.hasNext()) {
				userAward = i.next();
				userAwardNode = Json.newObject();
				userAwardNode.put("award", Json.toJson(Award.findById(userAward.award)));
				userAwardNode.put("timeStamp", Json.toJson(userAward.timeStamp));
				userAwardNode.put("isNew", userAward.isNew);
			
				userAwardList.add(userAwardNode);
			}
			
			return ok(Json.toJson(userAwardList));
		}
	}
	
	// TODO: Method to collect non-readed User Awards. How should be the route??
	public static Result findNewByUserId(String userId) {
		List<UserAward> userAwards = UserAward.findNewByUserId(userId);
		if(userAwards == null){
			return badRequest("No Awards Found");
		}else{
			return ok(toJson(userAwards));
		}
	}
	
	public static Result findByAwardTypeLimit(String triggerType, int limit) {
		Award awardWon = AwardTrigger.findByAwardTypeLimit(triggerType, limit);
		if(awardWon == null) {
			return badRequest("No Awards Found");
		}else{
			return ok(toJson(awardWon));
		}
	}
	/*
	public static Result saveUserAward() {
		
	}
	*/
}


