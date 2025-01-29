package kamkeel.npcs.network.packets.client.sound;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.controllers.ScriptClientSound;
import noppes.npcs.client.controllers.ScriptSoundController;

import java.io.IOException;

public final class PlaySoundToPacket extends AbstractPacket {
    public static final String packetName = "Client|PlaySoundTo";

    public PlaySoundToPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.PLAY_SOUND_TO;
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
        int soundId = in.readInt();
        NBTTagCompound nbt = Server.readNBT(in);
        ScriptClientSound sound = ScriptClientSound.fromScriptSound(nbt, player.worldObj);
        ScriptSoundController.Instance.playSound(soundId, sound);
    }
}
