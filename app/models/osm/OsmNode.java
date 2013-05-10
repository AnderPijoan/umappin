package models.osm;

import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.db.DB;
import play.libs.Json;

public class OsmNode extends OsmFeature {

	private Point2D lonlat; //Latitude and Longitude in Lat/Lon format AND EPSG:4326. 
	// It is traslated to an EPSG:90013 geometry in PostGIS

	/* EXPECTED DATA EXAMPLES :
	 * 
		  { "type": "Feature",
		  "id": "314",
		  "user": "Pepe",
		  "uid": "4345314",
		  "timestamp": "aaa"
	      "geometry": {"type": "Point", "coordinates": [102.0, 0.5]},
	      "properties": {"key0": "value0"}
	      }
	 *
	      <node id="25496583" lat="51.5173639" lon="-0.140043" version="1" changeset="203496" user="80n" uid="1238" visible="true" timestamp="2007-01-28T11:40:26Z">
	      <tag k="highway" v="traffic_signals"/>
		  </node>
	 *
	 */

	public OsmNode(JsonNode json) throws ParseException{

		id = json.findPath("id").getIntValue();
		version = json.findPath("version").getIntValue();
		user = json.findPath("user").getTextValue();
		uid = json.findPath("uid").getTextValue();
		timeStamp = new java.text.SimpleDateFormat("yyyy-mm-ddTHH:mm:ss:SSS").parse(json.findPath("timestamp").getTextValue());

		setGeometry(json.findPath("geometry"));

		JsonNode propertiesNode = json.findPath("properties");
		tags = new LinkedHashMap<String, String>();

		for (int x = 0; x < propertiesNode.size(); x++){
			tags.put(propertiesNode.get(x).get(0).asText(), propertiesNode.get(x).get(1).asText());
		}
	}

	public OsmNode(Node osmXml) throws ParseException{

		Element nodeElement = (Element) osmXml;

		id = Long.parseLong(nodeElement.getAttribute("id"));
		version = Integer.parseInt(nodeElement.getAttribute("version"));
		user = nodeElement.getAttribute("user");
		uid = nodeElement.getAttribute("uid");
		lonlat = new Point2D.Double(
				Double.parseDouble(nodeElement.getAttribute("lon")),
				Double.parseDouble(nodeElement.getAttribute("lat")));
		timeStamp = new java.text.SimpleDateFormat("yyyy-mm-ddTHH:mm:ss:SSS").parse(nodeElement.getAttribute("timestamp"));

		NodeList tagList = nodeElement.getElementsByTagName("tag");
		tags = new LinkedHashMap<String, String>();

		for (int y = 0; y < tagList.getLength(); y++){
			Node tag = tagList.item(y);

			if (tag.getNodeType() == Node.ELEMENT_NODE)
			{
				Element tagElement = (Element) tag;
				tags.put(tagElement.getAttribute("k"), tagElement.getAttribute("v"));
			}
		}
	}

	private OsmNode (long id, int version, String user, String uid, double lat, double lon, Date timestamp, LinkedHashMap<String,String> tags){
		this.id = id;
		this.version = version;
		this.user = user;
		this.uid = uid;
		this.lonlat = new Point2D.Double(lon, lat);
		this.timeStamp = timestamp;
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
			String sql = "select id, vers, usr, uid, timest, tags, inuse, st_asgeojson(geom) as geometry from osmnodes";
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				node = new OsmNode(rs.getLong("id"),
						rs.getInt("version"),
						rs.getString("user"),
						rs.getString("uid"),
						0,
						0,
						rs.getDate("timestamp"),
						new LinkedHashMap<String,String>());
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

	public OsmNode save(){

		if(id == 0)
			return null;

		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;
		try {

			conn = ds.getConnection();

			// Check if already exists
			String sql = "select id from osmnodes where geom = st_geomfromgeojson(?)";
			st = conn.prepareStatement(sql);
			st.setString(1, Json.stringify(this.getGeometry()));
			rs = st.executeQuery();

			if (rs.getLong("id") != 0){

				this.id = rs.getLong("id");
				// TODO check tags

			} else {
				// Create new node in DB
				sql = "insert into osmnodes (id, vers, usr, uid, timest, tags, inuse, geom) " +
						"values (?, ?, ?, ?, to_date(?, 'YYYY-MM-DDTHH:mm:ss:SSS'), ?, st_geomfromgeojson(?))";
				st = conn.prepareStatement(sql);
				st.setLong(1, this.id);
				st.setInt(2, this.version);
				st.setString(3, this.user);
				st.setString(4, this.uid);
				st.setDate(5, new java.sql.Date(timeStamp.getTime()));
				st.setString(6, tagsToHstoreFormat(this.tags));
				st.setBoolean(7, this.inUse);
				st.setString(8, Json.stringify(this.getGeometry()));
				rs = st.executeQuery();
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
		return this;
	}

	public OsmNode update(){
		return (id == 0) ? null : this;
	}

	public void delete(){
		
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		try {
			conn = ds.getConnection();
			String sql = "delete from osmnodes where id = ?";
			st = conn.prepareStatement(sql);
			st.setLong(1, this.id);
			st.executeQuery();
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
		geomNode.put("type", "point");
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
	public JsonNode toJson(){

		ObjectNode osmNodeNode = Json.newObject();
		osmNodeNode.put("type", "Feature");
		osmNodeNode.put("id", id);
		osmNodeNode.put("version", version);
		osmNodeNode.put("user", user);
		osmNodeNode.put("uid", uid);
		osmNodeNode.put("timestamp", new java.text.SimpleDateFormat("yyyy-mm-ddTHH:mm:ss:SSS").format(timeStamp));
		osmNodeNode.put("geometry", this.getGeometry());
		osmNodeNode.put("properties", Json.toJson(tags));

		return osmNodeNode;
	}

	public Document toOsmXml() throws ParserConfigurationException{

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		Element osmNodeElement = document.createElement("node");

		osmNodeElement.setAttribute("id", id+"");
		osmNodeElement.setAttribute("version", version+"");
		osmNodeElement.setAttribute("lat", lonlat.getY()+"");
		osmNodeElement.setAttribute("lon", lonlat.getX()+"");
		osmNodeElement.setAttribute("user", user);
		osmNodeElement.setAttribute("uid", uid);
		osmNodeElement.setAttribute("timestamp", new java.text.SimpleDateFormat("yyyy-mm-ddTHH:mm:ss:SSS").format(timeStamp));

		document.appendChild(osmNodeElement);

		for (String key : tags.keySet()){
			Element osmTagElement = document.createElement("tag");
			osmTagElement.setAttribute("k", key);
			osmTagElement.setAttribute("v", tags.get(key));
			osmNodeElement.appendChild(osmTagElement);
		}
		return document;
	}

	public static JsonNode listToJson(List<OsmNode> list) {
		ArrayNode json = new ArrayNode(JsonNodeFactory.instance);
		for (OsmNode node : list)
			json.add(node.toJson());
		return json;
	}

}
