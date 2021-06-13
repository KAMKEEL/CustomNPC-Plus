package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovGenerator {

	public MarkovDictionary markov;
	public Random rng;
	public String name;
	public String symbol;
	
	public MarkovGenerator(int seqlen, Random rng)
	{
	this.rng = rng;
	}
	
	public MarkovGenerator(int seqlen)
	{
		this(seqlen, new Random());
	}
	
	public MarkovGenerator()
	{
		this(3, new Random());
	}

	public String fetch(int gender) {
		return stylize(markov.generateWord());
	}
	
	public String fetch()
	{
		return fetch(0); //0 = random gender, 1 = male, 2 = female
	}

	public String stylize(String str)
	{
		return str;
	}

	public String feminize(String element, boolean flag) {
		return element;
	}
}
