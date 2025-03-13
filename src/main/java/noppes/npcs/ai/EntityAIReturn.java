package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.Vec3;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIReturn extends EntityAIBase {
    public static final int MaxTotalTicks = 600;
    private final EntityNPCInterface npc;
    private int stuckTicks = 0;
    private int totalTicks = 0;
    private double endPosX;
    private double endPosY;
    private double endPosZ;
    private boolean wasAttacked = false;
    private double[] preAttackPos;
    private int stuckCount = 0;

    public EntityAIReturn(EntityNPCInterface npc) {
        this.npc = npc;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (!npc.ais.returnToStart) {
            return false;
        }

        if (npc.hasOwner() || npc.isKilled() || npc.isInteracting()) {
            return false;
        }

        if (npc.ais.findShelter == 0 && (!npc.worldObj.isDaytime() || npc.worldObj.isRaining()) && !npc.worldObj.provider.hasNoSky) {
            if (npc.worldObj.canBlockSeeTheSky((int) npc.getStartXPos(), (int) npc.getStartYPos(), (int) npc.getStartZPos()) || npc.worldObj.getFullBlockLightValue((int) npc.getStartXPos(), (int) npc.getStartYPos(), (int) npc.getStartZPos()) <= 8) {
                return false;
            }
        } else if (npc.ais.findShelter == 1 && npc.worldObj.isDaytime()) {
            if (npc.worldObj.canBlockSeeTheSky((int) npc.getStartXPos(), (int) npc.getStartYPos(), (int) npc.getStartZPos())) {
                return false;
            }
        }

        if (npc.isAttacking()) {
            if (!wasAttacked) {
                wasAttacked = true;
                preAttackPos = new double[]{npc.posX, npc.posY, npc.posZ};
            }
            return false;
        }

        if (npc.ais.movingType == EnumMovingType.MovingPath && npc.ais.getDistanceSqToPathPoint() < ConfigMain.NpcNavRange * ConfigMain.NpcNavRange)
            return false;

        if (npc.getNavigator().noPath() || (!npc.isAttacking() && wasAttacked)) {
            return true;
        }

        if (npc.ais.movingType == EnumMovingType.Wandering)
            return this.npc.getDistanceSq(npc.getStartXPos(), npc.getStartYPos(), npc.getStartZPos()) > npc.ais.walkingRange * npc.ais.walkingRange;

        if (npc.ais.movingType == EnumMovingType.Standing)
            return !this.npc.isVeryNearAssignedPlace();

        return false;
    }

    public boolean continueExecuting() {
        return !npc.isFollower() && !npc.isKilled() && !npc.isAttacking() && !npc.isVeryNearAssignedPlace() && totalTicks <= MaxTotalTicks && !npc.isInteracting();
    }

    public void updateTask() {
        totalTicks++;
        if (totalTicks > MaxTotalTicks) {
            npc.setPosition(endPosX, endPosY, endPosZ);
            npc.getNavigator().clearPathEntity();
            return;
        }

        if (stuckTicks > 0) {
            stuckTicks--;
        } else if (npc.getNavigator().noPath()) {
            stuckCount++;
            stuckTicks = 10;
            if (totalTicks > 30 && wasAttacked && isTooFar() || stuckCount > 5) {
                npc.setPosition(endPosX, endPosY, endPosZ);
                npc.getNavigator().clearPathEntity();
            } else
                navigate(stuckCount % 2 == 1);
        } else {
            stuckCount = 0;
        }
    }

    private boolean isTooFar() {
        int allowedDistance = npc.stats.aggroRange * 2;
        if (npc.ais.movingType == EnumMovingType.Wandering)
            allowedDistance += npc.ais.walkingRange;
        return npc.getDistanceSq(endPosX, endPosY, endPosZ) > allowedDistance * allowedDistance;
    }

    public void startExecuting() {
        stuckTicks = 0;
        totalTicks = 0;
        stuckCount = 0;
        navigate(false);
    }

    private void navigate(boolean towards) {
        if (!wasAttacked) {
            endPosX = npc.getStartXPos();
            endPosY = npc.getStartYPos();
            endPosZ = npc.getStartZPos();
        } else {
            endPosX = preAttackPos[0];
            endPosY = preAttackPos[1];
            endPosZ = preAttackPos[2];
        }
        double posX = endPosX;
        double posY = endPosY;
        double posZ = endPosZ;
        double range = npc.getDistance(posX, posY, posZ);
        if (range > ConfigMain.NpcNavRange || towards) {
            int distance = (int) range;
            if (distance > ConfigMain.NpcNavRange)
                distance = ConfigMain.NpcNavRange / 2;
            else
                distance /= 2;
            if (distance > 2) {
                Vec3 start = Vec3.createVectorHelper(posX, posY, posZ);
                Vec3 pos = RandomPositionGeneratorAlt.findRandomTargetBlockTowards(npc, distance, Math.min(distance / 2, 7), start);
                if (pos != null) {
                    posX = pos.xCoord;
                    posY = pos.yCoord;
                    posZ = pos.zCoord;
                }
            }
        }

        if (range > 1.1f) {
            npc.getNavigator().clearPathEntity();
            npc.getNavigator().tryMoveToXYZ(posX, posY, posZ, 1);
        }
    }

    public void resetTask() {
        wasAttacked = false;
        this.npc.getNavigator().clearPathEntity();
    }
}
