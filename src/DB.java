import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class handles all MySQL DataBase Communication.
 * 
 * @author Paul Burkart
 * @version 1.0
 * @since 2017-10-17
 *
 */

public class DB {
	private static Connection con = null;
	static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public static void insertMessage(String user, String message) throws SQLException{
		/*
		 * This Method Inserts a Message into the Database
		 */
		
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat?user=admin&password=password&useSSL=false");

		Date date = new Date();
		
		// INSERT INTO logs (timestamp, username, message) VALUES ("1970-01-01 12:00:00", "Guest", "This is a sample message.");
		String query = "INSERT INTO logs (timestamp, username, message) VALUES (\"" + dateFormat.format(date) + "\",\"" + user + "\",\"" + message + "\");";
		
		try { // Execute Query
			con.createStatement().executeUpdate(query);
		} catch (SQLException e) { 
			System.err.println("SQLException: " + e);
		}
	}
}
