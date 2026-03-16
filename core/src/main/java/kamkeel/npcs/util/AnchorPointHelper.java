package kamkeel.npcs.util;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.ability.enums.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
/**
 * Utility class for calculating IWorld positions based on anchor points.
 * Supports both EntityNPCInterface and IPlayer with animation and model size awareness.
 */
public class AnchorPointHelper {

    // Y offset correction for the local client player.
    // EntityPlayerSP.posY is at feet, but the renderer adds yOffset (1.62) visually,
    // so anchor positions computed from posY appear ~1.6 blocks too high for the local player.
    // Remote players and NPCs don't have this discrepancy.
    private static final float CLIENT_PLAYER_Y_OFFSET = -1.6F;

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

    // Default height offsets as fraction of IEntity height
    private static final float FRONT_HEIGHT = 0.7f;
    private static final float CENTER_HEIGHT = 0.5f;
    private static final float ARM_HEIGHT = 0.75f;
    private static final float ABOVE_HEAD_HEIGHT = 1.2f;
    private static final float CHEST_HEIGHT = 0.65f;
    private static final float EYE_HEIGHT = 0.85f;

    // Distance in front for FRONT anchor
    private static final float DEFAULT_FRONT_DISTANCE = 1.0f;

    // Fallback arm positioning (when no animation, arms hang at sides)
    private static final float FALLBACK_LATERAL = 0.35f;   // Distance from body center to arm
    private static final float FALLBACK_FORWARD = 0.1f;    // Slight forward offset
    private static final float FALLBACK_ARM_HEIGHT = 0.45f; // Hand height when arms hang down (near hip)

    // Default arm scale (no scaling)
    private static final ModelScalePart DEFAULT_ARM_SCALE = new ModelScalePart();

    /**
     * Calculate IWorld position for an anchor point on an IEntity.
     */
    public static IVector3 calculateAnchorPosition(IEntityLivingBase IEntity, EnergyAnchorData anchorData) {
        return calculateAnchorPosition(IEntity, anchorData, DEFAULT_FRONT_DISTANCE);
    }

    /**
     * Calculate IWorld position for an anchor point with custom front offset.
     */
    public static IVector3 calculateAnchorPosition(IEntityLivingBase IEntity, EnergyAnchorData anchorData, float frontOffset) {
        float scale = getModelScale(IEntity);
        float height = IEntity.height;

        AnchorPoint anchor = anchorData.anchorPoint;

        float anchorX = anchorData.anchorOffsetX;
        float anchorY = anchorData.anchorOffsetY;
        float anchorZ = anchorData.anchorOffsetZ;

        double x = IEntity.posX;
        // EYE uses getEyeHeight() which already gives the correct offset from posY,
        // so the client player correction would double-subtract and place it at feet level
        double y = IEntity.posY + (anchor == AnchorPoint.EYE ? 0 : getClientPlayerYCorrection(IEntity));
        double z = IEntity.posZ;

        float bodyYaw = (float) Math.toRadians(IEntity.renderYawOffset);

        switch (anchor) {
            case FRONT:
                IVector3 look = IEntity.getLookVec();
                x += look.xCoord * frontOffset * scale;
                y += height * FRONT_HEIGHT + look.yCoord * frontOffset * scale;
                z += look.zCoord * frontOffset * scale;
                break;

            case CENTER:
                y += height * CENTER_HEIGHT;
                break;

            case RIGHT_HAND:
                return calculateHandPosition(IEntity, anchorData, true, scale);

            case LEFT_HAND:
                return calculateHandPosition(IEntity, anchorData, false, scale);

            case ABOVE_HEAD:
                y += height * ABOVE_HEAD_HEIGHT;
                break;

            case CHEST:
                y += height * CHEST_HEIGHT;
                break;

            case EYE:
                return calculateEyePosition(IEntity, anchorData, scale);
        }

        // Rotate offsets relative to IEntity's body yaw
        // +X = IEntity's right, +Y = up, +Z = IEntity's forward
        IVector3 rotatedOffset = rotateOffsetByYaw(anchorX * scale, anchorY * scale, anchorZ * scale, bodyYaw);
        x += rotatedOffset.xCoord;
        y += rotatedOffset.yCoord;
        z += rotatedOffset.zCoord;

        // Apply FULL_MODEL rotation if active
        FramePart fullModel = getFullModelPart(IEntity);
        if (fullModel != null) {
            double dx = x - IEntity.posX;
            double dy = y - IEntity.posY;
            double dz = z - IEntity.posZ;
            IVector3 rotated = applyFullModelToWorldOffset(fullModel, IEntity, dx, dy, dz, bodyYaw, scale);
            return IVector3.createVectorHelper(
                IEntity.posX + rotated.xCoord,
                IEntity.posY + rotated.yCoord,
                IEntity.posZ + rotated.zCoord
            );
        }

        return IVector3.createVectorHelper(x, y, z);
    }

