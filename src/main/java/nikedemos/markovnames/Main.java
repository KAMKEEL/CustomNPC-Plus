package nikedemos.markovnames;

import nikedemos.markovnames.generators.*;

import java.util.HashMap;
import java.util.Map.Entry;

public class Main {

    public static final int GENDER_RANDOM = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    // HashMap to hold all the generators for iteration and other operations
    public static HashMap<String, MarkovGenerator> GENERATORS = new HashMap<>();

    public static void main(String[] args) {
        initializeGenerators();

        // Iterate through generators and generate names
        for (Entry<String, MarkovGenerator> entry : GENERATORS.entrySet()) {
            String generatorName = entry.getKey();
            MarkovGenerator generator = entry.getValue();

            System.out.println("=== " + generatorName + " ===");

            generateNames(generator);
        }
    }

    /**
     * Initializes the Markov generators and stores them in the GENERATORS HashMap.
     */
    private static void initializeGenerators() {
        GENERATORS.put("ROMAN", new MarkovRoman(3));
        GENERATORS.put("JAPANESE", new MarkovJapanese(4));
        GENERATORS.put("SLAVIC", new MarkovSlavic(3));
        GENERATORS.put("WELSH", new MarkovWelsh(3));
        GENERATORS.put("SAAMI", new MarkovSaami(3));
        GENERATORS.put("OLDNORSE", new MarkovOldNorse(4));
        GENERATORS.put("ANCIENTGREEK", new MarkovAncientGreek(3));
        GENERATORS.put("AZTEC", new MarkovAztec(3));
        GENERATORS.put("CustomNPCsClassic", new MarkovCustomNPCsClassic(3));
        GENERATORS.put("Spanish", new MarkovSpanish(3));
    }

    /**
     * Generates and prints names using the specified generator.
     *
     * @param generator The Markov generator.
     */
    private static void generateNames(MarkovGenerator generator) {
        for (int i = 0; i < 16; i++) {
            if (i == 0) {
                System.out.println("-------- GENTLEMEN --------");
            }

            int gender = i < 8 ? GENDER_MALE : GENDER_FEMALE;
            String randomName = generator.fetch(gender);
            System.out.println(randomName);

            if (i == 7) {
                System.out.println("-------- LADIES --------");
            } else if (i == 15) {
                System.out.println("\n");
            }
        }
    }
}