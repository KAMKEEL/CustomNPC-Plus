package nikedemos.markovnames.generators;

import nikedemos.markovnames.MarkovDictionary;

import java.util.Random;

public class MarkovOldNorse extends MarkovGenerator {

	public MarkovDictionary markov2;

	public MarkovOldNorse(int seqlen, Random rng)
	{
		this.rng = rng;
		this.markov  = new MarkovDictionary("old_norse_bothgenders.txt",seqlen,rng);
	}
	
	public MarkovOldNorse(int seqlen)
	{
		this(seqlen,new Random());		
	}
	
	public MarkovOldNorse()
	{
		this(4, new Random()); //4 seems best-suited for Old Norse
	}

	@Override
	public String fetch(int gender) //Old Norse names are genderless
	{
		return markov.generateWord();
	}
}
