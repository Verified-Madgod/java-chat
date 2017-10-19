import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * This class creates a GUI for Client.java
 * 
 * @author Paul Burkart
 * @version 1.0
 * @since 2017-10-17
 */

public class ClientInterface extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JLabel label;
	private JTextField fieldUser, fieldServer, fieldPort;
	private JButton login, logout, listUsers;
	private JTextArea ta;
	
	private SimpleDateFormat sdf;
	
	private Client client;
	
	private boolean connected;
	private int defaultPort;
	private String defaultHost;
	private String username;
	
	
	
	// Constructor, receives socket and host
	ClientInterface(String host, int port){
		super("Chat Client");
		defaultPort = port;
		defaultHost = host;
		
		sdf = new SimpleDateFormat("HH:mm:ss");
		
		// North Panel Contains the Server, Port, and User fields
		JPanel northPanel = new JPanel(new GridLayout(1,1));
		JPanel serverPortUser = new JPanel(new GridLayout(2,1));
		
		fieldUser = new JTextField("Guest");
		fieldServer = new JTextField(host, 100);
		fieldPort = new JTextField("" + port, 10);
		fieldPort.setMinimumSize(fieldPort.getPreferredSize());
		
		fieldServer.setHorizontalAlignment(SwingConstants.RIGHT);
		fieldPort.setHorizontalAlignment(SwingConstants.RIGHT);
		fieldUser.setHorizontalAlignment(SwingConstants.RIGHT);
		
		serverPortUser.add(new JLabel("    Server:"));
		serverPortUser.add(fieldServer);
		
		serverPortUser.add(new JLabel("    Port:"));
		serverPortUser.add(fieldPort);
		
		northPanel.add(serverPortUser);
		
		fieldUser.setBackground(Color.WHITE);
		
		add(northPanel, BorderLayout.NORTH);
		
		// Chat Room
		String time = sdf.format(new Date());
		ta = new JTextArea(time + "  Welcome...\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);
		
		// Login, Logout, and List Users Buttons
		login = new JButton("Login");
		login.addActionListener(this);;
		logout = new JButton("Logout");
		logout.addActionListener(this);;
		logout.setEnabled(false);
		listUsers = new JButton("List Users");
		listUsers.addActionListener(this);
		listUsers.setEnabled(false);
	
		// southPanel contains subPanel1 & subPanel2
		JPanel southPanel = new JPanel(new GridLayout(2,1));
		
		// subPanel1 contains the user field and it's label
		JPanel subPanel1 = new JPanel(new GridLayout(1,1));
		
		// subPanel2 contains the Login, Logout, and List Users buttons
		JPanel subPanel2 = new JPanel(new GridLayout(1,1));
		
		label = new JLabel("    Username: ");
		subPanel1.add(label);
		subPanel1.add(fieldUser);
		subPanel2.add(login);
		subPanel2.add(logout);
		subPanel2.add(listUsers);
		
		southPanel.add(subPanel1);
		southPanel.add(subPanel2);
		add(southPanel, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		setResizable(false);
		fieldUser.requestFocus();
	}
	
	
	// Appends Text in TextArea
	void append(String text) {
		ta.append(text);
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	
	// Resets GUI on Reset Connection
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		listUsers.setEnabled(false);
		label.setText("    Username");
		fieldUser.setText("Guest    ");
		
		fieldPort.setText("" + defaultPort);
		fieldServer.setText(defaultHost);
		
		fieldServer.setEditable(true);
		fieldPort.setEditable(true);
		
		fieldUser.removeActionListener(this);
		connected = false;
	}
	
	// Send Message, Login, Logout, or List Users
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if(o == logout) {
			try {
				client.sendMessage(new Message(Message.LOGOUT, "", username));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return;
		}
		
		if(o == listUsers) {
			try {
				client.sendMessage(new Message(Message.LISTUSERS, "", username));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return;
		}
		
		if(connected) {
			client.sendMessage(new Message(Message.MESSAGE, fieldUser.getText(), username));
			fieldUser.setText("");
			return;
		}
		
		if(o == login) {
			String user = fieldUser.getText().trim();
			username = user;
			if(user.length() == 0) return; // empty user name
			
			String server = fieldServer.getText().trim();
			if(server.length() == 0) return; // empty server address
			
			String port = fieldPort.getText().trim();
			if(port.length() == 0) return; // empty port

			int portNumber = 1;
			
			try {
				portNumber = Integer.parseInt(port);
			} catch (Exception e2) {
				return;
			}
			
			// Create Client
			client = new Client(server, portNumber, user, this);
			
			// Test if Client Starts
			if(!client.start()) return;
			
			fieldUser.setText("");
			label.setText("    Enter Message");
			connected = true;
			
			// Disable Login Button
			login.setEnabled(false);
			
			// Enable Logout & List Users Buttons
			logout.setEnabled(true);
			listUsers.setEnabled(true);
			
			fieldServer.setEditable(false);
			fieldPort.setEditable(false);
			fieldUser.addActionListener(this);
		}
	}
	
	public static void main(String[] args) {
		new ClientInterface("localhost", 1500);
	}
}
