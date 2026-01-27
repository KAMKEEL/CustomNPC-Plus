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

    // ==================== TUNABLE CONSTANTS ====================
    // These can be adjusted to fine-tune positioning

    // Shoulder position in model units (default biped arm rotation points)
    private static final float SHOULDER_X_RIGHT = -5.0f;  // Right arm is on the right side (-X in model space)
    private static final float SHOULDER_X_LEFT = 5.0f;    // Left arm is on the left side (+X in model space)
    private static final float SHOULDER_Y = 2.0f;         // Shoulder height from model origin
    private static final float SHOULDER_Z = 0.0f;         // Shoulder depth

    // Hand offset in model units (where items are held, relative to shoulder after rotation)
    // In arm's local space: X = toward/away from body, Y = down the arm, Z = forward/back
    private static float HAND_OFFSET_X_RIGHT = -2.0f;  // Slightly toward body for right
    private static float HAND_OFFSET_X_LEFT = 2.0f;    // Slightly toward body for left
    private static float HAND_OFFSET_Y = 14.0f;        // Down the arm (arm is ~12 units long)
    private static float HAND_OFFSET_Z = 0.0f;         // Forward from arm

    // Height of shoulder relative to entity position (feet)
    private static final float SHOULDER_HEIGHT_BLOCKS = 1.35f;

    // Model scale factor (1/16 - converts model units to blocks)
    private static final float MODEL_SCALE = 0.0625f;

    // Fallback offsets (in blocks) when no animation
    private static final float ARM_LATERAL_OFFSET = 0.35f;
    private static final float ARM_FORWARD_OFFSET = 0.3f;
    private static final float FRONT_OFFSET = 1.0f;

    // Height multipliers relative to eye height
    private static final float FRONT_HEIGHT_MULT = 0.7f;
    private static final float CENTER_HEIGHT_MULT = 0.5f;
    private static final float ARM_HEIGHT_MULT = 0.75f;
    private static final float ABOVE_HEAD_HEIGHT_MULT = 1.3f;
    private static final float CHEST_HEIGHT_MULT = 0.5f;

    /**
     * Calculate the world position for a given anchor point on an entity.
     */
    public static Vec3 calculateAnchorPosition(EntityLivingBase entity, AnchorPoint anchor) {
        return calculateAnchorPosition(entity, anchor, FRONT_OFFSET);
    }

    /**
     * Calculate the world position for a given anchor point on an entity.
     */
    public static Vec3 calculateAnchorPosition(EntityLivingBase entity, AnchorPoint anchor, float frontOffset) {
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        float eyeHeight = entity.getEyeHeight();

        // Body yaw in radians (used for arm positioning)
        // Minecraft: yaw 0 = south (+Z), 90 = west (-X), 180 = north (-Z), 270 = east (+X)
        float bodyYawRad = (float) Math.toRadians(entity.renderYawOffset);
        float headYawRad = (float) Math.toRadians(entity.rotationYawHead);

        switch (anchor) {
            case FRONT:
                // In front of entity face (use head yaw)
                x -= Math.sin(headYawRad) * frontOffset;
                y += eyeHeight * FRONT_HEIGHT_MULT;
                z += Math.cos(headYawRad) * frontOffset;
                break;

            case CENTER:
                y += eyeHeight * CENTER_HEIGHT_MULT;
                break;

            case RIGHT_HAND:
                Vec3 rightPos = getAnimatedHandPosition(entity, true);
                if (rightPos != null) {
                    return rightPos;
                }
                // Fallback
                return calculateFallbackHandPosition(entity, bodyYawRad, true);

            case LEFT_HAND:
                Vec3 leftPos = getAnimatedHandPosition(entity, false);
                if (leftPos != null) {
                    return leftPos;
                }
                // Fallback
                return calculateFallbackHandPosition(entity, bodyYawRad, false);

            case ABOVE_HEAD:
                y += eyeHeight * ABOVE_HEAD_HEIGHT_MULT;
                break;

            case CHEST:
                y += eyeHeight * CHEST_HEIGHT_MULT;
                break;
        }

        return Vec3.createVectorHelper(x, y, z);
    }

    /**
     * Calculate hand position using NPC animation data.
     * Uses the same transformation logic as the renderer.
     */
    private static Vec3 getAnimatedHandPosition(EntityLivingBase entity, boolean rightArm) {
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

        // === STEP 1: Get shoulder position in model space ===
        float shoulderX = rightArm ? SHOULDER_X_RIGHT : SHOULDER_X_LEFT;
        float shoulderY = SHOULDER_Y;
        float shoulderZ = SHOULDER_Z;

        // Add animation pivot offsets
        if (part.prevPivots != null) {
            shoulderX += part.prevPivots[0];
            shoulderY += part.prevPivots[1];
            shoulderZ += part.prevPivots[2];
        }

        // === STEP 2: Get hand offset in arm's local space ===
        float handX = rightArm ? HAND_OFFSET_X_RIGHT : HAND_OFFSET_X_LEFT;
        float handY = HAND_OFFSET_Y;
        float handZ = HAND_OFFSET_Z;

        // === STEP 3: Apply arm rotations to hand offset (Z → Y → X order) ===
        // Rotations are in radians (already converted by interpolateAngles)
        float rotX = part.prevRotations != null ? part.prevRotations[0] : 0;
        float rotY = part.prevRotations != null ? part.prevRotations[1] : 0;
        float rotZ = part.prevRotations != null ? part.prevRotations[2] : 0;

        // Start with hand offset
        double hx = handX;
        double hy = handY;
        double hz = handZ;

        // Apply Z rotation first
        double cosZ = Math.cos(rotZ);
        double sinZ = Math.sin(rotZ);
        double newX = hx * cosZ - hy * sinZ;
        double newY = hx * sinZ + hy * cosZ;
        hx = newX;
        hy = newY;

        // Apply Y rotation
        double cosY = Math.cos(rotY);
        double sinY = Math.sin(rotY);
        newX = hx * cosY + hz * sinY;
        double newZ = -hx * sinY + hz * cosY;
        hx = newX;
        hz = newZ;

        // Apply X rotation
        double cosX = Math.cos(rotX);
        double sinX = Math.sin(rotX);
        newY = hy * cosX - hz * sinX;
        newZ = hy * sinX + hz * cosX;
        hy = newY;
        hz = newZ;

        // === STEP 4: Combine shoulder + rotated hand offset (in model units) ===
        double modelX = shoulderX + hx;
        double modelY = shoulderY + hy;
        double modelZ = shoulderZ + hz;

        // === STEP 5: Convert to blocks ===
        double blockX = modelX * MODEL_SCALE;
        double blockY = modelY * MODEL_SCALE;
        double blockZ = modelZ * MODEL_SCALE;

        // === STEP 6: Apply body yaw rotation ===
        // In model space: +X = left, -X = right, +Z = back, -Z = front
        // Need to rotate to align with entity's facing direction
        float bodyYaw = (float) Math.toRadians(entity.renderYawOffset);
        double cosYaw = Math.cos(bodyYaw);
        double sinYaw = Math.sin(bodyYaw);

        // Rotate around Y axis (body rotation)
        double worldOffsetX = blockX * cosYaw + blockZ * sinYaw;
        double worldOffsetZ = -blockX * sinYaw + blockZ * cosYaw;

        // === STEP 7: Final world position ===
        double worldX = entity.posX + worldOffsetX;
        double worldY = entity.posY + SHOULDER_HEIGHT_BLOCKS - blockY;  // Subtract because +Y in model is down
        double worldZ = entity.posZ + worldOffsetZ;

        return Vec3.createVectorHelper(worldX, worldY, worldZ);
    }

    /**
     * Calculate fallback hand position when no animation is active.
     */
    private static Vec3 calculateFallbackHandPosition(EntityLivingBase entity, float bodyYawRad, boolean rightHand) {
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        float eyeHeight = entity.getEyeHeight();

        // Perpendicular direction for lateral offset
        float perpYaw = bodyYawRad + (float) (rightHand ? Math.PI / 2 : -Math.PI / 2);

        // Lateral offset (perpendicular to body facing)
        double lateralX = -Math.sin(perpYaw) * ARM_LATERAL_OFFSET;
        double lateralZ = Math.cos(perpYaw) * ARM_LATERAL_OFFSET;

        // Forward offset (in facing direction)
        double forwardX = -Math.sin(bodyYawRad) * ARM_FORWARD_OFFSET;
        double forwardZ = Math.cos(bodyYawRad) * ARM_FORWARD_OFFSET;

        x += lateralX + forwardX;
        y += eyeHeight * ARM_HEIGHT_MULT;
        z += lateralZ + forwardZ;

        return Vec3.createVectorHelper(x, y, z);
    }

    /**
     * Get the height multiplier for a given anchor point.
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
