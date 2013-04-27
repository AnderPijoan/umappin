package controllers;

//import com.sun.javaws.exceptions.InvalidArgumentException;
import models.User;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.*;
import play.mvc.Controller;
import play.mvc.Result;
import models.Photo;

import static play.data.Form.form;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.xml.bind.DatatypeConverter;

public class PhotosREST extends Controller {

    //{"owner_id":"515b13eddd530c927811e78c", "last":123,"lons":123, "date_created":123, "description":"ciao", "title":"this title"}

    //json mapping
    private static final String DATE_CREATED = "date_created";
    private static final String OWNER_ID = "owner_id";
    private static final String ID = "id";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String DESCRIPTION = "description";
    private static final String TITLE = "title";
    public static final String CONTENT = "content";
    //generated in output, are not inherent properties of the object
    private static final String GET_PHOTO_CONTENT_LOCATION = "get_content_location";
    public static final String POST_PHOTO_CONTENT_LOCATION = "post_content_location";

    private static final int MAX_BASE64_UPLOAD_SIZE = 1024 * 512; //max size of a photo uploaded in json



    //@BodyParser.Of(BodyParser.MultipartFormData.class)
    public static Result uploadMultipartContent(String id){

//        if (request.headers.get("type").value().equals("multipart/form-data")){
//            return ok("file received from user: "+ request().username() + "; " + CONTENT_TYPE + "");
//        }

        MultipartFormData body = request().body().asMultipartFormData();


        if(body == null) {
            return badRequest("Expecting multipart/form-data request body");
        }

        Logger.info("multipart file received");


        ObjectId objId = null;
        try {
            objId = stringToObjectId(id);
        } catch (IllegalArgumentException e) {
            badRequest(e.getMessage());
        }

        Photo photo = Photo.findById(objId);

        if(photo == null){
            return notFound("error while uploading: photo with id '" + id + "' was not found");
        }

        //verify the user is the ower of the photo
        User user = Auth.getRequestingUser();
        if(user == null){
            return badRequest("user not recognized");
        }
        if(!photo.getOwnerId().equals(user.id)){
            return unauthorized("not allowed to update the photo");
        }



        FilePart picture;

        //let's take just one file, the first, ignoring any other potentially there
        picture = body.getFiles().get(0);

        File f = picture.getFile();

        if(f == null) {
            return badRequest("no file found in request");
        }

        Logger.info("file uploading: " + f + picture.getContentType());

        try {
            photo.addContent(f, picture.getContentType());
            photo.save();
        } catch (IOException e) {
            return badRequest("error in file content");
        }


        return created("file uploaded for picture '" + id + "' type: " + picture.getContentType());

    }

    public static ObjectId stringToObjectId(String id) {
        /*
        ObjectId objId;
        try {
            objId = new ObjectId(id);
        } catch (Exception e) {
            if(id == null) {
                id = "";
            }
            throw new IllegalArgumentException("invalid identifier '" + id +"'");
        }
        return objId;
        */
        return id != null ? new ObjectId(id) : null;
    }

