package controllers;

import models.PhotoUserLike;
import models.User;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.*;
import play.mvc.Controller;
import play.mvc.Result;
import models.Photo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

import javax.imageio.*;

public class PhotosREST extends Controller {

    //{"id":"5175a1253cdbf70ca1164d10","date_created":123,"owner_id":"5151c9c03609e0d8112d9a5a","title":"this title","description":"ciao","is_useful_count":0,"is_beautiful_count":0,"post_content_location":"/rest/photos/5175a1253cdbf70ca1164d10/content","get_content_location":"/rest/photos/5175a1253cdbf70ca1164d10/content"}

    //json mapping for Photo
    public static final String DATE_CREATED = "date_created";
    public static final String OWNER_ID = "owner_id";
    public static final String ID = "id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String IS_USEFUL_COUNT = "is_useful_count";
    public static final String IS_BEAUTIFUL_COUNT = "is_beautiful_count";
    //generated in output, are not inherent properties of the object
    public static final String GET_PHOTO_CONTENT_LOCATION = "get_content_location";
    public static final String POST_PHOTO_CONTENT_LOCATION = "post_content_location";

    //json mapping for PhotoUserLike
    public static final String USER_ID = "user_id";
    public static final String PHOTO_IS_BEAUTIFUL = "is_beautiful";
    public static final String PHOTO_IS_USEFUL = "is_useful";

    //max size of a photo uploaded in json = 1MB (base64 encoded, so it's like 6/8 x 1MB)
    public static final int MAX_BASE64_UPLOAD_SIZE = 1024 * 1024;
    //json attribute that specify the pagination params
    public static final String RESULTS_OFFSET = "offset";
    public static final String RESULTS_LIMIT = "limit";


    //========================Photo ===============//
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
        if(photo.getCreated()==null){
            photo.setCreated(new Date());
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

        //get the newly created id
        String id = photo.save().toString();

        response().setHeader(
                CONTENT_LOCATION,
                routes.PhotosREST.getPhoto(id).toString());
        return created(photoToJson(photo));

    }

    @BodyParser.Of(value = BodyParser.Json.class, maxLength = MAX_BASE64_UPLOAD_SIZE)
    public static Result updatePhoto(String id){

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

        ObjectId objId = null;
        try {
            objId = stringToObjectId(id);
        } catch (IllegalArgumentException e) {
            badRequest(e.getMessage());
        }

        Photo existingPhoto = Photo.findById(objId);

        if(photo == null){
            return notFound("error while uploading: photo with id '" + id + "' was not found");
        }

        if(!existingPhoto.getOwnerId().equals(user.id)){
            return unauthorized("not allowed to update the photo");
        }

        //here we are completely replacing the old with the new metadata.
        // If the new photo lacks a field, it will miss in the saved instance
        // Only the photo content and owner are preserved!
        photo.setId(objId);
        photo.setOwnerId(existingPhoto.getOwnerId());

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

        //delete any associated photoUserLike
        Photo.deleteUserLikesForPhoto(photo);

        photo.delete();

        return ok("photo '" + id + "' succesfully deleted");

    }

    //========================Photo Content===============//
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


    //========================Photo Content===============//

    /**
     * Returns a list of 'likes' for the photo, voted by the specified user.
     * The results are limited in number by
     *
     * @param photoId identifier of the photo
     * @param userId indentifier of the user;
     *               optional, if is null the query returns the results irrespective of the user
     * @param offset offset in the resultset
     * @param limit limit of number of results. Cannot be higher than PhotoUserLike.MAX_RESULTS_RETURNED
     * @return list of photoUserLikes in json
     */

