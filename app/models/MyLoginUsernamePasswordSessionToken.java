package models;

import com.google.code.morphia.annotations.Entity;
import providers.MyLoginUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthUser;

import java.util.Date;

@Entity
public class MyLoginUsernamePasswordSessionToken extends SessionToken {

	private static final long serialVersionUID = 1L;

    public MyLoginUsernamePasswordAuthUser authUsr;

    public MyLoginUsernamePasswordAuthUser getAuthUsr() {
        return authUsr;
    }

    public void setAuthUsr(MyLoginUsernamePasswordAuthUser authUsr) {
        this.authUsr = authUsr;
    }

    public boolean expired() {
        return new Date().after(new Date(authUsr.expires()));
    }

    public static String create(MyLoginUsernamePasswordAuthUser authUsr) {
        MyLoginUsernamePasswordSessionToken st = new MyLoginUsernamePasswordSessionToken();
        st.authUsr = authUsr;
        st.token = st.createToken();
        st.save();
        return st.token;
    }
}
