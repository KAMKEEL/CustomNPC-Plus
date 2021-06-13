package nikedemos.markovnames;

import java.util.Random;

//used exclusively by the Spanish surname dictionary - because the capitalization is a little bit different
public class MarkovDictionarySPA extends MarkovDictionary {

	public MarkovDictionarySPA(String dictionary, int seqlen, Random rng) {
		super(dictionary, seqlen, rng);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getPost(String str) {
		return getCapitalizedSPA(str);
	}

	public String getCapitalizedSPA(String str) {
		// for spanish capitalization

		String[] parts = str.split("#");

		StringBuilder build = new StringBuilder("");

		for (int p = 0; p < parts.length; p++) {
			// capitalize this part, but only if it's not "de", "del", "la" or "los"
			if (!parts[p].equals("de") && !parts[p].equals("del") && !parts[p].equals("la")
					&& !parts[p].equals("los")) {
				parts[p] = getCapitalized(parts[p]);
			}

			if (p > 0) // don't add spaces before the first part
				build.append(" ");

			build.append(parts[p]);
		}

		return build.toString();
	}

}