    public static Result getPhotoUserLikes(String photoId, String userId, Integer offset, Integer limit){


        if(limit > PhotoUserLike.MAX_RESULTS_RETURNED){
            return badRequest("can't request more than " + PhotoUserLike.MAX_RESULTS_RETURNED + " results");
        }
        if(offset < 0){
            offset = 0;
        }

        ObjectId photoObjId;
        try {
            photoObjId = stringToObjectId(photoId);;
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }


        ObjectId userObjId = null;
        if(userId!=null){
            try {
                userObjId = stringToObjectId(userId);;
            } catch (IllegalArgumentException e) {
                return badRequest(e.getMessage());
            }
        }

        PhotoUserLike photoUserLike = null;
        {
            List<PhotoUserLike> userLikesList = PhotoUserLike
                    .getFromPhotoAndUser(
                            photoObjId, userObjId, offset, limit);

            //results found and user was not specified: return a list
            if(userObjId == null  && userLikesList.size() > 0){

                //ObjectNode json = Json.newObject();
                Logger.info("found " + userLikesList.size() + " likes");
                ObjectNode json = Json.newObject();

                ArrayNode arrayNode = json.putArray("userLikes");

                for(PhotoUserLike ul : userLikesList){
                    Logger.info("user like currently is " + ul.getId().toString());
                    Logger.info("user in like currently is " + ul.getUserId().toString());

                    arrayNode.add(userLikeToJson(ul));
                }
                json.put(RESULTS_OFFSET, offset);
                json.put(RESULTS_LIMIT, limit);
                return ok(json);

            }
            //a user was specified and a result was found, so return the (single) result
            if(userObjId != null && userLikesList.size() > 0 ){
                return ok(userLikeToJson(userLikesList.get(0)));
            }


        }
        return notFound("no 'photo like' found");

    }

    @BodyParser.Of(value = BodyParser.Json.class)
    public static Result setPhotoUserLike(String photoId){

        ObjectId photoObjId;
        try {
            photoObjId = stringToObjectId(photoId);;
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }

        PhotoUserLike userLike;
        JsonNode json;
        try {
            json = request().body().asJson();
            userLike =  jsonToPhotoUserLike(json);
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            Logger.error("error parsing jsonToPhotoUserLike, error message: " + e.getMessage());
            return badRequest("error parsing json");
        }

        User user = Auth.getRequestingUser();
        if(user == null){
            return badRequest("user not recognized");
        }


        //complete the details of the photoLike object

        //if the user id is not declared in the json, set it from the authenticated
        if(userLike.getUserId() == null){
            userLike.setUserId(user.id);
        }else{
            //if it is declared instead, make sure it is equal to the one currently authenticated
            if(!userLike.getUserId().equals(user.id)){
                return unauthorized("not allowed to set like/unlike with that " + USER_ID);
            }
        }

        //check that the photo actually exists
        Photo photo;
        photo = Photo.findById(photoObjId);
        if(photo == null){
            return notFound("photo '" + userLike.getPhotoId().toString() + "' not found");
        }
        userLike.setPhotoId(photoObjId);

        if(photo.getOwnerId().equals(userLike.getUserId())){
            return badRequest("don't cheat! you can't vote for your own photo :) ");
        }

        //retrieve any vote that the user has already given
        PhotoUserLike oldUserLike = null;
        {
            List<PhotoUserLike> userLikesList = PhotoUserLike.getFromPhotoAndUser(
                    userLike.getPhotoId(),
                    userLike.getUserId(), 1, 1); //pagination params: get first with limit 1
            if(userLikesList!=null && userLikesList.size() > 0){
                oldUserLike = userLikesList.get(0);
            }
        }

        boolean photoBeautiful = userLike.getPhotoIsBeautiful();
        boolean photoUseful = userLike.getPhotoIsUseful();

        //these are the values already set by the user in the past, if any
        boolean oldPhotoBeautiful = false;
        boolean oldPhotoUseful = false;

        Logger.info("received 'is beautiful' with value: " + photoBeautiful);
        Logger.info("received 'is useful' with value: " + photoUseful);

        //if there is an older photo 'like', and is different from the current one, update it, otherwise create a new one
        if (oldUserLike != null){
            oldPhotoBeautiful = oldUserLike.getPhotoIsBeautiful();
            oldPhotoUseful = oldUserLike.getPhotoIsUseful();
            //'old' is different from 'new'? update the old then!
            if((oldPhotoBeautiful != photoBeautiful || oldPhotoUseful != photoUseful)){
                oldUserLike.setPhotoIsBeautiful(photoBeautiful);
                oldUserLike.setPhotoIsUseful(photoUseful);
                oldUserLike.save();
                Logger.info("previously was 'useful/beautiful': " + oldPhotoUseful + oldPhotoBeautiful);
                Logger.info("updated with 'useful/beautiful': " + photoUseful + photoBeautiful);
            }
        }else{
            //just create from scratch
            userLike.save();
        }


        {
            //increment the counters in the photo, but just if there is something different

            int incrBeautiful = 0;
            int incrUseful = 0;

            if(photoUseful != oldPhotoUseful){
                incrUseful = (photoUseful ? 1 : -1);
                Logger.info("incremented 'useful' by: " + (photoUseful ? 1 : -1));
            }
            if(photoBeautiful != oldPhotoBeautiful){
                incrBeautiful = (photoBeautiful ? 1 : -1);
                Logger.info("incremented 'beautiful' by: " + (photoBeautiful ? 1 : -1));
            }

            if(incrBeautiful != 0 || incrUseful != 0)
            photo.incrementCountersAndSave(incrUseful, incrBeautiful);
        }

        response().setHeader(
                CONTENT_LOCATION,
                routes.PhotosREST.getPhotoUserLikes(
                        photo.getId().toString(),
                        userLike.getUserId().toString(), 1, 1
                ).toString()
        );

        return ok(userLikeToJson(userLike));
    }

