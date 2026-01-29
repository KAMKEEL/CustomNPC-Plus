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
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.BuiltInAnimation;

import java.io.IOException;

/**
 * Packet to request a built-in animation by name.
 * Built-in animations don't have IDs, so we fetch by name.
 */
public final class BuiltInAnimationGetPacket extends AbstractPacket {
    public static String packetName = "Request|BuiltInAnimationGet";

    private String animationName;

    public BuiltInAnimationGetPacket() {
    }

    public BuiltInAnimationGetPacket(String animationName) {
        this.animationName = animationName;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.BuiltInAnimationGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, animationName);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;

        if (!PacketUtil.verifyItemPacket(packetName, EnumItemPacketType.WAND, player))
            return;

        String name = ByteBufUtils.readString(in);
        BuiltInAnimation animation = AnimationController.getInstance().getBuiltInAnimation(name);
        if (animation != null) {
            NBTTagCompound compound = animation.writeToNBT();
            // Mark as built-in so client knows
            compound.setBoolean("BuiltIn", true);
            GuiDataPacket.sendGuiData((EntityPlayerMP) player, compound);
        }
    }
}
