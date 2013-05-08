package models;

import com.feth.play.module.pa.PlayAuthenticate;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Entity;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import com.feth.play.module.pa.user.AuthUser;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;


@Entity
public class SessionToken {

	private static final long serialVersionUID = 1L;

    @Id
    public ObjectId id;
    public ObjectId userId;
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
    
    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
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
        st.userId = (ObjectId) PlayAuthenticate.getUserService().getLocalIdentity(authUsr);
        st.expires = new Date(new Date().getTime() + 3600000);
        st.providerId = authUsr.getProvider();
        st.token = st.createToken();
        st.save();
        return st.token;
    }
    
    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public void delete() {
        MorphiaObject.datastore.delete(this);
    }

    public static SessionToken findByToken(String token) {
        String tokn = null;
        try {
            tokn = URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return MorphiaObject.datastore.find(SessionToken.class).field("token").equal(tokn).get();
    }

    public String createToken() {
        String token = null;
        try {
            token = URLEncoder.encode(BCrypt.gensalt(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return token;
    }
}
