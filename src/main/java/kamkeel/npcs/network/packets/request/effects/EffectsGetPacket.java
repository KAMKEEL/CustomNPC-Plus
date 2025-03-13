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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;

import java.io.IOException;

public final class EffectsGetPacket extends AbstractPacket {
    public static final String packetName = "NPC|EffectsGetPacket";
    private int effectID;

    public EffectsGetPacket(int outlineID) {
        this.effectID = outlineID;
    }

    public EffectsGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.EffectList;
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

        if (effectID != -1) {
            CustomEffect effect = (CustomEffect) CustomEffectController.getInstance().get(effectID);
            if (effect != null) {
                NBTTagCompound compound = effect.writeToNBT(false);
                compound.setString("PACKETTYPE", "Effect");
                GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
            }
        } else {
            NoppesUtilServer.sendCustomEffectDataAll((EntityPlayerMP) player);
        }
    }
}
