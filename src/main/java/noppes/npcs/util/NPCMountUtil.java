package noppes.npcs.util;

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

        npc.getNavigator().clearPathEntity();
        npc.setAttackTarget(null);
        npc.setRevengeTarget(null);

        npc.fallDistance = 0.0F;
        rider.fallDistance = 0.0F;

        applyMountedVerticalMotion(npc, state, mount, rider, moveSpeed, controlledForward);
        npc.performMountedMovement(controlledStrafe, controlledForward, moveSpeed);

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

                return;
            }

            if (jumpPressed && npc.onGround) {
                npc.motionY = jumpFactor;
                npc.setNpcJumpingState(true);
                npc.isAirBorne = true;
                npc.fallDistance = 0.0F;
                if (!npc.worldObj.isRemote) {
                    net.minecraftforge.common.ForgeHooks.onLivingJump(npc);
                }
                return;
            }

            if (!npc.onGround) {
                npc.motionY = Math.max(npc.motionY, -descendSpeed);
                npc.setNpcFlyingState(!npc.isInWater());
            } else {
                npc.setNpcFlyingState(false);
            }

            return;
        }

        if (npc.onGround) {
            if (rider.isJumping) {
                npc.motionY = jumpFactor;
                npc.setNpcJumpingState(true);
                npc.isAirBorne = true;
                npc.fallDistance = 0.0F;
                if (!npc.worldObj.isRemote) {
                    net.minecraftforge.common.ForgeHooks.onLivingJump(npc);
                }
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

        // Validate rider is alive and valid - dismount dead/invalid riders
        if (currentRider != null && !currentRider.isEntityAlive()) {
            currentRider.mountEntity(null);
            stabilizeDismountedRider(currentRider);
            haltMountedMotion(npc, state);
            applyUnriddenFlightDescent(npc, state, mount);
            currentRider = null;
        }

        // Only allow EntityPlayer as riders
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

        npc.motionY = -descendSpeed;
        npc.isAirBorne = true;
        npc.fallDistance = 0.0F;
        npc.setNpcFlyingState(true);
        npc.setNpcJumpingState(false);
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
