package models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;

import controllers.MorphiaObject;

@Entity
public class User2Routes extends Item {
	
	///////////////////////////////////////////////////////////////////////////////
	// THE OBJECTID OF USER2DISCUSSION IS THE SAME AS THE USERS, TO GET IT DIRECTLY
	///////////////////////////////////////////////////////////////////////////////
	
	public List<ObjectId> routeIds;
	
	public List<Route> all() {
		if (MorphiaObject.datastore != null) {
			List<Route> result = new ArrayList<Route>();
			Iterator<ObjectId> it = routeIds.iterator();
			while(it.hasNext()){
				ObjectId oid = it.next();
				Route route = Route.findById(oid);
				if (route != null){
					result.add(route);
				} else {
					Route.removeFromGis(oid);
					it.remove();
				}
			}
			return result;
		} else {
			return new ArrayList<Route>();
		}
	}
	
	
	public Route findRouteById(String id) {
		if (routeIds != null && routeIds.contains(new ObjectId(id))){
			Route route = Route.findById(new ObjectId(id));
			if (route != null){
				return route;
			} else {
				Route.removeFromGis(new ObjectId(id));
				routeIds.remove(new ObjectId(id));
			}
		} 
		return null;
	}

	
	public Route findRouteById(ObjectId oid) {
		if (routeIds != null && routeIds.contains(oid)){
			Route route = Route.findById(oid);
			if (route != null){
				return route;
			} else {
				Route.removeFromGis(oid);
				routeIds.remove(oid);
			}
		}
		return null;
	}
	
	
	public void addRoute(Route route){
		if(routeIds == null)
			routeIds = new ArrayList<ObjectId>();
		
		if (routeIds != null && !routeIds.contains(route.id)){
			routeIds.add(route.id);
			this.save();
		}
	}
	
	
	public void removeRoute(Route route){
		if (routeIds != null){
			routeIds.remove(route.id);
			this.save();
		}
	}
	
}
