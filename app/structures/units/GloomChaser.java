package structures.units;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.Game;
import structures.GameState;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import utils.SubUnitCreator;

public class GloomChaser extends Unit implements OpeningGambitAbilityUnit {

    // Method representing the opening gambit ability of the GloomChaser
    public void openingGambitAbility(ActorRef out, GameState gameState) {

        Tile[][] board = Game.getBoard().getTiles();
        Position unitPosition = this.getPosition();
        Position avatarPosition = GameState.player1.getAvatar().getPosition();

        int x = unitPosition.getTilex() - 1;
        int y = unitPosition.getTiley();
        
        // getting left tile using clicked tile co-rodinates
        if (x >= 0 && x < 9) {
        	try {
        		Tile leftTile = board[x][y];
                // summon wrathling if the tile on the left has no unit present on it
                
                if (!leftTile.hasUnit()) {
                    Game.summonToken(out, leftTile);
                }
        	}
        	catch (ArrayIndexOutOfBoundsException e) {
        		e.printStackTrace();
        	}
            
        }

    }

}
