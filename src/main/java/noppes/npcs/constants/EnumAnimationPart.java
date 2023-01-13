package noppes.npcs.constants;

public enum EnumAnimationPart {
	HEAD ("HEAD"),
	BODY ("BODY"),
	RIGHT_ARM ("RARM"),
	LEFT_ARM ("LARM"),
	RIGHT_LEG ("RLEG"),
	LEFT_LEG ("LLEG"),
	FULL_MODEL ("MODEL");

	public final String partName;

	EnumAnimationPart(String name){
		this.partName = name;
	}

}
