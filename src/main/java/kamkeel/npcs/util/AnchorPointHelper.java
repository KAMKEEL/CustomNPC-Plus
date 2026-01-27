package kamkeel.npcs.util;

import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.ModelScalePart;

/**
 * Utility class for calculating world positions based on anchor points.
 * Supports both EntityNPCInterface and EntityPlayer with animation and model size awareness.
 */
public class AnchorPointHelper {

    // Default biped model constants (in model units, 1 unit = 1/16 block)
    private static final float MODEL_SCALE = 1f / 16f;

    // Shoulder position relative to model origin
    private static final float SHOULDER_Y = 2f;
    private static final float SHOULDER_X_OFFSET = 5f;  // Distance from center to shoulder

    // Palm position (in arm's local space, relative to shoulder)
    // Arm is 12 units long; palm is near the end
    private static final float PALM_DOWN = 11f;      // Distance down the arm to palm center
    private static final float PALM_FORWARD = 2f;    // Forward offset to palm surface (not center of hand)
    private static final float PALM_INWARD = 0f;     // Lateral offset (0 = centered on arm)

    // Default height offsets as fraction of entity height
    private static final float FRONT_HEIGHT = 0.7f;
    private static final float CENTER_HEIGHT = 0.5f;
    private static final float ARM_HEIGHT = 0.75f;
    private static final float ABOVE_HEAD_HEIGHT = 1.2f;
    private static final float CHEST_HEIGHT = 0.65f;

    // Distance in front for FRONT anchor
    private static final float DEFAULT_FRONT_DISTANCE = 1.0f;

    // Fallback arm positioning (when no animation)
    private static final float FALLBACK_LATERAL = 0.35f;
    private static final float FALLBACK_FORWARD = 0.3f;

    /**
     * Calculate world position for an anchor point on an entity.
     */
    public static Vec3 calculateAnchorPosition(EntityLivingBase entity, AnchorPoint anchor) {
        return calculateAnchorPosition(entity, anchor, DEFAULT_FRONT_DISTANCE);
    }

    /**
     * Calculate world position for an anchor point with custom front offset.
     */
    public static Vec3 calculateAnchorPosition(EntityLivingBase entity, AnchorPoint anchor, float frontOffset) {
        float scale = getModelScale(entity);
        float height = entity.height;

        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;

        float headYaw = (float) Math.toRadians(entity.rotationYawHead);
        float bodyYaw = (float) Math.toRadians(entity.renderYawOffset);

        switch (anchor) {
            case FRONT:
                x -= Math.sin(headYaw) * frontOffset * scale;
                y += height * FRONT_HEIGHT;
                z += Math.cos(headYaw) * frontOffset * scale;
                break;

            case CENTER:
                y += height * CENTER_HEIGHT;
                break;

            case RIGHT_HAND:
                return calculateHandPosition(entity, true, scale);

            case LEFT_HAND:
                return calculateHandPosition(entity, false, scale);

            case ABOVE_HEAD:
                y += height * ABOVE_HEAD_HEIGHT;
                break;

            case CHEST:
                y += height * CHEST_HEIGHT;
                break;
        }

        return Vec3.createVectorHelper(x, y, z);
    }

    /**
     * Get model scale factor for an entity.
     * NPCs have configurable model size, players use default scale.
     */
    private static float getModelScale(EntityLivingBase entity) {
        if (entity instanceof EntityNPCInterface) {
            int modelSize = ((EntityNPCInterface) entity).display.modelSize;
            return modelSize / 5f;  // modelSize 5 = 100% scale
        }
        return 1f;
    }

    // Default arm scale (no scaling)
    private static final ModelScalePart DEFAULT_ARM_SCALE = new ModelScalePart();

    /**
     * Get arm scale for an entity.
     * EntityCustomNpc has per-part scaling, others use default (1,1,1).
     */
    private static ModelScalePart getArmScale(EntityLivingBase entity) {
        if (entity instanceof EntityCustomNpc) {
            return ((EntityCustomNpc) entity).modelData.modelScale.arms;
        }
        return DEFAULT_ARM_SCALE;
    }

