package controllers.games;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import play.mvc.Result;

import models.osm.OsmNode;


public class OsmFixmesREST {

	public static Result populateDatabase() {

		// World max latitudes and longitudes
		int left = -10; // -180
		int right = 40; // 180
		int up = 50; // 45
		int down = -10; // -45

		// Search for FIXME tags in each world cuadrant
		for(int lon = left; lon < right; lon++){
			for(int lat = down; lat < up; lat++){

				String bbox = "(" + lat + "," + lon + "," + (lat+1) + "," + (lon+1) + ")";

				System.out.println("Downloading " + bbox);

				try {

					// Create URL for downloading from OVERPASS API
					URL url = new URL("http://overpass-api.de/api/interpreter?data=" +
							"(node" + bbox + "[fixme];" +
							//"way" + bbox + "[fixme];>;" +
							//"rel" + bbox + "[fixme];>>;" + 
							");out%20meta;");

					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setDoOutput(true);
					connection.connect();

					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document osmXML = builder.parse(new InputSource(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"))));

					NodeList osmList = osmXML.getElementsByTagName("osm");
					osmXML.getDocumentElement().normalize();

					// <osm>
					for (int x = 0; x < osmList.getLength(); x++)
					{
						Node osm = osmList.item(x);
						if (osm.getNodeType() == Node.ELEMENT_NODE)
						{
							Element osmElement = (Element) osm;

							// Parse Nodes
							NodeList nodeList = osmElement.getElementsByTagName("node");

							for (int y = 0; y < nodeList.getLength(); y++)
							{
								// <node>
								Node nodeXML = nodeList.item(y);
								if (nodeXML.getNodeType() == Node.ELEMENT_NODE)
								{

									try {
										OsmNode node = new OsmNode(nodeXML);
										node.save();
										
									} catch (Exception e) {
										e.printStackTrace();
									}

								}
							}

							// Parse Ways

							// Parse Relations
						}
					}
					// </osm>
					connection.disconnect();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
}
