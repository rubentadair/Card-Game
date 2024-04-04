package structures.spells;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import structures.units.*;

/*
 * Spell Name - Wraithling Swarm
 * Player Deck - Human Player (Player 1)
 * Cost = 3
 * Effects:
 *          Summon 3 Wraithlings in sequence.
 */

public class WraithlingSwarm extends Spell {
	private boolean isStillCasting = true;
	
	public WraithlingSwarm() {
		this.manaCost = 3;
		this.name = "Wraithling Swarm";
	}
	
    // Method representing the spell's effect
    public void spell(ActorRef out, GameState gameState, Tile tile) {
    	int wraiths = 0;
    	while(wraiths < 4) {
    		if (!tile.hasUnit()) {
                Game.summonToken(out, tile);
                wraiths++;
                try {
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
    		else {
    			continue;
    		}
    	}
    	isStillCasting = true;
    	  	
    }
    
    public boolean getIsStillCasting() {
    	return this.isStillCasting;
    }


}