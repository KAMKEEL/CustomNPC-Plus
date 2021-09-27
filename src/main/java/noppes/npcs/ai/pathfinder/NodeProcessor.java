package noppes.npcs.ai.pathfinder;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

public abstract class NodeProcessor {
    public IBlockAccess blockaccess;
    public IntHashMap pointMap = new IntHashMap();
    public int entitySizeX;
    public int entitySizeY;
    public int entitySizeZ;


    public void initProcessor(IBlockAccess iblockaccessIn, Entity entityIn) {
        this.blockaccess = iblockaccessIn;
        this.pointMap.clearMap();
        this.entitySizeX = MathHelper.floor_float(entityIn.width + 1.0F);
        this.entitySizeY = MathHelper.floor_float(entityIn.height + 1.0F);
        this.entitySizeZ = MathHelper.floor_float(entityIn.width + 1.0F);
    }

    public void postProcess() {
    }

    public PathPoint openPoint(int x, int y, int z) {
        int i = NPCPathPoint.makeHash(x, y, z);
        NPCPathPoint pathpoint = (NPCPathPoint) this.pointMap.lookup(i);

        if (pathpoint == null) {
            pathpoint = new NPCPathPoint(x, y, z);
            this.pointMap.addKey(i, pathpoint);
        }

        return pathpoint;
    }

    public abstract PathPoint getPathPointTo(Entity entityIn);

    public abstract PathPoint getPathPointToCoords(Entity entityIn, double x, double y, double target);

    public abstract int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint, float maxDistance);
}
