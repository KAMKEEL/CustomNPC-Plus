package kamkeel.npcs.network.packets.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class SpecialKeyStatePacket extends AbstractPacket {
    private boolean pressed;

    public SpecialKeyStatePacket() {
    }

    private SpecialKeyStatePacket(boolean pressed) {
        this.pressed = pressed;
    }

    public static void send(boolean pressed) {
        PacketClient.sendClient(new SpecialKeyStatePacket(pressed));
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.SpecialKeyState;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        out.writeBoolean(pressed);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        boolean keyDown = in.readBoolean();
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        PlayerData data = PlayerDataController.Instance.getPlayerData((EntityPlayerMP) player);
        if (data != null) {
            data.setSpecialKeyDown(keyDown);
        }
    }
}
