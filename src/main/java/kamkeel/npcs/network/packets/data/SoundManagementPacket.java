package kamkeel.npcs.network.packets.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.network.enums.EnumSoundOperation;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.ScriptClientSound;
import noppes.npcs.client.controllers.ScriptSoundController;

import java.io.IOException;

public final class SoundManagementPacket extends AbstractPacket {
    private EnumSoundOperation operation;
    private String soundName;
    private float x, y, z;
    private NBTTagCompound nbt;
    private int soundId;

    public SoundManagementPacket() {
    }

    public SoundManagementPacket(EnumSoundOperation operation) {
        this.operation = operation;
    }

    public SoundManagementPacket(EnumSoundOperation operation, String soundName) {
        this.operation = operation;
        this.soundName = soundName;
    }

    public SoundManagementPacket(EnumSoundOperation operation, String soundName, float x, float y, float z) {
        this.operation = operation;
        this.soundName = soundName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SoundManagementPacket(EnumSoundOperation operation, int soundId, NBTTagCompound nbt) {
        this.operation = operation;
        this.soundId = soundId;
        this.nbt = nbt;
    }

    public SoundManagementPacket(EnumSoundOperation operation, NBTTagCompound nbt) {
        this.operation = operation;
        this.nbt = nbt;
    }

    public SoundManagementPacket(EnumSoundOperation operation, int soundId) {
        this.operation = operation;
        this.soundId = soundId;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.SOUND;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(operation.ordinal());
        switch (operation) {
            case PLAY_MUSIC:
            case PLAY_SOUND:
                ByteBufUtils.writeString(out, soundName);
                if (operation == EnumSoundOperation.PLAY_SOUND) {
                    out.writeFloat(x);
                    out.writeFloat(y);
                    out.writeFloat(z);
                }
                break;
            case PLAY_SOUND_TO:
                out.writeInt(soundId);
                ByteBufUtils.writeNBT(out, nbt);
                break;
            case PLAY_SOUND_TO_NO_ID:
                ByteBufUtils.writeNBT(out, nbt);
                break;
            case STOP_SOUND_FOR:
                out.writeInt(soundId);
                break;
            default:
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        EnumSoundOperation operation = EnumSoundOperation.values()[in.readInt()];
        switch (operation) {
            case CONTINUE_SOUNDS:
                ScriptSoundController.Instance.continueAllSounds();
                break;
            case PAUSE_SOUNDS:
                ScriptSoundController.Instance.pauseAllSounds();
                break;
            case PLAY_MUSIC:
                String soundName = ByteBufUtils.readString(in);
                MusicController.Instance.playMusicBackground(soundName, player, Integer.MAX_VALUE);
                break;
            case PLAY_SOUND:
                String name = ByteBufUtils.readString(in);
                float x = in.readFloat();
                float y = in.readFloat();
                float z = in.readFloat();
                MusicController.Instance.playSound(name, x, y, z);
                break;
            case PLAY_SOUND_TO:
                int id = in.readInt();
                NBTTagCompound comp = ByteBufUtils.readNBT(in);
                ScriptClientSound sound = ScriptClientSound.fromScriptSound(comp, player.worldObj);
                ScriptSoundController.Instance.playSound(id, sound);
                break;
            case PLAY_SOUND_TO_NO_ID:
                NBTTagCompound tagCompound = ByteBufUtils.readNBT(in);
                ScriptClientSound soundNoId = ScriptClientSound.fromScriptSound(tagCompound, player.worldObj);
                ScriptSoundController.Instance.playSound(soundNoId);
                break;
            case STOP_SOUND_FOR:
                int stopID = in.readInt();
                ScriptSoundController.Instance.stopSound(stopID);
                break;
            case STOP_SOUNDS:
                ScriptSoundController.Instance.stopAllSounds();
                break;
            default:
                break;
        }
    }
}
