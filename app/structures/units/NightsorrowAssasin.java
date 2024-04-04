package structures.units;

import java.util.List;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import structures.Board;
import structures.Game;
import structures.GameState;
import structures.basic.EffectAnimation;
import structures.basic.Position;
import structures.basic.Tile;

public class NightsorrowAssasin extends Unit implements OpeningGambitAbilityUnit {
	
    // Method representing the opening gambit ability of the Nightsorrow Assassin
    public void openingGambitAbility(ActorRef out, GameState gameState) {
    	
        Tile[][] board = Game.getBoard().getTiles();
        Position unitPosition = this.getPosition();
        int tilex = unitPosition.getTilex();
        int tiley = unitPosition.getTiley();
        Tile currentTile = Game.getBoard().getTile(tilex, tiley);
        Unit clickedUnit = Game.getBoard().getTile(tilex, tiley).getUnit();
        List<Unit> player2Units = Game.getBoard().getPlayer2Units();
        
        List<Tile> adjTiles = Game.getBoard().getAdjacentTiles(currentTile);
        for (Tile tile: adjTiles) {
        	if (tile.hasUnit()) {
        		if (player2Units.contains(tile.getUnit())) {
        			if (tile.getUnit().getMaxHealth() > tile.getUnit().getHealth()) {
        				EffectAnimation spellEffect = BasicObjectBuilders.loadEffect("conf/gameconfs/effects/f1_soulshatter.json");
        		        int animationDuration = BasicCommands.playEffectAnimation(out, spellEffect, tile);
        		        try {
        					Thread.sleep(animationDuration);
        				} catch (InterruptedException e) {
        					e.printStackTrace();
        				}
        				
        				BasicCommands.playUnitAnimation(out, tile.getUnit(), UnitAnimationType.death);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        BasicCommands.deleteUnit(out, tile.getUnit());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        tile.setUnit(null);
        			}
        		}
        	}
        }
        
        
    }

}