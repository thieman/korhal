package jnibwapi.types;

/**
 * Represents a StarCraft unit type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/UnitType
 */
public class UnitType {
	
	public static final int numAttributes = 56;
	public static final double fixedScale = 100.0;
	
	private String name;
	private int ID;
	private int raceID;
	private int whatBuildID;
	private int armorUpgradeID;
	private int maxHitPoints;
	private int maxShields;
	private int maxEnergy;
	private int armor;
	private int mineralPrice;
	private int gasPrice;
	private int buildTime;
	private int supplyRequired;
	private int supplyProvided;
	private int spaceRequired;
	private int spaceProvided;
	private int buildScore;
	private int destroyScore;
	private int sizeID;
	private int tileWidth;
	private int tileHeight;
	private int dimensionLeft;
	private int dimensionUp;
	private int dimensionRight;
	private int dimensionDown;
	private int seekRange;
	private int sightRange;
	private int groundWeaponID;
	private int maxGroundHits;
	private int airWeaponID;
	private int maxAirHits;
	private double topSpeed;
	private int acceleration;
	private int haltDistance;
	private int turnRadius;
	private boolean produceCapable;
	private boolean attackCapable;
	private boolean canMove;
	private boolean flyer;
	private boolean regenerates;
	private boolean spellcaster;
	private boolean invincible;
	private boolean organic;
	private boolean mechanical;
	private boolean robotic;
	private boolean detector;
	private boolean resourceContainer;
	private boolean refinery;
	private boolean worker;
	private boolean requiresPsi;
	private boolean requiresCreep;
	private boolean burrowable;
	private boolean cloakable;
	private boolean building;
	private boolean addon;
	private boolean flyingBuilding;
	private boolean spell;
	
