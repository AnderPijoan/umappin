package models.osm;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import scala.util.control.Exception;

public abstract class OsmWay extends OsmFeature{

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

	
	public void setGeometry(JsonNode geometry) {

		/* EXAMPLE :
		 * {"type": "LineString", "coordinates": [[-104.05, 48.99],[-97.22,  48.98],[-104.05, 48.99]], "node_ids":[13,52,13]},
		 */

		nodes = new ArrayList<OsmNode>();
		JsonNode coordinatesNode = geometry.findPath("coordinates");
		JsonNode idsNode = geometry.findPath("node_ids");

		if (geometry.findPath("type").asText().toUpperCase().equals("LINESTRING") && coordinatesNode.size() == idsNode.size()){

			for(int x = 0; x < coordinatesNode.size(); x++){
				
				OsmNode node = OsmNode.findById(idsNode.get(x).getLongValue());
				
				// first position in array is lon and second lat
				// If position doesnt match
				if ( ( node.getLon() != coordinatesNode.get(x).get(0).asDouble() || 
						node.getLat() != coordinatesNode.get(x).get(1).asDouble() )){
						throw new ConcurrentModificationException("Malformed");
				}
				
				nodes.add(node);
			}
			
		}
		
		if (geometry.findPath("type").asText().toUpperCase().equals("POLYGON") && coordinatesNode.size() == idsNode.size()){

		}
	}
	
	
}
