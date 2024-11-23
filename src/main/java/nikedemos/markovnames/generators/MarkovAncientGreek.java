package nikedemos.markovnames.generators;

import nikedemos.markovnames.Main;
import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovAncientGreek extends MarkovGenerator {
    public MarkovDictionary femaleDictionary;

    public MarkovAncientGreek(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionary("ancient_greek_male.txt", sequenceLen, random);
        this.femaleDictionary = new MarkovDictionary("ancient_greek_female.txt", sequenceLen, random);
    }

    public MarkovAncientGreek(int sequenceLen) {
        this(sequenceLen, new Random());
    }

    public MarkovAncientGreek() {
        this(3); // 3 seems best-suited for Welsh
    }

    @Override
    public String fetch(int gender) {
        if (gender == Main.GENDER_RANDOM)
            gender = random.nextBoolean() ? Main.GENDER_MALE : Main.GENDER_FEMALE;

        return (gender == Main.GENDER_FEMALE) ? femaleDictionary.generateWord() : surnameDictionary.generateWord();
    }
}
