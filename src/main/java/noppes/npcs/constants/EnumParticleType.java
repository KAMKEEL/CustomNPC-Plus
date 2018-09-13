package noppes.npcs.constants;

public enum EnumParticleType {
	None(""), 
	Smoke("smoke"), 
	Portal("portal"), 
	Redstone("reddust"), 
	Lightning("magicCrit"), 
	LargeSmoke("largesmoke"), 
	Magic("witchMagic"), 
	Enchant("enchantmenttable"), 
	Crit("crit");
	
	public String particleName;
	EnumParticleType(String name){
		this.particleName = name;
	}
}