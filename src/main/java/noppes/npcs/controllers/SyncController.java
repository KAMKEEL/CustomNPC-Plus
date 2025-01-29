package noppes.npcs.controllers;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.addon.DBCAddon;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumPacketClient;

public class SyncController {

    public static void syncConfigs(EntityPlayerMP player){
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setBoolean("Profiles", true);
        Server.sendData(player, EnumPacketClient.SYNC_CONFIG, nbtTagCompound);
    }

    public static void receiveConfigs(NBTTagCompound compound){
        ConfigMain.EnableProfiles = compound.getBoolean("Profiles");
    }

}
