package model;

import java.io.Serializable;
//this class models a playing card from a standard deck of cards
public class PlayingCard implements Serializable {
	
	private static final long serialVersionUID = -702276098924548638L;
	
	boolean faceUp;
	private PlayingCardSuit suit;
	private PlayingCardValue value;
	//constructor
	public PlayingCard(PlayingCardValue value, PlayingCardSuit suit) {
		this.value = value;
		this.suit = suit;
		this.faceUp = true;
	}
	//alternate constructor
	public PlayingCard(PlayingCardValue value, PlayingCardSuit suit, boolean isFaceUp) {
		this.value = value;
		this.suit = suit;
		this.faceUp = isFaceUp;
	}
	//setters and getters
	public PlayingCardSuit getSuit() {
		return suit;
	}
	public void setSuit(PlayingCardSuit suit) {
		this.suit = suit;
	}
	public PlayingCardValue getValue() {
		return value;
	}
	public void setValue(PlayingCardValue value) {
		this.value = value;
	}
	public boolean isFaceUp() {
		return faceUp;
	}
	public void setFaceUp(boolean faceUp) {
		this.faceUp = faceUp;
	}
	//returns the expected path to the image resource used to visually represent the playing card
	public String getCardResourcePath(boolean isFaceUp) {
		String path = "/images/";
		if(isFaceUp == false) {
			path += "cardback_red.png";
			return path;
		}
		if(value == PlayingCardValue.TWO)
			path += "2";
		else if(value == PlayingCardValue.THREE)
			path += "3";
		else if(value == PlayingCardValue.FOUR)
			path += "4";
		else if(value == PlayingCardValue.FIVE)
			path += "5";
		else if(value == PlayingCardValue.SIX)
			path += "6";
		else if(value == PlayingCardValue.SEVEN)
			path += "7";
		else if(value == PlayingCardValue.EIGHT)
			path += "8";
		else if(value == PlayingCardValue.NINE)
			path += "9";
		else if(value == PlayingCardValue.TEN)
			path += "10";
		else if(value == PlayingCardValue.JACK)
			path += "jack";
		else if(value == PlayingCardValue.QUEEN)
			path += "queen";
		else if(value == PlayingCardValue.KING)
			path += "king";
		else if(value == PlayingCardValue.ACE)
			path += "ace";
		else {
			path += "joker.png";
			return path;
		}
		if(suit == PlayingCardSuit.CLUBS)
			path += "clubs.png";
		else if(suit == PlayingCardSuit.DIAMONDS)
			path += "diamonds.png";
		else if(suit == PlayingCardSuit.HEARTS)
			path += "hearts.png";
		else if(suit == PlayingCardSuit.SPADES)
			path += "spades.png";
		else {
			path += "joker.png";
		}
		return path;
	}
}
