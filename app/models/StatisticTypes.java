package models;

public enum StatisticTypes {
	LOGINS				("Logins"),
    EDITIONS			("Map Editions"),
    APPROVALS   		("Map Edition Approvals received"),
    PARTICIPATIONS		("Game Participations"),
    WINS				("Games won"),
    UNFINISHED			("Games unfinished"),
    PHOTOLIKES			("Likes on Loaded photos"),
    ROUTELIKES			("RouteLikes"),
    FEATURELIKES		("FeatureLikes");
	
	private final String description;
	
	private StatisticTypes(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
}
