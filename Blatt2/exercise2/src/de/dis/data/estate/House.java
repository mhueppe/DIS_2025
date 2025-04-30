package de.dis.data.estate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dis.data.DbConnectionManager;

public class House extends Estate {

    private int floors;
    private double price;
    private boolean garden;

    public int getFloors() {
        return floors;
    }

    public void setFloors(int floors) {
        this.floors = floors;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean hasGarden() {
        return garden;
    }

    public void setGarden(boolean garden) {
        this.garden = garden;
    }

    public String toString() {
        return "House [id=" + getId() + ", floors=" + floors + ", price=" + price + ", garden=" + garden + ", city="
                + getCity() + ", postalCode=" + getPostalCode() + ", street=" + getStreet() + ", streetNumber="
                + getStreetNumber() + ", squareArea=" + getSquareArea() + "]";
    }

    public void save() {

        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            // Füge neues Element hinzu, wenn das Objekt noch keine ID hat.
            if (getId() == -1) {
                // Insert into Estate table
                String estateSQL = "INSERT INTO Estate (city, postal_code, street, street_number, square_area, agent_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement estateStmt = con.prepareStatement(estateSQL, PreparedStatement.RETURN_GENERATED_KEYS);
                estateStmt.setString(1, getCity());
                estateStmt.setString(2, getPostalCode());
                estateStmt.setString(3, getStreet());
                estateStmt.setString(4, getStreetNumber());
                estateStmt.setDouble(5, getSquareArea());
                estateStmt.setDouble(6, getAgentId());
                estateStmt.executeUpdate();

                // Hole die Id des eingefügten Datensatzes
                ResultSet estateRs = estateStmt.getGeneratedKeys();
                if (estateRs.next()) {
                    setId(estateRs.getInt(1));
                }

                // Insert into House table
                String houseSQL = "INSERT INTO House (estate_id, floors, price, garden) VALUES (?, ?, ?, ?)";
                PreparedStatement houseStmt = con.prepareStatement(houseSQL);
                houseStmt.setInt(1, getId());
                houseStmt.setInt(2, getFloors());
                houseStmt.setDouble(3, getPrice());
                houseStmt.setBoolean(4, hasGarden());
                houseStmt.executeUpdate();
                houseStmt.close();
                estateStmt.close();

            } else {
                // Update Estate table
                String estateSQL = "UPDATE Estate SET city = ?, postal_code = ?, street = ?, street_number = ?, square_area = ? WHERE id = ?";
                PreparedStatement estateStmt = con.prepareStatement(estateSQL);
                estateStmt.setString(1, getCity());
                estateStmt.setString(2, getPostalCode());
                estateStmt.setString(3, getStreet());
                estateStmt.setString(4, getStreetNumber());
                estateStmt.setDouble(5, getSquareArea());
                estateStmt.setInt(6, getId());
                estateStmt.executeUpdate();

                // Update House table
                String houseSQL = "UPDATE House SET floors = ?, price = ?, garden = ? WHERE estate_id = ?";
                PreparedStatement houseStmt = con.prepareStatement(houseSQL);
                houseStmt.setInt(1, getFloors());
                houseStmt.setDouble(2, getPrice());
                houseStmt.setBoolean(3, hasGarden());
                houseStmt.setInt(4, getId());
                houseStmt.executeUpdate();
                houseStmt.close();
                estateStmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static House getHouse(int id) {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            // Use JOIN to combine Estate and House tables
            String selectSQL = "SELECT e.*, h.floors, h.price, h.garden " +
                    "FROM Estate e " +
                    "JOIN House h ON e.id = h.estate_id " +
                    "WHERE e.id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                House h = new House();
                h.setId(rs.getInt("id"));
                h.setCity(rs.getString("city"));
                h.setPostalCode(rs.getString("postal_code"));
                h.setStreet(rs.getString("street"));
                h.setStreetNumber(rs.getString("street_number"));
                h.setSquareArea(rs.getDouble("square_area"));
                h.setFloors(rs.getInt("floors"));
                h.setPrice(rs.getDouble("price"));
                h.setGarden(rs.getBoolean("garden"));

                rs.close();
                pstmt.close();
                return h;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void delete(int id) {
        deleteEstate(id);
        Connection con = DbConnectionManager.getInstance().getConnection();

        // Delete on cascade, so we only need to delete from House table
        try {
            String deleteSQL = "DELETE FROM House WHERE Estate_ID = ?";
            PreparedStatement pstmt = con.prepareStatement(deleteSQL);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static House[] getAllHouses() {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            String selectSQL = "SELECT e.*, h.Floors, h.Price, h.Garden " +
                    "FROM Estate e " +
                    "JOIN House h ON e.ID = h.Estate_ID";
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(selectSQL);

            // Zähle die Anzahl der Datensätze
            rs.last();
            int count = rs.getRow();
            rs.beforeFirst();

            // Erzeuge Array mit House-Objekten
            House[] houses = new House[count];
            int i = 0;
            while (rs.next()) {
                House h = new House();
                h.setId(rs.getInt("id"));
                h.setCity(rs.getString("city"));
                h.setPostalCode(rs.getString("postal_code"));
                h.setStreet(rs.getString("street"));
                h.setStreetNumber(rs.getString("street_number"));
                h.setSquareArea(rs.getDouble("square_area"));
                h.setFloors(rs.getInt("floors"));
                h.setPrice(rs.getDouble("price"));
                h.setGarden(rs.getBoolean("garden"));

                houses[i++] = h;
            }

            rs.close();
            stmt.close();
            return houses;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void listAll() {
        House[] houses = getAllHouses();
        if (houses != null && houses.length > 0) {
            for (House house : houses) {
                System.out.println(house);
            }
        } else {
            System.out.println("No Houses found.");
        }
    }

    public static List<Integer> getAllIds() {
		return getAllIds("house");
	}

    public static String getAllIdsFormatted(String table){
		return getAllIdsFormatted("house");
	}

	public static boolean deleteById(int id) {
		if (!exists(id)){
			return false;
		}
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();
			String deleteSQL = "DELETE FROM house WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(deleteSQL);
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			pstmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
    public static boolean exists(int id) {
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();
			String checkSQL = "SELECT 1 FROM house WHERE id = ? LIMIT 1";
			PreparedStatement pstmt = con.prepareStatement(checkSQL);
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
	
			boolean found = rs.next(); // true if a row exists
	
			rs.close();
			pstmt.close();
	
			return found;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}