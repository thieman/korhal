package jnibwapi.types;

/**
 * Represents a StarCraft race type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/Race
 */
public class RaceType {
	
	public static final int numAttributes = 6;
	
	private String name;
	private int ID;
	private int workerID;
	private int centerID;
	private int refineryID;
	private int transportID;
	private int supplyProviderID;
	
	public enum RaceTypes {
		Zerg,
		Terran,
		Protoss,
		// NOTE: Changes in BWAPI4 to:
		// Unused = 3,4,5, Random = 6, None = 7, Unknown = 8
		Random,
		Other,
		None,
		Unknown;
		public int getID() {
			return ordinal();
		}
	}
	
	public RaceType(int[] data, int index) {
		ID = data[index++];
		workerID = data[index++];
		centerID = data[index++];
		refineryID = data[index++];
		transportID = data[index++];
		supplyProviderID = data[index++];
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
	
	public int getWorkerID() {
		return workerID;
	}
	
	public int getCenterID() {
		return centerID;
	}
	
	public int getRefineryID() {
		return refineryID;
	}
	
	public int getTransportID() {
		return transportID;
	}
	
	public int getSupplyProviderID() {
		return supplyProviderID;
	}
}
