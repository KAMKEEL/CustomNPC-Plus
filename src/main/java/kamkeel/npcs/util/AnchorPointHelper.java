package kamkeel.npcs.util;

import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Utility class for calculating world positions based on anchor points.
 * Used for positioning charging effects on different body parts.
 * Animation-aware: reads arm positions from NPC animation data when available.
 */
public class AnchorPointHelper {

    // Model coordinate scale factor (model coords to blocks)
    private static final float MODEL_SCALE = 0.0625f; // 1/16

    // Default offsets (in blocks)
    private static final float ARM_LATERAL_OFFSET = 0.35f;   // ~5.5 pixels lateral from center
    private static final float ARM_FORWARD_OFFSET = 0.25f;   // Arm extended slightly forward
    private static final float FRONT_OFFSET = 1.0f;          // Distance in front for FRONT anchor

    // Height multipliers relative to eye height
    private static final float FRONT_HEIGHT_MULT = 0.7f;
    private static final float CENTER_HEIGHT_MULT = 0.5f;
    private static final float ARM_HEIGHT_MULT = 0.75f;
    private static final float ABOVE_HEAD_HEIGHT_MULT = 1.3f;
    private static final float CHEST_HEIGHT_MULT = 0.5f;

    /**
     * Calculate the world position for a given anchor point on an entity.
     *
     * @param entity The entity to calculate the anchor position for
     * @param anchor The anchor point type
     * @return Vec3 containing the world coordinates for the anchor point
     */
    public static Vec3 calculateAnchorPosition(EntityLivingBase entity, AnchorPoint anchor) {
        return calculateAnchorPosition(entity, anchor, FRONT_OFFSET);
    }

    /**
     * Calculate the world position for a given anchor point on an entity.
     * For NPCs with active animations, this will read the actual arm positions.
     *
     * @param entity The entity to calculate the anchor position for
     * @param anchor The anchor point type
     * @param frontOffset Custom offset distance for FRONT anchor point
     * @return Vec3 containing the world coordinates for the anchor point
     */
    public static Vec3 calculateAnchorPosition(EntityLivingBase entity, AnchorPoint anchor, float frontOffset) {
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        float eyeHeight = entity.getEyeHeight();

        // Get yaw in radians (use body yaw for arm positioning, head yaw for facing)
        float bodyYaw = (float) Math.toRadians(entity.renderYawOffset);
        float headYaw = (float) Math.toRadians(entity.rotationYawHead);

        switch (anchor) {
            case FRONT:
                // In front of entity face (use head yaw)
                x -= Math.sin(headYaw) * frontOffset;
                y += eyeHeight * FRONT_HEIGHT_MULT;
                z += Math.cos(headYaw) * frontOffset;
                break;

            case CENTER:
                // Entity center
                y += eyeHeight * CENTER_HEIGHT_MULT;
                break;

            case RIGHT_HAND:
                // Try to get animated arm position
                Vec3 rightArmPos = getAnimatedArmPosition(entity, true);
                if (rightArmPos != null) {
                    return rightArmPos;
                }
                // Fallback: calculated position using body yaw
                x += calculateHandOffsetX(bodyYaw, true);
                y += eyeHeight * ARM_HEIGHT_MULT;
                z += calculateHandOffsetZ(bodyYaw, true);
                break;

            case LEFT_HAND:
                // Try to get animated arm position
                Vec3 leftArmPos = getAnimatedArmPosition(entity, false);
                if (leftArmPos != null) {
                    return leftArmPos;
                }
                // Fallback: calculated position using body yaw
                x += calculateHandOffsetX(bodyYaw, false);
                y += eyeHeight * ARM_HEIGHT_MULT;
                z += calculateHandOffsetZ(bodyYaw, false);
                break;

            case ABOVE_HEAD:
                // Above the entity
                y += eyeHeight * ABOVE_HEAD_HEIGHT_MULT;
                break;

            case CHEST:
                // Chest level, centered
                y += eyeHeight * CHEST_HEIGHT_MULT;
                break;
        }

        return Vec3.createVectorHelper(x, y, z);
    }

