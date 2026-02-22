package kamkeel.npcs.network.packets.request.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

/**
 * Request packet to get a specific chained ability by name.
 */
public final class ChainedAbilityGetPacket extends AbstractPacket {
    public static String packetName = "Request|ChainedAbilityGet";

    private String name;

    public ChainedAbilityGetPacket() {
    }

    public ChainedAbilityGetPacket(String name) {
        this.name = name;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ChainedAbilityGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, name);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        String chainName = ByteBufUtils.readString(in);
        ChainedAbility chain = AbilityController.Instance.getChainedAbility(chainName);
        if (chain != null) {
            NBTTagCompound compound = chain.writeNBT();
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        }
    }
}
