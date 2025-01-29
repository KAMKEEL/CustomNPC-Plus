package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.Achievement;
import net.minecraft.util.StatCollector;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;

import java.io.IOException;

public final class MessagePacket extends AbstractPacket {
    public static final String packetName = "Client|Message";

    public MessagePacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.MESSAGE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        // TODO: Fix Message Packet
//        String description = StatCollector.translateToLocal(Server.readString(in));
//        String message = Server.readString(in);
//        Achievement achievement = new Achievement(message, description);
//        GuiAchievement guiAchievement = Minecraft.getMinecraft().guiAchievement;
//        guiAchievement.func_146256_a(achievement);
//        ObfuscationReflectionHelper.setPrivateValue(GuiAchievement.class, guiAchievement, achievement.getDescription(), 4);
    }
}
