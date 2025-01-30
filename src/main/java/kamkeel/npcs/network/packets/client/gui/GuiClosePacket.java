package kamkeel.npcs.network.packets.client.gui;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.client.gui.util.IGuiClose;

import java.io.IOException;

public final class GuiClosePacket extends AbstractPacket {
    public static final String packetName = "Client|GuiClose";

    private int code;
    private NBTTagCompound compound;

    public GuiClosePacket() {}

    public GuiClosePacket(int code, NBTTagCompound compound) {
        this.code = code;
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.GUI_CLOSE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(code);
        ByteBufUtils.writeNBT(out, compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof IGuiClose) {
            int closeCode = in.readInt();
            NBTTagCompound nbt = ByteBufUtils.readNBT(in);
            ((IGuiClose) gui).setClose(closeCode, nbt);
        }
        Minecraft.getMinecraft().displayGuiScreen(null);
        Minecraft.getMinecraft().setIngameFocus();
    }
}
