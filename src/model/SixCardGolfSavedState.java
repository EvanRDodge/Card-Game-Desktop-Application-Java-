package model;

import java.io.Serializable;
import controller.SixCardGolfGamePanel.SixCardGolfPlayerType;
//this class stores the necessary data to load a game of six card golf at the beginning of the round at which the progress was saved
public class SixCardGolfSavedState implements Serializable {

	private static final long serialVersionUID = -1324092295221452961L;
	
	private int[][] playerPoints;
	private SixCardGolfPlayerType[] playerTypes;
	private int gameLength;
	private int roundNumber;
	private int goesFirstNumber;
	
	public SixCardGolfSavedState(int[][] playerPoints, SixCardGolfPlayerType[] playerTypes, int gameLength, int roundNumber, int goesFirstNumber) {
		this.playerPoints = playerPoints;
		this.playerTypes = playerTypes;
		this.gameLength = gameLength;
		this.roundNumber = roundNumber;
		this.goesFirstNumber = goesFirstNumber;
	}
	//getter functions
	public int[][] getPlayerPoints() {
		return playerPoints;
	}
	public SixCardGolfPlayerType[] getPlayerTypes() {
		return playerTypes;
	}
	public int getGameLength() {
		return gameLength;
	}
	public int getRoundNumber() {
		return roundNumber;
	}
	public int getGoesFirstNumber() {
		return goesFirstNumber;
	}
}
