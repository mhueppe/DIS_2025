package de.dis;

import de.dis.data.contract.PurchaseContract;
import de.dis.data.contract.TenancyContract;
import de.dis.data.contract.Contract;
import de.dis.data.Makler;
import de.dis.data.Person;
import de.dis.data.estate.Estate;
import de.dis.data.estate.House;
import de.dis.data.estate.Apartment;

/**
 * Hauptklasse
 */
public class Main_2 {
	/**
	 * Startet die Anwendung
	 */
	public static void main(String[] args) {
		showMainMenu();
	}
	
	/**
	 * Zeigt das Hauptmenü
	 */
	public static void showMainMenu() {
		//Menüoptionen
		final int MENU_ESTATE_AGENT = 0;
		final int MENU_ESTATE_MANAGEMENT = 1;
		final int MENU_CONTRACT_MANAGEMENT = 2;
		final int QUIT = 3;
		
		//Erzeuge Menü
		Menu mainMenu = new Menu("Main Menu");
		mainMenu.addEntry("Estate Agent Menu", MENU_ESTATE_AGENT);
		mainMenu.addEntry("Estate Menu", MENU_ESTATE_MANAGEMENT);
		mainMenu.addEntry("Contract Management", MENU_CONTRACT_MANAGEMENT);

		mainMenu.addEntry("Quit", QUIT);
		
		//Verarbeite Eingabe
		while(true) {
			int response = mainMenu.show();
			
			switch(response) {
				case MENU_ESTATE_AGENT:
					showMaklerMenu();
					break;
				case MENU_ESTATE_MANAGEMENT:
					showEstateManagementMenu();
					break;
				case MENU_CONTRACT_MANAGEMENT:
					showContractManagementMenu();
					break;
				case QUIT:
					return;
			}
		}
	}
	
	
	/**
	 * Zeigt die Maklerverwaltung
	 */
	public static void showMaklerMenu() {
		//Menüoptionen
		final int NEW_ESTATE_AGENT = 0;
		final int EDIT_ESTATE_AGENT = 1;
		final int REMOVE_ESTATE_AGENT = 2;
		final int BACK = 3;
		
		//Maklerverwaltungsmenü
		Menu maklerMenu = new Menu("Estate Agent Menu");
		maklerMenu.addEntry("New Estate Agent", NEW_ESTATE_AGENT);
		maklerMenu.addEntry("Edit Estate Agent", EDIT_ESTATE_AGENT);
		maklerMenu.addEntry("Remove Estate Agent", REMOVE_ESTATE_AGENT);
		maklerMenu.addEntry("Back to main menu", BACK);
		
		//Verarbeite Eingabe
		while(true) {
			int response = maklerMenu.show();
			
			switch(response) {
				case NEW_ESTATE_AGENT:
					newMakler();
					break;
				case EDIT_ESTATE_AGENT:
					if (adminAccess()){
						editMakler();
					}else{
						System.out.println("Wrong password.");
					}
					break;
				case REMOVE_ESTATE_AGENT:
					if (adminAccess()){
						removeMakler();
					}else{
						System.out.println("Wrong password.");
					}
					break;
				case BACK:
					return;
			}
		}
	}
	public static boolean adminAccess(){
		String enteredPass = FormUtil.readString("Please enter the admin password for this action. Admin Password");
		return enteredPass.equals("12345");
	}

	/**
	 * Legt einen neuen Makler an, nachdem der Benutzer
	 * die entprechenden Daten eingegeben hat.
	 */
	public static void newMakler() {
		Makler m = new Makler();
		setMaklerSettings(m);
		System.out.println("Estate agent with ID "+m.getId()+" was created.");
	}

	public static void setMaklerSettings(Makler m) {
		m.setName(FormUtil.readString("Name"));
		m.setAddress(FormUtil.readString("Address"));
		m.setLogin(FormUtil.readString("Login"));
		m.setPassword(FormUtil.readString("Password"));
		m.save();
	}

	public static void editMakler() {
		System.out.println("Select estate agent by id to edit:");
		System.out.println("____________");
		Makler.listAll();
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		Makler m = Makler.load(id);
		setMaklerSettings(m);
		System.out.println("Estate agent with ID "+m.getId()+" was edited.");
	}

	public static void removeMakler() {		
		System.out.println("Select estate agent id to remove:");
		System.out.println("____________");
		Makler.listAll();
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		Makler.delete(id);
	}

