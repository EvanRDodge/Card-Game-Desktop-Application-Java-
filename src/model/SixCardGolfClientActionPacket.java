package model;

import java.io.Serializable;
//this class contains data describing a remote player's action in six card golf
public class SixCardGolfClientActionPacket implements Serializable{
	
	private static final long serialVersionUID = -3941032596148695749L;

	public enum SixCardGolfButtonInput {DRAWDECK, DRAWDISCARD, SWAPDISCARD, SWAPCARDS};
	
	private int playerNumber;
	private SixCardGolfButtonInput input;
	private int cardSlot;

	public SixCardGolfClientActionPacket(int playerNumber, SixCardGolfButtonInput input, int cardSlot) {
		this.input = input;
		this.playerNumber = playerNumber;
		this.cardSlot = cardSlot;
	}
	//getter functions
	public SixCardGolfButtonInput getInput() {
		return input;
	}
	public int getPlayerNumber() {
		return playerNumber;
	}
	public int getCardSlot() {
		return cardSlot;
	}
}
