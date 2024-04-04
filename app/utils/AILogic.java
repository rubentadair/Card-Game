package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.spells.BeamShock;
import structures.spells.Spell;
import structures.spells.SundropElixir;
import structures.spells.TrueStrike;
import structures.units.Avatar;
import structures.basic.Card;


//AI Specific logic 
public class AILogic {
	
    // Main method to orchestrate the AI's turn.
	public static void playAITurn(ActorRef out, GameState gameState) {
		
		// AI checks its hand for playable cards.
		checkCards(out, gameState);
		
        // AI identifies valid moves it can make this turn.
		identifyValidMoves(out, gameState);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
        // Ends the AI's turn and potentially triggers the start of the next turn.
		endAITurn(out, gameState);
	}
	
	
    // Checks the AI's hand to decide on playing a card.
	public static void checkCards(ActorRef out, GameState gameState) {
		Card[] AIHand = gameState.player2.getPlayerHand();
		for (int i = 0; i < AIHand.length; i++) {
		
			//if the card is not a unit card and the ai has enough mana to use it then ai spell logic will come into effect
			if (AIHand[i] == null) {
				continue;
			}
			else if (!AIHand[i].getIsCreature()) {
				if (gameState.player2.getMana() > AIHand[i].getManacost()) {
					spellAILogic(AIHand[i], gameState, out);
					Game.getBoard().resetAllTiles(out);
				}
			}
			else {
				if (gameState.player2.getMana() > AIHand[i].getManacost()) {
					unitSummonAILogic(AIHand[i], gameState, out);
					Game.getBoard().resetAllTiles(out);
				}

			}
		}
		
        // Reset tile highlights after actions.
		Game.getBoard().resetAllTiles(out);
		
	}
	
    // Logic for the AI to play spell cards.
	public static void spellAILogic(Card spellCard, GameState gameState, ActorRef out) {
		Spell spell = SpellHandler.returnSpell(spellCard.getCardname());
		List<Unit> player1Units = Game.getBoard().getPlayer1Units();
		Unit weakestUnit = null;
		
		if (player1Units.size() == 0) {
			return;
		}
		
		if (spell instanceof TrueStrike) {
			for (Unit unit: player1Units) {
				//if its a guaranteed kill then the AI will go for it
				if (unit.getHealth() <= 2) {
					int tileX = unit.getPosition().getTilex();
					int tileY = unit.getPosition().getTiley();
					Tile targetTile = Game.getBoard().getTile(tileX, tileY);
					SpellHandler.performSpell(spell, targetTile, out, gameState);
					return;
				}
				//logic for finding the weakest unit hp wise to target
				else {
					if (weakestUnit == null) {
						weakestUnit = unit;
					}
					else {
						if (unit.getHealth() < weakestUnit.getHealth()) {
							weakestUnit = unit;
						}
					}
				}
			}
			if (weakestUnit == null) {
				return;
			}
			int tileX = weakestUnit.getPosition().getTilex();
			int tileY = weakestUnit.getPosition().getTiley();
			Tile targetTile = Game.getBoard().getTile(tileX, tileY);
			SpellHandler.performSpell(spell, targetTile, out, gameState);
		}
		
		if (spell instanceof BeamShock) {
						
				int tileX = gameState.player1.getAvatar().getPosition().getTilex();
				int tileY = gameState.player1.getAvatar().getPosition().getTiley();
				Tile targetTile = Game.getBoard().getTile(tileX, tileY);
				SpellHandler.performSpell(spell, targetTile, out, gameState);
			
			
		}
		
		if (spell instanceof SundropElixir) {
			List<Unit> player2Units = Game.getBoard().getPlayer2Units();
			Unit unitToHeal = null;
			int hpDiffFromMax = 0;
			Unit avatar = player2Units.get(0);
			int avatarXTile = gameState.player2.getAvatar().getPosition().getTilex();
			int avatarYTile = gameState.player2.getAvatar().getPosition().getTiley();
			Tile avatarTile = Game.getBoard().getTile(avatarXTile, avatarYTile);
			
			//if the avatar is missing more than 2hp the ai will just heal it for safety 
			if (avatar.getMaxHealth() - avatar.getHealth() >= 2) {
				SpellHandler.performSpell(spell, avatarTile, out, gameState);
				return;
			}
			else {
				for (Unit unit: player2Units) {
					if (unitToHeal == null) {
						if (unit.getHealth() < unit.getMaxHealth()) {
							hpDiffFromMax = unit.getMaxHealth() - unit.getHealth();
							unitToHeal = unit;
						}
					}
					else {
						if (unit.getMaxHealth() - unit.getHealth() > hpDiffFromMax) {
							hpDiffFromMax = unit.getMaxHealth() - unit.getHealth();
							unitToHeal = unit;
						}
					}
				}
				
				//the ai avatar will not use the spell unless there is a unit missing more than 2 hp from its max
				if (unitToHeal != null && hpDiffFromMax >= 2) {
					int tileX = unitToHeal.getPosition().getTilex();
					int tileY = unitToHeal.getPosition().getTiley();
					Tile targetTile = Game.getBoard().getTile(tileX, tileY);
					SpellHandler.performSpell(spell, targetTile, out, gameState);
				}
			}	
		}
	}
	
	
	
