package structures;

import commands.BasicCommands;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.spells.Spell;

/**
 * This class can be used to hold information about the on-going game.
 * Its created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {

	public boolean gameInitalised = false;

	public boolean something = false;

	// tracks if a player has a card current selected
	public boolean cardSelected = false;
	
	//checks if card selected is a spell
	public boolean isCardSelectedSpell = false;
	
	//checks what the spell is if the current card selected is a spell
	public Spell currentSpell = null;

	// tracks the unit on tile
	public static Tile unitCurrentTile = null;

	// tracks the turn of the game
	public static int turn = 0;

	// tracks the current tile the unit was on, before the movement
	public Unit currentSelectedUnit = null;

	// tracks which card is being highlighted - if -1 then no card is currently
	// highlighted
	public int currentCardSelected = -1;

	// keeping track of the state of the game statically here as required - maybe
	// move over to game class however this really can be static as its the only
	// instance of its kind
	public static Player player1;
	public static Player player2;

	// used for keeping track of if a tile has been selected and if so then which
	// one
	public Tile previousSelectedTile = null;
	public boolean isTileSelected = false;

	public static Player currentPlayer = player1;
	
	public static boolean gameOver = false;
	
}