	public enum UnitTypes {
		Terran_Marine,
		Terran_Ghost,
		Terran_Vulture,
		Terran_Goliath,
		Undefined4, // Goliath Turret
		Terran_Siege_Tank_Tank_Mode,
		Undefined6, // Siege Tank Turret (Tank Mode)
		Terran_SCV,
		Terran_Wraith,
		Terran_Science_Vessel,
		Hero_Gui_Montag,
		Terran_Dropship,
		Terran_Battlecruiser,
		Terran_Vulture_Spider_Mine,
		Terran_Nuclear_Missile,
		Terran_Civilian,
		Hero_Sarah_Kerrigan,
		Hero_Alan_Schezar,
		Undefined18, // Alan Schezar Turret
		Hero_Jim_Raynor_Vulture,
		Hero_Jim_Raynor_Marine,
		Hero_Tom_Kazansky,
		Hero_Magellan,
		Hero_Edmund_Duke_Tank_Mode,
		Undefined24, // Edmund Duke Turret (Tank Mode)
		Hero_Edmund_Duke_Siege_Mode,
		Undefined26, // Edmund Duke Turret (Siege Mode)
		Hero_Arcturus_Mengsk,
		Hero_Hyperion,
		Hero_Norad_II,
		Terran_Siege_Tank_Siege_Mode,
		Undefined31, // Siege Tank Turret (Siege Mode)
		Terran_Firebat,
		Spell_Scanner_Sweep,
		Terran_Medic,
		Zerg_Larva,
		Zerg_Egg,
		Zerg_Zergling,
		Zerg_Hydralisk,
		Zerg_Ultralisk,
		Zerg_Broodling,
		Zerg_Drone,
		Zerg_Overlord,
		Zerg_Mutalisk,
		Zerg_Guardian,
		Zerg_Queen,
		Zerg_Defiler,
		Zerg_Scourge,
		Hero_Torrasque,
		Hero_Matriarch,
		Zerg_Infested_Terran,
		Hero_Infested_Kerrigan,
		Hero_Unclean_One,
		Hero_Hunter_Killer,
		Hero_Devouring_One,
		Hero_Kukulza_Mutalisk,
		Hero_Kukulza_Guardian,
		Hero_Yggdrasill,
		Terran_Valkyrie,
		Zerg_Cocoon,
		Protoss_Corsair,
		Protoss_Dark_Templar,
		Zerg_Devourer,
		Protoss_Dark_Archon,
		Protoss_Probe,
		Protoss_Zealot,
		Protoss_Dragoon,
		Protoss_High_Templar,
		Protoss_Archon,
		Protoss_Shuttle,
		Protoss_Scout,
		Protoss_Arbiter,
		Protoss_Carrier,
		Protoss_Interceptor,
		Hero_Dark_Templar,
		Hero_Zeratul,
		Hero_Tassadar_Zeratul_Archon,
		Hero_Fenix_Zealot,
		Hero_Fenix_Dragoon,
		Hero_Tassadar,
		Hero_Mojo,
		Hero_Warbringer,
		Hero_Gantrithor,
		Protoss_Reaver,
		Protoss_Observer,
		Protoss_Scarab,
		Hero_Danimoth,
		Hero_Aldaris,
		Hero_Artanis,
		Critter_Rhynadon,
		Critter_Bengalaas,
		Undefined91, // Cargo Ship (Unused)
		Undefined92, // Mercenary Gunship (Unused)
		Critter_Scantid,
		Critter_Kakaru,
		Critter_Ragnasaur,
		Critter_Ursadon,
		Zerg_Lurker_Egg,
		Hero_Raszagal,
		Hero_Samir_Duran,
		Hero_Alexei_Stukov,
		Undefined101, // Map Revealer
		Hero_Gerard_DuGalle,
		Zerg_Lurker,
		Hero_Infested_Duran,
		Spell_Disruption_Web,
		Terran_Command_Center,
		Terran_Comsat_Station,
		Terran_Nuclear_Silo,
		Terran_Supply_Depot,
		Terran_Refinery,
		Terran_Barracks,
		Terran_Academy,
		Terran_Factory,
		Terran_Starport,
		Terran_Control_Tower,
		Terran_Science_Facility,
		Terran_Covert_Ops,
		Terran_Physics_Lab,
		Undefined119,
		Terran_Machine_Shop,
		Undefined121, // Repair Bay (Unused)
		Terran_Engineering_Bay,
		Terran_Armory,
		Terran_Missile_Turret,
		Terran_Bunker,
		Special_Crashed_Norad_II,
		Special_Ion_Cannon,
		Powerup_Uraj_Crystal,
		Powerup_Khalis_Crystal,
		Zerg_Infested_Command_Center,
		Zerg_Hatchery,
		Zerg_Lair,
		Zerg_Hive,
		Zerg_Nydus_Canal,
		Zerg_Hydralisk_Den,
		Zerg_Defiler_Mound,
		Zerg_Greater_Spire,
		Zerg_Queens_Nest,
		Zerg_Evolution_Chamber,
		Zerg_Ultralisk_Cavern,
		Zerg_Spire,
		Zerg_Spawning_Pool,
		Zerg_Creep_Colony,
		Zerg_Spore_Colony,
		Undefined145, // Unused Zerg Building 1
		Zerg_Sunken_Colony,
		Special_Overmind_With_Shell,
		Special_Overmind,
		Zerg_Extractor,
		Special_Mature_Chrysalis,
		Special_Cerebrate,
		Special_Cerebrate_Daggoth,
		Undefined153, // Unused Zerg Building 2
		Protoss_Nexus,
		Protoss_Robotics_Facility,
		Protoss_Pylon,
		Protoss_Assimilator,
		Undefined158, // Unused Protoss Building 1
		Protoss_Observatory,
		Protoss_Gateway,
		Undefined161, // Unused Protoss Building 2
		Protoss_Photon_Cannon,
		Protoss_Citadel_of_Adun,
		Protoss_Cybernetics_Core,
		Protoss_Templar_Archives,
		Protoss_Forge,
		Protoss_Stargate,
		Special_Stasis_Cell_Prison,
		Protoss_Fleet_Beacon,
		Protoss_Arbiter_Tribunal,
		Protoss_Robotics_Support_Bay,
		Protoss_Shield_Battery,
		Special_Khaydarin_Crystal_Form,
		Special_Protoss_Temple,
		Special_XelNaga_Temple,
		Resource_Mineral_Field,
		Resource_Mineral_Field_Type_2,
		Resource_Mineral_Field_Type_3,
		Undefined179, // Cave (Unused)
		Undefined180, // Cave-in (Unused)
		Undefined181, // Cantina (Unused)
		Undefined182, // Mining Platform (Unused)
		Undefined183, // Independent Command Center (Unused)
		Undefined184, // Independent Starport (Unused)
		Undefined185, // Independent Jump Gate (Unused)
		Undefined186, // Ruins (Unused)
		Undefined187, // Khaydarin Crystal Formation (Unused)
		Resource_Vespene_Geyser,
		Special_Warp_Gate,
		Special_Psi_Disrupter,
		Undefined191, // Zerg Marker
		Undefined192, // Terran Marker
		Undefined193, // Protoss Marker
		Special_Zerg_Beacon,
		Special_Terran_Beacon,
		Special_Protoss_Beacon,
		Special_Zerg_Flag_Beacon,
		Special_Terran_Flag_Beacon,
		Special_Protoss_Flag_Beacon,
		Special_Power_Generator,
		Special_Overmind_Cocoon,
		Spell_Dark_Swarm,
		Undefined203, // Floor Missile Trap
		Undefined204, // Floor Hatch (Unused)
		Undefined205, // Left Upper Level Door
		Undefined206, // Right Upper Level Door
		Undefined207, // Left Pit Door
		Undefined208, // Right Pit Door
		Undefined209, // Floor Gun Trap
		Undefined210, // Left Wall Missile Trap
		Undefined211, // Left Wall Flame Trap
		Undefined212, // Right Wall Missile Trap
		Undefined213, // Right Wall Flame Trap
		Undefined214, // Start Location
		Powerup_Flag,
		Powerup_Young_Chrysalis,
		Powerup_Psi_Emitter,
		Powerup_Data_Disk,
		Powerup_Khaydarin_Crystal,
		Undefined220, // Mineral Cluster Type 1
		Undefined221, // Mineral Cluster Type 2
		Undefined222, // Protoss Vespene Gas Orb Type 1
		Undefined223, // Protoss Vespene Gas Orb Type 2
		Undefined224, // Zerg Vespene Gas Sac Type 1
		Undefined225, // Zerg Vespene Gas Sac Type 2
		Undefined226, // Terran Vespene Gas Tank Type 1
		Undefined227, // Terran Vespene Gas Tank Type 2
		None,
		Undefined229,
		Undefined230,
		Undefined231,
		Undefined232,
		Unknown;
		public int getID() {
			return ordinal();
		}
	}
	
