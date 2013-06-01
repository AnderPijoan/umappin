package models;

//import play.modules.morphia.Model;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.annotations.*;


import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import controllers.MorphiaObject;
import models.osm.OsmFeature;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints.*;
import play.db.DB;
import play.libs.Json;
import play.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;


@Entity
public class Photo {

	private static final String IS_BEAUTIFUL_COUNT = "is_beautiful_count";
	private static final String IS_USEFUL_COUNT = "is_useful_count";
	private static final String RANKING = "ranking";

	private static final int OPEN_LAYERS_SRID = 4326;
	private static final int OSM_SRID = 900913;

	public static final  int MAX_RESULTS_RETURNED = 20;

	@Id
	private ObjectId id;

	@Required
	@Embedded("owner_id")
	private ObjectId ownerId;

	@Reference(lazy=true)
	private Set<Content> photoContents = new HashSet<Content>();

	@Embedded("title")
	private String title;

	@Embedded("description")
	private String description;

	@Embedded("latitude")
	private Double latitude;

	@Embedded("longitude")
	private Double longitude;

	@Embedded("date_created")
	private Date created;

	@Embedded(IS_BEAUTIFUL_COUNT)
	private int isBeautifulCount;

	@Embedded(IS_USEFUL_COUNT)
	private int isUsefulCount;

	@Embedded(RANKING)
	private int ranking;

	private java.util.Map<String, String> tags;

	public static Photo findById(ObjectId obj) {
		return MorphiaObject.datastore.get(Photo.class, obj);
	}

	public int getBeautifulCount() {
		return isBeautifulCount;
	}

	public void setBeautifulCount(int beautifulCount) {
		isBeautifulCount = beautifulCount;
	}


	public int getUsefulCount() {
		return isUsefulCount;
	}

	public void setUsefulCount(int usefulCount) {
		isUsefulCount = usefulCount;
	}



	public void setId(ObjectId id) {
		this.id = id;
	}

	public ObjectId getId() {
		return id;
	}


	public ObjectId getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(ObjectId ownerId) {
		this.ownerId = ownerId;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}


	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Content> getPhotoContents() {
		return photoContents;
	}

	public void setPhotoContents(Set<Content> photoContents) {
		this.photoContents = photoContents;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}


	/**
	 * Saves in MONGO and the {@code GeoPoint} info is replicated in PostGIS (inserted/updated/deleted)
	 * @return
	 */
	public ObjectId save() {
		//save all Contents that are not saved yet
		for (Content c : photoContents) {
			if (c.getId() == null) {
				c.save();
			}
		}

		//save in morphia first; we have to ensure it has an id, otherwise
		//cannot be saved in GIS
		MorphiaObject.datastore.save(this);

		//check whether we have a longitude AND latitude and has content,
		//if so, save in PostGIS
		if(longitude != null && latitude != null && photoContents.size() > 0){
			Logger.info("GeoPoint attempting to insert/update point for photo " + this.getId().toString());
			GeoPoint.geosave(this);
		}
		else {
			//if not, try to delete the potential postGis point associated to the photo
			Logger.info("GeoPoint attempting to delete any point corresponding to photo " + this.getId().toString());
			GeoPoint.geoDelete(this);
		}

		return this.getId();
	}


	public void addUpdateContent(File f, String mimeType) throws IOException {
		Content c = new Content(f);
		c.setMimeType(mimeType);
		cleanUpExistingContents();
		photoContents.add(c);
	}
	public void addUpdateContent(byte[] bytes, String mimeType){
		Content c = new Content(bytes);
		c.setMimeType(mimeType);
		cleanUpExistingContents();
		photoContents.add(c);
	}


	private void cleanUpExistingContents(){
		for(Content c : photoContents) {
			photoContents.remove(c);
			c.delete();
		}

	}


	public void delete() {
		GeoPoint.geoDelete(this);
		this.cleanUpExistingContents();
		MorphiaObject.datastore.delete(this);

	}

	public void incrementCountersAndSave(int useful, int beautiful) {
		Datastore ds = MorphiaObject.datastore;

		Query<Photo> updateQuery = ds.createQuery(Photo.class)
				.field("_id").equal(this.getId());

		UpdateOperations<Photo> ops = ds.createUpdateOperations(Photo.class)
				.inc(IS_USEFUL_COUNT, useful)
				.inc(IS_BEAUTIFUL_COUNT, beautiful);
		//note that 'update' gives a compile time error (overloaded
		// 'update' methods are not correctly resolved by the compiler)
		// so we use the 'updateFirst' version, that is fine, given that we
		// expect one result only
		ds.updateFirst(updateQuery, ops);


		//here there is slight chance that the in-memory model (this.isUsefulCount)
		// gets out of sync
		// if the current value of isBeautifulCount is stale (updated concurrently
		// by another user)
		// The update however is atomic (so in the db it is always correct)
		this.isUsefulCount += useful;
		this.isBeautifulCount += beautiful;
	}

