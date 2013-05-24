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

	// Message/Content that the creator of the post entered
	public ObjectId firstMessage;

	public List<ObjectId> repliesIds;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date lastWrote = new Date();

	@Override
	public void save() {
		lastWrote = new Date();
		MorphiaObject.datastore.save(this);
	}


	@Override
	public void delete() {

		Message message = Message.findById(firstMessage, Message.class);
		message.delete();

		for(ObjectId oid : repliesIds){
			message = Message.findById(oid, Message.class);
			message.delete();
		}

		MorphiaObject.datastore.delete(this);
	}


	public Message findMessageById(String id) {

		ObjectId oid = new ObjectId(id);

		if (oid.equals(firstMessage)){
			Message message = MorphiaObject.datastore.get(Message.class, oid);
			if (message != null){
				return message;
			}
			this.delete();
		}

		if (repliesIds.contains(oid)){
			Message message = Message.findById(oid, Message.class);
			if (message != null){
				return message;
			}
			repliesIds.remove(oid);
		}
		return null;
	}


	public Message findMessageById(ObjectId oid) {

		if (oid.equals(firstMessage)){
			Message message = MorphiaObject.datastore.get(Message.class, oid);
			if (message != null){
				return message;
			}
			this.delete();
			return null;
		}

		if (repliesIds != null)
			if (repliesIds.contains(oid)){
				Message message = MorphiaObject.datastore.get(Message.class, oid);
				if (message != null){
					return message;
				}
				repliesIds.remove(oid);
			}
		return null;
	}


	public List<Message> getMessages() {
		List<Message> messages = new ArrayList<Message>();

		Message message = MorphiaObject.datastore.get(Message.class, firstMessage);
		if (message != null){
			messages.add(message);
		} else {
			this.delete();
			return null;
		}

		if (repliesIds != null)
			for(ObjectId oid : repliesIds){
				message = MorphiaObject.datastore.get(Message.class, oid);
				if (message != null){
					messages.add(message);
				} else {
					repliesIds.remove(oid);
				}
			}
		return messages;
	}


	public void addMessage(Message message) {
		if(repliesIds == null){
			repliesIds = new ArrayList<ObjectId>();
		}
		if (!repliesIds.contains(message.id))
			repliesIds.add(message.id);
		this.save();
	}


	public void deleteMessage(Message message) {

		if(repliesIds != null){
			repliesIds.remove(message.id);
			if (repliesIds.isEmpty()){
				repliesIds = null;
			}
		}
		this.save();

		if(message.id.equals(firstMessage)){
			this.delete();
		}
	}

}
