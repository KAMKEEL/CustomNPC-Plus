package nikedemos.markovnames;

import nikedemos.markovnames.generators.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Main {
	
	public static final int GENDER_RANDOM=0;
	public static final int GENDER_MALE=1;
	public static final int GENDER_FEMALE=2;
	
	public static HashMap<String, MarkovGenerator> GENERATORS = new HashMap<String, MarkovGenerator>();
	
	public static void main(String[] args){
		
		//all the generators neatly in one HashMap, so you can iterate or w/e
		
		GENERATORS.put("ROMAN", new MarkovRoman(3));
		GENERATORS.put("JAPANESE", new MarkovJapanese(4));
		GENERATORS.put("SLAVIC", new MarkovSlavic(3));
		GENERATORS.put("WELSH", new MarkovWelsh(3));
		GENERATORS.put("SAAMI", new MarkovSaami(3));
		GENERATORS.put("OLDNORSE", new MarkovOldNorse(4));
		GENERATORS.put("ANCIENTGREEK", new MarkovAncientGreek(3));
		GENERATORS.put("AZTEC", new MarkovAztec(3));
		
		//iterate through generators, generate 5 male and 5 female names, move on to the next

		for (Entry<String, MarkovGenerator> pair : GENERATORS.entrySet()) {
			System.out.println("===" + pair.getKey() + "===");

			for (int i = 0; i < 16; i++) {
				if (i == 0)
					System.out.println("GENTLEMEN-----------");
				int gender = i < 8 ? 1 : 2;
				String random_name = pair.getValue().fetch(gender);
				System.out.println(random_name);

				if (i == 15) //extra padding
					System.out.println("\n");
				else if (i == 7) //separate genders
					System.out.println("LADIES--------------");
			}
		}
	}
}
