package nikedemos.markovnames;

import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class MarkovDictionary {
	private Random rng;
	private int sequenceLen = 3;

	private HashMap2D<String, String, Integer> occurrences = new HashMap2D<String, String, Integer>();

	public MarkovDictionary(String dictionary, int seqlen, Random rng) {
		this.rng = rng;

		try {
			applyDictionary(dictionary, seqlen);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MarkovDictionary(String dictionary, int seqlen) {
		this(dictionary, seqlen, new Random());
	}

	public MarkovDictionary(String dictionary) {
		this(dictionary, 3, new Random()); // 3 is the default, anyway
	}

	public MarkovDictionary(String dictionary, Random rng) {
		this(dictionary, 3, rng); // 3 is the default, anyway
	}

	// Blatantly copied from EnderIO:
	// https://github.com/SleepyTrousers/EnderIO/blob/master/enderio-base/src/main/java/crazypants/enderio/base/config/recipes/RecipeFactory.java#L44-L57
	// Thanks again for your help, Henry Loenwind!

	private InputStream getResource(ResourceLocation resourceLocation) {
		final ModContainer container = Loader.instance().activeModContainer();
		if (container != null) {
			final String resourcePath = String.format("/%s/%s/%s", "assets", resourceLocation.getResourceDomain(),
					resourceLocation.getResourcePath());
			InputStream resourceAsStream = null;

			try {
				resourceAsStream = container.getMod().getClass().getResourceAsStream(resourcePath);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (resourceAsStream != null) {
				return resourceAsStream;
			} else {
				throw new RuntimeException("Could not find resource " + resourceLocation);
			}
		} else {
			throw new RuntimeException("Failed to find current mod while looking for resource " + resourceLocation);
		}
	}

	public String getCapitalized(String str) {
		if (str == null || str.isEmpty())
			return str;
		char[] chars = str.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	public static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}

	public void incrementSafe(String str1, String str2) {
		// so for instance, "_DUP_AUX", "_TOTAL" will count how many "DUP" seqCurrs we have
		if (occurrences.containsKeys(str1, str2)) 
		{
			int curr = occurrences.get(str1, str2).intValue();
			occurrences.put(str1, str2, Integer.valueOf(curr + 1));
		} else {
			occurrences.put(str1, str2, Integer.valueOf(1));
		}
	}

	public String generateWord() {

		// let's pick the first element, from which further picking shall proceed.
		// first, we need to know how many top-level sequences (sequenceLen length)
		// strings
		// we have. So just take into account those surrounded by "_" - and count them.
		// From that we'll get the weights.

		int allEntries = 0;

		// first iteration: we count top level entries. There's just no other way.
		Iterator<Entry<String, Map<String, Integer>>> i = occurrences.mMap.entrySet().iterator();

		while (i.hasNext()) {
			Entry<String, Map<String, Integer>> pair = (Entry<String, Map<String, Integer>>) i.next();

			String k = pair.getKey();
			if (k.startsWith("_[") && k.endsWith("_")) // dealing with meta entry here
			{
				allEntries += occurrences.get(k, "_TOTAL_");
			}
		}
		if(allEntries == 0) {
			return "Kamkeel"; //shouldnt happen
		}
		int topLevelEntries;
		int randomNumber = rng.nextInt(allEntries);

		// ok, so how does this weighted random work?
		// easy. Check if the randomNumber is LESS than
		// topLevelEntries variable for that entry (see below).
		// If it is, break the while loop - we got our first element,
		// from which we will go further. Just remember to
		// remove the underscores at both ends!

		Iterator<Entry<String, Map<String, Integer>>> it = occurrences.mMap.entrySet().iterator();

		StringBuilder sequence = new StringBuilder("");

		while (it.hasNext()) {
			Entry<String, Map<String, Integer>> pair = (Entry<String, Map<String, Integer>>) it.next();

			String k = pair.getKey();

			if (k.startsWith("_[") && k.endsWith("_")) // dealing with meta entry here
			{
				topLevelEntries = occurrences.get(k, "_TOTAL_");

				if (randomNumber < topLevelEntries) {
					sequence.append(k.substring(1, sequenceLen + 1)); // removing the underscores
					break;
				} else {
					// keep going
					randomNumber -= topLevelEntries;
				}
			}
		}
		// great! now that we have the first element, time for some generic iterations.
		// in a very similar manner. Basically - perform this loop till you encounter
		// "]" at the end
		StringBuilder word = new StringBuilder("");

		word.append(sequence); // now we're gonna use firstElement to keep the sequence
		while (sequence.charAt(sequence.length() - 1) != ']') {
			// sequence is now your HashMap key for the 1st dimension.
			// for that sequence:
			// - get total elements that are not meta (not surrounded by underscores)
			// and count their total occurrences
			int subSize = 0;

			Iterator<Entry<String, Integer>> j = occurrences.mMap.get(sequence.toString()).entrySet().iterator();

			while (j.hasNext()) {
				Entry<String, Integer> entry = j.next();
				subSize += entry.getValue();
			}

			// and now, the last iterator, with a random, just like before
			randomNumber = rng.nextInt(subSize);

			Iterator<Entry<String, Integer>> k = occurrences.mMap.get(sequence.toString()).entrySet().iterator();

			String chosen = "";

			while (k.hasNext()) {
				Entry<String, Integer> entry = k.next();
				int occu = occurrences.get(sequence.toString(), entry.getKey());

				if (randomNumber < occu) {
					chosen = entry.getKey();
					break;
				} else { // keep going!
					randomNumber -= occu;
				}
			}
			// now, append the word...
			word.append(chosen);

			// delete the first character of the sequence,
			// and also append it with chosen character.
			// So if the Sequence is ABC, and chosen is D,
			// it now becomes BCD.
			sequence.delete(0, 1);
			sequence.append(chosen);
			// System.out.println("FINAL: "+sequence);
			// System.out.println("CHOSEN: "+chosen);

		}
		// and now remove the square brackets surrounding it.
		return this.getPost(word.substring(1, word.length() - 1));
	}

	public String getPost(String str) {
		return getCapitalized(str);
	}

	public void applyDictionary(String dictionaryFile, int seqLen) throws IOException {
		StringBuilder input = new StringBuilder();

		ResourceLocation resource = new ResourceLocation("customnpcs:markovnames/" + dictionaryFile);

		BufferedReader readIn = new BufferedReader(new InputStreamReader(getResource(resource), "UTF-8"));
		// Thread.currentThread().getContextClassLoader().getResourceAsStream("path/to/resource/file.ext");

		for (String line = readIn.readLine(); line != null; line = readIn.readLine()) {
			input.append(line).append(" ");
		}

		readIn.close();

		if (input.length() == 0) {
			throw new RuntimeException("Resource was empty: + " + resource);
		}
		// if seqLen != this.sequenceLen, we must clear occurrences

		if (this.sequenceLen != seqLen) {
			sequenceLen = seqLen;
			this.occurrences.clear();
		}

		String input_str = '[' + input.toString().toLowerCase().replaceAll("[\\t\\n\\r\\s]+", "][") + ']';

		int maxCursorPos = input_str.length() - 1 - sequenceLen;

		for (int i = 0; i <= maxCursorPos; i++) {
			String seqCurr = input_str.substring(i, i + (sequenceLen)); // i plus 2 characters next to it
			String seqNext = input_str.substring(i + sequenceLen, i + sequenceLen + 1); // next character after that
			incrementSafe(seqCurr, seqNext);
			// aux counters

			StringBuilder meta = new StringBuilder("_").append(seqCurr).append("_");

			// String aux1="_"+seqCurr+"_";
			incrementSafe(meta.toString(), "_TOTAL_");
			// String aux2="_"+seqNext+"_";
			// incrementSafe(aux1, aux2);

			// debug, feel free to uncomment

			/*
			 * System.out.println("<"+seqCurr+"><"+seqNext+"> => "+occurrences.get(seqCurr,
			 * seqNext));
			 * System.out.println("<"+aux1+"><"+"_TOTAL_"+"> => "+occurrences.get(aux1,
			 * "_TOTAL_"));
			 * System.out.println("<"+aux1+"><"+aux2+"> => "+occurrences.get(aux1, aux2));
			 * System.out.println("");
			 */

		}
		// by now, we should have

	}
}
