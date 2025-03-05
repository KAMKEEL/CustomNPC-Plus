package kamkeel.npcs.network.packets.player.customgui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.EventHooks;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.gui.ScriptGui;

import java.io.IOException;

public final class CustomGuiClosePacket extends AbstractPacket {
    public static final String packetName = "Request|CustomGuiClose";

    private NBTTagCompound compound;

    public CustomGuiClosePacket() {
    }

    public CustomGuiClosePacket(NBTTagCompound comp) {
        this.compound = comp;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.CustomGuiClose;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, compound);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player.openContainer instanceof ContainerCustomGui))
            return;

        NBTTagCompound comp = ByteBufUtils.readNBT(in);
        EventHooks.onCustomGuiClose((IPlayer) NpcAPI.Instance().getIEntity(player), (new ScriptGui()).fromNBT(comp));
    }
}
