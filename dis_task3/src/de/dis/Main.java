package de.dis;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws SQLException, InterruptedException {

        // Setup
        Connection i1 = setup_new_connection();
        if (i1 == null) {
            System.err.println("Could not initialize initial setup connection.");
            return;
        }

        Statement cs = i1.createStatement();
        cs.execute("DROP TABLE if exists dissheet3;" +
                "CREATE TABLE dissheet3 (" +
                "id integer primary key," +
                "name VARCHAR(50));" +
                "INSERT INTO dissheet3 (id, name) VALUES (1, 'Goofy'),(2, 'Donald'),(3, 'Tick')," +
                "                                  (4, 'Trick'),(5, 'Track');");
        i1.close();

        Connection c1 = setup_new_connection();
        Connection c2 = setup_new_connection();

        if (c1 == null || c2 == null) {
            System.err.println("One or more connections failed. Aborting.");
            return;
        }

        c1.setAutoCommit(false);
        c1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

        c2.setAutoCommit(false);
        c2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

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

        // Test Serializable Isolation
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

        // 3.2 a)
        lockConflicts();

        // 3.2 b)
        repeatableReadPreventsPhantoms();

        // 3.3 a) Schedule S1 = r1(x) w2(x) c2 w1(x) r1(x) c1

        c1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        c2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        // Set auto-commit to false for manual transaction control
        c1.setAutoCommit(false);
        c2.setAutoCommit(false);

        resetTable();
        System.out.println("\nRunning Schedule S1");
        List<RunnableOperation> s1 = new ArrayList<>(Arrays.asList(
                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
                new RunnableOperation(c2, 'c', "COMMIT;"),
                new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;"),
                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c1, 'c', "COMMIT;"))
        );
        runSchedule(s1, c1, c2);
        printTableContents();

        resetTable();
        System.out.println("\nRunning Schedule S2");
        List<RunnableOperation> s2 = new ArrayList<>(Arrays.asList(
                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'UpdatedByT2' WHERE id = 1;"),
                new RunnableOperation(c2, 'c', "COMMIT;"),
                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c1, 'c', "COMMIT;")
        ));
        runSchedule(s2, c1, c2);
        printTableContents();

        resetTable();
        System.out.println("\nRunning Schedule S3");
        List<RunnableOperation> s3 = new ArrayList<>(Arrays.asList(
                new RunnableOperation(c2, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"), // r2(x)
                new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = name || ' + X1' WHERE id = 1;"), // w1(x)
                new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = name || ' + Y1' WHERE id = 2;"), // w1(y)
                new RunnableOperation(c1, 'c', "COMMIT;"), // c1
                new RunnableOperation(c2, 'r', "SELECT name FROM dissheet3 WHERE id = 2;"), // r2(y)
                new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = name || ' + X2' WHERE id = 1;"), // w2(x)
                new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = name || ' + Y2' WHERE id = 2;"), // w2(y)
                new RunnableOperation(c2, 'c', "COMMIT;") // c2
        ));
        runSchedule(s3, c1, c2);
        printTableContents();

    }

    private static void runSchedule(List<RunnableOperation> operations, Connection c1, Connection c2) throws InterruptedException {
        ExecutorService executor_t1 = Executors.newFixedThreadPool(1);
        ExecutorService executor_t2 = Executors.newFixedThreadPool(1);
        for (RunnableOperation op : operations) {
            if (op.c == c1) executor_t1.execute(op);
            if (op.c == c2) executor_t2.execute(op);
            Thread.sleep(250);  // Ensures scheduling order
        }

        executor_t1.shutdown();
        executor_t2.shutdown();

        while (!executor_t1.isTerminated() || !executor_t2.isTerminated()) {
            Thread.sleep(500);
            System.out.println("Waiting for threads...");
        }

        System.out.println("Finished all threads");
    }

    private static void printTableContents() throws SQLException {
// Print table after schedule
        Connection i2 = setup_new_connection();
        if (i2 != null) {
            Statement cs2 = i2.createStatement();
            ResultSet rs = cs2.executeQuery("SELECT id, name FROM dissheet3 ORDER BY id");
            while (rs.next())
                System.out.println(rs.getInt("id") + "," + rs.getString("name"));
            cs2.close();
        } else {
            System.err.println("Could not connect to print final table state.");
        }
    }

    private static void resetTable() {
        try (Connection conn = setup_new_connection()) {
            assert conn != null;
            Statement stmt = conn.createStatement();

            // Reset table to initial state
            stmt.executeUpdate("DROP TABLE IF EXISTS dissheet3");
            stmt.executeUpdate("CREATE TABLE dissheet3 (id INT PRIMARY KEY, name VARCHAR(100))");
            stmt.executeUpdate("INSERT INTO dissheet3 VALUES (1, 'Initial1'), (2, 'Initial2')");

            System.out.println("Table reset to initial state.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static Connection setup_new_connection() {
        try {
            String path = "dis_task3/db.properties";
            System.out.println("Loading DB config from: " + new File(path).getAbsolutePath());

            Properties properties = new Properties();
            FileInputStream stream = new FileInputStream(path);
            properties.load(stream);
            stream.close();

            String jdbcUser = properties.getProperty("jdbc_user");
            String jdbcPass = properties.getProperty("jdbc_pass");
            String jdbcUrl = properties.getProperty("jdbc_url");

            return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
        } catch (Exception e) {
            System.err.println("Failed to load DB connection: " + e.getMessage());
            return null;
        }
    }

    private static void lockConflicts() {
        System.out.println("=== Lock Conflict Test ===");
        try {
            Connection c1 = setup_new_connection();
            if (c1 == null) {
                System.err.println("C1 connection failed in lockConflicts");
                return;
            }

            c1.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            c1.setAutoCommit(false);

            Connection c2 = setup_new_connection(); // auto-commit true
            if (c2 == null) {
                System.err.println("C2 connection failed in lockConflicts");
                return;
            }

            Statement s1 = c1.createStatement();
            ResultSet rs1 = s1.executeQuery("SELECT * FROM dissheet3 WHERE id > 3;");
            while (rs1.next())
                System.out.println("C1 First Read: " + rs1.getInt("id") + " " + rs1.getString("name"));

            Statement s2 = c2.createStatement();
            s2.execute("INSERT INTO dissheet3 (id, name) VALUES (6, 'Pluto');");
            System.out.println("C2 Inserted row with id=6");

            ResultSet rs2 = s1.executeQuery("SELECT * FROM dissheet3 WHERE id > 3;");
            while (rs2.next())
                System.out.println("C1 Second Read: " + rs2.getInt("id") + " " + rs2.getString("name"));

            c1.commit();
            System.out.println("C1 committed");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void repeatableReadPreventsPhantoms() {
        System.out.println("3.2b: Phantom Read Test (Repeatable Read)");

        try {
            // T1: Repeatable Read
            Connection t1 = setup_new_connection();
            if (t1 == null) {
                System.err.println("T1 connection failed.");
                return;
            }
            t1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            t1.setAutoCommit(false);

            // T2: Auto-commit
            Connection t2 = setup_new_connection();
            if (t2 == null) {
                System.err.println("T2 connection failed.");
                return;
            }

            Statement stmt1 = t1.createStatement();
            ResultSet r1a = stmt1.executeQuery("SELECT * FROM dissheet3 WHERE id > 3;");
            System.out.println("T1 - First Read:");
            while (r1a.next()) {
                System.out.println("  " + r1a.getInt("id") + ": " + r1a.getString("name"));
            }

            // T2 inserts a new row with id > 3
            Statement stmt2 = t2.createStatement();
            stmt2.execute("INSERT INTO dissheet3 (id, name) VALUES (7, 'Phantom');");
            System.out.println("T2 - Inserted (id=7, name='Phantom')");

            // T1 reads again
            ResultSet r1b = stmt1.executeQuery("SELECT * FROM dissheet3 WHERE id > 3;");
            System.out.println("T1 - Second Read (should NOT see id=7):");
            while (r1b.next()) {
                System.out.println("  " + r1b.getInt("id") + ": " + r1b.getString("name"));
            }

            t1.commit();
            System.out.println("T1 committed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
