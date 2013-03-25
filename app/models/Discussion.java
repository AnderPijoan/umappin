package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.feth.play.module.pa.user.AuthUser;
import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import controllers.MorphiaObject;

/**
 * User: a.pijoan
 * Date: 16/03/13
 * Time: 12:34
 */
@Entity
public class Discussion {

	@Id
	public ObjectId id;

	public String subject;

	@Embedded
	public List<Message> messages;

	public Date timeStamp;
	
	public static List<Discussion> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Discussion.class).asList();
		} else {
			return new ArrayList<Discussion>();
		}
	}
	
	public ObjectId save() {
		timeStamp = new Date();
		MorphiaObject.datastore.save(this);
		return this.id;
	}
	
	public static Discussion findById(String id) {
		Discussion discussion = MorphiaObject.datastore.get(Discussion.class, new ObjectId(id));
		if (discussion == null){
			return null;
		} else {
			return discussion;
		}
	}
	
	public static Discussion findById(ObjectId id) {
		Discussion discussion = MorphiaObject.datastore.get(Discussion.class, id);
		if (discussion == null){
			return null;
		} else {
			return discussion;
		}
	}
	
	public Message findMessageById(String id) {
		Message message = MorphiaObject.datastore.get(Message.class, new ObjectId(id));
		if (this.messages.contains(message)){
			return message;
		} else {
			return null;
		}
	}
	
	public Message findMessageById(ObjectId id) {
		Message message = MorphiaObject.datastore.get(Message.class, id);
		if (this.messages.contains(message)){
			return message;
		} else {
			return null;
		}
	}
	
	public List<Message> getMessages() {
		return messages;
	}
	
	public void addMessage(Message message) {
		this.messages.add(message);
		this.save();
	}
}
