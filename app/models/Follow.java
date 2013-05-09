package models;

import java.util.List;

public class Follow extends Item {

	private static final long serialVersionUID = 1L;

    public String userId;
    public List<String> follow;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getFollow() {
        return follow;
    }

    public void setFollow(List<String> follow) {
        this.follow = follow;
    }

}
