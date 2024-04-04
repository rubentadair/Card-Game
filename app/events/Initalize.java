package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import demo.CommandDemo;
import demo.Loaders_2024_Check;
import structures.Game;
import structures.GameState;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.OrderedCardLoader;

import structures.basic.Player;


/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * {
 * messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Initalize implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		gameState.gameInitalised = true;
		gameState.something = true;	
		gameState.turn = 1; 
		Game.createBoard(out);
		
		//to add correct coordinates when identified 
		Unit[] avatars = Game.avatarSummonSetup(out, 1, 2, 7, 2);
		
		//This is required for the mana system code to run successfully 
		Player player1 = new Player(avatars[0]);
		gameState.player1 = player1;
		gameState.player2 = new Player(avatars[1]);
		gameState.currentPlayer = player1;
		
		BasicCommands.setPlayer1Health(out, GameState.player1);
		BasicCommands.setPlayer2Health(out, GameState.player2);
		
		Game.initialisePlayerDeck(out, gameState);
		Game.setManaOnStartTurn(out, gameState);

	
		// User 1 makes a change
		// CommandDemo.executeDemo(out); // this executes the command demo, comment out
		// this when implementing your solution
		// Loaders_2024_Check.test(out);

		// replace this with the initialisation method from the game class

	}

}
