package kamkeel.npcs.network.packets.request.profile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.InfoEntry;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.controllers.data.Slot;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ProfileGetInfoPacket extends AbstractPacket {
    public static String packetName = "Request|ProfileGetInfo";

    public ProfileGetInfoPacket() {}

    @Override
    public Enum getType() {
        return EnumRequestPacket.ProfileGetInfo;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {}

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        sendProfileInfo(player);
    }

    public static void sendProfileInfo(EntityPlayer player){
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("PROFILE_INFO", true);
        NBTTagList slotList = new NBTTagList();
        Profile profile = ProfileController.getProfile(player);
        if(profile != null){
            for(Slot slot : profile.slots.values()){
                NBTTagCompound slotCompound = new NBTTagCompound();
                NBTTagList infoList = new NBTTagList();
                slotCompound.setInteger("ID", slot.getId());
                List<InfoEntry> profileInfo = ProfileController.getProfileInfo(player, slot.getId());
                for(InfoEntry infoEntry : profileInfo){
                    infoList.appendTag(infoEntry.writeToNBT());
                }
                slotCompound.setTag("INFO", infoList);
                slotList.appendTag(slotCompound);
            }
            compound.setTag("SLOTS", slotList);
        }
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }

    public static HashMap<Integer, List<InfoEntry>> readProfileInfo(NBTTagCompound compound) {
        HashMap<Integer, List<InfoEntry>> slotInfoMap = new HashMap<>();

        if (!compound.getBoolean("PROFILE_INFO")) return slotInfoMap;

        NBTTagList slotList = compound.getTagList("SLOTS", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < slotList.tagCount(); i++) {
            NBTTagCompound slotCompound = slotList.getCompoundTagAt(i);

            int slotId = slotCompound.getInteger("ID");
            NBTTagList infoList = slotCompound.getTagList("INFO", Constants.NBT.TAG_COMPOUND);

            List<InfoEntry> infoEntries = new ArrayList<>();
            for (int j = 0; j < infoList.tagCount(); j++) {
                NBTTagCompound infoTag = infoList.getCompoundTagAt(j);
                infoEntries.add(InfoEntry.readFromNBT(infoTag)); // Convert back to InfoEntry
            }

            slotInfoMap.put(slotId, infoEntries);
        }

        return slotInfoMap;
    }

}
