package kamkeel.npcs.util;

import java.util.ArrayList;
import java.util.List;

public class TextSplitter {
    public static List<String> splitText(String input, int maxSegmentLength) {
        List<String> result = new ArrayList<>();

        while (input.length() > maxSegmentLength) {
            int spaceIndex = input.lastIndexOf(" ", maxSegmentLength);
            if (spaceIndex == -1) {
                // No space found within the maxSegmentLength, so split at the exact length.
                result.add(input.substring(0, maxSegmentLength));
                input = input.substring(maxSegmentLength);
            } else {
                // Split at the last space within maxSegmentLength.
                result.add(input.substring(0, spaceIndex));
                input = input.substring(spaceIndex + 1); // Skip the space.
            }
        }

        if (!input.isEmpty()) {
            result.add(input);
        }

        return result;
    }

    public static void main(String[] args) {
        String longText = "This is a very long string that needs to be split into multiple lines based on a maximum line length of 30 characters.";
        int maxLineLength = 30;

        List<String> lines = splitText(longText, maxLineLength);

        for (String line : lines) {
            System.out.println(line);
        }
    }
}
