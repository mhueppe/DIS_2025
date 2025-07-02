package de.dis;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main class for the Data Warehouse application.
 * This application implements an ETL process to populate a data warehouse
 * and provides functionality to analyze sales data.
 */
public class DataWarehouse {
    // Database connection parameters
    private static final String DB_HOST = "localhost:5433";
    private static final String DB_NAME = "postgres";
    private static final String DB_USER = "username";
    private static final String DB_PASSWORD = "password";

    // Connection to the database
    private Connection connection;

    /**
     * Main method to run the application.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        DataWarehouse dataWarehouse = new DataWarehouse();

        try {
            // Connect to the database
            dataWarehouse.connect();

            // Create the data warehouse schema
            dataWarehouse.createSchema();

            // Run the ETL process
            dataWarehouse.runETL();

            // Run a sample analysis
            dataWarehouse.analysis("region", "quarter", "productGroup");

            // Close the database connection
            dataWarehouse.disconnect();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O error (file handling): " + e.getMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Connect to the PostgreSQL database.
     * @throws SQLException If a database access error occurs
     */
    public void connect() throws SQLException {
        String url = "jdbc:postgresql://" + DB_HOST + "/" + DB_NAME;
        connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
        System.out.println("Connected to the database.");
    }

    /**
     * Disconnect from the PostgreSQL database.
     * @throws SQLException If a database access error occurs
     */
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Disconnected from the database.");
        }
    }

    /**
     * Create the schema for the data warehouse.
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs while reading the SQL script
     */
    public void createSchema() throws SQLException, IOException {
        System.out.println("Creating data warehouse schema...");

        Statement stmt = connection.createStatement();

        // Drop existing tables if they exist
        stmt.executeUpdate("DROP TABLE IF EXISTS FactSales");
        stmt.executeUpdate("DROP TABLE IF EXISTS DimTime");
        stmt.executeUpdate("DROP TABLE IF EXISTS DimProduct");
        stmt.executeUpdate("DROP TABLE IF EXISTS DimGeography");

        // Also drop source tables if they exist
        stmt.executeUpdate("DROP TABLE IF EXISTS Article CASCADE");
        stmt.executeUpdate("DROP TABLE IF EXISTS ProductGroup CASCADE");
        stmt.executeUpdate("DROP TABLE IF EXISTS ProductFamily CASCADE");
        stmt.executeUpdate("DROP TABLE IF EXISTS ProductCategory CASCADE");
        stmt.executeUpdate("DROP TABLE IF EXISTS Shop CASCADE");
        stmt.executeUpdate("DROP TABLE IF EXISTS City CASCADE");
        stmt.executeUpdate("DROP TABLE IF EXISTS Region CASCADE");
        stmt.executeUpdate("DROP TABLE IF EXISTS Country CASCADE");

        // Load and execute the SQL script to create source tables
        String sqlScriptPath = "Blatt6/ressources/stores-and-products.sql";
        executeSqlScript(sqlScriptPath);

        // Create data warehouse tables
        createDataWarehouseTables(stmt);

        stmt.close();
        System.out.println("Data warehouse schema created successfully.");
    }

    /**
     * Create the data warehouse tables.
     * @param stmt Statement to execute SQL commands
     * @throws SQLException If a database access error occurs
     */
    private void createDataWarehouseTables(Statement stmt) throws SQLException {
        System.out.println("Creating data warehouse tables...");

        // Time dimension
        stmt.executeUpdate(
            "CREATE TABLE DimTime (" +
            "    TimeID SERIAL PRIMARY KEY," +
            "    Date DATE NOT NULL," +
            "    Day INT NOT NULL," +
            "    Month INT NOT NULL," +
            "    Quarter INT NOT NULL," +
            "    Year INT NOT NULL" +
            ")"
        );

        // Product dimension
        stmt.executeUpdate(
            "CREATE TABLE DimProduct (" +
            "    ProductID SERIAL PRIMARY KEY," +
            "    ArticleID INT NOT NULL," +
            "    ArticleName VARCHAR(255) NOT NULL," +
            "    ProductGroupID INT NOT NULL," +
            "    ProductGroupName VARCHAR(255) NOT NULL," +
            "    ProductFamilyID INT NOT NULL," +
            "    ProductFamilyName VARCHAR(255) NOT NULL," +
            "    ProductCategoryID INT NOT NULL," +
            "    ProductCategoryName VARCHAR(255) NOT NULL," +
            "    Price DOUBLE PRECISION NOT NULL" +
            ")"
        );

        // Geography dimension
        stmt.executeUpdate(
            "CREATE TABLE DimGeography (" +
            "    GeographyID SERIAL PRIMARY KEY," +
            "    ShopID INT NOT NULL," +
            "    ShopName VARCHAR(255) NOT NULL," +
            "    CityID INT NOT NULL," +
            "    CityName VARCHAR(255) NOT NULL," +
            "    RegionID INT NOT NULL," +
            "    RegionName VARCHAR(255) NOT NULL," +
            "    CountryID INT NOT NULL," +
            "    CountryName VARCHAR(255) NOT NULL" +
            ")"
        );

        // Create fact table
        stmt.executeUpdate(
            "CREATE TABLE FactSales (" +
            "    SalesID SERIAL PRIMARY KEY," +
            "    TimeID INT REFERENCES DimTime(TimeID)," +
            "    ProductID INT REFERENCES DimProduct(ProductID)," +
            "    GeographyID INT REFERENCES DimGeography(GeographyID)," +
            "    QuantitySold INT NOT NULL," +
            "    Revenue DOUBLE PRECISION NOT NULL" +
            ")"
        );

        System.out.println("Data warehouse tables created successfully.");
    }

    /**
     * Execute an SQL script file.
     * @param scriptPath Path to the SQL script file
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs while reading the script
     */
    private void executeSqlScript(String scriptPath) throws SQLException, IOException {
        System.out.println("Executing SQL script: " + scriptPath);

        // Read the SQL script file
        StringBuilder scriptContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                scriptContent.append(line).append("\n");
            }
        }

        // Execute the script
        Statement stmt = connection.createStatement();
        String[] statements = scriptContent.toString().split(";");

        for (String statement : statements) {
            String trimmedStatement = statement.trim();
            if (!trimmedStatement.isEmpty()) {
                stmt.execute(trimmedStatement);
            }
        }

        stmt.close();
        System.out.println("SQL script executed successfully.");
    }

    /**
     * Run the ETL process to populate the data warehouse.
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws ParseException If a date parsing error occurs
     */
    public void runETL() throws SQLException, IOException, ParseException {
        System.out.println("Starting ETL process...");

        // Extract and load dimension data
        loadGeographyDimension();
        loadProductDimension();

        // Extract, transform, and load sales data
        loadSalesData();

        System.out.println("ETL process completed successfully.");
    }

    /**
     * Load the geography dimension from the source database.
     * @throws SQLException If a database access error occurs
     */
    private void loadGeographyDimension() throws SQLException {
        System.out.println("Loading geography dimension...");

        // Query to extract geography data from source tables
        String query = 
            "SELECT s.ShopID, s.Name AS ShopName, " +
            "       c.CityID, c.Name AS CityName, " +
            "       r.RegionID, r.Name AS RegionName, " +
            "       co.CountryID, co.Name AS CountryName " +
            "FROM Shop s " +
            "JOIN City c ON s.CityID = c.CityID " +
            "JOIN Region r ON c.RegionID = r.RegionID " +
            "JOIN Country co ON r.CountryID = co.CountryID";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        // Prepare insert statement for the geography dimension
        PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO DimGeography (ShopID, ShopName, CityID, CityName, " +
            "RegionID, RegionName, CountryID, CountryName) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );

        int count = 0;
        while (rs.next()) {
            pstmt.setInt(1, rs.getInt("ShopID"));
            pstmt.setString(2, rs.getString("ShopName"));
            pstmt.setInt(3, rs.getInt("CityID"));
            pstmt.setString(4, rs.getString("CityName"));
            pstmt.setInt(5, rs.getInt("RegionID"));
            pstmt.setString(6, rs.getString("RegionName"));
            pstmt.setInt(7, rs.getInt("CountryID"));
            pstmt.setString(8, rs.getString("CountryName"));

            pstmt.executeUpdate();
            count++;
        }

        rs.close();
        stmt.close();
        pstmt.close();

        System.out.println("Loaded " + count + " geography dimension records.");
    }

    /**
     * Load the product dimension from the source database.
     * @throws SQLException If a database access error occurs
     */
    private void loadProductDimension() throws SQLException {
        System.out.println("Loading product dimension...");

        // Query to extract product data from source tables
        String query = 
            "SELECT a.ArticleID, a.Name AS ArticleName, a.Price, " +
            "       pg.ProductGroupID, pg.Name AS ProductGroupName, " +
            "       pf.ProductFamilyID, pf.Name AS ProductFamilyName, " +
            "       pc.ProductCategoryID, pc.Name AS ProductCategoryName " +
            "FROM Article a " +
            "JOIN ProductGroup pg ON a.ProductGroupID = pg.ProductGroupID " +
            "JOIN ProductFamily pf ON pg.ProductFamilyID = pf.ProductFamilyID " +
            "JOIN ProductCategory pc ON pf.ProductCategoryID = pc.ProductCategoryID";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        // Prepare insert statement for the product dimension
        PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO DimProduct (ArticleID, ArticleName, ProductGroupID, " +
            "ProductGroupName, ProductFamilyID, ProductFamilyName, " +
            "ProductCategoryID, ProductCategoryName, Price) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        );

        int count = 0;
        while (rs.next()) {
            pstmt.setInt(1, rs.getInt("ArticleID"));
            pstmt.setString(2, rs.getString("ArticleName"));
            pstmt.setInt(3, rs.getInt("ProductGroupID"));
            pstmt.setString(4, rs.getString("ProductGroupName"));
            pstmt.setInt(5, rs.getInt("ProductFamilyID"));
            pstmt.setString(6, rs.getString("ProductFamilyName"));
            pstmt.setInt(7, rs.getInt("ProductCategoryID"));
            pstmt.setString(8, rs.getString("ProductCategoryName"));
            pstmt.setDouble(9, rs.getDouble("Price"));

            pstmt.executeUpdate();
            count++;
        }

        rs.close();
        stmt.close();
        pstmt.close();

        System.out.println("Loaded " + count + " product dimension records.");
    }

    /**
     * Load the sales data from the CSV file.
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws ParseException If a date parsing error occurs
     */
    private void loadSalesData() throws SQLException, IOException, ParseException {
        System.out.println("Loading sales data...");

        // Path to the sales CSV file
        String csvFile = "Blatt6/ressources/sales.csv";

        // Maps to store dimension keys
        Map<String, Integer> timeMap = new HashMap<>();
        Map<String, Integer> productMap = new HashMap<>();
        Map<String, Integer> geographyMap = new HashMap<>();

        // Load dimension maps
        loadDimensionMaps(timeMap, productMap, geographyMap);

        // Prepare statements for inserting time dimension and fact data
        PreparedStatement timeStmt = connection.prepareStatement(
            "INSERT INTO DimTime (Date, Day, Month, Quarter, Year) " +
            "VALUES (?, ?, ?, ?, ?) RETURNING TimeID"
        );

        PreparedStatement factStmt = connection.prepareStatement(
            "INSERT INTO FactSales (TimeID, ProductID, GeographyID, QuantitySold, Revenue) " +
            "VALUES (?, ?, ?, ?, ?)"
        );

        // Read the CSV file
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String line;

        // Skip header line
        br.readLine();

        int batchSize = 1000;
        int count = 0;

        // Process each line in the CSV file
        while ((line = br.readLine()) != null) {
            String[] data = line.split(";");

            if (data.length < 5) {
                System.out.println("Skipping invalid line: " + line);
                continue;
            }

            try {
                // Parse date
                String dateStr = data[0];
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                java.util.Date parsedDate = dateFormat.parse(dateStr);
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(parsedDate);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH) + 1; // Calendar months are 0-based
                int year = cal.get(Calendar.YEAR);
                int quarter = ((month - 1) / 3) + 1;

                // Get or create time dimension record
                Integer timeID = timeMap.get(dateStr);
                if (timeID == null) {
                    timeStmt.setDate(1, sqlDate);
                    timeStmt.setInt(2, day);
                    timeStmt.setInt(3, month);
                    timeStmt.setInt(4, quarter);
                    timeStmt.setInt(5, year);

                    ResultSet rs = timeStmt.executeQuery();
                    if (rs.next()) {
                        timeID = rs.getInt(1);
                        timeMap.put(dateStr, timeID);
                    }
                    rs.close();
                }

                // Get shop name and article name
                String shopName = data[1];
                String articleName = data[2];

                // Get dimension keys
                Integer productID = productMap.get(articleName);
                Integer geographyID = geographyMap.get(shopName);

                if (productID == null || geographyID == null) {
                    System.out.println("Skipping record with unknown product or geography: " + line);
                    continue;
                }

                // Parse quantity and revenue
                int quantitySold = Integer.parseInt(data[3]);
                double revenue = Double.parseDouble(data[4].replace(',', '.'));

                // Add to fact table
                factStmt.setInt(1, timeID);
                factStmt.setInt(2, productID);
                factStmt.setInt(3, geographyID);
                factStmt.setInt(4, quantitySold);
                factStmt.setDouble(5, revenue);

                factStmt.addBatch();

                count++;

                // Execute batch if batch size is reached
                if (count % batchSize == 0) {
                    factStmt.executeBatch();
                    System.out.println("Processed " + count + " sales records...");
                }
            } catch (NumberFormatException | ParseException e) {
                System.out.println("Error processing line: " + line);
                System.out.println("Error: " + e.getMessage());
            }
        }

        // Execute any remaining batch
        factStmt.executeBatch();

        br.close();
        timeStmt.close();
        factStmt.close();

        System.out.println("Loaded " + count + " sales records.");
    }

    /**
     * Load dimension maps for efficient lookups during ETL.
     * @param timeMap Map to store time dimension keys
     * @param productMap Map to store product dimension keys
     * @param geographyMap Map to store geography dimension keys
     * @throws SQLException If a database access error occurs
     */
    private void loadDimensionMaps(Map<String, Integer> timeMap, Map<String, Integer> productMap, 
                                  Map<String, Integer> geographyMap) throws SQLException {
        // Load product dimension map
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ProductID, ArticleName FROM DimProduct");

        while (rs.next()) {
            productMap.put(rs.getString("ArticleName"), rs.getInt("ProductID"));
        }

        rs.close();

        // Load geography dimension map
        rs = stmt.executeQuery("SELECT GeographyID, ShopName FROM DimGeography");

        while (rs.next()) {
            geographyMap.put(rs.getString("ShopName"), rs.getInt("GeographyID"));
        }

        rs.close();

        // Load time dimension map
        rs = stmt.executeQuery("SELECT TimeID, Date FROM DimTime");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        while (rs.next()) {
            Date date = rs.getDate("Date");
            String dateStr = dateFormat.format(date);
            timeMap.put(dateStr, rs.getInt("TimeID"));
        }

        rs.close();
        stmt.close();
    }

    /**
     * Produces output that the manager can use.
     * The desired granularity level of each dimension is given by the parameters;
     * e.g. geo = "country" is the most general and geo = "shop" is the most fine-grained
     * granularity level for the geographical dimension.
     *
     * @param geo admissible values: shop, city, region, country
     * @param time admissible values: date, day, month, quarter, year
     * @param product admissible values: article, productGroup, productFamily, productCategory
     * @throws SQLException If a database access error occurs
     */
    public void analysis(String geo, String time, String product) throws SQLException {
        System.out.println("\nPerforming analysis with parameters:");
        System.out.println("Geography level: " + geo);
        System.out.println("Time level: " + time);
        System.out.println("Product level: " + product);

        // Validate parameters
        validateAnalysisParameters(geo, time, product);

        // Build the SQL query based on the parameters
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");

        // Select geography column
        switch (geo.toLowerCase()) {
            case "shop":
                queryBuilder.append("g.ShopName AS Geography");
                break;
            case "city":
                queryBuilder.append("g.CityName AS Geography");
                break;
            case "region":
                queryBuilder.append("g.RegionName AS Geography");
                break;
            case "country":
                queryBuilder.append("g.CountryName AS Geography");
                break;
        }

        // Select time column
        queryBuilder.append(", ");
        switch (time.toLowerCase()) {
            case "date":
                queryBuilder.append("t.Date AS Time");
                break;
            case "day":
                queryBuilder.append("t.Day AS Time");
                break;
            case "month":
                queryBuilder.append("t.Month AS Time");
                break;
            case "quarter":
                queryBuilder.append("CONCAT('quarter ', t.Quarter, ', ', t.Year) AS Time");
                break;
            case "year":
                queryBuilder.append("t.Year AS Time");
                break;
        }

        // Select product column
        queryBuilder.append(", ");
        switch (product.toLowerCase()) {
            case "article":
                queryBuilder.append("p.ArticleName AS Product");
                break;
            case "productgroup":
                queryBuilder.append("p.ProductGroupName AS Product");
                break;
            case "productfamily":
                queryBuilder.append("p.ProductFamilyName AS Product");
                break;
            case "productcategory":
                queryBuilder.append("p.ProductCategoryName AS Product");
                break;
        }

        // Select measures
        queryBuilder.append(", SUM(f.QuantitySold) AS TotalSold, SUM(f.Revenue) AS TotalRevenue ");

        // From clause
        queryBuilder.append("FROM FactSales f ");
        queryBuilder.append("JOIN DimTime t ON f.TimeID = t.TimeID ");
        queryBuilder.append("JOIN DimProduct p ON f.ProductID = p.ProductID ");
        queryBuilder.append("JOIN DimGeography g ON f.GeographyID = g.GeographyID ");

        // Group by clause
        queryBuilder.append("GROUP BY GROUPING SETS ((");

        // Group by geography
        switch (geo.toLowerCase()) {
            case "shop":
                queryBuilder.append("g.ShopName");
                break;
            case "city":
                queryBuilder.append("g.CityName");
                break;
            case "region":
                queryBuilder.append("g.RegionName");
                break;
            case "country":
                queryBuilder.append("g.CountryName");
                break;
        }

        // Group by time
        queryBuilder.append(", ");
        switch (time.toLowerCase()) {
            case "date":
                queryBuilder.append("t.Date");
                break;
            case "day":
                queryBuilder.append("t.Day");
                break;
            case "month":
                queryBuilder.append("t.Month");
                break;
            case "quarter":
                queryBuilder.append("CONCAT('quarter ', t.Quarter, ', ', t.Year)");
                break;
            case "year":
                queryBuilder.append("t.Year");
                break;
        }

        // Group by product
        queryBuilder.append(", ");
        switch (product.toLowerCase()) {
            case "article":
                queryBuilder.append("p.ArticleName");
                break;
            case "productgroup":
                queryBuilder.append("p.ProductGroupName");
                break;
            case "productfamily":
                queryBuilder.append("p.ProductFamilyName");
                break;
            case "productcategory":
                queryBuilder.append("p.ProductCategoryName");
                break;
        }

        queryBuilder.append("), ())");

        // Order by clause
        queryBuilder.append(" ORDER BY Geography, Time, Product");

        // Execute the query
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(queryBuilder.toString());

        // Process the results to create a cross table
        createCrossTable(rs, geo, time, product);

        rs.close();
        stmt.close();
    }

    /**
     * Validate the parameters for the analysis method.
     * @param geo Geography level
     * @param time Time level
     * @param product Product level
     * @throws IllegalArgumentException If any parameter is invalid
     */
    private void validateAnalysisParameters(String geo, String time, String product) {
        // Validate geography parameter
        if (!Arrays.asList("shop", "city", "region", "country").contains(geo.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid geography parameter. Valid values are: shop, city, region, country");
        }

        // Validate time parameter
        if (!Arrays.asList("date", "day", "month", "quarter", "year").contains(time.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid time parameter. Valid values are: date, day, month, quarter, year");
        }

        // Validate product parameter
        if (!Arrays.asList("article", "productgroup", "productfamily", "productcategory").contains(product.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid product parameter. Valid values are: article, productGroup, productFamily, productCategory");
        }
    }

    /**
     * Create and display a cross table from the query results.
     * @param rs ResultSet containing the query results
     * @param geo Geography level
     * @param time Time level
     * @param product Product level
     * @throws SQLException If a database access error occurs
     */
    private void createCrossTable(ResultSet rs, String geo, String time, String product) throws SQLException {
        // Maps to store the data for the cross table
        Map<String, Map<String, Double>> crossTable = new TreeMap<>();
        Set<String> timeValues = new TreeSet<>();
        Set<String> productValues = new TreeSet<>();
        Set<String> geoValues = new TreeSet<>();

        // Totals
        Map<String, Double> productTotals = new HashMap<>();
        Map<String, Double> timeTotals = new HashMap<>();
        Map<String, Double> geoTotals = new HashMap<>();
        double grandTotal = 0.0;

        // Process the result set
        while (rs.next()) {
            String geography = rs.getString("Geography");
            String timeValue = rs.getString("Time");
            String productValue = rs.getString("Product");
            double revenue = rs.getDouble("TotalRevenue");

            // Handle null values (totals)
            if (geography == null) geography = "total";
            if (timeValue == null) timeValue = "total";
            if (productValue == null) productValue = "total";

            // Skip the grand total row (all nulls)
            if (geography.equals("total") && timeValue.equals("total") && productValue.equals("total")) {
                grandTotal = revenue;
                continue;
            }

            // Add to sets for column/row headers
            if (!geography.equals("total")) geoValues.add(geography);
            if (!timeValue.equals("total")) timeValues.add(timeValue);
            if (!productValue.equals("total")) productValues.add(productValue);

            // Update totals
            if (timeValue.equals("total") && !productValue.equals("total")) {
                productTotals.put(productValue, revenue);
            }
            if (productValue.equals("total") && !timeValue.equals("total")) {
                timeTotals.put(timeValue, revenue);
            }
            if (timeValue.equals("total") && productValue.equals("total")) {
                geoTotals.put(geography, revenue);
            }

            // Add to cross table
            if (!geography.equals("total") && !timeValue.equals("total") && !productValue.equals("total")) {
                String key = geography + "|" + timeValue;
                crossTable.computeIfAbsent(key, k -> new HashMap<>()).put(productValue, revenue);
            }
        }

        // Print the cross table
        System.out.println("\nCross Table:");

        // Print header row with product names
        System.out.printf("%-20s", geo + " / " + time);
        for (String prod : productValues) {
            System.out.printf("%-20s", prod);
        }
        System.out.printf("%-20s\n", "total");

        // Print data rows
        for (String geo1 : geoValues) {
            for (String time1 : timeValues) {
                System.out.printf("%-20s", geo1 + ", " + time1);

                String key = geo1 + "|" + time1;
                Map<String, Double> row = crossTable.getOrDefault(key, new HashMap<>());

                double rowTotal = 0.0;
                for (String prod : productValues) {
                    double value = row.getOrDefault(prod, 0.0);
                    System.out.printf("%-20.2f", value);
                    rowTotal += value;
                }

                System.out.printf("%-20.2f\n", rowTotal);
            }

            // Print total for this geography
            System.out.printf("%-20s", geo1 + ", total");
            double geoTotal = geoTotals.getOrDefault(geo1, 0.0);

            for (String prod : productValues) {
                // This would require additional queries to get exact breakdowns
                // For now, we'll just print the total
                System.out.printf("%-20s", "");
            }

            System.out.printf("%-20.2f\n", geoTotal);
            System.out.println();
        }

        // Print product totals
        System.out.printf("%-20s", "total");
        for (String prod : productValues) {
            System.out.printf("%-20.2f", productTotals.getOrDefault(prod, 0.0));
        }
        System.out.printf("%-20.2f\n", grandTotal);
    }
}
