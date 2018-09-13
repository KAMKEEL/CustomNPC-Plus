package noppes.npcs.controllers;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.scripted.constants.JobType;

import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.common.Loader;

public class PixelmonHelper {
    public static boolean Enabled = false;
    
    private static Object PokeballManager = null;    
    private static Method getPlayerStorage = null;
    
    private static Object ComputerManager = null;
    private static Method getPlayerComputerStorage = null;

    private static Constructor attackByID = null;
    private static Constructor attackByName = null; 
    private static Field baseAttack = null;
    private static Field getAttackID = null;
    private static Field getAttackName = null;

    private static Method getPixelmonModel = null;
    
	public static void load(){
		Enabled = Loader.isModLoaded("pixelmon");
        if(!Enabled)
        	return;

        try{
			Class c = Class.forName("com.pixelmonmod.pixelmon.storage.PixelmonStorage");
			PokeballManager = c.getField("PokeballManager").get(null);
			ComputerManager = c.getField("ComputerManager").get(null);

			getPlayerStorage = PokeballManager.getClass().getMethod("getPlayerStorage", EntityPlayerMP.class);
			getPlayerComputerStorage = ComputerManager.getClass().getMethod("getPlayerStorage", EntityPlayerMP.class);
			
			c = Class.forName("com.pixelmonmod.pixelmon.battles.attacks.Attack");
			attackByID = c.getConstructor(int.class);
			attackByName = c.getConstructor(String.class);
			baseAttack = c.getField("baseAttack");

			c = Class.forName("com.pixelmonmod.pixelmon.battles.attacks.AttackBase");
			getAttackID = c.getField("attackIndex");
			getAttackName = c.getDeclaredField("attackName");
			

			c = Class.forName("com.pixelmonmod.pixelmon.entities.pixelmon.Entity2HasModel");
			getPixelmonModel = c.getMethod("getModel");
        }
        catch(Exception e){
        	LogWriter.except(e);
        }
        
	}
	
	public static List<String> getPixelmonList(){
		List<String> list = new ArrayList<String>();
		if(!Enabled)
			return list;
		try {
			Class c = Class.forName("com.pixelmonmod.pixelmon.enums.EnumPokemon");
			Object[] array = c.getEnumConstants();
			for(Object ob : array)
				list.add(ob.toString());
			
		} catch (Exception e) {
			LogManager.getLogger().error("getPixelmonList", e);
		}
		return list;
	}

	public static boolean isPixelmon(Entity entity) {
		if(!Enabled)
			return false;
		String s = EntityList.getEntityString(entity);
		if(s == null)
			return false;
		return s.equals("pixelmon.Pixelmon");
	}

	public static void setName(EntityLivingBase entity, String name) {
		if(!Enabled || !isPixelmon(entity))
			return;
		try {
			Method m = entity.getClass().getMethod("init", String.class);
			m.invoke(entity, name);

			Class c = Class.forName("com.pixelmonmod.pixelmon.entities.pixelmon.Entity2HasModel");
			m = c.getDeclaredMethod("loadModel");
			m.setAccessible(true);
			m.invoke(entity);
		} catch (Exception e) {
			LogManager.getLogger().error("setName", e);
		}
	}
	
	public static String getName(EntityLivingBase entity) {
		if(!Enabled || !isPixelmon(entity))
			return "";
		try {
			Method m = entity.getClass().getMethod("getName");
			return m.invoke(entity).toString();
		} catch (Exception e) {
			LogManager.getLogger().error("getName", e);
		}
		return "";
	}

	
	public static Object getModel(EntityLivingBase entity){
		try {
			return getPixelmonModel.invoke(entity);
		} catch (Exception e) {
			LogManager.getLogger().error("getModel", e);
		}
		return null;
	}