    /**
     * Get the animated arm position from NPC animation data.
     * Uses Minecraft's hand positioning logic from RenderNPCHumanMale.
     * Hand offset values: (-0.0625, 0.4375, 0.0625) for right hand after arm postRender.
     *
     * @param entity The entity (must be EntityNPCInterface for animation support)
     * @param rightArm True for right arm, false for left arm
     * @return Vec3 world position if animation data available, null otherwise
     */
    private static Vec3 getAnimatedArmPosition(EntityLivingBase entity, boolean rightArm) {
        if (!(entity instanceof EntityNPCInterface)) {
            return null;
        }

        EntityNPCInterface npc = (EntityNPCInterface) entity;
        AnimationData animData = npc.display.animationData;

        if (animData == null || !animData.isActive() || animData.animation == null) {
            return null;
        }

        Frame frame = (Frame) animData.animation.currentFrame();
        if (frame == null) {
            return null;
        }

        EnumAnimationPart armPart = rightArm ? EnumAnimationPart.RIGHT_ARM : EnumAnimationPart.LEFT_ARM;
        if (!frame.frameParts.containsKey(armPart)) {
            return null;
        }

        FramePart part = frame.frameParts.get(armPart);
        if (part == null) {
            return null;
        }

        // Ensure interpolation is up to date
        part.interpolateOffset();
        part.interpolateAngles();

        // Get body yaw for world rotation
        float bodyYaw = (float) Math.toRadians(entity.renderYawOffset);

        // === SHOULDER POSITION (model coordinates, in model units) ===
        // Right arm pivot: X = -5 (right side), Y = -2 (below head), Z = 0
        // Left arm pivot: X = 5 (left side), Y = -2, Z = 0
        // Note: In Minecraft model coords, Y=0 is at head level, shoulder is at Y=-2
        float shoulderModelX = rightArm ? -5.0f : 5.0f;
        float shoulderModelY = -2.0f;
        float shoulderModelZ = 0.0f;

        // Add animation pivot offsets (if any)
        if (part.prevPivots != null) {
            shoulderModelX += part.prevPivots[0];
            shoulderModelY += part.prevPivots[1];
            shoulderModelZ += part.prevPivots[2];
        }

        // === HAND OFFSET IN ARM'S LOCAL SPACE (from RenderNPCHumanMale) ===
        // These values are in blocks, applied AFTER postRender transforms
        // Right: (-0.0625, 0.4375, 0.0625), Left: (0.0625, 0.4375, 0.0625)
        float handLocalX = rightArm ? -0.0625f : 0.0625f;
        float handLocalY = 0.4375f;  // 7 model units down the arm
        float handLocalZ = 0.0625f;  // Slightly forward

        // === ARM ROTATION ANGLES ===
        float rotX = part.prevRotations != null ? part.prevRotations[0] : 0;
        float rotY = part.prevRotations != null ? part.prevRotations[1] : 0;
        float rotZ = part.prevRotations != null ? part.prevRotations[2] : 0;

        // === APPLY ARM ROTATIONS TO HAND OFFSET ===
        // Rotation order matches ModelRenderer.postRender: Z, Y, X
        double hx = handLocalX;
        double hy = handLocalY;
        double hz = handLocalZ;

        // Rotate around Z axis
        double cosZ = Math.cos(rotZ);
        double sinZ = Math.sin(rotZ);
        double tempX = hx * cosZ - hy * sinZ;
        double tempY = hx * sinZ + hy * cosZ;
        hx = tempX;
        hy = tempY;

        // Rotate around Y axis
        double cosY = Math.cos(rotY);
        double sinY = Math.sin(rotY);
        tempX = hx * cosY + hz * sinY;
        double tempZ = -hx * sinY + hz * cosY;
        hx = tempX;
        hz = tempZ;

        // Rotate around X axis
        double cosX = Math.cos(rotX);
        double sinX = Math.sin(rotX);
        tempY = hy * cosX - hz * sinX;
        tempZ = hy * sinX + hz * cosX;
        hy = tempY;
        hz = tempZ;

        // === COMBINE SHOULDER + ROTATED HAND OFFSET ===
        // Convert shoulder from model units to blocks
        double worldOffsetX = shoulderModelX * MODEL_SCALE + hx;
        double worldOffsetY = shoulderModelY * MODEL_SCALE + hy;
        double worldOffsetZ = shoulderModelZ * MODEL_SCALE + hz;

        // === ROTATE BY BODY YAW ===
        double cosYaw = Math.cos(bodyYaw);
        double sinYaw = Math.sin(bodyYaw);
        double rotatedX = worldOffsetX * cosYaw + worldOffsetZ * sinYaw;
        double rotatedZ = -worldOffsetX * sinYaw + worldOffsetZ * cosYaw;

        // === FINAL WORLD POSITION ===
        // Entity posY is at feet, shoulder height is approximately eyeHeight * 0.9
        double shoulderWorldY = entity.posY + entity.getEyeHeight() * 0.9;

        double worldX = entity.posX + rotatedX;
        double worldY = shoulderWorldY + worldOffsetY;
        double worldZ = entity.posZ + rotatedZ;

        return Vec3.createVectorHelper(worldX, worldY, worldZ);
    }

