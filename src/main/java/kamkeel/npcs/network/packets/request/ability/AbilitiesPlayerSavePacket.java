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
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public class AbilitiesPlayerSavePacket extends AbstractPacket {
    public static final String packetName = "Request|PlayerDataAbilitiesSave";

    private NBTTagCompound abilitiesData;

    public AbilitiesPlayerSavePacket() {
    }

    public AbilitiesPlayerSavePacket(NBTTagCompound abilitiesData) {
        this.abilitiesData = abilitiesData;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PlayerDataAbilitiesSave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
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
        PlayerData.get(player).abilities.readFromNBT(compound);
    }
}
