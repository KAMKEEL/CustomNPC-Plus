package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovSaami extends MarkovGenerator {

    public MarkovSaami(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionary("saami_bothgenders.txt", sequenceLen, random);
    }

    public MarkovSaami(int sequenceLen) {
        this(sequenceLen, new Random());
    }

    public MarkovSaami() {
        this(3); // 3 seems best-suited for Saami
    }

    @Override
    public String fetch(int gender) {
        // Saami names are genderless
        return surnameDictionary.generateWord();
    }
}