	// ----------- Estate Management Menu
	public static void showEstateManagementMenu() {
		//Menüoptionen
		final int NEW_ESTATE = 0;
		final int EDIT_ESTATE = 1;
		final int REMOVE_ESTATE = 2;
		final int BACK = 3;
		
		String login = FormUtil.readString("Login");
		String pass = FormUtil.readString("Password");
		Makler m = Makler.login(login, pass);
		if (m != null){
			System.out.println("Login sucessfull.");
		}else{
			System.out.println("Login failed.");
		}

		//Maklerverwaltungsmenü
		Menu estateMenu = new Menu("Estate Menu");
		estateMenu.addEntry("New Estate", NEW_ESTATE);
		estateMenu.addEntry("Edit Estate", EDIT_ESTATE);
		estateMenu.addEntry("Remove Estate", REMOVE_ESTATE);
		estateMenu.addEntry("Back to main menu", BACK);
		
		//Verarbeite Eingabe
		while(true) {
			int response = estateMenu.show();
			
			switch(response) {
				case NEW_ESTATE:
					newEstate(m);
					break;
				case EDIT_ESTATE:
					editEstate(m);
					break;
				case REMOVE_ESTATE:
					removeEstate();
					break;
				case BACK:
					return;
			}
		}
	}

	public static int estateTypeSelection(){
		final int HOUSE = 0;
		final int APARTMENT = 1;
		final int BACK = 2;
		Menu estateTypeMenu = new Menu("Estate Type Menu");
		estateTypeMenu.addEntry("House", HOUSE);
		estateTypeMenu.addEntry("Apartment", APARTMENT);
		estateTypeMenu.addEntry("Back", BACK);
		return estateTypeMenu.show();
	}

	public static void newEstate(Makler m) {
		final int HOUSE = 0;
		final int APARTMENT = 1;
		final int BACK = 2;
		switch(estateTypeSelection()) {
			case HOUSE:
				newHouse(m);
				break;
			case APARTMENT:
				newApartment(m);
				break;
			case BACK:
				return;
		}
	}

	public static void editEstate(Makler m) {
		final int HOUSE = 0;
		final int APARTMENT = 1;
		final int BACK = 2;
		switch(estateTypeSelection()) {
			case HOUSE:
				editHouse(m);
				break;
			case APARTMENT:
				editApartment(m);
				break;
			case BACK:
				return;
		}
	}
	
	public static void setEstateSettings(Estate e, Makler m) {
		e.setCity(FormUtil.readString("City"));
		e.setPostalCode(FormUtil.readString("Postal Code"));
		e.setStreet(FormUtil.readString("Street"));
		e.setStreetNumber(FormUtil.readString("Street Number"));
		e.setSquareArea(FormUtil.readInt("Square Area"));
		e.setAgentId(m.getId());
	}
	
	public static void newHouse(Makler m) {
		House h = new House();
		setHouseSettings(h, m);
	}

	public static void setHouseSettings(House h, Makler m) {
		setEstateSettings(h, m);
		h.setFloors(FormUtil.readInt("Floors"));
		h.setPrice(FormUtil.readInt("Price"));
		h.setGarden(FormUtil.readString("Garden [y/n]").equals("y"));
		h.save();
	}

	public static void editHouse(Makler m) {
		System.out.println("Select house by id to edit:");
		System.out.println("____________");
		House.listAll();
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		House h = House.getHouse(id);
		System.out.println("Change House:" + h.toString());
		setHouseSettings(h, m);
		System.out.println("House with ID "+h.getId()+" was edited.");
	}

	public static void newApartment(Makler m) {
		Apartment a = new Apartment();
		setApartmentSettings(a, m);
	}

	public static void setApartmentSettings(Apartment a, Makler m) {
		setEstateSettings(a, m);
		a.setFloor(FormUtil.readInt("Floor"));
		a.setRent(FormUtil.readInt("Rent"));
		a.setRooms(FormUtil.readInt("Rooms"));
		a.setBalcony(FormUtil.readString("Balcony [y/n]").equals("y"));
		a.setBuiltInKitchen(FormUtil.readString("Built-in Kitchen [y/n]").equals("y"));
		a.save();
	}

	public static void editApartment(Makler m) {
		System.out.println("Select apartment by id to edit:");
		System.out.println("____________");
		Apartment.listAll();
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		Apartment a = Apartment.getApartment(id);
		System.out.println("Change Apartment:" + a.toString());
		setApartmentSettings(a, m);
		System.out.println("Apartment with ID "+a.getId()+" was edited.");
	}

