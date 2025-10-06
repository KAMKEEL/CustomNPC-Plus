package kamkeel.npcs.network.packets.player.profile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import kamkeel.npcs.controllers.data.profile.ProfileInfoEntry;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.api.handler.data.ISlot;
import noppes.npcs.config.ConfigMain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ProfileGetInfoPacket extends AbstractPacket {
    public static String packetName = "Request|ProfileGetInfo";

    public ProfileGetInfoPacket() {
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.ProfileGetInfo;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!ConfigMain.ProfilesEnabled)
            return;

        sendProfileInfo(player);
    }

    public static void sendProfileInfo(EntityPlayer player) {
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, profileInfoPayload(player));
    }

    public static NBTTagCompound profileInfoPayload(EntityPlayer player) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("PROFILE_INFO", true);
        NBTTagList slotList = new NBTTagList();
        Profile profile = ProfileController.Instance.getProfile(player);
        if (profile != null) {
            for (ISlot slot : profile.getSlots().values()) {
                NBTTagCompound slotCompound = new NBTTagCompound();
                NBTTagList infoList = new NBTTagList();
                slotCompound.setInteger("ID", slot.getId());
                List<ProfileInfoEntry> profileInfo = ProfileController.Instance.getProfileInfo(player, slot.getId());
                for (ProfileInfoEntry profileInfoEntry : profileInfo) {
                    infoList.appendTag(profileInfoEntry.writeToNBT());
                }
                slotCompound.setTag("INFO", infoList);
                slotList.appendTag(slotCompound);
            }
            compound.setTag("SLOTS", slotList);
        }
        return compound;
    }

    public static HashMap<Integer, List<ProfileInfoEntry>> readProfileInfo(NBTTagCompound compound) {
        HashMap<Integer, List<ProfileInfoEntry>> slotInfoMap = new HashMap<>();

        if (!compound.getBoolean("PROFILE_INFO")) return slotInfoMap;

        NBTTagList slotList = compound.getTagList("SLOTS", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < slotList.tagCount(); i++) {
            NBTTagCompound slotCompound = slotList.getCompoundTagAt(i);

            int slotId = slotCompound.getInteger("ID");
            NBTTagList infoList = slotCompound.getTagList("INFO", Constants.NBT.TAG_COMPOUND);

            List<ProfileInfoEntry> infoEntries = new ArrayList<>();
            for (int j = 0; j < infoList.tagCount(); j++) {
                NBTTagCompound infoTag = infoList.getCompoundTagAt(j);
                infoEntries.add(ProfileInfoEntry.readFromNBT(infoTag)); // Convert back to InfoEntry
            }

            slotInfoMap.put(slotId, infoEntries);
        }

        return slotInfoMap;
    }

}