	public UnitType(int[] data, int index) {
		ID = data[index++];
		raceID = data[index++];
		whatBuildID = data[index++];
		armorUpgradeID = data[index++];
		maxHitPoints = data[index++];
		maxShields = data[index++];
		maxEnergy = data[index++];
		armor = data[index++];
		mineralPrice = data[index++];
		gasPrice = data[index++];
		buildTime = data[index++];
		supplyRequired = data[index++];
		supplyProvided = data[index++];
		spaceRequired = data[index++];
		spaceProvided = data[index++];
		buildScore = data[index++];
		destroyScore = data[index++];
		sizeID = data[index++];
		tileWidth = data[index++];
		tileHeight = data[index++];
		dimensionLeft = data[index++];
		dimensionUp = data[index++];
		dimensionRight = data[index++];
		dimensionDown = data[index++];
		seekRange = data[index++];
		sightRange = data[index++];
		groundWeaponID = data[index++];
		maxGroundHits = data[index++];
		airWeaponID = data[index++];
		maxAirHits = data[index++];
		topSpeed = data[index++] / fixedScale;
		acceleration = data[index++];
		haltDistance = data[index++];
		turnRadius = data[index++];
		produceCapable = data[index++] == 1;
		attackCapable = data[index++] == 1;
		canMove = data[index++] == 1;
		flyer = data[index++] == 1;
		regenerates = data[index++] == 1;
		spellcaster = data[index++] == 1;
		invincible = data[index++] == 1;
		organic = data[index++] == 1;
		mechanical = data[index++] == 1;
		robotic = data[index++] == 1;
		detector = data[index++] == 1;
		resourceContainer = data[index++] == 1;
		refinery = data[index++] == 1;
		worker = data[index++] == 1;
		requiresPsi = data[index++] == 1;
		requiresCreep = data[index++] == 1;
		burrowable = data[index++] == 1;
		cloakable = data[index++] == 1;
		building = data[index++] == 1;
		addon = data[index++] == 1;
		flyingBuilding = data[index++] == 1;
		spell = data[index++] == 1;
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
	
	public int getWhatBuildID() {
		return whatBuildID;
	}
	
	public int getArmorUpgradeID() {
		return armorUpgradeID;
	}
	
	public int getMaxHitPoints() {
		return maxHitPoints;
	}
	
	public int getMaxShields() {
		return maxShields;
	}
	
	public int getMaxEnergy() {
		return maxEnergy;
	}
	
	public int getArmor() {
		return armor;
	}
	
	public int getMineralPrice() {
		return mineralPrice;
	}
	
	public int getGasPrice() {
		return gasPrice;
	}
	
	public int getBuildTime() {
		return buildTime;
	}
	
	public int getSupplyRequired() {
		return supplyRequired;
	}
	
	public int getSupplyProvided() {
		return supplyProvided;
	}
	
	public int getSpaceRequired() {
		return spaceRequired;
	}
	
	public int getSpaceProvided() {
		return spaceProvided;
	}
	
	public int getBuildScore() {
		return buildScore;
	}
	
	public int getDestroyScore() {
		return destroyScore;
	}
	
	public int getSizeID() {
		return sizeID;
	}
	
	public int getTileWidth() {
		return tileWidth;
	}
	
	public int getTileHeight() {
		return tileHeight;
	}
	
	public int getDimensionLeft() {
		return dimensionLeft;
	}
	
	public int getDimensionUp() {
		return dimensionUp;
	}
	
	public int getDimensionRight() {
		return dimensionRight;
	}
	
	public int getDimensionDown() {
		return dimensionDown;
	}
	
	public int getSeekRange() {
		return seekRange;
	}
	
	public int getSightRange() {
		return sightRange;
	}
	
	public int getGroundWeaponID() {
		return groundWeaponID;
	}
	
	public int getMaxGroundHits() {
		return maxGroundHits;
	}
	
	public int getAirWeaponID() {
		return airWeaponID;
	}
	
	public int getMaxAirHits() {
		return maxAirHits;
	}
	
	public double getTopSpeed() {
		return topSpeed;
	}
	
	public int getAcceleration() {
		return acceleration;
	}
	
	public int getHaltDistance() {
		return haltDistance;
	}
	
	public int getTurnRadius() {
		return turnRadius;
	}
	
	public boolean isProduceCapable() {
		return produceCapable;
	}
	
	public boolean isAttackCapable() {
		return attackCapable;
	}
	
	public boolean isCanMove() {
		return canMove;
	}
	
	public boolean isFlyer() {
		return flyer;
	}
	
	public boolean isRegenerates() {
		return regenerates;
	}
	
	public boolean isSpellcaster() {
		return spellcaster;
	}
	
	public boolean isInvincible() {
		return invincible;
	}
	
	public boolean isOrganic() {
		return organic;
	}
	
	public boolean isMechanical() {
		return mechanical;
	}
	
	public boolean isRobotic() {
		return robotic;
	}
	
	public boolean isDetector() {
		return detector;
	}
	
	public boolean isResourceContainer() {
		return resourceContainer;
	}
	
	public boolean isRefinery() {
		return refinery;
	}
	
	public boolean isWorker() {
		return worker;
	}
	
	public boolean isRequiresPsi() {
		return requiresPsi;
	}
	
	public boolean isRequiresCreep() {
		return requiresCreep;
	}
	
	public boolean isBurrowable() {
		return burrowable;
	}
	
	public boolean isCloakable() {
		return cloakable;
	}
	
	public boolean isBuilding() {
		return building;
	}
	
	public boolean isAddon() {
		return addon;
	}
	
	public boolean isFlyingBuilding() {
		return flyingBuilding;
	}
	
	public boolean isSpell() {
		return spell;
	}
}
