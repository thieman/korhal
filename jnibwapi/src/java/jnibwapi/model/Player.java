package jnibwapi.model;

import java.awt.Point;

/**
 * Represents a StarCraft player.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/Player
 */
public class Player {
	
	public static final int numAttributes = 11;
	
	private final int ID;
	private final int raceID;
	private final int typeID;
	private final int startLocationX;
	private final int startLocationY;
	private final boolean self;
	private final boolean ally;
	private final boolean enemy;
	private final boolean neutral;
	private final boolean observer;
	private final int color;
	private final String name;
	
	private int minerals;
	private int gas;
	private int supplyUsed;
	private int supplyTotal;
	private int cumulativeMinerals;
	private int cumulativeGas;
	private int unitScore;
	private int killScore;
	private int buildingScore;
	private int razingScore;
	
	private boolean[] researching = null;
	private boolean[] researched = null;
	private boolean[] upgrading = null;
	private int[] upgradeLevel = null;
	
	public Player(int[] data, int index, String name) {
		ID = data[index++];
		raceID = data[index++];
		typeID = data[index++];
		startLocationX = data[index++];
		startLocationY = data[index++];
		self = (data[index++] == 1);
		ally = (data[index++] == 1);
		enemy = (data[index++] == 1);
		neutral = (data[index++] == 1);
		observer = (data[index++] == 1);
		color = data[index++];
		this.name = name;
	}
	
	public void update(int[] data) {
		int index = 0;
		minerals = data[index++];
		gas = data[index++];
		supplyUsed = data[index++];
		supplyTotal = data[index++];
		cumulativeMinerals = data[index++];
		cumulativeGas = data[index++];
		unitScore = data[index++];
		killScore = data[index++];
		buildingScore = data[index++];
		razingScore = data[index++];
	}
	
	public void updateResearch(int[] researchData, int[] upgradeData) {
		researched = new boolean[researchData.length / 2];
		researching = new boolean[researchData.length / 2];
		
		for (int i = 0; i < researchData.length; i += 2) {
			researched[i / 2] = (researchData[i] == 1);
			researching[i / 2] = (researchData[i + 1] == 1);
		}
		
		upgradeLevel = new int[upgradeData.length / 2];
		upgrading = new boolean[upgradeData.length / 2];
		
		for (int i = 0; i < upgradeData.length; i += 2) {
			upgradeLevel[i / 2] = upgradeData[i];
			upgrading[i / 2] = (upgradeData[i + 1] == 1);
		}
	}
	
	public int getID() {
		return ID;
	}
	
	public int getRaceID() {
		return raceID;
	}
	
	public int getTypeID() {
		return typeID;
	}
	
	/**
	 * Returns the starting tile position of the Player, or null if unknown (eg. for enemy players
	 * without complete map information).
	 */
	public Point getStartLocation() {
		if (startLocationX == 1000) {
			return null; // In the case of Invalid/None/Unknown TilePosition
		}
		return new Point(startLocationX, startLocationY);
	}
	
	public boolean isSelf() {
		return self;
	}
	
	public boolean isAlly() {
		return ally;
	}
	
	public boolean isEnemy() {
		return enemy;
	}
	
	public boolean isNeutral() {
		return neutral;
	}
	
	public boolean isObserver() {
		return observer;
	}
	
	public int getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}
	
	public int getMinerals() {
		return minerals;
	}
	
	public int getGas() {
		return gas;
	}
	
	public int getSupplyUsed() {
		return supplyUsed;
	}
	
	public int getSupplyTotal() {
		return supplyTotal;
	}
	
	public int getCumulativeMinerals() {
		return cumulativeMinerals;
	}
	
	public int getCumulativeGas() {
		return cumulativeGas;
	}
	
	public int getUnitScore() {
		return unitScore;
	}
	
	public int getKillScore() {
		return killScore;
	}
	
	public int getBuildingScore() {
		return buildingScore;
	}
	
	public int getRazingScore() {
		return razingScore;
	}
	
	public boolean hasResearched(int techID) {
		return (researched != null && techID < researched.length) ? researched[techID] : false;
	}
	
	public boolean isResearching(int techID) {
		return (researching != null && techID < researching.length) ? researching[techID] : false;
	}
	
	public int upgradeLevel(int upgradeID) {
		return (upgradeLevel != null && upgradeID < upgradeLevel.length) ?
				upgradeLevel[upgradeID] : 0;
	}
	
	public boolean isUpgrading(int upgradeID) {
		return (upgrading != null && upgradeID < upgrading.length) ? upgrading[upgradeID] : false;
	}
}
