package noppes.npcs.ai.pathfinder;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Vec3;

public class NPCPath extends PathEntity{
    public final NPCPathPoint[] points;
    /** PathEntity Array Index the Entity is currently targeting */
    public int currentPathIndex;
    /** The total length of the path */
    public int pathLength;

    public NPCPath(NPCPathPoint[] pointsIn)
    {
        super(pointsIn);
        this.points = pointsIn;
        this.pathLength = pointsIn.length;
    }

    /**
     * Directs this path to the next point in its array
     */
    public void incrementPathIndex()
    {
        ++this.currentPathIndex;
    }

    /**
     * Returns true if this path has reached the end
     */
    public boolean isFinished()
    {
        return this.currentPathIndex >= this.pathLength;
    }

    /**
     * returns the last PathPoint of the Array
     */
    public NPCPathPoint getFinalPathPoint()
    {
        return this.pathLength > 0 ? this.points[this.pathLength - 1] : null;
    }

    /**
     * return the PathPoint located at the specified PathIndex, usually the current one
     */
    public NPCPathPoint getPathPointFromIndex(int p_75877_1_)
    {
        return this.points[p_75877_1_];
    }

    public int getCurrentPathLength()
    {
        return this.pathLength;
    }

    public void setCurrentPathLength(int p_75871_1_)
    {
        this.pathLength = p_75871_1_;
    }

    public int getCurrentPathIndex()
    {
        return this.currentPathIndex;
    }

    public void setCurrentPathIndex(int p_75872_1_)
    {
        this.currentPathIndex = p_75872_1_;
    }

    /**
     * Gets the vector of the PathPoint associated with the given index.
     */
    public Vec3 getVectorFromIndex(Entity entity, int index)
    {
        double d0 = (double)this.points[index].xCoord + (double)((int)(entity.width + 1.0F)) * 0.5D;
        double d1 = (double)this.points[index].yCoord + 0.05D;
        double d2 = (double)this.points[index].zCoord + (double)((int)(entity.width + 1.0F)) * 0.5D;
        return Vec3.createVectorHelper(d0, d1, d2);
    }

    /**
     * returns the current PathEntity target node as Vec3D
     */
    public Vec3 getPosition(Entity p_75878_1_)
    {
        return this.getVectorFromIndex(p_75878_1_, this.currentPathIndex);
    }

    public Vec3 getCurrentPos()
    {
        NPCPathPoint pathpoint = this.points[this.currentPathIndex];
        return Vec3.createVectorHelper((double)pathpoint.xCoord, (double)pathpoint.yCoord, (double)pathpoint.zCoord);
    }

    /**
     * Returns true if the EntityPath are the same. Non instance related equals.
     */
    public boolean isSamePath(PathEntity pathEntity)
    {
        NPCPath NPCPath = (NPCPath) pathEntity;
        if (NPCPath == null)
        {
            return false;
        }
        else if (NPCPath.points.length != this.points.length)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < this.points.length; ++i)
            {
                if (this.points[i].xCoord != NPCPath.points[i].xCoord || this.points[i].yCoord != NPCPath.points[i].yCoord || this.points[i].zCoord != NPCPath.points[i].zCoord)
                {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Returns true if the final PathPoint in the PathEntity is equal to Vec3D coords.
     */
    public boolean isDestinationSame(Vec3 vec3)
    {
        NPCPathPoint pathpoint = this.getFinalPathPoint();
        return pathpoint == null ? false : pathpoint.xCoord == (int)vec3.xCoord && pathpoint.zCoord == (int)vec3.zCoord;
    }
}
