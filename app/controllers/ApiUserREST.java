package controllers;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;

import play.libs.Json;
import play.mvc.Result;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import providers.MyUsernamePasswordAuthProvider;

import play.data.format.Formats;
import java.util.*;


import models.ApiAppUser;
import models.User;

import providers.MyUsernamePasswordAuthUser;
import providers.MyLoginUsernamePasswordAuthUser;


public class ApiUserREST extends ItemREST {

	public static Result login(String email, String password) {
		//TODO
		//This is a mokc function without authentication	
		
		ApiAppUser user = findUserByEmail(email);
		if (user == null) {
			return badRequest("no email found");
		}


		JsonFactory factory = new JsonFactory();
		ObjectMapper om = new ObjectMapper(factory);
		factory.setCodec(om);
		ObjectNode node = om.createObjectNode();
		node.put("session_tokken", user.sessionToken);
		
		return ok(node);
	}

	public static ApiAppUser findById(String id) {
		return MorphiaObject.datastore.get(ApiAppUser.class, new ObjectId(id));
	}

	public static ApiAppUser findById(ObjectId oid) {
		return MorphiaObject.datastore.get(ApiAppUser.class, oid);
	}

	public static ApiAppUser findUserByEmail(String email){
		System.out.println(email);
		User  user=  User.findByEmail(email);
		System.out.println(user.getIdentifier());

		return new ApiAppUser(new ObjectId(user.getIdentifier()));
	}
	public static ApiAppUser findByToken(String token) {
		ApiAppUser user = MorphiaObject.datastore.find(ApiAppUser.class)
											.field("sessionToken").equal(token).get();
		if (user.logged())
			return user;

		return null;
	}

	
	

}