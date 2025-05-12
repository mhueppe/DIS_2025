package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Person {
    private int id = -1;
    private String firstName;
    private String name;
    private String address;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Saves the person to the database. If no ID exists yet, a new entry is created
     * and the generated ID is assigned to the object.
     */
    public void save() {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            if (getId() == -1) {
                // Insert new person
                String insertSQL = "INSERT INTO Person(first_name, name, address) VALUES (?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

                pstmt.setString(1, getFirstName());
                pstmt.setString(2, getName());
                pstmt.setString(3, getAddress());
                pstmt.executeUpdate();

                // Get the generated ID
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    setId(rs.getInt(1));
                }

                rs.close();
                pstmt.close();
            } else {
                // Update existing person
                String updateSQL = "UPDATE Person SET first_name = ?, last_name = ?, address = ? WHERE id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                pstmt.setString(1, getFirstName());
                pstmt.setString(2, getName());
                pstmt.setString(3, getAddress());
                pstmt.setInt(4, getId());
                pstmt.executeUpdate();

                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a person from the database
     * @param id ID of the person to load
     * @return Person instance
     */
    public static Person load(int id) {
        try {
            Connection con = DbConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM Person WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Person p = new Person();
                p.setId(id);
                p.setFirstName(rs.getString("first_name"));
                p.setName(rs.getString("name"));
                p.setAddress(rs.getString("address"));

                rs.close();
                pstmt.close();
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all persons from the database
     * @return List of Person objects
     */
    public static List<Person> getAll() {
        List<Person> persons = new ArrayList<>();
        try {
            Connection con = DbConnectionManager.getInstance().getConnection();
            String selectSQL = "SELECT * FROM Person";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Person p = new Person();
                p.setId(rs.getInt("id"));
                p.setFirstName(rs.getString("first_name"));
                p.setName(rs.getString("name"));
                p.setAddress(rs.getString("address"));
                persons.add(p);
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return persons;
    }

    public static String getAllFormatted() {
        List<Person> persons = getAll();
        StringBuilder sb = new StringBuilder();
        for (Person p : persons) {
            sb.append("ID: ").append(p.getId())
                    .append(", Name: ").append(p.getFirstName()).append(" ").append(p.getName())
                    .append(", Address: ").append(p.getAddress())
                    .append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", firstName=" + firstName + ", name=" + name + ", address=" + address + "]";
    }
}