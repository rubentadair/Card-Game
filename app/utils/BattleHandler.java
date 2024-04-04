package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import structures.spells.HornOfTheForsaken;
import structures.units.DeathAbilityUnit;
import structures.units.DeathwatchAbilityUnit;
import structures.units.SilverguardKnight;
import structures.units.Avatar;

public class BattleHandler {
	
    // Initiates an attack from one unit to another, processing the battle outcome.
	public static void attackUnit(ActorRef out, Unit attacker, Tile defenderTile, GameState gameState) {
		List<Unit> player1Units = Game.getBoard().getPlayer1Units();
		List<Unit> player2Units = Game.getBoard().getPlayer2Units();
		Unit defender = defenderTile.getUnit();		
	
		Tile attackerTile = Game.getBoard().getTile(attacker.getPosition().getTilex(), attacker.getPosition().getTiley());
	
		
		if (!travelToFightUnit(out, gameState, attackerTile, defenderTile)) {
			BasicCommands.addPlayer1Notification(out, "I cannot reach that unit.", 5);
			return;
		}
	
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("can attack");

		// debugging code to ensure positions and values are intended
		System.out.println("defender: " + String.valueOf(defender.getPosition()));
		System.out.println("attacker: " + String.valueOf(attacker.getPosition()));
		System.out.println(defender.getName() + " is defending");
		System.out.println(attacker.getName() + " is attacking");
		int defenderPostCombatHealth = defender.getHealth() - attacker.getAttack();
		
		if(defender instanceof Avatar && ((Avatar) defender).getHasHornOfForsaken()) {
			HornOfTheForsaken hornOfTheForsaken = (HornOfTheForsaken) ((Avatar) defender).getHornOfTheForsaken();
			hornOfTheForsaken.onPlayerAvatarDamaged(out, gameState);
			defenderPostCombatHealth = 0;
		}
		
		else if(attacker instanceof Avatar && ((Avatar) attacker).getHasHornOfForsaken()) {
			HornOfTheForsaken hornOfTheForsaken = (HornOfTheForsaken) ((Avatar) attacker).getHornOfTheForsaken();
			hornOfTheForsaken.onUnitHitsEnemy(out, gameState, attackerTile);
		}
		
		defender.setHealth(defenderPostCombatHealth);

		BasicCommands.setUnitHealth(out, defender, defenderPostCombatHealth);

		// display defender player's health on top
		if ((defender instanceof Avatar) && gameState.currentPlayer == gameState.player1) {
			gameState.player2.setHealth(defenderPostCombatHealth);
			for (Unit unit: player2Units) {
				//if a silverguard knight is on the board then it will gain two attack as per its ability zeal
				if (unit instanceof SilverguardKnight) {
					unit.setAttack(unit.getAttack() + 2);
				}
			}
			BasicCommands.setPlayer2Health(out, gameState.player2); // Update player 2's health
		} else if ((defender instanceof Avatar) && gameState.currentPlayer == gameState.player2) {
			gameState.player1.setHealth(defenderPostCombatHealth);
			BasicCommands.setPlayer1Health(out, gameState.player1); // Update player 1's health
		}
		
		Game.resetGameState(out, gameState);
		BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(defender.getPosition());
		BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.idle);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (defenderPostCombatHealth > 0 && !defender.isStunned(out)) {
			counterAttack(out, defender, attacker, gameState);
		} else {

			// check for end game condition
			if (defender instanceof Avatar) {
				if (defender == gameState.player1.getAvatar()) {
					Player player1 = gameState.player1;
					gameOver(out, player1, gameState);
				} else {
					Player player2 = gameState.player2;
					gameOver(out, player2, gameState);
				}
			}

			if (gameState.currentPlayer == gameState.player1) {
				BasicCommands.addPlayer1Notification(out, "Another one dies but the war continues", 5);
			}
			BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.death);
			defenderTile.setUnit(null);	
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			unitDeathwatchAbilityCheck(out);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BasicCommands.deleteUnit(out, defender);
			
