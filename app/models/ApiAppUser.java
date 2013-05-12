package models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import play.data.format.Formats;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.Date;


import providers.MyUsernamePasswordAuthUser;
import providers.MyLoginUsernamePasswordAuthUser;
import providers.MyUsernamePasswordAuthProvider;

import controllers.MorphiaObject;



@Entity
public class ApiAppUser extends Item{

	/** ------------------------ Attributes ------------------------- **/

	@Id
	public ObjectId id;
	
	public String sessionToken;
	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date expire;


	
	public  ApiAppUser(ObjectId id){
		this.id =id;
		SecureRandom random = new SecureRandom();

		this.sessionToken =  new BigInteger(130, random).toString(32);
		updateExpireTime();
	}
	public ApiAppUser (){
	}

	public void updateExpireTime(){
		long hoursInMillis = 60L * 60L * 1000L;
		expire = new Date((new Date()).getTime() + 
                        (1L * hoursInMillis)); // Expire in 1 hour
		this.save();

	}

	public boolean logged(){
		if (new Date().getTime() <= this.expire.getTime()){
			this.updateExpireTime();
			return true;
		}else{
			return false;
		}
	}

	

}