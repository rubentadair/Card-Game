package structures;

import commands.BasicCommands;
import utils.BasicObjectBuilders;
import structures.basic.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import akka.actor.ActorRef;


//stores tiles
public class Board {
	private Tile[][] tiles;
	private int rows = 9;
	private int columns = 5;
	
    private List<Unit> player1Units;
    private List<Unit> player2Units;
    private ArrayList<Tile> tileList = new ArrayList<Tile>();
	
	public Board(ActorRef out) {
		System.out.println("Board : inside Board constructor ");
		tiles = createTiles();
		drawBoard(out, tiles);
        player1Units = new ArrayList<>();
        player2Units = new ArrayList<>();
	}
	
	public Tile[][] createTiles() {
		System.out.println("Board : inside createTiles ");

		Tile[][] tiles = new Tile[rows][columns];
		for(int i=0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				tiles[i][j] = BasicObjectBuilders.loadTile(i,j);
				tileList.add(tiles[i][j]);
			}
		}
		return tiles;
	}
	
	public void drawBoard(ActorRef out, Tile[][] tiles) {
		System.out.println("Board : drawBoard ");

		for(int i=0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				//multi thread this to prevent buffer overflow
				BasicCommands.drawTile(out, tiles[i][j], 0);
				try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();} 
			}
		}
	}
	
	//returns all adjacent tiles from an incoming tile
	public List<Tile> getAdjacentTiles(Tile middleTile){
		List<Tile> adjTiles = new ArrayList<Tile>();
		int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
        int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };
        int x = middleTile.getTilex();
        int y = middleTile.getTiley();
        // Iterate over adjacent tiles
        for (int i = 0; i < 8; i++) {
            int adjx = x + dx[i];
            int adjy = y + dy[i];
            if (adjx >= 9 || adjy >= 5 || adjx < 0 || adjy < 0) {
            	continue;
            }
            else {
            	try{
            	adjTiles.add(getTile(adjx, adjy));
            	}
            	catch (ArrayIndexOutOfBoundsException e) {
            		e.printStackTrace();
            		continue;
            	}
            }
            
        }
        return adjTiles;
        
	}
	
	//makes all tiles unactionable
	public void resetAllTiles(ActorRef out) {
		System.out.println("resetting tiles");
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if(tiles[i][j].getIsActionableTile()) {
					tiles[i][j].setIsActionableTile(false);
					BasicCommands.drawTile(out, tiles[i][j], 0);
				}
				
			}
		}
	}
	
	//removes all drawings on tiles
	public void unHighlightAllTiles(ActorRef out) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				//BasicCommands.drawTile(out, tiles[i][j], 0);
			}
		}
	}
	
	public Tile getTile(int x, int y) {
		//double check x and y are in the right order
		return tiles[x][y];
	}
	
	public Tile[][] getTiles(){
		return this.tiles;
	}
	
	public void setPlayer1Units(List<Unit> player1Units) {
	    this.player1Units = player1Units;
	}

	public List<Unit> getPlayer1Units() {
	    return player1Units;
	}

	public void addPlayer1Unit(Unit unit) {
	    player1Units.add(unit);
	}

	public void removePlayer1Unit(Unit unit) {
	    player1Units.remove(unit);
	}

	public void setPlayer2Units(List<Unit> player2Units) {
	    this.player2Units = player2Units;
	}

	public List<Unit> getPlayer2Units() {
	    return player2Units;
	}

	public void addPlayer2Unit(Unit unit) {
	    player2Units.add(unit);
	}

	public void removePlayer2Unit(Unit unit) {
	    player2Units.remove(unit);
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}
	
	public ArrayList<Tile> getTileList(){
		return tileList;
	}

	
}