    private static IVector3 calculateEyePosition(IEntityLivingBase IEntity, EnergyAnchorData anchorData, float scale) {
        double pivotWorldX = IEntity.posX;
        double pivotWorldY = IEntity.posY + IEntity.getEyeHeight();
        double pivotWorldZ = IEntity.posZ;

        float bodyYaw = (float) Math.toRadians(IEntity.renderYawOffset);

        float[] rotations = getHeadBoneRotations(IEntity);

        double ox = -anchorData.anchorOffsetX * scale;
        double oy = -anchorData.anchorOffsetY * scale;
        double oz = -anchorData.anchorOffsetZ * scale;

        // X rotation
        double cosX = Math.cos(rotations[0]);
        double sinX = Math.sin(rotations[0]);
        double ry = oy * cosX - oz * sinX;
        double rz = oy * sinX + oz * cosX;
        double rx = ox;
        oy = ry; oz = rz;

        // Y rotation
        double cosY = Math.cos(rotations[1]);
        double sinY = Math.sin(rotations[1]);
        double rx2 = rx * cosY + oz * sinY;
        double rz2 = -rx * sinY + oz * cosY;
        rx = rx2; oz = rz2;

        // Z rotation
        double cosZ = Math.cos(rotations[2]);
        double sinZ = Math.sin(rotations[2]);
        double rx3 = rx * cosZ - oy * sinZ;
        double ry3 = rx * sinZ + oy * cosZ;
        rx = rx3; oy = ry3;

        double cosYaw = Math.cos(bodyYaw);
        double sinYaw = Math.sin(bodyYaw);

        double worldOffsetX = rx * cosYaw + oz * sinYaw;
        double worldOffsetZ = rx * sinYaw - oz * cosYaw;
        double worldOffsetY = -oy;   // model Y-down → IWorld Y-up

        double x = pivotWorldX + worldOffsetX;
        double y = pivotWorldY + worldOffsetY;
        double z = pivotWorldZ + worldOffsetZ;

        FramePart fullModel = getFullModelPart(IEntity);
        if (fullModel != null) {
            double dx = x - IEntity.posX;
            double dy = y - IEntity.posY;
            double dz = z - IEntity.posZ;
            IVector3 rotatedFull = applyFullModelToWorldOffset(fullModel, IEntity, dx, dy, dz, bodyYaw, scale);
            return IVector3.createVectorHelper(
                IEntity.posX + rotatedFull.xCoord,
                IEntity.posY + rotatedFull.yCoord,
                IEntity.posZ + rotatedFull.zCoord
            );
        }

        return IVector3.createVectorHelper(x, y, z);
    }

    private static float[] getHeadBoneRotations(IEntityLivingBase IEntity) {
        // Try animated HEAD part first
        AnimationData animData = AnimationData.getData(IEntity);
        if (animData != null && animData.isActive() && animData.animation != null) {
            Frame frame = (Frame) animData.animation.currentFrame();
            if (frame != null) {
                FramePart headPart = frame.frameParts.get(EnumAnimationPart.HEAD);
                if (headPart != null) {
                    return getRotations(headPart, IEntity);
                }
            }
        }

        // Fallback: live head angles
        float pitch = (float) Math.toRadians(IEntity.rotationPitch);
        float yawDelta = (float) Math.toRadians(IEntity.rotationYawHead - IEntity.renderYawOffset);

        return new float[]{ pitch, yawDelta, 0f };
    }

