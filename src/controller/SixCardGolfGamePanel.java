package controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;

import model.ConnectionTerminationPacket;
import model.OnlinePlayerInitializationPacket;
import model.PlayingCard;
import model.PlayingCardDeck;
import model.PlayingCardSuit;
import model.PlayingCardValue;
import model.SixCardGolfClientActionPacket;
import model.SixCardGolfClientActionPacket.SixCardGolfButtonInput;
import model.SixCardGolfFlipPacket;
import model.SixCardGolfGameStatePacket;
import model.SixCardGolfPlayer;
import model.SixCardGolfSavedState;
//this class is the six card golf game screen. it controls the logic of the game and the ui
public class SixCardGolfGamePanel extends JPanel implements ActionListener, SocketedController{
	
	private static final int PORTNUMBER = 8446;
	
	public enum SixCardGolfPlayerType {
		CLOSED, LOCAL, COMPUTER, REMOTE
	}
		
	private Font font_Ariel16B = new Font("Ariel", Font.BOLD, 16);
	private Font font_Ariel16P = new Font("Ariel", Font.PLAIN, 16);
	
	private SixCardGolfPlayer[] players = new SixCardGolfPlayer[4];
	private SixCardGolfPlayerPanel[] playerPanels = new SixCardGolfPlayerPanel[4];
	
	private JLabel labelRoundNumber = new JLabel();
	private JButton buttonStartRound = new JButton();
	private JButton buttonDrawDeck = new JButton();
	private JButton buttonDrawDiscard = new JButton();
	private JButton buttonSwapCard = new JButton();
	private JLabel labelDiscardTag = new JLabel();
	private JLabel labelDrawnTag = new JLabel();
	private JPanel panelDiscard = new JPanel();
	private JPanel panelDrawn = new JPanel();
	private JToggleButton toggleButtonDiscard = new JToggleButton();
	private JLabel labelDrawn = new JLabel();
	private JTextArea textAreaInfo = new JTextArea();
	private ButtonGroup swapDestinationGroup = new ButtonGroup();
	
	private boolean isFirstRound = true;
	private int onlinePlayerNumber = -1;
	private int openPlayerSlotCount = 0;
	private int turnCounter = 0;
	private int roundCounter = 0;
	private int goesFirstCounter = 0;
	private int finalTurnNumber = -1;
	private boolean isHost = true;
	private boolean isSinglePlayerGame = true;
	private int gameLength;
	private PlayingCardDeck deck;
	private ArrayList<PlayingCard> discardPile = new ArrayList<PlayingCard>();
	private PlayingCard currentDrawnCard;
	private PlayingCard defaultFaceDownCard = new PlayingCard(PlayingCardValue.JOKER, PlayingCardSuit.NONE, false);
	
	private ServerSocket serverSocket;
	private ArrayList<SocketConnection> connections = new ArrayList<SocketConnection>();
	
	public SixCardGolfGamePanel() {
		onlinePlayerNumber = -1;

		labelRoundNumber.setText("Round: ");
		labelRoundNumber.setFont(font_Ariel16B);
		
		buttonStartRound.setText("Start Round");
		buttonStartRound.setFont(font_Ariel16B);
		buttonStartRound.setEnabled(false);
		buttonStartRound.addActionListener(this);
		
		buttonDrawDeck.setText("Draw From Deck");
		buttonDrawDeck.setFont(font_Ariel16B);
		buttonDrawDeck.setEnabled(false);
		buttonDrawDeck.addActionListener(this);
		
		buttonDrawDiscard.setText("Take Discard");
		buttonDrawDiscard.setFont(font_Ariel16B);
		buttonDrawDiscard.setEnabled(false);
		buttonDrawDiscard.addActionListener(this);
		
		buttonSwapCard.setText("Swap/Discard");
		buttonSwapCard.setFont(font_Ariel16B);
		buttonSwapCard.setEnabled(false);
		buttonSwapCard.addActionListener(this);
		
		textAreaInfo.setText("HINT: Draw a card from the deck or discard pile, select a destination for that card, then press the swap/discard button.");
		textAreaInfo.setFont(font_Ariel16P);
		textAreaInfo.setWrapStyleWord(true);
		textAreaInfo.setLineWrap(true);
		
		labelDiscardTag.setText("Discard");
		labelDiscardTag.setFont(font_Ariel16B);
		
		labelDrawnTag.setText("Drawn Card");
		labelDrawnTag.setFont(font_Ariel16B);
		
		panelDiscard.add(toggleButtonDiscard);
		panelDrawn.add(labelDrawn);
		
		//set initial images for deck, discard, and drawn spaces
		BufferedImage img;
		try {
			img = ImageIO.read(this.getClass().getResource("/images/cardback_red.png"));
			Image scaledImage = img.getScaledInstance(60, 87, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(scaledImage);
			toggleButtonDiscard.setIcon(icon);
			labelDrawn.setIcon(icon);
		} catch (IOException e) {
			e.printStackTrace();
		}

		playerPanels[0] = new SixCardGolfPlayerPanel("Player 1");
		playerPanels[1] = new SixCardGolfPlayerPanel("Player 2");
		playerPanels[2] = new SixCardGolfPlayerPanel("Player 3");
		playerPanels[3] = new SixCardGolfPlayerPanel("Player 4");

		swapDestinationGroup.add(toggleButtonDiscard);
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 6; j++) {
				swapDestinationGroup.add(playerPanels[i].getCardToggleButton(j));
			}
		}
		//add components to layout
		JPanel cardPilePanel = new JPanel();
		cardPilePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		cardPilePanel.add(labelDiscardTag, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		cardPilePanel.add(labelDrawnTag, gbc);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 2;
		gbc.weighty = 2;
		gbc.gridx = 0;
		gbc.gridy = 1;
		cardPilePanel.add(panelDiscard, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		cardPilePanel.add(panelDrawn, gbc);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 2));
		
		buttonPanel.add(labelRoundNumber);
		buttonPanel.add(buttonDrawDeck);
		buttonPanel.add(new JLabel());
		buttonPanel.add(buttonDrawDiscard);
		buttonPanel.add(buttonStartRound);
		buttonPanel.add(buttonSwapCard);
		
