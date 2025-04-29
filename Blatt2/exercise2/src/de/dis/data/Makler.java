
package de.dis.data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	
	/**
	 * Lädt einen Makler aus der Datenbank
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
				// damit später generierte IDs zurückgeliefert werden!
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


	public static List<Integer> getAllIds() {
		List<Integer> ids = new ArrayList<>();
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();
			String sql = "SELECT id FROM estateagent";
			PreparedStatement pstmt = con.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				ids.add(rs.getInt("id"));
			}

			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}

	public static String getAllIdsFormatted(){
		return getAllIds().stream().map(String::valueOf).collect(Collectors.joining(", \n"));  // Join with comma and space
	}

	public static boolean deleteById(int id) {
		if (!exists(id)){
			return false;
		}
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();
			String deleteSQL = "DELETE FROM estateagent WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(deleteSQL);
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			pstmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean exists(int id) {
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();
			String checkSQL = "SELECT 1 FROM estateagent WHERE id = ? LIMIT 1";
			PreparedStatement pstmt = con.prepareStatement(checkSQL);
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
	
			boolean found = rs.next(); // true if a row exists
	
			rs.close();
			pstmt.close();
	
			return found;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
