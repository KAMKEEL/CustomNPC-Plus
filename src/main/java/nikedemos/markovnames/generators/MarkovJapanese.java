package nikedemos.markovnames.generators;

import nikedemos.markovnames.Main;
import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovJapanese extends MarkovGenerator {
    public MarkovDictionary maleDictionary;
    public MarkovDictionary femaleDictionary;

    public MarkovJapanese(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionary("japanese_surnames.txt", sequenceLen, random);
        this.maleDictionary = new MarkovDictionary("japanese_given_male.txt", sequenceLen, random);
        this.femaleDictionary = new MarkovDictionary("japanese_given_female.txt", sequenceLen, random);
    }

    public MarkovJapanese(int sequenceLen) {
        this(sequenceLen, new Random());

    }

    public MarkovJapanese() {
        this(4); // 4 seems best suited for Japanese
    }

    @Override
    public String fetch(int gender) {
        StringBuilder name = new StringBuilder(surnameDictionary.generateWord()).append(" ");

        if (gender == Main.GENDER_RANDOM)
            gender = random.nextBoolean() ? Main.GENDER_MALE : Main.GENDER_FEMALE;

        name.append(gender == Main.GENDER_FEMALE ? femaleDictionary.generateWord() : maleDictionary.generateWord());

        return name.toString();
    }
}
