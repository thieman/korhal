package jnibwapi.types;

/**
 * Represents a StarCraft upgrade type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/UpgradeType
 */
public class UpgradeType {
	
	public static final int numAttributes = 10;
	
	private String name;
	private int ID;
	private int raceID;
	private int mineralPriceBase;
	private int mineralPriceFactor;
	private int gasPriceBase;
	private int gasPriceFactor;
	private int upgradeTimeBase;
	private int upgradeTimeFactor;
	private int maxRepeats;
	private int whatUpgradesTypeID;
	
	public enum UpgradeTypes {
		Terran_Infantry_Armor,
		Terran_Vehicle_Plating,
		Terran_Ship_Plating,
		Zerg_Carapace,
		Zerg_Flyer_Carapace,
		Protoss_Ground_Armor,
		Protoss_Air_Armor,
		Terran_Infantry_Weapons,
		Terran_Vehicle_Weapons,
		Terran_Ship_Weapons,
		Zerg_Melee_Attacks,
		Zerg_Missile_Attacks,
		Zerg_Flyer_Attacks,
		Protoss_Ground_Weapons,
		Protoss_Air_Weapons,
		Protoss_Plasma_Shields,
		U_238_Shells, // Marine Range
		Ion_Thrusters, // Vulture Speed
		Undefined18,
		Titan_Reactor, // Science Vessel Energy
		Ocular_Implants, // Ghost Sight
		Moebius_Reactor, // Ghost Energy
		Apollo_Reactor, // Wraith Energy
		Colossus_Reactor, // Battle Cruiser Energy
		Ventral_Sacs, // Overlord Transport
		Antennae, // Overlord Sight
		Pneumatized_Carapace, // Overlord Speed
		Metabolic_Boost, // Zergling Speed
		Adrenal_Glands, // Zergling Attack
		Muscular_Augments, // Hydralisk Speed
		Grooved_Spines, // Hydralisk Range
		Gamete_Meiosis, // Queen Energy
		Metasynaptic_Node, // Defiler Energy
		Singularity_Charge, // Dragoon Range
		Leg_Enhancements, // Zealot Speed
		Scarab_Damage,
		Reaver_Capacity,
		Gravitic_Drive, // Shuttle Speed
		Sensor_Array, // Observer Sight
		Gravitic_Boosters, // Observer Speed
		Khaydarin_Amulet, // Templar Energy
		Apial_Sensors, // Scout Sight
		Gravitic_Thrusters, // Scout Speed
		Carrier_Capacity,
		Khaydarin_Core, // Arbiter Energy
		Undefined45,
		Undefined46,
		Argus_Jewel, // Corsair Energy
		Undefined48,
		Argus_Talisman, // Dark Archon Energy
		Undefined50,
		Caduceus_Reactor, // Medic Energy
		Chitinous_Plating, // Ultralisk Armor
		Anabolic_Synthesis, // Ultralisk Speed
		Charon_Boosters, // Goliath Range
		Undefined55,
		Undefined56,
		Undefined57,
		Undefined58,
		Undefined59,
		Undefined60,
		None,
		Unknown;
		public int getID() {
			return ordinal();
		}
	}
	
	public UpgradeType(int[] data, int index) {
		ID = data[index++];
		raceID = data[index++];
		mineralPriceBase = data[index++];
		mineralPriceFactor = data[index++];
		gasPriceBase = data[index++];
		gasPriceFactor = data[index++];
		upgradeTimeBase = data[index++];
		upgradeTimeFactor = data[index++];
		maxRepeats = data[index++];
		whatUpgradesTypeID = data[index++];
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getID() {
		return ID;
	}
	
	public int getRaceID() {
		return raceID;
	}
	
	public int getMineralPriceBase() {
		return mineralPriceBase;
	}
	
	public int getMineralPriceFactor() {
		return mineralPriceFactor;
	}
	
	public int getGasPriceBase() {
		return gasPriceBase;
	}
	
	public int getGasPriceFactor() {
		return gasPriceFactor;
	}
	
	public int getUpgradeTimeBase() {
		return upgradeTimeBase;
	}
	
	public int getUpgradeTimeFactor() {
		return upgradeTimeFactor;
	}
	
	public int getMaxRepeats() {
		return maxRepeats;
	}
	
	public int getWhatUpgradesTypeID() {
		return whatUpgradesTypeID;
	}
}
