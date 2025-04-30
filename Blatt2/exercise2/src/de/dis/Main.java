package de.dis;

import java.util.List;
import java.util.stream.Collectors;

import de.dis.data.Makler;
import de.dis.data.estate.Estate;
import de.dis.data.estate.House;
import de.dis.data.estate.Apartment;

/**
 * Hauptklasse
 */
public class Main {
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
					//showContractManagementMenu();
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
			return;
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
		setHouseSettings(h, m, false);
	}
	
	public static void editHouse(Makler m) {
		System.out.println("Select house by id to edit:");
		System.out.println("____________");
		House.listAll();
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		House h = House.getHouse(id);
		System.out.println("Change House: " + h.toString());
		setHouseSettings(h, m, true);
		System.out.println("House with ID " + h.getId() + " was edited.");
	}
	
	public static void setHouseSettings(House h, Makler m, boolean isEdit) {
		setEstateSettings(h, m);
	
		String floorsStr = FormUtil.readString("Floors" + (isEdit ? " (leave empty to keep: " + h.getFloors() + ")" : ""));
		if (!floorsStr.isEmpty()) {
			h.setFloors(Integer.parseInt(floorsStr));
		}
	
		String priceStr = FormUtil.readString("Price" + (isEdit ? " (leave empty to keep: " + h.getPrice() + ")" : ""));
		if (!priceStr.isEmpty()) {
			h.setPrice(Integer.parseInt(priceStr));
		}
	
		String gardenStr = FormUtil.readString("Garden [y/n]" + (isEdit ? " (leave empty to keep: " + (h.hasGarden() ? "y" : "n") + ")" : ""));
		if (!gardenStr.isEmpty()) {
			h.setGarden(gardenStr.equalsIgnoreCase("y"));
		}
	
		h.save();
	}
	
	public static void setApartmentSettings(Apartment a, Makler m, boolean isEdit) {
		setEstateSettings(a, m);
	
		String floorStr = FormUtil.readString("Floor" + (isEdit ? " (leave empty to keep: " + a.getFloor() + ")" : ""));
		if (!floorStr.isEmpty()) {
			a.setFloor(Integer.parseInt(floorStr));
		}
	
		String rentStr = FormUtil.readString("Rent" + (isEdit ? " (leave empty to keep: " + a.getRent() + ")" : ""));
		if (!rentStr.isEmpty()) {
			a.setRent(Integer.parseInt(rentStr));
		}
	
		String roomsStr = FormUtil.readString("Rooms" + (isEdit ? " (leave empty to keep: " + a.getRooms() + ")" : ""));
		if (!roomsStr.isEmpty()) {
			a.setRooms(Integer.parseInt(roomsStr));
		}
	
		String balconyStr = FormUtil.readString("Balcony [y/n]" + (isEdit ? " (leave empty to keep: " + (a.hasBalcony() ? "y" : "n") + ")" : ""));
		if (!balconyStr.isEmpty()) {
			a.setBalcony(balconyStr.equalsIgnoreCase("y"));
		}
	
		String kitchenStr = FormUtil.readString("Built-in Kitchen [y/n]" + (isEdit ? " (leave empty to keep: " + (a.hasBuiltInKitchen() ? "y" : "n") + ")" : ""));
		if (!kitchenStr.isEmpty()) {
			a.setBuiltInKitchen(kitchenStr.equalsIgnoreCase("y"));
		}
	
		a.save();
	}
	
	public static void editApartment(Makler m) {
		System.out.println("Select apartment by id to edit:");
		System.out.println("____________");
		Apartment.listAll();
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		Apartment a = Apartment.getApartment(id);
		System.out.println("Change Apartment: " + a.toString());
		setApartmentSettings(a, m, true);
		System.out.println("Apartment with ID " + a.getId() + " was edited.");
	}
	
	public static void newApartment(Makler m) {
		Apartment a = new Apartment();
		setApartmentSettings(a, m, false);
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

}
