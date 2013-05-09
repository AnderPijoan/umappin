package models;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Entity;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import com.feth.play.module.pa.user.AuthUser;
import java.util.Date;


@Entity
public class SessionToken {

	private static final long serialVersionUID = 1L;

    @Id
    public ObjectId id;
    public String userId;
    public Date expires;
    public String providerId;
    public String token;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
    
    public boolean expired() {
        return new Date().after(expires);
    }

    public static String create(AuthUser authUsr) {
        SessionToken st = new SessionToken();
        st.userId = authUsr.getId();
        st.expires = new Date(new Date().getTime() + 3600000);
        st.providerId = authUsr.getProvider();
        st.token = st.createToken();
        st.save();
        return st.token;
    }

    public static void remove(String token) {
        SessionToken st = findByToken(token);
        if (st != null)
            st.delete();
    }

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public void delete() {
        MorphiaObject.datastore.delete(this);
    }

    public static SessionToken findByToken(String token) {
        return MorphiaObject.datastore.find(SessionToken.class).field("token").equal(token).get();
    }

    public String createToken() {
        return BCrypt.gensalt();
    }
}
