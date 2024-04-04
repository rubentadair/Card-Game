package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Board;
import structures.Game;
import structures.GameState;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BattleHandler;
import utils.SpellHandler;


/**
 * Indicates that the user has clicked an object on the game canvas, in this
 * case a tile.
 * The event returns the x (horizontal) and y (vertical) indices of the tile
 * that was
 * clicked. Tile indices start at 1.
 *
 * {
 * messageType = “tileClicked”
 * tilex = <x index of the tile>
 * tiley = <y index of the tile>
 * }
 *
 * @author Dr. Richard McCreadie
 *
 */
public class TileClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();
		Tile clickedTile = Game.getBoard().getTile(tilex, tiley);
		Tile[][] tiles = Game.getBoard().getTiles();
		Unit selectedUnit = clickedTile.getUnit();
		Game.getBoard().unHighlightAllTiles(out);
		
		System.out.println(selectedUnit);


		if (gameState.something == true) {
			// do some logic
		}

		if (!gameState.gameOver) {
			

			Board board = Game.getBoard();
			Tile tileSelected = board.getTile(tilex, tiley);
			if (gameState.cardSelected) { // tile clicked after card is selected, summon that card unit on the tile
				
				
				//if current card selected is a spell card this happens
				if (gameState.isCardSelectedSpell) {
					SpellHandler.performSpell(gameState.currentSpell, tileSelected, out, gameState);
				}
				else {
					gameState.currentSelectedUnit = null;
					gameState.unitCurrentTile = null;
					
					gameState.isTileSelected = false;
					Game.summonUnit(out, gameState, tilex, tiley);
				}
				
			} 
			
			//checks for if unit has attack or move action available
			else if ( (tileSelected.hasUnit() && !gameState.isTileSelected && (!tileSelected.getUnit().getHasMoved() || !tileSelected.getUnit().getHasAttacked())
					&& Game.getBoard().getPlayer1Units().contains(tileSelected.getUnit()) && !tileSelected.getUnit().isStunned(out)) ) { // if the tile clicked has unit on it show
																				// valid moves
				

				BasicCommands.addPlayer1Notification(out, "Where can I move?", 5);
				gameState.currentSelectedUnit = tileSelected.getUnit();
				gameState.unitCurrentTile = tileSelected;
				
				// the selected unit is now in a position to either move or perform combat
				gameState.isTileSelected = true;
				Game.showValidMovement(out, tiles, clickedTile, 2, gameState);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return;
			} else if (gameState.currentSelectedUnit != null && selectedUnit == null) {// if a tile is clicked after a unit
																						// is clicked, move the
																						// unit to that tile (valid move not implemented, for now it
																						// just moves)
				
				
				System.out.println("TileClicked:  gameState.currentSelectedUnit != null && selectedUnit == null");
				
				if (tileSelected.getIsActionableTile()) {
					System.out.println("able to move");
					BasicCommands.addPlayer1Notification(out, "Move my unit!", 5);
					gameState.unitCurrentTile.setUnit(null); // remove unit reference from previous tile before moving to new
					// tile
					gameState.currentSelectedUnit.setPositionByTile(tileSelected);
					tileSelected.setUnit(gameState.currentSelectedUnit);
					BasicCommands.moveUnitToTile(out, gameState.currentSelectedUnit, tileSelected);
					gameState.currentSelectedUnit.setHasMoved(true);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Game.resetGameState(out, gameState);
				}

				// Reset conditions after performing a move - might not need
				gameState.currentSelectedUnit = null;
				gameState.unitCurrentTile = null;
				gameState.isTileSelected = false;
				board.resetAllTiles(out);
				return;
			}
			if (gameState.unitCurrentTile != tileSelected && gameState.isTileSelected
					&& gameState.unitCurrentTile != null && tileSelected.getIsActionableTile()) {
				System.out.println("performing combat");
				System.out.println(selectedUnit.getName());
				BasicCommands.addPlayer1Notification(out, "Let us go to war", 5);
				BattleHandler.attackUnit(out, gameState.currentSelectedUnit, tileSelected, gameState);

				return;

			} 
		}

	}

}
