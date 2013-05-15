package models.osm;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class OsmFeature {
	
	protected DataSource ds;
	
	protected long id; 				// ID at OSM, negative if it is created by us
	protected int version;          // Elements version
	protected String user;          // Users name
	protected String uid;           // Users id
	protected Date timeStamp;       // Elements timestamp
	protected LinkedHashMap<String,String> tags; // Elements tags
	
	protected ObjectId inUseByUser;		// Know if this feature is being used by a user
	
	
	/**
	 * !!!!!!!!! SETTERS ONLY THROUGH JSON CONSTRUCTOR AND UPDATER TO ENSURE INTEGRITY !!!!!!!!!!! *
	 */
	public long getId() {
		return id;
	}
	
	public long getVesion() {
		return version;
	}

	public Map<String,String> getTags() {
		return tags;
	}
	/**
	 * !!!!!!!!! SETTERS ONLY THROUGH JSON CONSTRUCTOR AND UPDATER TO ENSURE INTEGRITY !!!!!!!!!!! *
	 */

	public boolean isInUse(){
		return inUseByUser == null;
	}
	
	public void serUserUsing(ObjectId oid){
		this.inUseByUser = oid;
	}
	
	public abstract OsmFeature save();
	
	public abstract OsmFeature update();
	
	public abstract void delete();
	
	public abstract JsonNode getGeometry();
	
	public abstract void setGeometry(JsonNode geometry);
	
	public abstract JsonNode toJson();
	
	public abstract Document toOsmXml() throws ParserConfigurationException;
	
	public static Document osmXMLParser(String osmXML) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	    DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(osmXML);
	}
	
	public static String tagsToHstoreFormat(LinkedHashMap<String,String> tags){
		
		/* EXAMPLE:
		 * hstore(array['author','date','stock'],array['Mike','Nov 2012','200'])
		 */
		
		StringBuilder result = new StringBuilder("hstore(array[");
		boolean first = true;
		
		if (tags != null)
		for(String key : tags.keySet()){
			if (first){
				result.append("'" + key + "'");
				first = false;
			} else {
				result.append(",'" + key + "'");
			}
		}
		
		first = true;
		result.append("],array[");
		
		if (tags != null)
		for (String value : tags.values()){
			if (first){
				result.append("'" + value + "'");
				first = false;
			} else {
				result.append(",'" + value + "'");
			}
		}
		
		result.append("])");
		return result.toString();
	}
}
