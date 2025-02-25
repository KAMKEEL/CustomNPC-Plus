package kamkeel.npcs.addon;

import cpw.mods.fml.common.Loader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class DBCAddon {
    public static DBCAddon instance;
    public boolean supportEnabled = true;

    /**
     * This class is a shell class to be changed via mixins
     * The Addon Mod will replace all blank functions within
     * this class to change the ongoing code.
     */
    public DBCAddon(){
        instance = this;
    }

    public static boolean IsAvailable() {
        return Loader.isModLoaded("npcdbc");
    }

    public void dbcCopyData(EntityLivingBase copied, EntityLivingBase entity){}

    // AI / STATS
    public boolean canDBCAttack(EntityNPCInterface npc, float attackStrength, Entity receiver){
        return false;
    }
    public void doDBCDamage(EntityNPCInterface npc, float attackStrength, Entity receiver){}
    public boolean isKO(EntityNPCInterface npc, EntityPlayer player){
        return false;
    }

    // PlayerData NBT
    public void writeToNBT(PlayerData playerData, NBTTagCompound nbtTagCompound){}
    public void readFromNBT(PlayerData playerData, NBTTagCompound nbtTagCompound){}

    // Client Sync
    public void syncPlayer(EntityPlayerMP playerMP){}
}
