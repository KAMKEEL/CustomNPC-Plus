package noppes.npcs.ai.pathfinder;

public class PathHeap {
    private NPCPathPoint[] pathPoints = new NPCPathPoint[128];
    /** The number of points in this path */
    private int count;

    /**
     * Adds a point to the path
     */
    public NPCPathPoint addPoint(NPCPathPoint pathPoint)
    {
        if (pathPoint.index >= 0)
        {
            throw new IllegalStateException("OW KNOWS!");
        }
        else
        {
            if (this.count == this.pathPoints.length)
            {
                NPCPathPoint[] apathpoint = new NPCPathPoint[this.count << 1];
                System.arraycopy(this.pathPoints, 0, apathpoint, 0, this.count);
                this.pathPoints = apathpoint;
            }

            this.pathPoints[this.count] = pathPoint;
            pathPoint.index = this.count;
            this.sortBack(this.count++);
            return pathPoint;
        }
    }

    /**
     * Clears the path
     */
    public void clearPath()
    {
        this.count = 0;
    }

    /**
     * Returns and removes the first point in the path
     */
    public NPCPathPoint dequeue()
    {
        NPCPathPoint pathpoint = this.pathPoints[0];
        this.pathPoints[0] = this.pathPoints[--this.count];
        this.pathPoints[this.count] = null;

        if (this.count > 0)
        {
            this.sortForward(0);
        }

        pathpoint.index = -1;
        return pathpoint;
    }

    /**
     * Changes the provided point's distance to target
     */
    public void changeDistance(NPCPathPoint point, float distance)
    {
        float f1 = point.distanceToTarget;
        point.distanceToTarget = distance;

        if (distance < f1)
        {
            this.sortBack(point.index);
        }
        else
        {
            this.sortForward(point.index);
        }
    }

    /**
     * Sorts a point to the left
     */
    private void sortBack(int index)
    {
        NPCPathPoint pathpoint = this.pathPoints[index];
        int j;

        for (float f = pathpoint.distanceToTarget; index > 0; index = j)
        {
            j = index - 1 >> 1;
            NPCPathPoint pathpoint1 = this.pathPoints[j];

            if (f >= pathpoint1.distanceToTarget)
            {
                break;
            }

            this.pathPoints[index] = pathpoint1;
            pathpoint1.index = index;
        }

        this.pathPoints[index] = pathpoint;
        pathpoint.index = index;
    }

    /**
     * Sorts a point to the right
     */
    private void sortForward(int index)
    {
        NPCPathPoint pathpoint = this.pathPoints[index];
        float f = pathpoint.distanceToTarget;

        while (true)
        {
            int j = 1 + (index << 1);
            int k = j + 1;

            if (j >= this.count)
            {
                break;
            }

            NPCPathPoint pathpoint1 = this.pathPoints[j];
            float f1 = pathpoint1.distanceToTarget;
            NPCPathPoint pathpoint2;
            float f2;

            if (k >= this.count)
            {
                pathpoint2 = null;
                f2 = Float.POSITIVE_INFINITY;
            }
            else
            {
                pathpoint2 = this.pathPoints[k];
                f2 = pathpoint2.distanceToTarget;
            }

            if (f1 < f2)
            {
                if (f1 >= f)
                {
                    break;
                }

                this.pathPoints[index] = pathpoint1;
                pathpoint1.index = index;
                index = j;
            }
            else
            {
                if (f2 >= f)
                {
                    break;
                }

                this.pathPoints[index] = pathpoint2;
                pathpoint2.index = index;
                index = k;
            }
        }

        this.pathPoints[index] = pathpoint;
        pathpoint.index = index;
    }

    /**
     * Returns true if this path contains no points
     */
    public boolean isPathEmpty() { return this.count == 0; }
}
