package model;

import java.io.Serializable;
import java.util.ArrayList;
//this class is used to store data, describing the results of a round of blackjack, to be sent to remote players
public class BlackjackRoundResultsPacket implements Serializable{

	private static final long serialVersionUID = 4703627594126170837L;
	
	private ArrayList<BlackjackPlayer> players;
	private int[][] results;
	
	public BlackjackRoundResultsPacket(ArrayList<BlackjackPlayer> players, int[][] results) {
		this.players = players;
		this.results = results;
	}
	//getter functions
	public ArrayList<BlackjackPlayer> getPlayers(){
		return players;
	}
	public int getResult(int playerNumber, int handNumber){
		return results[playerNumber][handNumber];
	}
}
