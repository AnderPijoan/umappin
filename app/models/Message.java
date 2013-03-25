package models;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: a.digangi
 * Date: 23/02/13
 * Time: 22.44
 */
@Embedded
public class Message {

	@Id
	public ObjectId id;

	public String body;

	public String writerId;
	
	@Reference
	public String replyToMsg;

	public Date timeStamp;

	public static List<Message> all() {
		if (MorphiaObject.datastore != null) {
			return MorphiaObject.datastore.find(Message.class).asList();
		} else {
			return new ArrayList<Message>();
		}
	}

	public ObjectId save() {
		timeStamp = new Date();
		MorphiaObject.datastore.save(this);
		return this.id;
	}

	public static Message findById(ObjectId id) {
		return MorphiaObject.datastore.get(Message.class, id);
	}
	
	public static Message findById(String id) {
		return MorphiaObject.datastore.get(Message.class, new ObjectId(id));
	}
}