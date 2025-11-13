package noppes.npcs.util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.roles.RoleMount;

public final class NPCMountUtil {
    private static final int MOUNT_FLIGHT_TOGGLE_WINDOW = 7;

    private NPCMountUtil() {
    }

    public static class MountState {
        public Entity lastRider;
        public boolean flightMode;
        public boolean jumpPressed;
        public int flightToggleTimer;
        public float surfaceFriction = 0.91F;
    }

    public static boolean handleMountedMovement(EntityNPCInterface npc, MountState state, float strafe, float forward) {
        if (npc.advanced.role != EnumRoleType.Mount || !(npc.roleInterface instanceof RoleMount)) {
            resetMountedFlightState(npc, state);
            return false;
        }
        if (npc.riddenByEntity != null && !(npc.riddenByEntity instanceof EntityPlayer)) {
            npc.riddenByEntity.mountEntity(null);
            return false;
        }
        if (!(npc.riddenByEntity instanceof EntityPlayer)) {
            resetMountedFlightState(npc, state);
            return false;
        }

        RoleMount mount = (RoleMount) npc.roleInterface;
        EntityPlayer rider = (EntityPlayer) npc.riddenByEntity;
        updateMountedFlightState(npc, state, mount, rider);

        npc.prevRotationYaw = npc.rotationYaw = rider.rotationYaw;
        npc.rotationPitch = rider.rotationPitch * 0.5F;
        npc.setPositionAndRotation(npc.posX, npc.posY, npc.posZ, npc.rotationYaw, npc.rotationPitch);
        npc.renderYawOffset = npc.rotationYaw;
        npc.rotationYawHead = npc.rotationYaw;

        float controlledStrafe = rider.moveStrafing;
        float controlledForward = rider.moveForward;
        if (controlledForward <= 0.0F) {
            controlledForward *= 0.25F;
        }

        float moveSpeed = getMountMoveSpeed(npc);
        boolean riderSprinting = rider.isSprinting() && mount.isSprintAllowed();
        if (riderSprinting) {
            moveSpeed *= 1.2F;
        }

        if (!mount.isSprintAllowed() && rider.isSprinting()) {
            rider.setSprinting(false);
        }

        if (moveSpeed <= 0.0F) {
            controlledStrafe = 0.0F;
            controlledForward = 0.0F;
        }

        npc.setSprinting(riderSprinting);
        npc.stepHeight = 1.0F;
        npc.jumpMovementFactor = moveSpeed * 0.1F;

        npc.getNavigator().clearPathEntity();
        npc.setAttackTarget(null);
        npc.setRevengeTarget(null);

        npc.fallDistance = 0.0F;
        rider.fallDistance = 0.0F;

        applyMountedVerticalMotion(npc, state, mount, rider, moveSpeed, controlledForward);
        state.surfaceFriction = resolveSurfaceFriction(npc);

        npc.performMountedMovement(controlledStrafe, controlledForward, moveSpeed);

        syncRiderVelocity(npc, rider);

        return true;
    }

    private static float getMountMoveSpeed(EntityNPCInterface npc) {
        return Math.max(0.0F, npc.getSpeed());
    }

    private static void applyMountedVerticalMotion(EntityNPCInterface npc, MountState state, RoleMount mount, EntityLivingBase rider, float moveSpeed, float forward) {
        if (mount == null) {
            return;
        }
        double jumpFactor = MathHelper.clamp_double(mount.getJumpStrength(), 0.1D, 3.0D);
        boolean flyingEnabled = mount.isFlyingMountEnabled();

        npc.setNpcJumpingState(false);

        if (flyingEnabled) {
            double ascendSpeed = MathHelper.clamp_double(mount.getFlyingAscendSpeed(), 0.1D, 3.0D);
            double descendSpeed = MathHelper.clamp_double(mount.getFlyingDescendSpeed(), 0.05D, 3.0D);
            boolean flightMode = isMountInFlightMode(state);
            boolean jumpPressed = rider.isJumping;
            boolean descendPressed = rider instanceof EntityPlayer && isSpecialKeyDown((EntityPlayer) rider);

            if (flightMode) {
                double previousMotionY = npc.motionY;
                double newMotionY = 0.0D;

                if (jumpPressed) {
                    newMotionY += ascendSpeed;
                } else if (descendPressed) {
                    newMotionY -= descendSpeed;
                }

                newMotionY = MathHelper.clamp_double(newMotionY, -descendSpeed, ascendSpeed);
                npc.motionY = newMotionY;
                npc.setNpcFlyingState(true);
                npc.setNpcJumpingState(jumpPressed);
                npc.isAirBorne = true;
                npc.fallDistance = 0.0F;

                if (!npc.worldObj.isRemote && Math.abs(previousMotionY - newMotionY) > 1.0E-4D) {
                    npc.velocityChanged = true;
                }

                if (!npc.worldObj.isRemote && rider instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) rider;
                    if (!player.capabilities.isFlying) {
                        double playerPrevMotion = player.motionY;
                        player.motionY = npc.motionY;
                        if (Math.abs(playerPrevMotion - player.motionY) > 1.0E-4D) {
                            player.velocityChanged = true;
                        }
                    }
                }
                return;
            }

            if (jumpPressed && npc.onGround) {
                double previousMotionY = npc.motionY;
                npc.motionY = jumpFactor;
                npc.setNpcJumpingState(true);
                npc.isAirBorne = true;
                npc.fallDistance = 0.0F;
                if (!npc.worldObj.isRemote) {
                    if (Math.abs(previousMotionY - npc.motionY) > 1.0E-4D) {
                        npc.velocityChanged = true;
                    }
                    net.minecraftforge.common.ForgeHooks.onLivingJump(npc);
                }
                return;
            }

            if (!npc.onGround) {
                double previousMotionY = npc.motionY;
                double limitedMotionY = Math.max(npc.motionY, -descendSpeed);
                if (Math.abs(previousMotionY - limitedMotionY) > 1.0E-4D) {
                    npc.motionY = limitedMotionY;
                    if (!npc.worldObj.isRemote) {
                        npc.velocityChanged = true;
                    }
                } else {
                    npc.motionY = limitedMotionY;
                }
                npc.setNpcFlyingState(!npc.isInWater());
            } else {
                npc.setNpcFlyingState(false);
            }

            return;
        }

