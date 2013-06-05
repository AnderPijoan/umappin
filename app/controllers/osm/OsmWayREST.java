package controllers.osm;

import java.text.ParseException;
import java.util.List;

import models.User;
import models.osm.OsmFeature;
import models.osm.OsmWay;

import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import play.libs.Json;
import play.mvc.Result;
import controllers.Application;
import controllers.Constants;
import controllers.ItemREST;

public class OsmWayREST extends ItemREST {


	public static Result getWayById(Long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		OsmWay way = OsmWay.findById(id);
		if (way == null){
			return badRequest(Constants.WAYS_EMPTY.toString());
		}
		if (request().accepts("application/json")){
			return ok(way.toOsmJson());
		} else if (request().accepts("text/xml")) {
			try{
				Document doc = way.toOsmXml();
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


	public static Result getJsonWayById(Long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		OsmWay way = OsmWay.findById(id);
		if (way == null){
			return badRequest(Constants.WAYS_EMPTY.toString());
		}
		return ok(Json.toJson(way.toOsmJson()));
	}


	public static Result getXmlWayById(Long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		OsmWay way = OsmWay.findById(id);
		if (way == null){
			return badRequest(Constants.WAYS_EMPTY.toString());
		}
		try{
			Document doc = way.toOsmXml();
			DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
			LSSerializer lsSerializer = domImplementation.createLSSerializer();
			return ok(lsSerializer.writeToString(doc));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError();
		}
	}


	public static Result getWayByGeom(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmWay way = OsmWay.findByGeom(json.findPath("geometry"));
		if (way == null){
			return badRequest(Constants.WAYS_EMPTY.toString());
		}
		if (request().accepts("application/json")){
			return ok(way.toOsmJson());
		} else if (request().accepts("text/xml")) {
			try{
				Document doc = way.toOsmXml();
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


	public static Result getJsonWayByGeom(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmWay way = OsmWay.findByGeom(json.findPath("geometry"));
		if (way == null){
			return badRequest(Constants.WAYS_EMPTY.toString());
		}
		return ok(Json.toJson(way.toOsmJson()));
	}


	public static Result getXmlWayByGeom(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmWay way = OsmWay.findByGeom(json.findPath("geometry"));
		if (way == null){
			return badRequest(Constants.WAYS_EMPTY.toString());
		}
		try{
			Document doc = way.toOsmXml();
			DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
			LSSerializer lsSerializer = domImplementation.createLSSerializer();
			return ok(lsSerializer.writeToString(doc));
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError();
		}
	}


	public static Result addWay(){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmWay newWay;
		try {
			if (json.has("way") && !json.findPath("way").isNull()){
				newWay = new OsmWay(json.findPath("way"));
			} else {
				newWay = new OsmWay(json);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(Constants.JSON_MALFORMED.toString());
		}

		newWay = newWay.save();

		if (newWay == null){
			return badRequest(Constants.FEATURE_REJECTED.toString());
		} else {
			return ok(Json.toJson(newWay.toOsmJson()));
		}
	}


	public static Result deleteWay(long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		OsmWay way = OsmWay.findById(id);
		if (way == null){
			return badRequest(Constants.WAYS_EMPTY.toString());
		}
		way.delete();
		return ok();
	}


	public static Result updateWay(Long id){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}
		OsmWay way = OsmWay.findById(id);
		if (way == null){
			return badRequest(Constants.NODES_EMPTY.toString());
		}
		OsmWay newWay;
		try {
			if (json.has("way") && !json.findPath("way").isNull()){
				newWay = new OsmWay(json.findPath("way"));
			} else {
				newWay = new OsmWay(json);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return badRequest(Constants.JSON_MALFORMED.toString());
		}

		if(newWay.getId() != way.getId() || newWay.getId() != id){
			return badRequest(Constants.JSON_MALFORMED.toString());
		}

		if (way.getVesion() < newWay.getVesion()) {
			newWay = newWay.save();
		}

		if (newWay == null){
			return badRequest(Constants.FEATURE_REJECTED.toString());
		} else {
			return ok(Json.toJson(newWay.toOsmJson()));
		}
	}


	public static Result getNearWays(int limit){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}

		List<OsmFeature> ways = OsmWay.findByLocation(json.findPath("geometry"),limit);
		return ok(Json.toJson(OsmFeature.toObjectNodes(ways)));
	}


	public static Result getIntersectionWays(int limit){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}

		List<OsmFeature> ways = OsmWay.findByIntersection(json.findPath("geometry"),limit);
		return ok(Json.toJson(OsmFeature.toObjectNodes(ways)));
	}
}
