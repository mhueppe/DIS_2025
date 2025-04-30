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
		mainMenu.addEntry("Estate Menu", MENU_ESTATE_AGENT);
		mainMenu.addEntry("Contract Management", MENU_ESTATE_AGENT);

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
		
		m.setName(FormUtil.readString("Name"));
		m.setAddress(FormUtil.readString("Address"));
		m.setLogin(FormUtil.readString("Login"));
		m.setPassword(FormUtil.readString("Password"));
		m.save();
		
		System.out.println("Estate agent with ID "+m.getId()+" was created.");
	}	

	public static void editMakler() {
		System.out.println("Select estate agent by id to edit:");
		System.out.println("____________");
		System.out.println(Makler.getAllIdsFormatted());
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		Makler m = Makler.load(id);
		
		m.setName(FormUtil.readString("Name"));
		m.setAddress(FormUtil.readString("Address"));
		m.setLogin(FormUtil.readString("Login"));
		m.setPassword(FormUtil.readString("Password"));
		m.save();
		
		System.out.println("Estate agent with ID "+m.getId()+" was edited.");
	}

	public static void removeMakler() {		
		System.out.println("Select estate agent id to remove:");
		System.out.println("____________");
		System.out.println(Makler.getAllIdsFormatted());
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		if (Makler.deleteById(id)){
			System.out.println("Estate agent with id: "+ id +" was removed.");
		}else{
			System.out.println("Invalid id.");
		};
	}

	// ----------- Estate Management Menu
	public static void showEstateManagementMenu() {
		//Menüoptionen
		final int NEW_ESTATE = 0;
		final int EDIT_ESTATE = 1;
		final int REMOVE_ESTATE = 2;
		final int BACK = 3;
		
		String login = FormUtil.readString("Login");
		int id = Makler.getIdByLogin(login);
		if (id == -1){
			System.out.println("Invalid login.");
			return;
		}
		Makler makler = Makler.load(id);
		String pass = FormUtil.readString("Password");
		if (!makler.getPassword().equals(pass)){
			System.out.println("Invalid password.");
			return;
		}
		
		
		//Maklerverwaltungsmenü
		Menu estateMenu = new Menu("Estate Menu");
		estateMenu.addEntry("New Estate Agent", NEW_ESTATE);
		estateMenu.addEntry("Edit Estate Agent", EDIT_ESTATE);
		estateMenu.addEntry("Remove Estate Agent", REMOVE_ESTATE);
		estateMenu.addEntry("Back to main menu", BACK);
		
		//Verarbeite Eingabe
		while(true) {
			int response = estateMenu.show();
			
			switch(response) {
				case NEW_ESTATE:
					newEstate();
					break;
				case EDIT_ESTATE:
					editEstate();
					break;
				case REMOVE_ESTATE:
					removeEstate();
					break;
				case BACK:
					return;
			}
		}
	}

	public static void newEstate() {
		//Maklerverwaltungsmenü
		final int HOUSE = 0;
		final int APARTMENT = 1;
		final int BACK = 2;
		Menu estateTypeMenu = new Menu("Estate Type Menu");
		estateTypeMenu.addEntry("House", HOUSE);
		estateTypeMenu.addEntry("Apartment", APARTMENT);
		estateTypeMenu.addEntry("Back", BACK);
		int response = estateTypeMenu.show();

		switch(response) {
			case HOUSE:
				newHouse();
				break;
			case APARTMENT:
				newApartment();
				break;
			case BACK:
				return;
		}
		
	}
	
	public static void setEstateSettings(Estate e) {
		e.setCity(FormUtil.readString("City"));
		e.setPostalCode(FormUtil.readString("Postal Code"));
		e.setStreet(FormUtil.readString("Street"));
		e.setStreetNumber(FormUtil.readString("Street Number"));
		e.setSquareArea(FormUtil.readInt("Square Area"));
	}
	

	public static void newHouse() {
		House m = new House();
		setEstateSettings(m);
		m.setFloors(FormUtil.readInt("Floors"));
		m.setPrice(FormUtil.readInt("Price"));
		m.setGarden(FormUtil.readString("Garden [y/n]").equals("y"));
		m.save();
	}

	public static void newApartment() {
		Apartment a = new Apartment();
		setEstateSettings(a);
		a.setFloor(FormUtil.readInt("Floor"));
		a.setRent(FormUtil.readInt("Rent"));
		a.setRooms(FormUtil.readInt("Rooms"));
		a.setBalcony(FormUtil.readString("Balcony [y/n]").equals("y"));
		a.setBuiltInKitchen(FormUtil.readString("Built-in Kitchen [y/n]").equals("y"));
		a.save();
	}
	public static void editEstate() {
		System.out.println("Select estate agent by id to edit:");
		System.out.println("____________");
		System.out.println(Makler.getAllIdsFormatted());
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		Makler m = Makler.load(id);
		
		m.setName(FormUtil.readString("Name"));
		m.setAddress(FormUtil.readString("Address"));
		m.setLogin(FormUtil.readString("Login"));
		m.setPassword(FormUtil.readString("Password"));
		m.save();
		
		System.out.println("Estate agent with ID "+m.getId()+" was edited.");
	}

	public static void removeEstate() {		
		System.out.println("Select estate agent id to remove:");
		System.out.println("____________");
		System.out.println(Makler.getAllIdsFormatted());
		System.out.println("____________");
		int id = FormUtil.readInt("Id");
		if (Makler.deleteById(id)){
			System.out.println("Estate agent with id: "+ id +" was removed.");
		}else{
			System.out.println("Invalid id.");
		};
	}
}
