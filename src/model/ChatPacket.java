package model;

import java.io.Serializable;
//this class models a message to be sent using the chat window
public class ChatPacket implements Serializable{

	private static final long serialVersionUID = -1950872707057888846L;
	
	private String message;

	public ChatPacket(String message) {
		this.message = message;
	}
	//getter function
	public String getMessage() {
		return message;
	}
}
