package models.osm;

import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import play.db.DB;
import play.libs.Json;

public class OsmNode extends OsmFeature {

	private Point2D lonlat; //Latitude and Longitude in Lat/Lon format AND EPSG:4326. 
	// It is translated to an EPSG:90013 geometry in PostGIS

	public Point2D getPoint() {
		return lonlat;
	}

	/* EXPECTED DATA EXAMPLES :
	 * 
		  { "type": "Feature",
		  "id": "314",
		  "user": "Pepe",
		  "uid": "4345314",
		  "timeStamp": "aaa"
	      "geometry": {"type": "Point", "coordinates": [102.0, 0.5]},
	      "properties": {"key0": "value0"}
	      }
	 *
	      <node id="25496583" lat="51.5173639" lon="-0.140043" version="1" changeset="203496" user="80n" uid="1238" visible="true" timeStamp="2007-01-28T11:40:26Z">
	      <tag k="highway" v="traffic_signals"/>
		  </node>
	 *
	 */

	/** OSM JSON Node parser
	 * @param osmXml
	 * @throws ParseException
	 */
	public OsmNode(JsonNode json) throws ParseException{

		id = json.has("id") ? json.findPath("id").getIntValue() : 0;
		version = json.findPath("version").getIntValue();

		user = json.findPath("user").getTextValue();
		uid = json.findPath("uid").getTextValue();
		timeStamp = (json.has("timeStamp") && !json.findPath("timeStamp").isNull())
				? new java.text.SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssZ").parse(json.findPath("timeStamp").getTextValue())
						: new Date();
				this.featurePicture =  json.findPath("featurePicture").getTextValue();

				setGeometry(json.findPath("geometry"));

				JsonNode propertiesNode = json.findPath("properties");
				tags = new LinkedHashMap<String, String>();

				Iterator it = propertiesNode.getFieldNames();
				while (it.hasNext()) {
					String key = (String)it.next();
					tags.put(key, propertiesNode.get(key).getTextValue());
				}
	}

	/** OSM XML Node parser
	 * @param osmXml
	 * @throws ParseException
	 */
	public OsmNode(Node osmXml) throws ParseException{

		Element nodeElement = (Element) osmXml;

		id = Long.parseLong(nodeElement.getAttribute("id"));
		version = Integer.parseInt(nodeElement.getAttribute("version"));
		user = nodeElement.getAttribute("user") == null? "uMappin" : nodeElement.getAttribute("user");
		uid = nodeElement.getAttribute("uid") == null? "uMappin" : nodeElement.getAttribute("uid");
		lonlat = new Point2D.Double(
				Double.parseDouble(nodeElement.getAttribute("lon")),
				Double.parseDouble(nodeElement.getAttribute("lat")));

		System.out.println("FIX timeStamp : " + nodeElement.getAttribute("timeStamp"));
		timeStamp = new java.sql.Date(0);

		//timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").parse("2010-01-02T10:04:33Z");
		//timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").parse(nodeElement.getAttribute("timeStamp"));

		setTags(nodeElement.getElementsByTagName("tag"));
	}

	public OsmNode (long id, int version, String user, String uid, double lat, double lon, Date timeStamp, String featurePicture, LinkedHashMap<String,String> tags){
		this.id = id;
		this.version = version;
		this.user = user;
		this.uid = uid;
		this.lonlat = new Point2D.Double(lon, lat);
		this.timeStamp = timeStamp;
		this.featurePicture = featurePicture;
		this.tags = tags;
	}

