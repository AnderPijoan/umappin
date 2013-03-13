package models;

//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.ManyToOne;

//import play.db.ebean.Model;

import com.feth.play.module.pa.user.AuthUser;

import controllers.MorphiaObject;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import providers.MyUsernamePasswordAuthUser;

@Entity
public class LinkedAccount {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public String id;

	//@ManyToOne
	public String userId;

	public String providerUserId;
	public String providerKey;

	//public static final Finder<Long, LinkedAccount> find = new Finder<Long, LinkedAccount>(Long.class, LinkedAccount.class);


    public void save() {
        MorphiaObject.datastore.save(this);
    }

	public static LinkedAccount findByProviderKey(final User user, String key) {
        return MorphiaObject.datastore.find(LinkedAccount.class)
                .field("userId").equal(user.id)
                .field("providerKey").equal(key).get();
		//return find.where().eq("user", user).eq("providerKey", key).findUnique();
	}

	public static LinkedAccount create(final AuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.update(authUser);
        ret.save();
		return ret;
	}
	
	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

    public void setUserId(String userId) {
        this.userId = userId;
    }

	public static LinkedAccount create(final LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;
        ret.save();
		return ret;
	}
}