    // Performs logic to summon units, considering optimal positioning.
	public static void unitSummonAILogic(Card unitCard, GameState gameState, ActorRef out) {
		Unit AIAvatar = gameState.player2.getAvatar();
		Unit playerAvatar = gameState.player1.getAvatar();
		Tile tileToSummonOn = null;
		//the type casted avatar 
		Avatar aAIAvatar = (Avatar) AIAvatar;
		aAIAvatar.highlightAdjacentTiles(out, Game.getBoard().getTiles(), gameState);
		
		//displays direction the ai wants to spawn the unit - -1 = left, 1 = right
		int directionModifier = 0;
		if (AIAvatar.getPosition().getTilex() > playerAvatar.getPosition().getTilex()) {
			directionModifier = -1;
			tileToSummonOn = identifySummonTile(directionModifier, AIAvatar.getPosition().getTilex());
		}
		else if (AIAvatar.getPosition().getTilex() < playerAvatar.getPosition().getTilex()) {
			directionModifier = 1;
			tileToSummonOn = identifySummonTile(directionModifier, AIAvatar.getPosition().getTilex());
		}
		else {
			directionModifier = 0;
			tileToSummonOn = identifySummonTile(directionModifier, AIAvatar.getPosition().getTilex());
		}
		
		if (tileToSummonOn == null) {
			return;
		}
		else {
			summonAICard(out, gameState, tileToSummonOn, unitCard);
			Game.getBoard().resetAllTiles(out);
		}
		
		
	}
	
    // Finds a tile close to a target tile that aligns with the AI's strategy.
	public static Tile identifySummonTile(int directionModifier, int baseTileX) {
		List<Tile> tiles = Game.getBoard().getTileList();
		
		//checks x axis first
		for (Tile tile: tiles) {
			if (((tile.getTilex() + directionModifier) == baseTileX) && tile.getIsActionableTile()) {
				return tile;
			}
		}
		
		for (Tile tile: tiles) {
			if ((tile.getTilex() == baseTileX) && tile.getIsActionableTile()) {
				return tile;
			}
		}
		
		//if it cannot place the unit on the desired column or on the same column as the avatar it will look for the otherside
		directionModifier = directionModifier * -1;
		for (Tile tile: tiles) {
			if (((tile.getTilex() + directionModifier) == baseTileX) && tile.getIsActionableTile()) {
				return tile;
			}
		}
		
		return null;
	}
	
