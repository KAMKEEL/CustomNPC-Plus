package noppes.npcs.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.network.packets.data.ability.PlayerAbilityStatePacket;

/**
 * Client-side state holder for player ability lock flags.
 * Updated by {@link PlayerAbilityStatePacket} from the server.
 * Read by {@link ClientTickHandler} to suppress WASD input and enforce rotation.
 */
@SideOnly(Side.CLIENT)
public class ClientAbilityState {
    public static boolean movementLocked = false;
    public static boolean rotationLocked = false;
    public static boolean hasAbilityMovement = false;
    public static boolean positionLocked = false;
    public static boolean wasFlyingAtLock = false;
    public static float lockedYaw = 0;
    public static float lockedPitch = 0;

    /**
     * Update state from a received packet.
     */
    public static void update(byte flags, float yaw, float pitch) {
        movementLocked = (flags & PlayerAbilityStatePacket.FLAG_MOVEMENT_LOCKED) != 0;
        rotationLocked = (flags & PlayerAbilityStatePacket.FLAG_ROTATION_LOCKED) != 0;
        hasAbilityMovement = (flags & PlayerAbilityStatePacket.FLAG_HAS_ABILITY_MOVEMENT) != 0;
        positionLocked = (flags & PlayerAbilityStatePacket.FLAG_POSITION_LOCKED) != 0;
        wasFlyingAtLock = (flags & PlayerAbilityStatePacket.FLAG_WAS_FLYING_AT_LOCK) != 0;
        lockedYaw = yaw;
        lockedPitch = pitch;
    }

    /**
     * Whether player WASD movement input should be suppressed.
     * True when movement/position is locked or ability has its own movement.
     */
    public static boolean shouldSuppressMovementInput() {
        return movementLocked || hasAbilityMovement || positionLocked;
    }

    /**
     * Whether player camera rotation should be locked.
     */
    public static boolean shouldLockRotation() {
        return rotationLocked;
    }

    /**
     * Reset all state. Called on world change / disconnect.
     */
    public static void reset() {
        movementLocked = false;
        rotationLocked = false;
        hasAbilityMovement = false;
        positionLocked = false;
        wasFlyingAtLock = false;
        lockedYaw = 0;
        lockedPitch = 0;
    }
}
