package controllers;

import models.Award;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import org.bson.types.ObjectId;

import static play.libs.Json.toJson;

public class AwardREST extends Controller{

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
	public static Result findByAwardType(String awardType) {
		List<Award> awardList = Award.findByType(awardType);
		if(awardList == null) {
			return badRequest("No Awards Found");
		}else{
			return ok(toJson(Award.awardsToObjectNodes(awardList)));
		}
	}
}


