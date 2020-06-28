package model;

import java.io.Serializable;
import java.util.ArrayList;
//this class contains the data required to convey the state of the game to a remote player
public class BlackjackGameStatePacket implements Serializable{

	private static final long serialVersionUID = 4703627594126170837L;
	
	private ArrayList<BlackjackPlayer> players;
	private int turnNumber;
	private int handNumber;
	private boolean isRequestingBet;
	
	public BlackjackGameStatePacket(ArrayList<BlackjackPlayer> players, int turnNumber, int handNumber, boolean isRequestingBet) {
		this.players = players;
		this.turnNumber = turnNumber;
		this.handNumber = handNumber;
		this.isRequestingBet = isRequestingBet;
	}
	//getter functions
	public ArrayList<BlackjackPlayer> getPlayers() {
		return players;
	}
	public int getTurnNumber() {
		return turnNumber;
	}
	public int getHandNumber() {
		return handNumber;
	}
	public boolean getIsRequestingBet() {
		return isRequestingBet;
	}
}