	public static void removeEstate() {
		final int HOUSE = 0;
		final int APARTMENT = 1;
		final int BACK = 2;
		switch(estateTypeSelection()) {
			case HOUSE:
				removeHouse();
				break;
			case APARTMENT:
				removeApartment();
				break;
			case BACK:
				return;
		}
	}

	public static void removeHouse() {
		System.out.println("Select house id to remove:");
		System.out.println("____________");
		House.listAll();
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		House.delete(id);
	}

	public static void removeApartment() {
		System.out.println("Select Apartment id to remove:");
		System.out.println("____________");
		Apartment.listAll();
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		Apartment.delete(id);
	}


	/**
	 * Shows the contract management menu
	 */
	public static void showContractManagementMenu() {
		//Menu options
		final int NEW_PERSON = 0;
		final int CREATE_CONTRACT = 1;
		final int SHOW_ALL_CONTRACTS = 2;
		final int BACK = 3;

		//Contract management menu
		Menu contractMenu = new Menu("Contract Management");
		contractMenu.addEntry("Insert New Person", NEW_PERSON);
		contractMenu.addEntry("Create Contract", CREATE_CONTRACT);
		contractMenu.addEntry("Show All Contracts", SHOW_ALL_CONTRACTS);
		contractMenu.addEntry("Back to main menu", BACK);

		//Process input
		while(true) {
			int response = contractMenu.show();

			switch(response) {
				case NEW_PERSON:
					insertPerson();
					break;
				case CREATE_CONTRACT:
					createContract();
					break;
				case SHOW_ALL_CONTRACTS:
					showAllContracts();
					break;
				case BACK:
					return;
			}
		}
	}

	/**
	 * Inserts a new person after collecting required data
	 */
	public static void insertPerson() {
		Person p = new Person();

		p.setFirstName(FormUtil.readString("First Name"));
		p.setName(FormUtil.readString("Last Name"));
		p.setAddress(FormUtil.readString("Address"));
		p.save();

		System.out.println("Person with ID "+p.getId()+" was created.");
	}

	/**
	 * Creates a new contract after collecting required data
	 */
	public static void createContract() {
		System.out.println("Contract type:");
		System.out.println("1: Purchase contract (House)");
		System.out.println("2: Tenancy contract (Apartment)");
		Contract c;
		int contractTypeChoice = FormUtil.readInt("Type (1 or 2)");

		// Select contract type (purchase or tenancy)
		if (contractTypeChoice == 1) {
			c = new PurchaseContract();
			// Display available houses
			House.listAll();
			c.setEstateId(FormUtil.readInt("House ID"));
			((PurchaseContract) c).setNumberOfInstallments(FormUtil.readInt("Number of Installments: "));
			((PurchaseContract) c).setInterestRate(FormUtil.readInt("Interest Rate: "));


		} else {
			c = new TenancyContract();
			// Display available apartments
			Apartment.listAll();
			c.setEstateId(FormUtil.readInt("Apartment ID"));
			((TenancyContract) c).setStartDate(java.sql.Date.valueOf(FormUtil.readString("Date (YYYY-MM-DD): ")));
			((TenancyContract) c).setDuration(FormUtil.readInt("Duration (in months): "));
			((TenancyContract) c).setAdditionalCosts(FormUtil.readInt("Additional Cost: "));
		}


		// Display persons to select
		System.out.println("Available persons:");
		System.out.println("____________");
		System.out.println(Person.getAllFormatted());
		System.out.println("____________");
		c.setPersonId(FormUtil.readInt("Person ID"));

		// Display estate agents to select
		//System.out.println("Available estate agents:");
		//System.out.println("____________");
		//Makler.listAll();
		//System.out.println("____________");
		//c.setEstateAgentId(FormUtil.readInt("Estate Agent ID"));



		c.setDate(java.sql.Date.valueOf(FormUtil.readString("Date (YYYY-MM-DD)")));
		c.setPlace(FormUtil.readString("Place"));
		c.save();

		System.out.println("Contract with ID " + c.getContractNo() + " was created.");
    }

	/**
	 * Shows all contracts
	 */
	public static void showAllContracts() {

		System.out.println("All Purchase Contracts:");
		System.out.println("=============");
		PurchaseContract.listAll();
		System.out.println("All Tenancy Contracts:");
		System.out.println("=============");
		TenancyContract.listAll();
	}
}
