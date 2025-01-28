package kamkeel.network.packets.client.sound;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.controllers.ScriptClientSound;
import noppes.npcs.client.controllers.ScriptSoundController;

import java.io.IOException;

public final class PlaySoundToNoIdPacket extends AbstractPacket {
    public static final String packetName = "Client|PlaySoundToNoId";

    public PlaySoundToNoIdPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.PLAY_SOUND_TO_NO_ID;
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
        NBTTagCompound nbt = Server.readNBT(in);
        ScriptClientSound sound = ScriptClientSound.fromScriptSound(nbt, player.worldObj);
        ScriptSoundController.Instance.playSound(sound);
    }
}
