package kamkeel.npcs.network.packets.request;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.PlayerDataController;

import java.io.IOException;

public final class AnimationCachePacket extends AbstractPacket {
    public static final String packetName = "Request|AnimationCache";
    private int id;

    public AnimationCachePacket() {
    }

    public AnimationCachePacket(int id) {
        this.id = id;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.CacheAnimation;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.id);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        PlayerDataController.Instance.getPlayerData(player).animationData.cacheAnimation(in.readInt());
    }


}
