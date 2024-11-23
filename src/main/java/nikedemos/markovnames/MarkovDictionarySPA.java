package nikedemos.markovnames;

import java.util.Random;

/**
 * This class is used exclusively by the Spanish surname dictionary to handle
 * Spanish-specific capitalization rules.
 */
public class MarkovDictionarySPA extends MarkovDictionary {

    // Words that should not be capitalized
    private static final String[] EXCEPTIONS = {"de", "del", "la", "los"};

    public MarkovDictionarySPA(String dictionary, int sequenceLen, Random random) {
        super(dictionary, sequenceLen, random);
    }

    /**
     * Overrides the base class method to apply Spanish-specific capitalization rules.
     *
     * @param str The string to be processed.
     * @return The string after applying Spanish capitalization rules.
     */
    @Override
    public String getPost(String str) {
        return getCapitalizedSPA(str);
    }

    /**
     * Capitalizes parts of the string according to Spanish capitalization rules.
     * Specifically, it capitalizes each part except for the predefined exception words.
     *
     * @param str The input string where parts are separated by '#'.
     * @return The capitalized string.
     */
    private String getCapitalizedSPA(String str) {
        // Split the input string into parts based on the '#' delimiter
        String[] parts = str.split("#");

        StringBuilder builder = new StringBuilder();

        // Iterate over each part and apply capitalization rules
        for (int i = 0; i < parts.length; i++) {
            // Capitalize this part, but only if it's not an exception word
            if (!isException(parts[i])) {
                parts[i] = getCapitalized(parts[i]);
            }

            // Add a space before each part except the first part
            if (i > 0) {
                builder.append(" ");
            }

            builder.append(parts[i]);
        }

        return builder.toString();
    }

    /**
     * Checks if a given part is one of the predefined exception words.
     *
     * @param part The part to be checked.
     * @return true if the part is an exception word, false otherwise.
     */
    private boolean isException(String part) {
        for (String exception : EXCEPTIONS) {
            if (exception.equals(part)) {
                return true;
            }
        }
        return false;
    }
}