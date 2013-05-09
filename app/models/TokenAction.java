package models;

import java.util.Date;
import controllers.MorphiaObject;
import com.google.code.morphia.annotations.Entity;
import play.data.format.Formats;

@Entity
public class TokenAction extends Item {

    /** ------------------------ Authentication parameters -------------------------- **/

	public enum Type {
		//@EnumValue("EV")
		EMAIL_VERIFICATION,
		//@EnumValue("PR")
		PASSWORD_RESET
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Verification time frame (until the user clicks on the link in the email) in seconds
	 * Defaults to one week
	 */
	private final static long VERIFICATION_TIME = 7 * 24 * 3600;

    /** ------------------------ Attributes ------------------------- **/

	//@Column(unique = true)
	public String token;
	//@ManyToOne
	public User targetUser;
	public Type type;
	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date created;
	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date expires;

    /** ------------------------ Getters / Setters ------------------------- **/

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    /** ------------------------ Authentication methods -------------------------- **/

    public static TokenAction findByToken(final String token, final Type type) {
        return MorphiaObject.datastore.find(TokenAction.class)
                .field("token").equal(token)
                .field("type").equal(type).get();
	}

	public static void deleteByUser(final User u, final Type type) {
        MorphiaObject.datastore.delete(
                MorphiaObject.datastore.find(TokenAction.class)
                    .field("targetUser.id").equal(u.id)
                    .field("type").equal(type)
        );
	}

	public boolean isValid() {
		return this.expires.after(new Date());
	}

	public static TokenAction create(final Type type, final String token,
			final User targetUser) {
		final TokenAction ua = new TokenAction();
		ua.targetUser = targetUser;
		ua.token = token;
		ua.type = type;
		final Date created = new Date();
		ua.created = created;
		ua.expires = new Date(created.getTime() + VERIFICATION_TIME * 1000);
		ua.save();
		return ua;
	}
}
