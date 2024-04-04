package structures.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import utils.OrderedCardLoader;

/**
 * A basic representation of of the Player. A player
 * has health and mana.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Player {

	int health;
	int mana;
	Unit avatar;


	private Card[] playerHand = new Card[6];

	private List<Card> playerDeck = new ArrayList<Card>(20);

	public Player(Unit avatar) {
		super();
		this.health = 20;
		this.mana = 0;
		this.avatar = avatar;
	}

	public Player(int health, int mana) {
		super();
		this.health = health;
		this.mana = mana;
	}



	// function for drawing cards
	public void drawCard(ActorRef out, int cardsToDraw) {
		System.out.println("Player - inside drawCard");
		if (cardsToDraw <= 0) {
			removeCardFromDeck(0);
			return;
		}
		for (int i = 0; i < cardsToDraw; i++) {
			Card card = playerDeck.get(0);
			int freeHandPosition = identifyFreeHandPosition();
			if (freeHandPosition != -1) {
				System.out.println("Player - inside if");
				System.out.println("Player - card name "+i+ " "+card.getCardname());
				BasicCommands.drawCard(out, card, freeHandPosition, 0);
				setPlayerHandCard(freeHandPosition, card);
				removeCardFromDeck(0);
			}
		}
	}
	
	//This method will identify the free hand position if there is any space
	//If there isnt any space then it will return -1 
	public int identifyFreeHandPosition() {
		
		System.out.println("Player - inside identifyFreeHandPosition");

		for (int i = 0; i < playerHand.length; i++) {
			if (playerHand[i] instanceof Card) {
				continue;
			}
			else {
				return i;
			}
		}
		return -1;
	}
	
	
	public void drawAICard(ActorRef out, int cardsToDraw) {
		if (cardsToDraw <= 0) {
			removeCardFromDeck(0);
			return;
		}
		
		for (int i = 0; i < cardsToDraw; i++) {
			Card card = playerDeck.get(0);
			int freeHandPosition = identifyFreeHandPosition();
			setPlayerHandCard(freeHandPosition, card);
			System.out.println("AI card : @@@"+playerHand[freeHandPosition].cardname+ " "+i);
			removeCardFromDeck(0);
		}
	}

	public void drawInitialHand(ActorRef out) {
		System.out.println("Player - inside drawInitialHand");
		if (!this.playerDeck.isEmpty()) {
			this.drawCard(out, 3);
		}
	}
	
	//could be added to the AI child class extending Player
	public void drawInitialHandAI(ActorRef out) {
		System.out.println("Player - inside drawInitialHandAI");
		if (!this.playerDeck.isEmpty()) {
			this.drawAICard(out, 3);
		}
	}

	public void drawCardAtTurnEnd(ActorRef out, GameState gameState) {
		if (this.playerDeck.isEmpty()) {
			System.out.println("Card Deck Empty. Deck Reshuffled!");
			BasicCommands.addPlayer1Notification(out, "Card Deck Empty. Deck Reshfulled", 5);
			if (gameState.currentPlayer == gameState.player1) {
				setPlayerDeck(OrderedCardLoader.getPlayer1Cards(2));
			}
			else {
				setPlayerDeck(OrderedCardLoader.getPlayer2Cards(2));
			}
			shufflePlayerCards();
		}
		//if there are no free spaces in the hand then it will return -1
		//this means there is no need to draw a card and that top card will be discarded instead
		int freeHandPos = identifyFreeHandPosition();
		if (freeHandPos == -1) {
		}
		else {
				if (gameState.currentPlayer == gameState.player1) {
					drawCard(out, 1);
					return;
				}
				else {
					drawAICard(out, 1);
					return;
				}
				
			}
	
		removeCardFromDeck(0);
		
	}
	
	//shuffles the deck when drawing a new deck
	public void shufflePlayerCards() {
		ArrayList<Card> cardsToShuffle = (ArrayList<Card>) this.getPlayerDeck();
		Collections.shuffle(cardsToShuffle);
		this.setPlayerDeck(cardsToShuffle);
		
	}

	public void removeCardFromDeck(int deckPosition) {
		this.playerDeck.remove(deckPosition);
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getMana() {
		return mana;
	}

	public void setMana(int mana) {
		this.mana = mana;
	}

	public Card getPlayerHandCard(int handPosition) {
		// handPosition returning -1 has been crashing the game
		System.out.println(handPosition);
		return this.playerHand[handPosition];
	}

	public void setPlayerHandCard(int handPosition, Card card) {
		this.playerHand[handPosition] = card;
	}
	
	public Card[] getPlayerHand(){
		return this.playerHand;
	}

	public List<Card> getPlayerDeck() {
		return this.playerDeck;
	}

	public void setPlayerDeck(List<Card> list) {
		this.playerDeck = list;
	}

	public Unit getAvatar() {
		return this.avatar;
	}
	
	public void removeCardFromHand(int handPosition) {
		// test bandaid
		playerHand[handPosition] = null;
	}
	
	public void removeCardFromHand(String cardName) {
		// test bandaid
		
		System.out.println("attempting to remove: " + cardName);
		for (int i = 0; i < playerHand.length; i++) {
			if (playerHand[i] != null) {
				System.out.println("current card name: " + playerHand[i].getCardname());
				if (playerHand[i].getCardname().equals(cardName)) {
					playerHand[i] = null;
					System.out.println("removing card from hand");
				}
			}
		}
	}
	

}
