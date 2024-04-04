package structures;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Board;
import structures.basic.Card;
import structures.basic.EffectAnimation;
import structures.basic.Player;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.spells.BeamShock;
import structures.spells.DarkTerminus;
import structures.spells.SundropElixir;
import structures.spells.TrueStrike;
import structures.spells.WraithlingSwarm;
import structures.units.Avatar;
import structures.units.DeathwatchAbilityUnit;
import structures.units.NightsorrowAssasin;
import structures.units.OpeningGambitAbilityUnit;
import structures.units.ProvokeAbilityUnit;
import utils.AILogic;
import utils.BasicObjectBuilders;
import utils.OrderedCardLoader;
import utils.StaticConfFiles;
import utils.SubUnitCreator;
import utils.BattleHandler;

import java.util.*;

//game logic will be stored here - contemplating just using GameState and making this whole concept redundant 
public class Game {

	private static Board board;

	public Game(ActorRef out) {
		System.out.println("Game : inside game constructor");
		createBoard(out);
	}

	public static void createBoard(ActorRef out) {
		System.out.println("Game : inside createBoard ");

		board = new Board(out);
	}

	public static Board getBoard() {
		return board;
	}

	public static void initialisePlayerDeck(ActorRef out, GameState gameState) {
		System.out.println("Game : inside createPlayerDeck");

		gameState.player1.setPlayerDeck(OrderedCardLoader.getPlayer1Cards(2));
		System.out.println("Game : inside createPlayerDeck : " + gameState.player1.getPlayerDeck().size());

		gameState.player1.drawInitialHand(out);

		// get AI cards and draw initial cards for AI
		gameState.player2.setPlayerDeck(OrderedCardLoader.getPlayer2Cards(2));
		gameState.player2.drawInitialHandAI(out);
	}

	public static void setManaOnStartTurn(ActorRef out, GameState gameState) {

		if (gameState.gameInitalised) {

			System.out.println("Game : inside setManaOnStartTurn");

			// checks for turn number, if it is greater than 8 then every turn after will
			// give the current player 9 mana.
			// players must start with 2 mana as the lowest so this will ensure the lowest
			// mana possible is 2
			if (gameState.turn < 2) {
				gameState.currentPlayer.setMana(2);
			} else if (gameState.turn >= 2 && gameState.turn <= 8) {
				gameState.currentPlayer.setMana(gameState.turn + 1);
			}
			else {
				gameState.currentPlayer.setMana(9);
			}

			// utilises a method created for this purpose rather than making additional work
			updateManaVisual(out, gameState.currentPlayer, gameState);

		}

	}

