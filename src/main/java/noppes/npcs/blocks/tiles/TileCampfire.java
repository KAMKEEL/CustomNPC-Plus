package noppes.npcs.blocks.tiles;


import noppes.npcs.CustomNpcs;

import java.util.Random;

public class TileCampfire extends TileColorable {

    public static final Random RAND = new Random();
    private boolean rainAndSky = false;
    private boolean previousTickLit = false;

    @Override
    public void updateEntity()
    {
        if (getWorldObj().isRemote)
        {
            if (isLit())
            {
                if (!previousTickLit || distributedInterval(50L))
                    rainAndSky = isBeingRainedOn();

                addParticles();
            }

            previousTickLit = isLit();
        }
    }

    public boolean distributedInterval(long interval)
    {
        if (hasWorldObj())
            return (getWorldObj().getTotalWorldTime() + (long) (xCoord + yCoord + zCoord)) % interval == 0L;
        else
            return false;
    }

    protected void addParticles()
    {
        CustomNpcs.proxy.generateBigSmokeParticles(getWorldObj(), xCoord, yCoord, zCoord, false);
        if (rainAndSky)
        {
            for (int i = 0; i < RAND.nextInt(3); ++i)
                getWorldObj().spawnParticle("smoke", xCoord + RAND.nextDouble(), yCoord + 0.9, zCoord + RAND.nextDouble(), 0.0, 0.0, 0.0);
        }
    }
    public boolean isBeingRainedOn()
    {
        return getWorldObj().isRaining() && getWorldObj().getPrecipitationHeight(xCoord, zCoord) <= yCoord + 1
            && getWorldObj().getBiomeGenForCoords(xCoord, zCoord).canSpawnLightningBolt();
    }

    public boolean isLit()
    {
        return this.getBlockMetadata() == 0;
    }
}
