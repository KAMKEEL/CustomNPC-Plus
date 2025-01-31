package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class PlayerUpdateSkinOverlaysPacket extends AbstractPacket {
    public static final String packetName = "Data|PlayerUpdateSkinOverlays";

    private String playerName;
    private NBTTagCompound compound;

    public PlayerUpdateSkinOverlaysPacket() {}

    public PlayerUpdateSkinOverlaysPacket(String playerName, NBTTagCompound compound) {
        this.playerName = playerName;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.PLAYER_UPDATE_SKIN_OVERLAYS;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, this.playerName);
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String playerName = ByteBufUtils.readString(in);
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);
        EntityPlayer sendingPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(playerName);
        if (sendingPlayer != null) {
            NoppesUtil.updateSkinOverlayData(sendingPlayer, nbt);
        }
    }
}
