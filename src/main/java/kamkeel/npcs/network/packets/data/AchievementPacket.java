package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.stats.Achievement;
import net.minecraft.util.StatCollector;
import noppes.npcs.CustomItems;
import noppes.npcs.client.MessageAchievement;
import noppes.npcs.config.ConfigClient;

import java.io.IOException;

public final class AchievementPacket extends AbstractPacket {
    public static final String packetName = "Data|Achievement";

    private boolean isParty;
    private String description;
    private String message;

    public AchievementPacket() {
    }

    public AchievementPacket(boolean isParty, String description, String message) {
        this.isParty = isParty;
        this.description = description;
        this.message = message;
    }

    public static void sendAchievement(EntityPlayerMP playerMP, boolean isParty, String description, String message) {
        AchievementPacket packet = new AchievementPacket(isParty, description, message);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.ACHIEVEMENT;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeBoolean(this.isParty);
        ByteBufUtils.writeString(out, this.description);
        ByteBufUtils.writeString(out, this.message);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!ConfigClient.BannerAlerts)
            return;

        boolean isNotParty = !in.readBoolean();
        String description = StatCollector.translateToLocal(ByteBufUtils.readString(in));
        String message = ByteBufUtils.readString(in);
        Achievement ach = isNotParty ? new MessageAchievement(message, description) : new MessageAchievement(CustomItems.bag == null ? Items.paper : CustomItems.bag, message, description);
        Minecraft.getMinecraft().guiAchievement.func_146256_a(ach);
        ObfuscationReflectionHelper.setPrivateValue(GuiAchievement.class, Minecraft.getMinecraft().guiAchievement, ach.getDescription(), 4);
    }
}
