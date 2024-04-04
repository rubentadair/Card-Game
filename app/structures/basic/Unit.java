package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;

/**
 * This is a representation of a Unit on the game board.
 * A unit has a unique id (this is used by the front-end.
 * Each unit has a current UnitAnimationType, e.g. move,
 * or attack. The position is the physical position on the
 * board. UnitAnimationSet contains the underlying information
 * about the animation frames, while ImageCorrection has
 * information for centering the unit on the tile.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Unit {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java
																// objects from a file

	int id;
	UnitAnimationType animation;
	Position position;
	UnitAnimationSet animations;
	ImageCorrection correction;
	protected int maxHealth;
	protected int maxAttack;

	protected boolean hasMoved;
	protected boolean hasAttacked;

	// attributes required for combat
	int health;
	int attack;

	// Attribute to track if the unit is stunned
	protected boolean stunned;

	// checks if the unit has an ability
	private boolean hasAbility = false;
	private String name;

	public Unit() {
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;

		position = new Position(0, 0, 0, 0);
		this.correction = correction;
		this.animations = animations;
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;

		position = new Position(currentTile.getXpos(), currentTile.getYpos(), currentTile.getTilex(),
				currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
	}

	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UnitAnimationType getAnimation() {
		return animation;
	}

	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}

	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}

	public boolean getHasMoved() {
		return this.hasMoved;
	}

	public void setHasAttacked(boolean hasAttacked) {
		this.hasAttacked = hasAttacked;
	}

	public boolean getHasAttacked() {
		return this.hasAttacked;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void setMaxHealth(int maxHealth) {
		System.out.println("Set max health to: " + maxHealth);
		this.maxHealth = maxHealth;
	}

	public int getHealth() {
		return this.health;
	}

	public int getMaxHealth() {
		return this.maxHealth;
	}

	public int getMaxAttack() {
		return this.maxAttack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public void setMaxAttack(int maxAttack) {
		this.maxAttack = maxAttack;
	}

	public int getAttack() {
		return this.attack;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * This command sets the position of the Unit to a specified
	 * tile.
	 * 
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(), tile.getYpos(), tile.getTilex(), tile.getTiley());
	}

	// SPELL
	// Method to check if the unit is stunned
	public boolean isStunned(ActorRef out) {
		if (stunned) {
			BasicCommands.addPlayer1Notification(out, this.name + " is stunned and cannot move or attack!", 2);
		}
		return stunned;
	}

	// Method to set the unit's stunned state
	public void setStunned(ActorRef out, boolean stunned) {
		this.stunned = stunned;
		if (stunned) {
			BasicCommands.addPlayer1Notification(out, this.name + " is stunned and cannot move or attack!", 2);
		}
	}

	// Check if a unit is stunned before allowing it to move or attack
	public void performAction(ActorRef out, GameState gameState) {
		if (this.isStunned(out)) {
			BasicCommands.addPlayer1Notification(out, this.name + " is stunned and cannot act this turn.", 2);
		} else {
			// Normal action handling here
		}
	}
}
