package model;

import java.io.Serializable;
import controller.BlackjackGamePanel.BlackjackPlayerType;
//this class stores the necessary data to load a game of blackjack at the beginning of the round at which the progress was saved
public class BlackjackSavedState implements Serializable {

	private static final long serialVersionUID = 930314229450935936L;
	
	private int[] playerPoints;
	private BlackjackPlayerType[] playerTypes;
	private int standardDeckCount;
	
	public BlackjackSavedState(int[] playerPoints, BlackjackPlayerType[] playerTypes, int standardDeckCount) {
		this.playerPoints = playerPoints;
		this.playerTypes = playerTypes;
		this.standardDeckCount = standardDeckCount;
	}
	//getter functions
	public int[] getPlayerPoints() {
		return playerPoints;
	}
	public BlackjackPlayerType[] getPlayerTypes() {
		return playerTypes;
	}
	public int getStandardDeckCount() {
		return standardDeckCount;
	}
}
