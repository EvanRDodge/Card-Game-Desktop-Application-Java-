package model;

import java.io.Serializable;
//this class stores data describing a remote blackjack player's action during their turn
public class BlackjackClientActionPacket implements Serializable{

	private static final long serialVersionUID = 6105684084213082030L;
	
	public enum BlackjackButtonInput {HIT, STAY, SPLIT, DOUBLEDOWN};
	
	private int playerNumber;
	private int handNumber;
	private BlackjackButtonInput input;

	public BlackjackClientActionPacket(int playerNumber, int handNumber, BlackjackButtonInput input) {
		this.input = input;
		this.playerNumber = playerNumber;
		this.handNumber = handNumber;
	}
	//getter functions
	public BlackjackButtonInput getInput() {
		return input;
	}
	public int getPlayerNumber() {
		return playerNumber;
	}
	public int getHandNumber() {
		return handNumber;
	}
}