    /**
     * Get model scale factor for an IEntity.
     * NPCs have configurable model size, players use default scale.
     */
    private static float getModelScale(IEntityLivingBase IEntity) {
        if (IEntity instanceof EntityNPCInterface) {
            int modelSize = ((EntityNPCInterface) IEntity).display.modelSize;
            return modelSize / 5f;  // modelSize 5 = 100% scale
        }
        return 1f;
    }

    /**
     * Returns Y offset correction for the local client player.
     * On the client, EntityPlayerSP.posY is at feet level but the renderer adds yOffset (1.62),
     * causing anchor positions to appear ~1.6 blocks too high. This matches DBC's correction.
     */
    private static double getClientPlayerYCorrection(IEntityLivingBase IEntity) {
        if (IEntity.worldObj.isRemote && IEntity instanceof EntityPlayerSP) {
            return CLIENT_PLAYER_Y_OFFSET;
        }
        return 0.0;
    }

    /**
     * Get arm scale for an IEntity.
     * EntityCustomNpc has per-part scaling, others use default (1,1,1).
     */
    private static ModelScalePart getArmScale(IEntityLivingBase IEntity) {
        if (IEntity instanceof EntityCustomNpc) {
            return ((EntityCustomNpc) IEntity).modelData.modelScale.arms;
        }
        return DEFAULT_ARM_SCALE;
    }

    /**
     * Calculate hand position using animation data if available.
     * Applies FULL_MODEL rotation if an active animation has it.
     */
    private static IVector3 calculateHandPosition(IEntityLivingBase IEntity, EnergyAnchorData anchor, boolean rightHand, float scale) {
        // Try to get animated position first
        IVector3 pos = getAnimatedHandPosition(IEntity, anchor, rightHand, scale);
        if (pos == null) {
            // Fallback to static position
            pos = calculateFallbackHandPosition(IEntity, anchor, rightHand, scale);
        }

        // Apply FULL_MODEL rotation if active
        FramePart fullModel = getFullModelPart(IEntity);
        if (fullModel != null) {
            double dx = pos.xCoord - IEntity.posX;
            double dy = pos.yCoord - IEntity.posY;
            double dz = pos.zCoord - IEntity.posZ;
            float bodyYaw = (float) Math.toRadians(IEntity.renderYawOffset);
            IVector3 rotated = applyFullModelToWorldOffset(fullModel, IEntity, dx, dy, dz, bodyYaw, scale);
            return IVector3.createVectorHelper(
                IEntity.posX + rotated.xCoord,
                IEntity.posY + rotated.yCoord,
                IEntity.posZ + rotated.zCoord
            );
        }

        return pos;
    }

    /**
     * Calculate hand position from animation data.
     * Works for both NPCs and Players.
     */
    private static IVector3 getAnimatedHandPosition(IEntityLivingBase IEntity, EnergyAnchorData anchor, boolean rightHand, float scale) {
        AnimationData animData = AnimationData.getData(IEntity);
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
        ModelScalePart armScale = getArmScale(IEntity);

        // Get rotation and pivot values
        float[] rotations = getRotations(part, IEntity);
        float[] pivots = getPivots(part, IEntity);

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
        // Renderer applies glRotatef(180 - yaw) then glScalef(-1, -1, 1).
        // Combined transform: x' = mx*cos(yaw) + mz*sin(yaw), z' = mx*sin(yaw) - mz*cos(yaw)
        float bodyYaw = (float) Math.toRadians(IEntity.renderYawOffset);
        double cosYaw = Math.cos(bodyYaw);
        double sinYaw = Math.sin(bodyYaw);

        double worldOffsetX = blockX * cosYaw + blockZ * sinYaw;
        double worldOffsetZ = blockX * sinYaw - blockZ * cosYaw;

        // Calculate shoulder height based on IEntity and scale
        float shoulderHeight = IEntity.height * ARM_HEIGHT;

        // Final IWorld position
        double worldX = IEntity.posX + worldOffsetX;
        double worldY = IEntity.posY + getClientPlayerYCorrection(IEntity) + shoulderHeight - blockY;
        double worldZ = IEntity.posZ + worldOffsetZ;

        // Rotate offsets relative to IEntity's body yaw
        IVector3 rotatedOffset = rotateOffsetByYaw(anchor.anchorOffsetX * scale, anchor.anchorOffsetY * scale, anchor.anchorOffsetZ * scale, bodyYaw);
        worldX += rotatedOffset.xCoord;
        worldY += rotatedOffset.yCoord;
        worldZ += rotatedOffset.zCoord;

        return IVector3.createVectorHelper(worldX, worldY, worldZ);
    }

