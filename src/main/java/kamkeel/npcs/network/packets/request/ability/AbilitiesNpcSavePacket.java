package kamkeel.npcs.network.packets.request.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;

import java.io.IOException;

public final class AbilitiesNpcSavePacket extends AbstractPacket {
    public static final String packetName = "Request|NpcAbilitiesSave";

    private NBTTagCompound abilitiesData;

    public AbilitiesNpcSavePacket() {
    }

    public AbilitiesNpcSavePacket(NBTTagCompound abilitiesData) {
        this.abilitiesData = abilitiesData;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.NpcAbilitiesSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.NPC_ADVANCED_ABILITIES;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        NBTTagCompound compound = new NBTTagCompound();
        if (abilitiesData != null) {
            compound = abilitiesData;
        }
        ByteBufUtils.writeNBT(out, compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.WAND))
            return;

        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        npc.abilities.readFromNBT(compound);
    }
}
