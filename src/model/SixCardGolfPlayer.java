package model;

import java.io.Serializable;
import controller.SixCardGolfGamePanel.SixCardGolfPlayerType;
//this class models a six card golf player
public class SixCardGolfPlayer implements Serializable{

	private static final long serialVersionUID = -2621921722857425872L;

	private int playerNumber;
	private int[] points;
	private SixCardGolfPlayerType playerType;
	private PlayingCard[] hand;
	private int connectionNumber;
	
	public SixCardGolfPlayer(int playerNumber, SixCardGolfPlayerType playerType) {
		this.playerNumber = playerNumber;
		this.points = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		this.playerType = playerType;
		this.hand = new PlayingCard[6];
		this.connectionNumber = -1;
	}
	//getter functions
	public int getPlayerNumber() {
		return playerNumber;
	}
	public SixCardGolfPlayerType getPlayerType() {
		return playerType;
	}
	public PlayingCard[] getHand() {
		return hand;
	}
	public int getConnectionNumber() {
		return connectionNumber;
	}
	public int getPoints(int roundNumber) {
		return points[roundNumber];
	}
	//setter functions
	public void setConnectionNumber(int connectionNumber) {
		this.connectionNumber = connectionNumber;
	}
	public void setPlayerType(SixCardGolfPlayerType playerType) {
		this.playerType = playerType;
	}
	public void setScore(int roundNumber, int newScore) {
		if(roundNumber < 18)
			points[roundNumber] = newScore;
	}
	//sums up the score up to the given round number
	public int getTotalScore(int gameLength) {
		int totalScore = 0;
		for(int i = 0; i < gameLength; i++) {
			totalScore += points[i];
		}
		return totalScore;
	}
	//calculates the value of the player's hand according to the rules of six card golf
	public int getHandValue() {
		int totalValue = 0;
		if(playerType != SixCardGolfPlayerType.CLOSED) {
			//check columns
			for(int i = 0; i < 3; i++) {
				if(hand[i].getValue() != hand[i + 3].getValue()) {
					for(int j = 0; j < 2; j++) {
						if(hand[i + (3 * j)].getValue() == PlayingCardValue.QUEEN || hand[i + (3 * j)].getValue() == PlayingCardValue.JACK || hand[i + (3 * j)].getValue() == PlayingCardValue.TEN)
							totalValue += 10;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.NINE)
							totalValue += 9;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.EIGHT)
							totalValue += 8;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.SEVEN)
							totalValue += 7;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.SIX)
							totalValue += 6;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.FIVE)
							totalValue += 5;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.FOUR)
							totalValue += 4;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.THREE)
							totalValue += 3;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.TWO)
							totalValue += 2;
						else if(hand[i + (3 * j)].getValue() == PlayingCardValue.ACE)
							totalValue += 1;
					}
				}
			}
			//check for jokers
			for(int i = 0 ; i < 6; i++) {
				if(hand[i].getValue() == PlayingCardValue.JOKER)
					totalValue -= 2;
			}
			return totalValue;
		}
		else
			return 0;
	}
	//removes all cards from the hand
	public void clearHand() {
		for(int i = 0; i < 6; i++) {
			hand[i] = null;
		}
	}
	//sets all round scores to zero
	public void clearPoints() {
		for(int i = 0; i < 18; i++) {
			points[i] = 0;
		}
	}
	//counts the number of face up cards
	public int FaceUpCardCount() {
		int faceUpCount = 0;
		for(int i = 0; i < 6; i++) {
			if(hand[i].isFaceUp())
				faceUpCount++;
		}
		return faceUpCount;
	}
}
