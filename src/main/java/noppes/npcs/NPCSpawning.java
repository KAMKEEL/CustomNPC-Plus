package noppes.npcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.CustomNPCsEvent;

public class NPCSpawning {
	private static Set<ChunkCoordIntPair> eligibleChunksForSpawning = Sets.newHashSet();

    private static boolean animalSpawn;
    private static boolean monsterSpawn;
    private static boolean airSpawn;
    private static boolean liquidSpawn;

    public static void findChunksForSpawning(WorldServer world){
    	if(SpawnController.instance.data.isEmpty() || world.getWorldInfo().getWorldTotalTime() % 400L != 0L)
    		return;
    	eligibleChunksForSpawning.clear();
        for (int i = 0; i < world.playerEntities.size(); ++i){
            EntityPlayer entityplayer = (EntityPlayer)world.playerEntities.get(i);
            int j = MathHelper.floor_double(entityplayer.posX / 16.0D);
            int k = MathHelper.floor_double(entityplayer.posZ / 16.0D);
            byte size = 7;

            for (int x = -size; x <= size; ++x){
                for (int z = -size; z <= size; ++z){
                    ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(x + j, z + k);
                    if (!eligibleChunksForSpawning.contains(chunkcoordintpair)){
                        eligibleChunksForSpawning.add(chunkcoordintpair);
                    }
                }
            }
        }
        if (countNPCs(world) > eligibleChunksForSpawning.size()){
        	return;
        }
        ArrayList<ChunkCoordIntPair> tmp = new ArrayList(eligibleChunksForSpawning);
        Collections.shuffle(tmp);
        Iterator<ChunkCoordIntPair> iterator = tmp.iterator();

        while (iterator.hasNext()){
            ChunkCoordIntPair chunkcoordintpair1 = iterator.next();

            ChunkPosition chunkposition = getChunk(world, chunkcoordintpair1.chunkXPos, chunkcoordintpair1.chunkZPos);
            int j1 = chunkposition.chunkPosX;
            int y = chunkposition.chunkPosY;
            int l1 = chunkposition.chunkPosZ;

            for(int i = 0; i < 3; i++){
                int x = j1;
                int z = l1;
                byte b1 = 6;

                x += world.rand.nextInt(b1) - world.rand.nextInt(b1);
                z += world.rand.nextInt(b1) - world.rand.nextInt(b1);
                

                
                Block block = world.getBlock(x, y, z);

    			String name = world.getBiomeGenForCoords(x, z).biomeName;
    			SpawnData data = SpawnController.instance.getRandomSpawnData(name, block.getMaterial() == Material.air);
                if (data == null || !canCreatureTypeSpawnAtLocation(data, world, x, y, z) || world.getClosestPlayer(x, y, z, 24.0D) != null)
                	continue;
                
                spawnData(data, world, x, y, z);
            }
        }
    }
    
    public static int countNPCs(World world){
        int count = 0;
        List<Entity> list = world.loadedEntityList;
        for (Entity entity : list){
            if (entity instanceof EntityNPCInterface){
                count++;
            }
        }
        return count;
    }

    protected static ChunkPosition getChunk(World world, int x, int z){
        Chunk chunk = world.getChunkFromChunkCoords(x, z);
        int k = x * 16 + world.rand.nextInt(16);
        int l = z * 16 + world.rand.nextInt(16);
        int i1 = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
        return new ChunkPosition(k, i1, l);
    }
    
    public static void performWorldGenSpawning(World world, int x, int z, Random rand){
        BiomeGenBase biome = world.getBiomeGenForCoords(x + 8, z + 8);
    	while (rand.nextFloat() < biome.getSpawningChance()){
    		SpawnData data = SpawnController.instance.getRandomSpawnData(biome.biomeName, true);
    		if(data == null)
    			continue;
    		
    		int size = 16;
    		
            int j1 = x + rand.nextInt(size);
            int k1 = z + rand.nextInt(size);
            int l1 = j1;
            int i2 = k1;

            for (int k2 = 0; k2 < 4; ++k2){
                int l2 = world.getTopSolidOrLiquidBlock(j1, k1);
                if (l2 > data.spawnHeightMax || l2 < data.spawnHeightMin) {
                    continue;
                } else if (data.airSpawning && l2 < data.spawnHeightMax) {
                    l2 = l2 + (int) ((data.spawnHeightMax - l2) * Math.random());
                }

                if (!canCreatureTypeSpawnAtLocation(data, world, j1, l2, k1)){
                    j1 += rand.nextInt(5) - rand.nextInt(5);

                    for (k1 += rand.nextInt(5) - rand.nextInt(5); j1 < x || j1 >= x + size || k1 < z || k1 >= z + size; k1 = i2 + rand.nextInt(5) - rand.nextInt(5))
                    {
                        j1 = l1 + rand.nextInt(5) - rand.nextInt(5);
                    }
                }
                else if(spawnData(data, world, j1, l2, k1))
	                break;
                
            }
        }
    }
    
    private static boolean spawnData(SpawnData data, World world, int x, int y, int z){
        EntityLiving entityliving;

        try{
            NBTTagCompound[] allCompoundList = new NBTTagCompound[]{data.compound1,data.compound2,data.compound3,data.compound4,data.compound5};
            ArrayList<Entity> entities = new ArrayList<>();
            for (NBTTagCompound compound : allCompoundList) {
                Entity entity;
                try {
                    Class<?> oclass = (Class<?>)EntityList.stringToClassMapping.get(compound.getString("id"));
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

            Entity spawnEntity = entities.get((int)Math.floor(Math.random()*entities.size()));

			if (!(spawnEntity instanceof EntityLiving)) {
                return false;
            }

			entityliving = (EntityLiving) spawnEntity;
			
			if(spawnEntity instanceof EntityCustomNpc){
				EntityCustomNpc npc = (EntityCustomNpc) spawnEntity;
				npc.stats.spawnCycle = 3;
				npc.ai.returnToStart = false;
				npc.ai.startPos = new int[]{x,y, z};
                npc.updateAI = true;
                npc.updateClient = true;
                npc.getNavigator().clearPathEntity();
			}
			spawnEntity.setLocationAndAngles(x + 0.5, y, z + 0.5, world.rand.nextFloat() * 360.0F, 0.0F);
        }
        catch (Exception exception){
            exception.printStackTrace();
            return false;
        }

        Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, world, x + 0.5f, y, z + 0.5f);
        if (canSpawn == Result.DENY || (canSpawn == Result.DEFAULT && !canEntitySpawn(data,entityliving)))
        	return false;

        CustomNPCsEvent.CNPCNaturalSpawnEvent event = new CustomNPCsEvent.CNPCNaturalSpawnEvent(data, NpcAPI.Instance().getIPos(new BlockPos(x,y,z)), NPCSpawning.animalSpawn, NPCSpawning.monsterSpawn, NPCSpawning.liquidSpawn, NPCSpawning.airSpawn);
        if (EventHooks.onCNPCNaturalSpawn(event)) {
            return false;
        }

        entityliving.setPosition(event.attemptPosition.getX(),event.attemptPosition.getY(),event.attemptPosition.getZ());
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
    
    public static boolean canCreatureTypeSpawnAtLocation(SpawnData data, World world, int x, int y, int z){
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

