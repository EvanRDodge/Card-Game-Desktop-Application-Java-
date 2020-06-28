package model;

import java.io.Serializable;
//this class is used to inform other connected players that the connection is being terminated
public class ConnectionTerminationPacket implements Serializable{

	private static final long serialVersionUID = -3112425802380111073L;

	private int playerNumber;
	
	public ConnectionTerminationPacket(int playerNumber) {
		this.playerNumber = playerNumber;
	}
	//getter function
	public int getPlayerNumber() {
		return playerNumber;
	}
}
