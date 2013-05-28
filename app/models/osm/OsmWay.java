package models.osm;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
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

public class OsmWay extends OsmFeature {

	private List<OsmNode> nodes; //Nodes  
	// It is translated to an EPSG:90013 geometry in PostGIS

	/* EXPECTED DATA EXAMPLES :
	 * 
		  { "type": "Feature",
		  "id": "514",
		  "user": "Pepe",
		  "uid": "4345314",
		  "timestamp": "aaa"
	      "geometry": {"type": "LineString", "coordinates": [[-104.05, 48.99],[-97.22,  48.98],[-96.58,  45.94],[-104.03, 45.94]], "node_ids": [13,-4,73,52] },
	      "properties": {"key0": "value0"}
	      }
	 *
	      { "type": "Feature",
		  "id": "344",
		  "user": "Pepe",
		  "uid": "4345314",
		  "timestamp": "aaa"
	      "geometry": {"type": "Polygon", "coordinates": [[-104.05, 48.99],[-97.22,  48.98],[-104.05, 48.99]], "node_ids":[13,-4,13] },
	      "properties": {"key0": "value0"}
	      }
	 *
	      <way id="5090250" visible="true" timestamp="2009-01-19T19:07:25Z" version="8" changeset="816806" user="Blumpsy" uid="64226">
		    <nd ref="822403"/>
		    <nd ref="-4"/>
		    <nd ref="821601"/>
		    <nd ref="823771"/>
		    <tag k="highway" v="residential"/>
		    <tag k="name" v="Clipstone Street"/>
		    <tag k="oneway" v="yes"/>
		  </way>
	 *
	 */