	public static void deleteUserLikesForPhoto(Photo photo) {
		Query q = MorphiaObject.datastore.createQuery(PhotoUserLike.class)
				.field(PhotoUserLike.PHOTO_ID)
				.equal(photo.getId());
		MorphiaObject.datastore.delete(q);

	}


	public static List<Photo> findByPoligonAndTags(long[][][] polygon, int limit, int offset, java.util.Map<String, String> tags){

		if(limit > MAX_RESULTS_RETURNED){
			throw new IllegalArgumentException("can't query more than " + MAX_RESULTS_RETURNED + " records");
		}

		if(limit == 0){
			limit = MAX_RESULTS_RETURNED;
		}

		// create a polygon json, see polygon example at http://www.geojson.org/geojson-spec.html#id4
		// for instance the convex polygon
		//
		//		{ "type": "Polygon",
		//				"coordinates": [
		//			[ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
		//			]
		//		}
		ObjectNode json = Json.newObject();
		try {
			json.put("type", "Polygon");
			ArrayNode polygonJson = json.putArray("coordinates");
			//nested array, one for each closed route; realisticly there will be just one, the outer boundary
			for(long[][] boundary : polygon){
				ArrayNode boundaryJson = polygonJson.addArray();
				for(long[] point : boundary){
					ArrayNode pointJson = boundaryJson.addArray();
					for(long coordinate : point){
						pointJson.add(coordinate);
					}
				}
			}
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(
					"array must be long[][][] and no element or nested array can be null: "
					+ polygon != null ? polygon.toString() : "[]");
		}

		Logger.info("converted array " + polygon.toString() + " to geoJson " + Json.stringify(json));

		Set<ObjectId> objIds = GeoPoint.findByIntersection(json, limit, offset, tags);

		List<Photo> photos = MorphiaObject.datastore.createQuery(Photo.class)
				.field("_id")
				.in(objIds)
				.order("-" + RANKING).asList();

		return photos;

	}


	@Entity("Photo_Content")
	public static class Content{

		public void setId(ObjectId id) {
			this.id = id;
		}

		@Id
		private ObjectId id;

		@Embedded("x_size")
		private int xSize;

		@Embedded("y_size")
		private int ySize;

		@Embedded("mime_type")
		private String mimeType;

		@Embedded("file_bytes")
		private byte[] fileBytes;

		public String getMimeType() {
			return mimeType;
		}

		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		public int getxSize() {
			return xSize;
		}

		public void setxSize(int xSize) {
			this.xSize = xSize;
		}

		public int getySize() {
			return ySize;
		}

		public void setySize(int ySize) {
			this.ySize = ySize;
		}

		public byte[] getFileBytes() {
			return fileBytes;
		}


		public void setFileBytes(byte[] fileBytes) {
			this.fileBytes = fileBytes;
		}

		@Override
		public boolean equals(Object obj) {
			//TODO two contents are the same if they point at the same database address
			//I am not sure Morphia already supports this behaviour, I couldn't find this out on the internet
			if(!(obj instanceof Content)){
				return false;
			}

			//in general this won't  work if the Content is in a different Mongo Instance
			return this.id.equals(((Content) obj).getId());
		}


		//needed for Morphia deserialilization
		private Content() {}

		//only an instance of photo can create Content.
		private Content(File file) throws IOException {
			InputStream ios = new FileInputStream(file);
			//this.fileBytes = IOUtils.toByteArray(ios);


			this.fileBytes = inputStreamToByteArray(ios);
			//this.fileBytes = org.apache.commons.io.IOUtils.toByteArray(ios);


		}

		private Content(byte[] bytes){
			this.fileBytes = bytes;
		}



		private byte[] inputStreamToByteArray(InputStream inStream) throws IOException {
			ByteArrayOutputStream biteOutputStr = new ByteArrayOutputStream();
			try {
				byte[] buffer = new byte[524288];
				int bytesRead;
				while ((bytesRead = inStream.read(buffer)) > 0) {
					biteOutputStr.write(buffer, 0, bytesRead);
				}
				return biteOutputStr.toByteArray();
			} finally {
				inStream.close();
				biteOutputStr.close();

			}
		}


