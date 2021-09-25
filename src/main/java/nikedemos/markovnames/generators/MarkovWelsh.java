package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovWelsh extends MarkovGenerator {

	public MarkovDictionary markov2;

	public MarkovWelsh(int seqlen, Random rng) {
		this.rng = rng;
		this.markov = new MarkovDictionary("welsh_male.txt", seqlen, rng);
		this.markov2 = new MarkovDictionary("welsh_female.txt", seqlen, rng);
	}

	public MarkovWelsh(int seqlen) {
		this(seqlen, new Random());

	}

	public MarkovWelsh() {
		this(3, new Random()); // 3 seems best-suited for Welsh
	}

	@Override
	public String fetch(int gender) {

		String seq1;

		// check the gender.
		// 0 = random gender, 1 = male, 2 = female
		// if there's no gender specified (0),
		// now it's time to pick it at random
		//
		if (gender == 0) {
			gender = rng.nextBoolean() ? 1 : 2;
		}

		// now if it's 2 - a lady - feminize the 3 sequences
		if (gender == 2) {
			seq1 = markov2.generateWord();
		} 
		else {
			seq1 = markov.generateWord();
		}

		return seq1;
	}
}
