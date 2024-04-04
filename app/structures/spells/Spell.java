package structures.spells;
 
import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
 
public abstract class Spell {
	public int manaCost;
	public String name = "";
	public abstract void spell(ActorRef out, GameState gameState, Tile tile);
	
	
	public  int getManaCost() {
		return this.manaCost;
	}
	public String getName() {
		return this.name;
	}
	public void setManaCost(int cost) {
		this.manaCost = cost;
	}
	public void setName(String name) {
		this.name = name;
	}
    
}