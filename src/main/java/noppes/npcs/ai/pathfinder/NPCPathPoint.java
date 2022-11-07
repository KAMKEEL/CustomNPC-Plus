package noppes.npcs.ai.pathfinder;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;

public class NPCPathPoint extends PathPoint
{
    /** The x coordinate of this point */
    public final int xCoord;
    /** The y coordinate of this point */
    public final int yCoord;
    /** The z coordinate of this point */
    public final int zCoord;
    /** A hash of the coordinates used to identify this point */
    public final int hash;
    /** The index of this point in its assigned path */
    public int index = -1;
    /** The distance along the path to this point */
    public float totalPathDistance;
    /** The linear distance to the next point */
    public float distanceToNext;
    /** The distance to the target */
    public float distanceToTarget;
    /** The point preceding this in its assigned path */
    public NPCPathPoint previous;
    /** Indicates this is the origin */
    public boolean isFirst;
    public float costMalus;
    public float cost;
    public float distanceFromOrigin;
    public PathNodeType nodeType = PathNodeType.BLOCKED;

    public NPCPathPoint(int xIn, int yIn, int zIn)
    {
        super(xIn,yIn,zIn);
        this.xCoord = xIn;
        this.yCoord = yIn;
        this.zCoord = zIn;
        this.hash = makeHash(xIn, yIn, zIn);
    }

    public static int makeHash(int xIn, int yIn, int zIn)
    {
        return yIn & 255 | (xIn & 32767) << 8 | (zIn & 32767) << 24 | (xIn < 0 ? Integer.MIN_VALUE : 0) | (zIn < 0 ? 32768 : 0);
    }

    /**
     * Returns the linear distance to another path point
     */
    public float distanceTo(NPCPathPoint point)
    {
        float f = (float)(point.xCoord - this.xCoord);
        float f1 = (float)(point.yCoord - this.yCoord);
        float f2 = (float)(point.zCoord - this.zCoord);
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }

    /**
     * Returns the squared distance to another path point
     */
    public float distanceToSquared(NPCPathPoint point)
    {
        float f = (float)(point.xCoord - this.xCoord);
        float f1 = (float)(point.yCoord - this.yCoord);
        float f2 = (float)(point.zCoord - this.zCoord);
        return f * f + f1 * f1 + f2 * f2;
    }

    public float distanceManhattan(NPCPathPoint point)
    {
        float f = (float)Math.abs(point.xCoord - this.xCoord);
        float f1 = (float)Math.abs(point.yCoord - this.yCoord);
        float f2 = (float)Math.abs(point.zCoord - this.zCoord);
        return f + f1 + f2;
    }

    public boolean equals(Object point)
    {
        if (!(point instanceof NPCPathPoint))
        {
            return false;
        }
        else
        {
            NPCPathPoint pathpoint = (NPCPathPoint)point;
            return this.hash == pathpoint.hash && this.xCoord == pathpoint.xCoord && this.yCoord == pathpoint.yCoord && this.zCoord == pathpoint.zCoord;
        }
    }

    public int hashCode()
    {
        return this.hash;
    }

    /**
     * Returns true if this point has already been assigned to a path
     */
    public boolean isAssigned()
    {
        return this.index >= 0;
    }

    public String toString()
    {
        return this.xCoord + ", " + this.yCoord + ", " + this.zCoord;
    }
}