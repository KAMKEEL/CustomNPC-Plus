package noppes.npcs.scripted;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.util.ValueUtil;

public class ScriptPixelmon extends ScriptAnimal{
	private EntityTameable pixelmon;
	private NBTTagCompound compound = null;
	public ScriptPixelmon(EntityTameable pixelmon){
		super(pixelmon);
		this.pixelmon = pixelmon;
		compound = new NBTTagCompound();
		pixelmon.writeEntityToNBT(compound);
	}
	public ScriptPixelmon(EntityTameable pixelmon, NBTTagCompound compound){
		this(pixelmon);
		this.compound = compound;
	}
	
	public boolean getIsShiny(){
		return compound.getBoolean("IsShiny");
	}
	
	public void setIsShiny(boolean bo){
		compound.setBoolean("IsShiny", bo);
		pixelmon.readEntityFromNBT(compound);
	}
	
	public int getLevel(){
		return compound.getInteger("Level");
	}
	
	public void setLevel(int level){
		compound.setInteger("Level", level);
		pixelmon.readEntityFromNBT(compound);
	}
	
	/**
	 * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
	 */
	public int getIV(int type){
		if(type == 0)
			return compound.getInteger("IVHP");
		if(type == 1)
			return compound.getInteger("IVAttack");
		if(type == 2)
			return compound.getInteger("IVDefence");
		if(type == 3)
			return compound.getInteger("IVSpAtt");
		if(type == 4)
			return compound.getInteger("IVSpDef");
		if(type == 5)
			return compound.getInteger("IVSpeed");
		
		return -1;
	}

	
	/**
	 * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
	 */
	public void setIV(int type, int value){
		if(type == 0)
			compound.setInteger("IVHP", value);
		else if(type == 1)
			compound.setInteger("IVAttack", value);
		else if(type == 2)
			compound.setInteger("IVDefence", value);
		else if(type == 3)
			compound.setInteger("IVSpAtt", value);
		else if(type == 4)
			compound.setInteger("IVSpDef", value);
		else if(type == 5)
			compound.setInteger("IVSpeed", value);
		pixelmon.readEntityFromNBT(compound);
	}
	
	/**
	 * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
	 */
	public int getEV(int type){
		if(type == 0)
			return compound.getInteger("EVHP");
		if(type == 1)
			return compound.getInteger("EVAttack");
		if(type == 2)
			return compound.getInteger("EVDefence");
		if(type == 3)
			return compound.getInteger("EVSpecialAttack");
		if(type == 4)
			return compound.getInteger("EVSpecialDefence");
		if(type == 5)
			return compound.getInteger("EVSpeed");
		
		return -1;
	}

	
	/**
	 * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
	 */
	public void setEV(int type, int value){
		if(type == 0)
			compound.setInteger("EVHP", value);
		else if(type == 1)
			compound.setInteger("EVAttack", value);
		else if(type == 2)
			compound.setInteger("EVDefence", value);
		else if(type == 3)
			compound.setInteger("EVSpecialAttack", value);
		else if(type == 4)
			compound.setInteger("EVSpecialDefence", value);
		else if(type == 5)
			compound.setInteger("EVSpeed", value);
		pixelmon.readEntityFromNBT(compound);
	}
	
	/**
	 * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
	 */
	public int getStat(int type){
		if(type == 0)
			return compound.getInteger("StatsHP");
		if(type == 1)
			return compound.getInteger("StatsAttack");
		if(type == 2)
			return compound.getInteger("StatsDefence");
		if(type == 3)
			return compound.getInteger("StatsSpecialAttack");
		if(type == 4)
			return compound.getInteger("StatsSpecialDefence");
		if(type == 5)
			return compound.getInteger("StatsSpeed");
		
		return -1;
	}

	
	/**
	 * @param type 0:HP, 1:Attack, 2:Defense, 3:SpAttack, 4:SpDefense, 5:Speed
	 */
	public void setStat(int type, int value){
		if(type == 0)
			compound.setInteger("StatsHP", value);
		else if(type == 1)
			compound.setInteger("StatsAttack", value);
		else if(type == 2)
			compound.setInteger("StatsDefence", value);
		else if(type == 3)
			compound.setInteger("StatsSpecialAttack", value);
		else if(type == 4)
			compound.setInteger("StatsSpecialDefence", value);
		else if(type == 5)
			compound.setInteger("StatsSpeed", value);
		pixelmon.readEntityFromNBT(compound);
	}
	
	/**
	 * @return type 0:Pygmy, 1:Runt, 2:Small, 3:Normal, 4:Huge, 5:Giant, 6:Enormous, 7:Ginormous, 8:Microscopic
	 */
	public int getSize(){
		return compound.getShort("Growth");
	}
	
