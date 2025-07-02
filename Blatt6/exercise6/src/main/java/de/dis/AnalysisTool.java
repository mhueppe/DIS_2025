package de.dis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Command-line interface for the data warehouse analysis tool.
 * This class provides a user-friendly interface for running analyses
 * on the data warehouse.
 */
public class AnalysisTool {

    /**
     * Main method to run the analysis tool.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        DataWarehouse dataWarehouse = new DataWarehouse();
        Scanner scanner = new Scanner(System.in);

        try {
            // Connect to the database
            System.out.println("Connecting to the database...");
            dataWarehouse.connect();

            boolean exit = false;
            while (!exit) {
                System.out.println("\n===== Superstore Sales Analysis Tool =====");
                System.out.println("1. Create data warehouse schema");
                System.out.println("2. Run ETL process");
                System.out.println("3. Run analysis");
                System.out.println("4. Exit");
                System.out.print("Enter your choice (1-4): ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        createSchema(dataWarehouse);
                        break;
                    case "2":
                        runETL(dataWarehouse);
                        break;
                    case "3":
                        runAnalysis(dataWarehouse, scanner);
                        break;
                    case "4":
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }

            // Disconnect from the database
            System.out.println("Disconnecting from the database...");
            dataWarehouse.disconnect();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    /**
     * Create the data warehouse schema.
     * @param dataWarehouse The data warehouse instance
     */
    private static void createSchema(DataWarehouse dataWarehouse) {
        try {
            System.out.println("\nCreating data warehouse schema...");
            dataWarehouse.createSchema();
            System.out.println("Schema created successfully.");
        } catch (SQLException e) {
            System.err.println("Database error creating schema: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error creating schema (file handling): " + e.getMessage());
        }
    }

    /**
     * Run the ETL process.
     * @param dataWarehouse The data warehouse instance
     */
    private static void runETL(DataWarehouse dataWarehouse) {
        try {
            System.out.println("\nRunning ETL process...");
            dataWarehouse.runETL();
            System.out.println("ETL process completed successfully.");
        } catch (Exception e) {
            System.err.println("Error running ETL process: " + e.getMessage());
        }
    }

    /**
     * Run an analysis with user-specified parameters.
     * @param dataWarehouse The data warehouse instance
     * @param scanner Scanner for reading user input
     */
    private static void runAnalysis(DataWarehouse dataWarehouse, Scanner scanner) {
        try {
            System.out.println("\n----- Analysis Parameters -----");

            // Get geography level
            System.out.println("Geography level options: shop, city, region, country");
            System.out.print("Enter geography level: ");
            String geo = scanner.nextLine().trim().toLowerCase();

            // Get time level
            System.out.println("Time level options: date, day, month, quarter, year");
            System.out.print("Enter time level: ");
            String time = scanner.nextLine().trim().toLowerCase();

            // Get product level
            System.out.println("Product level options: article, productGroup, productFamily, productCategory");
            System.out.print("Enter product level: ");
            String product = scanner.nextLine().trim().toLowerCase();

            // Run the analysis
            System.out.println("\nRunning analysis...");
            dataWarehouse.analysis(geo, time, product);

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid parameter: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error during analysis: " + e.getMessage());
        }
    }
}
