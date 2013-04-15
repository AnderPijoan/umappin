package models;

import com.feth.play.module.pa.user.AuthUser;
import controllers.MorphiaObject;
import com.google.code.morphia.annotations.Entity;

@Entity
public class LinkedAccount extends Item {

	private static final long serialVersionUID = 1L;

    /** ------------------------ Attributes ------------------------- **/

	//@ManyToOne
	public String userId;
	public String providerUserId;
	public String providerKey;

    /** ------------------------ Getters / Setters ------------------------- **/

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** ------------------------ Authentication methods -------------------------- **/

    public static LinkedAccount findByProviderKey(final User user, String key) {
        return MorphiaObject.datastore.find(LinkedAccount.class)
                .field("userId").equal(user.id)
                .field("providerKey").equal(key).get();
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

	public static LinkedAccount create(final LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;
        ret.save();
		return ret;
	}
}