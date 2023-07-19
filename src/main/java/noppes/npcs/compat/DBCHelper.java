package noppes.npcs.compat;

import cpw.mods.fml.common.Loader;
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
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DBCHelper {
    public static boolean Enabled = false;

	private static Method StatusEffectsMethod = null;
	private static Method PlyrSkillsMethod = null;
	private static Method TransPwrModAtr = null;
	private static Method statMethod = null;
    
	public static void load(){
		Enabled = Loader.isModLoaded("jinryuujrmcore");
        if(!Enabled)
        	return;

        try{
			Class<?> c = Class.forName("JinRyuu.JRMCore.JRMCoreH");
			StatusEffectsMethod = c.getMethod("StusEfcts",int.class, String.class);
			PlyrSkillsMethod = c.getMethod("PlyrSkills", EntityPlayer.class);
			TransPwrModAtr = c.getMethod("TransPwrModAtr", int[].class, int.class, int.class, int.class, int.class, String.class, int.class, int.class, boolean.class, boolean.class, boolean.class, boolean.class, int.class, String[].class, boolean.class);
			statMethod = c.getMethod("stat", int.class, int.class, int.class, int.class, int.class, float.class);
        }
        catch(Exception e){
        	LogWriter.except(e);
        }
        
	}

	public static boolean StatusEffects(int id, String effect){
		try {
			Object returnValue =  StatusEffectsMethod.invoke(null, id, effect);
			return (Boolean) returnValue;
		} catch (Exception e) {
			LogManager.getLogger().error("StatusEffects", e);
		}
		return false;
	}

	public static String[] PlayerSkills(EntityPlayer p){
		try {
			Object returnValue =  PlyrSkillsMethod.invoke(null, p);
			if (returnValue instanceof String[]) {
				return (String[]) returnValue; // Cast and return the String[] value
			} else {
				return new String[]{};
			}
		} catch (Exception e) {
			LogManager.getLogger().error("PlayerSkills", e);
		}

		return new String[]{};
	}

	public static int TransPwrMod(int[] currAttributes, int attribute, int st, int st2, int race, String SklX, int currRelease, int arcRel, boolean legendOn, boolean majinOn, boolean mysticOn, boolean uiOn, int powerType, String[] Skls, boolean isFused){
		try {
			Object returnValue =  TransPwrModAtr.invoke(null, currAttributes, attribute, st, st2, race, SklX, currRelease, arcRel, legendOn, majinOn, mysticOn, uiOn, powerType, Skls, isFused);
			if (returnValue instanceof Integer) {
				return (int) returnValue;
			} else {
				return -1;
			}
		} catch (Exception e) {
			LogManager.getLogger().error("TransPwrModAtr", e);
		}

		return -1;
	}

	public static int StatMethod(int g, int s, int a, int r, int c, float b){
		try {
			Object returnValue =  statMethod.invoke(null, g, s, a, r, c, b);
			if (returnValue instanceof Integer) {
				return (int) returnValue;
			} else {
				return -1;
			}
		} catch (Exception e) {
			LogManager.getLogger().error("statMethod", e);
		}

		return -1;
	}
}
