package models;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sql.DataSource;

import models.osm.OsmFeature;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import play.db.DB;
import play.libs.Json;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.NotSaved;

import controllers.MorphiaObject;

@Entity
public class Route extends Item {

	public String name;
	
	public int difficulty;
	
	public ObjectId userId;
	
	@NotSaved
	public LinkedHashMap<String,String> tags; // This one goes to PostGIS, not to Mongo
	
	@NotSaved
	public JsonNode geometry; // This one goes to PostGIS, not to Mongo
	
	
	public static List<Route> findByLocation(JsonNode geometryPoint, int amount, int difficulty){
		
		/* EXAMPLE :
		 * {"type":"Point","coordinates":[-48.23456,20.12345]}
		 */
		
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;
		
		List<Route> routes = new ArrayList<Route>();
		
		try {
			conn = ds.getConnection();
			String sql = "SELECT mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry, tags " +
					"from routes " + 
					(difficulty < 0? "" : " WHERE difficulty = " + difficulty) 
					+ " ORDER BY geom <-> ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913) LIMIT ?";
			st = conn.prepareStatement(sql);
			st.setString(1, Json.stringify(geometryPoint));
			st.setInt(2, amount);
			rs = st.executeQuery();
			while (rs.next()) {
				
				Route route = Route.findById(rs.getString("mongo_oid"));
				
				if (route != null && !routes.contains(route)){
					route.setGeometry(Json.parse(rs.getString("geometry")));
                    String tags = rs.getString("tags");
                    if (tags != null && !tags.equals(""))
                        route.setTags(OsmFeature.hstoreFormatToTags(tags));
					routes.add(route);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return routes;
	}
	
	
	public static Route findById(String id) {
		Route route = MorphiaObject.datastore.get(Route.class, new ObjectId(id));
		if (route == null){
			return null;
		} else {
			
			DataSource ds = DB.getDataSource();
			Connection conn = null;
			PreparedStatement st;
			ResultSet rs;
			
			try {
				conn = ds.getConnection();
				String sql = "SELECT st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry, tags from routes WHERE mongo_oid = ?";
				st = conn.prepareStatement(sql);
				st.setString(1, id);
				rs = st.executeQuery();
				
				while (rs.next()) {
					route.setGeometry(Json.parse(rs.getString("geometry")));
                    String tags = rs.getString("tags");
                    if (tags != null && !tags.equals(""))
                        route.setTags(OsmFeature.hstoreFormatToTags(tags));
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return route;
		}
	}
	
	
	public static Route findById(ObjectId oid) {
		Route route = MorphiaObject.datastore.get(Route.class, oid);
		if (route == null){
			return null;
		} else {
			
			DataSource ds = DB.getDataSource();
			Connection conn = null;
			PreparedStatement st;
			ResultSet rs;
			
			try {
				conn = ds.getConnection();
				String sql = "SELECT st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry, tags from routes WHERE mongo_oid = ?";
				st = conn.prepareStatement(sql);
				st.setString(1, oid.toString());
				rs = st.executeQuery();
				while (rs.next()) {
					
					route.setGeometry(Json.parse(rs.getString("geometry")));
                    String tags = rs.getString("tags");
                    if (tags != null && !tags.equals(""))
					    route.setTags(OsmFeature.hstoreFormatToTags(tags));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return route;
		}
	}
	
	
	@Override
	public void save() {
		if (this.id == null)
            MorphiaObject.datastore.save(this);
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		
		try {
			conn = ds.getConnection();
			// Try updating, if the route doesnt exists, the query does nothing
			
			String sql = "update routes set mongo_oid = ?, difficulty = ?, timest = ?, " +
					"geom = ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913)" + 
					((tags != null && tags.size() > 0) ? ", tags = " + OsmFeature.tagsToHstoreFormat(tags) : "" ) +
					" where mongo_oid = ?";
			st = conn.prepareStatement(sql);

            st.setString(1, this.id.toString());
			st.setInt(2, this.difficulty);
            st.setDate(3, new java.sql.Date(this.id.getTime()));
			st.setString(4, Json.stringify(this.getGeometry()));
			st.setString(5, this.id.toString());
			st.executeUpdate();
			
			// Try inserting, if the node exists, the query does nothing
			
			sql = "insert into routes (mongo_oid, difficulty, timest, geom " + 
					((tags != null && tags.size() > 0) ? ",tags" : "" ) + ") " +
					"select ?, ?, ?, ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913) " +
					((tags != null && tags.size() > 0) ? ", " + OsmFeature.tagsToHstoreFormat(tags) : "" ) + " " +
					"where not exists (select 1 from routes where mongo_oid = ?)";
			st = conn.prepareStatement(sql);
			st.setString(1, this.id.toString());
			st.setInt(2, this.difficulty);
			st.setDate(3, new java.sql.Date(this.id.getTime()));
			st.setString(4, Json.stringify(this.getGeometry()));
			st.setString(5, this.id.toString());
			st.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			MorphiaObject.datastore.save(this);
		}
	}
	
	
	@Override
	public void delete() {
		
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		
		try {
			conn = ds.getConnection();
			
			String sql = "delete from routes where mongo_oid = ?";
			st = conn.prepareStatement(sql);
			st.setString(1, this.id.toString());
			st.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			MorphiaObject.datastore.delete(this);
		}
	}
	
	
	/* To ensure sometimes consistency
	 */
	public static void removeFromGis(ObjectId oid){
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		
		try {
			conn = ds.getConnection();
			
			String sql = "delefe from routes where mongo_oid = ?";
			st = conn.prepareStatement(sql);
			st.setString(1, oid.toString());
			st.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void setGeometry(JsonNode geometry){
		this.geometry = geometry;
	}
	
	
	public JsonNode getGeometry(){
		return this.geometry;
	}
	
	public void setTags(LinkedHashMap<String,String> tags){
		this.tags = tags;
	}
	
	
	public LinkedHashMap<String,String> getTags(){
		return tags;
	}


	public static List<ObjectNode> routesToObjectNodes(List<Route> rts) {
		
		List<ObjectNode> routes = new ArrayList<ObjectNode>();
		for(Route route : rts){
			routes.add(Route.routeToFullObjectNode(route));
		}
		return routes;
	}


	public static ObjectNode routeToFullObjectNode(Route route) {
		
		ObjectNode routeNode = Json.newObject();
		routeNode.put("id", route.id.toString());
		routeNode.put("name", route.name);
        routeNode.put("difficulty", route.difficulty);
		routeNode.put("user", User.userToShortObjectNode(route.userId));
		routeNode.put("timeStamp", route.id.getTime());
		routeNode.put("geometry", route.geometry);
		routeNode.put("properties", Json.toJson(route.tags));
		return routeNode;
	}
	
}
