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
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import java.io.IOException;

public final class AnimationSavePacket extends AbstractPacket {
    public static String packetName = "Request|AnimationSave";

    private NBTTagCompound animationNBT;

    public AnimationSavePacket(NBTTagCompound animationNBT) {
        this.animationNBT = animationNBT;
    }

    public AnimationSavePacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.AnimationSave;
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
        ByteBufUtils.writeNBT(out, animationNBT);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(EnumItemPacketType.WAND, player))
            return;

        NBTTagCompound compound = ByteBufUtils.readNBT(in);
        Animation animation = new Animation();
        animation.readFromNBT(compound);
        AnimationController.getInstance().saveAnimation(animation);
        NoppesUtilServer.sendAnimationDataAll((EntityPlayerMP) player);
        GuiDataPacket.sendGuiData((EntityPlayerMP) player, animation.writeToNBT());
    }
}