    /**
     * Calculate the X offset for hand positions (fallback when no animation).
     *
     * @param yaw Entity's body yaw in radians
     * @param rightHand True for right hand, false for left hand
     * @return X offset in world coordinates
     */
    private static double calculateHandOffsetX(float yaw, boolean rightHand) {
        // Perpendicular direction (90 degrees from look direction)
        // Right hand: +90 degrees, Left hand: -90 degrees
        float perpYaw = yaw + (float) (rightHand ? Math.PI / 2 : -Math.PI / 2);

        // Lateral offset (perpendicular to look direction)
        double lateralX = Math.sin(perpYaw) * ARM_LATERAL_OFFSET;

        // Forward offset (in look direction)
        double forwardX = -Math.sin(yaw) * ARM_FORWARD_OFFSET;

        return lateralX + forwardX;
    }

    /**
     * Calculate the Z offset for hand positions (fallback when no animation).
     *
     * @param yaw Entity's body yaw in radians
     * @param rightHand True for right hand, false for left hand
     * @return Z offset in world coordinates
     */
    private static double calculateHandOffsetZ(float yaw, boolean rightHand) {
        // Perpendicular direction (90 degrees from look direction)
        float perpYaw = yaw + (float) (rightHand ? Math.PI / 2 : -Math.PI / 2);

        // Lateral offset (perpendicular to look direction)
        double lateralZ = -Math.cos(perpYaw) * ARM_LATERAL_OFFSET;

        // Forward offset (in look direction)
        double forwardZ = Math.cos(yaw) * ARM_FORWARD_OFFSET;

        return lateralZ + forwardZ;
    }

    /**
     * Get the height multiplier for a given anchor point.
     * Useful for external code that needs to know the Y position offset.
     *
     * @param anchor The anchor point type
     * @return Height multiplier relative to eye height
     */
    public static float getHeightMultiplier(AnchorPoint anchor) {
        switch (anchor) {
            case FRONT:
                return FRONT_HEIGHT_MULT;
            case CENTER:
                return CENTER_HEIGHT_MULT;
            case RIGHT_HAND:
            case LEFT_HAND:
                return ARM_HEIGHT_MULT;
            case ABOVE_HEAD:
                return ABOVE_HEAD_HEIGHT_MULT;
            case CHEST:
                return CHEST_HEIGHT_MULT;
            default:
                return FRONT_HEIGHT_MULT;
        }
    }
}
