package events;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.AILogic;

import java.util.*;

/**
 * Indicates that the user has clicked an object on the game canvas, in this
 * case
 * the end-turn button.
 * 
 * {
 * messageType = “endTurnClicked”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class EndTurnClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		if(gameState.gameOver == false) {			
			System.out.println("End Turned : inside");
			Game.beginNewTurn(out, gameState);
		}


	
	
	}
}
