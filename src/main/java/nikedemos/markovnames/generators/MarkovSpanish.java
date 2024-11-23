package nikedemos.markovnames.generators;

import nikedemos.markovnames.Main;
import nikedemos.markovnames.MarkovDictionary;
import nikedemos.markovnames.MarkovDictionarySPA;

import java.util.Random;

public class MarkovSpanish extends MarkovGenerator {
    public MarkovDictionary femaleDictionary;
    public MarkovDictionary maleDictionary;

    public MarkovSpanish(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionarySPA("spanish_surnames.txt", sequenceLen, random);
        this.femaleDictionary = new MarkovDictionary("spanish_given_female.txt", sequenceLen, random);
        this.maleDictionary = new MarkovDictionary("spanish_given_male.txt", sequenceLen, random);
    }

    public MarkovSpanish(int sequenceLen) {
        this(sequenceLen, new Random());
    }

    public MarkovSpanish() {
        this(3);
    }

    @Override
    public String fetch(int gender) {
        if (gender == Main.GENDER_RANDOM)
            gender = random.nextBoolean() ? Main.GENDER_MALE : Main.GENDER_FEMALE;

        return (gender == Main.GENDER_MALE ? maleDictionary.generateWord() : femaleDictionary.generateWord()) + " " + surnameDictionary.generateWord();
    }
}
