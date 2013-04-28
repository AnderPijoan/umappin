package controllers;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import models.User;
import models.UserStatistics;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class UserStatisticsREST extends Controller {

	// GET
	@Restrict(@Group(Application.USER_ROLE))
	public static Result findByUserId(String userId) {
		ObjectNode node;
		boolean isConnectedUser = false;
		final User connectedUser = Application.getLocalUser(session());
		if(connectedUser != null){
			isConnectedUser = (userId.equals(connectedUser.getIdentifier()));
		}
		UserStatistics userStatistics = UserStatistics.findByUserId(userId);
		if(userStatistics == null){
			userStatistics = UserStatistics.init(userId).save();
			node = UserStatistics.userStatisticsToObjectNode(userStatistics);
		}else if(isConnectedUser) { // set to false the non-read data flags...
			node = UserStatistics.userStatisticsToObjectNode(userStatistics);
			userStatistics.setRead();
			userStatistics.save();
		}else {
			node = UserStatistics.userStatisticsToObjectNode(userStatistics);
		}
		return ok(Json.toJson(node));
	}

	// PUT
	@Restrict(@Group(Application.USER_ROLE))
	public static Result updateUserStatistics(String userId) {
		boolean isConnectedUser = false;
		final User connectedUser = Application.getLocalUser(session());
		if(connectedUser != null){
			isConnectedUser = (userId == connectedUser.getIdentifier());
		}
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = request().body().asJson();
		Map<String, Integer> statistics;
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		// Parse the received json:
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
		// Retrieve the existing Statistics:
		UserStatistics userStatistics = UserStatistics.findByUserId(userId);
		if(userStatistics == null){
			userStatistics = UserStatistics.init(userId);
		}
		Iterator<String> it = statistics.keySet().iterator();
		String key;
		while (it.hasNext()) {
			key = it.next();
			userStatistics.updateStatistic(key, statistics.get(key));
		}
		UserStatistics userStatisticsToUpdate = userStatistics;
		if(isConnectedUser){
			userStatisticsToUpdate.setRead();
		}
		userStatisticsToUpdate.update();
		return ok(UserStatistics.userStatisticsToObjectNode(userStatistics));
	}
}
