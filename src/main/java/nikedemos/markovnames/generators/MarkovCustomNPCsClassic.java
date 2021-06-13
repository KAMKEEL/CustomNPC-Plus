package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

/* This implementation is made specifically for
 * CustomNPCs, one of the best Minecraft mods ever...
 * I'm a huge fan and would recommend it to anyone.
 * Check it out if you haven't yet, at
 * http://www.kodevelopment.nl/minecraft/category/customnpcs/
 * Minecraft 1.12.2 Java Edition + Forge required.
 * Also, you have to like cats, it's in CNPCs EULA, I'm quite sure
 */

public class MarkovCustomNPCsClassic extends MarkovGenerator {

	public MarkovCustomNPCsClassic(int seqlen, Random rng)
	{
		this.rng = rng;
		this.markov  = new MarkovDictionary("customnpcs_classic.txt",seqlen,rng);
	}
	
	public MarkovCustomNPCsClassic(int seqlen)
	{
		this(seqlen,new Random());
		
	}
	
	public MarkovCustomNPCsClassic()
	{
		this(3, new Random());
	}

	@Override
	public String fetch(int gender)
	{
		return markov.generateWord();
	}
}
