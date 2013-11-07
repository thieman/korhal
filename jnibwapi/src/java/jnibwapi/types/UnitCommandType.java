package jnibwapi.types;

/**
 * Represents a StarCraft unit command type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/UnitCommandType
 */
public class UnitCommandType {
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	public enum UnitCommandTypes {
		// Attack_Move - corresponds to Unit::attack
		Attack_Move,
		// Attack_Unit - corresponds to Unit::attack
		Attack_Unit,
		// Build - corresponds to Unit::build
		Build,
		// Build_Addon - corresponds to Unit::buildAddon
		Build_Addon,
		// Train - corresponds to Unit::train
		Train,
		// Morph - corresponds to Unit::morph
		Morph,
		// Research - corresponds to Unit::research
		Research,
		// Upgrade - corresponds to Unit::upgrade
		Upgrade,
		// Set_Rally_Position - corresponds to Unit::setRallyPoint
		Set_Rally_Position,
		// Set_Rally_Unit - corresponds to Unit::setRallyPoint
		Set_Rally_Unit,
		// Move - corresponds to Unit::move
		Move,
		// Patrol - corresponds to Unit::patrol
		Patrol,
		// Hold_Position - corresponds to Unit::holdPosition
		Hold_Position,
		// Stop - corresponds to Unit::stop
		Stop,
		// Follow - corresponds to Unit::follow
		Follow,
		// Gather - corresponds to Unit::gather
		Gather,
		// Return_Cargo - corresponds to Unit::returnCargo
		Return_Cargo,
		// Repair - corresponds to Unit::repair
		Repair,
		// Burrow - corresponds to Unit::burrow
		Burrow,
		// Unburrow - corresponds to Unit::unburrow
		Unburrow,
		// Cloak - corresponds to Unit::cloak
		Cloak,
		// Decloak - corresponds to Unit::decloak
		Decloak,
		// Siege - corresponds to Unit::siege
		Siege,
		// Unsiege - corresponds to Unit::unsiege
		Unsiege,
		// Lift - corresponds to Unit::lift
		Lift,
		// Land - corresponds to Unit::land
		Land,
		// Load - corresponds to Unit::load
		Load,
		// Unload - corresponds to Unit::unload
		Unload,
		// Unload_All - corresponds to Unit::unloadAll
		Unload_All,
		// Unload_All_Position - corresponds to Unit::unloadAll
		Unload_All_Position,
		// Right_Click_Position - corresponds to Unit::rightClick
		Right_Click_Position,
		// Right_Click_Unit - corresponds to Unit::rightClick
		Right_Click_Unit,
		// Halt_Construction - corresponds to Unit::haltConstruction
		Halt_Construction,
		// Cancel_Construction - corresponds to Unit::cancelConstruction
		Cancel_Construction,
		// Cancel_Addon - corresponds to Unit::cancelAddon
		Cancel_Addon,
		// Cancel_Train - corresponds to Unit::cancelTrain
		Cancel_Train,
		// Cancel_Train_Slot - corresponds to Unit::cancelTrain
		Cancel_Train_Slot,
		// Cancel_Morph - corresponds to Unit::cancelMorph
		Cancel_Morph,
		// Cancel_Research - corresponds to Unit::cancelResearch
		Cancel_Research,
		// Cancel_Upgrade - corresponds to Unit::cancelUpgrade
		Cancel_Upgrade,
		// Use_Tech - corresponds to Unit::useTech
		Use_Tech,
		// Use_Tech_Position - corresponds to Unit::useTech
		Use_Tech_Position,
		// Use_Tech_Unit - corresponds to Unit::useTech
		Use_Tech_Unit,
		// Place a flag from a beacon
		Place_COP,
		// None
		None,
		// Unknown
		Unknown;
		public int getID() {
			return ordinal();
		}
	}
	
	public UnitCommandType(int[] data, int index) {
		ID = data[index++];
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
}
