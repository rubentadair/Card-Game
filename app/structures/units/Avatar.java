package structures.units;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.spells.HornOfTheForsaken;
import structures.spells.Spell;

public class Avatar extends Unit{
	
	public Avatar() {
		super();
		hasMoved = false;
		hasAttacked = false;
	}
	
	//attribute for storing the horn of the forsaken to utilise its methods
	private HornOfTheForsaken hornOfTheForsaken = null;
	
    // Method to highlight adjacent tiles
	public void highlightAdjacentTiles(ActorRef out, Tile[][] grid, GameState gameState) {
		int X = this.getPosition().getTilex();
		int Y = this.getPosition().getTiley();
		
	    for (int x = Math.max(0, X - 1); x <= Math.min(X + 1, grid.length - 1); x++) {
	        for (int y = Math.max(0, Y - 1); y <= Math.min(Y + 1, grid[0].length - 1); y++) {
	            if (x != X || y != Y) { 
	            	// highlight tile if empty
	            	if (!Game.getBoard().getTile(x, y).hasUnit()) {
	            		if(gameState.currentPlayer == gameState.player1	) {
	            			BasicCommands.drawTile(out, grid[x][y], 1); 
	            		}
					grid[x][y].setIsActionableTile(true);
					try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
	            	}
	            }
	        }
	    }
	}
	
    // Method to check if the Avatar has Horn of the Forsaken
	public boolean getHasHornOfForsaken() {
		if (this.hornOfTheForsaken == null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	
	public HornOfTheForsaken getHornOfTheForsaken() {
		return this.hornOfTheForsaken;
	}
	
	public void setHornOfTheForsaken(HornOfTheForsaken hornOfTheForsaken) {
		this.hornOfTheForsaken = (HornOfTheForsaken) hornOfTheForsaken;
	}

}
