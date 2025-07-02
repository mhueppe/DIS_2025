# Data Warehouse for Superstore Sales Analysis

This project implements a data warehouse solution for analyzing sales data from the Superstore department store chain. It includes an ETL (Extract, Transform, Load) process to populate the data warehouse and an analysis tool to explore the sales data.

## Project Structure

- `src/main/java/de/dis/DataWarehouse.java`: Main application class implementing both ETL and analysis functionality
- `pom.xml`: Maven project configuration file
- `README.md`: This file

## Prerequisites

- Java 11 or higher
- Maven
- PostgreSQL database
- Access to the source database (credentials provided in the code)
- Sales data CSV file (located at `Blatt6/ressources/sales.csv`)

## Building the Project

To build the project, run the following command in the project root directory:

```bash
mvn clean package
```

This will create an executable JAR file with all dependencies included in the `target` directory.

## Running the Application

There are several ways to run the application:

### 1. Using the executable JAR (recommended)

The executable JAR is configured to run the interactive Analysis Tool by default:

```bash
java -jar target/data-warehouse-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 2. Using Maven Exec Plugin

You can use the Maven Exec Plugin to run either the DataWarehouse class or the AnalysisTool class:

To run the DataWarehouse class (automatic process):
```bash
mvn exec:java@run-warehouse
```

To run the AnalysisTool class (interactive interface):
```bash
mvn exec:java@run-tool
```

### 3. Using specific class with classpath

You can also run specific classes directly:

To run the DataWarehouse class:
```bash
java -cp target/data-warehouse-1.0-SNAPSHOT-jar-with-dependencies.jar de.dis.DataWarehouse
```

This will:
1. Connect to the database
2. Create the data warehouse schema
3. Run the ETL process to populate the data warehouse
4. Perform a sample analysis
5. Disconnect from the database

To run the AnalysisTool class:
```bash
java -cp target/data-warehouse-1.0-SNAPSHOT-jar-with-dependencies.jar de.dis.AnalysisTool
```

This launches an interactive command-line interface that allows you to:
1. Create the data warehouse schema
2. Run the ETL process
3. Run analyses with custom parameters
4. Exit the application

The interactive tool provides a more user-friendly way to explore the data warehouse and run different analyses without modifying the code.

## Data Warehouse Schema

The data warehouse follows a star schema design with the following tables:

### Dimension Tables

1. **DimTime**
   - TimeID (PK)
   - Date
   - Day
   - Month
   - Quarter
   - Year

2. **DimProduct**
   - ProductID (PK)
   - ArticleID
   - ArticleName
   - ProductGroupID
   - ProductGroupName
   - ProductFamilyID
   - ProductFamilyName
   - ProductCategoryID
   - ProductCategoryName
   - Price

3. **DimGeography**
   - GeographyID (PK)
   - ShopID
   - ShopName
   - CityID
   - CityName
   - RegionID
   - RegionName
   - CountryID
   - CountryName

### Fact Table

**FactSales**
   - SalesID (PK)
   - TimeID (FK)
   - ProductID (FK)
   - GeographyID (FK)
   - QuantitySold
   - Revenue

## ETL Process

The ETL process extracts data from two sources:
1. A PostgreSQL database containing store and product information
2. A CSV file containing sales data

The process transforms the data to fit the star schema and loads it into the data warehouse.

## Analysis Functionality

The application provides an analysis method that allows exploring the sales data at different granularity levels:

```java
analysis(String geo, String time, String product)
```

### Parameters

- **geo**: Geography level (shop, city, region, country)
- **time**: Time level (date, day, month, quarter, year)
- **product**: Product level (article, productGroup, productFamily, productCategory)

### Example

```java
dataWarehouse.analysis("region", "quarter", "productGroup");
```

This will generate a cross table showing sales revenue by region, quarter, and product group.

### Output

The output is a cross table with:
- Rows: Combinations of geography and time values
- Columns: Product values
- Cells: Revenue values
- Totals: Row, column, and grand totals

## Navigating the Data Cube

To navigate along the dimensional hierarchies (drill down, roll up), simply change the parameters in the `analysis` method:

- **Drill Down**: Move from a more general level to a more detailed level
  - Example: Change from `region` to `city` for geography dimension

- **Roll Up**: Move from a more detailed level to a more general level
  - Example: Change from `month` to `quarter` for time dimension

## Customization

You can modify the `main` method in `DataWarehouse.java` to run different analyses by changing the parameters to the `analysis` method.
