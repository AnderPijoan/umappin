package models;

import java.util.Date;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.ManyToOne;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import play.data.format.Formats;
//import play.db.ebean.Model;

//import com.avaje.ebean.Ebean;
//import com.avaje.ebean.annotation.EnumValue;

@Entity
public class TokenAction {

	public enum Type {
		//@EnumValue("EV")
		EMAIL_VERIFICATION,

		//@EnumValue("PR")
		PASSWORD_RESET
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static long VERIFICATION_TIME = 7 * 24 * 3600;

	@Id
	public ObjectId id;

	//@Column(unique = true)
	public String token;

	//@ManyToOne
	public User targetUser;

	public Type type;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date created;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date expires;

	//public static final Finder<Long, TokenAction> find = new Finder<Long, TokenAction>(Long.class, TokenAction.class);

    public void save() {
        MorphiaObject.datastore.save(this);
    }

	public static TokenAction findByToken(final String token, final Type type) {
        return MorphiaObject.datastore.find(TokenAction.class)
                .field("token").equal(token)
                .field("type").equal(type).get();
		//return find.where().eq("token", token).eq("type", type).findUnique();
	}

	public static void deleteByUser(final User u, final Type type) {
        MorphiaObject.datastore.delete(
                MorphiaObject.datastore.find(TokenAction.class)
                    .field("targetUser.id").equal(u.id)
                    .field("type").equal(type)
        );

		//Ebean.delete(find.where().eq("targetUser.id", u.id).eq("type", type).findIterate());
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
