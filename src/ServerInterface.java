import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * This class creates a GUI for Server.java
 * 
 * @author Paul Burkart
 * @version 1.0
 * @since 2017-10-17
 */

public class ServerInterface extends JFrame implements ActionListener, WindowListener {
	private static final long serialVersionUID = 1L;
	
	private JButton startStop;
	private JTextArea chat, event;
	private JTextField tPortNumber;
	private Server server;
	
	public SimpleDateFormat sdf;
	
	
	// Constructor, receives port and listens for connection
	ServerInterface(int port){
		super("Chat Server");
		server = null;
		
		// North Panel contains the Port, and the Start and Stop buttons
		JPanel north = new JPanel(new GridLayout(1,1));
		
		sdf = new SimpleDateFormat("HH:mm:ss");
		
		tPortNumber = new JTextField(" " + port);
		startStop = new JButton("Start");
		startStop.addActionListener(this);

		north.add(new JLabel("Port: "));
		north.add(tPortNumber);
		north.add(startStop);
		
		// Center Panel contains the Chat Room and Event List
		JPanel center = new JPanel(new GridLayout(2, 1));
		
		chat = new JTextArea(80, 80);
		chat.setEditable(false);
		event = new JTextArea(80,80);
		event.setEditable(false);
		
		center.add(new JScrollPane(chat));
		center.add(new JScrollPane(event));
		
		// Add Panels to JFrame
		add(north, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		
		appendRoom("Chat Room\n\n");
		appendEvent("Event Log\n\n");
		
		addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
		setResizable(false);
	}
	
	// Append Message to Chat Room
	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	
	// Append Event to Event List
	void appendEvent(String str) {
		event.append(str);
		try { 
			event.setCaretPosition(chat.getText().length() - 1);
		} catch (Exception e) {}
	}
	
	// Start or Stop on Click
	public void actionPerformed(ActionEvent e) {
		
		// Stop Server if Running
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			startStop.setText("Start");
			return;
		}
		
		// Start Server
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		} catch (Exception e2) {
			appendEvent("Port Number (" + tPortNumber.getText().trim() + ") invalid");
			return;
		}
		
		// Create Server
		server = new Server(port, this);
		
		// Start Thread
		new ServerRunning().start();
		tPortNumber.setEditable(false);
		startStop.setText("Stop");
	}
	
	public static void main(String[] args) {
		new ServerInterface(1500); 
	}
	
	// Stop Server when user exits application
	public void windowClosing(WindowEvent e) {
		if(server != null){
			try {
				server.stop();
			} catch (Exception e2) {}
		}
		dispose();
		System.exit(0);
	}

	// Starts Server
	class ServerRunning extends Thread {
		public void run() {
			String time = sdf.format(new Date());
			server.start();
			startStop.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent(time + "  Server Shutdown...\n");
			server = null;
		}
	}

	// Unneeded WindowListener Methods
	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	
}
