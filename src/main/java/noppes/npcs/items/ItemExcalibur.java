package noppes.npcs.items;

import org.lwjgl.opengl.GL11;

public class ItemExcalibur extends ItemNpcWeaponInterface{
	
	public static String[] quotes = new String[]{
		"Victory and Glory!", 
		"Excalibur! Excalibur! From the United K! I'm looking for heaven! I'm going to California!",
		"FOOL! As someone who is unaware of the vital role that singing has played in the cultural history of mankind you are in no position to question me.",
		"SILENCE! This is number 349 of the 1,000 provisions you must observe. Meisters should eat everything regardless of personal likes and dislikes. Never say anything as selfish as \"I don't like carrots.\" again.",
		"About the 1,000 provisions that you are required to observe. Now I would like for you to participate in the most important provision. Number 452 the 5 hour story telling party.",
		"Thus at long last the fighting between the rival gangs had ended! To sum up, this leads us to number 778 of the 1,000 provisions you must observe. Never mail a letter without the return address or the proper postage. And don't call collect.",
		"To sum up, that is what led to number 679 of the 1,000 provisions you must observe. Always place a dehumidifier in your room.",
		"This brings us to number 278 of the 1,000 provisions you must observe. I hate carrots. Never even think about putting them in my food, you get it?",
		"Thus I found myself carrying the entire weight of the troop as I preformed my dance before a capacity crowd at the opera house.",
		"A symphony can not be created using common sense and probabilities. It is already written in the fate of the composer. No... that's not it. A symphony is the inevitable result of a bad childhood, deafness, and too many beans eaten after a certain time of day.",
		"Humming and the nightly news share a very tight connection indeed.",
		"Number 58 of the 1,000 provisions I would like you to observe, never talk to me while I'm humming to myself. This is an important provision do you understand?",
		"Hey! Hold on, come back! I'll tell you what! I can lower those 1,000 provisions down to 800, just as long as you take part in the five hour story telling party.",
		"Before becoming my Meister there is a list of 1,000 provisions you must peruse. Be sure to look through all of them, they're important. I greatly look forward to your participation in number 452 the five hour story telling party.",
		"Nothing beats a cup of herbal tea in the morning.",
		"My legend dates back to the 12th Century you see. My legend is quite old. The 12th Century was a long time ago.",
		"Young ones! Do you want to hear the legend of me? Do you want to hear a heroic tale?",
		"No autographs!",
		"The only thing you can change about the past is how you feel about it in the present.",
		"My mornings begin with a cup of coffee with cream at the cafe. My afternoon begins with hot tea with two lumps of sugar. And my evenings... In the evening I change into my pajamas.",
	};

	public ItemExcalibur(int par1, ToolMaterial tool) {
		super(par1, tool);
	}

	@Override
	public void renderSpecial(){
        GL11.glTranslatef(0.2F, 0.3f, 0.1f);
	}    
}
