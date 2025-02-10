package kamkeel.npcs.network.packets.request.effects;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.script.GuiScriptEffect;

import java.io.IOException;

public class EffectScriptGetPacket extends LargeAbstractPacket {
    public static final String packetName = "NPC|EffectScriptGetPacket";
    private NBTTagCompound compound;

    public EffectScriptGetPacket() {}

    public EffectScriptGetPacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.EffectScriptGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @Override
    protected byte[] getData() throws IOException {
        ByteBuf byteBuf = Unpooled.buffer();
        ByteBufUtils.writeNBT(byteBuf, compound);
        return byteBuf.array();
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException {
        NBTTagCompound scriptData = ByteBufUtils.readNBT(data);
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiScriptEffect) {
            GuiScriptEffect scriptGui = (GuiScriptEffect) screen;
            scriptGui.setGuiData(scriptData);
        }
    }
}