        if (npc.onGround) {
            double previousMotionY = npc.motionY;
            if (rider.isJumping) {
                npc.motionY = jumpFactor;
                npc.setNpcJumpingState(true);
                npc.isAirBorne = true;
                npc.fallDistance = 0.0F;
                if (!npc.worldObj.isRemote) {
                    if (Math.abs(previousMotionY - npc.motionY) > 1.0E-4D) {
                        npc.velocityChanged = true;
                    }
                    net.minecraftforge.common.ForgeHooks.onLivingJump(npc);
                }
            } else {
                npc.motionY = 0.0D;
            }
            return;
        }

        npc.setNpcFlyingState(false);
    }

    public static void handleMountRiderState(EntityNPCInterface npc, MountState state) {
        if (npc.worldObj.isRemote) {
            if (npc.advanced.role != EnumRoleType.Mount || !(npc.roleInterface instanceof RoleMount)) {
                state.lastRider = null;
            }
            return;
        }

        if (npc.advanced.role != EnumRoleType.Mount || !(npc.roleInterface instanceof RoleMount)) {
            state.lastRider = null;
            return;
        }

        RoleMount mount = (RoleMount) npc.roleInterface;
        Entity currentRider = npc.riddenByEntity;
        if (currentRider != null && !(currentRider instanceof EntityPlayer)) {
            Entity dismount = currentRider;
            dismount.mountEntity(null);
            stabilizeDismountedRider(dismount);
            haltMountedMotion(npc, state);
            applyUnriddenFlightDescent(npc, state, mount);
            currentRider = null;
        }
        if (currentRider != state.lastRider) {
            if (state.lastRider != null) {
                stabilizeDismountedRider(state.lastRider);
            }
            if (currentRider == null) {
                haltMountedMotion(npc, state);
                applyUnriddenFlightDescent(npc, state, mount);
            } else {
                resetMountedFlightState(npc, state);
            }
            state.lastRider = currentRider;
        } else if (currentRider == null) {
            applyUnriddenFlightDescent(npc, state, mount);
        }
    }

    public static boolean isFlyingMountWithFlightEnabled(EntityNPCInterface npc) {
        return npc.advanced.role == EnumRoleType.Mount && npc.roleInterface instanceof RoleMount && ((RoleMount) npc.roleInterface).isFlyingMountEnabled();
    }

    public static void haltMountedMotion(EntityNPCInterface npc, MountState state) {
        double prevMotionX = npc.motionX;
        double prevMotionY = npc.motionY;
        double prevMotionZ = npc.motionZ;
        npc.motionX = 0.0D;
        npc.motionY = 0.0D;
        npc.motionZ = 0.0D;
        npc.isAirBorne = false;
        npc.setSprinting(false);
        npc.moveForward = 0.0F;
        npc.moveStrafing = 0.0F;
        npc.setAIMoveSpeed(0.0F);
        npc.limbSwingAmount = 0.0F;
        npc.limbSwing = 0.0F;
        npc.getNavigator().clearPathEntity();
        npc.fallDistance = 0.0F;
        if (!npc.worldObj.isRemote && (Math.abs(prevMotionX) > 1.0E-4D || Math.abs(prevMotionY) > 1.0E-4D || Math.abs(prevMotionZ) > 1.0E-4D)) {
            npc.velocityChanged = true;
        }
        resetMountedFlightState(npc, state);
    }

    public static void applyUnriddenFlightDescent(EntityNPCInterface npc, MountState state, RoleMount mount) {
        if (mount == null || !mount.isFlyingMountEnabled()) {
            npc.setNpcFlyingState(false);
            npc.setNpcJumpingState(false);
            return;
        }
        if (npc.canFly()) {
            return;
        }
        if (npc.onGround) {
            npc.motionY = 0.0D;
            npc.setNpcFlyingState(false);
            npc.setNpcJumpingState(false);
            return;
        }

        double descendSpeed = MathHelper.clamp_double(mount.getFlyingDescendSpeed(), 0.05D, 3.0D);
        if (descendSpeed <= 0.0D) {
            npc.setNpcFlyingState(false);
            npc.setNpcJumpingState(false);
            return;
        }

        double desired = -descendSpeed;
        double previousMotionY = npc.motionY;
        npc.motionY = desired;
        npc.isAirBorne = true;
        npc.fallDistance = 0.0F;
        npc.setNpcFlyingState(true);
        npc.setNpcJumpingState(false);
        if (!npc.worldObj.isRemote && Math.abs(previousMotionY - desired) > 1.0E-4D) {
            npc.velocityChanged = true;
        }
    }

    public static void stabilizeDismountedRider(Entity rider) {
        boolean wasOnGround = rider.onGround;
        double previousMotionX = rider.motionX;
        double previousMotionY = rider.motionY;
        double previousMotionZ = rider.motionZ;
        float previousFall = rider.fallDistance;

        if (wasOnGround) {
            rider.motionX = 0.0D;
            rider.motionY = 0.0D;
            rider.motionZ = 0.0D;
            rider.fallDistance = 0.0F;
            rider.onGround = true;
        } else {
            rider.motionX = previousMotionX;
            rider.motionY = previousMotionY;
            rider.motionZ = previousMotionZ;
            rider.fallDistance = previousFall;
        }

        rider.velocityChanged = true;
    }

    private static void updateMountedFlightState(EntityNPCInterface npc, MountState state, RoleMount mount, EntityPlayer rider) {
        if (mount == null || rider == null || !mount.isFlyingMountEnabled()) {
            resetMountedFlightState(npc, state);
            return;
        }

        if (npc.onGround) {
            state.flightMode = false;
        }

        boolean jumpPressed = rider.isJumping;
        if (jumpPressed && !state.jumpPressed) {
            if (state.flightToggleTimer > 0) {
                state.flightMode = !state.flightMode;
                state.flightToggleTimer = 0;
            } else {
                state.flightToggleTimer = MOUNT_FLIGHT_TOGGLE_WINDOW;
            }
        }

        if (state.flightToggleTimer > 0) {
            state.flightToggleTimer--;
        }

        state.jumpPressed = jumpPressed;
    }

    public static boolean isMountInFlightMode(MountState state) {
        return state.flightMode;
    }

    public static void resetMountedFlightState(EntityNPCInterface npc, MountState state) {
        state.flightMode = false;
        state.jumpPressed = false;
        state.flightToggleTimer = 0;
        npc.setNpcFlyingState(false);
        npc.setNpcJumpingState(false);
    }

    private static float resolveSurfaceFriction(EntityNPCInterface npc) {
        float baseFriction = 0.91F;
        if (!npc.onGround) {
            return baseFriction;
        }

        int blockX = MathHelper.floor_double(npc.posX);
        int blockY = MathHelper.floor_double(npc.boundingBox.minY) - 1;
        int blockZ = MathHelper.floor_double(npc.posZ);
        Block surface = npc.worldObj.getBlock(blockX, blockY, blockZ);
        if (surface == null) {
            return baseFriction;
        }

        return MathHelper.clamp_float(surface.slipperiness * baseFriction, 0.4F, 0.99F);
    }

    private static void syncRiderVelocity(EntityNPCInterface npc, EntityPlayer rider) {
        if (rider == null) {
            return;
        }

        double deltaX = npc.motionX - rider.motionX;
        double deltaZ = npc.motionZ - rider.motionZ;
        rider.motionX = npc.motionX;
        rider.motionZ = npc.motionZ;
        rider.fallDistance = npc.fallDistance;

        if (!rider.capabilities.isFlying) {
            double deltaY = npc.motionY - rider.motionY;
            rider.motionY = npc.motionY;
            if (!npc.worldObj.isRemote && Math.abs(deltaY) > 1.0E-4D) {
                rider.velocityChanged = true;
            }
        }

        if (!npc.worldObj.isRemote && (Math.abs(deltaX) > 1.0E-4D || Math.abs(deltaZ) > 1.0E-4D)) {
            rider.velocityChanged = true;
        }
    }

    private static boolean isSpecialKeyDown(EntityPlayer rider) {
        if (rider == null) {
            return false;
        }
        if (rider.worldObj.isRemote) {
            PlayerData data = CustomNpcs.proxy.getPlayerData(rider);
            return data != null && data.isSpecialKeyDown();
        }
        PlayerData data = PlayerDataController.Instance.getPlayerData(rider);
        return data != null && data.isSpecialKeyDown();
    }
}
