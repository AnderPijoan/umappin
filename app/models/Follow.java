package models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;

public class Follow extends Item {

	private static final long serialVersionUID = 1L;

    public String userId;
    public List<String> follow = new ArrayList<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getFollow() {
        return follow;
    }

    @JsonIgnore
    public List<ObjectId> getFollowOids() {
    	List<ObjectId> result = new ArrayList<ObjectId>();
    	
    	for(String id : follow){
    		result.add(new ObjectId(id));
    	}
        return result;
    }

    public void setFollow(List<String> follow) {
        this.follow = follow;
    }

}
