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

import play.data.validation.Constraints.*;
import play.db.DB;
import play.libs.Json;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

@Entity
public class Photo {

	private static final String IS_BEAUTIFUL_COUNT = "is_beautiful_count";
	private static final String IS_USEFUL_COUNT = "is_useful_count";
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


	/**
	 * Save ONLY in MONGO
	 * @return
	 */
	public ObjectId save() {
		//save all Contents that are not saved yet
		for (Content c : photoContents) {
			if (c.getId() == null) {
				c.save();
			}
		}

		MorphiaObject.datastore.save(this);
		return this.getId();
	}


	/**
	 * Save in MONGO and POSTGIS
	 * @return
	 */
	public ObjectId geosave() {

		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;

		// FIXME FILL THIS MAP!!
		LinkedHashMap<String, String> tags = new LinkedHashMap<String, String>();

		// POSTGIS
		try {
			conn = ds.getConnection();

			// Try updating, if the photo doesnt exists, the query does nothing
			String sql = "update photos set mongo_oid = ?, ranking = ?, timest = ?, " +
					"location = ST_Transform(ST_SetSRID(?,4326),900913)" + 
					(tags != null? ", tags = " + OsmFeature.tagsToHstoreFormat(tags) : "" ) +
					" where id = ?";
			st = conn.prepareStatement(sql);
			st.setString(1, this.id.toString());
			st.setInt(2, this.getBeautifulCount());
			st.setDate(3, new java.sql.Date(created.getTime()));
			st.setString(4, "ST_MakePoint(" + this.getLongitude() + "," + this.getLatitude() + ")");
			st.executeUpdate();

			// Try inserting, if the photo exists, the query does nothing
			sql = "insert into photos (mongo_oid, ranking, timest, geom " + 
					(tags != null? ",tags" : "" ) + ") " +
					"select ?, ?, ?, ST_Transform(ST_SetSRID(?,4326),900913) " + 
					(tags != null? ", " + OsmFeature.tagsToHstoreFormat(tags) : "" ) + " " +
					"where not exists (select 1 from photos where mongo_oid = ?)";
			st = conn.prepareStatement(sql);
			st.setString(1, this.id.toString());
			st.setInt(2, this.getBeautifulCount());
			st.setDate(3, new java.sql.Date(created.getTime()));
			st.setString(4, "ST_MakePoint(" + this.getLongitude() + "," + this.getLatitude() + ")");
			st.setString(5, this.id.toString());
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

		// MONGO
		return this.save();
	}


	public static Photo findById(ObjectId obj) {
		return MorphiaObject.datastore.get(Photo.class, obj);
	}


	/** Get the amount limit tells of photos near the given location
	 * 
	 */
	public static List<Photo> findByLocation(Double lon, Double lat, int limit) {

		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		List<Photo> photos = new ArrayList<Photo>();
		Photo photo = null;

		try {
			conn = ds.getConnection();
			String sql = "select mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(location, 900913),4326)) as geometry " +
					"from photos ORDER BY location <-> ST_Transform(ST_SetSRID(?,4326),900913), ranking DESC LIMIT ?";
			st = conn.prepareStatement(sql);
			st.setString(1, "ST_MakePoint(" + lon + "," + lat + ")");
			st.setInt(2, limit);
			rs = st.executeQuery();

			while (rs.next()) {
				ObjectId oid = new ObjectId(rs.getString("mongo_oid"));
				if (oid != null){
					photo = Photo.findById(oid);
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
	
	
	/** Get the amount limit tells of photos near the given location but only for the given ranking
	 * 
	 */
	public static List<Photo> findByLocationAndRanking(Double lon, Double lat, int ranking, int limit) {

		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		List<Photo> photos = new ArrayList<Photo>();
		Photo photo = null;

		try {
			conn = ds.getConnection();
			String sql = "select mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(location, 900913),4326)) as geometry " +
					"from photos WHERE ranking = ? ORDER BY location <-> ST_Transform(ST_SetSRID(?,4326),900913) LIMIT ?";
			st = conn.prepareStatement(sql);
			st.setInt(1, ranking);
			st.setString(2, "ST_MakePoint(" + lon + "," + lat + ")");
			st.setInt(3, limit);
			rs = st.executeQuery();

			while (rs.next()) {
				ObjectId oid = new ObjectId(rs.getString("mongo_oid"));
				if (oid != null){
					photo = Photo.findById(oid);
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
	public static List<Photo> findByLocationAndKeyValue(Double lon, Double lat, String key, String value, int limit) {

		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		List<Photo> photos = new ArrayList<Photo>();
		Photo photo = null;

		try {
			conn = ds.getConnection();
			String sql = "select mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(location, 900913),4326)) as geometry " +
					"from photos WHERE lower(tags->lower(?)) = lower(?) ORDER BY location <-> ST_Transform(ST_SetSRID(?,4326),900913), ranking DESC LIMIT ?";
			st = conn.prepareStatement(sql);
			st.setString(1, key);
			st.setString(2, value);
			st.setString(3, "ST_MakePoint(" + lon + "," + lat + ")");
			st.setInt(4, limit);
			rs = st.executeQuery();

			while (rs.next()) {
				ObjectId oid = new ObjectId(rs.getString("mongo_oid"));
				if (oid != null){
					photo = Photo.findById(oid);
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
	
	
	public static List<Photo> findByIntersection(JsonNode geometry, int limit) {

		// Expected GEOJson geometry
		
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		ResultSet rs;

		List<Photo> photos = new ArrayList<Photo>();
		Photo photo = null;

		try {
			conn = ds.getConnection();
			String sql = "select mongo_oid, st_asgeojson(ST_Transform(ST_SetSRID(location, 900913),4326)) as geometry " +
					"from photos where ST_Intersects(location, ST_Transform(ST_SetSRID(st_geomfromgeojson(?),4326),900913)) ORDER BY ranking DESC LIMIT ?";
			st = conn.prepareStatement(sql);
			st.setString(1, Json.stringify(geometry));
			st.setInt(2, limit);
			rs = st.executeQuery();

			while (rs.next()) {
				ObjectId oid = new ObjectId(rs.getString("mongo_oid"));
				if (oid != null){
					photo = Photo.findById(oid);
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

	/**
	 * Delete from Mongo and PostGIS
	 */
	public void delete() {

		// POSTGIS
		DataSource ds = DB.getDataSource();
		Connection conn = null;
		PreparedStatement st;
		try {
			conn = ds.getConnection();
			String sql = "delete from photos where id = ?";
			st = conn.prepareStatement(sql);
			st.setString(1, this.id.toString());
			st.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// MONGO
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

}



