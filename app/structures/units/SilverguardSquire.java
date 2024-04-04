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

public class SilverguardSquire extends Unit implements OpeningGambitAbilityUnit {

    // Method representing the opening gambit ability of the Silverguard Squire
    public void openingGambitAbility(ActorRef out, GameState gameState) {
    	
        Tile[][] board = Game.getBoard().getTiles();
        Unit humanAvatar = GameState.player1.getAvatar();
        Position unitPosition = humanAvatar.getPosition();
        List<Unit> player1Units = Game.getBoard().getPlayer1Units();
        
        // array for getting tiles to the left and right of the Avatar
        int[][] array = {
                { -1, 0 },
                { 1, 0 },
        };
        
        // shuffling for random selection of tiles
        int[][] shuffledArray = BattleHandler.shuffleArray(array);
        
        for (int i = 0; i < shuffledArray.length; i++) {
            int x = unitPosition.getTilex() + shuffledArray[i][0];
            int y = unitPosition.getTiley() + shuffledArray[i][1];
            Tile adjacentTile = board[x][y];
            
            // condition to find allied units
            if (adjacentTile.hasUnit() && player1Units.contains(adjacentTile.getUnit())) {
                Unit adjacentUnit = adjacentTile.getUnit();
                adjacentUnit.setHealth(adjacentUnit.getHealth() + 1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                adjacentUnit.setAttack(adjacentUnit.getAttack() + 1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        return;
    }

}