	public static void resetMana(ActorRef out, GameState gameState) {
		try {
			Thread.sleep(300);
			if (gameState.currentPlayer == gameState.player1) {
				gameState.player2.setMana(0);
				BasicCommands.setPlayer2Mana(out, gameState.player2);
				System.out.println("setting player mana to: " + String.valueOf(gameState.currentPlayer.getMana()));
			} else {
				gameState.player1.setMana(0);
				BasicCommands.setPlayer1Mana(out, gameState.player1);
				System.out.println("setting player mana to: " + String.valueOf(gameState.currentPlayer.getMana()));
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	// when the player selects a card this method is called - essentially has a
	// highlight and de-highlight system in place
	public static void selectCard(ActorRef out, GameState gameState, int handPosition) {
		if (!gameState.cardSelected) {

			BasicCommands.drawCard(out, GameState.player1.getPlayerHandCard(handPosition), handPosition, 1);
			gameState.currentCardSelected = handPosition;
			gameState.cardSelected = true;
			System.out.println("current selected card: " + String.valueOf(gameState.currentCardSelected));
		}

		else {
			Card currentSelectedCard = GameState.player1.getPlayerHandCard(gameState.currentCardSelected);
			System.out.println("current selected card: " + String.valueOf(gameState.currentCardSelected));
			BasicCommands.drawCard(out, currentSelectedCard, gameState.currentCardSelected, 0);
			BasicCommands.drawCard(out, GameState.player1.getPlayerHandCard(handPosition), handPosition, 1);
			gameState.currentCardSelected = handPosition;
			gameState.cardSelected = true;

		}
	}


	public static void summonUnit(ActorRef out, GameState gameState, int x, int y) {
		System.out.println("summoning unit");
		Card cardToPlayer = GameState.player1.getPlayerHandCard(gameState.currentCardSelected);
		String cardJSONReference = cardToPlayer.getUnitConfig();
		Tile tileSelected = board.getTile(x, y);
		System.out.println(tileSelected.getIsActionableTile());
		// ensures the card attempting to be played is a unit card
		if (tileSelected.getIsActionableTile() && cardToPlayer.getIsCreature()) {
			// mana cost check to ensure the player attempting to summon the unit has enough mana
			if (gameState.currentPlayer.getMana() - cardToPlayer.getManacost() < 0) {
				BasicCommands.addPlayer1Notification(out, "NOT ENOUGH MANA", 3);
			} else {
				Unit unitSummon = SubUnitCreator.identifyUnitTypeAndSummon(cardToPlayer.getCardname(),
						cardJSONReference, x, y);
				
				System.out.println("unitSummon : "+unitSummon);
				unitSummon.setPositionByTile(tileSelected);
				tileSelected.setUnit(unitSummon);

				// add unit summon to player 1 unit array
				board.addPlayer1Unit(unitSummon);
				
				BasicCommands.addPlayer1Notification(out, "I summon an ally.", 5);
				EffectAnimation spellEffect = BasicObjectBuilders.loadEffect("conf/gameconfs/effects/f1_summon.json");
		        int animationDuration = BasicCommands.playEffectAnimation(out, spellEffect, tileSelected);
		        try {
					Thread.sleep(animationDuration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				BasicCommands.drawUnit(out, unitSummon, tileSelected);
				// stops tiles from highlighting after summon
				unitSummon.setHasMoved(true);

				// a delay is required from drawing to setting attack/hp or else it will not work
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

		
				int healthVal = cardToPlayer.getHealth();
				int attackVal = cardToPlayer.getAttack();

				unitSummon.setHealth(healthVal);
				unitSummon.setMaxHealth(healthVal);
				unitSummon.setAttack(attackVal);
				unitSummon.setMaxAttack(attackVal);

				String name = cardToPlayer.getCardname();
				unitSummon.setName(name);
				
				// now grabs health and attack values from the card for drawing
				BasicCommands.setUnitHealth(out, unitSummon, cardToPlayer.getHealth());
				BasicCommands.setUnitAttack(out, unitSummon, cardToPlayer.getAttack());

				GameState.player1.removeCardFromHand(gameState.currentCardSelected);
				BasicCommands.deleteCard(out, gameState.currentCardSelected);

				if (unitSummon instanceof OpeningGambitAbilityUnit) {
					System.out.println("unit has opening gambit ability");
					((OpeningGambitAbilityUnit) unitSummon).openingGambitAbility(out, gameState);
				}


				GameState.player1.setMana(GameState.player1.getMana() - cardToPlayer.getManacost());
				updateManaVisual(out, gameState.player1, gameState);
			}

		} else {
			BasicCommands.addPlayer1Notification(out, "CANNOT PLACE UNIT THERE", 3);
		}
		gameState.cardSelected = false;
		gameState.currentCardSelected = -1;
		resetGameState(out, gameState);
	}

	public static void summonToken(ActorRef out, Tile tileToSummonOn) {
		SubUnitCreator.globalUnitID++;
		Unit wraithUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.wraithling, SubUnitCreator.globalUnitID,
				Unit.class);
		wraithUnit.setHealth(1);
		wraithUnit.setAttack(1);
		wraithUnit.setPositionByTile(tileToSummonOn);
		tileToSummonOn.setUnit(wraithUnit);
		
		EffectAnimation spellEffect = BasicObjectBuilders.loadEffect("conf/gameconfs/effects/f1_wraithsummon.json");
        int animationDuration = BasicCommands.playEffectAnimation(out, spellEffect, tileToSummonOn);
        try {
			Thread.sleep(animationDuration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
		BasicCommands.drawUnit(out, wraithUnit, tileToSummonOn);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.setUnitHealth(out, wraithUnit, 1);
		BasicCommands.setUnitAttack(out, wraithUnit, 1);

	}

	// requires the correct coordinates for tile locations for both avatars
	public static Unit[] avatarSummonSetup(ActorRef out, int x, int y, int x2, int y2) {

		System.out.println("Game : inside avatarSummonSetup");

		// stores an array of the two units
		Unit[] avatars = new Unit[2];
		Unit humanAvatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Avatar.class);
		Tile humanAvatarStartTile = board.getTile(x, y);
		humanAvatar.setPositionByTile(humanAvatarStartTile);
		humanAvatarStartTile.setUnit(humanAvatar);
		BasicCommands.drawUnit(out, humanAvatar, humanAvatarStartTile);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		humanAvatar.setAttack(2);
		humanAvatar.setHealth(20);
		humanAvatar.setName("Human Avatar");
		
		humanAvatar.setMaxAttack(2);
		humanAvatar.setMaxHealth(20);

		BasicCommands.setUnitHealth(out, humanAvatar, 20);
		BasicCommands.setUnitAttack(out, humanAvatar, 2);
		avatars[0] = humanAvatar;
	
		board.addPlayer1Unit(humanAvatar);

		Unit aiAvatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 1, Avatar.class);
		Tile aiAvatarStartTile = board.getTile(x2, y2);
		aiAvatarStartTile.setUnit(aiAvatar);
		aiAvatar.setPositionByTile(aiAvatarStartTile);
		BasicCommands.drawUnit(out, aiAvatar, aiAvatarStartTile);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		aiAvatar.setAttack(2);
		aiAvatar.setHealth(20);
		aiAvatar.setName("AI Avatar");
		
		aiAvatar.setMaxAttack(2);
		aiAvatar.setMaxHealth(20);


		BasicCommands.setUnitHealth(out, aiAvatar, 20);
		BasicCommands.setUnitAttack(out, aiAvatar, 2);
		avatars[1] = aiAvatar;
		
		board.addPlayer2Unit(aiAvatar);


		return avatars;

	}

	public static void showValidMovement(ActorRef out, Tile[][] grid, Tile tile, int distance, GameState gameState) {
		int X = tile.getTilex();
		int Y = tile.getTiley();
		Unit clickedUnit = tile.getUnit();
		System.out.println("getting valid moves");
		// checks adjacent tiles for any units with provoke and draws red square if
		// present
		List<Tile> adjTiles = board.getAdjacentTiles(tile);
		for (int i = 0; i < adjTiles.size(); i++) {
			if (adjTiles.get(i).hasUnit()) {
				Unit adjUnit = adjTiles.get(i).getUnit();
				if (adjUnit instanceof ProvokeAbilityUnit && (gameState.currentPlayer == gameState.player1 &&
						getBoard().getPlayer2Units().contains(adjUnit) ||
						gameState.currentPlayer == gameState.player2 &&
								getBoard().getPlayer1Units().contains(adjUnit))) {
					BasicCommands.drawTile(out, adjTiles.get(i), 2);
					adjTiles.get(i).setIsActionableTile(true);
					return;
				}
			} else {
				// Iterate over the board to find tiles within the specified distance.
				for (int x = Math.max(X - distance, 0); x <= Math.min(X + distance, grid.length - 1); x++) {
					for (int y = Math.max(Y - distance, 0); y <= Math.min(Y + distance, grid[0].length - 1); y++) {
						if (x < 0 || x > 8 || y < 0 || y > 4) {
							continue;
						}
						Tile checkedTile = Game.getBoard().getTile(x, y);
						// Calculate the Manhattan distance to the unit.
						int manhattanDistance = Math.abs(x - X) + Math.abs(y - Y);
						if (manhattanDistance <= distance) {
							// Highlight this tile.
							if (!checkedTile.hasUnit() && !clickedUnit.getHasMoved()) {
								if (gameState.currentPlayer == gameState.player1) {
									BasicCommands.drawTile(out, grid[x][y], 1);
								}
								
								// sets the tile to be actionable
								grid[x][y].setIsActionableTile(true);
								try {
									Thread.sleep(10);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							Unit unitOnTile = checkedTile.getUnit();
							// Check if the unit belongs to player 2
							if (Game.getBoard().getPlayer2Units().contains(unitOnTile)
									&& GameState.currentPlayer == GameState.player1 && !clickedUnit.getHasAttacked()) {
								BasicCommands.drawTile(out, grid[x][y], 2);
								// sets the tile to be actionable
								grid[x][y].setIsActionableTile(true);
								try {
									Thread.sleep(10);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							} else if (Game.getBoard().getPlayer1Units().contains(unitOnTile)
									&& GameState.currentPlayer == GameState.player2) {
								
								grid[x][y].setIsActionableTile(true);
								try {
									Thread.sleep(10);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}

					}
				}

			}
		}
	}

		// Method to update and display health for both players
	public static void updateHealthVisual(ActorRef out, Player player, GameState gameState) {
		if (gameState.currentPlayer == gameState.player1) {
			BasicCommands.setPlayer1Health(out, player); // Update player 1's health
		} else {
			BasicCommands.setPlayer2Health(out, player); // Update player 2's health
		}

	}

	// Method to update and display mana for both players
	public static void updateManaVisual(ActorRef out, Player player, GameState gameState) {

		System.out.println("Game : inside updateManaVisual");

		if (gameState.currentPlayer == gameState.player1) {
			BasicCommands.setPlayer1Mana(out, player); // Update player 1's mana
		} else {
			BasicCommands.setPlayer2Mana(out, player); // Update player 2's mana
		}

	}

	public static void highlightEnemyUnits(ActorRef out, GameState gameState) {
		
		System.out.println("Game : inside highlightEnemyUnits");
		for (Tile[] row : Game.getBoard().getTiles()) {
			for (Tile tile : row) {
				if (tile.hasUnit() && board.getPlayer2Units().contains(tile.getUnit())) {
					BasicCommands.drawTile(out, tile, 2);
					tile.setIsActionableTile(true);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void highlightFriendlyUnits(ActorRef out, GameState gameState) {
		for (Tile[] row : Game.getBoard().getTiles()) {
			for (Tile tile : row) {
				if (tile.hasUnit() && board.getPlayer1Units().contains(tile.getUnit())) {
					BasicCommands.drawTile(out, tile, 1); // 1 for friendly highlight
					tile.setIsActionableTile(true);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	



	public static void resetGameState(ActorRef out, GameState gameState) {
		if (gameState.currentPlayer == gameState.player1) {
			if (gameState.currentCardSelected != -1) {
				Card currentCardHighlighted = gameState.player1.getPlayerHandCard(gameState.currentCardSelected);
				BasicCommands.drawCard(out, currentCardHighlighted, gameState.currentCardSelected, 0);
			}

		}
		gameState.previousSelectedTile = null;
		gameState.isTileSelected = false;
		gameState.cardSelected = false;
	
		gameState.isCardSelectedSpell = false;
		gameState.currentSpell = null;
		getBoard().resetAllTiles(out);

	}

	public static void beginNewTurn(ActorRef out, GameState gameState) {

		System.out.println("Game : inside beginNewTurn");
		System.out.println("Game : gameState.turn : " + gameState.turn);
		gameState.turn++;
		System.out.println("Game : gameState.turn : " + gameState.turn);

		
		getBoard().resetAllTiles(out);
		resetGameState(out, gameState);
		
		if(gameState.currentPlayer == gameState.player1) {
			gameState.player1.drawCardAtTurnEnd(out, gameState);
			System.out.println("End Turned : inside if");
			gameState.currentPlayer = gameState.player2;
			Game.resetMana(out, gameState);
			List<Unit> player1Units = Game.getBoard().getPlayer1Units();
			for (Unit unit : player1Units) {
			    unit.setHasMoved(false);
			    unit.setHasAttacked(false);
			    unit.setStunned(out, false);
			}
			
			setManaOnStartTurn(out, gameState);
			AILogic.playAITurn(out, gameState);
		}
		
		
		// these actions occur if it is the players turn
		else if (gameState.currentPlayer == gameState.player2) {
			gameState.player2.drawCardAtTurnEnd(out, gameState);
			gameState.currentPlayer = gameState.player1;
			List<Unit> player2Units = Game.getBoard().getPlayer2Units();
			for (Unit unit : player2Units) {
			    unit.setHasMoved(false);
			    unit.setHasAttacked(false);
			    unit.setStunned(out, false);
			}
			Game.resetMana(out, gameState);
			setManaOnStartTurn(out, gameState);
		}


	
		
	}
}