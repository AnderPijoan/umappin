package controllers;

import models.Award;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import org.bson.types.ObjectId;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import static play.libs.Json.toJson;

public class AwardREST extends Controller{

	// GET
	@Restrict(@Group(Application.USER_ROLE))
	public static Result all(){
		List<Award> awards = Award.all();
		return ok(toJson(awards));
	}

	// GET
	@Restrict(@Group(Application.USER_ROLE))
	public static Result findById(String id){
		Award award = Award.findById(id);
		return ok(toJson(award));
	}

	// GET
	@Restrict(@Group(Application.USER_ROLE))
	public static Result findByAwardType(String awardType) {
		List<Award> awardList = Award.findByType(awardType);
		if(awardList == null) {
			return badRequest("No Awards Found");
		}else{
			return ok(toJson(Award.awardsToObjectNodes(awardList)));
		}
	}
}


