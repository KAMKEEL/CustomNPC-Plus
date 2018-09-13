package noppes.npcs.constants;

public enum EnumAnimation {
	NONE,SITTING,LYING,SNEAKING,DANCING,AIMING,CRAWLING,HUG,CRY, WAVING, BOW;

	public int getWalkingAnimation() {
		if(this == SNEAKING)
			return 1;
		if(this == AIMING)
			return 2;
		if(this == DANCING)
			return 3;
		if(this == CRAWLING)
			return 4;
		if(this == HUG)
			return 5;
		return 0;
	}
	

}
