package noppes.npcs.ai.pathfinder;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.world.IBlockAccess;

public class NPCPathFinder extends PathFinder
{

    public IBlockAccess worldMap;

    public Path path = new Path();
    public NPCPathPoint[] pathOptions = new NPCPathPoint[32];

    private final NodeProcessor nodeProcessor;

    public NPCPathFinder(NodeProcessor nodeProcessorIn)
    {
        super(nodeProcessorIn.blockaccess, true, true, false, false);
        this.nodeProcessor = nodeProcessorIn;
    }

    /**
     * Creates a path from one entity to another within a minimum distance
     */
    public PathEntity createEntityPathTo(Entity p_75856_1_, Entity p_75856_2_, float p_75856_3_)
    {
        return this.createEntityPathTo(p_75856_1_, p_75856_2_.posX, p_75856_2_.boundingBox.minY, p_75856_2_.posZ, p_75856_3_);
    }

    /**
     * Creates a path from an entity to a specified location within a minimum distance
     */
    public PathEntity createEntityPathTo(Entity p_75859_1_, int p_75859_2_, int p_75859_3_, int p_75859_4_, float p_75859_5_)
    {
        return this.createEntityPathTo(p_75859_1_, (double)((float)p_75859_2_ + 0.5F), (double)((float)p_75859_3_ + 0.5F), (double)((float)p_75859_4_ + 0.5F), p_75859_5_);
    }

    /**
     * Internal implementation of creating a path from an entity to a point
     */
    public PathEntity createEntityPathTo(Entity p_75857_1_, double p_75857_2_, double p_75857_4_, double p_75857_6_, float p_75857_8_)
    {
        this.path.clearPath();
        this.nodeProcessor.initProcessor(worldMap, p_75857_1_);
        NPCPathPoint pathpoint = (NPCPathPoint) this.nodeProcessor.getPathPointTo(p_75857_1_);
        NPCPathPoint pathpoint1 = (NPCPathPoint) this.nodeProcessor.getPathPointToCoords(p_75857_1_, p_75857_2_, p_75857_4_, p_75857_6_);
        PathEntity pathentity = this.addToPath(p_75857_1_, pathpoint, pathpoint1, p_75857_8_);
        this.nodeProcessor.postProcess();
        return pathentity;
    }

    public PathEntity addToPath(Entity entityIn, NPCPathPoint pathpointStart, NPCPathPoint pathpointEnd, float maxDistance)
    {
        pathpointStart.totalPathDistance = 0.0F;
        pathpointStart.distanceToNext = pathpointStart.distanceToSquared(pathpointEnd);
        pathpointStart.distanceToTarget = pathpointStart.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(pathpointStart);
        NPCPathPoint pathpoint = pathpointStart;

        while (!this.path.isPathEmpty())
        {
            NPCPathPoint pathpoint1 = (NPCPathPoint) this.path.dequeue();

            if (pathpoint1.equals(pathpointEnd))
            {
                return this.createEntityPath(pathpointStart, pathpointEnd);
            }

            if (pathpoint1.distanceToSquared(pathpointEnd) < pathpoint.distanceToSquared(pathpointEnd))
            {
                pathpoint = pathpoint1;
            }

            pathpoint1.isFirst = true;
            int i = this.nodeProcessor.findPathOptions(this.pathOptions, entityIn, pathpoint1, pathpointEnd, maxDistance);

            for (int j = 0; j < i; ++j)
            {
                NPCPathPoint pathpoint2 = (NPCPathPoint) this.pathOptions[j];
                float f = pathpoint1.totalPathDistance + pathpoint1.distanceToSquared(pathpoint2);

                if (f < maxDistance * 2.0F && (!pathpoint2.isAssigned() || f < pathpoint2.totalPathDistance))
                {
                    pathpoint2.previous = pathpoint1;
                    pathpoint2.totalPathDistance = f;
                    pathpoint2.distanceToNext = pathpoint2.distanceToSquared(pathpointEnd);

                    if (pathpoint2.isAssigned())
                    {
                        this.path.changeDistance(pathpoint2, pathpoint2.totalPathDistance + pathpoint2.distanceToNext);
                    }
                    else
                    {
                        pathpoint2.distanceToTarget = pathpoint2.totalPathDistance + pathpoint2.distanceToNext;
                        this.path.addPoint(pathpoint2);
                    }
                }
            }
        }

        if (pathpoint == pathpointStart)
        {
            return null;
        }
        else
        {
            return this.createEntityPath(pathpointStart, pathpoint);
        }
    }


    /**
     * Returns a new PathEntity for a given start and end point
     */
    private PathEntity createEntityPath(NPCPathPoint p_75853_1_, NPCPathPoint p_75853_2_)
    {
        int i = 1;
        NPCPathPoint pathpoint2;

        for (pathpoint2 = p_75853_2_; pathpoint2.previous != null; pathpoint2 = (NPCPathPoint) pathpoint2.previous)
        {
            ++i;
        }

        NPCPathPoint[] apathpoint = new NPCPathPoint[i];
        pathpoint2 = p_75853_2_;
        --i;

        for (apathpoint[i] = p_75853_2_; pathpoint2.previous != null; apathpoint[i] = pathpoint2)
        {
            pathpoint2 = (NPCPathPoint) pathpoint2.previous;
            --i;
        }

        return new PathEntity(apathpoint);
    }
}
