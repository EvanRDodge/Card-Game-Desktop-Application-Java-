package controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import model.BlackjackBetPacket;
import model.BlackjackClientActionPacket;
import model.BlackjackClientActionPacket.BlackjackButtonInput;
import model.BlackjackGameStatePacket;
import model.OnlinePlayerInitializationPacket;
import model.BlackjackPlayer;
import model.BlackjackRoundResultsPacket;
import model.BlackjackSavedState;
import model.ConnectionTerminationPacket;
import model.PlayingCard;
import model.PlayingCardDeck;
import model.PlayingCardSuit;
import model.PlayingCardValue;
//this class is the blackjack game screen. it controls the logic of the game and the ui
public class BlackjackGamePanel extends JPanel implements ActionListener, SocketedController{
	
	private static final int PORTNUMBER = 8446;
	
	public enum BlackjackPlayerType {
		CLOSED, LOCAL, COMPUTER, REMOTE, DEALER
	}
		
	private Font font_Ariel16B = new Font("Ariel", Font.BOLD, 16);
	
	private ArrayList<BlackjackPlayer> players = new ArrayList<BlackjackPlayer>();
	private ArrayList<BlackjackPlayerPanel> playerPanels = new ArrayList<BlackjackPlayerPanel>();
	
	private JButton buttonStartRound = new JButton();
	private JButton buttonHit = new JButton();
	private JButton buttonStay = new JButton();
	
	private boolean isFirstRound = true;
	private int onlinePlayerNumber;
	private int openPlayerSlotCount = 0;
	private int turnCounter = 0;
	private int handCounter = 0;
	private boolean isHost = true;
	private boolean isSinglePlayerGame = true;
	private int deckCount = 0;
	private PlayingCardDeck deck;
	
	private ServerSocket serverSocket;
	private ArrayList<SocketConnection> connections = new ArrayList<SocketConnection>();
	