	public static void debug(EntityLivingBase entity) {
		if(!Enabled || !isPixelmon(entity))
			return;
		try {
			Method m = entity.getClass().getMethod("getModel");
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText((String) m.invoke(entity)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isTrainer(EntityLivingBase entity) {
		if(!Enabled)
			return false;
		String s = EntityList.getEntityString(entity);
		if(s == null)
			return false;
		return s.equals("pixelmon.Trainer");
	}
	
	public static boolean isBattling(EntityPlayerMP player) {
		if(!Enabled)
			return false;
		try {
			Class c = Class.forName("com.pixelmonmod.pixelmon.battles.BattleRegistry");
			Method m = c.getMethod("getBattle", EntityPlayer.class);
			if(m.invoke(null, player) != null)
				return false;
			
			return true;
		} catch (Exception e) {
			LogManager.getLogger().error("canBattle", e);
			return false;
		}
	}	
	
	public static boolean isBattling(EntityLivingBase trainer) {
		if(!Enabled || !isTrainer(trainer))
			return false;
		try {
			Field f = trainer.getClass().getField("battleController");
			
			return f.get(trainer) != null;
		} catch (Exception e) {
			LogManager.getLogger().error("canBattle", e);
			return false;
		}
	}

	public static boolean canBattle(EntityPlayerMP player, EntityNPCInterface npc) {
		if(!Enabled || npc.advanced.job != EnumJobType.Spawner || isBattling(player))
			return false;
		try {			
			JobSpawner spawner = (JobSpawner) npc.jobInterface;
			if(spawner.isOnCooldown(player.getCommandSenderName()))
				return false;
			
			Object ob = getPlayerStorage.invoke(PokeballManager, player);

			Method m = ob.getClass().getMethod("countAblePokemon");
			
			if((Integer) m.invoke(ob) == 0)
				return false;
			
			return true;
		} catch (Exception e) {
			LogManager.getLogger().error("canBattle", e);
			return false;
		}
	}
	
	public static EntityTameable pixelmonFromNBT(NBTTagCompound compound, EntityPlayer player){
		if(!Enabled)
			return null;
		
		try {
			Object ob = getPlayerStorage.invoke(PokeballManager, player);
			return (EntityTameable) ob.getClass().getMethod("sendOut", NBTTagCompound.class, World.class).invoke(ob, compound, player);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static NBTTagCompound getPartySlot(int i, EntityPlayer player){
		if(!Enabled)
			return null;
		
		try {
			Object ob = getPlayerStorage.invoke(PokeballManager, player);
			NBTTagCompound[] party = (NBTTagCompound[]) ob.getClass().getFields()[0].get(ob);
			return party[i];
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean startBattle(EntityPlayerMP player, EntityLivingBase trainer) {
		if(!Enabled)
			return false;
		try {			

			Object ob = getPlayerStorage.invoke(PokeballManager, player);

			Class c = ob.getClass();

			Method m = c.getMethod("getFirstAblePokemon", World.class);
			Entity pixelmon = (Entity) m.invoke(ob, player.worldObj);
			Class cEntity = Class.forName("com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon");
			m = c.getMethod("EntityAlreadyExists", cEntity);
			if(!(Boolean)m.invoke(ob, pixelmon)){
				m = cEntity.getMethod("releaseFromPokeball");
				pixelmon.setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationYaw, 0);
			}

			c = Class.forName("com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant");
			Object parTrainer = c.getConstructor(new Class[] {trainer.getClass(), EntityPlayer.class, int.class}).newInstance(trainer, player, 1);

			Object[] pixelmonArray = (Object[]) Array.newInstance(cEntity, 1);
			pixelmonArray[0] = pixelmon;
			
			c = Class.forName("com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant");
			Object parPlayer = c.getConstructor(new Class[] {EntityPlayerMP.class, pixelmonArray.getClass()}).newInstance(player, pixelmonArray);
			
			cEntity = Class.forName("com.pixelmonmod.pixelmon.entities.pixelmon.Entity6CanBattle");
			c = Class.forName("com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant");
			m = cEntity.getMethod("StartBattle", c, c);
			m.invoke(pixelmon, parTrainer, parPlayer);
			
			return true;
		} catch (Exception e) {
			LogManager.getLogger().error("startBattle", e);
			return false;
		}
	}

	public static int countPCPixelmon(EntityPlayerMP player) {
		try {
			Object ob = getPlayerComputerStorage.invoke(player);
			return (Integer) ob.getClass().getMethod("count").invoke(ob);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return 0;
	}

	public static String getAttackName(int id) {
		try {
			Object ob = attackByID.newInstance(id);
			if(ob == null)
				return null;
			return getAttackName.get(baseAttack.get(ob)) + "";
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	public static int getAttackID(String name) {
		try {
			Object ob = attackByName.newInstance(name);
			if(ob == null)
				return -1;
			return getAttackName.getInt(baseAttack.get(ob));
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		return -1;
	}
}
