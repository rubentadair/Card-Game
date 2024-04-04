package structures.spells;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.BattleHandler;

/*
 * Spell Name - True Strike
 * Player Deck - AI Player (Player 2)
 * Cost = 1
 * Effects: Deal 2 damage to an enemy unit.
 * */

public class TrueStrike extends Spell implements EnemySpell{
	
	public void TrueStrike(ActorRef out, GameState gameState, Tile tile){
		this.manaCost = 1;
		this.name = "Truestrike";
    }


	
    // Method representing the spell's effect
	public void spell(ActorRef out, GameState gameState, Tile tile) {
		Unit targetUnit = tile.getUnit();
		
		EffectAnimation spellEffect = BasicObjectBuilders.loadEffect("conf/gameconfs/effects/f1_inmolation.json");
        int animationDuration = BasicCommands.playEffectAnimation(out, spellEffect, tile);
        try {
			Thread.sleep(animationDuration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
		targetUnit.setHealth(targetUnit.getHealth() - 2);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (targetUnit.getHealth() == 0 ) {
			BasicCommands.playUnitAnimation(out, targetUnit, UnitAnimationType.death);
			tile.setUnit(null);	
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BattleHandler.unitDeathwatchAbilityCheck(out);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BasicCommands.deleteUnit(out, targetUnit);
			Game.getBoard().removePlayer1Unit(targetUnit);
		}
		else {
			BasicCommands.setUnitHealth(out, targetUnit, targetUnit.getHealth());
		}
		
		
	}




}
