package model;

import java.util.ArrayList;
import java.util.Random;
//this class models a deck of standard playing cards
public class PlayingCardDeck {
	private ArrayList<PlayingCard> deck;
	private int startNumberStandardDecks;
	private int startNumberJokers;
	
	//constructor that builds one or a combination of multiple standard decks of playing cards with a certain number of jokers
	public PlayingCardDeck(int startNumberStandardDecks, int startNumberJokers) {
		this.startNumberStandardDecks = startNumberStandardDecks;
		this.startNumberJokers = startNumberJokers;		
		deck = new ArrayList<PlayingCard>();
		rebuildDeck();
	}
	//clears and fills the deck with cards consisting of the number of standard decks and jokers specified in the constructor
	public void rebuildDeck() {
		deck.clear();
		//add cards to deck
		if(startNumberStandardDecks > 0) {
			for(int i = 0; i < startNumberStandardDecks; i++) {
				for(int k = 0; k < 4; k++) {
					PlayingCardSuit tempSuit = PlayingCardSuit.values()[k + 1];
					deck.add(new PlayingCard(PlayingCardValue.TWO, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.THREE, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.FOUR, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.FIVE, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.SIX, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.SEVEN, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.EIGHT, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.NINE, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.TEN, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.JACK, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.QUEEN, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.KING, tempSuit));
					deck.add(new PlayingCard(PlayingCardValue.ACE, tempSuit));
				}
			}
		}
		for(int i = 0; i < startNumberJokers; i++) {
			deck.add(new PlayingCard(PlayingCardValue.JOKER, PlayingCardSuit.NONE));
		}
	}	
	//randomizes the order of the cards in the deck
	public void shuffle() {
		Random rn = new Random();
		for(int i = 0; i < deck.size(); i++) {
			int j = rn.nextInt(deck.size());
			PlayingCard tempCard = deck.get(j);
			deck.set(j, deck.get(i));
			deck.set(i, tempCard);
		}
	}
	//adds cards to deck from an arraylist
	public void addCardPile(ArrayList<PlayingCard> cards) {
		for(int i = 0; i < cards.size(); i++) {
			deck.add(cards.get(i));
		}
	}

	public void removeCardAt(int index) {
		if(deck.size() > index) {
			deck.remove(index);
		}
	}

	public PlayingCard getCardAt(int index) {
		return deck.get(index);
	}
	//draws a card from the deck by returning the top card and removing it from the deck
	public PlayingCard draw() {
		PlayingCard tempCard = deck.get(0);
		deck.remove(0);
		return tempCard;
	}
	//returns the number of cards currently in the deck
	public int getSize() {
		return deck.size();
	}
	//returns the number of cards in the deck when it is full
	public int getFullDeckSize() {
		return startNumberJokers + startNumberStandardDecks * 52;
	}
	//returns the number of standard decks used in this deck
	public int getNumberStandardDecks() {
		return startNumberStandardDecks;
	}
	//returns true if the deck is empty
	public boolean isEmpty() {
		if(deck.size() > 0)
			return false;
		else
			return true;
	}
	//empties the deck
	public void clearDeck() {
		deck.clear();
	}
	//prints the contents of the deck to console
	public void printDeck() {
		for(int i = 0; i < deck.size(); i++) {
			System.out.println(deck.get(i).getValue() + " of " + deck.get(i).getSuit());
		}
	}
}
