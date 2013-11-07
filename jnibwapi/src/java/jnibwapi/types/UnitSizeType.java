package jnibwapi.types;

/**
 * Represents a StarCraft unit size type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/UnitSizeType
 */
public class UnitSizeType {
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	public enum UnitSizeTypes {
		Independent,
		Small,
		Medium,
		Large,
		None,
		Unknown;
		public int getID() {
			return ordinal();
		}
	}
	
	public UnitSizeType(int[] data, int index) {
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
