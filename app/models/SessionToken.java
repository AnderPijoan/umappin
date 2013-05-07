package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import java.util.Date;

@Entity
public class SessionToken {

	private static final long serialVersionUID = 1L;

    @Id
    public ObjectId id;
    public ObjectId userId;
    public String token;
    public Date expires;

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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


    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public void delete() {
        MorphiaObject.datastore.delete(this);
    }

    public static SessionToken findByToken(String token) {
        return MorphiaObject.datastore.find(SessionToken.class).field("token").equal(token).get();
    }

    public static void create(ObjectId usrId, String token) {
        SessionToken st = new SessionToken();
        st.userId = usrId;
        st.expires = new Date(new Date().getTime() + 86400);
        st.token = token;
        st.save();
    }
}
