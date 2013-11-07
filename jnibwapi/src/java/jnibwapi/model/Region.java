package jnibwapi.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Represents a region in a StarCraft map.
 * 
 * For a description of fields see: http://code.google.com/p/bwta/wiki/Region
 */
public class Region {
	
	public static final int numAttributes = 3;
	
	private int ID;
	private int centerX;
	private int centerY;
	private int[] coordinates;
	private Set<Region> connectedRegions = new HashSet<>();
	private Set<ChokePoint> chokePoints = new HashSet<>();
	private Set<Region> allConnectedRegions = null;
	
	public Region(int[] data, int index) {
		ID = data[index++];
		centerX = data[index++];
		centerY = data[index++];
	}
	
	public int getID() {
		return ID;
	}
	
	public int getCenterX() {
		return centerX;
	}
	
	public int getCenterY() {
		return centerY;
	}
	
	protected void setCoordinates(int[] coordinates) {
		this.coordinates = coordinates;
	}
	
	public int[] getCoordinates() {
		return Arrays.copyOf(coordinates, coordinates.length);
	}
	
	protected void addChokePoint(ChokePoint chokePoint) {
		chokePoints.add(chokePoint);
	}
	
	public Set<ChokePoint> getChokePoints() {
		return Collections.unmodifiableSet(chokePoints);
	}
	
	protected void addConnectedRegion(Region other) {
		connectedRegions.add(other);
	}
	
	public Set<Region> getConnectedRegions() {
		return Collections.unmodifiableSet(connectedRegions);
	}
	
	/** Get all transitively connected regions for a given region */
	public Set<Region> getAllConnectedRegions() {
		// Evaluate on first call
		if (allConnectedRegions == null) {
			allConnectedRegions = new HashSet<Region>();
			LinkedList<Region> unexplored = new LinkedList<Region>();
			unexplored.add(this);
			while (!unexplored.isEmpty()) {
				Region current = unexplored.remove();
				if (allConnectedRegions.add(current)) {
					unexplored.addAll(current.getConnectedRegions());
				}
			}
		}
		return Collections.unmodifiableSet(allConnectedRegions);
	}
	
}
