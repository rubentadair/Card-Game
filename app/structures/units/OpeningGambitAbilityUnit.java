package structures.units;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * OpeningGambitAbility
 */
public interface OpeningGambitAbilityUnit {
    public void openingGambitAbility(ActorRef out, GameState gameState);

}