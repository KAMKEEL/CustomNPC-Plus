package kamkeel.npcs.network.packets.client;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.RenderChatMessages;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class ChatBubblePacket extends AbstractPacket {
    public static final String packetName = "Client|ChatBubble";

    private int entityId;
    private String text;
    private boolean hideText;

    public ChatBubblePacket() {}

    public ChatBubblePacket(int entityId, String text, boolean hideText) {
        this.entityId = entityId;
        this.text = text;
        this.hideText = hideText;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.CHATBUBBLE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.entityId);
        ByteBufUtils.writeString(out, this.text);
        out.writeBoolean(this.hideText);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if(CustomNpcs.side() != Side.CLIENT)
            return;

        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(in.readInt());
        if(!(entity instanceof EntityNPCInterface))
            return;

        EntityNPCInterface npc = (EntityNPCInterface) entity;
        if(npc.messages == null)
            npc.messages = new RenderChatMessages();
        String text = NoppesStringUtils.formatText(ByteBufUtils.readString(in), player, npc);
        npc.messages.addMessage(text, npc);

        if(in.readBoolean())
            player.addChatMessage(new ChatComponentTranslation(npc.getCommandSenderName() + ": " + text));
    }
}
