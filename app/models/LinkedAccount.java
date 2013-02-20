package models;

//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.ManyToOne;

//import play.db.ebean.Model;

import com.feth.play.module.pa.user.AuthUser;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity
public class LinkedAccount {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public ObjectId id;

	//@ManyToOne
	public User user;

	public String providerUserId;
	public String providerKey;

	//public static final Finder<Long, LinkedAccount> find = new Finder<Long, LinkedAccount>(Long.class, LinkedAccount.class);


    public void save() {
        MorphiaObject.datastore.save(this);
    }

	public static LinkedAccount findByProviderKey(final User user, String key) {
        return MorphiaObject.datastore.find(LinkedAccount.class)
                .field("user").equal(user)
                .field("providerKey").equal(key).get();
		//return find.where().eq("user", user).eq("providerKey", key).findUnique();
	}

	public static LinkedAccount create(final AuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.update(authUser);
		return ret;
	}
	
	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

	public static LinkedAccount create(final LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;

		return ret;
	}
}