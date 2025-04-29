
--Estate table

CREATE TABLE Estate (
    ID SERIAL PRIMARY KEY,
    City VARCHAR(100),
    Postal_Code VARCHAR(10),
    Street VARCHAR(100),
    Street_Number VARCHAR(10),
    Square_Area DECIMAL(10, 2)
);

--Estate Agent table

CREATE TABLE Estate_Agent (
    ID SERIAL PRIMARY KEY,
    Name VARCHAR(100),
    Address VARCHAR(255),
    Login VARCHAR(50) UNIQUE,
    Password VARCHAR(100)
);

-- Apartment table inheriting from Estate

CREATE TABLE Apartment (
    ID SERIAL PRIMARY KEY,
    Estate_ID INT REFERENCES Estate(ID) ON DELETE CASCADE,
    Floor INT,
    Rent DECIMAL(10, 2),
    Rooms INT,
    Balcony BOOLEAN,
    Built_in_Kitchen BOOLEAN
);

--House table inheriting from Estate

CREATE TABLE House (
    ID SERIAL PRIMARY KEY,
    Estate_ID INT REFERENCES Estate(ID) ON DELETE CASCADE,
    Floors INT,
    Price DECIMAL(10, 2),
    Garden BOOLEAN
);

-- Person table

CREATE TABLE Person (
    ID SERIAL PRIMARY KEY,
    First_Name VARCHAR(100),
    Last_Name VARCHAR(100),
    Address VARCHAR(255)
);

-- Contract table

CREATE TABLE Contract (
    Contract_No SERIAL PRIMARY KEY,
    Contract_Date DATE,
    Settlement_Place VARCHAR(255)
);

--  Tenancy Contract table inheriting from Contract

CREATE TABLE Tenancy_Contract (
    ID SERIAL PRIMARY KEY,
    Contract_No INT REFERENCES Contract(Contract_No) ON DELETE CASCADE,
    Start_Date DATE,
    Duration INT,
    Additional_Costs DECIMAL(10, 2)
);

-- Purchase Contract table inheriting from Contract

CREATE TABLE Purchase_Contract (
    ID SERIAL PRIMARY KEY,
    Contract_No INT REFERENCES Contract(Contract_No) ON DELETE CASCADE,
    No_of_Installments INT,
    Interest_Rate DECIMAL(5, 2)
);

CREATE TABLE Rents (
    Person_ID INT REFERENCES Person(ID),
    Tenancy_Contract_ID INT REFERENCES Tenancy_Contract(ID),
    Apartment_ID INT REFERENCES Apartment(ID), -- reference to the Apartment
    PRIMARY KEY (Person_ID, Tenancy_Contract_ID, Apartment_ID)
);

-- Updated Purchase Contract relationship to include House
CREATE TABLE Sells (
    Person_ID INT REFERENCES Person(ID),
    Purchase_Contract_ID INT REFERENCES Purchase_Contract(ID),
    House_ID INT REFERENCES House(ID), -- reference to the House
    PRIMARY KEY (Person_ID, Purchase_Contract_ID, House_ID)
);