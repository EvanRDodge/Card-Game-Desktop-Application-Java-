package model;

import java.io.Serializable;
//this class contains the data required to convey the state of the game to a remote player
public class SixCardGolfGameStatePacket implements Serializable{
	
	private static final long serialVersionUID = 4379266458064279748L;
	
	private SixCardGolfPlayer[] players;
	private int turnNumber;
	private int roundNumber;
	private PlayingCard topDiscard;
	private PlayingCard drawnCard;
	private boolean requestingFlip;
	private boolean roundOver;
	private boolean gameOver;
	
	public SixCardGolfGameStatePacket(SixCardGolfPlayer[] players, int turnNumber, int roundNumber, PlayingCard topDiscard, PlayingCard drawnCard, boolean requestingFlip, boolean roundOver, boolean gameOver) {
		this.players = players;
		this.turnNumber = turnNumber;
		this.roundNumber = roundNumber;
		this.topDiscard = topDiscard;
		this.drawnCard = drawnCard;
		this.requestingFlip = requestingFlip;
		this.roundOver = roundOver;
		this.gameOver = gameOver;
	}
	//getter functions
	public SixCardGolfPlayer[] getPlayers() {
		return players;
	}
	public int getTurnNumber() {
		return turnNumber;
	}
	public int getRoundNumber() {
		return roundNumber;
	}
	public PlayingCard getTopDiscard() {
		return topDiscard;
	}
	public PlayingCard getDrawnCard() {
		return drawnCard;
	}
	public boolean isRequestingFlip() {
		return requestingFlip;
	}
	public boolean isRoundOver() {
		return roundOver;
	}
	public boolean isGameOver() {
		return gameOver;
	}
}
