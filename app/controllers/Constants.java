package controllers;

public enum Constants {

	JSON_EMPTY("No JSON data found"),
	JSON_MALFORMED("JSON data is not well formated"),

	USERS_EMPTY("No user found"),
	USER_NOT_LOGGED_IN("User not logged in"),

	MESSAGES_EMPTY("No message found"),

	DISCUSSIONS_EMPTY("No discussion found");

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