    public static ObjectId stringToObjectId(String id) {
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
    }


    //========================static utils===============//
    private static Photo jsonToPhoto(JsonNode json){

        Photo photo = new Photo();

        //optional fields
        if(json.has(LATITUDE)){
            photo.setLatitude(new Double(json.findPath(LATITUDE).getDoubleValue()));
        }
        if(json.has(LONGITUDE)){
            photo.setLongitude(new Double(json.findPath(LONGITUDE).getDoubleValue()));
        }
        if(json.has(DATE_CREATED)){
            photo.setCreated(new Date(json.findPath(DATE_CREATED).getLongValue()));
        }
        if(json.has(TITLE)){
            photo.setTitle(json.findPath(TITLE).getTextValue());
        }
        if(json.has(DESCRIPTION)){
            photo.setDescription(json.findPath(DESCRIPTION).getTextValue());
        }

        //Date createDate = javax.xml.bind.DatatypeConverter.parseDateTime(json.findPath("date").getTextValue()).getTime();

        if(json.has(ID)){
            ObjectId obj = stringToObjectId(json.findPath(ID).getTextValue());
            photo.setId(obj);
        }
        return photo;

    }

    private static JsonNode photoToJson(Photo photo){
        ObjectNode json = Json.newObject();
        json.put(ID, photo.getId().toString());
        json.put(DATE_CREATED, photo.getCreated().getTime());
        json.put(OWNER_ID, photo.getOwnerId().toString());
        if(photo.getLatitude() != null){
            json.put(LATITUDE, photo.getLatitude());
        }
        if(photo.getLongitude() != null){
            json.put(LONGITUDE, photo.getLongitude());
        }
        if(photo.getTitle() != null){
            json.put(TITLE, photo.getTitle());
        }
        if(photo.getDescription() != null){
            json.put(DESCRIPTION, photo.getDescription());
        }
        json.put(IS_USEFUL_COUNT, photo.getUsefulCount());
        json.put(IS_BEAUTIFUL_COUNT, photo.getBeautifulCount());

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

    private static JsonNode userLikeToJson(PhotoUserLike userLike) {
        ObjectNode json = Json.newObject();
        json.put(USER_ID, userLike.getUserId().toString());

        if(userLike.getPhotoIsUseful() != null){
            json.put(PHOTO_IS_USEFUL, userLike.getPhotoIsUseful() ? 1 : 0);
        }
        if(userLike.getPhotoIsBeautiful() != null){
            json.put(PHOTO_IS_BEAUTIFUL, userLike.getPhotoIsBeautiful() ? 1 : 0);
        }

        return json;
    }

    private static PhotoUserLike jsonToPhotoUserLike(JsonNode json) {

        PhotoUserLike pul = new PhotoUserLike();

        if(json.has(USER_ID)){
            String userId = json.findPath(USER_ID).getTextValue();
            pul.setUserId(stringToObjectId(userId));
        }

        //is not beautiful is value sent is 0, if values is not 0 then is beautiful
        if(json.has(PHOTO_IS_BEAUTIFUL)){
            boolean beautiful = json.findPath(PHOTO_IS_BEAUTIFUL).getDoubleValue() == 0.0 ? false : true;
            pul.setPhotoIsBeautiful(beautiful);
        }

        if(json.has(PHOTO_IS_USEFUL)){
            boolean useful = json.findPath(PHOTO_IS_USEFUL).getDoubleValue() == 0.0 ? false : true;
            pul.setPhotoIsUseful(useful);
        }

        return pul;
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
