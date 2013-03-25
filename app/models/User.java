package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;

import models.TokenAction.Type;

import org.bson.types.ObjectId;
import play.data.format.Formats;

//import javax.persistence.*;

import java.util.*;
import play.Logger;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import com.google.code.morphia.query.*;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */

@Entity
public class User implements Subject {

	private static final long serialVersionUID = 1L;

	@Id
	public String id;

	public String email;

	public String name;
	
	public String firstName;
	
	public String lastName;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date lastLogin;

	public boolean active;

	public boolean emailValidated;

    public void save() {
        MorphiaObject.datastore.save(this);
    }
	
	public void delete() {
        MorphiaObject.datastore.delete(this);
    }
	
    public static List<User> all() {
        if (MorphiaObject.datastore != null) {
            return MorphiaObject.datastore.find(User.class).asList();
        } else {
            return new ArrayList<User>();
        }
    }

    public static User findByName(String name) {
        return MorphiaObject.datastore.find(User.class).field("name").equal(name).get();
    }

    public static void delete(String idToDelete) {
        User toDelete = MorphiaObject.datastore.find(User.class).field("_id").equal(idToDelete).get();
        if (toDelete != null) {
            Logger.info("toDelete: " + toDelete);
            MorphiaObject.datastore.delete(toDelete);
        } else {
            Logger.debug("ID No Found: " + idToDelete);
        }
    }

	//@ManyToMany
	public List<SecurityRole> roles;

	//@OneToMany(cascade = CascadeType.ALL)
	public List<LinkedAccount> linkedAccounts;

	@Override
	public String getIdentifier() {
		return id;
	}

	@Override
	public List<? extends Role> getRoles() {
		return roles;
	}

    @Override
    public List<? extends Permission> getPermissions() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
        return findByAuthUserIdentity(identity) != null;
	}

    public static User findById(String id){
    	return MorphiaObject.datastore.get(User.class, new ObjectId(id));
    }
    
    public static User findById(ObjectId id){
    	return MorphiaObject.datastore.get(User.class, id);
    }
    
	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		if (identity instanceof UsernamePasswordAuthUser) {
			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
		} else {
            return MorphiaObject.datastore.find(User.class)
                    .field("linkedAccounts.providerUserId").equal(identity.getId())
                    .field("active").equal(true)
                    .field("linkedAccounts.providerKey").equal(identity.getProvider()).get();
		}
	}

	public static User findByUsernamePasswordIdentity(final UsernamePasswordAuthUser identity) {
        return MorphiaObject.datastore.find(User.class)
                .field("email").equal(identity.getEmail())
                .field("active").equal(true)
                .field("linkedAccounts.providerKey").equal(identity.getProvider()).get();
	}

	public void merge(final User otherUser) {
		for (final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other merging stuff here - like resources, etc.
		// deactivate the merged user that got added to this one
		otherUser.active = false;
        MorphiaObject.datastore.save(Arrays.asList(new User[]{otherUser, this}));
	}

	public static User create(final AuthUser authUser) {
		final User user = new User();
		user.roles = Collections.singletonList(
                SecurityRole.findByRoleName(controllers.Application.USER_ROLE));
		user.active = true;
		user.lastLogin = new Date();
        LinkedAccount la = LinkedAccount.create(authUser);
		user.linkedAccounts = Collections.singletonList(la);

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be verified within t
			// he application as a security breach there might break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = false;
		}

		if (authUser instanceof NameIdentity) {
			final NameIdentity identity = (NameIdentity) authUser;
			final String name = identity.getName();
			if (name != null)
				user.name = name;
		}
		
		if (authUser instanceof FirstLastNameIdentity) {
		  final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
		  final String firstName = identity.getFirstName();
		  final String lastName = identity.getLastName();
		  if (firstName != null) {
		    user.firstName = firstName;
		  }
		  if (lastName != null) {
		    user.lastName = lastName;
		  }
		}

		// Fix - Manually create an ObjectID and get its String UUID
        user.id = new ObjectId().toString();
        user.save();
		
        // Fix - adding the User to the LinkedAccount
        la.setUserId(user.id);
        la.save();

		return user;
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentity(oldUser).merge(
				User.findByAuthUserIdentity(newUser));
	}

	public Set<String> getProviders() {
		final Set<String> providerKeys = new HashSet<String>(
				linkedAccounts.size());
		for (final LinkedAccount acc : linkedAccounts) {
			providerKeys.add(acc.providerKey);
		}
		return providerKeys;
	}

	public static void addLinkedAccount(final AuthUser oldUser,
			final AuthUser newUser) {
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}

	public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = new Date();
		u.save();
	}

	public static User findByEmail(final String email) {
        return MorphiaObject.datastore.find(User.class).field("email").equal(email)
                                                        .field("active").equal(true).get();
	}

	public LinkedAccount getAccountByProvider(final String providerKey) {
		return LinkedAccount.findByProviderKey(this, providerKey);
	}

	public static void verify(final User unverified) {
		// You might want to wrap this into a transaction
		unverified.emailValidated = true;
		unverified.save();
		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
	}

	public void changePassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		LinkedAccount a = this.getAccountByProvider(authUser.getProvider());
		if (a == null) {
			if (create) {
				a = LinkedAccount.create(authUser);
				a.setUserId(this.id);
			} else {
				throw new RuntimeException(
						"Account not enabled for password usage");
			}
		}
		a.providerUserId = authUser.getHashedPassword();
		a.save();
        for (final LinkedAccount acc : this.linkedAccounts)
            if (UsernamePasswordAuthProvider.PROVIDER_KEY.equals(acc.providerKey))
                acc.providerUserId = authUser.getHashedPassword();
        this.save();
	}

	public void resetPassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
	}
	
}
