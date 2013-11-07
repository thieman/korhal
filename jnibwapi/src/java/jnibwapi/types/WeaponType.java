package jnibwapi.types;

/**
 * Represents a StarCraft weapon type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/WeaponType
 */
public class WeaponType {
	
	public static final int numAttributes = 24;
	
	private String name;
	private int ID;
	private int techID;
	private int whatUsesTypeID;
	private int damageAmount;
	private int damageBonus;
	private int damageCooldown;
	private int damageFactor;
	private int upgradeTypeID;
	private int damageTypeID;
	private int explosionType;
	private int minRange;
	private int maxRange;
	private int innerSplashRadius;
	private int medianSplashRadius;
	private int outerSplashRadius;
	private boolean targetsAir;
	private boolean targetsGround;
	private boolean targetsMechanical;
	private boolean targetsOrganic;
	private boolean targetsNonBuilding;
	private boolean targetsNonRobotic;
	private boolean targetsTerrain;
	private boolean targetsOrgOrMech;
	private boolean targetsOwn;
	
	public enum WeaponTypes {
		Gauss_Rifle,
		Gauss_Rifle_Jim_Raynor,
		C_10_Canister_Rifle,
		C_10_Canister_Rifle_Sarah_Kerrigan,
		Fragmentation_Grenade,
		Fragmentation_Grenade_Jim_Raynor,
		Spider_Mines,
		Twin_Autocannons,
		Hellfire_Missile_Pack,
		Twin_Autocannons_Alan_Schezar,
		Hellfire_Missile_Pack_Alan_Schezar,
		Arclite_Cannon,
		Arclite_Cannon_Edmund_Duke,
		Fusion_Cutter,
		Undefined14,
		Gemini_Missiles,
		Burst_Lasers,
		Gemini_Missiles_Tom_Kazansky,
		Burst_Lasers_Tom_Kazansky,
		ATS_Laser_Battery,
		ATA_Laser_Battery,
		ATS_Laser_Battery_Hero,
		ATA_Laser_Battery_Hero,
		ATS_Laser_Battery_Hyperion,
		ATA_Laser_Battery_Hyperion,
		Flame_Thrower,
		Flame_Thrower_Gui_Montag,
		Arclite_Shock_Cannon,
		Arclite_Shock_Cannon_Edmund_Duke,
		Longbolt_Missile,
		Yamato_Gun,
		Nuclear_Strike,
		Lockdown,
		EMP_Shockwave,
		Irradiate,
		Claws,
		Claws_Devouring_One,
		Claws_Infested_Kerrigan,
		Needle_Spines,
		Needle_Spines_Hunter_Killer,
		Kaiser_Blades,
		Kaiser_Blades_Torrasque,
		Toxic_Spores,
		Spines,
		Undefined44,
		Undefined45,
		Acid_Spore,
		Acid_Spore_Kukulza,
		Glave_Wurm,
		Glave_Wurm_Kukulza,
		Undefined50,
		Undefined51,
		Seeker_Spores,
		Subterranean_Tentacle,
		Suicide_Infested_Terran,
		Suicide_Scourge,
		Parasite,
		Spawn_Broodlings,
		Ensnare,
		Dark_Swarm,
		Plague,
		Consume,
		Particle_Beam,
		Undefined63,
		Psi_Blades,
		Psi_Blades_Fenix,
		Phase_Disruptor,
		Phase_Disruptor_Fenix,
		Undefined68,
		Psi_Assault,
		Psionic_Shockwave,
		Psionic_Shockwave_Tassadar_Zeratul_Archon,
		Undefined72,
		Dual_Photon_Blasters,
		Anti_Matter_Missiles,
		Dual_Photon_Blasters_Mojo,
		Anti_Matter_Missiles_Mojo,
		Phase_Disruptor_Cannon,
		Phase_Disruptor_Cannon_Danimoth,
		Pulse_Cannon,
		STS_Photon_Cannon,
		STA_Photon_Cannon,
		Scarab,
		Stasis_Field,
		Psionic_Storm,
		Warp_Blades_Zeratul,
		Warp_Blades_Hero,
		Undefined87,
		Undefined88,
		Undefined89,
		Undefined90,
		Undefined91,
		Undefined92,
		Undefined93,
		Undefined94,
		Undefined95,
		Undefined96,
		Undefined97,
		Undefined98,
		Undefined99,
		Neutron_Flare,
		Disruption_Web,
		Restoration,
		Halo_Rockets,
		Corrosive_Acid,
		Mind_Control,
		Feedback,
		Optical_Flare,
		Maelstrom,
		Subterranean_Spines,
		Undefined110,
		Warp_Blades,
		C_10_Canister_Rifle_Samir_Duran,
		C_10_Canister_Rifle_Infested_Duran,
		Dual_Photon_Blasters_Artanis,
		Anti_Matter_Missiles_Artanis,
		C_10_Canister_Rifle_Alexei_Stukov,
		Undefined117,
		Undefined118,
		Undefined119,
		Undefined120,
		Undefined121,
		Undefined122,
		Undefined123,
		Undefined124,
		Undefined125,
		Undefined126,
		Undefined127,
		Undefined128,
		Undefined129,
		None,
		Unknown;
		public int getID() {
			return ordinal();
		}
	}
	