	/**
	 * @param type 0:Pygmy, 1:Runt, 2:Small, 3:Normal, 4:Huge, 5:Giant, 6:Enormous, 7:Ginormous, 8:Microscopic
	 */
	public void setSize(int type){
		compound.setShort("Growth", (short) type);
		pixelmon.readEntityFromNBT(compound);
	}
	
	/**
	 * @return 0-255
	 */
	public int getHapiness(){
		return compound.getInteger("Friendship");
	}
	
	/**
	 * @param value 0-255
	 */
	public void setHapiness(int value){
		value = ValueUtil.CorrectInt(value, 0, 255);
		compound.setInteger("Friendship", value);
		pixelmon.readEntityFromNBT(compound);
	}
	
	/**
	 * @return 0:Hardy, 1:Serious, 2:Docile, 3:Bashful, 4:Quirky, 5:Lonely, 6:Brave, 7:Adamant, 8:Naughty, 9:Bold, 10:Relaxed, 11:Impish, 12:Lax, 13:Timid, 14:Hasty, 15:Jolly, 16:Naive, 17:Modest, 18:Mild, 19:Quiet, 20:Rash, 21:Calm, 22:Gentle, 23:Sassy, 24:Careful
	 */
	public int getNature(){
		return compound.getShort("Nature");
	}
	
	/**
	 * @param type 0:Hardy, 1:Serious, 2:Docile, 3:Bashful, 4:Quirky, 5:Lonely, 6:Brave, 7:Adamant, 8:Naughty, 9:Bold, 10:Relaxed, 11:Impish, 12:Lax, 13:Timid, 14:Hasty, 15:Jolly, 16:Naive, 17:Modest, 18:Mild, 19:Quiet, 20:Rash, 21:Calm, 22:Gentle, 23:Sassy, 24:Careful
	 */
	public void setNature(int type){
		compound.setShort("Nature", (short) type);
		pixelmon.readEntityFromNBT(compound);
	}
	
	/**
	 * @return -1:Uncaught, 0:Pokeball, 1:GreatBall, 2:UltraBall, 3:MasterBall, 4:LevelBall, 5:MoonBall, 6:FriendBall, 7:LoveBall, 8:SafariBall, 9:HeavyBall, 10:FastBall, 11:RepeatBall, 12:TimerBall, 13:NestBall, 14:NetBall, 15:DiveBall, 16:LuxuryBall, 17:HealBall, 18:DuskBall, 19:PremierBall, 20:SportBall, 21:QuickBall, 22:ParkBall, 23:LureBall, 24:CherishBall, 25:GSBall
	 */
	public int getPokeball(){
		if(compound.hasKey("CaughtBall"))
			return -1;
		return compound.getInteger("CaughtBall");
	}

	/**
	 * @param type -1:Uncaught, 0:Pokeball, 1:GreatBall, 2:UltraBall, 3:MasterBall, 4:LevelBall, 5:MoonBall, 6:FriendBall, 7:LoveBall, 8:SafariBall, 9:HeavyBall, 10:FastBall, 11:RepeatBall, 12:TimerBall, 13:NestBall, 14:NetBall, 15:DiveBall, 16:LuxuryBall, 17:HealBall, 18:DuskBall, 19:PremierBall, 20:SportBall, 21:QuickBall, 22:ParkBall, 23:LureBall, 24:CherishBall, 25:GSBall
	 */
	public void setPokeball(int type){
		compound.setInteger("CaughtBall", type);
		pixelmon.readEntityFromNBT(compound);
	}
	
	public String getNickname(){
		return compound.getString("Nickname");
	}
	
	public boolean hasNickname(){
		return !getNickname().isEmpty();
	}
	
	public void setNickname(String name){
		compound.setString("Nickname", name);
		pixelmon.readEntityFromNBT(compound);
	}
	
	public String getMove(int slot){
		if(!compound.hasKey("PixelmonMoveID" + slot))
			return null;
		return PixelmonHelper.getAttackName(compound.getInteger("PixelmonMoveID" + slot));
	}
	
	public void setMove(int slot, String move){
		slot = ValueUtil.CorrectInt(slot, 0, 3);
		int id = PixelmonHelper.getAttackID(move);
		compound.removeTag("PixelmonMovePP" + slot);
		compound.removeTag("PixelmonMovePPBase" + slot);
		if(id < 0){
			compound.removeTag("PixelmonMoveID" + slot);
		}
		else{
			compound.setInteger("PixelmonMoveID" + slot, id);
		}
		int size = 0;
		for(int i = 0; i < 4; i++){
			if(compound.hasKey("PixelmonMoveID" + i))
				size++;
		}
		compound.setInteger("PixelmonNumberMoves", size);
		
		pixelmon.readEntityFromNBT(compound);
	}
}
