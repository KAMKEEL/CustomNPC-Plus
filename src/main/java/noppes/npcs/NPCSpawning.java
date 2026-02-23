package noppes.npcs;

import com.google.common.collect.Sets;
import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.CustomNPCsEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class NPCSpawning {
    private static Set<ChunkCoordIntPair> eligibleChunksForSpawning = Sets.newHashSet();
    private static final byte CHUNK_SPAWN_RADIUS = 7;
    private static final int SPAWN_ATTEMPTS_PER_CHUNK = 3;
    private static final long RUNTIME_SPAWN_TICK_INTERVAL = 20L;
    private static final int RUNTIME_SPAWN_CYCLE_TICKS = 400;
    private static final String NATURAL_SPAWN_ID_TAG = "CNPCNaturalSpawnId";
    private static final String NATURAL_SPAWN_NAME_TAG = "CNPCNaturalSpawnName";
    private static final String NATURAL_SPAWN_TIME_TAG = "CNPCNaturalSpawnTime";
    private static final Map<Long, Long> SPAWN_COOLDOWNS = new HashMap<Long, Long>();
    private static final Map<Integer, RuntimeChunkState> RUNTIME_CHUNK_STATES = new HashMap<Integer, RuntimeChunkState>();

    private static boolean animalSpawn;
    private static boolean monsterSpawn;
    private static boolean airSpawn;
    private static boolean liquidSpawn;

    private static class RuntimeChunkState {
        public ArrayList<ChunkCoordIntPair> chunks = new ArrayList<ChunkCoordIntPair>();
        public int index = 0;
        public int signature = 0;
    }

    public static void findChunksForSpawning(WorldServer world) {
        if (SpawnController.Instance.data.isEmpty() || world.getWorldInfo().getWorldTotalTime() % RUNTIME_SPAWN_TICK_INTERVAL != 0L)
            return;
        eligibleChunksForSpawning.clear();
        for (int i = 0; i < world.playerEntities.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) world.playerEntities.get(i);
            int j = MathHelper.floor_double(entityplayer.posX / 16.0D);
            int k = MathHelper.floor_double(entityplayer.posZ / 16.0D);
            byte size = CHUNK_SPAWN_RADIUS;

            for (int x = -size; x <= size; ++x) {
                for (int z = -size; z <= size; ++z) {
                    ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(x + j, z + k);
                    if (!eligibleChunksForSpawning.contains(chunkcoordintpair)) {
                        eligibleChunksForSpawning.add(chunkcoordintpair);
                    }
                }
            }
        }
        int cap = eligibleChunksForSpawning.size();
        int npcCount = countNPCs(world);
        if (npcCount >= cap) {
            return;
        }
        HashMap<Integer, Integer> spawnEntryCounts = getNaturalSpawnEntryCounts(world);
        int chunkBudget = getRuntimeChunkBudget(eligibleChunksForSpawning.size());
        List<ChunkCoordIntPair> chunksToProcess = getRuntimeChunkBatch(world, chunkBudget);

        for (ChunkCoordIntPair chunkcoordintpair1 : chunksToProcess) {
            if (npcCount >= cap) {
                return;
            }

            ChunkPosition chunkposition = getChunk(world, chunkcoordintpair1.chunkXPos, chunkcoordintpair1.chunkZPos);
            int j1 = chunkposition.chunkPosX;
            int y = chunkposition.chunkPosY;
            int l1 = chunkposition.chunkPosZ;

            for (int i = 0; i < SPAWN_ATTEMPTS_PER_CHUNK; i++) {
                if (npcCount >= cap) {
                    return;
                }
                int x = j1;
                int z = l1;
                byte b1 = 6;

                x += world.rand.nextInt(b1) - world.rand.nextInt(b1);
                z += world.rand.nextInt(b1) - world.rand.nextInt(b1);

                String name = world.getBiomeGenForCoords(x, z).biomeName;
                SpawnData data = SpawnController.Instance.getRandomSpawnData(name, world.provider.dimensionId);
                if (data == null)
                    continue;

                if (trySpawnWithEntryConfig(data, world, x, y, z, world.rand, spawnEntryCounts)) {
                    npcCount++;
                }
            }
        }
    }

    private static int getRuntimeChunkBudget(int eligibleChunkCount) {
        if (eligibleChunkCount <= 0) {
            return 0;
        }
        int runsPerCycle = Math.max(1, (int) (RUNTIME_SPAWN_CYCLE_TICKS / RUNTIME_SPAWN_TICK_INTERVAL));
        return Math.max(1, (int) Math.ceil((double) eligibleChunkCount / runsPerCycle));
    }

    private static List<ChunkCoordIntPair> getRuntimeChunkBatch(World world, int chunkBudget) {
        ArrayList<ChunkCoordIntPair> result = new ArrayList<ChunkCoordIntPair>();
        if (chunkBudget <= 0 || eligibleChunksForSpawning.isEmpty()) {
            return result;
        }

        int dimensionId = world.provider.dimensionId;
        RuntimeChunkState state = RUNTIME_CHUNK_STATES.get(dimensionId);
        if (state == null) {
            state = new RuntimeChunkState();
            RUNTIME_CHUNK_STATES.put(dimensionId, state);
        }

        int signature = eligibleChunksForSpawning.hashCode();
        if (state.chunks.isEmpty() || state.signature != signature || state.index >= state.chunks.size()) {
            state.chunks = new ArrayList<ChunkCoordIntPair>(eligibleChunksForSpawning);
            Collections.shuffle(state.chunks);
            state.index = 0;
            state.signature = signature;
        }

        int remaining = state.chunks.size() - state.index;
        int count = Math.min(chunkBudget, Math.max(0, remaining));
        for (int i = 0; i < count; i++) {
            result.add(state.chunks.get(state.index++));
        }
        return result;
    }

    public static int countNPCs(World world) {
        int count = 0;
        List<Entity> list = world.loadedEntityList;
        for (Entity entity : list) {
            if (entity instanceof EntityNPCInterface) {
                count++;
            }
        }
        return count;
    }

    protected static ChunkPosition getChunk(World world, int x, int z) {
        Chunk chunk = world.getChunkFromChunkCoords(x, z);
        int k = x * 16 + world.rand.nextInt(16);
        int l = z * 16 + world.rand.nextInt(16);
        int i1 = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
        return new ChunkPosition(k, i1, l);
    }

    private static int getCap(World world) {
        Set<ChunkCoordIntPair> chunkSet = Sets.newHashSet();
        for (int i = 0; i < world.playerEntities.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) world.playerEntities.get(i);
            int j = MathHelper.floor_double(entityplayer.posX / 16.0D);
            int k = MathHelper.floor_double(entityplayer.posZ / 16.0D);
            byte size = CHUNK_SPAWN_RADIUS;

            for (int x = -size; x <= size; ++x) {
                for (int z = -size; z <= size; ++z) {
                    chunkSet.add(new ChunkCoordIntPair(x + j, z + k));
                }
            }
        }
        return chunkSet.size();
    }

    private static HashMap<Integer, Integer> getNaturalSpawnEntryCounts(World world) {
        HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
        List<Entity> list = world.loadedEntityList;
        for (Entity entity : list) {
            if (entity == null || entity.isDead) {
                continue;
            }
            NBTTagCompound entityData = entity.getEntityData();
            if (!entityData.hasKey(NATURAL_SPAWN_ID_TAG)) {
                continue;
            }
            int spawnId = entityData.getInteger(NATURAL_SPAWN_ID_TAG);
            Integer count = counts.get(spawnId);
            counts.put(spawnId, count == null ? 1 : count + 1);
        }
        return counts;
    }

    private static long getSpawnCooldownKey(World world, int spawnId) {
        return ((long) world.provider.dimensionId << 32) ^ (spawnId & 0xffffffffL);
    }

    private static boolean isSpawnOnCooldown(World world, SpawnData data) {
        if (data.cooldownTicks <= 0 || data.id < 0) {
            return false;
        }
        long key = getSpawnCooldownKey(world, data.id);
        Long lastSpawnTick = SPAWN_COOLDOWNS.get(key);
        if (lastSpawnTick == null) {
            return false;
        }
        long tickDelta = world.getWorldInfo().getWorldTotalTime() - lastSpawnTick;
        return tickDelta < data.cooldownTicks;
    }

    private static void markSpawnCooldown(World world, SpawnData data) {
        if (data.cooldownTicks <= 0 || data.id < 0) {
            return;
        }
        long key = getSpawnCooldownKey(world, data.id);
        SPAWN_COOLDOWNS.put(key, world.getWorldInfo().getWorldTotalTime());
    }

    private static boolean isSpawnEntryAtCapacity(SpawnData data, Map<Integer, Integer> spawnEntryCounts) {
        if (data.maxAlive <= 0 || data.id < 0) {
            return false;
        }
        Integer currentCount = spawnEntryCounts.get(data.id);
        return currentCount != null && currentCount >= data.maxAlive;
    }

    private static void incrementSpawnEntryCount(SpawnData data, Map<Integer, Integer> spawnEntryCounts) {
        if (data.id < 0) {
            return;
        }
        Integer currentCount = spawnEntryCounts.get(data.id);
        spawnEntryCounts.put(data.id, currentCount == null ? 1 : currentCount + 1);
    }

    private static boolean hasNearbyPlayer(SpawnData data, World world, int x, int y, int z) {
        if (data.playerMinDistance <= 0) {
            return false;
        }
        return world.getClosestPlayer(x, y, z, data.playerMinDistance) != null;
    }

    private static boolean trySpawnWithEntryConfig(SpawnData data, World world, int x, int y, int z, Random rand, Map<Integer, Integer> spawnEntryCounts) {
        if (y < data.spawnHeightMin || y > data.spawnHeightMax) {
            return false;
        }
        if (isSpawnEntryAtCapacity(data, spawnEntryCounts) || isSpawnOnCooldown(world, data)) {
            return false;
        }

        int attempts = Math.max(1, data.attemptsPerCycle);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int attemptX = x;
            int attemptZ = z;
            if (attempt > 0) {
                attemptX += rand.nextInt(5) - rand.nextInt(5);
                attemptZ += rand.nextInt(5) - rand.nextInt(5);
            }

            if (!canCreatureTypeSpawnAtLocation(data, world, attemptX, y, attemptZ) || hasNearbyPlayer(data, world, attemptX, y, attemptZ)) {
                continue;
            }
            if (spawnData(data, world, attemptX, y, attemptZ)) {
                incrementSpawnEntryCount(data, spawnEntryCounts);
                markSpawnCooldown(world, data);
                return true;
            }
        }
        return false;
    }

    public static void performWorldGenSpawning(World world, int x, int z, Random rand) {
        int cap = getCap(world);
        int npcCount = countNPCs(world);
        if (npcCount >= cap) {
            return;
        }
        HashMap<Integer, Integer> spawnEntryCounts = getNaturalSpawnEntryCounts(world);
        BiomeGenBase biome = world.getBiomeGenForCoords(x + 8, z + 8);
        while (rand.nextFloat() < biome.getSpawningChance()) {
            if (npcCount >= cap) {
                return;
            }
            SpawnData data = SpawnController.Instance.getRandomSpawnData(biome.biomeName, world.provider.dimensionId);
            if (data == null)
                continue;

            int size = 16;

            int j1 = x + rand.nextInt(size);
            int k1 = z + rand.nextInt(size);
            int l1 = j1;
            int i2 = k1;

            for (int k2 = 0; k2 < 4; ++k2) {
                if (npcCount >= cap) {
                    return;
                }
                int l2 = world.getTopSolidOrLiquidBlock(j1, k1);
                if (l2 > data.spawnHeightMax || l2 < data.spawnHeightMin) {
                    continue;
                } else if (data.airSpawning && l2 < data.spawnHeightMax) {
                    l2 = l2 + (int) ((data.spawnHeightMax - l2) * Math.random());
                }

                if (!trySpawnWithEntryConfig(data, world, j1, l2, k1, rand, spawnEntryCounts)) {
                    j1 += rand.nextInt(5) - rand.nextInt(5);

                    for (k1 += rand.nextInt(5) - rand.nextInt(5); j1 < x || j1 >= x + size || k1 < z || k1 >= z + size; k1 = i2 + rand.nextInt(5) - rand.nextInt(5)) {
                        j1 = l1 + rand.nextInt(5) - rand.nextInt(5);
                    }
                } else {
                    npcCount++;
                    break;
                }

            }
        }
    }

    private static boolean spawnData(SpawnData data, World world, int x, int y, int z) {
        EntityLiving entityliving;

        try {
            NBTTagCompound[] allCompoundList = data.spawnCompounds.values().toArray(new NBTTagCompound[0]);
            ArrayList<Entity> entities = new ArrayList<>();
            for (NBTTagCompound compound : allCompoundList) {
                Entity entity;
                try {
                    Class<?> oclass = (Class<?>) EntityList.stringToClassMapping.get(compound.getString("id"));
                    if (oclass == null) {
                        continue;
                    }
                    entity = EntityList.createEntityFromNBT(compound, world);
                } catch (Exception e) {
                    continue;
                }
                if (entity != null) {
                    entities.add(entity);
                }
            }

            if (entities.size() == 0) {
                return false;
            }

            Entity spawnEntity = entities.get((int) Math.floor(Math.random() * entities.size()));

            if (!(spawnEntity instanceof EntityLiving)) {
                return false;
            }

            entityliving = (EntityLiving) spawnEntity;

            if (spawnEntity instanceof EntityCustomNpc) {
                EntityCustomNpc npc = (EntityCustomNpc) spawnEntity;
                npc.stats.spawnCycle = 3;
                if (data.despawnMode == SpawnData.DESPAWN_FORCE_PERSISTENT) {
                    npc.stats.canDespawn = false;
                    npc.stats.playerSetCanDespawn = false;
                } else if (data.despawnMode == SpawnData.DESPAWN_FORCE_NATURAL) {
                    npc.stats.canDespawn = true;
                    npc.stats.playerSetCanDespawn = true;
                }
                npc.ais.returnToStart = false;
                npc.ais.startPos = new int[]{x, y, z};
                npc.updateAI = true;
                npc.updateClient = true;
                npc.getNavigator().clearPathEntity();
            }
            entityliving.getEntityData().setInteger(NATURAL_SPAWN_ID_TAG, data.id);
            entityliving.getEntityData().setString(NATURAL_SPAWN_NAME_TAG, data.name);
            entityliving.getEntityData().setLong(NATURAL_SPAWN_TIME_TAG, world.getWorldInfo().getWorldTotalTime());
            spawnEntity.setLocationAndAngles(x + 0.5, y, z + 0.5, world.rand.nextFloat() * 360.0F, 0.0F);
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }

        Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, world, x + 0.5f, y, z + 0.5f);
        if (canSpawn == Result.DENY || (canSpawn == Result.DEFAULT && !canEntitySpawn(data, entityliving)))
            return false;

        CustomNPCsEvent.CNPCNaturalSpawnEvent event = new CustomNPCsEvent.CNPCNaturalSpawnEvent(entityliving, data, NpcAPI.Instance().getIPos(new BlockPos(x, y, z)), NPCSpawning.animalSpawn, NPCSpawning.monsterSpawn, NPCSpawning.liquidSpawn, NPCSpawning.airSpawn);
        if (EventHooks.onCNPCNaturalSpawn(event)) {
            return false;
        }

        entityliving.setPosition(event.attemptPosition.getX(), event.attemptPosition.getY(), event.attemptPosition.getZ());
        world.spawnEntityInWorld(entityliving);

        return true;
    }

    public static boolean canEntitySpawn(SpawnData data, EntityLiving entityLiving) {
        if (!data.liquidSpawning) {
            return entityLiving.getCanSpawnHere();
        } else {
            return entityLiving.worldObj.checkNoEntityCollision(entityLiving.boundingBox) && entityLiving.worldObj.getCollidingBoundingBoxes(entityLiving, entityLiving.boundingBox).isEmpty();
        }
    }

    public static boolean canCreatureTypeSpawnAtLocation(SpawnData data, World world, int x, int y, int z) {
        Block block = world.getBlock(x, y - 1, z);
        boolean hasSolidSurface = World.doesBlockHaveSolidTopSurface(world, x, y - 1, z);

        boolean spawnBlockCreature = block.canCreatureSpawn(EnumCreatureType.creature, world, x, y - 1, z);
        boolean spawnBlockMonster = block.canCreatureSpawn(EnumCreatureType.monster, world, x, y - 1, z);

        boolean animalSpawn = data.animalSpawning && hasSolidSurface && spawnBlockCreature && !world.getBlock(x, y, z).isNormalCube() && !world.getBlock(x, y, z).getMaterial().isLiquid() && !world.getBlock(x, y + 1, z).isNormalCube();
        boolean monsterSpawn = data.monsterSpawning && hasSolidSurface && spawnBlockMonster && !world.getBlock(x, y, z).isNormalCube() && !world.getBlock(x, y, z).getMaterial().isLiquid() && !world.getBlock(x, y + 1, z).isNormalCube();
        boolean liquidSpawn = data.liquidSpawning && world.getBlock(x, y - 1, z).getMaterial().isLiquid() && world.getBlock(x, y, z).getMaterial().isLiquid();
        boolean caveSpawn = data.airSpawning && world.getBlock(x, y - 1, z) == Blocks.air && world.getBlock(x, y, z) == Blocks.air && world.getBlock(x, y + 1, z) == Blocks.air;

        NPCSpawning.animalSpawn = animalSpawn;
        NPCSpawning.monsterSpawn = monsterSpawn;
        NPCSpawning.liquidSpawn = liquidSpawn;
        NPCSpawning.airSpawn = caveSpawn;

        return animalSpawn || monsterSpawn || caveSpawn || liquidSpawn;
    }
}
