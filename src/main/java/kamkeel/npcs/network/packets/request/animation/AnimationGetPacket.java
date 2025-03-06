package kamkeel.npcs.network.packets.request.animation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import java.io.IOException;

public final class AnimationGetPacket extends AbstractPacket {
    public static String packetName = "Request|AnimationGet";

    private int animationID;

    public AnimationGetPacket() { }

    public AnimationGetPacket(int animationID) {
        this.animationID = animationID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.AnimationGet;
    }
    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(animationID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        int id = in.readInt();
        Animation animation = (Animation) AnimationController.getInstance().get(id);
        NBTTagCompound compound = animation.writeToNBT();
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
    }
}
