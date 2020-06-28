package controller;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import model.ChatPacket;
//this class controls the logic and ui of the chat window
public class ChatFrame extends JFrame implements ActionListener, SocketedController, WindowListener{
	
	private static final int PORTNUMBER = 8644;
	private ServerSocket serverSocket;
	private ArrayList<SocketConnection> connections = new ArrayList<SocketConnection>();
	private JLabel labelName = new JLabel("Name:");
	private JTextField textFieldName = new JTextField();
	private JButton buttonSetName = new JButton("Set Name");
	private JLabel labelAddress = new JLabel("IP Address");
	private JButton buttonDisconnect = new JButton("Disconnect");
	private JRadioButton radioHostChat = new JRadioButton("Host Chat");
	private JRadioButton radioJoinChat = new JRadioButton("Join Chat");
	private JTextField textFieldHostAddress = new JTextField();
	private JTextField textFieldJoinAddress = new JTextField();
	private JButton buttonHostChat = new JButton("Host");
	private JButton buttonJoinChat = new JButton("Connect");
	private JTextArea textAreaChat = new JTextArea();
	private JScrollPane scrollPane = new JScrollPane(textAreaChat);
	private JTextField textFieldMessage = new JTextField();
	private JButton buttonSendMessage = new JButton("Send Message");
	private final Font font_Ariel_16B = new Font("Ariel", Font.BOLD, 16);
	private final Font font_Ariel_16P = new Font("Ariel", Font.PLAIN, 16);
	String chatName = "";
	boolean isHost = true;
	
