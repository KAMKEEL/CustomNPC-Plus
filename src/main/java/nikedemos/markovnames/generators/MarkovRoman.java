package nikedemos.markovnames.generators;

import nikedemos.markovnames.Main;
import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovRoman extends MarkovGenerator {
    public MarkovDictionary nominaDictionary;
    public MarkovDictionary cognominaDictionary;

    public MarkovRoman(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionary("roman_praenomina.txt", sequenceLen, random);
        this.nominaDictionary = new MarkovDictionary("roman_nomina.txt", sequenceLen, random);
        this.cognominaDictionary = new MarkovDictionary("roman_cognomina.txt", sequenceLen, random);
    }

    public MarkovRoman(int sequenceLen) {
        this(sequenceLen, new Random());
    }

    public MarkovRoman() {
        this(3, new Random());
    }

    @Override
    public String feminize(String element, boolean flag) {
        // change "us" into "a" and "o" at the end into "a"
        if (element.endsWith("us")) {
            element = element.substring(0, element.length() - 2) + "a";
        } else if (element.endsWith("o")) {
            element = element.substring(0, element.length() - 2) + "a";
        }

        return element;
    }

    @Override
    public String fetch(int gender) {
        String generatedSurname = surnameDictionary.generateWord();
        String generatedNomina = nominaDictionary.generateWord();
        String generatedMarkov = cognominaDictionary.generateWord();

        if (gender == Main.GENDER_RANDOM)
            gender = random.nextBoolean() ? Main.GENDER_MALE : Main.GENDER_FEMALE;

        // now if it's 2 - a lady - feminize the 3 sequences
        if (gender == Main.GENDER_FEMALE) {
            generatedSurname = feminize(generatedSurname, false);
            generatedNomina = feminize(generatedNomina, false);
            generatedMarkov = feminize(generatedMarkov, true);
        }

        return (generatedSurname + " " + generatedNomina + " " + generatedMarkov);
    }
}