    public static Result deletePhoto(String id){

        User user = Auth.getRequestingUser();

        if(user == null){
            return badRequest("user not recognized or not logged in");
        }

        ObjectId obj = stringToObjectId(id);
        Photo photo = Photo.findById(obj);

        if(photo == null){
            return notFound("photo with id '" + id + "' was not found");
        }

        if(!photo.getOwnerId().equals(user.id)){
            return unauthorized("not allowed to delete the photo");
        }

        photo.delete();

        return noContent();

    }


    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_BASE64_UPLOAD_SIZE)
    public static Result newPhoto(){

        Photo photo;
        JsonNode json;
        try {
            json = request().body().asJson();
            photo =  jsonToPhoto(json);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            Logger.error("error parsing jsonToPhoto, error message: " + e.getMessage());
            return badRequest("error parsing json to Photo or request total size exceeding " + MAX_BASE64_UPLOAD_SIZE + " bytes");
        }

        User user = Auth.getRequestingUser();

        if(user == null){
            return badRequest("user not recognized");
        }


        //requesting user is owner
        photo.setOwnerId(user.id);
        //blank out the photo id if any, it is a new photo, not an update
        photo.setId(null);


        if(json.has(CONTENT)){
            try{
                String contentBase64String = json.findPath(CONTENT).getTextValue();
                byte[] byteContent = extractBase64Content(contentBase64String);
                String contentType = extractUriContentType(contentBase64String);
                photo.addContent(byteContent, contentType);
            } catch (Exception e){
                return badRequest("error in " + CONTENT + ": " + e.getMessage());
            }
        }

        //get the newly created id
        String id = photo.save().toString();

        response().setHeader(
                CONTENT_LOCATION,
                routes.PhotosREST.getPhoto(id).toString());
        return created(photoToJson(photo));

    }


    public static Photo updatePhotoFromJson(Photo photo, JsonNode json) {
        if(json.has(LATITUDE)) photo.setLatitude(new Double(json.findPath(LATITUDE).getDoubleValue()));
        if(json.has(LONGITUDE)) photo.setLongitude(new Double(json.findPath(LONGITUDE).getDoubleValue()));
        if(json.has(DATE_CREATED)) photo.setCreated(new Date(json.findPath(DATE_CREATED).getLongValue()));
        if(json.has(OWNER_ID)) photo.setOwnerId(stringToObjectId(json.findPath(OWNER_ID).getTextValue()));
        if(json.has(TITLE)) photo.setTitle(json.findPath(TITLE).getTextValue());
        if(json.has(DESCRIPTION)) photo.setDescription(json.findPath(DESCRIPTION).getTextValue());
        if(json.has(ID)) photo.setId(stringToObjectId(json.findPath(ID).getTextValue()));
        return photo;
    }


    private static Photo jsonToPhoto(JsonNode json){
        Photo photo = new Photo();
        return updatePhotoFromJson(photo, json);
    }


    private static JsonNode photoToJson(Photo photo){
        ObjectNode json = Json.newObject();
        if(photo.getId() != null) json.put(ID, photo.getId().toString());
        if(photo.getCreated() != null) json.put(DATE_CREATED, photo.getCreated().getTime());
        if(photo.getOwnerId() != null) json.put(OWNER_ID, photo.getOwnerId().toString());
        if(photo.getLatitude() != null) json.put(LATITUDE, photo.getLatitude());
        if(photo.getLongitude() != null) json.put(LONGITUDE, photo.getLongitude());
        if(photo.getTitle() != null) json.put(TITLE, photo.getTitle());
        if(photo.getDescription() != null) json.put(DESCRIPTION, photo.getDescription());
        //if the photo is already persisted (as it normally would)
        //generate the location url that points to its content
        ObjectId objectId = photo.getId();
        if(objectId != null){
            json.put(POST_PHOTO_CONTENT_LOCATION, routes.PhotosREST.uploadMultipartContent(objectId.toString()).toString());
            //TODO, photo location
            if(photo.getPhotoContents().size() > 0){
                json.put(GET_PHOTO_CONTENT_LOCATION, routes.PhotosREST.getPhotoContent(objectId.toString()).toString());
            }
        }
        return json;
    }


    public static Result getPhotoContent(String id){

        ObjectId obj;
        try {
            obj = stringToObjectId(id);;
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
        Photo photo = Photo.findById(obj);

        if(photo == null){
            return notFound("photo with id '" + id + "' was not found");
        }

        ByteArrayInputStream inStream = null;
        //returns the first one
        //TODO: it will return the proper Content when handling multiple photo sizes (quality)
        for(Photo.Content c : photo.getPhotoContents()){
            response().setContentType(c.getMimeType());
            //response().setHeader("Content-Disposition", "attachment; filename=FILENAME");
            //response().setHeader(ETAG, "xxx");
            //ByteArrayInputStream bais = new ByteArrayInputStream(c.getFileBytes());
            Logger.info("found photo " + c.getId().toString());
            inStream = new ByteArrayInputStream(c.getFileBytes());

        }
        if(inStream == null){
            return notFound("photo content is missing");
        }
        return ok(inStream);
    }


    public static Result getPhoto(String id){

        ObjectId obj;
        try {
            obj = stringToObjectId(id);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
        Photo photo = Photo.findById(obj);
        if(photo == null){
            return notFound("photo with id '" + id + "' was not found");
        }
        return ok(photoToJson(Photo.findById(obj)));
    }

    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_BASE64_UPLOAD_SIZE)
    public static Result updatePhoto(String id){
        User user = Auth.getRequestingUser();

        if(user == null){
            return badRequest("user not recognized");
        }

        ObjectId objId = null;
        try {
            objId = stringToObjectId(id);
        } catch (IllegalArgumentException e) {
            badRequest(e.getMessage());
        }

        Photo photo = Photo.findById(objId);

        if(photo == null) {
            return notFound("error while uploading: photo with id '" + id + "' was not found");
        }

        if(!photo.getOwnerId().equals(user.id)){
            return unauthorized("not allowed to update the photo");
        }

        JsonNode json;
        try {
            json = request().body().asJson();
            photo = updatePhotoFromJson(photo, json);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            Logger.error("error parsing jsonToPhoto, error message: " + e.getMessage());
            return badRequest("error parsing json to Photo or request total size exceeding " + MAX_BASE64_UPLOAD_SIZE + " bytes");
        }

        if(json.has(CONTENT)){
            try{
                String contentBase64String = json.findPath(CONTENT).getTextValue();
                byte[] byteContent = extractBase64Content(contentBase64String);
                String contentType = extractUriContentType(contentBase64String);
                photo.addContent(byteContent, contentType);
            } catch (Exception e){
                return badRequest("error in " + CONTENT + ": " + e.getMessage());
            }
        }

        photo.save();

        //return the updated representation
        return ok(photoToJson(photo));
    }

    private static String extractUriContentType(String contentBase64String) {

        if(contentBase64String == null || contentBase64String.equals("")){
            throw new IllegalArgumentException(CONTENT + " is empty");
        }

        String contentType = "";

        {
            // content looks similar to this 'data:image/png;base64, ... '
            int start = contentBase64String.indexOf("data:");
            int stop = contentBase64String.indexOf(";");
            if(start < 0 || stop < 0 || start == stop){
                throw new IllegalArgumentException("error parsing " + CONTENT + ", no data type declaration found");
            }
            contentType = contentBase64String.substring(start + 5, stop);
        }

        Logger.info("declared type is " + contentType);

        return contentType;
    }

    private static byte[] extractBase64Content(String contentBase64String) {

        if(contentBase64String == null || contentBase64String.equals("")){
            throw new IllegalArgumentException(CONTENT + " is empty");
        }


        //the first characters will look like this; strip them off the content
        //data:image/png;base64,
        int contentIndex = contentBase64String.indexOf("base64") + 7;

        if(contentIndex < 7){
            throw new IllegalArgumentException("error parsing " + CONTENT + ", no 'base64' string found");
        }

        byte[] contentBytes = DatatypeConverter.parseBase64Binary(
                contentBase64String.substring(contentIndex)
        );
        if(contentBytes == null || contentBytes.length==0){
            throw new IllegalArgumentException(CONTENT + "payload seems empty");
        }

        return contentBytes;

    }


    private static class Auth {

        private static final String QUERYSTRING_USERID = "user_email";

        private static User getRequestingUser(){

            User user = null;
            String userEmail = request().getQueryString(QUERYSTRING_USERID);

            Logger.info("user sent querystring with '" + QUERYSTRING_USERID + "' = " + userEmail);

            if(userEmail != null && !"".equals(userEmail)){
                try {
                    return User.findByEmail(userEmail);
                } catch (Exception e) {
                    Logger.info("unable to find such user");
                    return null;
                }
            }
            Logger.info("play authenticate...");
            user = Application.getLocalUser(session());
            if(user == null){
                Logger.info("user not found");
            }
            return user;

        }
    }



}
