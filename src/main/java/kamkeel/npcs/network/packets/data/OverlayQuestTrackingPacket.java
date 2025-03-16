package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.hud.ClientHudManager;
import noppes.npcs.client.gui.hud.EnumHudComponent;
import noppes.npcs.client.gui.hud.HudComponent;

import java.io.IOException;

public final class OverlayQuestTrackingPacket extends AbstractPacket {
    public static final String packetName = "Data|OverlayQuestTracking";

    private NBTTagCompound compound;

    public OverlayQuestTrackingPacket() {
    }

    public OverlayQuestTrackingPacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.OVERLAY_QUEST_TRACKING;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        HudComponent component = ClientHudManager.getInstance().getHudComponents().get(EnumHudComponent.QuestTracker);
        try {
            NBTTagCompound nbt = ByteBufUtils.readNBT(in);
            component.loadData(nbt);
            if (nbt.hasNoTags())
                component.hasData = false;
        } catch (IOException e) {
            component.hasData = false;
        }
    }
}
