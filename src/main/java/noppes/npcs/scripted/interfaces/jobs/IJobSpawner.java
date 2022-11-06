package noppes.npcs.scripted.interfaces.jobs;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.scripted.interfaces.IPos;
import noppes.npcs.scripted.interfaces.IWorld;
import noppes.npcs.scripted.interfaces.entity.IEntityLivingBase;
import noppes.npcs.scripted.interfaces.entity.IPlayer;

public interface IJobSpawner extends IJob {

    IEntityLivingBase<?> spawnEntity(int number);

    IEntityLivingBase<?> getEntity(int number, int x, int y, int z, IWorld world);

    IEntityLivingBase<?> getEntity(int number, IPos pos, IWorld world);

    void setEntity(int number, IEntityLivingBase<?> entityLivingBase);

    /**
     * Removes all spawned entities
     */
    void removeAllSpawned();

    IEntityLivingBase<?>[] getNearbySpawned();

    boolean hasPixelmon();

    boolean isEmpty();

    boolean isOnCooldown(IPlayer<EntityPlayerMP> player);
}
