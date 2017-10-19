import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * A Simple Java Chat Client. 
 * 
 * Usage: java Client [username] [port] [server]
 * 
 * Defaults:
 * 	username: Guest
 * 	port: 1500
 * 	server: localhost
 * 
 * @author Paul Burkart
 * @version 1.0
 * @since 2017-10-17
 */

public class Client {
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput; 
	private Socket socket;
	private SimpleDateFormat sdf;
	private ClientInterface gui;
	
	private String server, user;
	private int port;
	
	/*
	 * Constructor used by Console 
	 * 
	 * Server: Server Address
	 * Port: Port Number
	 * User: Username
	 */
	Client(String server, int port, String user){
		this(server, port, user, null);
	}
	
	/*
	 * Constructor used by GUI
	 */
	Client(String server, int port, String user, ClientInterface gui){
		sdf = new SimpleDateFormat("HH:mm:ss");
		this.server = server;
		this.port = port;
		this.user = user;
		this.gui = gui;
	}
	
	// Starts Client-Server Communication
	public boolean start() {
		try {
			socket = new Socket(server, port);
		} catch (Exception e) {
			display("Exception: " + e);
			return false;
		}
		
		String time = sdf.format(new Date());
		String msg = time + "  Connection Accepted: " + socket.getInetAddress() + ":" + socket.getPort();
		
		display(msg);
		
		try {
			// Create Streams
			sInput = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			display("IOException Creating Stream: " + e);
			return false;
		}
		
		new ListenFromServer().start();
		
		try {
			// Sends Username to Server
			sOutput.writeObject(user); 
		} catch (IOException e) {
			display("Exception: " + e);
			return false;
		}
		
		return true;
	}
	
	
	// Formats Messages for GUI or Console based usage
	private void display(String msg) {
		if(gui == null)
			System.out.println(msg);
		else
			gui.append(msg + "\n");
	}
	
	// Sends message to Server
	void sendMessage(Message msg) {
		try { 
			sOutput.writeObject(msg);
		} catch (IOException e) {
			display("Exception: " + e);
		}
	}
	
	
	// Close Streams and Disconnect
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		} catch (Exception e) {}
		
		try {
			if(sOutput != null) sOutput.close();
		} catch (Exception e) {}
		
		try {
			if (socket != null) socket.close();
		} catch (Exception e) {}
		
		
		// Update GUI
		if(gui != null)
			gui.connectionFailed();
	}
	
	
	/*
	 * 
	 * Client Console Usage:
	 * 	java Client 
	 * 	java Client user
	 * 	java Client user port
	 * 	java Client user port host
	 * 
	 * Defaults:
	 * 	user: Guest
	 * 	port: 1500
	 * 	host: localhost
	 * 
	 */
	public static void main(String[] args) throws Exception {
		int port = 1500;
		String server = "localhost";
		String user = "Guest";
		
		switch(args.length) {
		case 3: // java Client user port host
			server = args[2];
		case 2: // java Client user port
			try {
				port = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: java Client [user] [port] [address]");
				return;
			}
		case 1: // java Client user
			user = args[0];
		
		case 0: // java Client
			break; 
		
		default: // invalid arguments
			System.out.println("Usage is: java Client [user] [port] [address]");
			return;
		}
		
		// Create New Client Object
		Client client = new Client(server, port, user); 
		
		if(!client.start())
			return;
		
		Scanner scan = new Scanner(System.in);
		
		// Read and Send Input to Server
		while(true) {
			System.out.print("> ");
			
			String msg = scan.nextLine();
			
			if(msg.equalsIgnoreCase("LOGOUT")) { // Logout
				client.sendMessage(new Message(Message.LOGOUT, "", user));
				break;
			} else if (msg.equalsIgnoreCase("LISTUSERS")) { // List Users
				client.sendMessage(new Message(Message.LISTUSERS, "", user));
			} else { // Send Message
				client.sendMessage(new Message(Message.MESSAGE, msg, user));
			}
			
			scan.close();
		}
		
		client.disconnect();
	}
	
	
	/*
	 * Appends new messages to JTextArea OR Prints new messages if in console mode 
	 */
	class ListenFromServer extends Thread {
		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					if(gui == null) { // if console mode
						System.out.println(msg);;
						System.out.print("> ");
					} else {
						gui.append(msg);
					}
				} catch (IOException e) {
					if(gui != null)
						gui.connectionFailed();
					break;
				} catch (ClassNotFoundException e) {}
				
			}
		}
	}
	
}
