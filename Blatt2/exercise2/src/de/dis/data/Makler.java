package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Makler-Bean
 * 
 * Beispiel-Tabelle:
 * CREATE TABLE makler (
 * name varchar(255),
 * address varchar(255),
 * login varchar(40) UNIQUE,
 * password varchar(40),
 * id serial primary key);
 */
public class Makler {
	private int id = -1;
	private String name;
	private String address;
	private String login;
	private String password;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "Estate Agent [id=" + id + ", name=" + name + ", address=" + address
				+ ", login=" + login + "]";
	}

	/**
	 * Lädt einen Makler aus der Datenbank
	 * 
	 * @param id ID des zu ladenden Maklers
	 * @return Makler-Instanz
	 */
	public static Makler load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM estateagent WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Makler ts = new Makler();
				ts.setId(id);
				ts.setName(rs.getString("name"));
				ts.setAddress(rs.getString("address"));
				ts.setLogin(rs.getString("login"));
				ts.setPassword(rs.getString("password"));

				rs.close();
				pstmt.close();
				return ts;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Speichert den Makler in der Datenbank. Ist noch keine ID vergeben
	 * worden, wird die generierte Id von der DB geholt und dem Model übergeben.
	 */
	public void save() {
		// Hole Verbindung
		Connection con = DbConnectionManager.getInstance().getConnection();

		try {
			// FC<ge neues Element hinzu, wenn das Objekt noch keine ID hat.
			if (getId() == -1) {
				// Achtung, hier wird noch ein Parameter mitgegeben,
				// damit spC$ter generierte IDs zurC<ckgeliefert werden!
				String insertSQL = "INSERT INTO estateagent(name, address, login, password) VALUES (?, ?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setString(1, getName());
				pstmt.setString(2, getAddress());
				pstmt.setString(3, getLogin());
				pstmt.setString(4, getPassword());
				pstmt.executeUpdate();

				// Hole die Id des engefC<gten Datensatzes
				ResultSet rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					setId(rs.getInt(1));
				}

				rs.close();
				pstmt.close();
			} else {
				// Falls schon eine ID vorhanden ist, mache ein Update...
				String updateSQL = "UPDATE estateagent SET name = ?, address = ?, login = ?, password = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);

				// Setze Anfrage Parameter
				pstmt.setString(1, getName());
				pstmt.setString(2, getAddress());
				pstmt.setString(3, getLogin());
				pstmt.setString(4, getPassword());
				pstmt.setInt(5, getId());
				pstmt.executeUpdate();

				pstmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gibt den Makler von der ID zurück
	 */
	public static Makler getMakler(int id) {
		Connection con = DbConnectionManager.getInstance().getConnection();

		try {
			String selectSQL = "SELECT * FROM estateagent WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				Makler m = new Makler();
				m.setId(rs.getInt("id"));
				m.setName(rs.getString("name"));
				m.setAddress(rs.getString("address"));
				m.setLogin(rs.getString("login"));
				m.setPassword(rs.getString("password"));
				rs.close();
				pstmt.close();
				return m;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Löscht den Makler aus der Datenbank
	 */
	public static void delete(int id) {
		Connection con = DbConnectionManager.getInstance().getConnection();

		try {
			String deleteSQL = "DELETE FROM estateagent WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(deleteSQL);
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gibt alle Makler aus der Datenbank zurück
	 * 
	 * @return Array mit Makler-Objekten
	 */
	public static Makler[] getAllMakler() {
		Connection con = DbConnectionManager.getInstance().getConnection();

		try {
			String selectSQL = "SELECT * FROM estateagent";
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(selectSQL);

			// Zähle die Anzahl der Datensätze
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();

			// Erzeuge Array mit Makler-Objekten
			Makler[] makler = new Makler[count];
			int i = 0;
			while (rs.next()) {
				Makler m = new Makler();
				m.setId(rs.getInt("id"));
				m.setName(rs.getString("name"));
				m.setAddress(rs.getString("address"));
				m.setLogin(rs.getString("login"));
				m.setPassword(rs.getString("password"));
				makler[i++] = m;
			}

			rs.close();
			stmt.close();
			return makler;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gibt alle Makler aus der Datenbank aus
	 */
	public static void listAll() {
		Makler[] makler = getAllMakler();
		if (makler != null && makler.length > 0) {
			for (Makler m : makler) {
				System.out.println(m);
			}
		} else {
			System.out.println("Keine Makler gefunden.");
		}
	}

	public static Makler login(String login, String password) {
		Connection con = DbConnectionManager.getInstance().getConnection();

		try {
			String selectSQL = "SELECT * FROM estateagent WHERE login = ? AND password = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setString(1, login);
			pstmt.setString(2, password);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				int id = rs.getInt("id");
				rs.close();
				pstmt.close();
				return load(id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}