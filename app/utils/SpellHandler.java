package utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.spells.BeamShock;
import structures.spells.DarkTerminus;
import structures.spells.EnemySpell;
import structures.spells.FriendlySpell;
import structures.spells.HornOfTheForsaken;
import structures.spells.*;
import structures.spells.Spell;
import structures.spells.SundropElixir;
import structures.spells.TrueStrike;
import structures.units.Avatar;


public class SpellHandler {
	
    // A mapping from spell names to their corresponding class types. This allows for dynamic spell instantiation.
	private static final HashMap<String, Class <? extends Spell>> spellMap = new HashMap<String, Class <? extends Spell>>(){
		{
			
			put("Horn of the Forsaken", HornOfTheForsaken.class);
			put("Wraithling Swarm", WraithlingSwarm.class);
			put("Dark Terminus", DarkTerminus.class);
			
			put("Sundrop Elixir", SundropElixir.class);
			put("Truestrike", TrueStrike.class);
			put("Beamshock", BeamShock.class);
			
		}
		
	};
	
	//identifies class type and through reflection gets an instance of the spell 
	public static Spell returnSpell(String spellname){
		System.out.println(spellname);
		if (spellMap.containsKey(spellname)) {
			Class<? extends Spell> spellOfInterest = spellMap.get(spellname);
			try {
				System.out.println("returning spell: " + spellOfInterest.getName());
				return (Spell)spellOfInterest.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
				
		}
		else {
			return null;
		}
		return null;
	}
	
	public static void HighlightTilesSpell(Spell spell, ActorRef out, GameState gameState) {
		
		if (spell instanceof WraithlingSwarm) {
			ArrayList<Tile> tiles = Game.getBoard().getTileList();
			for (Tile tile: tiles) {
				if (!tile.hasUnit()) {
					BasicCommands.drawTile(out, tile, 1);
					tile.setIsActionableTile(true);
				}
			}
			WraithlingSwarm ws = (WraithlingSwarm) spell;
			while(ws.getIsStillCasting()) {
				
			}
			Game.resetGameState(out, gameState);
		}
		
		else if (spell instanceof HornOfTheForsaken) {
			int x = gameState.player1.getAvatar().getPosition().getTilex();
			int y = gameState.player1.getAvatar().getPosition().getTiley();
			Tile tile = Game.getBoard().getTile(x, y);
			BasicCommands.drawTile(out, tile, 1);
			tile.setIsActionableTile(true);
		}
		
		else if (spell instanceof FriendlySpell) {
			System.out.println("CardClicked : inside spell instanceof FriendlySpell");

			Game.highlightFriendlyUnits(out, gameState);
		}
		else if (spell instanceof EnemySpell) {
			System.out.println("CardClicked : inside spell instanceof EnemySpell");

			Game.highlightEnemyUnits(out, gameState);
		}
		
	}
	
	//performs the spell
	public static void performSpell(Spell spell, Tile selectedTile, ActorRef out, GameState gameState) {
		if (spell instanceof DarkTerminus && selectedTile.getUnit() instanceof Avatar) {
        	BasicCommands.addPlayer1Notification(out, "He seems to be protected by something", 3);
        	Game.resetGameState(out, gameState);
        	return;
		}
		
		if (spell instanceof HornOfTheForsaken && !selectedTile.getIsActionableTile()) {
			Game.resetGameState(out, gameState);
			return;
			
		}
		
		else {
			gameState.currentPlayer.setMana(gameState.currentPlayer.getMana() - spell.getManaCost()); // Deducting mana cost
			if (gameState.currentPlayer == gameState.player1) {
				BasicCommands.addPlayer1Notification(out, "I use this spell!", 5);
				BasicCommands.setPlayer1Mana(out, gameState.currentPlayer);
				GameState.player1.removeCardFromHand(gameState.currentCardSelected);
				BasicCommands.deleteCard(out, gameState.currentCardSelected);
			}
			else {
				BasicCommands.setPlayer2Mana(out, gameState.currentPlayer);
				gameState.player2.removeCardFromHand(spell.name);
			}
			spell.spell(out, gameState, selectedTile);	
		}
		
		Game.resetGameState(out, gameState);
	}

}
