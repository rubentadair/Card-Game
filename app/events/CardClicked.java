package events;


import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Board;
import structures.Game;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.spells.EnemySpell;
import structures.spells.FriendlySpell;
import structures.spells.Spell;
import structures.units.Avatar;
import utils.SpellHandler;

/**
 * Indicates that the user has clicked an object on the game canvas, in this case a card.
 * The event returns the position in the player's hand the card resides within.
 * 
 * { 
 *   messageType = “cardClicked”
 *   position = <hand index position [1-6]>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class CardClicked implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		
		int handPosition = message.get("position").asInt();
		//the new method for dealing with card clicked
		Game.selectCard(out, gameState, handPosition);
		System.out.println("CardClicked : inside CardClicked");

		// get current card clicked
		Card clickedCard = GameState.player1.getPlayerHandCard(handPosition);

		if (clickedCard.getIsCreature()) {// replace with check to see if card is creature
			// Highlight potential summoning tiles
			
			System.out.println("CardClicked : inside if - clickedCard.getIsCreature()");
			Avatar player1Avatar = (Avatar) gameState.player1.getAvatar();
			player1Avatar.highlightAdjacentTiles(out, Game.getBoard().getTiles(), gameState);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		//handles clicking a spell card
		if (!clickedCard.getIsCreature()) {
			System.out.println("CardClicked : inside if - !clickedCard.getIsCreature()");
			Spell spell = SpellHandler.returnSpell(clickedCard.getCardname());
			gameState.cardSelected = true;
			gameState.isCardSelectedSpell = true;
			gameState.currentSpell = spell;
			SpellHandler.HighlightTilesSpell(spell, out, gameState);
			
		}

		
	}

}
