package structures.spells;
 
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.units.Avatar;
import utils.BasicObjectBuilders;
 
/*
* Spell Name - Dark Terminus
* Player Deck - Human Player (Player 1)
* Cost = 4
* Effects:
* 			Destroy an enemy creature.
* 			Summon a Wraithling on the tile of the destroyed creature
* */
public class DarkTerminus extends Spell implements EnemySpell {
	
	public DarkTerminus() {
		this.manaCost = 4;
		this.name = "Dark Terminus";
	}
	
    // Method representing the spell's effect
    public void spell(ActorRef out, GameState gameState, Tile tile){
        // Check if there is an enemy unit on the targeted tile
        if (tile.hasUnit()) {
            Unit targetUnit = tile.getUnit();
            EffectAnimation spellEffect = BasicObjectBuilders.loadEffect("conf/gameconfs/effects/f1_soulshatter.json");
            int animationDuration = BasicCommands.playEffectAnimation(out, spellEffect, tile);
            try {
    			Thread.sleep(animationDuration);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
            // Destroy the enemy unit
            BasicCommands.deleteUnit(out, targetUnit);
            tile.setUnit(null); // Remove the unit from the tile
            Game.getBoard().removePlayer2Unit(targetUnit);
            // Summon a Wraithling on the same tile
            Game.summonToken(out, tile); 
        }
    }
}