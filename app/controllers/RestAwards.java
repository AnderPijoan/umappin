package controllers;

import models.Award;
import models.UserAward;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
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
		Award award = Award.findById(id);
		return ok(toJson(award));
	}
	
	public static Result findByUserId(String userId) {
		List<UserAward> userAwards = UserAward.findByUserId(userId);
		if(userAwards == null){
			return ok(toJson(userAwards)); //TODO: Shouldn't return an 'ok'.
		}else{
			return ok(toJson(userAwards));
		}
	}
	
	// TODO: Method to collect non-readed User Awards. How should be the route??
	public static Result findNewByUserId(String userId) {
		List<UserAward> userAwards = UserAward.findNewByUserId(userId);
		if(userAwards == null){
			return ok(toJson(userAwards)); //TODO: Shouldn't return an 'ok'.
		}else{
			return ok(toJson(userAwards));
		}
	}
	
	public static Result findByAwardTypeLimit(String triggerType, int limit) {
		List<Award> awardsWon = AwardTrigger.findByAwardTypeLimit(triggerType, limit);
		if(awardsWon == null) {
			return ok(toJson(awardsWon)); //TODO: Shouldn't return an 'ok'.
		}else{
			return ok(toJson(awardsWon));
		}
		}
	}
}


