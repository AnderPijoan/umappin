package controllers;

import models.Award;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import static play.libs.Json.toJson;
import static play.mvc.Results.ok;

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
}
