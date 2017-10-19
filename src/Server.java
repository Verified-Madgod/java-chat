import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A Simple Java Chat Server that stores logs to a MySQL DataBase 
 * via the JDBC API. 
 * 
 * Usage: 
 * 	java Server 
 * 	java Server [port]
 * 
 * Defaults:
 * 	port: 1500
 * 
 * @author Paul Burkart 
 * @version 1.0
 * @since 2017-10-17
 * @
 * 
 */

public class Server {
	private static int uniqueId;
	private ArrayList<ClientThread> clientList;
	private ServerInterface gui;
	private SimpleDateFormat sdf;
	private int port;
	private boolean repeat;
	
	// Constructor, Receives Port Passed as Parameter
	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, ServerInterface gui) {
		this.gui = gui;
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		clientList = new ArrayList<ClientThread>();
	}
	
	// Start the Server
	public void start() {
		repeat = true;
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			// Wait for Connections
			display("Server listening on port " + port);
			while(repeat) {
				Socket socket = serverSocket.accept();
				
				if(!repeat)
					break;
				
				ClientThread t = new ClientThread(socket);
				clientList.add(t);
				t.start();
			}
		
			// Close Connections
			try { 
				serverSocket.close();
				for(int i = 0; i < clientList.size(); ++i) {
					ClientThread tc = clientList.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					} catch (IOException e) {
						System.err.println("IOException: " + e);
					}
				}
			} catch(Exception e) {
				display("Exception: " + e);
			}
		} catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception: " + e + "\n";
			display(msg);
		}
	}
	
	
	// Stops Server
	protected void stop() {
		repeat = false;
		try {
			new Socket("localhost", port).close(); // Connect to Localhost as a Client to Terminate Server
		} catch (Exception e) {
			System.err.println("Exception: " + e);
		}
	}
	
	
	// Format Messages for GUI or Console based usage
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if(gui == null) 
			System.out.println(time);
		else
			gui.appendEvent(time + "\n");
	}
	
	private synchronized void broadcast(String message) {
		/*
		 * This method broadcasts messages to all Clients 
		 * and removes them if they are no longer connected.
		 */
		
		String time = sdf.format(new Date());
		String messageLf = time + " " + message + "\n";
		
		if(gui == null)
			System.out.print(messageLf);
		else
			gui.appendRoom(messageLf);
		
		for(int i = clientList.size(); --i >= 0;) {
			ClientThread ct = clientList.get(i);
			
			// Remove Client from Connected Users if writing to the client fails
			if(!ct.writeMsg(messageLf)) {
				clientList.remove(i);
				display("Disconnected Client " + ct.user + " removed from list.");
			}
		}
	}
	
	// Remove User via logout
	synchronized void remove(int id) {
		for(int i = 0; i < clientList.size(); ++i) {
			ClientThread ct = clientList.get(i);
			if(ct.id == id) {
				clientList.remove(i);
				return;
			}
		}
	}
	
	/*
	 * Console Usage: 
	 * 	java Server 
	 * 	java Server [port]
	 * 
	 * Defaults:
	 * 	port: 1500
	 */
	public static void main(String[] args) {
		int port = 1500;
		switch(args.length) {
		case 1:
			try {
				port = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: java Server [port]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Usage is: java Server [port]");
			return;
		}
		Server server = new Server(port);
		server.start();
	}
	
	//* One Instance of ClientThread per Client */
	class ClientThread extends Thread{
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		
		int id;
		String user;
		Message cm;
		String date;
		
		// Constructor 
		ClientThread(Socket socket){
			id = ++uniqueId;
			this.socket = socket;

			try {
				// Create Data Streams, Output First
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput = new ObjectInputStream(socket.getInputStream());
				user = (String) sInput.readObject();
				display(user + " connected.");
				broadcast(user + " connected.");
			} catch (IOException e) {
				display("IOException Creating Streams: " + e);
				return;
			} catch (ClassNotFoundException e) {}
			
			date = new Date().toString() + "\n";
		}
		
		public void run() {
			boolean repeat = true;
			while(repeat) {
				try {
					cm = (Message) sInput.readObject(); // Read Message
				} catch (IOException e) {
					break;
				} catch (ClassNotFoundException e2) {
					break;
				}
				
				String str = cm.messageContent(); // Message Contents
				
				switch(cm.Type()) {
				case Message.MESSAGE:
					broadcast(" " + user + ": " + str);
					break;

				case Message.LOGOUT:
					broadcast(" " + user + " disconnected");
					repeat = false;
					break;
					
				case Message.LISTUSERS:
					broadcast(" List of connected users at " + sdf.format(new Date()));
					
					// Lists all Users Connected
					for(int i = 0; i < clientList.size(); ++i) {
						ClientThread ct = clientList.get(i);
						writeMsg("                      " + (i+1) + ") " + ct.user + " since " + ct.date);
					}
					
					writeMsg("\n");
					break;
				}
			}
			
			// Remove Server from Client List
			remove(id);
			close();
			
		}
		
		// Close All Connections
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			} catch (Exception e) {}
			
			try {
				if(sInput != null) sInput.close();
			} catch (Exception e) {}
			
			try {
				if(socket != null) socket.close();
			} catch (Exception e) {}
		}
		
		// Writes Message to Client
		private boolean writeMsg(String msg) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			
			try {
				sOutput.writeObject(msg);
			} catch (IOException e) { // Inform User if Error Occurs
				display("Error writing to " + user);
				display(e.toString());
			}
			return true;
		}
	}
}

