package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovOldNorse extends MarkovGenerator {

    public MarkovOldNorse(int sequenceLen, Random random) {
        this.random = random;
        this.surnameDictionary = new MarkovDictionary("old_norse_bothgenders.txt", sequenceLen, random);
    }

    public MarkovOldNorse(int sequenceLen) {
        this(sequenceLen, new Random());
    }

    public MarkovOldNorse() {
        this(4, new Random()); //4 seems best-suited for Old Norse
    }

    @Override
    public String fetch(int gender) {
        //Old Norse names are genderless
        return surnameDictionary.generateWord();
    }
}
