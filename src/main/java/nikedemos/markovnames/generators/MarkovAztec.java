package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovAztec extends MarkovGenerator {

    public MarkovAztec(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionary("aztec_given.txt", sequenceLen, random);
    }

    public MarkovAztec(int sequenceLen) {
        this(sequenceLen, new Random());
    }

    public MarkovAztec() {
        this(3); // 3 seems best-suited for Aztec
    }

    @Override
    public String fetch(int gender) {
        // Aztec names are genderless
        return surnameDictionary.generateWord();
    }
}
