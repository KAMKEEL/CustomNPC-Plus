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
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;

import java.io.IOException;

public final class EffectGetPacket extends AbstractPacket {
    public static final String packetName = "NPC|GetEffect";
    private int effectID;

    public EffectGetPacket(int EffectID) {
        this.effectID = EffectID;
    }

    public EffectGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.EffectGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.effectID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int effectID = in.readInt();
        NBTTagCompound compound = new NBTTagCompound();
        if (effectID != -1 && CustomEffectController.getInstance().has(effectID)) {
            CustomEffect Effect = CustomEffectController.getInstance().get(effectID);
            if (Effect != null) {
                compound = Effect.writeToNBT(false);
            }
        }
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
