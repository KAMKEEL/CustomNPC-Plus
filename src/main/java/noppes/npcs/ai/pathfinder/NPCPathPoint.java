package noppes.npcs.ai.pathfinder;

import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;

public class NPCPathPoint extends PathPoint
{
    /** The index of this point in its assigned path */
    public int index = -1;
    /** The distance along the path to this point */
    public float totalPathDistance;
    /** The linear distance to the next point */
    public float distanceToNext;
    /** The distance to the target */
    public float distanceToTarget;
    public PathPoint previous;

    public NPCPathPoint(int p_i2135_1_, int p_i2135_2_, int p_i2135_3_)
    {
        super(p_i2135_1_, p_i2135_2_,p_i2135_3_);
    }

}
