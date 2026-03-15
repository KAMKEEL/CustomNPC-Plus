package kamkeel.npcs.network.packets.data.script;

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
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.customoverlay.OverlayCustom;

import java.io.IOException;

public final class ScriptOverlayDataPacket extends AbstractPacket {
    public static final String packetName = "Data|ScriptOverlayData";

    private NBTTagCompound overlayCompound;

    public ScriptOverlayDataPacket() {
    }

    public ScriptOverlayDataPacket(NBTTagCompound overlayCompound) {
        this.overlayCompound = overlayCompound;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.SCRIPT_OVERLAY_DATA;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.overlayCompound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        OverlayCustom overlayCustom = new OverlayCustom(Minecraft.getMinecraft());
        overlayCustom.setOverlayData(ByteBufUtils.readNBT(in));
        ClientCacheHandler.customOverlays.put(overlayCustom.overlay.getID(), overlayCustom);
    }
}
