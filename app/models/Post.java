package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import play.data.format.Formats;


import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;


@Entity
public abstract class Post extends Item {

	public String subject;
	
	public List<ObjectId> messageIds;
	
	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date lastWrote = new Date();
    
	@Override
	public void save() {
		lastWrote = new Date();
		MorphiaObject.datastore.save(this);
	}

	
	@Override
	public void delete() {

		for(ObjectId oid : messageIds){
			Message message = Message.findById(oid, Message.class);
			message.delete();
		}

		MorphiaObject.datastore.delete(this);
	}
	
    
	public Message findMessageById(String id) {
		if (messageIds.contains(new ObjectId(id))){
			Message message = MorphiaObject.datastore.get(Message.class, new ObjectId(id));
			if (message != null){
				return message;
			}
			messageIds.remove(new ObjectId(id));
		}
		return null;
	}
	
	
	public Message findMessageById(ObjectId oid) {
		if (messageIds.contains(oid)){
			Message message = MorphiaObject.datastore.get(Message.class, oid);
			if (message != null){
				return message;
			}
			messageIds.remove(oid);
		}
		return null;
	}
	
	
	public List<Message> getMessages() {
		List<Message> messages = new ArrayList<Message>();
		for(ObjectId oid : messageIds){
			Message message = MorphiaObject.datastore.get(Message.class, oid);
			if (message != null){
				messages.add(message);
			} else {
			messageIds.remove(oid);
			}
		}
		return messages;
	}
	
	
	public void addMessage(Message message) {
		if(messageIds == null){
			messageIds = new ArrayList<ObjectId>();
		}
		if (!messageIds.contains(message.id))
			messageIds.add(message.id);
		this.save();
	}
	
	
	public void deleteMessage(Message message) {
		if(messageIds != null){
			messageIds.remove(message.id);
			if (messageIds.isEmpty()){
				messageIds = null;
			}
		}
		this.save();
	}
	
}
