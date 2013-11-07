package jnibwapi.types;

/**
 * Represents a StarCraft bullet type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/BulletType
 */
public class BulletType {
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	public enum BulletTypes {
		Melee(0),
		Fusion_Cutter_Hit(141),
		Gauss_Rifle_Hit(142),
		C_10_Canister_Rifle_Hit(143),
		Gemini_Missiles(144),
		Fragmentation_Grenade(145),
		Longbolt_Missile(146),
		Undefined147(147),
		ATS_ATA_Laser_Battery(148),
		Burst_Lasers(149),
		Arclite_Shock_Cannon_Hit(150),
		EMP_Missile(151),
		Dual_Photon_Blasters_Hit(152),
		Particle_Beam_Hit(153),
		Anti_Matter_Missile(154),
		Pulse_Cannon(155),
		Psionic_Shockwave_Hit(156),
		Psionic_Storm(157),
		Yamato_Gun(158),
		Phase_Disruptor(159),
		STA_STS_Cannon_Overlay(160),
		Sunken_Colony_Tentacle(161),
		Acid_Spore(163),
		Glave_Wurm(165),
		Seeker_Spores(166),
		Queen_Spell_Carrier(167),
		Plague_Cloud(168),
		Consume(169),
		Needle_Spine_Hit(171),
		Invisible(172),
		Optical_Flare_Grenade(201),
		Halo_Rockets(202),
		Subterranean_Spines(203),
		Corrosive_Acid_Shot(204),
		Neutron_Flare(206),
		None(209),
		Unknown(210);
		
		private int id;
		
		private BulletTypes(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public BulletType(int[] data, int index) {
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
