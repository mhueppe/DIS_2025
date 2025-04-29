package de.dis;

import java.util.List;
import java.util.stream.Collectors;

import de.dis.data.Makler;

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
		final int QUIT = 1;
		
		//Erzeuge Menü
		Menu mainMenu = new Menu("Main Menu");
		mainMenu.addEntry("Estate Agent Menu", MENU_ESTATE_AGENT);
		mainMenu.addEntry("Quit", QUIT);
		
		//Verarbeite Eingabe
		while(true) {
			int response = mainMenu.show();
			
			switch(response) {
				case MENU_ESTATE_AGENT:
					showMaklerMenu();
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

	public static boolean adminAccess(){
		String enteredPass = FormUtil.readString("Please enter the admin password for this action. Admin Password");
		return enteredPass.equals("12345");
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
}
