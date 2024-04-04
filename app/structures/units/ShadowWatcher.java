package structures.units;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;

public class ShadowWatcher extends Unit implements DeathwatchAbilityUnit {

    // Method representing the deathwatch ability of the ShadowWatcher
	public void deathwatchAbility(ActorRef out) {
		this.setHealth(getHealth() + 1);
		if (this.getAttack() < 3) {
			this.setAttack(getAttack() + 1);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.setUnitHealth(out, this, this.getHealth());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BasicCommands.setUnitAttack(out, this, this.getAttack());
	}
}
