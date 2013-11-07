package jnibwapi.types;

/**
 * Represents a StarCraft order type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/Order
 */
public class OrderType {
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	public enum OrderTypes {
		Die(0),
		Stop(1),
		Guard(2),
		PlayerGuard(3),
		TurretGuard(4),
		BunkerGuard(5),
		Move(6),
		AttackUnit(10),
		AttackTile(12),
		Hover(13),
		AttackMove(14),
		InfestedCommandCenter(15),
		UnusedNothing(16),
		UnusedPowerup(17),
		TowerGuard(18),
		VultureMine(20),
		Nothing(23),
		Nothing3(24),
		CastInfestation(27),
		InfestingCommandCenter(29),
		PlaceBuilding(30),
		BuildProtoss2(32),
		ConstructingBuilding(33),
		Repair(34),
		PlaceAddon(36),
		BuildAddon(37),
		Train(38),
		RallyPointUnit(39),
		RallyPointTile(40),
		ZergBirth(41),
		ZergUnitMorph(42),
		ZergBuildingMorph(43),
		IncompleteBuilding(44),
		BuildNydusExit(46),
		EnterNydusCanal(47),
		Follow(49),
		Carrier(50),
		ReaverCarrierMove(51),
		CarrierIgnore2(55),
		Reaver(58),
		TrainFighter(63),
		InterceptorAttack(64),
		ScarabAttack(65),
		RechargeShieldsUnit(66),
		RechargeShieldsBattery(67),
		ShieldBattery(68),
		InterceptorReturn(69),
		BuildingLand(71),
		BuildingLiftOff(72),
		DroneLiftOff(73),
		LiftingOff(74),
		ResearchTech(75),
		Upgrade(76),
		Larva(77),
		SpawningLarva(78),
		Harvest1(79),
		Harvest2(80),
		MoveToGas(81), // Unit is moving to refinery
		WaitForGas(82), // Unit is waiting to enter the refinery (another unit is currently in it)
		HarvestGas(83), // Unit is in refinery
		ReturnGas(84), // Unit is returning gas to center
		MoveToMinerals(85), // Unit is moving to mineral patch
		WaitForMinerals(86), // Unit is waiting to use the mineral patch (another unit is currently
								// mining from it)
		MiningMinerals(87), // Unit is mining minerals from mineral patch
		Harvest3(88),
		Harvest4(89),
		ReturnMinerals(90), // Unit is returning minerals to center
		Interrupted(91),
		EnterTransport(92),
		PickupIdle(93),
		PickupTransport(94),
		PickupBunker(95),
		Pickup4(96),
		PowerupIdle(97),
		Sieging(98),
		Unsieging(99),
		InitCreepGrowth(101),
		SpreadCreep(102),
		StoppingCreepGrowth(103),
		GuardianAspect(104),
		ArchonWarp(105),
		CompletingArchonsummon(106),
		HoldPosition(107),
		Cloak(109),
		Decloak(110),
		Unload(111),
		MoveUnload(112),
		FireYamatoGun(113),
		CastLockdown(115),
		Burrowing(116),
		Burrowed(117),
		Unburrowing(118),
		CastDarkSwarm(119),
		CastParasite(120),
		CastSpawnBroodlings(121),
		CastEMPShockwave(122),
		NukeWait(123),
		NukeTrain(124),
		NukeLaunch(125),
		NukePaint(126),
		NukeUnit(127),
		CastNuclearStrike(128),
		NukeTrack(129),
		CloakNearbyUnits(131),
		PlaceMine(132),
		RightClickAction(133),
		CastRecall(137),
		TeleporttoLocation(138),
		CastScannerSweep(139),
		Scanner(140),
		CastDefensiveMatrix(141),
		CastPsionicStorm(142),
		CastIrradiate(143),
		CastPlague(144),
		CastConsume(145),
		CastEnsnare(146),
		CastStasisField(147),
		CastHallucination(148),
		Hallucination2(149),
		ResetCollision(150),
		Patrol(152),
		CTFCOPInit(153),
		CTFCOP1(154),
		CTFCOP2(155),
		ComputerAI(156),
		AtkMoveEP(157),
		HarassMove(158),
		AIPatrol(159),
		GuardPost(160),
		RescuePassive(161),
		Neutral(162),
		ComputerReturn(163),
		SelfDestrucing(165),
		Critter(166),
		HiddenGun(167),
		OpenDoor(168),
		CloseDoor(169),
		HideTrap(170),
		RevealTrap(171),
		Enabledoodad(172),
		Disabledoodad(173),
		Warpin(174),
		Medic(175),
		MedicHeal1(176),
		HealMove(177),
		MedicHeal2(179),
		CastRestoration(180),
		CastDisruptionWeb(181),
		CastMindControl(182),
		DarkArchonMeld(183),
		CastFeedback(184),
		CastOpticalFlare(185),
		CastMaelstrom(186),
		JunkYardDog(187),
		Fatal(188),
		None(189),
		Unknown(190);
		
		private int id;
		
		private OrderTypes(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public OrderType(int[] data, int index) {
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
