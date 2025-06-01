package de.dis.data.contract;

import de.dis.data.DbConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseContract extends Contract {
    private int numberOfInstallments;
    private double interestRate;

    public int getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(int numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
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

            String purchaseSQL = "INSERT INTO PurchaseContract(contract_no, no_of_installments, interest_rate) VALUES (?, ?, ?)";
            PreparedStatement purchaseStmt = con.prepareStatement(purchaseSQL);
            purchaseStmt.setInt(1, contractNo);
            purchaseStmt.setInt(2, numberOfInstallments);
            purchaseStmt.setDouble(3, interestRate);
            purchaseStmt.executeUpdate();
            purchaseStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void listAll() {
        List<PurchaseContract> contracts = new ArrayList<>();
        Connection con = DbConnectionManager.getInstance().getConnection();
        try  {
            String sql = "SELECT c.contract_no, c.date, c.place, c.person_id, c.estate_id, " +
                    "p.no_of_installments, p.interest_rate " +
                    "FROM Contract c JOIN PurchaseContract p ON c.contract_no = p.contract_no";

            Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(sql);


            while (rs.next()) {
                PurchaseContract pc = new PurchaseContract();
                pc.setContractNo(rs.getInt("contract_no"));
                pc.setDate(rs.getDate("date"));
                pc.setPlace(rs.getString("place"));
                pc.setPersonId(rs.getInt("person_id"));
                pc.setEstateId(rs.getInt("estate_id"));
                pc.setNumberOfInstallments(rs.getInt("no_of_installments"));
                pc.setInterestRate(rs.getDouble("interest_rate"));
                contracts.add(pc);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (PurchaseContract pc : contracts) {
            System.out.println(pc.getContractNo());
        }
    }
}
