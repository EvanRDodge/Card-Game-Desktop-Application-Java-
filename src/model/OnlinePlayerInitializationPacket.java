package model;

import java.io.Serializable;
//this class contains data needed to assign a remote player their player number for a game
public class OnlinePlayerInitializationPacket implements Serializable{

	private static final long serialVersionUID = 4703627594126170837L;
	
	private int playerNumber;
	
	public OnlinePlayerInitializationPacket(int playerNumber) {
		this.playerNumber = playerNumber;
	}
	//getter function
	public int getPlayerNumber() {
		return playerNumber;
	}
}
