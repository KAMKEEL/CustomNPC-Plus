package kamkeel.npcs.network.packets.request.effects;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;

import java.io.IOException;

public class EffectRemovePacket extends AbstractPacket {
    public static final String packetName = "NPC|EffectRemove";

    private int effectID;

    public EffectRemovePacket(int outlineID) {
        this.effectID = outlineID;
    }

    public EffectRemovePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.EffectRemove;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_EFFECT;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.effectID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int effectID = in.readInt();
        CustomEffectController.getInstance().delete(effectID);
        NoppesUtilServer.sendCustomEffectDataAll((EntityPlayerMP) player);
        NBTTagCompound compound = (new CustomEffect()).writeToNBT(false);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
