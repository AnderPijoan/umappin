package controllers;

import java.io.IOException;
import java.util.Map;

import models.UserStatistics;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class UserStatisticsREST extends Controller {

	// GET
	public static Result findByUserId(String userId) {
		UserStatistics userStatistics = UserStatistics.findByUserId(userId);
		if(userStatistics == null){
			return badRequest(Constants.STATISTICS_EMPTY.toString());
		}else{
			return ok(Json.toJson(UserStatistics.userStatisticsToObjectNode(userStatistics)));
		}
	}

	// PUT
	public static Result updateUserStatistics(String userId) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = request().body().asJson();
		Map<String, Integer> statistics;
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		try{
			statistics = mapper
				.readValue(json.findPath("statistics"), new TypeReference<Map<String,Integer>>() { });
		}catch(JsonMappingException e){
			return badRequest(Constants.STATISTICS_PARSE_ERROR.toString());
		}catch(JsonParseException e){
			return badRequest(Constants.STATISTICS_PARSE_ERROR.toString());
		}catch(IOException e){
			return badRequest(Constants.STATISTICS_PARSE_ERROR.toString());
		}
		UserStatistics userStatistics = UserStatistics.updateByUserId(userId, statistics);
		return ok(UserStatistics.userStatisticsToObjectNode(userStatistics));
	}
}
