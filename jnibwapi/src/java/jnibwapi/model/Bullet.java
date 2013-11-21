package jnibwapi.model;

/**
 * Represents a StarCraft bullet. Addition to JNIBWAPI by Travis Thieman (@thieman).
 */
public class Bullet {

	public static final int numAttributes = 17;
	public static final double TO_DEGREES = 180.0 / Math.PI;
	public static final double fixedScale = 100.0;

	private int ID;
	private int playerID;
	private int typeID;
	private int sourceID;
	private int positionX;
	private int positionY;
	private boolean positionValid;
	private double angle;
	private double velocityX;
	private double velocityY;
	private int targetID;
	private int targetPositionX;
	private int targetPositionY;
	private boolean targetPositionValid;
	private int removeTimer;
	private boolean exists;
	private boolean visible;

	public Bullet(int ID) {
		this.ID = ID;
	}

	public void update(int[] data, int index) {
		index++;
		playerID = data[index++];
		typeID = data[index++];
		sourceID = data[index++];
		positionX = data[index++];
		positionY = data[index++];
		positionValid = data[index++] == 1;
		angle = data[index++] / TO_DEGREES;
		velocityX = data[index++] / fixedScale;
		velocityY = data[index++] / fixedScale;
		targetID = data[index++];
		targetPositionX = data[index++];
		targetPositionY = data[index++];
		targetPositionValid = data[index++] == 1;
		removeTimer = data[index++];
		exists = data[index++] == 1;
		visible = data[index++] == 1;
	}

	public int getID() {
		return ID;
	}

	public int getPlayer() {
		return playerID;
	}

	public int getType() {
		return typeID;
	}

	public int getSourceUnit() {
		return sourceID;
	}

	public int getPositionX() {
		return positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public boolean getPositionValid() {
		return positionValid;
	}

	public double getAngle() {
		return angle;
	}

	public double getVelocityX() {
		return velocityX;
	}

	public double getVelocityY() {
		return velocityY;
	}

	public int getTargetID() {
		return targetID;
	}

	public int getTargetPositionX() {
		return targetPositionX;
	}

	public int getTargetPositionY() {
		return targetPositionY;
	}

	public boolean getTargetPositionValid() {
		return targetPositionValid;
	}

	public int getRemoveTimer() {
		return removeTimer;
	}

	public boolean getExists() {
		return exists;
	}

	public boolean getVisible() {
		return visible;
	}

}
