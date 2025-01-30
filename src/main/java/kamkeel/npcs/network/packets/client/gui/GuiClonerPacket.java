package kamkeel.npcs.network.packets.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;

import java.io.IOException;

public final class GuiClonerPacket extends AbstractPacket {
    public static final String packetName = "Client|Clone";

    private NBTTagCompound compound;

    public GuiClonerPacket() {}

    public GuiClonerPacket(NBTTagCompound comp){
        this.compound = comp;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.CLONE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);
        NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(nbt));
    }
}
