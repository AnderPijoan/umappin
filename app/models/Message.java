package models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
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
@Entity
public class Message {

    @Id
    public String id;

    public String subject;

    public String body;

    public Date timeStamp;

    public static List<Message> all() {
        if (MorphiaObject.datastore != null) {
            return MorphiaObject.datastore.find(Message.class).asList();
        } else {
            return new ArrayList<Message>();
        }
    }


    public String save() {
        timeStamp = new Date();
        MorphiaObject.datastore.save(this);
        return this.id;
    }

    public static Message findById(String id) {
        return MorphiaObject.datastore.get(Message.class, new ObjectId(id));

    }


}
