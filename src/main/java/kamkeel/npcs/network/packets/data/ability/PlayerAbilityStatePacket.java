package kamkeel.npcs.network.packets.data.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.client.ClientAbilityState;

import java.io.IOException;

/**
 * Lightweight packet syncing ability lock state from server to client.
 * Sent every tick during ability execution when flags change, and once when ability ends.
 * <p>
 * Flags byte layout:
 * bit 0: movement locked (suppress WASD, zero motion)
 * bit 1: rotation locked (freeze yaw/pitch)
 * bit 2: has ability movement (ability controls motion — suppress WASD but don't zero)
 * bit 3: position locked (freeze position)
 * bit 5: ability is in ACTIVE phase
 */
public final class PlayerAbilityStatePacket extends AbstractPacket {
    public static final String packetName = "Data|PlayerAbilityState";

    public static final byte FLAG_MOVEMENT_LOCKED = 1;
    public static final byte FLAG_ROTATION_LOCKED = 2;
    public static final byte FLAG_HAS_ABILITY_MOVEMENT = 4;
    public static final byte FLAG_POSITION_LOCKED = 8;
    public static final byte FLAG_WAS_FLYING_AT_LOCK = 16;
    public static final byte FLAG_ACTIVE_PHASE = 32;

    private byte flags;
    private float lockedYaw;
    private float lockedPitch;

    public PlayerAbilityStatePacket() {
    }

    public PlayerAbilityStatePacket(byte flags, float lockedYaw, float lockedPitch) {
        this.flags = flags;
        this.lockedYaw = lockedYaw;
        this.lockedPitch = lockedPitch;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.PLAYER_ABILITY_STATE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeByte(flags);
        if ((flags & FLAG_ROTATION_LOCKED) != 0) {
            out.writeFloat(lockedYaw);
            out.writeFloat(lockedPitch);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        byte flags = in.readByte();
        float yaw = 0, pitch = 0;
        if ((flags & FLAG_ROTATION_LOCKED) != 0) {
            yaw = in.readFloat();
            pitch = in.readFloat();
        }
        ClientAbilityState.update(flags, yaw, pitch);
    }

    /**
     * Send the current ability state to the player's client.
     */
    public static void sendToPlayer(EntityPlayerMP player, byte flags, float lockedYaw, float lockedPitch) {
        PacketHandler.Instance.sendToPlayer(new PlayerAbilityStatePacket(flags, lockedYaw, lockedPitch), player);
    }
}