    /**
     * Get rotation values from FramePart.
     * Uses interpolated values on client, raw values on server.
     */
    private static float[] getRotations(FramePart part, IEntityLivingBase IEntity) {
        if (IEntity.worldObj.isRemote) {
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
    private static float[] getPivots(FramePart part, IEntityLivingBase IEntity) {
        if (IEntity.worldObj.isRemote) {
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
     * Represents arms hanging at sides in default idle pose.
     */
    private static IVector3 calculateFallbackHandPosition(IEntityLivingBase IEntity, EnergyAnchorData anchor, boolean rightHand, float scale) {
        float bodyYaw = (float) Math.toRadians(IEntity.renderYawOffset);

        // Get arm scale for EntityCustomNpc
        ModelScalePart armScale = getArmScale(IEntity);

        // Perpendicular direction for lateral offset (right = +90°, left = -90° from body facing)
        float perpYaw = bodyYaw + (rightHand ? (float) (Math.PI / 2) : (float) (-Math.PI / 2));

        // Calculate lateral offset - arms hang at sides
        // Scale by model size, but not by arm scale (lateral distance is fixed by skeleton)
        double lateralX = -Math.sin(perpYaw) * FALLBACK_LATERAL * scale;
        double lateralZ = Math.cos(perpYaw) * FALLBACK_LATERAL * scale;

        // Small forward offset
        double forwardX = -Math.sin(bodyYaw) * FALLBACK_FORWARD * scale;
        double forwardZ = Math.cos(bodyYaw) * FALLBACK_FORWARD * scale;

        // Arm length affects vertical position - longer arms = lower hands
        double armLengthOffset = armScale.scaleY * 0.3 * scale;

        double x = IEntity.posX + lateralX + forwardX;
        double y = IEntity.posY + getClientPlayerYCorrection(IEntity) + IEntity.height * FALLBACK_ARM_HEIGHT + armLengthOffset;
        double z = IEntity.posZ + lateralZ + forwardZ;

        // Rotate offsets relative to IEntity's body yaw
        IVector3 rotatedOffset = rotateOffsetByYaw(anchor.anchorOffsetX * scale, anchor.anchorOffsetY * scale, anchor.anchorOffsetZ * scale, bodyYaw);
        x += rotatedOffset.xCoord;
        y += rotatedOffset.yCoord;
        z += rotatedOffset.zCoord;

        return IVector3.createVectorHelper(x, y, z);
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
            case EYE:
                return EYE_HEIGHT;
            default:
                return FRONT_HEIGHT;
        }
    }

    /**
     * Rotate an offset vector by the IEntity's body yaw so offsets are IEntity-relative.
     * +X = IEntity's right, +Y = up, +Z = IEntity's forward.
     *
     * @param bodyYawRad body yaw in radians
     */
    private static IVector3 rotateOffsetByYaw(double offsetX, double offsetY, double offsetZ, float bodyYawRad) {
        double cos = Math.cos(bodyYawRad);
        double sin = Math.sin(bodyYawRad);
        double worldX = -offsetX * cos - offsetZ * sin;
        double worldZ = -offsetX * sin + offsetZ * cos;
        return IVector3.createVectorHelper(worldX, offsetY, worldZ);
    }

    // ==================== FULL_MODEL ANIMATION SUPPORT ====================

    /**
     * Get the FULL_MODEL FramePart if the IEntity has an active animation with it.
     * Returns null if no FULL_MODEL animation is active.
     */
    private static FramePart getFullModelPart(IEntityLivingBase IEntity) {
        AnimationData animData = AnimationData.getData(IEntity);
        if (animData == null || !animData.isActive() || animData.animation == null) {
            return null;
        }
        Frame frame = (Frame) animData.animation.currentFrame();
        if (frame == null) {
            return null;
        }
        FramePart part = frame.frameParts.get(EnumAnimationPart.FULL_MODEL);
        return part;
    }

    /**
     * Apply FULL_MODEL rotation to a IWorld-space offset from IEntity position.
     * Converts the offset to model space, applies FULL_MODEL rotation and pivots,
     * then converts back to IWorld space.
     * <p>
     * The rendering pipeline is: entity_pos * body_yaw * scale(-1,-1,1) * FULL_MODEL * parts
     * Combined body_yaw + scale maps model space to IWorld space:
     * worldX = mx*cos(yaw) + mz*sin(yaw)
     * worldY = -my  (model Y-down → IWorld Y-up)
     * worldZ = mx*sin(yaw) - mz*cos(yaw)
     * This transform is its own inverse for X,Z.
     * <p>
     * FULL_MODEL rotation order matches renderer: vertex sees Z, then Y, then X.
     */
    private static IVector3 applyFullModelToWorldOffset(FramePart fullModel, IEntityLivingBase IEntity,
                                                    double worldDX, double worldDY, double worldDZ,
                                                    float bodyYaw, float scale) {
        float[] rotations = getRotations(fullModel, IEntity);
        float[] pivots = getPivots(fullModel, IEntity);

        // Quick exit if no transform needed
        boolean hasRotation = rotations[0] != 0 || rotations[1] != 0 || rotations[2] != 0;
        boolean hasPivot = pivots[0] != 0 || pivots[1] != 0 || pivots[2] != 0;
        if (!hasRotation && !hasPivot) {
            return IVector3.createVectorHelper(worldDX, worldDY, worldDZ);
        }

        // Convert IWorld offset to model space
        // Body yaw + scale combined transform is an involution (self-inverse) for X,Z
        double cosYaw = Math.cos(bodyYaw);
        double sinYaw = Math.sin(bodyYaw);

        double mx = worldDX * cosYaw + worldDZ * sinYaw;
        double mz = worldDX * sinYaw - worldDZ * cosYaw;
        double my = -worldDY;  // Invert Y (IWorld Y-up → model Y-down)

        // Apply FULL_MODEL rotations (vertex order: Z first, then Y, then X)
        // Matches renderer: glRotatef(X), glRotatef(Y), glRotatef(Z) → vertex sees Z,Y,X

        // Z rotation
        double cosZ = Math.cos(rotations[2]);
        double sinZ = Math.sin(rotations[2]);
        double nx = mx * cosZ - my * sinZ;
        double ny = mx * sinZ + my * cosZ;
        mx = nx;
        my = ny;

        // Y rotation
        double cosY = Math.cos(rotations[1]);
        double sinY = Math.sin(rotations[1]);
        nx = mx * cosY + mz * sinY;
        double nz = -mx * sinY + mz * cosY;
        mx = nx;
        mz = nz;

        // X rotation
        double cosX = Math.cos(rotations[0]);
        double sinX = Math.sin(rotations[0]);
        ny = my * cosX - mz * sinX;
        nz = my * sinX + mz * cosX;
        my = ny;
        mz = nz;

        // Apply pivots in model space (after rotation, matching GL vertex ordering)
        // Renderer: glTranslatef(px, -py, pz) → pivot Y is negated for model Y-down space
        mx += pivots[0] * MODEL_SCALE * scale;
        my -= pivots[1] * MODEL_SCALE * scale;
        mz += pivots[2] * MODEL_SCALE * scale;

        // Convert back to IWorld space (same involution for X,Z; invert Y back)
        double newDX = mx * cosYaw + mz * sinYaw;
        double newDZ = mx * sinYaw - mz * cosYaw;
        double newDY = -my;

        return IVector3.createVectorHelper(newDX, newDY, newDZ);
    }
}
