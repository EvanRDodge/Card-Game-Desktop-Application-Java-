package controller;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.BlackjackGamePanel.BlackjackPlayerType;
//this class is the blackjack setup screen
public class BlackjackSetupPanel extends JPanel implements ActionListener{

	Font font_Ariel32B = new Font("Ariel", Font.BOLD, 32);
	Font font_Ariel24B = new Font("Ariel", Font.BOLD, 24);
	Font font_Ariel16P = new Font("Ariel", Font.PLAIN, 16);
		
	JPanel panelJoin = new JPanel();
	JPanel panelHost = new JPanel();
	JLabel labelTitle = new JLabel();
	
	JLabel labelJoinIP = new JLabel();
	JTextField textFieldIP = new JTextField(10);
	JButton buttonJoinIP = new JButton();
	JLabel labelStatusTag = new JLabel();
	JLabel labelJoinStatus = new JLabel();
	
	JLabel labelDecks = new JLabel();
	String[] deckOptions = {"1", "2", "3", "4", "5", "6"};
	JComboBox<String> comboDecks = new JComboBox<String>(deckOptions);
	JLabel labelPoints = new JLabel();
	JTextField textFieldPoints = new JTextField(6);
	JLabel labelPlayerTag = new JLabel();
	JLabel labelPlayerType = new JLabel();
	JButton buttonHostGame = new JButton();
	JLabel labelHostPlayerTypeError = new JLabel();
	JLabel labelHostPointsError = new JLabel();
	
	String[] playerOptions = {"Closed", "Local", "Computer", "Remote"};
	JLabel labelPlayer1 = new JLabel();
	JComboBox<String> comboPlayerType1 = new JComboBox<String>(playerOptions);
	JLabel labelPlayer2 = new JLabel();
	JComboBox<String> comboPlayerType2 = new JComboBox<String>(playerOptions);
	JLabel labelPlayer3 = new JLabel();
	JComboBox<String> comboPlayerType3 = new JComboBox<String>(playerOptions);
	JLabel labelPlayer4 = new JLabel();
	JComboBox<String> comboPlayerType4 = new JComboBox<String>(playerOptions);
	
