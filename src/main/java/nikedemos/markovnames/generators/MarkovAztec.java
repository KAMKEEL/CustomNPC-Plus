package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovAztec extends MarkovGenerator {

	public MarkovAztec(int seqlen, Random rng)
	{
		this.rng = rng;
		this.markov  = new MarkovDictionary("aztec_given.txt",seqlen,rng);
	}
	
	public MarkovAztec(int seqlen)
	{
		this(seqlen,new Random());
		
	}
	
	public MarkovAztec()
	{
		this(3, new Random()); //3 seems best-suited for Aztec
	}

	@Override
	public String fetch(int gender) //Aztec names are genderless
	{
		return markov.generateWord();
	}
}
