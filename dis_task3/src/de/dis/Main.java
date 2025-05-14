package de.dis;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {

    public static void main(String[] args) throws SQLException, InterruptedException {

        // Setup
        Connection i1 = setup_new_connection();
        Statement cs = i1.createStatement();
        cs.execute("DROP TABLE if exists dissheet3;" +
                "CREATE TABLE dissheet3 (" +
                "id integer primary key," +
                "name VARCHAR(50));" +
                "INSERT INTO dissheet3 (id, name) VALUES (1, 'Goofy'),(2, 'Donald'),(3, 'Tick')," +
                "                                  (4, 'Trick'),(5, 'Track');");
        i1.close();

        Connection c1 = setup_new_connection();
        c1.setAutoCommit(false);
        //c1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        c1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        Connection c2 = setup_new_connection();
        c2.setAutoCommit(false);
        c2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        //c2.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        Statement stmtC1 = c1.createStatement();
        ResultSet rs1 = stmtC1.executeQuery("SELECT name FROM dissheet3 WHERE id = 1;");
        if (rs1.next()) {
            System.out.println("Read (default isolation): " + rs1.getString("name"));
        }
        rs1.close();
        printLocks(c1, "Default Isolation (e.g., REPEATABLE READ)");
        printPredicateLocks(c1, "Default Isolation (e.g., REPEATABLE READ)");
        stmtC1.close();
        c1.commit();

        c1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        c1.setAutoCommit(false);
        Statement stmtC1Serializable = c1.createStatement();
        ResultSet rs2 = stmtC1Serializable.executeQuery("SELECT name FROM dissheet3 WHERE id = 1;");
        if (rs2.next()) {
            System.out.println("Read (Serializable): " + rs2.getString("name"));
        }
        rs2.close();
        printLocks(c1, "Serializable Isolation");
        printPredicateLocks(c1, "Serializable Isolation");
        stmtC1Serializable.close();
        c1.commit();


        //S1 = r1(x) w2(x) c2 w1(x) r1(x) c1
        List<RunnableOperation> operations = new ArrayList<>(Arrays.asList(

                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
                new RunnableOperation(c2, 'c', "COMMIT;"),
                new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;"),
                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c1, 'c', "COMMIT;"))
        );
        ExecutorService executor_t1 = Executors.newFixedThreadPool(1);
        ExecutorService executor_t2 = Executors.newFixedThreadPool(1);
        for (RunnableOperation op : operations) {

            if (op.c == c1)
                executor_t1.execute(op);

            if (op.c == c2)
                executor_t2.execute(op);

            Thread.sleep(250);  // Sleep, so the threads in both pools get executed in the desired order

        }
        executor_t1.shutdown();
        executor_t2.shutdown();

        while (!executor_t1.isTerminated() && !executor_t2.isTerminated()) {
            Thread.sleep(1000);
            System.out.println("Waiting for threads");
        }

        System.out.println("Finished all threads");


        // GET Table at the end
        Connection i2 = setup_new_connection();
        Statement cs2 = i2.createStatement();
        ResultSet rs = cs2.executeQuery("SELECT id, name FROM dissheet3 ORDER BY id");
        while (rs.next())
            System.out.println(Integer.toString(rs.getInt("id")) + "," + rs.getString("name"));
        cs2.close();

    }


    public static Connection setup_new_connection() {

        try {
            // Holen der Einstellungen aus der db.properties Datei
            Properties properties = new Properties();
            FileInputStream stream = new FileInputStream("dis_task3/db.properties");
            properties.load(stream);
            stream.close();

            String jdbcUser = properties.getProperty("jdbc_user");
            String jdbcPass = properties.getProperty("jdbc_pass");
            String jdbcUrl = properties.getProperty("jdbc_url");
            // Verbindung zur Datenbank herstellen
            return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void printLocks(Connection conn, String label) throws SQLException {
        System.out.println("==> " + label + ": pg_locks");
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT relation::regclass, mode, granted " +
                        "FROM pg_locks WHERE relation::regclass = 'dissheet3'::regclass;");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            System.out.println("Lock: " + rs.getString("relation") +
                    " | Mode: " + rs.getString("mode") +
                    " | Granted: " + rs.getBoolean("granted"));
        }
        rs.close();
        stmt.close();
    }

    private static void printPredicateLocks(Connection conn, String label) {
        System.out.println("==> " + label + ": pg_predicate_locks");
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM pg_catalog.pg_predicate_locks WHERE relation = 'dissheet3'::regclass;");
            ResultSet rs = stmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Predicate Lock: " + rs.getString("lockmode"));
            }
            if (!found) {
                System.out.println("No predicate locks found.");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Could not access pg_predicate_locks: " + e.getMessage());
        }
    }


}

