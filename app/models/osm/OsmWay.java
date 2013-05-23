package models.osm;

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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.db.DB;
import play.libs.Json;

public abstract class OsmWay extends OsmFeature {

	private List<OsmNode> nodes; //Nodes  
	// It is traslated to an EPSG:90013 geometry in PostGIS

	/* EXPECTED DATA EXAMPLES :
	 * 
		  { "type": "Feature",
		  "id": "514",
		  "user": "Pepe",
		  "uid": "4345314",
		  "timestamp": "aaa"
	      "geometry": {"type": "LineString", "coordinates": [[-104.05, 48.99],[-97.22,  48.98],[-96.58,  45.94],[-104.03, 45.94]], "node_ids": [13,52,73,52] },
	      "properties": {"key0": "value0"}
	      }
	 *
	      { "type": "Feature",
		  "id": "344",
		  "user": "Pepe",
		  "uid": "4345314",
		  "timestamp": "aaa"
	      "geometry": {"type": "Polygon", "coordinates": [[-104.05, 48.99],[-97.22,  48.98],[-104.05, 48.99]], "node_ids":[13,52,13] },
	      "properties": {"key0": "value0"}
	      }
	 *
	      <way id="5090250" visible="true" timestamp="2009-01-19T19:07:25Z" version="8" changeset="816806" user="Blumpsy" uid="64226">
		    <nd ref="822403"/>
		    <nd ref="21533912"/>
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
	
	
//	public static OsmWay findById(long id){
//		DataSource ds = DB.getDataSource();
//		Connection conn = null;
//		PreparedStatement st;
//		ResultSet rs;
//		OsmWay way = null;
//		try {
//			conn = ds.getConnection();
//			String sql = "select id, vers, usr, uid, timest, tags, inusebyuseroid, st_asgeojson(geom) as geometry from osmways";
//			st = conn.prepareStatement(sql);
//			rs = st.executeQuery();
//			while (rs.next()) {
//				node = new OsmNode(rs.getLong("id"),
//						rs.getInt("version"),
//						rs.getString("user"),
//						rs.getString("uid"),
//						0,
//						0,
//						rs.getDate("timestamp"),
//						hstoreFormatToTags(rs.getString("tags")));
//				way.setGeometry(Json.parse(rs.getString("geometry")));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} finally {
//			if (conn != null) try {
//				conn.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//		return way;
//	}
	
	
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
					node = new OsmNode(idsNode.get(x).getLongValue(), 1, this.user, this.uid, coordinatesNode.get(0).get(x).get(1).asDouble(), coordinatesNode.get(0).get(x).get(0).asDouble(), new Date(), null);
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
	
	
}