    /**
     * Calculate hand position using animation data if available.
     */
    private static Vec3 calculateHandPosition(EntityLivingBase entity, boolean rightHand, float scale) {
        // Try to get animated position first
        Vec3 animatedPos = getAnimatedHandPosition(entity, rightHand, scale);
        if (animatedPos != null) {
            return animatedPos;
        }

        // Fallback to static position
        return calculateFallbackHandPosition(entity, rightHand, scale);
    }

    /**
     * Calculate hand position from animation data.
     * Works for both NPCs and Players.
     */
    private static Vec3 getAnimatedHandPosition(EntityLivingBase entity, boolean rightHand, float scale) {
        AnimationData animData = AnimationData.getData(entity);
        if (animData == null || !animData.isActive() || animData.animation == null) {
            return null;
        }

        Frame frame = (Frame) animData.animation.currentFrame();
        if (frame == null) {
            return null;
        }

        EnumAnimationPart armPart = rightHand ? EnumAnimationPart.RIGHT_ARM : EnumAnimationPart.LEFT_ARM;
        FramePart part = frame.frameParts.get(armPart);
        if (part == null) {
            return null;
        }

        // Get arm scale for EntityCustomNpc
        ModelScalePart armScale = getArmScale(entity);

        // Get rotation and pivot values
        float[] rotations = getRotations(part, entity);
        float[] pivots = getPivots(part, entity);

        // Shoulder position in model space
        float shoulderX = rightHand ? -SHOULDER_X_OFFSET : SHOULDER_X_OFFSET;
        float shoulderY = SHOULDER_Y;
        float shoulderZ = 0f;

        // Add pivot offsets
        shoulderX += pivots[0];
        shoulderY += pivots[1];
        shoulderZ += pivots[2];

        // Palm offset in arm's local space (before rotation)
        // Apply arm scaling to the palm position
        float palmX = (rightHand ? -PALM_INWARD : PALM_INWARD) * armScale.scaleX;
        float palmY = PALM_DOWN * armScale.scaleY;
        float palmZ = PALM_FORWARD * armScale.scaleZ;

        // Apply arm rotations (order: X -> Y -> Z, matching GL's reverse application)
        double hx = palmX, hy = palmY, hz = palmZ;

        // X rotation first
        double cosX = Math.cos(rotations[0]);
        double sinX = Math.sin(rotations[0]);
        double newY = hy * cosX - hz * sinX;
        double newZ = hy * sinX + hz * cosX;
        hy = newY;
        hz = newZ;

        // Y rotation second
        double cosY = Math.cos(rotations[1]);
        double sinY = Math.sin(rotations[1]);
        double newX = hx * cosY + hz * sinY;
        newZ = -hx * sinY + hz * cosY;
        hx = newX;
        hz = newZ;

        // Z rotation last
        double cosZ = Math.cos(rotations[2]);
        double sinZ = Math.sin(rotations[2]);
        newX = hx * cosZ - hy * sinZ;
        newY = hx * sinZ + hy * cosZ;
        hx = newX;
        hy = newY;

        // Combine shoulder + rotated hand offset
        double modelX = shoulderX + hx;
        double modelY = shoulderY + hy;
        double modelZ = shoulderZ + hz;

        // Convert to blocks and apply model scale
        double blockX = modelX * MODEL_SCALE * scale;
        double blockY = modelY * MODEL_SCALE * scale;
        double blockZ = modelZ * MODEL_SCALE * scale;

        // Apply body yaw rotation
        // Renderer uses glRotate(180 - yaw), so: cos(180-yaw) = -cos(yaw), sin(180-yaw) = sin(yaw)
        // Y-axis rotation: x' = x*cos + z*sin, z' = -x*sin + z*cos
        // Substituting: x' = -x*cos(yaw) + z*sin(yaw), z' = -x*sin(yaw) - z*cos(yaw)
        float bodyYaw = (float) Math.toRadians(entity.renderYawOffset);
        double cosYaw = Math.cos(bodyYaw);
        double sinYaw = Math.sin(bodyYaw);

        double worldOffsetX = blockX * cosYaw + blockZ * sinYaw;
        double worldOffsetZ = blockX * sinYaw - blockZ * cosYaw;

        // Calculate shoulder height based on entity and scale
        float shoulderHeight = entity.height * ARM_HEIGHT;

        // Final world position
        double worldX = entity.posX + worldOffsetX;
        double worldY = entity.posY + shoulderHeight - blockY;
        double worldZ = entity.posZ + worldOffsetZ;

        return Vec3.createVectorHelper(worldX, worldY, worldZ);
    }

