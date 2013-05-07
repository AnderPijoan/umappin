package models;

import com.google.code.morphia.annotations.Id;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SessionToken {

	private static final long serialVersionUID = 1L;

    @Id
    public ObjectId id;
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

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public void delete() {
        MorphiaObject.datastore.delete(this);
    }

    public static <T extends SessionToken>  T findByToken(String token, Class<T> klass) {
        String tokn = null;
        try {
            tokn = URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return MorphiaObject.datastore.find(klass).field("token").equal(tokn).get();
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
