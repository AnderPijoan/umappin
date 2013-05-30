package controllers.osm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import models.User;
import models.osm.OsmFeature;
import models.osm.OsmNode;

import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import play.db.DB;
import play.libs.Json;
import play.mvc.Result;
import controllers.Application;
import controllers.Constants;
import controllers.ItemREST;

public class OsmNodeREST extends ItemREST {

	public static Result getNodeById(Long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		OsmNode node = OsmNode.findById(id);
		if (node == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		if (request().accepts("application/json")){
			return ok(Json.toJson(node.toOsmJson()));
		} else if (request().accepts("text/xml")) {
			try{
				Document doc = node.toOsmXml();
				DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
				LSSerializer lsSerializer = domImplementation.createLSSerializer();
				return ok(lsSerializer.writeToString(doc));
			} catch (Exception e) {
				e.printStackTrace();
				return internalServerError();
			}
		}
		return badRequest();
	}


	public static Result getJsonNodeById(Long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		OsmNode node = OsmNode.findById(id);
		if (node == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		return ok(Json.toJson(node.toOsmJson()));
	}


	public static Result getXmlNodeById(Long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		OsmNode node = OsmNode.findById(id);
		if (node == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		try{
			Document doc = node.toOsmXml();
			DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
			LSSerializer lsSerializer = domImplementation.createLSSerializer();
			return ok(lsSerializer.writeToString(doc));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError();
		}
	}


	public static Result getNodeByGeom(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmNode node = OsmNode.findByGeom(json.findPath("geometry"));
		if (node == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		if (request().accepts("application/json")){
			return ok(Json.toJson(node.toOsmJson()));
		} else if (request().accepts("text/xml")) {
			try{
				Document doc = node.toOsmXml();
				DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
				LSSerializer lsSerializer = domImplementation.createLSSerializer();
				return ok(lsSerializer.writeToString(doc));
			} catch (Exception e) {
				e.printStackTrace();
				return internalServerError();
			}
		}
		return badRequest();
	}


	public static Result getJsonNodeByGeom(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmNode node = OsmNode.findByGeom(json.findPath("geometry"));
		if (node == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		return ok(Json.toJson(node.toOsmJson()));
	}


	public static Result getXmlNodeByGeom(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmNode node = OsmNode.findByGeom(json.findPath("geometry"));
		if (node == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		try{
			Document doc = node.toOsmXml();
			DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
			LSSerializer lsSerializer = domImplementation.createLSSerializer();
			return ok(lsSerializer.writeToString(doc));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError();
		}
	}


	public static Result addNode(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmNode newNode;
		try {
			if (json.has("node") && !json.findPath("node").isNull()) {
				newNode = new OsmNode(json.findPath("node"));
			} else {
				newNode = new OsmNode(json);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(Constants.JSON_MALFORMED.toString());
		}

		newNode = newNode.save();

		if (newNode == null){
			return badRequest(Constants.FEATURE_REJECTED.toString());
		} else {
			return ok(Json.toJson(newNode.toOsmJson()));
		}
	}

	
	public static Result deleteNode(long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		OsmNode node = OsmNode.findById(id);
		if (node == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		node.delete();
		return ok();
	}

	

	public static Result updateNode(Long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmNode node = OsmNode.findById(id);
		if (node == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		OsmNode newNode;
		try {
			if (json.has("node") && !json.findPath("node").isNull()){
				newNode = new OsmNode(json.findPath("node"));
			} else {
				newNode = new OsmNode(json);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(Constants.JSON_MALFORMED.toString());
		}

		if(newNode.getId() != node.getId() || newNode.getId() != id){
			return badRequest(Constants.JSON_MALFORMED.toString());
		}

		if (node.getVesion() < newNode.getVesion()){
			newNode = newNode.save();
		}

		if (newNode == null){
			return badRequest(Constants.FEATURE_REJECTED.toString());
		} else {
			return ok(Json.toJson(newNode.toOsmJson()));
		}
	}

	
	public static Result getNearNodes(int limit){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}

		List<OsmFeature> nodes = OsmNode.findByLocation(json.findPath("geometry"),limit);
		return ok(Json.toJson(OsmFeature.toObjectNodes(nodes)));
	}
	
	
	public static Result getIntersectionNodes(int limit){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}

		List<OsmFeature> nodes = OsmNode.findByIntersection(json.findPath("geometry"),limit);
		return ok(Json.toJson(OsmFeature.toObjectNodes(nodes)));
	}


	public static Result getFirstFreeId(int limit){
		return ok(OsmNode.getFirstFreeId(limit) + "");
	}

}