		public void delete() {
			MorphiaObject.datastore.delete(Content.class, this.getId());
		}

		public void save(){
			MorphiaObject.datastore.save(this);
		}


		public ObjectId getId() {
			return id;
		}
	}

	private static class GeoPoint {


		/**
		 * Saves in POSTGIS only
		 * @return
		 */
		private static void geosave(Photo photo) {

			if(photo.getId() == null){
				throw new IllegalStateException("photo location cannot be saved, it lacks the id");
			}
			if(photo.getLongitude() == null || photo.getLatitude() == null || photo.getPhotoContents().size() == 0){
				throw new IllegalArgumentException("photo location cannot be saved, latitude or longitude or Photo.Content is empty");
			}

			DataSource ds = DB.getDataSource();
			Connection conn = null;
			PreparedStatement st;

			LinkedHashMap<String, String> tags = null;
			if(photo.tags != null){
				tags = new LinkedHashMap(photo.tags);
			}

			try {
				conn = ds.getConnection();
				// Try updating, if the photo doesn't exists, the query does nothing
				String sql = "update photos set ranking = ?, timest = ?, " +
					"location = ST_Transform(ST_SetSRID(ST_MakePoint(?, ?), " + OPEN_LAYERS_SRID + ")," + OSM_SRID + ")" +
					(tags != null && tags.size() > 0 ? ", tags = " + OsmFeature.tagsToHstoreFormat(tags) : "" ) +
					" where mongo_oid = ?";
				st = conn.prepareStatement(sql);
				st.setInt(1, photo.getRanking());
				st.setDate(2, new java.sql.Date(new Date().getTime()));
				st.setDouble(3, photo.getLongitude());
				st.setDouble(4, photo.getLatitude());
				st.setString(5, photo.getId().toString());
				st.executeUpdate();

				// Try inserting, if the photo exists, the query does nothing
				sql = "insert into photos (mongo_oid, ranking, timest, location " +
					(tags != null? ",tags" : "" ) + ") " +
					"select ?, ?, ?, ST_Transform(ST_SetSRID(ST_MakePoint(?, ?), " + OPEN_LAYERS_SRID + ")," + OSM_SRID + ") " +
					 (tags != null && tags.size() > 0 ? ", tags = " + OsmFeature.tagsToHstoreFormat(tags) : "" ) +
					"where not exists (select 1 from photos where mongo_oid = ?)";
				st = conn.prepareStatement(sql);
				st.setString(1, photo.getId().toString());
				st.setInt(2, photo.getRanking());
				st.setDate(3, new java.sql.Date(new Date().getTime()));
				st.setDouble(4, photo.getLongitude());
				st.setDouble(5, photo.getLatitude());
				st.setString(6, photo.getId().toString());
				st.executeUpdate();

				Logger.info("GeoPoint inserted/updated point for photo " + photo.getId().toString());

			} catch (SQLException e) {
				e.printStackTrace();
				Logger.error(
						"GeoPoint: exception when saving location to PostGIS; skipping arror and going aheade with Photo save: "
						+ e.getMessage());
			} finally {
				if (conn != null) try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}

		/** Get the amount limit tells of photos near the given location
		 *
		 */
		private static Set<ObjectId> findByLocation(Double lon, Double lat, int limit) {

			DataSource ds = DB.getDataSource();
			Connection conn = null;
			PreparedStatement st;
			ResultSet rs;

			List<Photo> photos = new ArrayList<Photo>();
			Photo photo = null;

			Set<ObjectId> oids = new HashSet<>();

			try {
				conn = ds.getConnection();
				String sql = "select mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(location, " + OSM_SRID + ")," + OPEN_LAYERS_SRID + ")) as geometry " +
				"from photos ORDER BY location <-> ST_Transform(ST_SetSRID(?," + OPEN_LAYERS_SRID + ")," + OSM_SRID + "), ranking DESC LIMIT ?";
				st = conn.prepareStatement(sql);
				st.setString(1, "ST_MakePoint(" + lon + "," + lat + ")");
				st.setInt(2, limit);
				rs = st.executeQuery();

				while (rs.next()) {
					ObjectId oid = new ObjectId(rs.getString("mongo_oid"));

					if (oid != null){
						oids.add(oid);
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return oids;
		}

		/** Get the amount limit tells of photos near the given location but only for the given ranking
		 *
		 */
		private static List<Photo> findByLocationAndRanking(Double lon, Double lat, int ranking, int limit) {

			DataSource ds = DB.getDataSource();
			Connection conn = null;
			PreparedStatement st;
			ResultSet rs;

			List<Photo> photos = new ArrayList<Photo>();
			Photo photo = null;

			try {
				conn = ds.getConnection();
				String sql = "select mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(location, " + OSM_SRID + ")," + OPEN_LAYERS_SRID + ")) as geometry " +
				"from photos WHERE ranking = ? ORDER BY location <-> ST_Transform(ST_SetSRID(?," + OPEN_LAYERS_SRID + ")," + OSM_SRID + ") LIMIT ?";
				st = conn.prepareStatement(sql);
				st.setInt(1, ranking);
				st.setString(2, "ST_MakePoint(" + lon + "," + lat + ")");
				st.setInt(3, limit);
				rs = st.executeQuery();

				while (rs.next()) {
					ObjectId oid = new ObjectId(rs.getString("mongo_oid"));
					if (oid != null){

						photo = findById(oid);
						if (photo != null){
							photos.add(photo);
							// TODO maybe you would like to update the photos geometry
						}
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return photos;
		}

		/** Get the amount limit tells of photos near the given location for the given key and value
		 *
		 */
		private static List<Photo> findByLocationAndKeyValue(Double lon, Double lat, String key, String value, int limit) {

			DataSource ds = DB.getDataSource();
			Connection conn = null;
			PreparedStatement st;
			ResultSet rs;

			List<Photo> photos = new ArrayList<Photo>();
			Photo photo = null;

			try {
				conn = ds.getConnection();
				String sql = "select mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(location, " + OSM_SRID + ")," + OPEN_LAYERS_SRID + ")) as geometry " +
				"from photos WHERE lower(tags->lower(?)) = lower(?) ORDER BY location <-> ST_Transform(ST_SetSRID(?," + OPEN_LAYERS_SRID + ")," + OSM_SRID + "), ranking DESC LIMIT ?";
				st = conn.prepareStatement(sql);
				st.setString(1, key);
				st.setString(2, value);
				st.setString(3, "ST_MakePoint(" + lon + "," + lat + ")");
				st.setInt(4, limit);
				rs = st.executeQuery();

				while (rs.next()) {
					ObjectId oid = new ObjectId(rs.getString("mongo_oid"));
					if (oid != null){
						photo = findById(oid);
						if (photo != null){
							photos.add(photo);
							// TODO maybe you would like to update the photos geometry
						}
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return photos;
		}

		private static Set<ObjectId> findByIntersection(JsonNode geometry, int limit, int offset, java.util.Map<String, String> tags) {

			// Expected GEOJson geometry

			DataSource ds = DB.getDataSource();
			Connection conn = null;
			PreparedStatement st;
			ResultSet rs;

			//convert to a suitable type
			LinkedHashMap<String, String> osmTags = null;
			if(tags != null && tags.size() > 0) {
				osmTags = new LinkedHashMap<String, String>(tags);
			}

			Set<ObjectId> oids = new HashSet<>();

			try {
				conn = ds.getConnection();
				String sql = "select mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(location, " + OSM_SRID + ")," + OPEN_LAYERS_SRID + ")) as geometry " +
				"from photos where ST_Intersects(location, ST_Transform(ST_SetSRID(st_geomfromgeojson(?)," + OPEN_LAYERS_SRID + ")," + OSM_SRID + ")) " +
						(osmTags != null ? " and tags = " + OsmFeature.tagsToHstoreFormat(osmTags) : "" ) +
						" ORDER BY ranking DESC LIMIT ? OFFSET ?";
				st = conn.prepareStatement(sql);
				st.setString(1, Json.stringify(geometry));
				st.setInt(2, limit);
				st.setInt(3, offset);
				rs = st.executeQuery();

				while (rs.next()) {
					ObjectId oid = new ObjectId(rs.getString("mongo_oid"));
					if (oid != null){
						oids.add(oid);
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return oids;
		}

		private static void geoDelete(Photo photo){

			DataSource ds = DB.getDataSource();
			Connection conn = null;
			PreparedStatement st;
			try {
				conn = ds.getConnection();
				String sql = "delete from photos where mongo_oid = ?";
				st = conn.prepareStatement(sql);
				st.setString(1, photo.getId().toString());
				st.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null) try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

}