	public OsmWay(JsonNode json) throws ParseException{

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


	/** OSM XML Way parser
	 * @param osmXml
	 * @throws ParseException
	 */
	public OsmWay(Node osmXml) throws ParseException{

		Element nodeElement = (Element) osmXml;

		id = Long.parseLong(nodeElement.getAttribute("id"));
		version = Integer.parseInt(nodeElement.getAttribute("version"));
		user = nodeElement.getAttribute("user");
		uid = nodeElement.getAttribute("uid");

		System.out.println("FIX TIMESTAMP : " + nodeElement.getAttribute("timestamp"));
		timeStamp = new java.sql.Date(0);
		//timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").parse("2010-01-02T10:04:33Z");
		//timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ").parse(nodeElement.getAttribute("timestamp"));

		setTags(nodeElement.getElementsByTagName("tag"));

		NodeList nodeList = nodeElement.getElementsByTagName("nd");
		if (nodeList != null) {

			for (int y = 0; y < nodeList.getLength(); y++){

				Node nd = nodeList.item(y);
				if (nd.getNodeType() == Node.ELEMENT_NODE)
				{
					Element refElement = (Element) nd;
					if (nodes == null)
						nodes = new ArrayList<OsmNode>();

					long nodeId = Long.parseLong(refElement.getAttribute("ref"));
					OsmNode node = OsmNode.findById(nodeId);

					if (node != null)
						nodes.add(node);
				}
			}
		}
	}


	public OsmWay (long id, int version, String user, String uid, List<OsmNode> nodes, Date timestamp, LinkedHashMap<String,String> tags){
		this.id = id;
		this.version = version;
		this.user = user;
		this.uid = uid;
		this.nodes = nodes;
		this.timeStamp = timestamp;
		this.tags = tags;
	}


	public static OsmWay findById(long id){
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;
		OsmWay way = null;
		try {
			conn = ds.getConnection();
			String sql = "select id, nodes, vers, usr, uid, timest, tags, st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry from osmways where id = ?";
			st = conn.prepareStatement(sql);
			st.setLong(1, id);
			rs = st.executeQuery();
			while (rs.next()) {
				Array nodesArray = rs.getArray("nodes");
				Long[] nodeIds = (Long[]) nodesArray.getArray();
				List<OsmNode> nodes = new ArrayList<OsmNode>();
				
				// Get the nodes
				for(long nodeId : nodeIds){
					OsmNode node = OsmNode.findById(nodeId);
					if (node != null){
						nodes.add(node);
					}
				}
				
				way = new OsmWay(rs.getLong("id"),
						rs.getInt("version"),
						rs.getString("user"),
						rs.getString("uid"),
						nodes,
						rs.getDate("timestamp"),
						hstoreFormatToTags(rs.getString("tags")));
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
		return way;
	}


	public static OsmWay findByGeom(JsonNode geometry){
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;
		OsmWay way = null;
		try {
			conn = ds.getConnection();
			String sql = "select id, vers, usr, uid, timest, tags, st_asgeojson(ST_Transform(ST_SetSRID(geom, 900913),4326)) as geometry " +
					"from osmways where geom = ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + ")";
			st = conn.prepareStatement(sql);
			st.setString(1, Json.stringify(geometry));
			rs = st.executeQuery();
			while (rs.next()) {
				way = new OsmWay(
						rs.getLong("id"),
						rs.getInt("version"),
						rs.getString("user"),
						rs.getString("uid"),
						null,
						rs.getDate("timest"),
						hstoreFormatToTags(rs.getString("tags")));
				way.setGeometry(Json.parse(rs.getString("geometry")));
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
		return way;
	}


	public OsmWay save(){

		if(id == 0)
			return null;

		if (this.ds == null){
			this.ds = DB.getDataSource();
		}

		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		boolean reject = false;

		try {

			////////////////////////////////////////////////////////////////////////////////////
			// OSM IDS START FROM 1 AND ON. IF WE CREATE A NEW WAY THAT DOESNT EXIST IN OSM,
			// WE GIVE IT A NEGATIVE ID. EVERYTIME A NEGATIVE ID ENTERS, IT WILL WE REALOCATED TO
			// THE HIGHEST AVAILABLE NEGATIVE ID
			////////////////////////////////////////////////////////////////////////////////////

			conn = ds.getConnection();

			// Check if already exists
			String sql = "select id, vers, tags from osmways where id = ? OR geom = ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + ")";
			st = conn.prepareStatement(sql);
			st.setLong(1, this.id);
			st.setString(2, Json.stringify(this.getGeometry()));
			rs = st.executeQuery();

			// A way with the same ID or Location exists, check possible cases
			if (rs.next()){

				// Way id already exists
				if(rs.getLong("id") == this.id){
					// If our Way has same or lower version than the one in DB, reject it
					if (rs.getInt("vers") >= this.version){
						reject = true;
					}
				}
				// Location is used by another way
				else {
					// Get the ways tags and merge them with ours
					this.tags.putAll(hstoreFormatToTags(rs.getString("tags")));
				}
			}

			// If theres no collition and the way won't be rejected
			if (!reject) {

				// If it is a way that we have created (its not taken from OSM)
				// Try to realocate it
				if(this.id < 0){

					// Returns first negative available ID, NEEDS ID=0 DUMMY WAY INSERTED
					sql = "SELECT (t1.id - 1) FROM osmways AS t1 LEFT JOIN osmways as t2 ON t1.id - 1 = t2.id WHERE t2.id IS NULL AND (t1.id <= 0) order by t1.id desc limit 1";
					st = conn.prepareStatement(sql);
					rs = st.executeQuery();

					// We found a wayId to give
					if (rs.next()){

						// Set the way to its new ID
						long oldId = this.id;
						this.id = rs.getLong("id");
						
						// Delete the old way
						sql = "delete from osmways where id = ?";
						st = conn.prepareStatement(sql);
						st.setLong(1, oldId);
						st.executeQuery();
					}
				}

				// Try updating, if the way doesnt exists, the query does nothing
				sql = "update osmways set vers = ?, usr = ?, uid = ?, timest = ?, nodes = ? " +
						"geom = ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + ")" + 
						(tags != null? ", tags = " + tagsToHstoreFormat(tags) : "" ) +
						" where id = ?";
				st = conn.prepareStatement(sql);
				st.setInt(1, this.version);
				st.setString(2, this.user);
				st.setString(3, this.uid);
				st.setDate(4, new java.sql.Date(timeStamp.getTime()));
				
				Long[] nodeIds = new Long[this.nodes.size()];
				for(int x = 0; x < this.nodes.size(); x++){
					nodeIds[x] = this.nodes.get(x).id;
				}
				
				st.setArray(5, conn.createArrayOf("BIGINT", nodeIds));
				st.setString(6, Json.stringify(this.getGeometry()));
				st.setLong(7, this.id);
				st.executeUpdate();

				// Try inserting, if the node exists, the query does nothing
				sql = "insert into osmnodes (id, vers, usr, uid, timest, nodes, geom " + 
						(tags != null? ",tags" : "" ) + ") " +
						"select ?, ?, ?, ?, ?, ?, ST_SimplifyPreserveTopology(ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913), " + TOLERANCE + ") " + 
						(tags != null? ", " + tagsToHstoreFormat(tags) : "" ) + " " +
						"where not exists (select 1 from osmnodes where id = ?)";
				st = conn.prepareStatement(sql);
				st.setLong(1, this.id);
				st.setInt(2, this.version);
				st.setString(3, this.user);
				st.setString(4, this.uid);
				st.setDate(5, new java.sql.Date(timeStamp.getTime()));
				st.setArray(6, conn.createArrayOf("BIGINT", nodeIds));
				st.setString(7, Json.stringify(this.getGeometry()));
				st.setLong(8, this.id);
				st.executeUpdate();
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

		if (reject)
			return null;

		return this;
	}


	/** Delete WayOSM
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
			String sql = "delete from osmways where id = ?";
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


	public List<OsmNode> getNodes() {
		return this.nodes;
	}

	public JsonNode getGeometry(){
		
		/* EXAMPLE :
		 * {"type": "LineString", "coordinates": [[-104.05, 48.99],[-97.22,  48.98]], "node_ids":[13,52]},
		 *
		 * {"type": "Polygon", "coordinates": [[[-104.05, 48.99],[-97.22,  48.98],[-104.05, 48.99]], ....(unused)], "node_ids":[13,52,13]},
		 */
		ObjectNode geomNode = Json.newObject();
		
		// If it is a closed way
		if (nodes.get(0).equals(nodes.get(nodes.size()-1))){
			geomNode.put("type", "Polygon");
		}
		// If it is not closed
		else {
			geomNode.put("type", "LineString");
		}
		
		Double[][] coordinates = new Double[nodes.size()][2];
		Long[] ids = new Long[nodes.size()];
		
		for(int x = 0; x < nodes.size(); x++){
			OsmNode node = nodes.get(x);
			coordinates[x][0] = node.getLon();
			coordinates[x][1] = node.getLat();
			ids[x] = node.id;
		}
		
		geomNode.put("coordinates", Json.toJson(coordinates));
		geomNode.put("node_ids", Json.toJson(ids));
		
		return geomNode;
	}


	public void setGeometry(JsonNode geometry) {

		/* EXAMPLE :
		 * {"type": "LineString", "coordinates": [[-104.05, 48.99],[-97.22,  48.98]], "node_ids":[13,52]},
		 *
		 * {"type": "Polygon", "coordinates": [[[-104.05, 48.99],[-97.22,  48.98],[-104.05, 48.99]], ....(unused)], "node_ids":[13,52,13]},
		 */

		nodes = new ArrayList<OsmNode>();
		JsonNode coordinatesNode = geometry.findPath("coordinates");
		JsonNode idsNode = geometry.findPath("node_ids");

		if (geometry.findPath("type").asText().toUpperCase().equals("LINESTRING") && coordinatesNode.get(0).size() == idsNode.size()){

			for(int x = 0; x < coordinatesNode.get(0).size(); x++){	

				ObjectNode osmNodeNode = Json.newObject();
				osmNodeNode.put("type", "Point");
				osmNodeNode.put("coordinates", coordinatesNode.get(0).get(x));

				OsmNode node = OsmNode.findById(idsNode.get(x).getLongValue());
				if (node == null){
					node = new OsmNode(
							idsNode.get(x).getLongValue(), 
							1, 
							this.user, 
							this.uid, 
							coordinatesNode.get(0).get(x).get(1).asDouble(), 
							coordinatesNode.get(0).get(x).get(0).asDouble(), 
							new Date(), 
							null);
				} else {
					node.setGeometry(osmNodeNode);
				}
				nodes.add(node);
			}
		}
		// A POLYGON way can only have one LINEARRING
		else if (geometry.findPath("type").asText().toUpperCase().equals("POLYGON") && coordinatesNode.get(0).size() == idsNode.size()){

			for(int x = 0; x < coordinatesNode.get(0).get(0).size(); x++){	

				ObjectNode osmNodeNode = Json.newObject();
				osmNodeNode.put("type", "Point");
				osmNodeNode.put("coordinates", coordinatesNode.get(0).get(0).get(x));

				OsmNode node = OsmNode.findById(idsNode.get(x).getLongValue());
				if (node == null){
					node = new OsmNode(
							idsNode.get(x).getLongValue(),
							1,
							this.user,
							this.uid,
							coordinatesNode.get(0).get(0).get(x).get(1).asDouble(),
							coordinatesNode.get(0).get(0).get(x).get(0).asDouble(),
							new Date(),
							null);
				} else {
					node.setGeometry(osmNodeNode);
				}
				nodes.add(node);
			}
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
		osmNodeNode.put("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(timeStamp));
		osmNodeNode.put("geometry", this.getGeometry());
		osmNodeNode.put("properties", Json.toJson(tags));

		return osmNodeNode;
	}


	public Document toOsmXml() throws ParserConfigurationException{

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();

		Element osmNodeElement = document.createElement("way");

		osmNodeElement.setAttribute("id", id+"");
		osmNodeElement.setAttribute("version", version+"");
		osmNodeElement.setAttribute("user", user);
		osmNodeElement.setAttribute("uid", uid);
		osmNodeElement.setAttribute("timestamp", new java.text.SimpleDateFormat("yyyy-mm-ddTHH:mm:ss:SSS").format(timeStamp));

		document.appendChild(osmNodeElement);

		for (OsmNode node : this.nodes){
			Element nodeRefElement = document.createElement("nd");
			nodeRefElement.setAttribute("ref", node.id + "");
			osmNodeElement.appendChild(nodeRefElement);
		}
		
		
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
