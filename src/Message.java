import java.io.Serializable;
import java.sql.SQLException;

/**
 * This class handles the communication between the Server & Client
 * 
 * @author Paul Burkart
 * @version 1.0
 * @since 2017-10-17
 */

public class Message implements Serializable{
	protected static final long serialVersionUID = 2L;
	
	/**
	 * Different Server Operations
	 * 
	 * LISTUSERS - List users in Chat
	 * MESSAGE - Send Message
	 * LOGOUT - Disconnect from Server
	 */
	
	static final int LISTUSERS = 0, MESSAGE = 1, LOGOUT = 2;
	
	private int type;
	private String message;
	private String user;

	// Constructor

	Message(int type, String message, String user) {
		this.type = type;
		this.message = message;
		this.user = user;
	}

	// Return Type of Operation
	int Type() {
		return type;
	}
	
	// Insert Message into DataBase and Return Contents
	String messageContent() {
		try {
			if(!message.isEmpty()) 
				DB.insertMessage(user, message);
		} catch (SQLException e) {
			System.err.println("SQLException: " + e);
		} catch (Exception e) {
			System.err.println("Exception: " + e);
		}
		return message;
	}
}
