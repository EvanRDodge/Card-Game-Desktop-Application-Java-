package model;

import java.io.Serializable;
//this class contains reply data for a request for bets
public class BlackjackBetPacket implements Serializable{

	private static final long serialVersionUID = -5877364506714672885L;

	private int playerNumber;
	private int handNumber;
	private int bet;

	public BlackjackBetPacket(int playerNumber, int handNumber, int bet) {
		this.playerNumber = playerNumber;
		this.handNumber = handNumber;
		this.bet = bet;
	}
	//getter functions
	public int getPlayerNumber() {
		return playerNumber;
	}
	public int getHandNumber() {
		return handNumber;
	}
	public int getBet() {
		return bet;
	}
}
