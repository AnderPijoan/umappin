package models;

import com.google.code.morphia.annotations.Entity;
import controllers.MorphiaObject;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Follows extends Follow {

    // Search methods
    public static List<Follows> all() {
        if (MorphiaObject.datastore != null) {
            List<Follows> res = MorphiaObject.datastore.find(Follows.class).asList();
            if (res != null)
                return res;
        }
        return new ArrayList<Follows>();
    }

    public static Follow findById(String id) {
        return MorphiaObject.datastore.find(Follows.class).field("_id").equal(id).get();
    }

    public static Follow findByUserId(String userId) {
        return MorphiaObject.datastore.find(Follows.class).field("userId").equal(userId).get();
    }

    // Delete method
    public static Follow delete(String userId) {
        Follow follow = findByUserId(userId);
        if (follow != null)
            return follow.delete();
        return null;
    }

    // Update method
    public static Follow update(String userId, List<String> followList) {
        Follow follow = findByUserId(userId);
        if (follow != null)
            return follow.update(followList);
        return null;
    }
}
