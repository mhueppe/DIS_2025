package de.dis.data.contract;


import de.dis.data.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TenancyContract extends Contract {
    private Date startDate;
    private int duration;
    private double additionalCosts;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getAdditionalCosts() {
        return additionalCosts;
    }

    public void setAdditionalCosts(double additionalCosts) {
        this.additionalCosts = additionalCosts;
    }

    @Override
    public void save() {
        Connection con = DbConnectionManager.getInstance().getConnection();

        try {
            String baseSQL = "INSERT INTO Contract(date, place, person_id, estate_id) VALUES (?, ?, ?, ?)";
            PreparedStatement baseStmt = con.prepareStatement(baseSQL, Statement.RETURN_GENERATED_KEYS);
            baseStmt.setDate(1, date);
            baseStmt.setString(2, place);
            baseStmt.setInt(3, personId);
            baseStmt.setInt(4, estateId);
            baseStmt.executeUpdate();

            ResultSet rs = baseStmt.getGeneratedKeys();
            if (rs.next()) this.contractNo = rs.getInt(1);
            rs.close(); baseStmt.close();

            String tenancySQL = "INSERT INTO TenancyContract(contract_no, start_date, duration, additional_costs) VALUES (?, ?, ?, ?)";
            PreparedStatement tenancyStmt = con.prepareStatement(tenancySQL);
            tenancyStmt.setInt(1, contractNo);
            tenancyStmt.setDate(2, startDate);
            tenancyStmt.setInt(3, duration);
            tenancyStmt.setDouble(4, additionalCosts);
            tenancyStmt.executeUpdate();
            tenancyStmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void listAll() {
        List<TenancyContract> contracts = new ArrayList<>();

        try (Connection con = DbConnectionManager.getInstance().getConnection()) {
            String sql = "SELECT c.contract_no, c.date, c.place, c.person_id, c.estate_id, " +
                    "t.start_date, t.duration, t.additional_costs " +
                    "FROM Contract c JOIN TenancyContract t ON c.contract_no = t.contract_no";

            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TenancyContract tc = new TenancyContract();
                tc.setContractNo(rs.getInt("contract_no"));
                tc.setDate(rs.getDate("date"));
                tc.setPlace(rs.getString("place"));
                tc.setPersonId(rs.getInt("person_id"));
                tc.setEstateId(rs.getInt("estate_id"));
                tc.setStartDate(rs.getDate("start_date"));
                tc.setDuration(rs.getInt("duration"));
                tc.setAdditionalCosts(rs.getDouble("additional_costs"));
                contracts.add(tc);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (TenancyContract tc : contracts) {
            System.out.println(tc);
        }
    }

}
