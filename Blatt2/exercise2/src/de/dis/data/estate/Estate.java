package de.dis.data.estate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dis.data.DbConnectionManager;

public abstract class Estate {

    private int id = -1;
    private String city;
    private String postalCode;
    private String street;
    private String streetNumber;
    private double squareArea;
    private int agentId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public double getSquareArea() {
        return squareArea;
    }

    public void setSquareArea(double squareArea) {
        this.squareArea = squareArea;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }


    public static String getAllIdsFormatted(String table){
		return getAllIds(table).stream().map(String::valueOf).collect(Collectors.joining(", \n"));  // Join with comma and space
	}

    public static List<Integer> getAllIds(String table) {
		List<Integer> ids = new ArrayList<>();
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();
			String sql = "SELECT id FROM " + table;
			PreparedStatement pstmt = con.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				ids.add(rs.getInt("id"));
			}

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}

    public static void deleteEstate(int id) {
        Connection con = DbConnectionManager.getInstance().getConnection();

        // Delete on cascade, so we only need to delete from House table
        try {
            String deleteSQL = "DELETE FROM estate WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(deleteSQL);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}