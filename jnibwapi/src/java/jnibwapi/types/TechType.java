package jnibwapi.types;

/**
 * Represents a StarCraft tech (research) type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/TechType
 */
public class TechType {
	
	public static final int numAttributes = 10;
	
	private String name;
	private int ID;
	private int raceID;
	private int mineralPrice;
	private int gasPrice;
	private int researchTime;
	private int energyUsed;
	private int whatResearchesTypeID;
	private int getWeaponID;
	private boolean targetsUnits;
	private boolean targetsPosition;
	
	public enum TechTypes {
		Stim_Packs,
		Lockdown,
		EMP_Shockwave,
		Spider_Mines,
		Scanner_Sweep,
		Tank_Siege_Mode,
		Defensive_Matrix,
		Irradiate,
		Yamato_Gun,
		Cloaking_Field,
		Personnel_Cloaking,
		Burrowing,
		Infestation,
		Spawn_Broodlings,
		Dark_Swarm,
		Plague,
		Consume,
		Ensnare,
		Parasite,
		Psionic_Storm,
		Hallucination,
		Recall,
		Stasis_Field,
		Archon_Warp,
		Restoration,
		Disruption_Web,
		Undefined26,
		Mind_Control,
		Dark_Archon_Meld,
		Feedback,
		Optical_Flare,
		Maelstrom,
		Lurker_Aspect,
		Undefined33,
		Healing,
		Undefined35,
		Undefined36,
		Undefined37,
		Undefined38,
		Undefined39,
		Undefined40,
		Undefined41,
		Undefined42,
		Undefined43,
		None,
		Unknown,
		Nuclear_Strike;
		public int getID() {
			return ordinal();
		}
	}
	
	public TechType(int[] data, int index) {
		ID = data[index++];
		raceID = data[index++];
		mineralPrice = data[index++];
		gasPrice = data[index++];
		researchTime = data[index++];
		energyUsed = data[index++];
		whatResearchesTypeID = data[index++];
		getWeaponID = data[index++];
		targetsUnits = data[index++] == 1;
		targetsPosition = data[index++] == 1;
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
	
	public int getMineralPrice() {
		return mineralPrice;
	}
	
	public int getGasPrice() {
		return gasPrice;
	}
	
	public int getResearchTime() {
		return researchTime;
	}
	
	public int getEnergyUsed() {
		return energyUsed;
	}
	
	public int getWhatResearchesTypeID() {
		return whatResearchesTypeID;
	}
	
	public int getGetWeaponID() {
		return getWeaponID;
	}
	
	public boolean isTargetsUnits() {
		return targetsUnits;
	}
	
	public boolean isTargetsPosition() {
		return targetsPosition;
	}
}
