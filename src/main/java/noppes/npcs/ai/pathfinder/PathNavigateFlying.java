package noppes.npcs.ai.pathfinder;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.world.World;

public class PathNavigateFlying extends PathNavigateSwimmer {

	public PathNavigateFlying(EntityLiving p_i45873_1_, World worldIn) {
		super(p_i45873_1_, worldIn);
	}
	
    protected PathFinder getPathFinder()
    {
        return new NPCPathFinder(new FlyNodeProcessor());
    }

    /**
     * If on ground or swimming and can swim
     */
    protected boolean canNavigate()
    {
        return true;
    }
}