	public BlackjackSetupPanel() {
		//settings for outer ui components
		labelTitle.setText("Setup: BlackJack");
		labelTitle.setFont(font_Ariel32B);
		
		panelJoin.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Join a Multiplayer Game"));
		((TitledBorder) panelJoin.getBorder()).setTitleFont(font_Ariel24B);
		panelHost.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Host a Game"));
		((TitledBorder) panelHost.getBorder()).setTitleFont(font_Ariel24B);
		//settings for join panel components
		labelJoinIP.setText("IP of Game Host:");
		labelJoinIP.setFont(font_Ariel16P);
		
		textFieldIP.setFont(font_Ariel16P);
		
		buttonJoinIP.setText("Join Multiplayer Game");
		buttonJoinIP.setFont(font_Ariel24B);
		buttonJoinIP.addActionListener(this);
		
		labelStatusTag.setText("Connection Status:");
		labelStatusTag.setFont(font_Ariel16P);
		
		labelJoinStatus.setText("Not Connected");
		labelJoinStatus.setFont(font_Ariel16P);
		//settings for host panel components
		labelDecks.setText("Number of Decks:");
		labelDecks.setFont(font_Ariel16P);
		
		comboDecks.setFont(font_Ariel16P);
		comboDecks.setPreferredSize(new Dimension(80, 32));
		
		labelPoints.setText("Starting Points:");
		labelPoints.setFont(font_Ariel16P);
		
		textFieldPoints.setFont(font_Ariel16P);
		textFieldPoints.setDocument(new LengthLimitedDocument(6));
		textFieldPoints.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				validateNumber();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				validateNumber();
			}
			//highlights the text field with color based on whether or not it contains a number
			private void validateNumber() {
				try {
					int tempPoints = Integer.parseInt(textFieldPoints.getText());
					if(tempPoints > 0) {
						textFieldPoints.setForeground(Color.BLACK);
						textFieldPoints.setBackground(Color.WHITE);
					}
					else {
						textFieldPoints.setForeground(Color.RED);
						textFieldPoints.setBackground(Color.PINK);
					}
				}
				catch(NumberFormatException e) {
					textFieldPoints.setForeground(Color.RED);
					textFieldPoints.setBackground(Color.PINK);
				}
			}
		});
		textFieldPoints.setText("100");
		
		labelPlayerTag.setText("Player #");
		labelPlayerTag.setFont(font_Ariel24B);
		
		labelPlayerType.setText("Player Type");
		labelPlayerType.setFont(font_Ariel24B);
		
		labelPlayer1.setText("Player 1:");
		labelPlayer1.setFont(font_Ariel16P);
		comboPlayerType1.setFont(font_Ariel16P);
		comboPlayerType1.setSelectedItem("Local");
		
		labelPlayer2.setText("Player 2:");
		labelPlayer2.setFont(font_Ariel16P);
		comboPlayerType2.setFont(font_Ariel16P);
		
		labelPlayer3.setText("Player 3:");
		labelPlayer3.setFont(font_Ariel16P);
		comboPlayerType3.setFont(font_Ariel16P);
		
		labelPlayer4.setText("Player 4:");
		labelPlayer4.setFont(font_Ariel16P);
		comboPlayerType4.setFont(font_Ariel16P);
		
		buttonHostGame.setText("Host Game");
		buttonHostGame.setFont(font_Ariel24B);
		buttonHostGame.addActionListener(this);
		
		labelHostPlayerTypeError.setText("");
		labelHostPlayerTypeError.setFont(font_Ariel16P);
		labelHostPlayerTypeError.setForeground(Color.RED);
		
		labelHostPointsError.setText("");
		labelHostPointsError.setFont(font_Ariel16P);
		labelHostPointsError.setForeground(Color.RED);
		
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(new GridBagLayout());
		//add outermost ui components to layout
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(labelTitle, gbc);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 2;
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(panelJoin, gbc);
		
		gbc.weightx = 1;
		gbc.weighty = 9;
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(panelHost, gbc);
		//add join panel ui components to layout
		panelJoin.setLayout(new GridBagLayout());
		
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panelJoin.add(labelJoinIP, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 0;
		panelJoin.add(textFieldIP, gbc);
		
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 2;
		gbc.gridy = 0;
		panelJoin.add(buttonJoinIP, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		panelJoin.add(labelStatusTag, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridwidth = 2;
		gbc.gridx = 1;
		gbc.gridy = 1;
		panelJoin.add(labelJoinStatus, gbc);
		//add host panel ui components to layout
		panelHost.setLayout(new GridBagLayout());
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panelHost.add(labelDecks, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 0;
		panelHost.add(comboDecks, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		panelHost.add(labelPoints, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 1;
		panelHost.add(textFieldPoints, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		panelHost.add(labelPlayerTag, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 2;
		panelHost.add(labelPlayerType, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 3;
		panelHost.add(labelPlayer1, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 3;
		panelHost.add(comboPlayerType1, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 4;
		panelHost.add(labelPlayer2, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 4;
		panelHost.add(comboPlayerType2, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 5;
		panelHost.add(labelPlayer3, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 5;
		panelHost.add(comboPlayerType3, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 6;
		panelHost.add(labelPlayer4, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 6;
		panelHost.add(comboPlayerType4, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		panelHost.add(labelHostPointsError, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 3;
		panelHost.add(labelHostPlayerTypeError, gbc);
		
		gbc.gridheight = 3;
		gbc.gridx = 2;
		gbc.gridy = 4;
		panelHost.add(buttonHostGame, gbc);
	}
	//button event handling
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == buttonJoinIP) {
			MainFrame frame = (MainFrame) getParent().getParent().getParent().getParent().getParent();
			if(frame.blackjack_GamePanel.joinGame(textFieldIP.getText())) {
				//connect to and show multiplayer blackjack game
				labelJoinStatus.setText("Not Connected");
				JPanel parent = (JPanel) getParent();
				((CardLayout) parent.getLayout()).show(parent, "2");
				frame.visibleCard = 2;
			}
			else {
				labelJoinStatus.setText("Failed to connect");
			}
		}
		else if(event.getSource() == buttonHostGame) {
			boolean inputError = false;
			int startPoints = 0;
			try {
				//check if at least one local player
				if(comboPlayerType1.getSelectedItem() != "Local" 
						&& comboPlayerType2.getSelectedItem() != "Local" 
						&& comboPlayerType3.getSelectedItem() != "Local" 
						&& comboPlayerType4.getSelectedItem() != "Local") {
					inputError = true;
					labelHostPlayerTypeError.setText("At least one player must be Local.");
				}
				else
					labelHostPlayerTypeError.setText("");
				//check for valid starting points
				startPoints = Integer.parseInt(textFieldPoints.getText());
				if(startPoints <= 0) {
					inputError = true;
					throw new NumberFormatException();
				}
				else
					labelHostPointsError.setText("");
			}
			catch(NumberFormatException exc) {
				inputError = true;
				labelHostPointsError.setText("Starting points must be a positive whole number.");
			}
			//start and show game if no input errors
			if(inputError == false) {
				BlackjackPlayerType PT1 = BlackjackPlayerType.valueOf(comboPlayerType1.getSelectedItem().toString().toUpperCase());
				BlackjackPlayerType PT2 = BlackjackPlayerType.valueOf(comboPlayerType2.getSelectedItem().toString().toUpperCase());
				BlackjackPlayerType PT3 = BlackjackPlayerType.valueOf(comboPlayerType3.getSelectedItem().toString().toUpperCase());
				BlackjackPlayerType PT4 = BlackjackPlayerType.valueOf(comboPlayerType4.getSelectedItem().toString().toUpperCase());
				MainFrame frame = (MainFrame) getParent().getParent().getParent().getParent().getParent();
				frame.blackjack_GamePanel.initializeGame(startPoints, Integer.parseInt(comboDecks.getSelectedItem().toString()), PT1, PT2, PT3, PT4);
				//tell frame layout manager to show blackjack game panel
				JPanel parent = (JPanel) getParent();
				((CardLayout) parent.getLayout()).show(parent, "2");
				frame.visibleCard = 2;
			}
		}
	}	
}
