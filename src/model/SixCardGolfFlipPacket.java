package model;

import java.io.Serializable;
//this class stores the response to a request for a player to flip over two of their starting cards
public class SixCardGolfFlipPacket implements Serializable{

	private static final long serialVersionUID = 2365025122989351829L;
	
	private int playerNumber;
	private int flipIndex1;
	private int flipIndex2;

	public SixCardGolfFlipPacket(int playerNumber, int flipIndex1, int flipIndex2) {
		this.playerNumber = playerNumber;
		this.flipIndex1 = flipIndex1;
		this.flipIndex2 = flipIndex2;
	}
	//getter functions
	public int getPlayerNumber() {
		return playerNumber;
	}
	public int getFlip1() {
		return flipIndex1;
	}
	public int getFlip2() {
		return flipIndex2;
	}
}
