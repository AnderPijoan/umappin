package controllers;

public enum Constants {

	UNAUTHORIZED("Unauthorized operation"),
	
	JSON_EMPTY("No JSON data found"),
	JSON_MALFORMED("JSON data is not well formated"),

	USERS_EMPTY("No user found"),
	USER_NOT_LOGGED_IN("User not logged in"),
	
	TIMELINE_EMPTY("No timeline found"),

	MESSAGES_EMPTY("No message found"),

	DISCUSSIONS_EMPTY("No discussion found"),
	
	PUBLICATIONS_EMPTY("No publication found"),
	
	ROUTES_EMPTY("No route found"),

	STATISTICS_EMPTY("No statistics found"),
	STATISTICS_PARSE_ERROR("Error Parsing request"),

	NO_API_USER_FOUND("Non existing user");

	//... add more cases here ...

	private final String message;

	Constants(String message) {
		this.message = message;
	}

	@Override
	public String toString() { 
		return message; 
	}
}