	public WeaponType(int[] data, int index) {
		ID = data[index++];
		techID = data[index++];
		whatUsesTypeID = data[index++];
		damageAmount = data[index++];
		damageBonus = data[index++];
		damageCooldown = data[index++];
		damageFactor = data[index++];
		upgradeTypeID = data[index++];
		damageTypeID = data[index++];
		explosionType = data[index++];
		minRange = data[index++];
		maxRange = data[index++];
		innerSplashRadius = data[index++];
		medianSplashRadius = data[index++];
		outerSplashRadius = data[index++];
		targetsAir = data[index++] == 1;
		targetsGround = data[index++] == 1;
		targetsMechanical = data[index++] == 1;
		targetsOrganic = data[index++] == 1;
		targetsNonBuilding = data[index++] == 1;
		targetsNonRobotic = data[index++] == 1;
		targetsTerrain = data[index++] == 1;
		targetsOrgOrMech = data[index++] == 1;
		targetsOwn = data[index++] == 1;
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
	
	public int getTechID() {
		return techID;
	}
	
	public int getWhatUsesTypeID() {
		return whatUsesTypeID;
	}
	
	public int getDamageAmount() {
		return damageAmount;
	}
	
	public int getDamageBonus() {
		return damageBonus;
	}
	
	public int getDamageCooldown() {
		return damageCooldown;
	}
	
	public int getDamageFactor() {
		return damageFactor;
	}
	
	public int getUpgradeTypeID() {
		return upgradeTypeID;
	}
	
	public int getDamageTypeID() {
		return damageTypeID;
	}
	
	public int getExplosionType() {
		return explosionType;
	}
	
	public int getMinRange() {
		return minRange;
	}
	
	public int getMaxRange() {
		return maxRange;
	}
	
	public int getInnerSplashRadius() {
		return innerSplashRadius;
	}
	
	public int getMedianSplashRadius() {
		return medianSplashRadius;
	}
	
	public int getOuterSplashRadius() {
		return outerSplashRadius;
	}
	
	public boolean isTargetsAir() {
		return targetsAir;
	}
	
	public boolean isTargetsGround() {
		return targetsGround;
	}
	
	public boolean isTargetsMechanical() {
		return targetsMechanical;
	}
	
	public boolean isTargetsOrganic() {
		return targetsOrganic;
	}
	
	public boolean isTargetsNonBuilding() {
		return targetsNonBuilding;
	}
	
	public boolean isTargetsNonRobotic() {
		return targetsNonRobotic;
	}
	
	public boolean isTargetsTerrain() {
		return targetsTerrain;
	}
	
	public boolean isTargetsOrgOrMech() {
		return targetsOrgOrMech;
	}
	
	public boolean isTargetsOwn() {
		return targetsOwn;
	}
}
