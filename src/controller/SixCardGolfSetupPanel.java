package controller;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import controller.SixCardGolfGamePanel.SixCardGolfPlayerType;
//this class is the six card golf setup screen
public class SixCardGolfSetupPanel extends JPanel implements ActionListener{

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
	
	JLabel labelGameLength = new JLabel();
	String[] gameLengthOptions = {"8 Holes", "9 Holes", "16 Holes", "18 Holes"};
	JComboBox<String> comboGameLength = new JComboBox<String>(gameLengthOptions);
	JLabel labelPlayerTag = new JLabel();
	JLabel labelPlayerType = new JLabel();
	JButton buttonHostGame = new JButton();
	JLabel labelHostPlayerTypeError = new JLabel();
	
	String[] playerOptions = {"Closed", "Local", "Computer", "Remote"};
	JLabel labelPlayer1 = new JLabel();
	JComboBox<String> comboPlayerType1 = new JComboBox<String>(playerOptions);
	JLabel labelPlayer2 = new JLabel();
	JComboBox<String> comboPlayerType2 = new JComboBox<String>(playerOptions);
	JLabel labelPlayer3 = new JLabel();
	JComboBox<String> comboPlayerType3 = new JComboBox<String>(playerOptions);
	JLabel labelPlayer4 = new JLabel();
	JComboBox<String> comboPlayerType4 = new JComboBox<String>(playerOptions);
	
	public SixCardGolfSetupPanel() {
		//settings for outer ui components
		labelTitle.setText("Setup: Six Card Golf");
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
		labelGameLength.setText("Number of Holes:");
		labelGameLength.setFont(font_Ariel16P);
		
		comboGameLength.setFont(font_Ariel16P);
		comboGameLength.setPreferredSize(new Dimension(100, 32));
		
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
		panelHost.add(labelGameLength, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 0;
		panelHost.add(comboGameLength, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		panelHost.add(labelPlayerTag, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 1;
		panelHost.add(labelPlayerType, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		panelHost.add(labelPlayer1, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 2;
		panelHost.add(comboPlayerType1, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 3;
		panelHost.add(labelPlayer2, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 3;
		panelHost.add(comboPlayerType2, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 4;
		panelHost.add(labelPlayer3, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 4;
		panelHost.add(comboPlayerType3, gbc);
		
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 0;
		gbc.gridy = 5;
		panelHost.add(labelPlayer4, gbc);
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 5;
		panelHost.add(comboPlayerType4, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		panelHost.add(labelHostPlayerTypeError, gbc);
		
		gbc.gridheight = 3;
		gbc.gridx = 2;
		gbc.gridy = 3;
		panelHost.add(buttonHostGame, gbc);
	}
	//button event handling
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == buttonJoinIP) {
			MainFrame frame = (MainFrame) getParent().getParent().getParent().getParent().getParent();
			if(frame.sixCardGolf_GamePanel.joinGame(textFieldIP.getText())) {
				labelJoinStatus.setText("Not Connected");
				JPanel parent = (JPanel) getParent();
				((CardLayout) parent.getLayout()).show(parent, "4");
				frame.visibleCard = 4;
			}
			else
				labelJoinStatus.setText("Failed to connect");
		}
		else if(event.getSource() == buttonHostGame) {
			boolean inputError = false;
			int selectedGameLength = 0;
			//check if there is at least one local player
			if(comboPlayerType1.getSelectedItem() != "Local" 
					&& comboPlayerType2.getSelectedItem() != "Local" 
					&& comboPlayerType3.getSelectedItem() != "Local" 
					&& comboPlayerType4.getSelectedItem() != "Local") {
				inputError = true;
				labelHostPlayerTypeError.setText("At least one player must be Local.");
			}
			else
				labelHostPlayerTypeError.setText("");
			if(comboGameLength.getSelectedIndex() == 0)
				selectedGameLength = 8;
			else if(comboGameLength.getSelectedIndex() == 1)
				selectedGameLength = 9;
			else if(comboGameLength.getSelectedIndex() == 2)
				selectedGameLength = 16;
			else if(comboGameLength.getSelectedIndex() == 3)
				selectedGameLength = 18;
			else
				inputError = true;
			//initialize and show game if there are no input errors
			if(inputError == false) {
				SixCardGolfPlayerType PT1 = SixCardGolfPlayerType.valueOf(comboPlayerType1.getSelectedItem().toString().toUpperCase());
				SixCardGolfPlayerType PT2 = SixCardGolfPlayerType.valueOf(comboPlayerType2.getSelectedItem().toString().toUpperCase());
				SixCardGolfPlayerType PT3 = SixCardGolfPlayerType.valueOf(comboPlayerType3.getSelectedItem().toString().toUpperCase());
				SixCardGolfPlayerType PT4 = SixCardGolfPlayerType.valueOf(comboPlayerType4.getSelectedItem().toString().toUpperCase());
				MainFrame frame = (MainFrame) getParent().getParent().getParent().getParent().getParent();
				frame.sixCardGolf_GamePanel.initializeGame(selectedGameLength, PT1, PT2, PT3, PT4);
				//tell frame layout manager to show blackjack game panel
				JPanel parent = (JPanel) getParent();
				((CardLayout) parent.getLayout()).show(parent, "4");
				frame.visibleCard = 4;
			}
		}
	}	
}
