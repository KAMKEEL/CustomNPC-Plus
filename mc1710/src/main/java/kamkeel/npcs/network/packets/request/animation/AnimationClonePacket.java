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
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import java.io.IOException;

public final class AnimationClonePacket extends AbstractPacket {
    public static String packetName = "Request|AnimationClone";

    private int id;

    public AnimationClonePacket() {
    }

    public AnimationClonePacket(int id) {
        this.id = id;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.AnimationClone;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return CustomNpcsPermissions.GLOBAL_ANIMATION;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.id);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        int animationId = in.readInt();
        Animation clone = AnimationController.getInstance().cloneAnimation(animationId);
        if (clone != null) {
            NoppesUtilServer.sendAnimationDataAll((EntityPlayerMP) player);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, clone.writeToNBT());
        }
    }
}
