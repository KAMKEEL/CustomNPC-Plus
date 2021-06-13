package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovJapanese extends MarkovGenerator {
	public MarkovDictionary markov2;
	public MarkovDictionary markov3;

	public MarkovJapanese(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("japanese_surnames.txt", seqlen, rng);
		this.markov2 = new MarkovDictionary("japanese_given_male.txt", seqlen, rng);
		this.markov3 = new MarkovDictionary("japanese_given_female.txt", seqlen, rng);
	}

	public MarkovJapanese(int seqlen) {
		this(seqlen, new Random());

	}

	public MarkovJapanese() {
		this(4, new Random()); // 4 seems best suited for Japanese
	}

	@Override
	public String fetch(int gender) {

		StringBuilder name = new StringBuilder(markov.generateWord());
		name.append(" ");

		// check the gender.
		// 0 = random gender, 1 = male, 2 = female
		// if there's no gender specified (0),
		// now it's time to pick it at random
		//
		if (gender == 0) {
			gender = rng.nextBoolean() == true ? 1 : 2;
		}

		if (gender == 2) {
			name.append(markov3.generateWord());
		} 
		else {
			name.append(markov2.generateWord());
		}

		return name.toString();
	}
}