	public ChatFrame() {
		super("GameHub Chat");
		
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(radioHostChat);
		radioGroup.add(radioJoinChat);
		
		addWindowListener(this);
		//initial settings for ui elements
		labelName.setFont(font_Ariel_16B);
		textFieldName.setFont(font_Ariel_16P);
		buttonSetName.setFont(font_Ariel_16B);
		buttonSetName.addActionListener(this);
		
		labelAddress.setFont(font_Ariel_16B);
		
		buttonDisconnect.setFont(font_Ariel_16B);
		buttonDisconnect.setEnabled(false);
		buttonDisconnect.addActionListener(this);
		
		radioHostChat.setFont(font_Ariel_16B);
		radioHostChat.setEnabled(false);
		radioHostChat.addActionListener(this);
		radioHostChat.setSelected(true);
		radioJoinChat.setFont(font_Ariel_16B);
		radioJoinChat.setEnabled(false);
		radioJoinChat.addActionListener(this);
		
		String hostIP = "";
		try {
			InetAddress inetAddress;
			inetAddress = InetAddress.getLocalHost();
	        hostIP = inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		textFieldHostAddress.setText(hostIP);
		textFieldHostAddress.setFont(font_Ariel_16P);
		textFieldHostAddress.setEditable(false);
		textFieldHostAddress.setEnabled(false);
		textFieldJoinAddress.setFont(font_Ariel_16P);
		textFieldJoinAddress.setEnabled(false);
		
		buttonHostChat.setFont(font_Ariel_16B);
		buttonHostChat.setEnabled(false);
		buttonHostChat.addActionListener(this);
		buttonJoinChat.setFont(font_Ariel_16B);
		buttonJoinChat.setEnabled(false);
		buttonJoinChat.addActionListener(this);
		
		textAreaChat.setFont(font_Ariel_16P);
		textAreaChat.setLineWrap(true);
		textAreaChat.setWrapStyleWord(true);
		textAreaChat.setEditable(false);
		
		textFieldMessage.setFont(font_Ariel_16P);
		textFieldMessage.setEnabled(false);
		buttonSendMessage.setFont(font_Ariel_16B);
		buttonSendMessage.setEnabled(false);
		buttonSendMessage.addActionListener(this);
		//layout settings
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(labelName, gbc);

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 4;
		gbc.gridx = 1;
		gbc.gridy = 0;
		add(textFieldName, gbc);
		
		gbc.weightx = 1;
		gbc.gridx = 2;
		gbc.gridy = 1;
		add(buttonDisconnect, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		add(buttonSetName, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 1;
		add(labelAddress, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(radioHostChat, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		add(radioJoinChat, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 4;
		gbc.gridx = 1;
		gbc.gridy = 2;
		add(textFieldHostAddress, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 3;
		add(textFieldJoinAddress, gbc);

		gbc.weightx = 1;
		gbc.gridx = 2;
		gbc.gridy = 2;
		add(buttonHostChat, gbc);
		
		gbc.weightx = 1;
		gbc.gridx = 2;
		gbc.gridy = 3;
		add(buttonJoinChat, gbc);
		
		gbc.gridwidth = 3;
		gbc.weighty = 20;
		gbc.gridx = 0;
		gbc.gridy = 4;
		add(scrollPane, gbc);
		
		gbc.gridwidth = 2;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 5;
		add(textFieldMessage, gbc);
		
		gbc.gridwidth = 1;
		gbc.gridx = 2;
		gbc.gridy = 5;
		add(buttonSendMessage, gbc);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setSize(800, 600);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == buttonSetName) {
			if(textFieldName.getText() != "") {
				textAreaChat.append("--!-- Name Changed to: " + textFieldName.getText() + "\n");
				//unlock ui if this is the first successful name change
				if(chatName == "") {
					radioJoinChat.setEnabled(true);
					radioHostChat.setEnabled(true);
					if(radioHostChat.isSelected()) {
						textFieldHostAddress.setEnabled(true);
						textFieldJoinAddress.setEnabled(false);
						buttonHostChat.setEnabled(true);
						buttonJoinChat.setEnabled(false);
					}
					else {
						textFieldHostAddress.setEnabled(false);
						textFieldJoinAddress.setEnabled(true);
						buttonHostChat.setEnabled(false);
						buttonJoinChat.setEnabled(true);
					}
				}
				chatName = textFieldName.getText();
			}
		}
		else if(event.getSource() == buttonDisconnect) {
			isHost = false;
			//close connections
			close();
			//lock and unlock ui components
			textFieldMessage.setEnabled(false);
			buttonSendMessage.setEnabled(false);
			radioHostChat.setEnabled(true);
			radioJoinChat.setEnabled(true);
			if(radioHostChat.isSelected()) {
				textFieldHostAddress.setEnabled(true);
				textFieldJoinAddress.setEnabled(false);
				buttonJoinChat.setEnabled(false);
				buttonHostChat.setEnabled(true);
			}
			else if(radioJoinChat.isSelected()) {
				textFieldHostAddress.setEnabled(false);
				textFieldJoinAddress.setEnabled(true);
				buttonHostChat.setEnabled(false);
				buttonJoinChat.setEnabled(true);
			}
			buttonDisconnect.setEnabled(false);
		}
		else if(event.getSource() == buttonHostChat) {
			String hostIP = "";
			try {
				InetAddress inetAddress;
				inetAddress = InetAddress.getLocalHost();
		        hostIP = inetAddress.getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			textFieldHostAddress.setText(hostIP);
			isHost = true;
			//create thread to listen for and accept connection requests
			ChatFrame frameHandle = this;
			new Thread() { 
				public void run() {
					try {
						serverSocket = new ServerSocket(PORTNUMBER);
						serverSocket.setReuseAddress(true);
						//prepare to accept a connection request
						while(isHost) {
							Socket socket = serverSocket.accept();
							if(isHost) {
								connections.add(new SocketConnection(frameHandle, socket));
								textAreaChat.append("--!-- User connected to server.\n");
							}
						}
					} catch (IOException e) {
						textAreaChat.append("--!-- Failed to connect\n");
						e.printStackTrace();
					}
				}
			}.start();
			textAreaChat.append("--!-- Server established at IP address: " + hostIP + "\n");
			//lock and unlock parts of ui
			textFieldJoinAddress.setEnabled(false);
			radioHostChat.setEnabled(false);
			radioJoinChat.setEnabled(false);
			buttonHostChat.setEnabled(false);
			buttonJoinChat.setEnabled(false);
			buttonDisconnect.setEnabled(true);
			textFieldMessage.setEnabled(true);
			buttonSendMessage.setEnabled(true);
		}
		else if(event.getSource() == buttonJoinChat) {
			isHost = false;
			String address = "";
			if(!textFieldJoinAddress.getText().isEmpty())
				address = textFieldJoinAddress.getText();
			textAreaChat.append("--!-- Attempting to join chat at IP address: " + address + "\n");
			try {
				//attempt to connect at the address and port
				Socket socket = new Socket(address, PORTNUMBER);
				connections.add(new SocketConnection(this, socket));
				textAreaChat.append("--!-- Successfully connected\n");
				//lock and unlock parts of ui
				textFieldJoinAddress.setEnabled(false);
				radioHostChat.setEnabled(false);
				radioJoinChat.setEnabled(false);
				buttonHostChat.setEnabled(false);
				buttonJoinChat.setEnabled(false);
				buttonDisconnect.setEnabled(true);
				textFieldMessage.setEnabled(true);
				buttonSendMessage.setEnabled(true);
			} catch (IOException e) {
				textAreaChat.append("--!-- Unable to connect\n");
				e.printStackTrace();
			}
		}
		else if(event.getSource() == buttonSendMessage) {
			if(!textFieldMessage.getText().equals("")) {
				String tempMessage = chatName + ": " + textFieldMessage.getText() + "\n";
				ChatPacket packet = new ChatPacket(tempMessage);
				for(SocketConnection c : connections) {
					c.sendPacket(packet);
				}
				if(isHost)
					textAreaChat.append(tempMessage);
				textFieldMessage.setText("");
			}
		}
		else if(event.getSource() == radioHostChat) {
			textFieldHostAddress.setEnabled(true);
			textFieldJoinAddress.setEnabled(false);
			buttonHostChat.setEnabled(true);
			buttonJoinChat.setEnabled(false);
		}
		else if(event.getSource() == radioJoinChat) {
			textFieldHostAddress.setEnabled(false);
			textFieldJoinAddress.setEnabled(true);
			buttonHostChat.setEnabled(false);
			buttonJoinChat.setEnabled(true);
		}
	}

	@Override
	public void packetReceived(Object object, SocketConnection connection) {
		if(object instanceof ChatPacket) {
			if(((ChatPacket) object).getMessage().equals("TERMINATE_CONNECTION")){
				//if a user disconnected then close that specific connection and remove it from the list of connections
				try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				connections.remove(connection);
				textAreaChat.append("--!-- A user has disconnected\n");
			}
			else {
				textAreaChat.append(((ChatPacket) object).getMessage());
				if(isHost) {
					for(SocketConnection c : connections) {
						c.sendPacket(object);
					}
				}
			}
		}
	}
	//sends terminate connection packets and closes all connections
	@Override
	public void close() {
		try {
			//close all connections and clear connection list
			for(SocketConnection c : connections) {
				//send a packet to all connections informing them of connection termination
				ChatPacket tempPacket = new ChatPacket("TERMINATE_CONNECTION");
				c.sendPacket(tempPacket);
				c.close();
			}
			connections.clear();
			//The server socket would not be null if the user was the host. Close the server socket.
			if(serverSocket != null) {
				serverSocket.close();
				serverSocket = null;
			}
			textAreaChat.append("--!-- Disconnected\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//window listener event handling
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void windowClosing(WindowEvent e) {
		//disconnect from the chat when the chat window is closed
		close();
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
	}
}
