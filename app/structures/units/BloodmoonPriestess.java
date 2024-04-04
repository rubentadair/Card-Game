package structures.units;

import java.util.List;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.BattleHandler;
import utils.StaticConfFiles;

public class BloodmoonPriestess extends Unit implements DeathwatchAbilityUnit {
	
    // Method representing the deathwatch ability of the Bloodmoon Priestess
	public void deathwatchAbility(ActorRef out) {

			
			Tile thisTile = Game.getBoard().getTile(this.getPosition().getTilex(), this.getPosition().getTiley());
			List<Tile> adjacentTiles = Game.getBoard().getAdjacentTiles(thisTile);
			
			for (Tile adjTile: adjacentTiles) {
				if (!adjTile.hasUnit()) {
					Game.summonToken(out, adjTile);
					return;
				}
				else continue;	
			}
		return;
	}
}