    // Identifies the most strategically advantageous tiles for the AI to move or attack.
	public static void identifyValidMoves(ActorRef out, GameState gameState) {
		
		ArrayList<Tile> moveTiles = new ArrayList<Tile>();
		ArrayList<Tile> attackTiles = new ArrayList<Tile>();
		List<Unit> aiUnits = new ArrayList<Unit>();

		aiUnits = Game.getBoard().getPlayer2Units();

		for (Unit unitOfInterest : aiUnits) {

			Tile tileOfInterest = Game.getBoard().getTile(unitOfInterest.getPosition().getTilex(),
					unitOfInterest.getPosition().getTiley());
			Game.showValidMovement(out, Game.getBoard().getTiles(), tileOfInterest, 2, gameState);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			performUnitBattle(unitOfInterest, out, gameState);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			performUnitMove(unitOfInterest, out, gameState);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Game.getBoard().resetAllTiles(out);
		}
	}
	
	
    // Finds a tile close to a target tile that aligns with the AI's strategy.
	public static Tile findPositioningTowardsTargetTile(Tile targetTile, int range) {
		if (range == 1) {
			List<Tile> adjTiles = Game.getBoard().getAdjacentTiles(targetTile);
			Collections.shuffle(adjTiles);
			for (Tile tile: adjTiles) {
				if (tile.getIsActionableTile() && !tile.hasUnit()) {
					return tile;
				}
			}
		}
		else {
			List<Tile> candidateTiles = new ArrayList<Tile>();
			int[] dx = { -range, -range, -range, 0, 0, range, range, range };
	        int[] dy = { -range, 0, range, -range, range, -range, 0, range };
	        int x = targetTile.getTilex();
	        int y = targetTile.getTiley();
	        // Iterate over adjacent tiles
	        for (int i = 0; i < 8; i++) {
	            int adjx = x + dx[i];
	            int adjy = y + dy[i];
	            if (adjx >= 9 || adjy >= 5 || adjx < 0 || adjy < 0) {
	            	continue;
	            }
	            else {
	            	try{
	            		candidateTiles.add(Game.getBoard().getTile(adjx, adjy));
	            	}
	            	catch (ArrayIndexOutOfBoundsException e) {
	            		e.printStackTrace();
	            		continue;
	            	}
	            	
	            }
	            
	        }
	        Collections.shuffle(candidateTiles);
	        for (Tile tile: candidateTiles) {
				if (tile.getIsActionableTile() && !tile.hasUnit()) {
					return tile;
				}
			}
	        
		}
		if (range <= 8) {
			return findPositioningTowardsTargetTile(targetTile, range + 1);
		}
		else {
			return null;
		}
	}

    // Executes an attack if a favorable battle opportunity is identified.
	public static void performUnitBattle(Unit unitOfInterest, ActorRef out, GameState gameState) {
		ArrayList<Tile> allTiles = Game.getBoard().getTileList();
		List<Unit> player1Units = Game.getBoard().getPlayer1Units();
		// we will first check all potential abilities for an easy kill with this loop
		for (Tile attackTile : allTiles) {
			if (attackTile.getIsActionableTile() && !unitOfInterest.getHasAttacked() && attackTile.hasUnit()) {
				if (player1Units.contains(attackTile.getUnit())) {
					Unit unitToAttack = attackTile.getUnit();
					// if the selected unit is able to kill the defending unit in 1 blow this
					// happens
					if (unitOfInterest.getAttack() > unitToAttack.getHealth()) {
						BattleHandler.attackUnit(out, unitOfInterest, attackTile, gameState);
						return;
					}
				
				}
			}
		}
		// if no easy kills are found this for loop is run instead for potential chip
		// damage
		for (Tile attackTile : allTiles) {
			if (attackTile.getIsActionableTile() && !unitOfInterest.getHasAttacked() && attackTile.hasUnit()) {
				if (player1Units.contains(attackTile.getUnit())) {
					Unit unitToAttack = attackTile.getUnit();
					// if the selected unit will survive a counter attack then it will attack for
					// the sake of attacking to chip the enemy unit
					if ((unitOfInterest.getHealth() > unitToAttack.getAttack()) && unitOfInterest.getAttack() > 0) {
						BattleHandler.attackUnit(out, unitOfInterest, attackTile, gameState);
						return;
					}
				}
			}
		}
	}
	