    /**
     * Get rotation values from FramePart.
     * Uses interpolated values on client, raw values on server.
     */
    private static float[] getRotations(FramePart part, EntityLivingBase entity) {
        if (entity.worldObj.isRemote) {
            // Client side - use interpolated values (already in radians)
            part.interpolateAngles();
            return part.prevRotations;
        } else {
            // Server side - convert raw degrees to radians
            float pi = (float) Math.PI / 180f;
            return new float[]{
                part.rotation[0] * pi,
                part.rotation[1] * pi,
                part.rotation[2] * pi
            };
        }
    }

    /**
     * Get pivot values from FramePart.
     * Uses interpolated values on client, raw values on server.
     */
    private static float[] getPivots(FramePart part, EntityLivingBase entity) {
        if (entity.worldObj.isRemote) {
            // Client side - use interpolated values
            part.interpolateOffset();
            return part.prevPivots;
        } else {
            // Server side - use raw values
            return part.pivot;
        }
    }

    /**
     * Calculate fallback hand position when no animation is active.
     */
    private static Vec3 calculateFallbackHandPosition(EntityLivingBase entity, boolean rightHand, float scale) {
        float bodyYaw = (float) Math.toRadians(entity.renderYawOffset);

        // Get arm scale for EntityCustomNpc
        ModelScalePart armScale = getArmScale(entity);

        // Perpendicular direction for lateral offset
        float perpYaw = bodyYaw + (rightHand ? (float) (Math.PI / 2) : (float) (-Math.PI / 2));

        // Calculate offsets scaled by model size and arm scale
        double lateralX = -Math.sin(perpYaw) * FALLBACK_LATERAL * scale * armScale.scaleX;
        double lateralZ = Math.cos(perpYaw) * FALLBACK_LATERAL * scale * armScale.scaleX;
        double forwardX = -Math.sin(bodyYaw) * FALLBACK_FORWARD * scale * armScale.scaleZ;
        double forwardZ = Math.cos(bodyYaw) * FALLBACK_FORWARD * scale * armScale.scaleZ;

        // Arm length affects vertical position
        double armLengthOffset = (1 - armScale.scaleY) * 0.3;

        double x = entity.posX + lateralX + forwardX;
        double y = entity.posY + entity.height * ARM_HEIGHT - armLengthOffset;
        double z = entity.posZ + lateralZ + forwardZ;

        return Vec3.createVectorHelper(x, y, z);
    }

    /**
     * Get default height multiplier for an anchor point.
     */
    public static float getHeightMultiplier(AnchorPoint anchor) {
        switch (anchor) {
            case FRONT:
                return FRONT_HEIGHT;
            case CENTER:
                return CENTER_HEIGHT;
            case RIGHT_HAND:
            case LEFT_HAND:
                return ARM_HEIGHT;
            case ABOVE_HEAD:
                return ABOVE_HEAD_HEIGHT;
            case CHEST:
                return CHEST_HEIGHT;
            default:
                return FRONT_HEIGHT;
        }
    }
}
