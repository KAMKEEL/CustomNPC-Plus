package kamkeel.npcs.network.packets.request.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
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
 * Request packet to get a specific custom ability by UUID.
 */
public final class CustomAbilityGetPacket extends AbstractPacket {
    public static String packetName = "Request|CustomAbilityGet";

    private String uuid;

    public CustomAbilityGetPacket() {
    }

    public CustomAbilityGetPacket(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CustomAbilityGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, uuid);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        String id = ByteBufUtils.readString(in);
        Ability ability = AbilityController.Instance.getCustomAbility(id);
        if (ability != null) {
            NBTTagCompound compound = ability.writeNBT();
            // Include the UUID so the client can track it
            compound.setString("_uuid", id);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        }
    }
}
