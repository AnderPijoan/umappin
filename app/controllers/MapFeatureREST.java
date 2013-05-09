package controllers;

import models.MapFeature;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.mvc.Result;

import java.util.List;

import static play.mvc.Http.Context.Implicit.request;
import static play.mvc.Results.*;

public class MapFeatureREST {

    public static final String DATABASE_ERROR = "Error operating with PostgreSQL Database";
	
	public static Result getAllFeatures() {
        List<MapFeature> features = MapFeature.all();
        if (features == null || features.size() < 1)
            return notFound(Constants.JSON_EMPTY.toString());
        else
            return ok(MapFeature.listToJson(features));
    }

    public static Result getFeature(String id) {
        MapFeature feature = MapFeature.findById(id);
        return (feature == null) ? notFound(Constants.JSON_EMPTY.toString()) : ok(feature.toJson());
    }

    public static Result addFeature() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else {
            ((ObjectNode)json).remove("id");
            MapFeature feature = MapFeature.fromJson(json);
            feature = feature.save();
            return feature == null ? internalServerError(DATABASE_ERROR) : ok(feature.toJson());
        }
    }

    public static Result updateFeature(String id) {
        JsonNode json = request().body().asJson();
        if(json == null || id == null) {
            return badRequest(Constants.JSON_EMPTY.toString());
        } else {
            MapFeature feature = MapFeature.findById(id);
            if (feature == null) {
                return notFound(Constants.JSON_EMPTY.toString());
            } else {
                MapFeature otherFeature = MapFeature.fromJson(json);
                feature = feature.update(otherFeature);
                return feature == null ? internalServerError(DATABASE_ERROR) : ok(feature.toJson());
            }
        }
    }

    public static Result deleteFeature(String id) {
        MapFeature feature = MapFeature.findById(id);
        if (feature == null) {
            return notFound(Constants.JSON_EMPTY.toString());
        } else {
            feature = feature.delete();
            return feature == null ? internalServerError(DATABASE_ERROR) : ok(feature.toJson());
        }
    }

}
