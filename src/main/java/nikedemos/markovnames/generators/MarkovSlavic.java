package nikedemos.markovnames.generators;

import nikedemos.markovnames.Main;
import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovSlavic extends MarkovGenerator {

    public MarkovSlavic(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionary("slavic_given.txt", sequenceLen, random);
    }

    public MarkovSlavic(int sequenceLen) {
        this(sequenceLen, new Random());
    }

    public MarkovSlavic() {
        this(3); // 3 seems best-suited for Slavic
    }

    @Override
    public String feminize(String element, boolean flag) {
        // add "a" at the end, if there isn't one
        String lastChar = element.substring(element.length() - 1);

        if (element.endsWith("o")) {
            element = element.substring(0, element.length() - 1) + "a";
        } else if (!lastChar.endsWith("a")) {
            element += "a";
        }

        return element;
    }

    @Override
    public String fetch(int gender) {
        String generateSurnameWord = surnameDictionary.generateWord();

        if (gender == Main.GENDER_RANDOM)
            gender = random.nextBoolean() ? Main.GENDER_MALE : Main.GENDER_FEMALE;

        return gender == Main.GENDER_FEMALE ? feminize(generateSurnameWord, false) : generateSurnameWord;
    }
}
