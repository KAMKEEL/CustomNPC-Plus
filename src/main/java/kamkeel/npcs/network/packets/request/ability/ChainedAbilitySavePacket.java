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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;

import java.io.IOException;

/**
 * Request packet to save a chained ability.
 */
public final class ChainedAbilitySavePacket extends AbstractPacket {
    public static String packetName = "Request|ChainedAbilitySave";

    private NBTTagCompound chainNBT;

    public ChainedAbilitySavePacket() {
    }

    public ChainedAbilitySavePacket(NBTTagCompound chainNBT) {
        this.chainNBT = chainNBT;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.ChainedAbilitySave;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_ABILITY;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, chainNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        if (compound == null) return;
        ChainedAbility chain = new ChainedAbility();
        chain.readNBT(compound);

        if (chain.getName() != null && !chain.getName().isEmpty()) {
            AbilityController.Instance.saveChainedAbility(chain);
            NoppesUtilServer.sendChainedAbilitiesData((EntityPlayerMP) player);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, chain.writeNBT(false));
        }
    }
}
