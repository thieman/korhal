package jnibwapi;

import java.util.HashSet;

import jnibwapi.model.Unit;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * Example Java AI Client using JNI-BWAPI.
 * 
 * Executes a 5-pool rush and cheats using perfect information.
 * 
 * Note: the agent often gets stuck when attempting to build the spawning pool. It works best on
 * maps where the overlord spawns with plenty of free space around it.
 */
public class ExampleAIClient implements BWAPIEventListener {
	
	/** reference to JNI-BWAPI */
	private JNIBWAPI bwapi;
	
	/** used for mineral splits */
	private HashSet<Integer> claimed = new HashSet<Integer>();
	
	/** has drone 5 been morphed */
	private boolean morphedDrone = false;
	
	/** has a drone been assigned to building a pool? */
	private int poolDrone = -1;
	
	/** when should the next overlord be spawned? */
	private int supplyCap = 0;
	
	/**
	 * Create a Java AI.
	 */
	public static void main(String[] args) {
		new ExampleAIClient();
	}
	
	/**
	 * Instantiates the JNI-BWAPI interface and connects to BWAPI.
	 */
	public ExampleAIClient() {
		bwapi = new JNIBWAPI(this);
		bwapi.start();
	}
	
	/**
	 * Connection to BWAPI established.
	 */
	@Override
	public void connected() {
		bwapi.loadTypeData();
	}
	
	/**
	 * Called at the beginning of a game.
	 */
	@Override
	public void gameStarted() {
		System.out.println("Game Started");
		
		bwapi.enableUserInput();
		bwapi.enablePerfectInformation();
		bwapi.setGameSpeed(0);
		bwapi.loadMapData(true);
		
		// reset agent state
		claimed.clear();
		morphedDrone = false;
		poolDrone = -1;
		supplyCap = 0;
	}
	
	/**
	 * Called each game cycle.
	 */
	@Override
	public void gameUpdate() {
		
		// spawn a drone
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Zerg_Larva.getID()) {
				if (bwapi.getSelf().getMinerals() >= 50 && !morphedDrone) {
					bwapi.morph(unit.getID(), UnitTypes.Zerg_Drone.getID());
					morphedDrone = true;
				}
			}
		}
		
		// collect minerals
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Zerg_Drone.getID()) {
				if (unit.isIdle() && unit.getID() != poolDrone) {
					
					for (Unit minerals : bwapi.getNeutralUnits()) {
						if (minerals.getTypeID() == UnitTypes.Resource_Mineral_Field.getID()
								&& !claimed.contains(minerals.getID())) {
							double distance = Math.sqrt(Math.pow(minerals.getX() - unit.getX(), 2)
									+ Math.pow(minerals.getY() - unit.getY(), 2));
							
							if (distance < 300) {
								bwapi.rightClick(unit.getID(), minerals.getID());
								claimed.add(minerals.getID());
								break;
							}
						}
					}
				}
			}
		}
		
		// build a spawning pool
		if (bwapi.getSelf().getMinerals() >= 200 && poolDrone < 0) {
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getTypeID() == UnitTypes.Zerg_Drone.getID()) {
					poolDrone = unit.getID();
					break;
				}
			}
			
			// build the pool under the overlord
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getTypeID() == UnitTypes.Zerg_Overlord.getID()) {
					bwapi.build(poolDrone, unit.getTileX(), unit.getTileY(),
							UnitTypes.Zerg_Spawning_Pool.getID());
				}
			}
		}
		
		// spawn overlords
		if (bwapi.getSelf().getSupplyUsed() + 2 >= bwapi.getSelf().getSupplyTotal()
				&& bwapi.getSelf().getSupplyTotal() > supplyCap) {
			if (bwapi.getSelf().getMinerals() >= 100) {
				for (Unit larva : bwapi.getMyUnits()) {
					if (larva.getTypeID() == UnitTypes.Zerg_Larva.getID()) {
						bwapi.morph(larva.getID(), UnitTypes.Zerg_Overlord.getID());
						supplyCap = bwapi.getSelf().getSupplyTotal();
					}
				}
			}
		}
		// spawn zerglings
		else if (bwapi.getSelf().getMinerals() >= 50) {
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getTypeID() == UnitTypes.Zerg_Spawning_Pool.getID() && unit.isCompleted()) {
					for (Unit larva : bwapi.getMyUnits()) {
						if (larva.getTypeID() == UnitTypes.Zerg_Larva.getID()) {
							bwapi.morph(larva.getID(), UnitTypes.Zerg_Zergling.getID());
						}
					}
				}
			}
		}
		
		// attack
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getTypeID() == UnitTypes.Zerg_Zergling.getID() && unit.isIdle()) {
				for (Unit enemy : bwapi.getEnemyUnits()) {
					bwapi.attack(unit.getID(), enemy.getX(), enemy.getY());
					break;
				}
			}
		}
	}

	public void gameEnded() {}
	public void keyPressed(int keyCode) {}
	public void matchEnded(boolean winner) {}
	public void sendText(String text) {}
	public void receiveText(String text) {}
	public void nukeDetect(int x, int y) {}
	public void nukeDetect() {}
	public void playerLeft(int playerID) {}
	public void unitCreate(int unitID) {}
	public void unitDestroy(int unitID) {}
	public void unitDiscover(int unitID) {}
	public void unitEvade(int unitID) {}
	public void unitHide(int unitID) {}
	public void unitMorph(int unitID) {}
	public void unitShow(int unitID) {}
	public void unitRenegade(int unitID) {}
	public void saveGame(String gameName) {}
	public void unitComplete(int unitID) {}
	public void playerDropped(int playerID) {}
}
