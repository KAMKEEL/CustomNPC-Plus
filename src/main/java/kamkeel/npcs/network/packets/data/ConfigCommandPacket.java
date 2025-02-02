package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumConfigOperation;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.ClientProxy.FontContainer;
import noppes.npcs.config.ConfigClient;

import java.io.IOException;

public final class ConfigCommandPacket extends AbstractPacket {
    public static final String packetName = "Data|ConfigCommand";

    private EnumConfigOperation configOperation;
    private Object[] objects;

    public ConfigCommandPacket() {
    }

    public ConfigCommandPacket(EnumConfigOperation operation, Object... obs) {
        this.configOperation = operation;
        this.objects = obs;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.CONFIG_COMMAND;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.configOperation.ordinal());
        ByteBufUtils.fillBuffer(out, objects);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        EnumConfigOperation configType = EnumConfigOperation.values()[in.readInt()];
        if (configType == EnumConfigOperation.FONT) { // Font Configuration
            String font = ByteBufUtils.readString(in);
            if(font == null)
                return;

            int size = in.readInt();
            if (!font.isEmpty()) {
                ConfigClient.FontType = font;
                ConfigClient.FontSize = size;
                ClientProxy.Font = new FontContainer(ConfigClient.FontType, ConfigClient.FontSize);

                ConfigClient.FontTypeProperty.set(ConfigClient.FontType);
                ConfigClient.FontSizeProperty.set(ConfigClient.FontSize);

                if (ConfigClient.config.hasChanged()) {
                    ConfigClient.config.save();
                }

                player.addChatMessage(new ChatComponentTranslation("Font set to %s", ClientProxy.Font.getName()));
            } else {
                player.addChatMessage(new ChatComponentTranslation("Current font is %s", ClientProxy.Font.getName()));
            }
        }
    }
}
