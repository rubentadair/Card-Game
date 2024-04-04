package structures.units;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Unit;

//for clarity and better programming practice, instead of doing a large check on all unit names and writing all the methods into battlehandler 
//an interface will be utilised for polymorphism. Ensures encapsulation and cleaner code in the long run. This will add additional complexity at larger numbers
public interface DeathwatchAbilityUnit {
	// public void deathwatchAbility(ActorRef out);

	public void deathwatchAbility(ActorRef out);
}
