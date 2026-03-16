package noppes.npcs.roles;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.compat.PixelmonHelper;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JobSpawner extends JobInterface {
    public INbt compound6;
    public INbt compound5;
    public INbt compound4;
    public INbt compound3;
    public INbt compound2;
    public INbt compound1;

    private int number = 0;

    public List<IEntityLivingBase> spawned = new ArrayList<IEntityLivingBase>();

    private Map<String, Long> cooldown = new HashMap<String, Long>();

    private String id = RandomStringUtils.random(8, true, true);

    public boolean doesntDie = false;
    public boolean despawnOnSummonerDeath = false;
    public boolean despawnOnTargetLost = true;

    // 0 - One By One, 1 - All at Once, 2 - Random, 3 - When Summoner Dies
    public int spawnType = 0;

    public int xOffset = 0;
    public int yOffset = 0;
    public int zOffset = 0;

    private IEntityLivingBase target;

    private boolean isResetting = false;

    public JobSpawner(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public INbt writeToNBT(INbt compound) {
        saveCompound(compound1, "SpawnerNBT1", compound);
        saveCompound(compound2, "SpawnerNBT2", compound);
        saveCompound(compound3, "SpawnerNBT3", compound);
        saveCompound(compound4, "SpawnerNBT4", compound);
        saveCompound(compound5, "SpawnerNBT5", compound);
        saveCompound(compound6, "SpawnerNBT6", compound);

        compound.setString("SpawnerId", id);
        compound.setBoolean("SpawnerDoesntDie", doesntDie);
        compound.setInteger("SpawnerType", spawnType);
        compound.setInteger("SpawnerXOffset", xOffset);
        compound.setInteger("SpawnerYOffset", yOffset);
        compound.setInteger("SpawnerZOffset", zOffset);

        compound.setBoolean("DespawnOnTargetLost", despawnOnTargetLost);
        compound.setBoolean("DespawnOnSummmoner", despawnOnSummonerDeath);
        return compound;
    }

    public INbt getTitles() {
        INbt compound = new INbt();
        compound.setString("Title1", getTitle(compound1));
        compound.setString("Title2", getTitle(compound2));
        compound.setString("Title3", getTitle(compound3));
        compound.setString("Title4", getTitle(compound4));
        compound.setString("Title5", getTitle(compound5));
        compound.setString("Title6", getTitle(compound6));
        return compound;
    }

    private String getTitle(INbt compound) {
        if (compound != null && compound.hasKey("ClonedName"))
            return compound.getString("ClonedName");

        return "gui.selectnpc";
    }

    private void saveCompound(INbt save, String name, INbt compound) {
        if (save != null)
            compound.setTag(name, save);
    }


    @Override
    public void readFromNBT(INbt compound) {
        compound1 = compound.getCompoundTag("SpawnerNBT1");
        compound2 = compound.getCompoundTag("SpawnerNBT2");
        compound3 = compound.getCompoundTag("SpawnerNBT3");
        compound4 = compound.getCompoundTag("SpawnerNBT4");
        compound5 = compound.getCompoundTag("SpawnerNBT5");
        compound6 = compound.getCompoundTag("SpawnerNBT6");

        id = compound.getString("SpawnerId");
        doesntDie = compound.getBoolean("SpawnerDoesntDie");
        spawnType = compound.getInteger("SpawnerType");
        xOffset = compound.getInteger("SpawnerXOffset");
        yOffset = compound.getInteger("SpawnerYOffset");
        zOffset = compound.getInteger("SpawnerZOffset");

        despawnOnTargetLost = compound.getBoolean("DespawnOnTargetLost");
        despawnOnSummonerDeath = compound.getBoolean("DespawnOnSummmoner");
    }


    public void cleanCompound(INbt compound) {
        compound.removeTag("SpawnerNBT1");
        compound.removeTag("SpawnerNBT2");
        compound.removeTag("SpawnerNBT3");
        compound.removeTag("SpawnerNBT4");
        compound.removeTag("SpawnerNBT5");
        compound.removeTag("SpawnerNBT6");
    }

    public void setJobCompound(int i, INbt compound) {
        if (i == 1)
            compound1 = compound;
        if (i == 2)
            compound2 = compound;
        if (i == 3)
            compound3 = compound;
        if (i == 4)
            compound4 = compound;
        if (i == 5)
            compound5 = compound;
        if (i == 6)
            compound6 = compound;
    }

    @Override
    public void aiUpdateTask() {
        if (spawned.isEmpty()) {
            if (spawnType == 0) {
                if (spawnEntity(number + 1) == null && !doesntDie)
                    npc.setDead();
            }
            if (spawnType == 1) {
                if (number >= 6 && !doesntDie)
                    npc.setDead();
                else {
                    spawnEntity(compound1);
                    spawnEntity(compound2);
                    spawnEntity(compound3);
                    spawnEntity(compound4);
                    spawnEntity(compound5);
                    spawnEntity(compound6);
                    number = 6;
                }
            }
            if (spawnType == 2) {
                ArrayList<INbt> list = new ArrayList<INbt>();
                if (compound1 != null && compound1.hasKey("id"))
                    list.add(compound1);
                if (compound2 != null && compound2.hasKey("id"))
                    list.add(compound2);
                if (compound3 != null && compound3.hasKey("id"))
                    list.add(compound3);
                if (compound4 != null && compound4.hasKey("id"))
                    list.add(compound4);
                if (compound5 != null && compound5.hasKey("id"))
                    list.add(compound5);
                if (compound6 != null && compound6.hasKey("id"))
                    list.add(compound6);

                if (!list.isEmpty()) {
                    INbt compound = list.get(npc.getRNG().nextInt(list.size()));
                    spawnEntity(compound);
                } else if (!doesntDie)
                    npc.setDead();
            }
        } else {
            checkSpawns();
        }

    }

    public void checkSpawns() {
        Iterator<IEntityLivingBase> iterator = spawned.iterator();
        while (iterator.hasNext()) {
            IEntityLivingBase spawn = iterator.next();
            if (shouldDelete(spawn)) {
                spawn.isDead = true;
                iterator.remove();
            } else {
                checkTarget(spawn);
            }
        }
    }

    public void checkTarget(IEntityLivingBase IEntity) {
        if (IEntity instanceof IEntityLiving) {
            IEntityLiving liv = (IEntityLiving) IEntity;
            if (liv.getAttackTarget() == null || npc.getRNG().nextInt(100) == 1)
                liv.setAttackTarget(target);
        } else if (IEntity.getAITarget() == null || npc.getRNG().nextInt(100) == 1) {
            IEntity.setRevengeTarget(target);
        }
    }

    public boolean shouldDelete(IEntityLivingBase IEntity) {
        return npc.getDistanceToEntity(IEntity) > 60 || IEntity.isDead || IEntity.getHealth() <= 0 ||
            PixelmonHelper.Enabled && hasPixelmon() && !PixelmonHelper.isBattling(IEntity) || (despawnOnSummonerDeath && npc.isDead) || despawnOnTargetLost && target == null;
    }

    private IEntityLivingBase getTarget() {
        IEntityLivingBase target = getTarget(npc);
        if (target != null)
            return target;

        for (IEntityLivingBase IEntity : spawned) {
            target = getTarget(IEntity);
            if (target != null)
                return target;
        }
        return null;
    }

    private IEntityLivingBase getTarget(IEntityLivingBase IEntity) {
        if (IEntity instanceof IEntityLiving) {
            target = ((IEntityLiving) IEntity).getAttackTarget();
            if (target != null && !target.isDead && target.getHealth() > 0)
                return target;
        }
        target = IEntity.getAITarget();
        if (target != null && !target.isDead && target.getHealth() > 0)
            return target;
        return null;
    }

    public boolean isEmpty() {
        if (compound1 != null && compound1.hasKey("id"))
            return false;
        if (compound2 != null && compound2.hasKey("id"))
            return false;
        if (compound3 != null && compound3.hasKey("id"))
            return false;
        if (compound4 != null && compound4.hasKey("id"))
            return false;
        if (compound5 != null && compound5.hasKey("id"))
            return false;
        if (compound6 != null && compound6.hasKey("id"))
            return false;

        return true;
    }

    private void setTarget(IEntityLivingBase base, IEntityLivingBase target) {
        if (PixelmonHelper.isTrainer(base) && target instanceof IPlayer) {
            IPlayer player = (IPlayer) target;
            if (!PixelmonHelper.canBattle(player, npc))
                return;
            cooldown.put(player.getCommandSenderName(), System.currentTimeMillis());

            Iterator<Entry<String, Long>> ita = cooldown.entrySet().iterator();

            while (ita.hasNext()) {
                Entry<String, Long> entry = ita.next();
                if (!isOnCooldown(entry.getKey()))
                    ita.remove();
            }
        } else if (base instanceof IEntityLiving)
            ((IEntityLiving) base).setAttackTarget(target);
        else
            base.setRevengeTarget(target);
    }

    @Override
    public boolean aiShouldExecute() {
        if (isEmpty() || npc.isKilled())
            return false;

        target = getTarget();
        if (npc.getRNG().nextInt(30) == 1) {
            if (spawned.isEmpty())
                spawned = getNearbySpawned();
        }
        if (!spawned.isEmpty())
            checkSpawns();
        return target != null;
    }

    public boolean aiContinueExecute() {
        return aiShouldExecute();
    }

    public void resetTask() {
        reset();
    }

    public void aiStartExecuting() {
        number = 0;
        for (IEntityLivingBase IEntity : spawned) {
            int i = IEntity.getEntityData().getInteger("NpcSpawnerNr");
            if (i > number)
                number = i;
            setTarget(IEntity, npc.getAttackTarget());
        }
    }

    @Override
    public void reset() {
        if (isResetting)
            return;
        isResetting = true;
        try {
            number = 0;
            if (spawned.isEmpty())
                spawned = getNearbySpawned();

            target = null;
            checkSpawns();
        } finally {
            isResetting = false;
        }
    }

    @Override
    public void delete() {
        if (isResetting)
            return;
        if (spawnType == 3 && npc.stats.spawnCycle == 3) {
            spawnEntity(compound1);
            spawnEntity(compound2);
            spawnEntity(compound3);
            spawnEntity(compound4);
            spawnEntity(compound5);
            spawnEntity(compound6);
            number = 6;
        }
        reset();
    }


    @Override
    public void killed() {
        if (spawnType == 3 && npc.stats.spawnCycle != 3) {
            spawnEntity(compound1);
            spawnEntity(compound2);
            spawnEntity(compound3);
            spawnEntity(compound4);
            spawnEntity(compound5);
            spawnEntity(compound6);
            number = 6;
        }
        reset();
    }

    public IEntityLivingBase spawnEntity(int i) {
        INbt compound = getCompound(i);
        if (compound == null) {
            return null;
        }
        return spawnEntity(compound);
    }

    private IEntityLivingBase spawnEntity(INbt compound) {
        if (compound == null || !compound.hasKey("id"))
            return null;
        double x = this.npc.posX + xOffset - 0.5 + this.npc.getRNG().nextFloat();
        double y = this.npc.posY + yOffset;
        double z = this.npc.posZ + zOffset - 0.5 + this.npc.getRNG().nextFloat();
        IEntity IEntity = NoppesUtilServer.getEntityFromNBT(compound, ValueUtil.floorDouble(x), ValueUtil.floorDouble(y), ValueUtil.floorDouble(z), npc.worldObj);
        if (IEntity == null) {
            return null;
        }
        IEntity.dimension = npc.worldObj.provider.dimensionId;
        if (!IEntity.forceSpawn && !npc.worldObj.checkChunksExist(
            (int) IEntity.posX, (int) IEntity.posY, (int) IEntity.posZ, (int) IEntity.posX, (int) IEntity.posY, (int) IEntity.posZ
        )) {
            return null;
        } else {
            if (!(IEntity instanceof IEntityLivingBase))
                return null;

            IEntityLivingBase living = (IEntityLivingBase) IEntity;
            living.getEntityData().setString("NpcSpawnerId", id);
            living.getEntityData().setInteger("NpcSpawnerNr", number);
            setTarget(living, this.npc.getAttackTarget());
            living.setPosition(x + 0.5, y + 1 + 0.2F, z + 0.5);

            if (living instanceof EntityNPCInterface) {
                EntityNPCInterface snpc = (EntityNPCInterface) living;
                snpc.stats.spawnCycle = 3;
                snpc.ais.returnToStart = false;
                snpc.stats.canDespawn = true;
                snpc.stats.playerSetCanDespawn = true;
            }

            int i = ValueUtil.floorDouble(living.posX / 16.0D);
            int j = ValueUtil.floorDouble(living.posZ / 16.0D);

            npc.worldObj.getChunkFromChunkCoords(i, j).addEntity(living);
            npc.worldObj.loadedEntityList.add(living);
            npc.worldObj.onEntityAdded(living);
            spawned.add(living);
            return living;
        }
    }

    public INbt getCompound(int i) {
        if (i <= 1 && compound1 != null && compound1.hasKey("id")) {
            number = 1;
            return compound1;
        }
        if (i <= 2 && compound2 != null && compound2.hasKey("id")) {
            number = 2;
            return compound2;
        }
        if (i <= 3 && compound3 != null && compound3.hasKey("id")) {
            number = 3;
            return compound3;
        }
        if (i <= 4 && compound4 != null && compound4.hasKey("id")) {
            number = 4;
            return compound4;
        }
        if (i <= 5 && compound5 != null && compound5.hasKey("id")) {
            number = 5;
            return compound5;
        }
        if (i <= 6 && compound6 != null && compound6.hasKey("id")) {
            number = 6;
            return compound6;
        }
        return null;
    }


    public List<IEntityLivingBase> getNearbySpawned() {
        List<IEntityLivingBase> spawnList = new ArrayList<IEntityLivingBase>();
        List<IEntityLivingBase> list = npc.worldObj.getEntitiesWithinAABB(IEntityLivingBase.class, npc.boundingBox.expand(40, 40, 40));
        for (IEntityLivingBase IEntity : list) {
            if (IEntity.getEntityData().getString("NpcSpawnerId").equals(id) && !IEntity.isDead)
                spawnList.add(IEntity);
        }
        return spawnList;
    }

    public boolean isOnCooldown(String name) {
        if (!cooldown.containsKey(name))
            return false;

        long time = cooldown.get(name);
        return System.currentTimeMillis() < time + 1200000; //20 minutes cooldown
    }


    public boolean hasPixelmon() {
        return compound1 != null && compound1.getString("id").equals("pixelmontainer");
    }
}