	public static OsmNode findById(long id){
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;
		OsmNode node = null;
		try {
			conn = ds.getConnection();
			String sql = "select id, vers, usr, uid, timest, featurepicture, tags, st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry from osmnodes where id = ?";
			st = conn.prepareStatement(sql);
			st.setLong(1, id);
			rs = st.executeQuery();
			while (rs.next()) {
				node = new OsmNode(rs.getLong("id"),
						rs.getInt("vers"),
						rs.getString("usr"),
						rs.getString("uid"),
						0,
						0,
						rs.getDate("timest"),
						rs.getString("featurepicture"),
						hstoreFormatToTags(rs.getString("tags")));
				node.setGeometry(Json.parse(rs.getString("geometry")));
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
		return node;
	}


	public static OsmNode findByGeom(JsonNode geometry){
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;
		OsmNode node = null;
		try {
			conn = ds.getConnection();
			String sql = "select id, vers, usr, uid, timest, featurepicture, tags, st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry " +
					"from osmnodes where geom = ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + ")";
			st = conn.prepareStatement(sql);
			st.setString(1, Json.stringify(geometry));
			rs = st.executeQuery();
			while (rs.next()) {
				node = new OsmNode(rs.getLong("id"),
						rs.getInt("vers"),
						rs.getString("usr"),
						rs.getString("uid"),
						0,
						0,
						rs.getDate("timest"),
						rs.getString("featurepicture"),
						hstoreFormatToTags(rs.getString("tags")));
				node.setGeometry(Json.parse(rs.getString("geometry")));
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
		return node;
	}


	public static List<OsmFeature> findByLocation(JsonNode geometry, int limit){

		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		List<OsmFeature> nodes = new ArrayList<OsmFeature>();
		OsmNode node = null;

		try {
			conn = ds.getConnection();
			String sql = "select id, vers, usr, uid, timest, featurepicture, tags, st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry " +
					"from osmnodes ORDER BY geom <-> ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913) LIMIT ?";
			st = conn.prepareStatement(sql);
			st.setString(1, Json.stringify(geometry));
			st.setInt(2, limit);
			rs = st.executeQuery();
			while (rs.next()) {
				node = new OsmNode(rs.getLong("id"),
						rs.getInt("vers"),
						rs.getString("usr"),
						rs.getString("uid"),
						0,
						0,
						rs.getDate("timest"),
						rs.getString("featurepicture"),
						OsmFeature.hstoreFormatToTags(rs.getString("tags")));
				node.setGeometry(Json.parse(rs.getString("geometry")));
				nodes.add(node);
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
		return nodes;
	}


	public static List<OsmFeature> findByIntersection(JsonNode geometry,int limit){

		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		List<OsmFeature> nodes = new ArrayList<OsmFeature>();
		OsmNode node = null;

		try {
			conn = ds.getConnection();
			String sql = "select id, vers, usr, uid, timest, featurepicture, tags, st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry " +
					"from osmnodes where ST_Intersects(geom , ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913)) limit "+ limit;
			st = conn.prepareStatement(sql);
			st.setString(1, Json.stringify(geometry));
			rs = st.executeQuery();
			while (rs.next()) {
				node = new OsmNode(rs.getLong("id"),
						rs.getInt("vers"),
						rs.getString("usr"),
						rs.getString("uid"),
						0,
						0,
						rs.getDate("timest"),
						rs.getString("featurepicture"),
						OsmFeature.hstoreFormatToTags(rs.getString("tags")));
				node.setGeometry(Json.parse(rs.getString("geometry")));
				nodes.add(node);
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
		return nodes;
	}


	/** Update NodeOSM
	 */
	public OsmNode save(){

		if (this.ds == null){
			this.ds = DB.getDataSource();
		}

		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		boolean reject = false;

		try {

			////////////////////////////////////////////////////////////////////////////////////
			// OSM IDS START FROM 1 AND ON. 
			// WHEN UPLOADING DATA TO OSM, THE NEW ELEMENTS HAVE NEGATIVE NODES.
			// IF WE CREATE A NEW ELEMENT THAT DOESNT EXIST IN OSM, WE WILL STORE IT WITH NEGATIVE ID
			// IN THE DATABASE.
			// IF AN EDITOR SEND US A NEW ELEMENT THAT HAS CREATED, THE ID HAS TO BE 0 TO DIFFER IT
			// FROM OSM EXISTING DATA (POSITIVE IDS) AND DATA WE HAVE CREATED (NEGATIVE IDS)
			////////////////////////////////////////////////////////////////////////////////////


			conn = ds.getConnection();
			String sql;

			// If it is a new node
			if (this.id == 0) {

				boolean sameLocation = false;
				
				// Check if location already exists
				sql = "select id, tags from osmnodes where ST_Intersects(ST_Buffer(geom, 10), ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + "))";
				st = conn.prepareStatement(sql);
				st.setString(1, Json.stringify(this.getGeometry()));
				rs = st.executeQuery();

				// If some node with the same Location exists, merge out tags
				while (rs.next()) {
					
					sameLocation = true;
					this.id = rs.getLong("id");

					HashMap<String, String> othertags = hstoreFormatToTags(rs.getString("tags"));
					if (othertags != null && othertags.size() >= 0) {
						if (this.tags == null)
							this.tags = new LinkedHashMap<>();
							this.tags.putAll(othertags);
					}
					
					sql = "update osmnodes set vers = ?, usr = ?, uid = ?, timest = ?, featurepicture = ?," +
							"geom = ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + ")" +
							((tags != null && tags.size() > 0) ? ", tags = " + tagsToHstoreFormat(tags) : "" ) +
							" where id = ?";

					st = conn.prepareStatement(sql);
					st.setInt(1, this.version);
					st.setString(2, this.user);
					st.setString(3, this.uid);
					st.setDate(4, new java.sql.Date(timeStamp.getTime()));
					st.setString(5, this.featurePicture);
					st.setString(6, Json.stringify(this.getGeometry()));
					st.setLong(7, this.id);
					st.executeUpdate();
				}

				// If a node with the same location doesnt exist, create the new node
				if (!sameLocation){
					sql = "insert into osmnodes (vers, usr, uid, timest, geom " +
							((tags != null && tags.size() > 0) ? ",tags" : "" ) + ") " +
							"values (?, ?, ?, ?, ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + ") " +
							((tags != null && tags.size() > 0) ? ", " + tagsToHstoreFormat(tags) : "" ) + ") returning id";

					st = conn.prepareStatement(sql);
					st.setInt(1, this.version);
					st.setString(2, this.user);
					st.setString(3, this.uid);
					st.setDate(4, new java.sql.Date(timeStamp.getTime()));
					st.setString(5, Json.stringify(this.getGeometry()));
					rs = st.executeQuery();
					while (rs.next()) {
						this.id = rs.getLong("id");
					}
				}

				// If its an already existing node
			} else {

				// Check if already exists
				sql = "select id, vers, tags from osmnodes where id = ? OR ST_Intersects(ST_Buffer(geom, 10), ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + "))";
				st = conn.prepareStatement(sql);
				st.setLong(1, this.id);
				st.setString(2, Json.stringify(this.getGeometry()));
				rs = st.executeQuery();
				boolean exists = false;
				
				// A node with the same ID or Location exists, check possible cases
				while (rs.next()){
					// Node id already exists
					if(this.id != 0 && rs.getLong("id") == this.id) {
						// If our Node has same or lower version than the one in DB, reject it
						exists = true;
						reject = rs.getInt("vers") >= this.version;
						break;
					} else { 
						// Location is used by another node
						// Get the nodes tags and merge them with ours
						// This should not happen
						HashMap<String, String> othertags = hstoreFormatToTags(rs.getString("tags"));
						if (othertags != null && othertags.size() >= 0) {
							if (this.tags == null)
								this.tags = new LinkedHashMap<>();
								this.tags.putAll(othertags);
						}
						
						// Remove the other node
						sql = "delete from osmnodes where id = ?";
						st = conn.prepareStatement(sql);
						st.setLong(1, rs.getLong("id"));
						st.executeUpdate();
					}
				}
				
				// If theres no collition and the node won't be rejected
				if (exists && !reject) {
					// Try updating, if the node doesnt exists, the query does nothing
					sql = "update osmnodes set vers = ?, usr = ?, uid = ?, timest = ?, featurepicture = ?," +
							"geom = ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " +
							TOLERANCE + ")" +
							((tags != null && tags.size() > 0) ? ", tags = " + tagsToHstoreFormat(tags) : "" ) +
							" where id = ?";

					st = conn.prepareStatement(sql);
					st.setInt(1, this.version);
					st.setString(2, this.user);
					st.setString(3, this.uid);
					st.setDate(4, new java.sql.Date(timeStamp.getTime()));
					st.setString(5, this.featurePicture);
					st.setString(6, Json.stringify(this.getGeometry()));
					st.setLong(7, this.id);
					st.executeUpdate();
				}

				if (!exists) {
					//Ok, it dos not exist but it has already an id
					sql = "insert into osmnodes (id, vers, usr, uid, timest, geom " +
							((tags != null && tags.size() > 0) ? ",tags" : "" ) + ") " +
							"values (?, ?, ?, ?, ?, ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + ") " +
							((tags != null && tags.size() > 0) ? ", " + tagsToHstoreFormat(tags) : "" ) + ")";

					st = conn.prepareStatement(sql);
					st.setLong(1, this.id);
					st.setInt(2, this.version);
					st.setString(3, this.user);
					st.setString(4, this.uid);
					st.setDate(5, new java.sql.Date(timeStamp.getTime()));
					st.setString(6, Json.stringify(this.getGeometry()));
					st.executeUpdate();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		if (reject){
			return null;
		}
		return this;
	}

	/** Delete NodeOSM
	 */
	public void delete(){

		if(id == 0)
			return;

		if (this.ds == null){
			this.ds = DB.getDataSource();
		}

		Connection conn = null;
		PreparedStatement st;
		try {
			conn = ds.getConnection();
			String sql = "delete from osmnodes where id = ?";
			st = conn.prepareStatement(sql);
			st.setLong(1, this.id);
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


	public double getLat() {
		return lonlat.getY();
	}

	public double getLon(){
		return lonlat.getX();
	}


	public JsonNode getGeometry(){

		/* EXAMPLE :
		 * {"type":"Point","coordinates":[-48.23456,20.12345]}
		 */

		ObjectNode geomNode = Json.newObject();
		geomNode.put("type", "Point");
		Double[] lonlat = {this.lonlat.getX(), this.lonlat.getY()};
		geomNode.put("coordinates", Json.toJson(lonlat));

		return geomNode;
	}


	public void setGeometry(JsonNode geometry) {

		/* EXAMPLE :
		 * {"type": "Point", "coordinates": [102.0, 0.5]},
		 */

		JsonNode coordinatesNode = geometry.findPath("coordinates");

		if (geometry.findPath("type").asText().toUpperCase().equals("POINT") && coordinatesNode.size() == 2){

			// first position in array is lon and second lat
			this.lonlat = new Point2D.Double(
					coordinatesNode.get(0).asDouble(),
					coordinatesNode.get(1).asDouble());
		}
	}


	// Export functions
	public static ObjectNode toObjectNode(OsmNode node){

		ObjectNode osmNodeNode = Json.newObject();
		osmNodeNode.put("type", "Feature");
		osmNodeNode.put("id", node.id);
		osmNodeNode.put("version", node.version);
		osmNodeNode.put("user", node.user);
		osmNodeNode.put("uid", node.uid);
		osmNodeNode.put("timeStamp", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(node.timeStamp));
		osmNodeNode.put("geometry", node.getGeometry());
		osmNodeNode.put("properties", Json.toJson(node.tags));
		osmNodeNode.put("featurePicture", node.featurePicture);

		return osmNodeNode;
	}


	public ObjectNode toOsmJson(){
		return toObjectNode(this);
	}


	public Document toOsmXml() throws ParserConfigurationException{

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		Element osmElement = document.createElement("osm");

		Element osmNodeElement = document.createElement("node");

		osmNodeElement.setAttribute("id", id+"");
		osmNodeElement.setAttribute("version", version+"");
		osmNodeElement.setAttribute("lat", lonlat.getY()+"");
		osmNodeElement.setAttribute("lon", lonlat.getX()+"");
		osmNodeElement.setAttribute("user", user);
		osmNodeElement.setAttribute("uid", uid);
		osmNodeElement.setAttribute("timeStamp", new java.text.SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ssZ").format(timeStamp));

		osmElement.appendChild(osmNodeElement);

		for (String key : tags.keySet()){
			Element osmTagElement = document.createElement("tag");
			osmTagElement.setAttribute("k", key);
			osmTagElement.setAttribute("v", tags.get(key));
			osmNodeElement.appendChild(osmTagElement);
		}

		document.appendChild(osmElement);
		return document;
	}


	/** Returns the first free negative id nearest to 0
	 * @return long id
	 */
	public static Long getFirstFreeId(){

		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		long id = 0;

		try {
			conn = ds.getConnection();

			// Returns first negative available ID, NEEDS ID=0 DUMMY NODE INSERTED
			String sql = "SELECT (t1.id - 1) as result FROM osmnodes AS t1 LEFT JOIN osmnodes as t2 ON t1.id - 1 = t2.id WHERE t2.id IS NULL AND (t1.id <= 0) order by t1.id desc limit 1";
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();

			// We found a nodeId to give
			while (rs.next()){
				id = rs.getLong("result");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && this.lonlat.equals(((OsmNode)obj).getPoint()); //TODO Maybe an OR instead ???
	}

}
