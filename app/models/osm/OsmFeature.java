package models.osm;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class OsmFeature {
	
	protected long id; 				// ID at OSM, negative if it is created by us
	protected int version;          // Elements version
	protected String user;          // Users name
	protected String uid;           // Users id
	protected Date timeStamp;       // Elements timestamp
	protected LinkedHashMap<String,String> tags; // Elements tags
	
	protected boolean inUse;		// Know if this feature is being used by another user
	
	
	/**
	 * !!!!!!!!! SETTERS ONLY THROUGH JSON CONSTRUCTOR AND UPDATER TO ENSURE INTEGRITY !!!!!!!!!!! *
	 */
	public long getId() {
		return id;
	}

	public Map<String,String> getTags() {
		return tags;
	}
	/**
	 * !!!!!!!!! SETTERS ONLY THROUGH JSON CONSTRUCTOR AND UPDATER TO ENSURE INTEGRITY !!!!!!!!!!! *
	 */

	public boolean isInUse(){
		return inUse;
	}
	
	public void serInUse(boolean inUse){
		this.inUse = inUse;
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
		
		for (String value : tags.values()){
			if (first){
				result.append("'" + value + "'");
				first = false;
			} else {
				result.append(",'" + value + "'");
			}
		}
		
		return result.toString();
	}
}