	public BlackjackGamePanel() {
		onlinePlayerNumber = -1;
		
		buttonStartRound.setText("Start Round");
		buttonStartRound.setFont(font_Ariel16B);
		buttonStartRound.setEnabled(false);
		buttonStartRound.addActionListener(this);
		
		buttonHit.setText("Hit");
		buttonHit.setFont(font_Ariel16B);
		buttonHit.setEnabled(false);
		buttonHit.addActionListener(this);

		buttonStay.setText("Stay");
		buttonStay.setFont(font_Ariel16B);
		buttonStay.setEnabled(false);
		buttonStay.addActionListener(this);
		
		playerPanels.add(new BlackjackPlayerPanel("Dealer"));
		playerPanels.add(new BlackjackPlayerPanel("Player 1"));
		playerPanels.add(new BlackjackPlayerPanel("Player 2"));
		playerPanels.add(new BlackjackPlayerPanel("Player 3"));
		playerPanels.add(new BlackjackPlayerPanel("Player 4"));

		//add ui components to layout
		GridBagLayout gbcLayout = new GridBagLayout();
		setLayout(gbcLayout);
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 8;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(playerPanels.get(1), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		add(playerPanels.get(2), gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(playerPanels.get(3), gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 1;
		add(playerPanels.get(4), gbc);
		
		gbc.gridheight = 3;
		gbc.gridwidth = 3;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(playerPanels.get(0), gbc);
		
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.gridx = 3;
		gbc.gridy = 2;
		add(buttonStartRound, gbc);
		
		gbc.gridx = 3;
		gbc.gridy = 3;
		add(buttonHit, gbc);

		gbc.gridx = 3;
		gbc.gridy = 4;
		add(buttonStay, gbc);
	}
	
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
		handCounter = -1;
		isHost = false;
		isSinglePlayerGame = false;
		buttonStartRound.setEnabled(false);
		buttonHit.setEnabled(false);
		buttonStay.setEnabled(false);
		players.clear();
		//reset ui; add a test card to each panel to get the card sizing right
		PlayingCard initializeCard = new PlayingCard(PlayingCardValue.JOKER, PlayingCardSuit.NONE);
		playerPanels.get(0).addCard(0, initializeCard);
		playerPanels.get(0).clearForNewRound();
		playerPanels.get(0).setTurnColor(false, 0);
		playerPanels.get(0).setBetColor(0, Color.WHITE);
		for(int i = 1; i < 5; i++) {
			playerPanels.get(i).addCard(0, initializeCard);
			playerPanels.get(i).addCard(1, initializeCard);
			playerPanels.get(i).clearForNewRound();
			playerPanels.get(i).setDisplayedPoints(0);
			playerPanels.get(i).setDisplayedBet(0, 0);
			playerPanels.get(i).setDisplayedBet(1, 0);
			playerPanels.get(i).setBetColor(0, Color.WHITE);
			playerPanels.get(i).setBetColor(1, Color.WHITE);
			playerPanels.get(i).setColors(BlackjackPlayerType.CLOSED);	
			playerPanels.get(i).setTurnColor(false, 0);
		}
		return true;
	}
	
	public void initializeGame(int points, int deckCount, BlackjackPlayerType PT1, BlackjackPlayerType PT2, BlackjackPlayerType PT3, BlackjackPlayerType PT4) {
		System.out.println("initializing game: " + points + " starting points | " + PT1.toString() + " | " + PT2.toString() + " | " + PT3.toString() + " | " + PT4.toString());
		isHost = true;
		turnCounter = -1;
		handCounter = -1;
		isSinglePlayerGame = true;
		this.deckCount = deckCount;
		buttonHit.setEnabled(false);
		buttonStay.setEnabled(false);
		//clear and create new player objects
		players.clear();
		players.add(new BlackjackPlayer(0, points, BlackjackPlayerType.DEALER));
		players.add(new BlackjackPlayer(1, points, PT1));
		players.add(new BlackjackPlayer(2, points, PT2));
		players.add(new BlackjackPlayer(3, points, PT3));
		players.add(new BlackjackPlayer(4, points, PT4));
		//reset ui
		playerPanels.get(0).clearForNewRound();
		playerPanels.get(0).setTurnColor(false, 0);
		playerPanels.get(0).setBetColor(0, Color.WHITE);
		for(int i = 1; i < 5; i++) {
			playerPanels.get(i).setTurnColor(false, 0);
			playerPanels.get(i).clearForNewRound();
			playerPanels.get(i).setDisplayedPoints(points);
			playerPanels.get(i).setDisplayedBet(0, 0);
			playerPanels.get(i).setDisplayedBet(1, 0);
			playerPanels.get(i).setBetColor(0, Color.WHITE);
			playerPanels.get(i).setBetColor(1, Color.WHITE);
			if(players.get(i).getPlayerType() == BlackjackPlayerType.REMOTE) {
				playerPanels.get(i).setColors(BlackjackPlayerType.CLOSED);
				isSinglePlayerGame = false;
			}
			else {
				playerPanels.get(i).setColors(players.get(i).getPlayerType());
			}
		}
		deck = new PlayingCardDeck(deckCount, 0);
		deck.shuffle();
		if(isSinglePlayerGame)
			buttonStartRound.setEnabled(true);
		else {
			//set up thread to listen for connecting players
			BlackjackGamePanel frameHandle = this;
			new Thread() {
				public void run() {
					openPlayerSlotCount = 0;
					for(int i = 1; i < 5; i++) {
						if(players.get(i).getPlayerType() == BlackjackPlayerType.REMOTE) {
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
									for(int i = 1; i < 5; i++) {
										if(players.get(i).getPlayerType() == BlackjackPlayerType.REMOTE && players.get(i).getConnectionNumber() == -1) {
											players.get(i).setConnectionNumber(connections.size() - 1);
											//send a packet telling the player what their player number is
											OnlinePlayerInitializationPacket tempPacket = new OnlinePlayerInitializationPacket(i);
											connections.get(connections.size() - 1).sendPacket(tempPacket);
											playerPanels.get(i).setColors(BlackjackPlayerType.REMOTE);
											i = 5;
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
			JOptionPane.showConfirmDialog(this, "Players will not be able to connect after the first round starts.\n"
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
			turnCounter = 1;
			handCounter = -1;
			//prepare deck for another round
			if(deck.getSize() <= 60 + (5 * (deck.getNumberStandardDecks() - 6))) {
				deck.rebuildDeck();
				deck.shuffle();
			}
			//close any unclaimed remote player slots
			openPlayerSlotCount = 0;
			for(int i = 1; i < 5; i++) {
				if(players.get(i).getPlayerType() == BlackjackPlayerType.REMOTE && players.get(i).getConnectionNumber() == -1) {
					players.get(i).setPlayerType(BlackjackPlayerType.CLOSED);
				}
			}
			//check if there are any remote players in the first round
			if(isFirstRound == true && serverSocket != null) {
				isSinglePlayerGame = true;
				int i = 1;
				while(isSinglePlayerGame == true && i < 5) {
					if(players.get(i).getPlayerType() == BlackjackPlayerType.REMOTE) {
						isSinglePlayerGame = false;
					}
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
			//clear cards and reset bets
			for(int i = 0; i < 5; i++) {
				players.get(i).clearHands();
				players.get(i).setBet(0, 0);
				players.get(i).setBet(1, 0);
				playerPanels.get(i).clearForNewRound();
				playerPanels.get(i).setBetColor(0, Color.WHITE);
				playerPanels.get(i).setBetColor(1, Color.WHITE);
				revalidate();
				repaint();
			}
			//update ui and request bets from remote players
			if(isHost == true && isSinglePlayerGame == false) {
				for(SocketConnection c : connections) {
					BlackjackGameStatePacket tempPacket = new BlackjackGameStatePacket(players, -1, -1, true);
					c.sendPacket(tempPacket);
				}
			}
			//request local and computer bets	
			for(int i = 1; i < 5; i++) {
				//request bet for a local player
				if(players.get(i).getPlayerType() == BlackjackPlayerType.LOCAL) {
					String betResult = (String)JOptionPane.showInputDialog(this, "You have " + players.get(i).getPoints() + " points. Enter your bet.", 
							"Place Your Bet", JOptionPane.PLAIN_MESSAGE, null, null, "1");
					int bet = 0;
					boolean invalidBet = false;
					//check for valid input and ask again until valid input
					do {
						try {
							bet = Integer.parseInt(betResult);
							if(bet > 0 && bet <= players.get(i).getPoints())
								invalidBet = false;
							else {
								betResult = (String)JOptionPane.showInputDialog(this, "Invalid Bet! You have " + players.get(i).getPoints() + " points. Enter your bet.", 
										"Place Your Bet", JOptionPane.ERROR_MESSAGE, null, null, "1");
								invalidBet = true;
							}
						}
						catch(NumberFormatException e) {
							betResult = (String)JOptionPane.showInputDialog(this, "Invalid Bet! You have " + players.get(i).getPoints() + " points. Enter your bet.", 
									"Place Your Bet", JOptionPane.ERROR_MESSAGE, null, null, "1");
							invalidBet = true;
						}
					} while(invalidBet == true);
					//save and display the bet
					players.get(i).setBet(0, bet);
					playerPanels.get(i).setDisplayedBet(0, bet);
				}
				//generate bet for a computer controlled player
				else if(players.get(i).getPlayerType() == BlackjackPlayerType.COMPUTER) {
					Random rn = new Random();
					int bet = rn.nextInt(players.get(i).getPoints() * 3 / 4 + 1);
					if(bet == 0)
						bet++;
					players.get(i).setBet(0, bet);
					playerPanels.get(i).setDisplayedBet(0, bet);
				}
			}
			//deal cards and start turn cycle once all bets are received
			if(checkAllBetsPlaced() == true) {
				dealStartingHands();
				revalidate();
				repaint();
				System.out.println("all bets received");
				startNewTurn();
			}
		}
		if(event.getSource() == buttonStay) {
			buttonHit.setEnabled(false);
			buttonStay.setEnabled(false);
			if(isHost == true) {
				startNewTurn();
			}
			else {
				BlackjackClientActionPacket inputPacket = new BlackjackClientActionPacket(onlinePlayerNumber, 0, BlackjackButtonInput.STAY);
				connections.get(0).sendPacket(inputPacket);
				System.out.println("sending stay input packet");
			}
		}
		if(event.getSource() == buttonHit) {
			if(isHost == true) {
				PlayingCard tempCard = deck.draw();
				players.get(turnCounter).getCards(handCounter).add(tempCard);
				playerPanels.get(turnCounter).addCard(handCounter, tempCard);
				revalidate();
				//end turn prematurely if drawing a card brings the player's hand over 21
				if(players.get(turnCounter).getHandValue(handCounter) > 21) {
					buttonStay.doClick();
				}
				else if(isSinglePlayerGame == false){
					//update remote players
					BlackjackGameStatePacket tempPacket = new BlackjackGameStatePacket(players, turnCounter, handCounter, false);
					for(SocketConnection c : connections) {
						c.sendPacket(tempPacket);
					}
				}
			}
			else {
				buttonHit.setEnabled(false);
				buttonStay.setEnabled(false);
				BlackjackClientActionPacket inputPacket = new BlackjackClientActionPacket(onlinePlayerNumber, handCounter, BlackjackButtonInput.HIT);
				connections.get(0).sendPacket(inputPacket);
			}
		}
	}
	//This function increments the turn counters, updates remote players with the game status, and lets local and computer players take their turn
	public void startNewTurn() {
		//out of bounds turn and hand counter handling
		handCounter++;
		if(handCounter > 1) {
			handCounter = 0;
			turnCounter ++;
			if(turnCounter > 4) {
				turnCounter = 0;
			}
		}
		//skip closed player slot turns
		if(players.get(turnCounter).getPlayerType() == BlackjackPlayerType.CLOSED || players.get(turnCounter).getCards(handCounter).size() <= 0) {
			startNewTurn();	
		}
		else {
			//display whose turn it is to host
			updateTurnColors(turnCounter, handCounter);
			revalidate();
			//update remote players
			if(isSinglePlayerGame == false && turnCounter != 0) {
				BlackjackGameStatePacket tempPacket = new BlackjackGameStatePacket(players, turnCounter, handCounter, false);
				for(SocketConnection c : connections) {
					c.sendPacket(tempPacket);
				}
			}
			//if it is a local turn
			if(players.get(turnCounter).getPlayerType() == BlackjackPlayerType.LOCAL) {
				buttonHit.setEnabled(true);
				buttonStay.setEnabled(true);
				//ask the player if they will split their hand if they have an eligible starting hand
				if(handCounter == 0 && 
						players.get(turnCounter).getCards(0).get(0).getValue() == players.get(turnCounter).getCards(0).get(1).getValue() &&
						players.get(turnCounter).getBet(0) * 2 <= players.get(turnCounter).getPoints())
				{
					int isSplitting = JOptionPane.showConfirmDialog(this,
							"You may split this starting hand into two separate hands.\nThe second hand will receive a bet equal to your original bet and both hands will get a card.\nDo you want to split this hand?", 
							"Split?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		            if(isSplitting == JOptionPane.YES_OPTION) {
		            	//move card from first hand to second hand
		            	players.get(turnCounter).getCards(1).add(players.get(turnCounter).getCards(0).get(1));
		            	players.get(turnCounter).getCards(0).remove(1);
		            	//draw new cards and update displayed cards
		            	for(int i = 0; i < 2; i++) {
			            	PlayingCard tempCard = deck.draw();
			            	players.get(turnCounter).getCards(i).add(tempCard);
			            	playerPanels.get(turnCounter).removeAllCards(i);
			            	for(int j = 0; j < 2; j++) {
				            	playerPanels.get(turnCounter).addCard(i, players.get(turnCounter).getCards(i).get(j));
			            	}
		            	}
						revalidate();
		            	//copy bet over to second hand
		            	players.get(turnCounter).setBet(1, players.get(turnCounter).getBet(0));
		            	playerPanels.get(turnCounter).setDisplayedBet(1, players.get(turnCounter).getBet(1));
		            	if(isSinglePlayerGame == false){
							//update remote players
							BlackjackGameStatePacket tempPacket = new BlackjackGameStatePacket(players, turnCounter, handCounter, false);
							for(SocketConnection c : connections) {
								c.sendPacket(tempPacket);
							}
						}
		            }
				}
				//ask player if they will double down if they have an eligible starting hand
				if(players.get(turnCounter).getCards(handCounter).size() == 2 &&
						players.get(turnCounter).getBet(0) + players.get(turnCounter).getBet(1) + players.get(turnCounter).getBet(handCounter) <= players.get(turnCounter).getPoints() &&
						(players.get(turnCounter).getHandValue(handCounter) == 9 || 
						players.get(turnCounter).getHandValue(handCounter) == 10 || 
						players.get(turnCounter).getHandValue(handCounter) == 11)) {
					int isDoublingDown = JOptionPane.showConfirmDialog(this,
							"You may double down on this starting hand.\nIf you do you will get one more card and your bet will be doubled.\nDo you want to double down?", 
							"Double Down?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		            if(isDoublingDown == JOptionPane.YES_OPTION) {
		            	//double the bet
		            	players.get(turnCounter).setBet(handCounter, players.get(turnCounter).getBet(handCounter) * 2);
		            	playerPanels.get(turnCounter).setDisplayedBet(handCounter, players.get(turnCounter).getBet(handCounter));
		            	//draw one card face down
		            	PlayingCard tempCard = deck.draw();
		            	tempCard.setFaceUp(false);
		    			players.get(turnCounter).getCards(handCounter).add(tempCard);
		    			playerPanels.get(turnCounter).addCard(handCounter, tempCard);
						revalidate();
		    			//end turn
		    			buttonStay.doClick();
		            }
				}
			}
			//if it is a computer turn
			else if(players.get(turnCounter).getPlayerType() == BlackjackPlayerType.COMPUTER) {
				while(players.get(turnCounter).getHandValue(handCounter) < 15) {
					PlayingCard tempCard = deck.draw();
					players.get(turnCounter).getCards(handCounter).add(tempCard);
					playerPanels.get(turnCounter).addCard(handCounter, tempCard);
				}
				revalidate();
				startNewTurn();
			}
			//if it is the dealer's turn
			else if(players.get(turnCounter).getPlayerType() == BlackjackPlayerType.DEALER) {
				//turn all cards face up and refresh host ui
				for(int i = 0; i < 5; i++) {
					if(players.get(i).getPlayerType() != BlackjackPlayerType.CLOSED) {
						for(int j = 0; j < 2; j++) {
							playerPanels.get(i).removeAllCards(j);
							for(int k = 0; k < players.get(i).getCards(j).size(); k++) {
								players.get(i).getCards(j).get(k).setFaceUp(true);
								playerPanels.get(i).addCard(j, players.get(i).getCards(j).get(k));
							}
						}
					}
				}
				//dealer draws cards until total value of hand is 17 or greater
				while(players.get(0).getHandValue(0) < 17) {
					PlayingCard tempCard = deck.draw();
					players.get(0).getCards(0).add(tempCard);
					playerPanels.get(0).addCard(0, tempCard);
				}
				//evaluate player hands
				int[][] roundResults = roundEndSettlement();
				//disable players that have run out of points
				for(int i = 1; i < 5; i++) {
					if(players.get(i).getPoints() <= 0) {
						players.get(i).setPlayerType(BlackjackPlayerType.CLOSED);
						playerPanels.get(i).setColors(players.get(i).getPlayerType());
						players.get(i).setBet(0, 0);
						players.get(i).setBet(1, 0);
					}
				}
				revalidate();
				repaint();
				if(isSinglePlayerGame == false) {
					//send results and a ui update to remote players
					BlackjackRoundResultsPacket resultPacket = new BlackjackRoundResultsPacket(players, roundResults);
					for(SocketConnection c : connections) {
						c.sendPacket(resultPacket);
					}
				}
				//re-enable start button
				buttonStartRound.setEnabled(true);
			}
		}
	}
	//Evaluates card hands and allocates payments. Returns an array of results that can be sent to remote clients (-1 = loss, 0 = tie, 1 = win).
	public int[][] roundEndSettlement() {
		int[][] resultArray = {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}};
		int dealerValue = players.get(0).getHandValue(0);
		//if dealer has natural 21
		if(dealerValue == 21 && players.get(0).getCards(0).size() == 2) {
			for(int i = 1; i < 5; i++) {
				if(players.get(i).getPlayerType() != BlackjackPlayerType.CLOSED) {
					for(int j = 0; j < 2; j++) {
						if(!players.get(i).getCards(j).isEmpty()) {
							//tie if both player and dealer have natural 21
							if(players.get(i).getHandValue(j) == 21 && players.get(i).getCards(j).size() == 2) {
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, true, true);
								resultArray[i][j] = 0;
							}
							//player does not have natural 21 and loses
							else {
								players.get(i).setPoints(players.get(i).getPoints() - players.get(i).getBet(j));
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, false, false);
								resultArray[i][j] = -1;
							}
						}
					}	
				}
			}
		}
		//dealer went over
		else if(dealerValue > 21) {
			for(int i = 1; i < 5; i++) {
				if(players.get(i).getPlayerType() != BlackjackPlayerType.CLOSED) {
					for(int j = 0; j < 2; j++) {
						if(!players.get(i).getCards(j).isEmpty()) {
							//natural 21
							if(players.get(i).getHandValue(j) == 21 && players.get(i).getCards(j).size() == 2) {
								players.get(i).setPoints((int) Math.round(players.get(i).getPoints() + players.get(i).getBet(j) * 1.5));
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, true, false);
								resultArray[i][j] = 1;
							}
							//21 or under then player wins
							else if(players.get(i).getHandValue(j) <= 21) {
								players.get(i).setPoints(players.get(i).getPoints() + players.get(i).getBet(j));
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, true, false);
								resultArray[i][j] = 1;
							}
							//over 21 then player loses
							else {
								players.get(i).setPoints(players.get(i).getPoints() - players.get(i).getBet(j));
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, false, false);
								resultArray[i][j] = -1;
							}
						}
					}
				}
			}
		}
		//dealer did not go over
		else {
			for(int i = 1; i < 5; i++) {
				if(players.get(i).getPlayerType() != BlackjackPlayerType.CLOSED) {
					for(int j = 0; j < 2; j++) {
						if(!players.get(i).getCards(j).isEmpty()) {
							//natural 21 then player wins
							if(players.get(i).getHandValue(j) == 21 && players.get(i).getCards(j).size() == 2) {
								players.get(i).setPoints((int) Math.round(players.get(i).getPoints() + players.get(i).getBet(j) * 1.5));
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, true, false);
								resultArray[i][j] = 1;
							}
							//21 or under and greater than dealer then player wins
							else if(players.get(i).getHandValue(j) <= 21 && players.get(i).getHandValue(j) > dealerValue) {
								players.get(i).setPoints(players.get(i).getPoints() + players.get(i).getBet(j));
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, true, false);
								resultArray[i][j] = 1;
							}
							//over 21  or less than dealer then player loses
							else if(players.get(i).getHandValue(j) > 21 || players.get(i).getHandValue(j) < dealerValue) {
								players.get(i).setPoints(players.get(i).getPoints() - players.get(i).getBet(j));
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, false, false);
								resultArray[i][j] = -1;
							}
							//tie
							else {
								playerPanels.get(i).setDisplayedPoints(players.get(i).getPoints());
								playerPanels.get(i).updateRoundEndHandDisplay(j, true, true);
								resultArray[i][j] = 0;
							}
						}
					}
				}
			}
		}
		return resultArray;
	}
	
	public boolean isSinglePlayer() {
		return isSinglePlayerGame;
	}
	//Checks bets for all player's starting hand. Returns true if all non-closed player slots have a positive bet.
	public boolean checkAllBetsPlaced() {
		for(int i = 1; i < 5; i++) {
			if(players.get(i).getBet(0) <= 0 && players.get(i).getPlayerType() != BlackjackPlayerType.CLOSED)
				return false;
		}
		return true;
	}
	//this function deals each player two face up cards and gives the dealer one face up card and one face down card. this function also adds the new cards to the player panels for display
	public void dealStartingHands() {
		//deal dealer's cards; one is face down
		PlayingCard tempCard = deck.draw();
		players.get(0).getCards(0).add(tempCard);
		playerPanels.get(0).addCard(0, tempCard);
		tempCard = deck.draw();
		tempCard.setFaceUp(false);
		players.get(0).getCards(0).add(tempCard);
		playerPanels.get(0).addCard(0, tempCard);
		//deal player cards
		for(int i = 1; i < 5; i++) {
			if(players.get(i).getPlayerType() != BlackjackPlayerType.CLOSED) {
				tempCard = deck.draw();
				players.get(i).getCards(0).add(tempCard);
				playerPanels.get(i).addCard(0, tempCard);
				tempCard = deck.draw();
				players.get(i).getCards(0).add(tempCard);
				playerPanels.get(i).addCard(0, tempCard);
			}
		}
	}
	//Clears displayed hands and displays hands according to the player list parameter.
	public void refreshDisplayedCards(ArrayList<BlackjackPlayer> playerList) {
		for(int i = 0; i < 5; i++) {
			playerPanels.get(i).removeAllCards(0);
			playerPanels.get(i).removeAllCards(1);
			for(int j = 0; j < 2; j++) {
				for(int k = 0; k < playerList.get(i).getCards(j).size(); k++) {
					playerPanels.get(i).addCard(j, playerList.get(i).getCards(j).get(k));
				}
			}
		}
	}
	//Updates the highlights on the player hand and name to indicate a specific player and hand's turn.
	public void updateTurnColors(int turnNumber, int handNumber) {
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 2; j++) {
				if(i == turnNumber) {
					playerPanels.get(i).setTurnColor(true, handNumber);
				}
				else {
					playerPanels.get(i).setTurnColor(false, handNumber);
				}
			}
		}
	}
	//attempts to initialize the game according to the save game state object in the provided file
	public void loadStateFromFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		try {
			BlackjackSavedState gameState = (BlackjackSavedState)ois.readObject();
			
			initializeGame(100, gameState.getStandardDeckCount(), gameState.getPlayerTypes()[1], gameState.getPlayerTypes()[2], gameState.getPlayerTypes()[3], gameState.getPlayerTypes()[4]);
			for(int i = 1; i < 5; i++) {
				players.get(i).setPoints(gameState.getPlayerPoints()[i]);
				playerPanels.get(i).setDisplayedPoints(gameState.getPlayerPoints()[i]);
			}
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
		int[] points = new int[5];
		BlackjackPlayerType[] types = new BlackjackPlayerType[5];
		for(int i = 0; i < 5; i++) {
			points[i] = players.get(i).getPoints();
			types[i] = players.get(i).getPlayerType();
		}
		BlackjackSavedState gameState = new BlackjackSavedState(points, types, deck.getNumberStandardDecks());
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
		if(object instanceof BlackjackGameStatePacket) {
			System.out.println("state packet received by " + onlinePlayerNumber);
			BlackjackGameStatePacket packet = (BlackjackGameStatePacket) object;
			turnCounter = packet.getTurnNumber();
			handCounter = packet.getHandNumber();
			//update dealer ui
			playerPanels.get(0).setDisplayedPoints(packet.getPlayers().get(0).getPoints());
			playerPanels.get(0).removeAllCards(0);
			for(int i = 0; i < packet.getPlayers().get(0).getCards(0).size(); i++) {
				playerPanels.get(0).addCard(0, packet.getPlayers().get(0).getCards(0).get(i));
			}
			//update player ui
			for(int i = 1; i < 5; i++) {
				//set player colors (local and remote colors swapped for this specific remote user)
				if(packet.getPlayers().get(i).getPlayerType() == BlackjackPlayerType.REMOTE && i == onlinePlayerNumber)
					playerPanels.get(i).setColors(BlackjackPlayerType.LOCAL);
				else if(packet.getPlayers().get(i).getPlayerType() == BlackjackPlayerType.LOCAL)
					playerPanels.get(i).setColors(BlackjackPlayerType.REMOTE);
				else
					playerPanels.get(i).setColors(packet.getPlayers().get(i).getPlayerType());
				//update points and bet labels
				playerPanels.get(i).setDisplayedPoints(packet.getPlayers().get(i).getPoints());
				for(int j = 0; j < 2; j++) {
					playerPanels.get(i).setDisplayedBet(j, packet.getPlayers().get(i).getBet(j));
				}				
			}
			//update displayed cards
			refreshDisplayedCards(packet.getPlayers());
			revalidate();
			repaint();
			if(packet.getTurnNumber() != -1 && packet.getHandNumber() != -1) {
				updateTurnColors(packet.getTurnNumber(), packet.getHandNumber());
				revalidate();
				if(packet.getTurnNumber() == onlinePlayerNumber) {
					boolean splitting = false;
					//check if player can split their hand
					if(packet.getHandNumber() == 0 && 
							packet.getPlayers().get(onlinePlayerNumber).getCards(0).size() == 2 && 
							packet.getPlayers().get(onlinePlayerNumber).getCards(1).size() <= 0 && 
							packet.getPlayers().get(onlinePlayerNumber).getCards(0).get(0).getValue() == packet.getPlayers().get(onlinePlayerNumber).getCards(0).get(1).getValue() &&
							packet.getPlayers().get(onlinePlayerNumber).getBet(0) * 2 <= packet.getPlayers().get(onlinePlayerNumber).getPoints()) {
						int splitResponse = JOptionPane.showConfirmDialog(this,
								"You may split this starting hand into two separate hands.\nThe second hand will receive a bet equal to your original bet and both hands will get a card.\nDo you want to split this hand?", 
								"Split?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			            if(splitResponse == JOptionPane.YES_OPTION) {
			            	BlackjackClientActionPacket inputPacket = new BlackjackClientActionPacket(onlinePlayerNumber, packet.getHandNumber(), BlackjackButtonInput.SPLIT);
			            	for(SocketConnection c : connections) {
								c.sendPacket(inputPacket);
							}
			            	splitting = true;
			            }
					}
					if(splitting == false) {
						//check if the player can double down
						if(packet.getPlayers().get(onlinePlayerNumber).getCards(packet.getHandNumber()).size() == 2 &&
								packet.getPlayers().get(onlinePlayerNumber).getBet(0) + packet.getPlayers().get(onlinePlayerNumber).getBet(1) + packet.getPlayers().get(onlinePlayerNumber).getBet(packet.getHandNumber())  <= packet.getPlayers().get(onlinePlayerNumber).getPoints() &&
								(packet.getPlayers().get(onlinePlayerNumber).getHandValue(packet.getHandNumber()) == 9 ||
								packet.getPlayers().get(onlinePlayerNumber).getHandValue(packet.getHandNumber()) == 10 ||
								packet.getPlayers().get(onlinePlayerNumber).getHandValue(packet.getHandNumber()) == 11)) {
							int doubleDownResponse = JOptionPane.showConfirmDialog(this,
									"You may double down on this starting hand.\nIf you do you will get one more card and your bet will be doubled.\nDo you want to double down?", 
									"Double Down?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				            if(doubleDownResponse == JOptionPane.YES_OPTION) {
				            	BlackjackClientActionPacket inputPacket = new BlackjackClientActionPacket(onlinePlayerNumber, packet.getHandNumber(), BlackjackButtonInput.DOUBLEDOWN);
				            	for(SocketConnection c : connections) {
									c.sendPacket(inputPacket);
								}
				            }
				            //if player chooses not to double down then they may take their turn now
				            else {
				            	buttonHit.setEnabled(true);
								buttonStay.setEnabled(true);
				            }
						}
						//if split or doubled down they must wait for host to process those plays otherwise the ui unlocks for the player to take their turn
						else {
							buttonHit.setEnabled(true);
							buttonStay.setEnabled(true);
						}
					}
				}
				//if it is not the player's turn then lock ui buttons
				else {
					buttonHit.setEnabled(false);
					buttonStay.setEnabled(false);
				}
			}
			//if bet was requested then prompt user to enter a bet and send it to host
			if(packet.getIsRequestingBet() == true) {
				for(int i = 1; i < 5; i++) {
					playerPanels.get(i).setBetColor(0, Color.WHITE);
					playerPanels.get(i).setBetColor(1, Color.WHITE);
				}
				revalidate();
				if(packet.getPlayers().get(onlinePlayerNumber).getPlayerType() != BlackjackPlayerType.CLOSED) {
					String betResult = (String)JOptionPane.showInputDialog(this, "You have " + packet.getPlayers().get(onlinePlayerNumber).getPoints() + " points. Enter your bet.", 
							"Place Your Bet", JOptionPane.PLAIN_MESSAGE, null, null, "1");
					int bet = 0;
					boolean invalidBet = false;
					//check for valid input and ask again until valid input
					do {
						try {
							bet = Integer.parseInt(betResult);
							if(bet > 0 && bet <= packet.getPlayers().get(onlinePlayerNumber).getPoints())
								invalidBet = false;
							else {
								betResult = (String)JOptionPane.showInputDialog(this, "Invalid Bet! You have " + packet.getPlayers().get(onlinePlayerNumber).getPoints() + " points. Enter your bet.", 
										"Place Your Bet", JOptionPane.ERROR_MESSAGE, null, null, "1");
								invalidBet = true;
							}
						}
						catch(NumberFormatException e) {
							betResult = (String)JOptionPane.showInputDialog(this, "Invalid Bet! You have " + packet.getPlayers().get(onlinePlayerNumber).getPoints() + " points. Enter your bet.", 
									"Place Your Bet", JOptionPane.ERROR_MESSAGE, null, null, "1");
							invalidBet = true;
						}
					} while(invalidBet == true);
					//send the bet to the host and display the bet
					BlackjackBetPacket betPacket = new BlackjackBetPacket(onlinePlayerNumber, 0, bet);
					connections.get(0).sendPacket(betPacket);
					playerPanels.get(onlinePlayerNumber).setDisplayedBet(0, bet);
				}
			}
			
		}
		else if(object instanceof BlackjackBetPacket) {
			BlackjackBetPacket betPacket = (BlackjackBetPacket) object;
			players.get(betPacket.getPlayerNumber()).setBet(betPacket.getHandNumber(), betPacket.getBet());
			playerPanels.get(betPacket.getPlayerNumber()).setDisplayedBet(betPacket.getHandNumber(), betPacket.getBet());
			//deal cards and start turn cycle once all bets are received
			if(checkAllBetsPlaced() == true) {
				dealStartingHands();
				System.out.println("all bets received");
				if(isSinglePlayerGame == false && isHost == true)
					startNewTurn();
			}
		}
		else if(object instanceof BlackjackClientActionPacket) {
			BlackjackClientActionPacket inputPacket = (BlackjackClientActionPacket) object;
			if(inputPacket.getInput() == BlackjackButtonInput.STAY) {
				System.out.println("stay input packet from player " + inputPacket.getPlayerNumber());
				startNewTurn();
			}
			else if(inputPacket.getInput() == BlackjackButtonInput.HIT) {
				System.out.println("hit input packet from player " + inputPacket.getPlayerNumber());
				//draw a card for that player
				PlayingCard tempCard = deck.draw();
				players.get(inputPacket.getPlayerNumber()).getCards(inputPacket.getHandNumber()).add(tempCard);
				playerPanels.get(inputPacket.getPlayerNumber()).addCard(inputPacket.getHandNumber(), tempCard);
				revalidate();
				//if the client hand went over then start next turn
				if(players.get(inputPacket.getPlayerNumber()).getHandValue(inputPacket.getHandNumber()) > 21)
					startNewTurn();
				//send an update to players if client hand did not go over 21
				else {
					playerPanels.get(inputPacket.getPlayerNumber()).addCard(inputPacket.getHandNumber(), tempCard);
					BlackjackGameStatePacket tempPacket = new BlackjackGameStatePacket(players, turnCounter, handCounter, false);
					for(SocketConnection c : connections) {
						c.sendPacket(tempPacket);
					}
				}
			}
			else if(inputPacket.getInput() == BlackjackButtonInput.SPLIT) {
				System.out.println("split input packet from player " + inputPacket.getPlayerNumber());
				//move card from first hand to second hand
            	players.get(inputPacket.getPlayerNumber()).getCards(1).add(players.get(inputPacket.getPlayerNumber()).getCards(0).get(1));
            	players.get(inputPacket.getPlayerNumber()).getCards(0).remove(1);
            	//draw new cards and update displayed cards
            	for(int i = 0; i < 2; i++) {
	            	PlayingCard tempCard = deck.draw();
	            	players.get(inputPacket.getPlayerNumber()).getCards(i).add(tempCard);
	            	//clear and re-add cards to display
	            	playerPanels.get(inputPacket.getPlayerNumber()).removeAllCards(i);
	            	for(int j = 0; j < 2; j++) {
		            	playerPanels.get(inputPacket.getPlayerNumber()).addCard(i, players.get(inputPacket.getPlayerNumber()).getCards(i).get(j));
	            	}
            	}
				revalidate();
            	//copy bet over
            	players.get(inputPacket.getPlayerNumber()).setBet(1, players.get(inputPacket.getPlayerNumber()).getBet(0));
            	playerPanels.get(inputPacket.getPlayerNumber()).setDisplayedBet(1, players.get(inputPacket.getPlayerNumber()).getBet(1));
            	//send an update to players
				BlackjackGameStatePacket tempPacket = new BlackjackGameStatePacket(players, turnCounter, handCounter, false);
				for(SocketConnection c : connections) {
					c.sendPacket(tempPacket);
				}
			}
			else if(inputPacket.getInput() == BlackjackButtonInput.DOUBLEDOWN) {
				System.out.println("doubledown input packet from player " + inputPacket.getPlayerNumber());
            	//double the bet
            	players.get(inputPacket.getPlayerNumber()).setBet(inputPacket.getHandNumber(), players.get(inputPacket.getPlayerNumber()).getBet(inputPacket.getHandNumber()) * 2);
            	playerPanels.get(inputPacket.getPlayerNumber()).setDisplayedBet(inputPacket.getHandNumber(), players.get(inputPacket.getPlayerNumber()).getBet(inputPacket.getHandNumber()));
            	//draw one card face down
            	PlayingCard tempCard = deck.draw();
            	tempCard.setFaceUp(false);
    			players.get(inputPacket.getPlayerNumber()).getCards(inputPacket.getHandNumber()).add(tempCard);
    			playerPanels.get(inputPacket.getPlayerNumber()).addCard(inputPacket.getHandNumber(), tempCard);
				revalidate();
    			startNewTurn();
			}
		}
		else if(object instanceof OnlinePlayerInitializationPacket) {
			OnlinePlayerInitializationPacket packet = (OnlinePlayerInitializationPacket) object;
			onlinePlayerNumber = packet.getPlayerNumber();
			playerPanels.get(onlinePlayerNumber).setColors(BlackjackPlayerType.LOCAL);
			JOptionPane.showConfirmDialog(this,
				"You are player number: " + onlinePlayerNumber + ".\nPlease wait until the host starts the round.", 
				"Connected", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			revalidate();
		}
		else if(object instanceof BlackjackRoundResultsPacket) {
			BlackjackRoundResultsPacket resultsPacket = (BlackjackRoundResultsPacket) object;
			turnCounter = 0;
			handCounter = 0;
			//update dealer ui
			playerPanels.get(0).setDisplayedPoints(resultsPacket.getPlayers().get(0).getPoints());
			playerPanels.get(0).removeAllCards(0);
			for(int i = 0; i < resultsPacket.getPlayers().get(0).getCards(0).size(); i++) {
				playerPanels.get(0).addCard(0, resultsPacket.getPlayers().get(0).getCards(0).get(i));
			}
			playerPanels.get(0).setTurnColor(true, 0);
			//update player ui
			for(int i = 1; i < 5; i++) {
				//set player colors (local and remote swapped for this specific remote player)
				if(resultsPacket.getPlayers().get(i).getPlayerType() == BlackjackPlayerType.REMOTE && i == onlinePlayerNumber)
					playerPanels.get(i).setColors(BlackjackPlayerType.LOCAL);
				else if(resultsPacket.getPlayers().get(i).getPlayerType() == BlackjackPlayerType.LOCAL)
					playerPanels.get(i).setColors(BlackjackPlayerType.REMOTE);
				else
					playerPanels.get(i).setColors(resultsPacket.getPlayers().get(i).getPlayerType());
				//update bets, points, and turn color
				playerPanels.get(i).setDisplayedPoints(resultsPacket.getPlayers().get(i).getPoints());
				for(int j = 0; j < 2; j++) {
					playerPanels.get(i).setDisplayedBet(j, resultsPacket.getPlayers().get(i).getBet(j));
				}
				playerPanels.get(i).setTurnColor(false, 0);
			}
			//update displayed cards
			refreshDisplayedCards(resultsPacket.getPlayers());
			revalidate();
			repaint();
			//highlight bets for win or loss
			for(int i = 0; i < 5; i++) {
				for(int j = 0; j < 2; j++) {
					if(resultsPacket.getResult(i, j) > 0)
						playerPanels.get(i).updateRoundEndHandDisplay(j, true, false);
					else if(resultsPacket.getResult(i, j) < 0)
						playerPanels.get(i).updateRoundEndHandDisplay(j, false, false);
					else
						playerPanels.get(i).updateRoundEndHandDisplay(j, false, true);
				}
			}
		}
		else if(object instanceof ConnectionTerminationPacket) {
			ConnectionTerminationPacket closePacket = (ConnectionTerminationPacket) object;
			System.out.println("connections at time packet received: " + connections.size());
			//if host received the disconnect packet
			if(isHost == true && closePacket.getPlayerNumber() > 0) {
				//close the connection and adjust connection numbers of other players
				int closingConnection = players.get(closePacket.getPlayerNumber()).getConnectionNumber();
				for(int i = 0; i < 5; i++){
					if(players.get(i).getPlayerType() == BlackjackPlayerType.REMOTE && 
							players.get(i).getConnectionNumber() > players.get(closePacket.getPlayerNumber()).getConnectionNumber()) {
						players.get(i).setConnectionNumber(players.get(i).getConnectionNumber() - 1);
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
				players.get(closePacket.getPlayerNumber()).setPlayerType(BlackjackPlayerType.COMPUTER);
				playerPanels.get(closePacket.getPlayerNumber()).setColors(BlackjackPlayerType.COMPUTER);
				revalidate();
				//if it was the disconnected player's turn when they disconnected, then restart that player's turn under the computer's control
				if(closePacket.getPlayerNumber() == turnCounter) {
					handCounter--;
					if(handCounter < 0) {
						turnCounter--;
						handCounter = 1;
					}
					startNewTurn();
				}
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
				for(int i = 0; i < 5; i++) {
					playerPanels.get(i).clearForNewRound();
				}
				isSinglePlayerGame = true;
				revalidate();
				buttonHit.setEnabled(false);
				buttonStay.setEnabled(false);
			}
			System.out.println("single player: " + isSinglePlayerGame);
		}
	}

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
	public class BlackjackPlayerPanel extends JPanel{
		
		private static final long serialVersionUID = -5341108043993052429L;
		
		private Font font_Ariel16B = new Font("Ariel", Font.BOLD, 16);
		private Font font_Ariel16P = new Font("Ariel", Font.PLAIN, 16);
		
		private int cardDisplayHeight;
		private int cardDisplayWidth;
		
		private JLabel labelName = new JLabel();
		private JLabel labelPoints = new JLabel();
		private JLabel[] betLabels = {new JLabel(), new JLabel()};
		private JPanel[] cardPanels = {new JPanel(), new JPanel()};
		private JScrollPane[] scrollPanes = {new JScrollPane(cardPanels[0]), new JScrollPane(cardPanels[1])};
		private List<ArrayList<JLabel>> cardLabelList = new ArrayList<ArrayList<JLabel>>();
		
		public BlackjackPlayerPanel(String name) {
			this.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			//initial default card dimensions
			cardDisplayHeight = 60;
			cardDisplayWidth = 60 * 20 / 29;
			
			cardLabelList.add(new ArrayList<JLabel>());
			cardLabelList.add(new ArrayList<JLabel>());
			
			for(int i = 0 ; i < 2; i++) {
				betLabels[i].setFont(font_Ariel16P);
				betLabels[i].setText("Bet: 0");
				betLabels[i].setOpaque(true);
				scrollPanes[i].setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPanes[i].setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			}
			
			scrollPanes[0].setPreferredSize(new Dimension(this.getHeight() * 1 / 3, this.getWidth() * 3 / 4));
			scrollPanes[1].setPreferredSize(new Dimension(this.getHeight() * 1 / 3, this.getWidth() * 3 / 4));
			
			cardPanels[0].setBackground(Color.WHITE);
			cardPanels[0].setOpaque(true);
			cardPanels[1].setBackground(Color.WHITE);
			cardPanels[1].setOpaque(true);
			
			labelName.setFont(font_Ariel16B);
			labelName.setText(name);
			labelName.setOpaque(true);

			labelPoints.setFont(font_Ariel16P);
			labelPoints.setText("Points: 0");

			//add ui components to layout
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy = 0;
			add(labelName, gbc);

			gbc.anchor = GridBagConstraints.CENTER;
			gbc.weightx = 3;
			gbc.gridwidth = 1;
			gbc.gridx = 1;
			gbc.gridy = 0;
			add(labelPoints, gbc);

			gbc.gridwidth = 1;
			gbc.weightx = 1;
			gbc.weighty = 3;
			gbc.gridx = 0;
			gbc.gridy = 1;
			add(betLabels[0], gbc);
			
			if(name != "Dealer") {
				gbc.gridx = 0;
				gbc.gridy = 2;
				add(betLabels[1], gbc);
			}
			
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridwidth = 1;
			gbc.weightx = 3;
			gbc.gridx = 1;
			gbc.gridy = 1;
			add(scrollPanes[0], gbc);
			
			if(name != "Dealer") {
				gbc.gridx = 1;
				gbc.gridy = 2;
				add(scrollPanes[1], gbc);
			}
		}
		//setters for the points and bet label's text
		public void setDisplayedPoints(int points) {
			labelPoints.setText("Points: " + points);
		}
		public void setDisplayedBet(int betIndex, int bet) {
			betLabels[betIndex].setText("Bet: " + bet);
		}
		//adds a label with an image of the given card to the player cards display
		public void addCard(int handNumber, PlayingCard card) {
			try {
				scrollPanes[0].setPreferredSize(new Dimension(this.getHeight() * 1 / 3, this.getWidth() * 3 / 4));
				scrollPanes[1].setPreferredSize(new Dimension(this.getHeight() * 1 / 3, this.getWidth() * 3 / 4));
				cardDisplayHeight = scrollPanes[0].getSize().height - 10;
				cardDisplayWidth = (scrollPanes[0].getSize().height - 10) * 20 / 29;
				JLabel tempLabel = new JLabel();
				//get image from resources folder
				BufferedImage img = ImageIO.read(this.getClass().getResource(card.getCardResourcePath(card.isFaceUp())));
				//img = ImageIO.read(new File(getClass().getResource(card.getCardResourcePath(card.isFaceUp())).getPath()));
			    Image scaledImage = img.getScaledInstance(cardDisplayWidth, cardDisplayHeight, Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(scaledImage);
				tempLabel.setIcon(icon);
				cardLabelList.get(handNumber).add(tempLabel);
				cardPanels[handNumber].add(tempLabel);
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}
		//removes all displayed cards for the specified hand
		public void removeAllCards(int handNumber) {
			cardLabelList.get(handNumber).clear();
			cardPanels[handNumber].removeAll();
		}
		//resets player panel bet and displayed cards for a new round
		public void clearForNewRound() {
			for(int i = 0; i < 2; i++) {
				cardPanels[i].removeAll();
				cardLabelList.get(i).clear();
				betLabels[i].setText("Bet: ");
			}
		}
		//sets bet background color for the bet label
		public void setBetColor(int handNumber, Color color) {
			betLabels[handNumber].setBackground(color);
		}
		//sets colors of text for the labels to indicate the type of player
		public void setColors(BlackjackPlayerType playerType) {
			if(playerType == BlackjackPlayerType.CLOSED)
				labelName.setForeground(Color.LIGHT_GRAY);
			else if(playerType == BlackjackPlayerType.LOCAL)
				labelName.setForeground(Color.BLUE);
			else if(playerType == BlackjackPlayerType.COMPUTER)
				labelName.setForeground(Color.BLACK);
			else if(playerType == BlackjackPlayerType.REMOTE)
				labelName.setForeground(Color.RED);
		}
		//used to highlight player name and hand yellow when it is their turn and white when it is not their turn
		public void setTurnColor(boolean isMyTurn, int handNumber) {
			if(isMyTurn == true) {
				labelName.setBackground(Color.YELLOW);
				if(handNumber == 0) {
					cardPanels[0].setBackground(Color.YELLOW);
					cardPanels[1].setBackground(Color.WHITE);
				}
				else if(handNumber == 1) {
					cardPanels[0].setBackground(Color.WHITE);
					cardPanels[1].setBackground(Color.YELLOW);
				}
			}
			else {
				labelName.setBackground(Color.WHITE);
				cardPanels[0].setBackground(Color.WHITE);
				cardPanels[1].setBackground(Color.WHITE);
			}
		}
		//used to highlight the bets with different colors based upon whether the corresponding hand won, lost, or tied
		public void updateRoundEndHandDisplay(int handNumber, boolean victory, boolean tie) {
			if(tie != true) {
				if(victory == true)
					betLabels[handNumber].setBackground(Color.GREEN);
				else
					betLabels[handNumber].setBackground(Color.PINK);
			}
			else
				betLabels[handNumber].setBackground(Color.WHITE);
		}
	}

}
