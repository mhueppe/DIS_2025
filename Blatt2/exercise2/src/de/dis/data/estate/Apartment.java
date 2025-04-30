package de.dis.data.estate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dis.data.DbConnectionManager;

public class Apartment extends Estate {

    private int floor;
    private double rent;
    private int rooms;
    private boolean balcony;
    private boolean builtInKitchen;

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public double getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public boolean hasBalcony() {
        return balcony;
    }

    public void setBalcony(boolean balcony) {
        this.balcony = balcony;
    }

    public boolean hasBuiltInKitchen() {
        return builtInKitchen;
    }

    public void setBuiltInKitchen(boolean builtInKitchen) {
        this.builtInKitchen = builtInKitchen;
    }

    public String toString() {
        return "Apartment [id=" + getId() + ", floor=" + floor + ", rent=" + rent + ", rooms=" + rooms + ", balcony="
                + balcony
                + ", builtInKitchen=" + builtInKitchen + ", city=" + getCity() + ", postalCode="
                + getPostalCode() + ", street=" + getStreet() + ", streetNumber=" + getStreetNumber() + ", squareArea="
                + getSquareArea() + "]";
    }

    public void save() {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            // Füge neues Element hinzu, wenn das Objekt noch keine ID hat.
            if (getId() == -1) {
                // Insert into Estate table with generated keys
                String estateSQL = "INSERT INTO Estate (City, Postal_Code, Street, Street_Number, Square_Area, Agent_ID) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement estateStmt = con.prepareStatement(estateSQL, PreparedStatement.RETURN_GENERATED_KEYS);
                estateStmt.setString(1, getCity());
                estateStmt.setString(2, getPostalCode());
                estateStmt.setString(3, getStreet());
                estateStmt.setString(4, getStreetNumber());
                estateStmt.setDouble(5, getSquareArea());
                estateStmt.setInt(6, getAgentId());
                estateStmt.executeUpdate();

                // Retrieve generated ID for Estate
                ResultSet estateRs = estateStmt.getGeneratedKeys();
                if (estateRs.next()) {
                    setId(estateRs.getInt(1));
                }

                // Insert into Apartment table
                String apartmentSQL = "INSERT INTO Apartment (Estate_ID, Floor, Rent, Rooms, Balcony, Built_In_Kitchen) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement apartmentStmt = con.prepareStatement(apartmentSQL);
                apartmentStmt.setInt(1, getId());
                apartmentStmt.setInt(2, getFloor());
                apartmentStmt.setDouble(3, getRent());
                apartmentStmt.setInt(4, getRooms());
                apartmentStmt.setBoolean(5, hasBalcony());
                apartmentStmt.setBoolean(6, hasBuiltInKitchen());

                apartmentStmt.executeUpdate();
                apartmentStmt.close();
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

                // Update Apartment table
                String apartmentSQL = "UPDATE Apartment SET Floor = ?, Rent = ?, Rooms = ?, Balcony = ?, Built_In_Kitchen = ? WHERE Estate_ID = ?";
                PreparedStatement apartmentStmt = con.prepareStatement(apartmentSQL);
                apartmentStmt.setInt(1, getFloor());
                apartmentStmt.setDouble(2, getRent());
                apartmentStmt.setInt(3, getRooms());
                apartmentStmt.setBoolean(4, hasBalcony());
                apartmentStmt.setBoolean(5, hasBuiltInKitchen());
                apartmentStmt.setInt(6, getId());

                apartmentStmt.executeUpdate();
                apartmentStmt.close();
                estateStmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Apartment getApartment(int id) {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            // Use JOIN to combine Estate and Apartment tables
            String selectSQL = "SELECT e.*, a.Floor, a.Rent, a.Rooms, a.Balcony, a.Built_In_Kitchen " +
                    "FROM Estate e " +
                    "JOIN Apartment a ON e.ID = a.Estate_ID " +
                    "WHERE e.ID = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Apartment a = new Apartment();
                a.setId(rs.getInt("ID"));
                a.setCity(rs.getString("City"));
                a.setPostalCode(rs.getString("Postal_Code"));
                a.setStreet(rs.getString("Street"));
                a.setStreetNumber(rs.getString("Street_Number"));
                a.setSquareArea(rs.getDouble("Square_Area"));
                a.setFloor(rs.getInt("Floor"));
                a.setRent(rs.getDouble("Rent"));
                a.setRooms(rs.getInt("Rooms"));
                a.setBalcony(rs.getBoolean("Balcony"));
                a.setBuiltInKitchen(rs.getBoolean("Built_In_Kitchen"));

                rs.close();
                pstmt.close();
                return a;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Apartment[] getAllApartments() {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            String selectSQL = "SELECT e.*, a.Floor, a.Rent, a.Rooms, a.Balcony, a.Built_In_Kitchen " +
                    "FROM Estate e " +
                    "JOIN Apartment a ON e.ID = a.Estate_ID";
            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(selectSQL);

            // Count the number of rows
            rs.last();
            int count = rs.getRow();
            rs.beforeFirst();

            // Create an array of Apartment objects
            Apartment[] apartments = new Apartment[count];
            int i = 0;
            while (rs.next()) {
                Apartment a = new Apartment();
                a.setId(rs.getInt("ID"));
                a.setCity(rs.getString("City"));
                a.setPostalCode(rs.getString("Postal_Code"));
                a.setStreet(rs.getString("Street"));
                a.setStreetNumber(rs.getString("Street_Number"));
                a.setSquareArea(rs.getDouble("Square_Area"));
                a.setFloor(rs.getInt("Floor"));
                a.setRent(rs.getDouble("Rent"));
                a.setRooms(rs.getInt("Rooms"));
                a.setBalcony(rs.getBoolean("Balcony"));
                a.setBuiltInKitchen(rs.getBoolean("Built_In_Kitchen"));

                apartments[i++] = a;
            }

            rs.close();
            stmt.close();
            return apartments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void listAll() {
        Apartment[] apartments = getAllApartments();
        if (apartments != null && apartments.length > 0) {
            for (Apartment apartment : apartments) {
                System.out.println(apartment.getId());
            }
        } else {
            System.out.println("Keine Wohnungen gefunden.");
        }
    }

    public static boolean delete(int id) {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            String deleteSQL = "DELETE FROM Apartment WHERE Estate_ID = ?";
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
}