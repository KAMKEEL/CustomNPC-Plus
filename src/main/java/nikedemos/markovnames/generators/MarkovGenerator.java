package nikedemos.markovnames.generators;

import nikedemos.markovnames.Main;
import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovGenerator {
    public MarkovDictionary surnameDictionary;
    public Random random;
    public String name;
    public String symbol;

    public MarkovGenerator(int sequenceLen, Random random) {
        this.random = random;
    }

    public MarkovGenerator(int sequenceLen) {
        this(sequenceLen, new Random());
    }

    public MarkovGenerator() {
        this(3);
    }

    public String fetch(int gender) {
        return stylize(surnameDictionary.generateWord());
    }

    public String fetch() {
        return fetch(Main.GENDER_RANDOM);
    }

    public String stylize(String str) {
        return str;
    }

    public String feminize(String element, boolean flag) {
        return element;
    }
}
