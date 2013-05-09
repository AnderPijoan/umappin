package models;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;

import play.db.*;
import play.libs.Json;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MapFeature {

	private static final long serialVersionUID = 1L;

    public Long id;
    public String ownerId ;
    public String name;
    public JsonNode geometry;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNode getGeometry() {
        return geometry;
    }

    public void setGeometry(JsonNode geometry) {
        this.geometry = geometry;
    }


    // GET /features
    public static List<MapFeature> all() {
        DataSource ds = DB.getDataSource();
        Connection conn = null;
        PreparedStatement st;
        ResultSet rs;
        List<MapFeature> list = new ArrayList<MapFeature>();
        try {
            conn = ds.getConnection();
            String sql = "select id, ownerid, name, st_asgeojson(geometry) as geometry from mapfeature";
            st = conn.prepareStatement(sql);
            rs = st.executeQuery();
            while (rs.next()) {
                MapFeature ft = new MapFeature();
                ft.id = rs.getLong("id");
                ft.ownerId =  rs.getString("ownerid");
                ft.name =  rs.getString("name");
                ft.geometry =  Json.parse(rs.getString("geometry"));
                list.add(ft);
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
        return list;
    }

    // GET /features/{id}
    public static MapFeature findById(String id) {
        DataSource ds = DB.getDataSource();
        Connection conn = null;
        PreparedStatement st;
        ResultSet rs;
        MapFeature ft = null;
        try {
            conn = ds.getConnection();
            String sql = "select id, ownerid, name, st_asgeojson(geometry) as geometry from mapfeature where id = ?";
            st = conn.prepareStatement(sql);
            st.setLong(1, Long.parseLong(id));
            rs = st.executeQuery();
            if (rs.next()) {
                ft = new MapFeature();
                ft.id = rs.getLong("id");
                ft.ownerId =  rs.getString("ownerid");
                ft.name =  rs.getString("name");
                ft.geometry =  Json.parse(rs.getString("geometry"));
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
        return ft;
    }

    // POST /features
    public MapFeature save() {
        DataSource ds = DB.getDataSource();
        Connection conn = null;
        PreparedStatement st;
        ResultSet rs;
        try {
            conn = ds.getConnection();
            String sql = "insert into mapfeature (id, ownerid, name, geometry) " +
                         "values (DEFAULT, ?, ?, st_geomfromgeojson(?)) returning id";
            st = conn.prepareStatement(sql);
            st.setString(1, this.ownerId);
            st.setString(2, this.name);
            st.setString(3, Json.stringify(this.geometry));
            rs = st.executeQuery();
            if (rs.next())
                this.id = rs.getLong("id");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
        return this.id == null ? null : this;
    }

    // PUT /features/{id}
    public MapFeature update(MapFeature other) {
        DataSource ds = DB.getDataSource();
        Connection conn = null;
        PreparedStatement st;
        ResultSet rs;
        try {
            conn = ds.getConnection();
            String sql = "update mapfeature set ownerid = ?, name = ?, geometry = st_geomfromgeojson(?) " +
                         "where id = ? returning id";
            st = conn.prepareStatement(sql);
            st.setString(1, other.ownerId);
            st.setString(2, other.name);
            st.setString(3, Json.stringify(other.geometry));
            st.setLong(4, this.id);
            rs = st.executeQuery();
            if (rs.next())
                other.id = this.id;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return other.id == null ? null : other;
    }

    // DELETE /features/{id}
    public MapFeature delete() {
        DataSource ds = DB.getDataSource();
        Connection conn = null;
        PreparedStatement st;
        ResultSet rs;
        try {
            conn = ds.getConnection();
            String sql = "delete from mapfeature where id = ? returning id";
            st = conn.prepareStatement(sql);
            st.setLong(1, this.id);
            rs = st.executeQuery();
            if (rs.next())
                return this;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    // JSON handling functions
    public static JsonNode listToJson(List<MapFeature> list) {
        ArrayNode json = new ArrayNode(JsonNodeFactory.instance);
        for (MapFeature mf : list)
            json.add(mf.toJson());
        return json;
    }

    public JsonNode toJson() { return Json.toJson(this); }

    public static MapFeature fromJson(JsonNode json) { return Json.fromJson(json, MapFeature.class); }

}
