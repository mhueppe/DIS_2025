DROP TABLE IF EXISTS PurchaseContract, TenancyContract, Contract, House, Apartment, Estate, Person, EstateAgent CASCADE;

CREATE TABLE EstateAgent (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(255) NOT NULL,
                             address VARCHAR(255),
                             login VARCHAR(100) UNIQUE NOT NULL,
                             password VARCHAR(100) NOT NULL
);

CREATE TABLE Person (
                        id SERIAL PRIMARY KEY,
                        first_name VARCHAR(100) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        address VARCHAR(255)
);

CREATE TABLE Estate (
                        id SERIAL PRIMARY KEY,
                        city VARCHAR(100) NOT NULL,
                        postal_code VARCHAR(20) NOT NULL,
                        street VARCHAR(100) NOT NULL,
                        street_number VARCHAR(20) NOT NULL,
                        square_area FLOAT NOT NULL,
                        agent_id INTEGER NOT NULL,
                        FOREIGN KEY (agent_id) REFERENCES EstateAgent(id) ON DELETE CASCADE
);

CREATE TABLE House (
                       estate_id INTEGER PRIMARY KEY,
                       floors INTEGER NOT NULL,
                       price FLOAT NOT NULL,
                       garden BOOLEAN NOT NULL,
                       FOREIGN KEY (estate_id) REFERENCES Estate(id) ON DELETE CASCADE
);

CREATE TABLE Apartment (
                           estate_id INTEGER PRIMARY KEY,
                           floor INTEGER NOT NULL,
                           rent FLOAT NOT NULL,
                           rooms INTEGER NOT NULL,
                           balcony BOOLEAN NOT NULL,
                           built_in_kitchen BOOLEAN NOT NULL,
                           FOREIGN KEY (estate_id) REFERENCES Estate(id) ON DELETE CASCADE
);

CREATE TABLE Contract (
                          contract_no SERIAL PRIMARY KEY,
                          date DATE NOT NULL,
                          place VARCHAR(100) NOT NULL,
                          person_id INTEGER NOT NULL,
                          estate_id INTEGER NOT NULL,
                          FOREIGN KEY (person_id) REFERENCES Person(id) ON DELETE CASCADE,
                          FOREIGN KEY (estate_id) REFERENCES Estate(id) ON DELETE CASCADE
);

CREATE TABLE PurchaseContract (
                                  contract_no INTEGER PRIMARY KEY,
                                  no_of_installments INTEGER NOT NULL,
                                  interest_rate FLOAT NOT NULL,
                                  FOREIGN KEY (contract_no) REFERENCES Contract(contract_no) ON DELETE CASCADE
);

CREATE TABLE TenancyContract (
                                 contract_no INTEGER PRIMARY KEY,
                                 start_date DATE NOT NULL,
                                 duration INTEGER NOT NULL,
                                 additional_costs FLOAT NOT NULL,
                                 FOREIGN KEY (contract_no) REFERENCES Contract(contract_no) ON DELETE CASCADE
);