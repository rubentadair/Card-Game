package structures.spells;

import akka.actor.ActorRef;
import structures.Game;
import structures.GameState;
import structures.basic.*;
import structures.units.Avatar;
import utils.BasicObjectBuilders;

import java.util.List;
import java.util.Random;
import commands.BasicCommands;

/*
 * Spell Name - Horn of the Forsaken
 * Player Deck - Human Player (Player 1)
 * Cost = 1
 * Effects:
 *      Artifact 3: When cast, the artifact is equipped to the player’s avatar with 3 robustness.
 *                  Whenever the player’s avatar takes damage from any source, decrease this artifact’s
 *                  robustness by 1 (regardless of the amount of damage taken).
 *                  When this artifact’s robustness reaches 0, the artifact is destroyed and its effects no longer trigger.
 *                  
 *      On Hit (whenever this unit deals damage to an enemy unit): Summon a Wraithling on a randomly
 *      selected unoccupied adjacent tile. If there are no unoccupied tiles, then this ability has no effect.
 */

public class HornOfTheForsaken extends Spell {
    private int robustness = 3; // Initial robustness of the artifact

    public HornOfTheForsaken() {
   
        // Initialize the card with proper values
        this.manaCost = 1;
        this.name = "Horn of the Forsaken";
    }
    
    // Method representing the spell's effect
    public void spell(ActorRef out, GameState gameState, Tile tile) {
        Avatar playerAvatar = (Avatar) tile.getUnit();
        playerAvatar.setHornOfTheForsaken(this); // Equipping the artifact to the player's avatar

        BasicCommands.addPlayer1Notification(out, "Horn of the Forsaken equipped", 2); // Display notification
        EffectAnimation spellEffect = BasicObjectBuilders.loadEffect("conf/gameconfs/effects/f1_buff.json");
        int animationDuration = BasicCommands.playEffectAnimation(out, spellEffect, tile);
        try {
			Thread.sleep(animationDuration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

    }

    public void onPlayerAvatarDamaged(ActorRef out, GameState gameState) {
    	Avatar playerAvatar = (Avatar) gameState.currentPlayer.getAvatar();
        robustness -= 1;
        BasicCommands.addPlayer1Notification(out, "Horn of the Forsaken robustness decreased", 2);
        if (robustness <= 0) {
            BasicCommands.addPlayer1Notification(out, "Horn of the Forsaken destroyed", 2);
            playerAvatar.setHornOfTheForsaken(null); // Remove the artifact when robustness is 0
        }
    }

    public void onUnitHitsEnemy(ActorRef out, GameState gameState, Tile tile) {
        List<Tile> adjacentTiles = Game.getBoard().getAdjacentTiles(tile);
        Random rand = new Random();
        
        //why is this boolean here? is this for a while loop that disappeared mid development?
        boolean wraithlingSummoned = false;

        for (Tile adjacentTile : adjacentTiles) {
            if (!adjacentTile.hasUnit()) {
            	Game.summonToken(out, adjacentTile);
                wraithlingSummoned = true;
                break; // Summon only one wraithling
            }
        }

        if (!wraithlingSummoned) {
            BasicCommands.addPlayer1Notification(out, "No space for Wraithling", 2);
        }
    }
}