    // Moves the AI's unit towards strategic positions on the board.
	public static void performUnitMove(Unit unitOfInterest, ActorRef out, GameState gameState) {

		int currentTileX = unitOfInterest.getPosition().getTilex();
		int currentTileY = unitOfInterest.getPosition().getTiley();
		Tile currentTile = Game.getBoard().getTile(currentTileX, currentTileY);
		Tile tileToMoveTo = null;
		//if the ai avatars hp difference is greater than 5 (has less) then it will positioning safer
		if (gameState.player1.getAvatar().getHealth() - gameState.player2.getAvatar().getHealth() > 5) {
			if(unitOfInterest instanceof Avatar) {
				//8 , 0 is the safest coordinates for the AI in most situations
				Tile safestTile = Game.getBoard().getTile(8, 0);
				tileToMoveTo = findPositioningTowardsTargetTile(safestTile, 0);
			}
			else {
			int avatarXTile = gameState.player2.getAvatar().getPosition().getTilex();
			int avatarYTile = gameState.player2.getAvatar().getPosition().getTiley();
			Tile avatarTile = Game.getBoard().getTile(avatarXTile, avatarYTile);
			tileToMoveTo = findPositioningTowardsTargetTile(avatarTile, 0);
			}
		}
		
		else {
			int avatarXTile = gameState.player1.getAvatar().getPosition().getTilex();
			int avatarYTile = gameState.player1.getAvatar().getPosition().getTiley();
			Tile avatarTile = Game.getBoard().getTile(avatarXTile, avatarYTile);
			tileToMoveTo = findPositioningTowardsTargetTile(avatarTile, 0);
		}

		if (tileToMoveTo == null) {
			return;
		}
		else {
			
			BasicCommands.moveUnitToTile(out, unitOfInterest, tileToMoveTo);
			tileToMoveTo.setUnit(unitOfInterest);
			unitOfInterest.setPositionByTile(tileToMoveTo);
			currentTile.setUnit(null);
			unitOfInterest.setHasMoved(true);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Game.getBoard().resetAllTiles(out);
		}
	}


    // Helper method to handle the summoning process for an AI card.
	private static void summonAICard(ActorRef out, GameState gameState, Tile tile, Card card) {

		String cardJSONReference = card.getUnitConfig();
		gameState.player2.setMana(gameState.player2.getMana() - card.getManacost());
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		BasicCommands.setPlayer2Mana(out, gameState.player2);
		
		
		Unit unitSummon = SubUnitCreator.identifyUnitTypeAndSummon(card.getCardname(), cardJSONReference,
				tile.getTilex(), tile.getTiley());

		unitSummon.setPositionByTile(tile);
		tile.setUnit(unitSummon);
		
		EffectAnimation spellEffect = BasicObjectBuilders.loadEffect("conf/gameconfs/effects/f1_summon.json");
        int animationDuration = BasicCommands.playEffectAnimation(out, spellEffect, tile);
        try {
			Thread.sleep(animationDuration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		BasicCommands.drawUnit(out, unitSummon, tile);
		// stops tiles from highlighting after summon
		int health = card.getHealth();
		int attack = card.getAttack();
		unitSummon.setHealth(health);
		unitSummon.setMaxHealth(health);
		unitSummon.setAttack(attack);
		unitSummon.setMaxAttack(attack);
		unitSummon.setHasMoved(true);
		unitSummon.setName(card.getCardname());
		
		Game.getBoard().addPlayer2Unit(unitSummon);

		// a delay is required from drawing to setting attack/hp or else it will not
		// work
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// now grabs health and attack values from the card for drawing
		BasicCommands.setUnitHealth(out, unitSummon, card.getHealth());
		BasicCommands.setUnitAttack(out, unitSummon, card.getAttack());
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		BasicCommands.setPlayer2Mana(out, gameState.player2);
		try {
			
				for (int i = 0; i < gameState.player2.getPlayerHand().length; i++) {
			        if (gameState.player2.getPlayerHand()[i] != null && gameState.player2.getPlayerHand()[i].getCardname().equals(card.getCardname())) {
			        	gameState.player2.removeCardFromHand(i);  // Remove by setting the reference to null
			            break; // Stop if you only need to remove the first match
			        }
			    }
			
		}catch(Exception e) {
			System.out.println("exception");
		}
		Game.resetGameState(out, gameState);
	}


    // Ends the AI's turn and potentially triggers the start of the next player's turn.

	public static void endAITurn(ActorRef out, GameState gameState) {
		
        // Resets all tiles and initiates the next turn.
		Game.getBoard().resetAllTiles(out);
		Game.beginNewTurn(out, gameState);

	}

}
