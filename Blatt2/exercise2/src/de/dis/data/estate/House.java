package de.dis.data.estate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
                String estateSQL = "INSERT INTO Estate (City, Postal_Code, Street, Street_Number, Square_Area, Agent_ID) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement estateStmt = con.prepareStatement(estateSQL, PreparedStatement.RETURN_GENERATED_KEYS);
                estateStmt.setString(1, getCity());
                estateStmt.setString(2, getPostalCode());
                estateStmt.setString(3, getStreet());
                estateStmt.setString(4, getStreetNumber());
                estateStmt.setDouble(5, getSquareArea());
                estateStmt.setInt(6, getAgentId());
                estateStmt.executeUpdate();

                // Hole die Id des eingefügten Datensatzes
                ResultSet estateRs = estateStmt.getGeneratedKeys();
                if (estateRs.next()) {
                    setId(estateRs.getInt(1));
                }

                // Insert into House table
                String houseSQL = "INSERT INTO House (Estate_ID, Floors, Price, Garden) VALUES (?, ?, ?, ?)";
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
                String estateSQL = "UPDATE Estate SET City = ?, Postal_Code = ?, Street = ?, Street_Number = ?, Square_Area = ? WHERE ID = ?";
                PreparedStatement estateStmt = con.prepareStatement(estateSQL);
                estateStmt.setString(1, getCity());
                estateStmt.setString(2, getPostalCode());
                estateStmt.setString(3, getStreet());
                estateStmt.setString(4, getStreetNumber());
                estateStmt.setDouble(5, getSquareArea());
                estateStmt.setInt(6, getId());
                estateStmt.executeUpdate();

                // Update House table
                String houseSQL = "UPDATE House SET Floors = ?, Price = ?, Garden = ? WHERE Estate_ID = ?";
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
            String selectSQL = "SELECT e.*, h.Floors, h.Price, h.Garden " +
                    "FROM Estate e " +
                    "JOIN House h ON e.ID = h.Estate_ID " +
                    "WHERE e.ID = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                House h = new House();
                h.setId(rs.getInt("ID"));
                h.setCity(rs.getString("City"));
                h.setPostalCode(rs.getString("Postal_Code"));
                h.setStreet(rs.getString("Street"));
                h.setStreetNumber(rs.getString("Street_Number"));
                h.setSquareArea(rs.getDouble("Square_Area"));
                h.setFloors(rs.getInt("Floors"));
                h.setPrice(rs.getDouble("Price"));
                h.setGarden(rs.getBoolean("Garden"));

                rs.close();
                pstmt.close();
                return h;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean delete(int id) {
        Connection con = DbConnectionManager.getInstance().getConnection();

        // Delete on cascade, so we only need to delete from House table
        try {
            String deleteSQL = "DELETE FROM House WHERE Estate_ID = ?";
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
                h.setId(rs.getInt("ID"));
                h.setCity(rs.getString("City"));
                h.setPostalCode(rs.getString("Postal_Code"));
                h.setStreet(rs.getString("Street"));
                h.setStreetNumber(rs.getString("Street_Number"));
                h.setSquareArea(rs.getDouble("Square_Area"));
                h.setFloors(rs.getInt("Floors"));
                h.setPrice(rs.getDouble("Price"));
                h.setGarden(rs.getBoolean("Garden"));

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
                System.out.println(house.getId());
            }
        } else {
            System.out.println("Keine Häuser gefunden.");
        }
    }
}