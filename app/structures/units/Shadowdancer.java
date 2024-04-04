package structures.units;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;

public class Shadowdancer extends Unit implements DeathwatchAbilityUnit {
	
    // Method representing the deathwatch ability of the Shadowdancer
	public void deathwatchAbility(ActorRef out) {
		
		Unit humanAvatar = GameState.player1.getAvatar();
		Unit aiAvatar = GameState.player2.getAvatar();

		int humanAvatarHealth = humanAvatar.getHealth();
		int aiAvatarHealth = aiAvatar.getHealth();

		System.out.println(String.format("huamn avatar hp: %d, attack: %d \n ai avatar hp: %d, attack: %d",
				humanAvatar.getHealth(), humanAvatar.getAttack(),
				aiAvatar.getHealth(), aiAvatar.getAttack()));

		if (humanAvatar.getHealth() < 20) {
			humanAvatar.setHealth(humanAvatarHealth + 1);
		}
		aiAvatar.setHealth(aiAvatarHealth - 1);

		System.out.println(" " +
				humanAvatar.getHealth() + " " +
				humanAvatar.getAttack() + " " +
				aiAvatar.getHealth() + " " +
				aiAvatar.getAttack());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		BasicCommands.setUnitHealth(out, humanAvatar, humanAvatar.getHealth());

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("removing hp from enemy avatar");
		BasicCommands.setUnitHealth(out, aiAvatar, aiAvatar.getHealth());
	}

}
