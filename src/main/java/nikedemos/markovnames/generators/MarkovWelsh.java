package nikedemos.markovnames.generators;

import nikedemos.markovnames.Main;
import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovWelsh extends MarkovGenerator {

    public MarkovDictionary femaleDictionary;

    public MarkovWelsh(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionary("welsh_male.txt", sequenceLen, random);
        this.femaleDictionary = new MarkovDictionary("welsh_female.txt", sequenceLen, random);
    }

    public MarkovWelsh(int sequenceLen) {
        this(sequenceLen, new Random());

    }

    public MarkovWelsh() {
        this(3); // 3 seems best-suited for Welsh
    }

    @Override
    public String fetch(int gender) {
        if (gender == Main.GENDER_RANDOM)
            gender = random.nextBoolean() ? Main.GENDER_MALE : Main.GENDER_FEMALE;

        return gender == Main.GENDER_FEMALE ? femaleDictionary.generateWord() : surnameDictionary.generateWord();
    }
}
