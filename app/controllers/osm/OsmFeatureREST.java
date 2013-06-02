package controllers.osm;

import java.sql.Array;
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
import models.osm.OsmWay;

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

public class OsmFeatureREST extends ItemREST {

	public static Result getNearFeatures(int limit){
		final User user = Application.getLocalUser(session());
		if (user == null){
			return badRequest(Constants.USER_NOT_LOGGED_IN.toString());
		}
		JsonNode json = request().body().asJson();
		if(json == null) {
			return badRequest(Constants.JSON_EMPTY.toString());
		}

		List<OsmFeature> features = OsmWay.findByLocation(json.findPath("geometry"),limit);
        features.addAll(OsmNode.findByLocation(json.findPath("geometry"),limit));
		return ok(Json.toJson(OsmFeature.toObjectNodes(features)));
	}

}
