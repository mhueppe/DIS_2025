package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import de.dis.data.estate.Apartment;
import de.dis.data.estate.House;

public class Contract {
    private int id = -1;
    private Date date;
    private String place;
    private int personId;
    private int estateId;
    private int estateAgentId;
    private String contractType; // "purchase" or "tenancy"

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

    public int getEstateAgentId() {
        return estateAgentId;
    }

    public void setEstateAgentId(int estateAgentId) {
        this.estateAgentId = estateAgentId;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    /**
     * Saves the contract to the database. If no ID exists yet, a new entry is created
     * and the generated ID is assigned to the object.
     */
    public void save() {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            if (getId() == -1) {
                // Insert new contract
                String insertSQL = "INSERT INTO Contract(date, place, person_id, estate_id, estate_agent_id, contract_type) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

                pstmt.setDate(1, getDate());
                pstmt.setString(2, getPlace());
                pstmt.setInt(3, getPersonId());
                pstmt.setInt(4, getEstateId());
                pstmt.setInt(5, getEstateAgentId());
                pstmt.setString(6, getContractType());
                pstmt.executeUpdate();

                // Get the generated ID
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    setId(rs.getInt(1));
                }

                rs.close();
                pstmt.close();
            } else {
                // Update existing contract
                String updateSQL = "UPDATE Contract SET date = ?, place = ?, person_id = ?, estate_id = ?, " +
                        "estate_agent_id = ?, contract_type = ? WHERE id = ?";
                PreparedStatement pstmt = con.prepareStatement(updateSQL);

                pstmt.setDate(1, getDate());
                pstmt.setString(2, getPlace());
                pstmt.setInt(3, getPersonId());
                pstmt.setInt(4, getEstateId());
                pstmt.setInt(5, getEstateAgentId());
                pstmt.setString(6, getContractType());
                pstmt.setInt(7, getId());
                pstmt.executeUpdate();

                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a contract from the database
     * @param id ID of the contract to load
     * @return Contract instance
     */
    public static Contract load(int id) {
        try {
            Connection con = DbConnectionManager.getInstance().getConnection();

            String selectSQL = "SELECT * FROM Contract WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Contract c = new Contract();
                c.setId(id);
                c.setDate(rs.getDate("date"));
                c.setPlace(rs.getString("place"));
                c.setPersonId(rs.getInt("person_id"));
                c.setEstateId(rs.getInt("estate_id"));
                c.setEstateAgentId(rs.getInt("estate_agent_id"));
                c.setContractType(rs.getString("contract_type"));

                rs.close();
                pstmt.close();
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all contracts from the database with detailed information
     * @return List of Contract objects with related information
     */
    public static List<ContractDetail> getAllWithDetails() {
        List<ContractDetail> contracts = new ArrayList<>();
        try {
            Connection con = DbConnectionManager.getInstance().getConnection();
            String selectSQL =
                    "SELECT c.*, p.first_name, p.name, ea.name as agent_name, " +
                            "e.city, e.street, e.street_number " +
                            "FROM Contract c " +
                            "JOIN Person p ON c.person_id = p.id " +
                            "JOIN estateagent ea ON c.estate_agent_id = ea.id " +
                            "JOIN Estate e ON c.estate_id = e.id";

            PreparedStatement pstmt = con.prepareStatement(selectSQL);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ContractDetail cd = new ContractDetail();
                cd.setId(rs.getInt("id"));
                cd.setDate(rs.getDate("date"));
                cd.setPlace(rs.getString("place"));
                cd.setPersonId(rs.getInt("person_id"));
                cd.setEstateId(rs.getInt("estate_id"));
                cd.setEstateAgentId(rs.getInt("estate_agent_id"));
                cd.setContractType(rs.getString("contract_type"));

                // Additional details
                cd.setPersonName(rs.getString("first_name") + " " + rs.getString("name"));
                cd.setAgentName(rs.getString("agent_name"));
                cd.setEstateAddress(rs.getString("street") + " " + rs.getString("street_number") + ", " + rs.getString("city"));

                contracts.add(cd);
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contracts;
    }

    public static class ContractDetail extends Contract {
        private String personName;
        private String agentName;
        private String estateAddress;

        public String getPersonName() {
            return personName;
        }

        public void setPersonName(String personName) {
            this.personName = personName;
        }

        public String getAgentName() {
            return agentName;
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public String getEstateAddress() {
            return estateAddress;
        }

        public void setEstateAddress(String estateAddress) {
            this.estateAddress = estateAddress;
        }

        @Override
        public String toString() {
            return "Contract [id=" + getId() + ", date=" + getDate() + ", place=" + getPlace() +
                    ", type=" + getContractType() + "]\n" +
                    "   Person: " + personName + "\n" +
                    "   Estate: " + estateAddress + "\n" +
                    "   Agent: " + agentName;
        }
    }
}