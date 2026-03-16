package common.minecraft.util;

import noppes.npcs.api.entity.IEntity;

/**
 * Platform abstraction for Minecraft's ray trace / moving object position.
 * MC 1.7.10: MovingObjectPosition
 * MC 1.12+: RayTraceResult
 */
public interface IRayTraceResult {
    /** 0=MISS, 1=BLOCK, 2=ENTITY */
    int getTypeOfHit();
    int getBlockX();
    int getBlockY();
    int getBlockZ();
    int getSideHit();
    IEntity getEntityHit();
    IVector3 getHitVec();
}
