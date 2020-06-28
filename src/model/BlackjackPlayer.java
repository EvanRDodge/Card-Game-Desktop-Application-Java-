package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import controller.BlackjackGamePanel.BlackjackPlayerType;
//this class models a blackjack player
public class BlackjackPlayer implements Serializable{

	private static final long serialVersionUID = 5948025705352675558L;
	
	private int playerNumber;
	private int points;
	private int[] bets = {0, 0};
	private BlackjackPlayerType playerType;
	private List<ArrayList<PlayingCard>> hands = new ArrayList<ArrayList<PlayingCard>>();
	private int connectionNumber;
	
	public BlackjackPlayer(int playerNumber, int points, BlackjackPlayerType playerType) {
		this.playerNumber = playerNumber;
		this.points = points;
		this.playerType = playerType;
		hands.add(new ArrayList<PlayingCard>());
		hands.add(new ArrayList<PlayingCard>());
		this.connectionNumber = -1;
	}

	//setters and getters
	public int getPlayerNumber() {
		return playerNumber;
	}
	public int getPoints() {
		return points;
	}
	public int getBet(int handNumber) {
		return bets[handNumber];
	}
	public ArrayList<PlayingCard> getCards(int handNumber){
		return hands.get(handNumber);
	}
	public BlackjackPlayerType getPlayerType() {
		return playerType;
	}
	public int getConnectionNumber() {
		return connectionNumber;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public void setBet(int handNumber, int bet) {
		this.bets[handNumber] = bet;
	}
	public void setPlayerType(BlackjackPlayerType playerType) {
		this.playerType = playerType;
	}
	public void setConnectionNumber(int connectionNumber) {
		this.connectionNumber = connectionNumber;
	}
	//calculates the value of the player's hand according to the rules of blackjack
	public int getHandValue(int handNumber) {
		int totalValue = 0;
		for(PlayingCard temp : hands.get(handNumber)) {
			if(temp.getValue() == PlayingCardValue.TWO)
				totalValue += 2;
			else if(temp.getValue() == PlayingCardValue.THREE)
				totalValue += 3;
			else if(temp.getValue() == PlayingCardValue.FOUR)
				totalValue += 4;
			else if(temp.getValue() == PlayingCardValue.FIVE)
				totalValue += 5;
			else if(temp.getValue() == PlayingCardValue.SIX)
				totalValue += 6;
			else if(temp.getValue() == PlayingCardValue.SEVEN)
				totalValue += 7;
			else if(temp.getValue() == PlayingCardValue.EIGHT)
				totalValue += 8;
			else if(temp.getValue() == PlayingCardValue.NINE)
				totalValue += 9;
			else if(temp.getValue() == PlayingCardValue.TEN || 
					temp.getValue() == PlayingCardValue.JACK || 
					temp.getValue() == PlayingCardValue.QUEEN ||
					temp.getValue() == PlayingCardValue.KING)
				totalValue += 10;
			else if(temp.getValue() == PlayingCardValue.ACE) {
				totalValue += 11;
			}
		}
		int index = 0;
		while(totalValue > 21 && index < hands.get(handNumber).size()) {
			if(hands.get(handNumber).get(index).getValue() == PlayingCardValue.ACE) {
				totalValue -= 10;
			}
			index++;
		}
		return totalValue;
	}
	//empties the player's hands of cards
	public void clearHands() {
		for(int i = 0; i < hands.size(); i++) {
			hands.get(i).clear();
		}
	}
}
