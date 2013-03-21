package models;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: igarri
 * Date: 09/03/13
 * Time: 11:31
 */

@Entity
public class UserAward {
	@Id
	public ObjectId id;
	
	public String userId;
	
	public ObjectId award;
	
	public Date timeStamp;
	
	public boolean isNew;
	
	public String save() {
		timeStamp = new Date();
		isNew = true;
		MorphiaObject.datastore.save(this);
		return this.userId;
}
	
	public static List<UserAward> findByUserId(String userId) {
		//Incorporar Awards en el JSON.
		return	MorphiaObject.datastore.find(UserAward.class).
				field("userId").equal(userId).order("-timeStamp").asList();
	}
	
	public static List<UserAward> findNewByUserId(String userId) {
		//Incorporar Awards en el JSON.
		return	MorphiaObject.datastore.find(UserAward.class).
				field("userId").equal(userId).
				field("isNew").equal(true).order("-timeStamp").asList();
	}
	
	/*Método actualizar UserAwards con campo isNew del usuario recibido a false.*/
}
