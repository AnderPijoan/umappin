package controllers;

import static play.libs.Json.toJson;

import java.util.Map;

import models.UserStatistics;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.ObjectMapper;

import play.libs.Json;
import play.mvc.Result;

public class UserStatisticsREST {

	// GET
	public static Result findByUserId(String userId) {
		UserStatistics userStatistics = UserStatistics.findByUserId(userId);
		if(userStatistics == null){
			return badRequest("No User Statistics Found");
		}else{
			return ok(Json.toJson(UserStatistics.userStatisticsToObjectNode(userStatistics)));
		}
	}

	// PUT
	public static Result updateUserStatistics(String userId) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest("No Statistics received");
		}
		UserStatistics userStatistics = new UserStatistics();
		userStatistics.userId = userId;
		Map<String, Integer> statistics = mapper
				.readValue(json.findPath("statistics"), new TypeReference<Map<String,Integer>>() { });

		return ok("User Award Saved");
	}
}