			//removes the unit from the list as it has been defeated
			if (gameState.currentPlayer == gameState.player1) {
				Game.getBoard().removePlayer2Unit(defender);
			}
			else {
				Game.getBoard().removePlayer1Unit(defender);
			}
		}
		attacker.setHasAttacked(true);
		Game.resetGameState(out, gameState);
		
	}

    // Counter-attack logic when the defending unit strikes back at the attacker, if applicable.
	public static void counterAttack(ActorRef out, Unit counterAttacker, Unit defender, GameState gameState) {
		System.out.println("COUNTER attacking");
		int defenderPostCombatHealth = defender.getHealth();
		System.out.println("post combat health " + defenderPostCombatHealth);
		//checks for the horn of the forsaken
		if(defender instanceof Avatar && ((Avatar) defender).getHasHornOfForsaken()) {
			System.out.println("HAS HORN");
			HornOfTheForsaken hornOfTheForsaken = (HornOfTheForsaken) ((Avatar) defender).getHornOfTheForsaken();
			hornOfTheForsaken.onPlayerAvatarDamaged(out, gameState);
		}
		else {
			defenderPostCombatHealth = defender.getHealth() - counterAttacker.getAttack();
		}
		defender.setHealth(defenderPostCombatHealth);
		BasicCommands.setUnitHealth(out, defender, defenderPostCombatHealth);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		BasicCommands.playUnitAnimation(out, counterAttacker, UnitAnimationType.attack);

		// display defender player's health on top
		if ((defender instanceof Avatar) && gameState.currentPlayer == gameState.player1) {
			gameState.player1.setHealth(defenderPostCombatHealth);
			BasicCommands.setPlayer1Health(out, gameState.player1); // Update player 1's health
		} else if ((defender instanceof Avatar) && gameState.currentPlayer == gameState.player2) {
			gameState.player2.setHealth(defenderPostCombatHealth);
			BasicCommands.setPlayer2Health(out, gameState.player2); // Update player 2's health
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (defenderPostCombatHealth <= 0) {
			BasicCommands.playUnitAnimation(out, defender, UnitAnimationType.death);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			unitDeathwatchAbilityCheck(out);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BasicCommands.deleteUnit(out, defender);
			
			
			int dX = defender.getPosition().getTilex();
			int dY = defender.getPosition().getTiley();
			Tile defenderTile = Game.getBoard().getTile(dX, dY);
			defenderTile.setUnit(null);	
			if (gameState.currentPlayer == gameState.player1) {
				Game.getBoard().removePlayer2Unit(defender);
			}
			else {
				Game.getBoard().removePlayer1Unit(defender);
			}
			
			
		} else {
			defender.setHealth(defenderPostCombatHealth);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.playUnitAnimation(out, counterAttacker, UnitAnimationType.idle);
	}

	// will now loop over all units on the board
	public static void unitDeathwatchAbilityCheck(ActorRef out) {
		
		System.out.println("checking for deathwatch ability");
		// logic for checking if it has death ability or not
		List<Unit> player1Units = Game.getBoard().getPlayer1Units();

		for (int i = 0; i < player1Units.size(); i++) {
			Unit unit = player1Units.get(i);
			if (unit instanceof DeathwatchAbilityUnit) {
				System.out.println(unit.getName() + " is a deathwatch unit and its ability will go off");
				((DeathwatchAbilityUnit) unit).deathwatchAbility(out);
			}
		}

	}

	public static int[][] shuffleArray(int[][] array) {
		Random random = new Random();

		// Shuffle the array
		for (int i = 0; i < array.length; i++) {
			// Swap with a random row
			int randomIndex = random.nextInt(array.length);
			int[] temp = array[i];
			array[i] = array[randomIndex];
			array[randomIndex] = temp;
		}
		return array;
	}
	
	public static boolean travelToFightUnit(ActorRef out, GameState gameState, Tile startTile, Tile targetTile) {
		System.out.println("start tile: " + startTile.getUnit().getName());
		System.out.println("target tile: " + targetTile.getUnit().getName());
		List<Tile> adjTiles = Game.getBoard().getAdjacentTiles(startTile);
		if (adjTiles.contains(targetTile)) {
			return true;
		}
		else {
			for (Tile tile: adjTiles) {
				System.out.println("checking tiles");
				if (startTile.getUnit().getHasMoved()) {
					return false;
				}
				//this might need testing
				else if (canAttack(tile, targetTile) && !tile.hasUnit() && tile.getIsActionableTile()) {
					System.out.println("able to move");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					System.out.println("Moving: " + startTile.getUnit().getName());
					BasicCommands.moveUnitToTile(out, startTile.getUnit(), tile);
					startTile.getUnit().setPositionByTile(tile);
					tile.setUnit(startTile.getUnit());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					startTile.getUnit().setHasMoved(true);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("finished finding square");
					startTile.setUnit(null);
					return true;
				}
			}
			
		}
		return false;
	}
	
	public static boolean canAttack(Tile startTile, Tile targetTile) {
		List<Tile> adjTiles = Game.getBoard().getAdjacentTiles(startTile);
		if (adjTiles.contains(targetTile)) {
			return true;
		}
		else {
			return false;
		}
	}

	public static void gameOver(ActorRef out, Player winner, GameState gamestate) {
		if (winner == GameState.player1) {
			BasicCommands.addPlayer1Notification(out, "you win", 10000);
		} else {
			BasicCommands.addPlayer1Notification(out, "you lose", 10000);
		}
		GameState.gameOver = true;
	}

}
