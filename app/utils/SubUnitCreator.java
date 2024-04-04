package utils;

import java.util.HashMap;
import java.util.Map;

import structures.basic.Card;
import structures.basic.Unit;
import structures.units.BadOmen;
import structures.units.BloodmoonPriestess;
import structures.units.GloomChaser;
import structures.units.IroncliffGuardian;
import structures.units.ShadowWatcher;
import structures.units.Shadowdancer;
import structures.units.SilverguardKnight;
import structures.units.SilverguardSquire;
import structures.units.SwampEntangler;
import structures.units.NightsorrowAssasin;
import structures.units.RockPulveriser;

public class SubUnitCreator {
	public static int globalUnitID = 1;

    // A mapping from unit names to their corresponding class types. This allows for dynamic instantiation of units with specific abilities.
	final static Map<String, Class<? extends Unit>> unitMap = new HashMap<String, Class<? extends Unit>>() {
		{
            // Units with the "Deathwatch" ability, triggering effects upon death of other units.
			put("Bad Omen", BadOmen.class);
			put("Bloodmoon Priestess", BloodmoonPriestess.class);
			put("Shadowdancer", Shadowdancer.class);
			put("Shadow Watcher", ShadowWatcher.class);
			
            // Units with "Opening Gambit" ability, triggering effects when the unit enters play.
			put("Nightsorrow Assassin", NightsorrowAssasin.class);
			put("Gloom Chaser", GloomChaser.class);
			put("Silverguard Squire", SilverguardSquire.class);
			
            // Units with the "Provoke" ability, forcing adjacent enemy units to attack them.
			put("Rock Pulveriser", RockPulveriser.class);
			put("Swamp Entangler", SwampEntangler.class);
			put("Ironcliff Guardian", IroncliffGuardian.class);
			put("Silverguard Knight", SilverguardKnight.class);
			
		}
	};

	// Identifies a unit's type based on its name and summons it to the specified location on the board.
    // This method dynamically instantiates units with specific abilities or as a generic unit if no ability is matched.
	public static Unit identifyUnitTypeAndSummon(String unitName, String jsonConfig, int x, int y) {
		System.out.println(unitName);
		if (unitMap.containsKey(unitName)) {
			Class<? extends Unit> classType = unitMap.get(unitName);
			System.out.println(classType);
			globalUnitID++;
			System.out.println("printing a specific unit type");
			return BasicObjectBuilders.loadUnit(jsonConfig, globalUnitID, classType);
		}
		// If the unit is not an ability type then it will just be constructed as a
		// normal unit
		else {
			globalUnitID++;
			System.out.println("printing generic");
			return BasicObjectBuilders.loadUnit(jsonConfig, globalUnitID, Unit.class);
		}
	}

}
