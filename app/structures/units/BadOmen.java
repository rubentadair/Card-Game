package structures.units;

import java.util.List;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.BattleHandler;
import utils.StaticConfFiles;

public class BadOmen extends Unit implements DeathwatchAbilityUnit {
	
    // Method representing the deathwatch ability of the Bad Omen
	
	@Override
	public void deathwatchAbility(ActorRef out) {
		System.out.println("TILE: " + this.getPosition().getTilex() + "," +
				this.getPosition().getTiley());
		
		this.setAttack(getAttack() + 1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.setUnitAttack(out, this, this.getAttack());
	}
}
