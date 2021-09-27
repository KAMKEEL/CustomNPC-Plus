package noppes.npcs.util;

public class ValueUtil {
	public static float correctFloat(float given, float min, float max){
		if(given < min)
			return min;
		return Math.min(given, max);
	}

	public static int CorrectInt(int given, int min, int max) {
		if(given < min)
			return min;
		return Math.min(given, max);
	}
}