		GridLayout gridLayout = new GridLayout(3, 2);
		setLayout(gridLayout);
		//add ui components to layout
		add(playerPanels[0]);
		add(playerPanels[1]);
		add(playerPanels[2]);
		add(playerPanels[3]);
		add(cardPilePanel);
		add(buttonPanel);
	}
	//initializes the game for a non-host player that is joining a multiplayer game
	public boolean joinGame(String address) {
		//connect to host
		try {
			Socket socket = new Socket(address, PORTNUMBER);
			connections.add(new SocketConnection(this, socket));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		turnCounter = -1;
		roundCounter = -1;
		goesFirstCounter = -1;
		isHost = false;
		isSinglePlayerGame = false;
		buttonStartRound.setEnabled(false);
		buttonDrawDeck.setEnabled(false);
		buttonDrawDiscard.setEnabled(false);
		buttonSwapCard.setEnabled(false);
		players[0] = null;
		players[1] = null;
		players[2] = null;
		players[3] = null;	
		//player panel ui
		for(int i = 0; i < 4; i++) {
			playerPanels[i].clearForNewRound();
			playerPanels[i].setDisplayedPoints(0);
			playerPanels[i].setColors(SixCardGolfPlayerType.CLOSED);	
			playerPanels[i].setTurnColor(false);
		}
		return true;
	}
	//initializes the game for a host of a singleplayer or multiplayer game
	public void initializeGame(int gameLength, SixCardGolfPlayerType PT1, SixCardGolfPlayerType PT2, SixCardGolfPlayerType PT3, SixCardGolfPlayerType PT4) {
		System.out.println("initializing game " + PT1.toString() + " | " + PT2.toString() + " | " + PT3.toString() + " | " + PT4.toString());
		turnCounter = -1;
		roundCounter = -1;
		goesFirstCounter = -1;
		onlinePlayerNumber = -1;
		isFirstRound = true;
		this.gameLength = gameLength;
		isHost = true;
		isSinglePlayerGame = true;
		
		buttonDrawDeck.setEnabled(false);
		buttonDrawDiscard.setEnabled(false);
		buttonSwapCard.setEnabled(false);
		//create new player objects
		players[0] = new SixCardGolfPlayer(0, PT1);
		players[1] = new SixCardGolfPlayer(1, PT2);
		players[2] = new SixCardGolfPlayer(2, PT3);
		players[3] = new SixCardGolfPlayer(3, PT4);
		//reset ui
		labelRoundNumber.setText("Round: " + (roundCounter + 1));
		for(int i = 0; i < 4; i++) {
			playerPanels[i].clearForNewRound();
			playerPanels[i].setTurnColor(false);
			playerPanels[i].setDisplayedPoints(0);
			if(players[i].getPlayerType() == SixCardGolfPlayerType.REMOTE) {
				playerPanels[i].setColors(SixCardGolfPlayerType.CLOSED);
				isSinglePlayerGame = false;
			}
			else {
				playerPanels[i].setColors(players[i].getPlayerType());
			}
		}
		//prepare deck and discard pile
		deck = new PlayingCardDeck(1, 2);
		deck.shuffle();
		discardPile.clear();
		if(isSinglePlayerGame)
			buttonStartRound.setEnabled(true);
		else {
			//set up thread to listen for connecting players
			SixCardGolfGamePanel frameHandle = this;
			new Thread() {
				public void run() {
					openPlayerSlotCount = 0;
					for(int i = 0; i < 4; i++) {
						if(players[i].getPlayerType() == SixCardGolfPlayerType.REMOTE) {
							openPlayerSlotCount++;
						}
					}
					//listen for connections until all slots are filled
					try {
						serverSocket = new ServerSocket(PORTNUMBER);
						serverSocket.setReuseAddress(true);
						while(openPlayerSlotCount > 0) {
							try {
								Socket socket = serverSocket.accept();
								if(openPlayerSlotCount > 0) {
									connections.add(new SocketConnection(frameHandle, socket));
									//assign a connection number to the first open player slot.
									for(int i = 0; i < 4; i++) {
										if(players[i].getPlayerType() == SixCardGolfPlayerType.REMOTE && players[i].getConnectionNumber() == -1) {
											players[i].setConnectionNumber(connections.size() - 1);
											//send a packet telling the player what their player number is
											OnlinePlayerInitializationPacket tempPacket = new OnlinePlayerInitializationPacket(i);
											connections.get(connections.size() - 1).sendPacket(tempPacket);
											playerPanels[i].setColors(SixCardGolfPlayerType.REMOTE);
											i = 4;
										}
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
							openPlayerSlotCount--;
						}
						serverSocket.close();
					} catch (IOException e) {
						System.out.println("failed to make server");
						e.printStackTrace();
					}
				}
			}.start();
			
			//display connection instructions to user
			JOptionPane.showConfirmDialog(this, "- Players will not be able to connect after the first round starts.\n"
					+ "- Players on other instances of this application that are running on this machine can connect via 127.0.0.1.\n"
					+ "- If you are connected to a network, then players on other systems on the network can connect via your\n"
					+ "  ethernet (eth...) or wireless (wlan...) IP address depending on which one this system is using.", 
					"Hosting Multiplayer Game", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

			buttonStartRound.setEnabled(true);
		}	
	}	
	//button event handling
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == buttonStartRound) {
			buttonStartRound.setEnabled(false);
			if(roundCounter < 0) {
				for(int i = 0; i < 4; i++) {
					players[i].clearPoints();
					playerPanels[i].setDisplayedPoints(0);
				}
			}
			do {
				if(goesFirstCounter >= 3)
					goesFirstCounter = 0;
				else
					goesFirstCounter++;
			} while(players[goesFirstCounter].getPlayerType() == SixCardGolfPlayerType.CLOSED);
			System.out.println(goesFirstCounter);
			turnCounter = goesFirstCounter - 1;
			roundCounter++;
			finalTurnNumber = -1;
			//prepare deck and discard pile for a new round
			discardPile.clear();
			deck.rebuildDeck();
			deck.shuffle();
			//any unclaimed remote slots become closed
			openPlayerSlotCount = 0;
			for(int i = 1; i < 4; i++) {
				if(players[i].getPlayerType() == SixCardGolfPlayerType.REMOTE && players[i].getConnectionNumber() == -1) {
					players[i].setPlayerType(SixCardGolfPlayerType.CLOSED);
				}
			}
			//check if there are any remote players in the first round
			if(isFirstRound == true && serverSocket != null) {
				isSinglePlayerGame = true;
				int i = 0;
				while(isSinglePlayerGame == true && i < 4) {
					if(players[i].getPlayerType() == SixCardGolfPlayerType.REMOTE)
						isSinglePlayerGame = false;
					i++;
				}
				//if there are no remote players in this first round then change to singleplayer
				if(isSinglePlayerGame == true) {
					openPlayerSlotCount = 0;
					try {
						serverSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("no remote players in first round. moving to singleplayer");
				}
			}
			isFirstRound = false;
			labelRoundNumber.setText("Round: " + (roundCounter + 1));
			//clear cards
			for(int i = 0; i < 4; i++) {
				playerPanels[i].clearForNewRound();
			}
			//deal starting cards
			PlayingCard tempCard;
			for(int i = 0; i < 4; i++) {
				if(players[i].getPlayerType() != SixCardGolfPlayerType.CLOSED) {
					for(int j = 0; j < 6; j++) {
						tempCard = deck.draw();
						tempCard.setFaceUp(false);
						players[i].getHand()[j] = tempCard;
						playerPanels[i].swapCard(j, tempCard);
					}
				}
				playerPanels[i].setToggleButtonsEnabled(false);
			}
			//place top card of deck into discard pile and clear drawn card
			currentDrawnCard = null;
			setDrawnIcon(defaultFaceDownCard);
			tempCard = deck.draw();
			discardPile.add(tempCard);
			setDiscardIcon(tempCard);
			
			revalidate();
			repaint();
			//update ui and request remote players to flip over two of their starting cards
			if(isHost == true && isSinglePlayerGame == false) {
				PlayingCard topOfDiscard;
				if(discardPile.size() > 0)
					topOfDiscard = discardPile.get(discardPile.size() - 1);
				else
					topOfDiscard = defaultFaceDownCard;
				SixCardGolfGameStatePacket tempPacket = new SixCardGolfGameStatePacket(players, -1, -1, topOfDiscard, currentDrawnCard, true, false, false);
				for(SocketConnection c : connections) {
					c.sendPacket(tempPacket);
				}
			}
			//ask local and computer players to flip over starting cards
			for(int i = 0; i < 4; i++) {
				//request local player to flip two starting cards
				if(players[i].getPlayerType() == SixCardGolfPlayerType.LOCAL) {
					String[] flipOptions = new String[] {"1", "2", "3", "4", "5", "6"};
					JComboBox<String> comboFlip1 = new JComboBox<String>(flipOptions);
					JComboBox<String> comboFlip2 = new JComboBox<String>(flipOptions);
					JTextArea textAreaFlipMessage = new JTextArea("Choose two of your starting cards to flip over.\n"
							+ "Position numbers are as follows: 1 2 3\n"
							+ "                                                             4 5 6");
					textAreaFlipMessage.setEditable(false);
					textAreaFlipMessage.setFocusable(false);
					JPanel panelFlipOption = new JPanel();
					panelFlipOption.setLayout(new GridLayout(3, 1));
					panelFlipOption.add(comboFlip2, 2, 0);
					panelFlipOption.add(comboFlip1, 1, 0);
					panelFlipOption.add(textAreaFlipMessage, 0, 0);
					boolean validInput = false;
					do {
						comboFlip1.setSelectedIndex(0);
						comboFlip2.setSelectedIndex(1);
						int flipResult = JOptionPane.showConfirmDialog(this, panelFlipOption, "Round Starting", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(flipResult == JOptionPane.OK_OPTION && comboFlip1.getSelectedIndex() != comboFlip2.getSelectedIndex()) {
							players[i].getHand()[comboFlip1.getSelectedIndex()].setFaceUp(true);
							players[i].getHand()[comboFlip2.getSelectedIndex()].setFaceUp(true);
							playerPanels[i].swapCard(comboFlip1.getSelectedIndex(), players[i].getHand()[comboFlip1.getSelectedIndex()]);
							playerPanels[i].swapCard(comboFlip2.getSelectedIndex(), players[i].getHand()[comboFlip2.getSelectedIndex()]);
							validInput = true;
						}
					}while(validInput == false);
				}
				//flip two starting cards for a computer controlled player
				else if(players[i].getPlayerType() == SixCardGolfPlayerType.COMPUTER) {
					players[i].getHand()[0].setFaceUp(true);
					players[i].getHand()[1].setFaceUp(true);
					playerPanels[i].swapCard(0, players[i].getHand()[0]);
					playerPanels[i].swapCard(1, players[i].getHand()[1]);
				}
			}
			revalidate();
			repaint();
			//check if all players have flipped over starting cards
			if(checkIfPlayersFlipped() == true) {
				System.out.println("all flips received");
				startNewTurn();
			}
		}
		else if(event.getSource() == buttonSwapCard) {
			buttonSwapCard.setEnabled(false);
			//get selection
			boolean selectionFound = false;
			int swapDest = -1;
			if(toggleButtonDiscard.isSelected() == true) {
				selectionFound = true;
				swapDest = 6;
			}
			else {
				for(int i = 0;selectionFound == false && i < 6; i++) {
					if(playerPanels[turnCounter].getCardToggleButton(i).isSelected()) {
						selectionFound = true;
						swapDest = i;
					}
				}
			}
			//invalid selection
			if(swapDest == -1) {
				System.out.println("invalid selections");
				swapDestinationGroup.clearSelection();
				buttonSwapCard.setEnabled(true);
			}
			//valid selection
			else {
				toggleButtonDiscard.setEnabled(false);
				playerPanels[turnCounter].setToggleButtonsEnabled(false);
				if(isHost == true) {
					//swap selected card with drawn card. send swapped out card to discard pile
					if(swapDest >= 0 && swapDest < 6) {
						PlayingCard discardedCard = players[turnCounter].getHand()[swapDest];
						discardedCard.setFaceUp(true);
						discardPile.add(discardedCard);
						setDiscardIcon(discardedCard);
						players[turnCounter].getHand()[swapDest] = currentDrawnCard;
						playerPanels[turnCounter].swapCard(swapDest, currentDrawnCard);
						currentDrawnCard = null;
						setDrawnIcon(currentDrawnCard);
					}
					else {
						discardPile.add(currentDrawnCard);
						setDiscardIcon(currentDrawnCard);
						currentDrawnCard = null;
						setDrawnIcon(currentDrawnCard);
					}
					revalidate();
					repaint();
					startNewTurn();
				}
				else {
					//request for a card swap from the host
					SixCardGolfClientActionPacket inputPacket;
					if(swapDest == 6)
						inputPacket = new SixCardGolfClientActionPacket(onlinePlayerNumber, SixCardGolfButtonInput.SWAPDISCARD, 6);
					else
						inputPacket = new SixCardGolfClientActionPacket(onlinePlayerNumber, SixCardGolfButtonInput.SWAPCARDS, swapDest);
					connections.get(0).sendPacket(inputPacket);
				}
			}
		}
		else if(event.getSource() == buttonDrawDeck) {
			buttonDrawDeck.setEnabled(false);
			buttonDrawDiscard.setEnabled(false);
			buttonSwapCard.setEnabled(true);
			if(isHost == true) {
				currentDrawnCard = deck.draw();
				PlayingCard topOfDiscard;
				if(discardPile.size() > 0)
					topOfDiscard = discardPile.get(discardPile.size() - 1);
				else
					topOfDiscard = defaultFaceDownCard;
				//update remote players
				SixCardGolfGameStatePacket tempPacket = new SixCardGolfGameStatePacket(players, turnCounter, roundCounter, topOfDiscard, currentDrawnCard, false, false, false);
				for(SocketConnection c : connections) {
					c.sendPacket(tempPacket);
				}
				//update ui to show drawn card
				setDrawnIcon(currentDrawnCard);
				swapDestinationGroup.clearSelection();
				toggleButtonDiscard.setEnabled(true);
				for(int i = 0; i < 4; i++) {
					if(i == turnCounter)
						playerPanels[i].setToggleButtonsEnabled(true);
					else
						playerPanels[i].setToggleButtonsEnabled(false);
				}
				revalidate();
				repaint();
			}
			else {
				//request a card from host
				SixCardGolfClientActionPacket inputPacket = new SixCardGolfClientActionPacket(onlinePlayerNumber, SixCardGolfButtonInput.DRAWDECK, -1);
				connections.get(0).sendPacket(inputPacket);
			}
		}
		else if(event.getSource() == buttonDrawDiscard) {
			buttonDrawDeck.setEnabled(false);
			buttonDrawDiscard.setEnabled(false);
			buttonSwapCard.setEnabled(true);
			if(isHost == true) {
				currentDrawnCard = discardPile.get(discardPile.size() - 1);
				discardPile.remove(discardPile.size() - 1);
				PlayingCard topOfDiscard;
				if(discardPile.size() > 0) {
					topOfDiscard = discardPile.get(discardPile.size() - 1);
					setDiscardIcon(discardPile.get(discardPile.size() - 1));
				}
				else {
					topOfDiscard = defaultFaceDownCard;
					setDiscardIcon(defaultFaceDownCard);
				}
				//update remote players
				SixCardGolfGameStatePacket tempPacket = new SixCardGolfGameStatePacket(players, turnCounter, roundCounter, topOfDiscard, currentDrawnCard, false, false, false);
				for(SocketConnection c : connections) {
					c.sendPacket(tempPacket);
				}
				//update ui to show drawn card
				setDrawnIcon(currentDrawnCard);
				swapDestinationGroup.clearSelection();
				toggleButtonDiscard.setEnabled(true);
				for(int i = 0; i < 4; i++) {
					if(i == turnCounter)
						playerPanels[i].setToggleButtonsEnabled(true);
					else
						playerPanels[i].setToggleButtonsEnabled(false);
				}
				revalidate();
				repaint();
			}
			else {
				//request a card from host
				SixCardGolfClientActionPacket inputPacket = new SixCardGolfClientActionPacket(onlinePlayerNumber, SixCardGolfButtonInput.DRAWDISCARD, -1);
				connections.get(0).sendPacket(inputPacket);
			}
		}
	}
	//This function increments the turn counters, checks for a round over/game over, updates remote players with the game status, and lets local and computer players take their turn
	public void startNewTurn() {
		//check if final round should start
		if(finalTurnNumber == -1 && turnCounter != -1 && players[turnCounter].getPlayerType() != SixCardGolfPlayerType.CLOSED) {
			boolean somethingFaceDown = false;
			for(int i = 0; somethingFaceDown == false && i < 6; i++) {
				if(players[turnCounter].getHand()[i].isFaceUp() == false)
					somethingFaceDown = true;
			}
			if(somethingFaceDown == false)
				finalTurnNumber = turnCounter;
		}
		//out of bounds turn and hand counter handling
		if(turnCounter + 1 > 3)
			turnCounter = 0;
		else
			turnCounter++;
		//check if final turn has been reached
		if(turnCounter == finalTurnNumber) {
			//save score for this round
			for(int i = 0; i < 4; i++) {
				if(players[i].getPlayerType() != SixCardGolfPlayerType.CLOSED) {
					players[i].setScore(roundCounter, players[i].getHandValue());
					playerPanels[i].setDisplayedPoints(players[i].getTotalScore(gameLength));
					for(int j = 0; j < 6; j++) {
						players[i].getHand()[j].setFaceUp(true);
						playerPanels[i].swapCard(j, players[i].getHand()[j]);
					}
				}
			}
			//update remote players with game state data and results
			if(isSinglePlayerGame == false && turnCounter != -1) {
				PlayingCard topOfDiscard;
				if(discardPile.size() > 0)
					topOfDiscard = discardPile.get(discardPile.size() - 1);
				else
					topOfDiscard = defaultFaceDownCard;
				SixCardGolfGameStatePacket resultsPacket;
				if(roundCounter + 1 >= gameLength)
					resultsPacket = new SixCardGolfGameStatePacket(players, turnCounter, roundCounter, topOfDiscard, currentDrawnCard, false, true, true);
				else
					resultsPacket = new SixCardGolfGameStatePacket(players, turnCounter, roundCounter, topOfDiscard, currentDrawnCard, false, true, false);
				for(SocketConnection c : connections) {
					c.sendPacket(resultsPacket);
				}
			}			
			revalidate();
			repaint();
			JOptionPane.showConfirmDialog(this,
					"Player 1: " + players[0].getHandValue() + "\nPlayer 2: " + players[1].getHandValue() + "\nPlayer 3: " + players[2].getHandValue() + "\nPlayer 4: " + players[3].getHandValue(), 
					"Round " + (roundCounter + 1) + " Score", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			System.out.println("round over");
			if(roundCounter + 1 >= gameLength) {
				JOptionPane.showConfirmDialog(this,
						"Total Scores\nPlayer 1: " + players[0].getTotalScore(gameLength) + "\nPlayer 2: " + players[1].getTotalScore(gameLength) + "\nPlayer 3: " + players[2].getTotalScore(gameLength) + "\nPlayer 4: " + players[3].getTotalScore(gameLength), 
						"Game Over", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
				System.out.println("game over");
				roundCounter = -1;
			}
			buttonStartRound.setEnabled(true);
		}
		else {
			currentDrawnCard = null;
			//skip closed player slot turns
			if(players[turnCounter].getPlayerType() == SixCardGolfPlayerType.CLOSED) {
				startNewTurn();	
			}
			else {
				//display whose turn it is to host
				updateTurnColors(turnCounter);
				for(int i = 0; i < 4; i++) {
					playerPanels[i].setToggleButtonsEnabled(false);
				}
				toggleButtonDiscard.setEnabled(false);
				refreshDisplayedCards(players, discardPile.get(discardPile.size() - 1), currentDrawnCard);
				revalidate();
				repaint();
				//shuffle discard pile into deck if deck is empty
				if(deck.getSize() == 0) {
					PlayingCard lastCard = discardPile.get(discardPile.size() - 1);
					discardPile.remove(discardPile.size() - 1);
					deck.addCardPile(discardPile);
					deck.shuffle();
					discardPile.clear();
					discardPile.add(lastCard);
				}
				//update remote players
				if(isSinglePlayerGame == false) {
					PlayingCard topOfDiscard;
					if(discardPile.size() > 0)
						topOfDiscard = discardPile.get(discardPile.size() - 1);
					else
						topOfDiscard = defaultFaceDownCard;
					SixCardGolfGameStatePacket tempPacket = new SixCardGolfGameStatePacket(players, turnCounter, roundCounter, topOfDiscard, currentDrawnCard, false, false, false);
					for(SocketConnection c : connections) {
						c.sendPacket(tempPacket);
					}
				}
				//if it is a local turn enable buttons for drawing cards
				if(players[turnCounter].getPlayerType() == SixCardGolfPlayerType.LOCAL) {
					buttonDrawDeck.setEnabled(true);
					buttonDrawDiscard.setEnabled(true);
				}
				//if it is a computer turn
				else if(players[turnCounter].getPlayerType() == SixCardGolfPlayerType.COMPUTER) {
					PlayingCard drawnCard = new PlayingCard(PlayingCardValue.JOKER, PlayingCardSuit.NONE);
					boolean hasDrawn = false;
					boolean hasPlayed = false;
					int swapDest = -1;
					//check if top of discard pile will complete a match
					for(int i = 0; i < 6 && hasDrawn == false; i++) {
						//if the top of discard pile matches a face up card
						if(players[turnCounter].getHand()[i].isFaceUp() && players[turnCounter].getHand()[i].getValue() == discardPile.get(discardPile.size() - 1).getValue()) {
							//if column is not already matched
							if(i > 2 && (players[turnCounter].getHand()[i].getValue() != players[turnCounter].getHand()[i - 3].getValue() || players[turnCounter].getHand()[i - 3].isFaceUp() == false)) {
								drawnCard = discardPile.get(discardPile.size() - 1);
								discardPile.remove(discardPile.size() - 1);
								swapDest = i - 3;
								hasDrawn = true;
								hasPlayed = true;
							}
							else if(i < 3 && (players[turnCounter].getHand()[i].getValue() != players[turnCounter].getHand()[i + 3].getValue() || players[turnCounter].getHand()[i + 3].isFaceUp() == false)) {
								drawnCard = discardPile.get(discardPile.size() - 1);
								discardPile.remove(discardPile.size() - 1);
								swapDest = i + 3;
								hasDrawn = true;
								hasPlayed = true;
							}
						}
					}
					//if the top card of the discard pile has a low value then draw it
					if(hasDrawn == false &&
							(discardPile.get(discardPile.size() - 1).getValue() == PlayingCardValue.JOKER ||
							discardPile.get(discardPile.size() - 1).getValue() == PlayingCardValue.KING ||
							discardPile.get(discardPile.size() - 1).getValue() == PlayingCardValue.ACE)) {
						drawnCard = discardPile.get(discardPile.size() - 1);
						discardPile.remove(discardPile.size() - 1);
						hasDrawn = true;
						//search for a column with a similar card
						for(int i = 0; i < 6 && hasPlayed == false; i++) {
							if(players[turnCounter].getHand()[i].isFaceUp() == true &&
									(players[turnCounter].getHand()[i].getValue() == PlayingCardValue.JOKER ||
									players[turnCounter].getHand()[i].getValue() == PlayingCardValue.KING ||
									players[turnCounter].getHand()[i].getValue() == PlayingCardValue.ACE)) {
								//if other card in the column is face down or is less valuable than the drawn card
								if(i > 2 && (players[turnCounter].getHand()[i - 3].isFaceUp() == false || players[turnCounter].getHand()[i - 3].getValue().ordinal() < drawnCard.getValue().ordinal())) {
									swapDest = i - 3;
									hasPlayed = true;
								}
								else if(i < 3 && (players[turnCounter].getHand()[i + 3].isFaceUp() == false || players[turnCounter].getHand()[i + 3].getValue().ordinal() < drawnCard.getValue().ordinal())) {
									swapDest = i + 3;
									hasPlayed = true;
								}
							}
						}
						//search for a face down column
						for(int i = 0; i < 3 && hasPlayed == false; i++) {
							if(players[turnCounter].getHand()[i].isFaceUp() == false && players[turnCounter].getHand()[i + 3].isFaceUp() == false) {
								swapDest = i;
								hasPlayed = true;
							}
						}
						//otherwise find the first face down card
						for(int i = 0; i < 6 && hasPlayed == false; i++) {
							if(players[turnCounter].getHand()[i].isFaceUp() == false) {
								swapDest = i;
								hasPlayed = true;
							}
						}
					}
					//draw a card from the deck if the computer player has not taken a card from the discard pile
					if(hasDrawn == false) {
						drawnCard = deck.draw();
						hasDrawn = true;
						//check if the drawn card will complete a match
						for(int i = 0; i < 6 && hasPlayed == false; i++) {
							//if the top of discard pile matches a face up card
							if(players[turnCounter].getHand()[i].isFaceUp() && players[turnCounter].getHand()[i].getValue() == drawnCard.getValue()) {
								//if column is not already matched
								if(i > 2 && (players[turnCounter].getHand()[i].getValue() != players[turnCounter].getHand()[i - 3].getValue() || players[turnCounter].getHand()[i - 3].isFaceUp() == false)) {
									drawnCard = discardPile.get(discardPile.size() - 1);
									discardPile.remove(discardPile.size() - 1);
									swapDest = i - 3;
									hasPlayed = true;
								}
								else if(i < 3 && (players[turnCounter].getHand()[i].getValue() != players[turnCounter].getHand()[i + 3].getValue() || players[turnCounter].getHand()[i + 3].isFaceUp() == false)) {
									drawnCard = discardPile.get(discardPile.size() - 1);
									discardPile.remove(discardPile.size() - 1);
									swapDest = i + 3;
									hasPlayed = true;
								}
							}
						}
						if(hasPlayed == false) {
							//put card in first face down slot if it will not end the round
							if(players[turnCounter].FaceUpCardCount() < 5) {
								for(int i = 0; i < 6 && hasPlayed == false; i++) {
									if(players[turnCounter].getHand()[i].isFaceUp() == false) {
										swapDest = i;
										hasPlayed = true;
									}
								}
							}
							//choose slot of highest value card that is not matched
							else {
								int highestValue = -1;
								for(int i = 0; i < 6; i++) {
									//find highest value card among face up slots
									if(players[turnCounter].getHand()[i].isFaceUp() == true) {
										//check if face up card is matched with another face up card
										if((i > 2 && (players[turnCounter].getHand()[i].getValue() != players[turnCounter].getHand()[i - 3].getValue() || players[turnCounter].getHand()[i - 3].isFaceUp() == false)) || 
												(i < 3 && (players[turnCounter].getHand()[i].getValue() != players[turnCounter].getHand()[i + 3].getValue() || players[turnCounter].getHand()[i + 3].isFaceUp() == false))) {
											//note this to be highest value index of there is no highest value yet or this is a worse card than the previous highest value index
											if(highestValue == -1 ||
													(players[turnCounter].getHand()[i].getValue().ordinal() > players[turnCounter].getHand()[highestValue].getValue().ordinal() &&
															players[turnCounter].getHand()[i].getValue().ordinal() < PlayingCardValue.KING.ordinal()))
												highestValue = i;
										}
									}
								}
								if(highestValue != -1) {
									swapDest = highestValue;
									hasPlayed = true;
								}
							}
						}	
					}
					//swap the cards or discard the drawn card
					if(swapDest != -1 && drawnCard != null) {
						//swap the cards
						PlayingCard discardedCard = players[turnCounter].getHand()[swapDest];
						discardedCard.setFaceUp(true);
						discardPile.add(discardedCard);
						setDiscardIcon(discardedCard);
						players[turnCounter].getHand()[swapDest] = drawnCard;
						playerPanels[turnCounter].swapCard(swapDest, drawnCard);
					}
					else {
						discardPile.add(drawnCard);
						setDiscardIcon(drawnCard);
					}
					currentDrawnCard = null;
					setDrawnIcon(currentDrawnCard);
					revalidate();
					repaint();
					startNewTurn();
				}
			}
		}
	}
	
	public boolean isSinglePlayer() {
		return isSinglePlayerGame;
	}
	//Checks all players for face up cards. Returns true if all players have a face up card.
	public boolean checkIfPlayersFlipped() {
		for(int i = 0; i < 4; i++) {
			if(players[i].getPlayerType() != SixCardGolfPlayerType.CLOSED) {
				boolean keepChecking = true;
				for(int j = 0 ; j < 6 && keepChecking == true; j++) {
					//if a face up card is found then exit loop early
					if(players[i].getHand()[j].isFaceUp() == true) {
						keepChecking = false;
					}
				}
				//if no flipped card was found in this hand
				if(keepChecking != false)
					return false;
			}
		}
		return true;
	}
	//sets the displayed image for the discard pile to match the provided card. this function also resizes the image to better fit the size of the window
	public void setDiscardIcon(PlayingCard card) {
		try {
			int cardDisplayHeight = panelDiscard.getSize().height * 4 / 5;
			int cardDisplayWidth = cardDisplayHeight * 20 / 29;
			BufferedImage img;
			if(card != null)
				img = ImageIO.read(this.getClass().getResource(card.getCardResourcePath(card.isFaceUp())));
			else
				img = ImageIO.read(this.getClass().getResource("/images/cardback_red.png"));
			Image scaledImage = img.getScaledInstance(cardDisplayWidth, cardDisplayHeight, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(scaledImage);
			toggleButtonDiscard.setIcon(icon);
			toggleButtonDiscard.setDisabledIcon(icon);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	//sets the displayed image for the drawn card to match the provided card. this function also resizes the image to better fit the size of the window
	public void setDrawnIcon(PlayingCard card) {
		try {
			int cardDisplayHeight = panelDiscard.getSize().height * 4 / 5;
			int cardDisplayWidth = cardDisplayHeight * 20 / 29;
			BufferedImage img;
			if(card != null)
				img = ImageIO.read(this.getClass().getResource(card.getCardResourcePath(card.isFaceUp())));
			else
				img = ImageIO.read(this.getClass().getResource("/images/cardback_red.png"));
			Image scaledImage = img.getScaledInstance(cardDisplayWidth, cardDisplayHeight, Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(scaledImage);
			labelDrawn.setIcon(icon);
			labelDrawn.setDisabledIcon(icon);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}	
	//Clears displayed hands and displays hands according to the player list parameter.
	public void refreshDisplayedCards(SixCardGolfPlayer[] playerList, PlayingCard topOfDiscard, PlayingCard drawnCard) {
		for(int i = 0; i < 4; i++) {
			if(playerList[i].getPlayerType() != SixCardGolfPlayerType.CLOSED) {
				for(int j = 0; j < 6; j++) {
					playerPanels[i].swapCard(j, playerList[i].getHand()[j]);
				}
			}
		}
		setDiscardIcon(topOfDiscard);
		setDrawnIcon(drawnCard);
	}
	//Updates the player name and hand highlights to indicate a specific player and hand's turn.
	public void updateTurnColors(int turnNumber) {
		for(int i = 0; i < 4; i++) {
			if(i == turnNumber)
				playerPanels[i].setTurnColor(true);
			else
				playerPanels[i].setTurnColor(false);
		}
	}

	//attempts to initialize the game according to the save game state object in the provided file
	public void loadStateFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		try {
			SixCardGolfSavedState gameState = (SixCardGolfSavedState)ois.readObject();
			
			initializeGame(gameState.getGameLength(), gameState.getPlayerTypes()[0], gameState.getPlayerTypes()[1], gameState.getPlayerTypes()[2], gameState.getPlayerTypes()[3]);
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < gameState.getRoundNumber(); j++) {
					players[i].setScore(j, gameState.getPlayerPoints()[i][j]);
				}
				playerPanels[i].setDisplayedPoints(players[i].getTotalScore(gameState.getRoundNumber()));;
			}
			roundCounter = gameState.getRoundNumber();
			goesFirstCounter = gameState.getGoesFirstNumber();
		} catch (ClassNotFoundException e) {
			JOptionPane.showConfirmDialog(this,
					"An error occurred! Your progress could not be loaded.", 
					"Load Game Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		ois.close();
		fis.close();
	}
	//attempts to to save the game state to a file
	public void saveStateToFile(File file) throws IOException {
			FileOutputStream fos;
			ObjectOutputStream oos;
			int[][] points = new int[4][18];
			SixCardGolfPlayerType[] types = new SixCardGolfPlayerType[4];
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < gameLength; j++) {
					points[i][j] = players[i].getPoints(j);
				}
				types[i] = players[i].getPlayerType();
			}
			SixCardGolfSavedState gameState;
			if(buttonStartRound.isEnabled() == true) {
				gameState = new SixCardGolfSavedState(points, types, gameLength, roundCounter, goesFirstCounter);
			}
			else {
				gameState = new SixCardGolfSavedState(points, types, gameLength, roundCounter - 1, goesFirstCounter - 1);
			}
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(gameState);
			oos.close();
			fos.close();
	}
	
	//socket connection packet handling
	@Override
	public void packetReceived(Object object, SocketConnection connection) {
		System.out.println("packet received");
		if(object instanceof SixCardGolfGameStatePacket) {
			SixCardGolfGameStatePacket packet = (SixCardGolfGameStatePacket) object;
			turnCounter = packet.getTurnNumber();
			roundCounter = packet.getRoundNumber();
			//update player ui
			for(int i = 0; i < 4; i++) {
				//set player colors (local and remote colors swapped for this remote user)
				if(packet.getPlayers()[i].getPlayerType() == SixCardGolfPlayerType.REMOTE && i == onlinePlayerNumber)
					playerPanels[i].setColors(SixCardGolfPlayerType.LOCAL);
				else if(packet.getPlayers()[i].getPlayerType() == SixCardGolfPlayerType.LOCAL)
					playerPanels[i].setColors(SixCardGolfPlayerType.REMOTE);
				else
					playerPanels[i].setColors(packet.getPlayers()[i].getPlayerType());
				playerPanels[i].setDisplayedPoints(packet.getPlayers()[i].getTotalScore(gameLength));
				playerPanels[i].setToggleButtonsEnabled(false);
			}
			toggleButtonDiscard.setEnabled(false);
			//update displayed cards
			refreshDisplayedCards(packet.getPlayers(), packet.getTopDiscard(), packet.getDrawnCard());
			revalidate();
			repaint();
			//if the round has ended display results
			if(packet.isRoundOver() == true) {
				JOptionPane.showConfirmDialog(this,
						"Player 1: " + packet.getPlayers()[0].getHandValue() + "\nPlayer 2: " + packet.getPlayers()[1].getHandValue() + "\nPlayer 3: " + packet.getPlayers()[2].getHandValue() + "\nPlayer 4: " + packet.getPlayers()[3].getHandValue(), 
						"Round " + (packet.getRoundNumber() + 1) + " Score", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
				System.out.println("round over packet received");
				if(packet.isGameOver() == true) {
					JOptionPane.showConfirmDialog(this,
							"Total Scores\nPlayer 1: " + packet.getPlayers()[0].getTotalScore(gameLength) + "\nPlayer 2: " + packet.getPlayers()[1].getTotalScore(gameLength) + "\nPlayer 3: " + packet.getPlayers()[2].getTotalScore(gameLength) + "\nPlayer 4: " + packet.getPlayers()[3].getTotalScore(gameLength), 
							"Game Over", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
					System.out.println("game over packet received");
				}
			}
			//if the round is still going
			else {
				if(packet.getTurnNumber() != -1 && packet.getRoundNumber() != -1) {
					labelRoundNumber.setText("Round: " + (packet.getRoundNumber() + 1));
					updateTurnColors(packet.getTurnNumber());
					revalidate();
					if(packet.getTurnNumber() == onlinePlayerNumber) {
						if(packet.getDrawnCard() == null) {
							buttonDrawDeck.setEnabled(true);
							buttonDrawDiscard.setEnabled(true);
						}
						else {
							buttonSwapCard.setEnabled(true);
							toggleButtonDiscard.setEnabled(true);
							playerPanels[onlinePlayerNumber].setToggleButtonsEnabled(true);
						}
					}
				}
				//if packet is requesting a choice for flipping starting cards
				if(packet.isRequestingFlip() == true) {
					if(packet.getPlayers()[onlinePlayerNumber].getPlayerType() != SixCardGolfPlayerType.CLOSED) {
						int flipChoice1 = 0;
						int flipChoice2 = 1;
						//display a dialog box requesting a choice for which two cards to flip
						String[] flipOptions = new String[] {"1", "2", "3", "4", "5", "6"};
						JComboBox<String> comboFlip1 = new JComboBox<String>(flipOptions);
						JComboBox<String> comboFlip2 = new JComboBox<String>(flipOptions);
						JTextArea textAreaFlipMessage = new JTextArea("Choose two of your starting cards to flip over.\n"
								+ "Position numbers are as follows: 1 2 3\n"
								+ "                                                             4 5 6");
						textAreaFlipMessage.setEditable(false);
						textAreaFlipMessage.setFocusable(false);
						JPanel panelFlipOption = new JPanel();
						panelFlipOption.setLayout(new GridLayout(3, 1));
						panelFlipOption.add(comboFlip2, 2, 0);
						panelFlipOption.add(comboFlip1, 1, 0);
						panelFlipOption.add(textAreaFlipMessage, 0, 0);
						boolean validInput = false;
						do {
							comboFlip1.setSelectedIndex(0);
							comboFlip2.setSelectedIndex(1);
							int flipResult = JOptionPane.showConfirmDialog(this, panelFlipOption, "Round Starting", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
							if(flipResult == JOptionPane.OK_OPTION && comboFlip1.getSelectedIndex() != comboFlip2.getSelectedIndex()) {
								flipChoice1 = comboFlip1.getSelectedIndex();
								flipChoice2 = comboFlip2.getSelectedIndex();
								validInput = true;
							}
						}while(validInput == false);
						//send the flip choice to the host and display the bet
						SixCardGolfFlipPacket flipPacket = new SixCardGolfFlipPacket(onlinePlayerNumber, flipChoice1, flipChoice2);
						connections.get(0).sendPacket(flipPacket);
					}
				}
			}
		}
		else if(object instanceof SixCardGolfFlipPacket) {
			SixCardGolfFlipPacket flipPacket = (SixCardGolfFlipPacket) object;
			players[flipPacket.getPlayerNumber()].getHand()[flipPacket.getFlip1()].setFaceUp(true);
			players[flipPacket.getPlayerNumber()].getHand()[flipPacket.getFlip2()].setFaceUp(true);
			playerPanels[flipPacket.getPlayerNumber()].swapCard(flipPacket.getFlip1(), players[flipPacket.getPlayerNumber()].getHand()[flipPacket.getFlip1()]);
			playerPanels[flipPacket.getPlayerNumber()].swapCard(flipPacket.getFlip2(), players[flipPacket.getPlayerNumber()].getHand()[flipPacket.getFlip2()]);
			//deal cards and start turn cycle once all flip choices are received
			revalidate();
			repaint();
			if(checkIfPlayersFlipped() == true) {
				System.out.println("all flips received");
				if(isSinglePlayerGame == false && isHost == true)
					startNewTurn();
			}
		}
		else if(object instanceof SixCardGolfClientActionPacket) {
			SixCardGolfClientActionPacket inputPacket = (SixCardGolfClientActionPacket) object;
			if(inputPacket.getInput() == SixCardGolfButtonInput.DRAWDECK) {
				//player is requesting to draw from the deck
				currentDrawnCard = deck.draw();
				setDrawnIcon(currentDrawnCard);
				//update all players with status of game since turn is not over
				PlayingCard topOfDiscard;
				if(discardPile.size() > 0)
					topOfDiscard = discardPile.get(discardPile.size() - 1);
				else
					topOfDiscard = defaultFaceDownCard;
				SixCardGolfGameStatePacket tempPacket = new SixCardGolfGameStatePacket(players, turnCounter, roundCounter, topOfDiscard, currentDrawnCard, false, false, false);
				for(SocketConnection c : connections) {
					c.sendPacket(tempPacket);
				}
				revalidate();
			}
			else if(inputPacket.getInput() == SixCardGolfButtonInput.DRAWDISCARD) {
				//player is requesting to draw from the discard pile
				currentDrawnCard = discardPile.get(discardPile.size() - 1);
				discardPile.remove(discardPile.size() - 1);
				setDrawnIcon(currentDrawnCard);
				PlayingCard topOfDiscard;
				if(discardPile.size() > 0) {
					setDiscardIcon(discardPile.get(discardPile.size() - 1));
					topOfDiscard = discardPile.get(discardPile.size() - 1);
				}
				else {
					setDiscardIcon(defaultFaceDownCard);
					topOfDiscard = defaultFaceDownCard;
				}
				//update all players with status of game since turn is not over
				SixCardGolfGameStatePacket tempPacket = new SixCardGolfGameStatePacket(players, turnCounter, roundCounter, topOfDiscard, currentDrawnCard, false, false, false);
				for(SocketConnection c : connections) {
					c.sendPacket(tempPacket);
				}
				revalidate();
			}
			else if(inputPacket.getInput() == SixCardGolfButtonInput.SWAPDISCARD) {
				//player is requesting to discard their drawn card
				discardPile.add(currentDrawnCard);
				setDiscardIcon(currentDrawnCard);
				currentDrawnCard = null;
				setDrawnIcon(currentDrawnCard);
				revalidate();
				repaint();
				startNewTurn();
			}
			else if(inputPacket.getInput() == SixCardGolfButtonInput.SWAPCARDS) {
				//player is requesting to swap their drawn card with one of their cards
				PlayingCard discardedCard = players[inputPacket.getPlayerNumber()].getHand()[inputPacket.getCardSlot()];
				discardedCard.setFaceUp(true);
				discardPile.add(discardedCard);
				setDiscardIcon(discardedCard);
				players[inputPacket.getPlayerNumber()].getHand()[inputPacket.getCardSlot()] = currentDrawnCard;
				playerPanels[inputPacket.getPlayerNumber()].swapCard(inputPacket.getCardSlot(), currentDrawnCard);
				currentDrawnCard = null;
				setDrawnIcon(currentDrawnCard);
				revalidate();
				repaint();
				startNewTurn();
			}
		}
		else if(object instanceof OnlinePlayerInitializationPacket) {
			OnlinePlayerInitializationPacket packet = (OnlinePlayerInitializationPacket) object;
			onlinePlayerNumber = packet.getPlayerNumber();
			JOptionPane.showConfirmDialog(this,
				"You are player number: " + onlinePlayerNumber + ".\nPlease wait until the host starts the round.", 
				"Connected", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			playerPanels[onlinePlayerNumber].setColors(SixCardGolfPlayerType.LOCAL);
			revalidate();
		}
		else if(object instanceof ConnectionTerminationPacket) {
			ConnectionTerminationPacket closePacket = (ConnectionTerminationPacket) object;
			//if host received the disconnect packet
			if(isHost == true) {
				//close the connection and adjust connection numbers of other players
				int closingConnection = players[closePacket.getPlayerNumber()].getConnectionNumber();
				for(int i = 0; i < 4; i++){
					if(players[i].getPlayerType() == SixCardGolfPlayerType.REMOTE && 
							players[i].getConnectionNumber() > players[closePacket.getPlayerNumber()].getConnectionNumber()) {
						players[i].setConnectionNumber(players[i].getConnectionNumber() - 1);
					}
				}
				try {
					connections.get(closingConnection).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				connections.remove(closingConnection);
				if(connections.size() == 0) {
					isSinglePlayerGame = true;
					try {
						serverSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				//change disconnected player to a computer player
				players[closePacket.getPlayerNumber()].setPlayerType(SixCardGolfPlayerType.COMPUTER);
				playerPanels[closePacket.getPlayerNumber()].setColors(SixCardGolfPlayerType.COMPUTER);
				revalidate();
				//if it was the disconnected player's turn when they disconnected, then skip the rest of their turn
				if(closePacket.getPlayerNumber() == turnCounter)
					startNewTurn();
			}
			else {
				try {
					for(SocketConnection c : connections) {
						c.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				connections.clear();
				//clear and lock ui if host terminated the connection
				for(int i = 0; i < 4; i++) {
					playerPanels[i].clearForNewRound();
				}
				revalidate();
				buttonDrawDeck.setEnabled(false);
				buttonDrawDiscard.setEnabled(false);
				buttonSwapCard.setEnabled(false);
			}
			System.out.println("single player: " + isSinglePlayerGame);
		}
	}
	//sends termination packets and closes all socket connections
	@Override
	public void close() {
		if(isSinglePlayerGame == false) {
			ConnectionTerminationPacket closePacket;
			if(isHost)
				closePacket = new ConnectionTerminationPacket(-1);
			else
				closePacket = new ConnectionTerminationPacket(onlinePlayerNumber);
			for(SocketConnection c : connections) {
				//send a packet to all connections informing them of connection termination
				c.sendPacket(closePacket);
				try {
					c.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			connections.clear();
			//The server socket would not be null if the user was the host. Close the server socket.
			if(serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				serverSocket = null;
			}
			System.out.println("disconnected");
		}
	}
	//ui class that displays the player hands, name, bets, and points
	public class SixCardGolfPlayerPanel extends JPanel{
		
		private static final long serialVersionUID = -5341108043993052429L;
		
		private Font font_Ariel16B = new Font("Ariel", Font.BOLD, 16);
		private Font font_Ariel16P = new Font("Ariel", Font.PLAIN, 16);
		
		private int cardDisplayHeight;
		private int cardDisplayWidth;
		
		private JLabel labelName = new JLabel();
		private JLabel labelPoints = new JLabel();
		private JPanel cardPanel = new JPanel();
		private JPanel[] cardGridPanels = new JPanel[] {new JPanel(), new JPanel(), new JPanel(), new JPanel(), new JPanel(), new JPanel()};
		private JToggleButton[] cardToggleButtonList = new JToggleButton[] {new JToggleButton(), new JToggleButton(), new JToggleButton(), new JToggleButton(), new JToggleButton(), new JToggleButton()};
		
		public SixCardGolfPlayerPanel(String name) {
			this.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			//initial default card dimensions
			cardDisplayHeight = 60;
			cardDisplayWidth = 60 * 20 / 29;
			
			cardPanel.setLayout(new GridLayout(2, 3));			
			
			for(int i = 0; i < 6; i++) {
				cardGridPanels[i].setOpaque(true);
				cardGridPanels[i].setBackground(Color.WHITE);
				cardPanel.add(cardGridPanels[i]);
			}
			try {
				cardDisplayHeight = 60;
				cardDisplayWidth = 40;
				//get image from resources folder
				BufferedImage img = ImageIO.read(this.getClass().getResource("/images/cardback_red.png"));
				Image scaledImage = img.getScaledInstance(cardDisplayWidth, cardDisplayHeight, Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(scaledImage);
				for(int i = 0; i < 6; i++) {
					cardToggleButtonList[i].setIcon(icon);
					cardGridPanels[i].add(cardToggleButtonList[i]);
				}
			} catch (IOException e) {
			    e.printStackTrace();
			}
			revalidate();
			repaint();
			
			labelName.setFont(font_Ariel16B);
			labelName.setText(name);
			labelName.setOpaque(true);

			labelPoints.setFont(font_Ariel16P);
			labelPoints.setText("Points: 0");

			//add ui components to layout
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy = 0;
			add(labelName, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			add(labelPoints, gbc);
			
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridheight = 2;
			gbc.weightx = 4;
			gbc.gridx = 1;
			gbc.gridy = 0;
			add(cardPanel, gbc);
		}
		//setter for the points label text
		public void setDisplayedPoints(int points) {
			labelPoints.setText("Points: " + points);
		}
		//swaps out the old label for a card with a new and resized label
		public void swapCard(int index, PlayingCard card) {
			try {
				cardDisplayHeight = cardPanel.getSize().height * 2 / 5;
				cardDisplayWidth = cardDisplayHeight * 20 / 29;				
				//get image from resources folder
				BufferedImage img = ImageIO.read(this.getClass().getResource(card.getCardResourcePath(card.isFaceUp())));
				Image scaledImage = img.getScaledInstance(cardDisplayWidth, cardDisplayHeight, Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(scaledImage);
				cardGridPanels[index].remove(cardToggleButtonList[index]);
				cardToggleButtonList[index].setIcon(icon);
				cardToggleButtonList[index].setDisabledIcon(icon);
				cardGridPanels[index].add(cardToggleButtonList[index]);
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}
		//enables or disables all card toggle buttons for this player panel
		public void setToggleButtonsEnabled(boolean enabled) {
			for(int i = 0; i < 6; i++) {
				cardToggleButtonList[i].setEnabled(enabled);
			}
		}
		//returns a toggle button from the list
		public JToggleButton getCardToggleButton(int index) {
			return cardToggleButtonList[index];
		}
		//resets player panel display for a new round
		public void clearForNewRound() {
			for(int i = 0; i < 6; i++) {
				cardGridPanels[i].remove(cardToggleButtonList[i]);
				cardToggleButtonList[i].setIcon(null);
				cardGridPanels[i].add(cardToggleButtonList[i]);
			}
		}
		//sets colors of text for the labels to indicate the type of player
		public void setColors(SixCardGolfPlayerType playerType) {
			if(playerType == SixCardGolfPlayerType.CLOSED)
				labelName.setForeground(Color.LIGHT_GRAY);
			else if(playerType == SixCardGolfPlayerType.LOCAL)
				labelName.setForeground(Color.BLUE);
			else if(playerType == SixCardGolfPlayerType.COMPUTER)
				labelName.setForeground(Color.BLACK);
			else if(playerType == SixCardGolfPlayerType.REMOTE)
				labelName.setForeground(Color.RED);
		}
		//used to highlight player name and hand yellow when it is their turn and white when it is not their turn
		public void setTurnColor(boolean isMyTurn) {
			if(isMyTurn == true)
				labelName.setBackground(Color.YELLOW);
			else
				labelName.setBackground(Color.WHITE);
		}
	}

}


