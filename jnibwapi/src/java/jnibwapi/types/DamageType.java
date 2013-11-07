package jnibwapi.types;

/**
 * Represents a StarCraft damage type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/DamageType
 */
public class DamageType {
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	public enum DamageTypes {
		Independent,
		Explosive,
		Concussive,
		Normal,
		Ignore_Armor,
		None,
		Unknown;
		public int getID() {
			return ordinal();
		}
	}
	
	public DamageType(int[] data, int index) {
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
