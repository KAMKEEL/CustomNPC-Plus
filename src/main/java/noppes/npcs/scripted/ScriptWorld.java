package noppes.npcs.scripted;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.*;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ISound;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.scoreboard.IScoreboard;
import noppes.npcs.blocks.tiles.TileBigSign;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.scoreboard.ScriptScoreboard;

import java.util.*;

public class ScriptWorld implements IWorld {
	private static final Map<String,Object> tempData = new HashMap<>();
	public WorldServer world;
	public ScriptWorld(WorldServer world){
		this.world = world;
	}

	public static ScriptWorld createNew(int dimensionId) {
		WorldServer[] worlds = CustomNpcs.getServer().worldServers;

		WorldServer world = worlds[0];
		for (WorldServer w : worlds) {
			if (w.provider.dimensionId == dimensionId) {
				world = w;
			}
		}
		return new ScriptWorld(world);
	}

	/**
	 * @return The worlds time
	 */
	public long getTime(){
		return world.getWorldTime();
	}

	/**
	 * @return The total world time
	 */
	public long getTotalTime(){
		return world.getTotalWorldTime();
	}

	public boolean areAllPlayersAsleep(){
		return world.areAllPlayersAsleep();
	}

	/**
	 * @param x World position x
	 * @param y World position y
	 * @param z World position z
	 * @return The block at the given position. Returns null if there isn't a block
	 */
	public IBlock getBlock(int x, int y, int z){
		return NpcAPI.Instance().getIBlock(this, x,y,z);
	}

	/**
	 * @param pos
	 * @return The block at the given position. Returns null if there isn't a block
	 */
	public IBlock getBlock(IPos pos){
		return NpcAPI.Instance().getIBlock(this, pos);
	}

	public boolean isBlockFreezable(IPos pos) {
		return this.isBlockFreezable(pos.getX(),pos.getY(),pos.getZ());
	}

	public boolean isBlockFreezable(int x, int y, int z){
		return world.isBlockFreezable(x,y,z);
	}

	public boolean isBlockFreezableNaturally(IPos pos) {
		return this.isBlockFreezableNaturally(pos.getX(),pos.getY(),pos.getZ());
	}

	public boolean isBlockFreezableNaturally(int x, int y, int z){
		return world.isBlockFreezableNaturally(x,y,z);
	}

	public boolean canBlockFreeze(IPos pos, boolean adjacentToWater) {
		return this.canBlockFreeze(pos.getX(),pos.getY(),pos.getZ(), adjacentToWater);
	}

	public boolean canBlockFreeze(int x, int y, int z, boolean adjacentToWater){
		return world.canBlockFreeze(x,y,z,adjacentToWater);
	}

	public boolean canBlockFreezeBody(IPos pos, boolean adjacentToWater) {
		return this.canBlockFreezeBody(pos.getX(),pos.getY(),pos.getZ(), adjacentToWater);
	}

	public boolean canBlockFreezeBody(int x, int y, int z, boolean adjacentToWater){
		return world.canBlockFreezeBody(x,y,z,adjacentToWater);
	}

	public boolean canSnowAt(IPos pos, boolean checkLight) {
		return this.canSnowAt(pos.getX(),pos.getY(),pos.getZ(), checkLight);
	}

	public boolean canSnowAt(int x, int y, int z, boolean checkLight){
		return world.func_147478_e(x,y,z,checkLight);
	}

	public boolean canSnowAtBody(IPos pos, boolean checkLight) {
		return this.canSnowAtBody(pos.getX(),pos.getY(),pos.getZ(), checkLight);
	}

	public boolean canSnowAtBody(int x, int y, int z, boolean checkLight){
		return world.canSnowAtBody(x,y,z,checkLight);
	}

	public IBlock getTopBlock(int x, int z){
		return NpcAPI.Instance().getIBlock(this, x, world.getTopSolidOrLiquidBlock(x,z) ,z);
	}

	public IBlock getTopBlock(IPos pos) {
		return this.getTopBlock(pos.getX(),pos.getZ());
	}

	public int getHeightValue(int x, int z){
		return world.getHeightValue(x,z);
	}

	public int getHeightValue(IPos pos) {
		return this.getHeightValue(pos.getX(),pos.getZ());
	}

	public int getChunkHeightMapMinimum(int x, int z){
		return world.getChunkHeightMapMinimum(x,z);
	}

	public int getChunkHeightMapMinimum(IPos pos){
		return this.getChunkHeightMapMinimum(pos.getX(),pos.getZ());
	}

	public int getBlockMetadata(int x, int y, int z){
		return world.getBlockMetadata(x,y,z);
	}

	public int getBlockMetadata(IPos pos){
		return this.getBlockMetadata(pos.getX(),pos.getY(),pos.getZ());
	}

	public boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag){
		return world.setBlockMetadataWithNotify(x,y,z,metadata,flag);
	}

	public boolean setBlockMetadataWithNotify(IPos pos, int metadata, int flag){
		return this.setBlockMetadataWithNotify(pos.getX(),pos.getY(),pos.getZ(), metadata, flag);
	}

	public boolean canSeeSky(int x, int y, int z) {
		return world.canBlockSeeTheSky(x, y, z);
	}
	public boolean canSeeSky(IPos pos) {
		return canSeeSky(pos.getX(), pos.getY(), pos.getZ());
	}

	public int getFullBlockLightValue(int x, int y, int z){
		return world.getFullBlockLightValue(x,y,z);
	}

	public int getFullBlockLightValue(IPos pos){
		return this.getFullBlockLightValue(pos.getX(),pos.getY(),pos.getZ());
	}

	public int getBlockLightValue(int x, int y, int z){
		return world.getBlockLightValue(x,y,z);
	}

	public int getBlockLightValue(IPos pos){
		return this.getBlockLightValue(pos.getX(),pos.getY(),pos.getZ());
	}

	public void playSoundAtEntity(IEntity entity, String sound, float volume, float pitch){
		world.playSoundAtEntity(entity.getMCEntity(), sound, volume, pitch);
	}

	public void playSoundToNearExcept(IPlayer player, String sound, float volume, float pitch){
		world.playSoundToNearExcept((EntityPlayerMP) player.getMCEntity(), sound, volume, pitch);
	}

	public void playSound(int id, ISound sound) {
		IPlayer[] players = getAllServerPlayers();
		for (IPlayer player : players) {
			if (player.getDimension() == this.getDimensionID()) {
				player.playSound(id, sound);
			}
		}
	}

	public void stopSound(int id) {
		IPlayer[] players = getAllServerPlayers();
		for (IPlayer player : players) {
			if (player.getDimension() == this.getDimensionID()) {
				player.stopSound(id);
			}
		}
	}

	public void pauseSounds() {
		IPlayer[] players = getAllServerPlayers();
		for (IPlayer player : players) {
			if (player.getDimension() == this.getDimensionID()) {
				player.pauseSounds();
			}
		}
	}

	public void continueSounds() {
		IPlayer[] players = getAllServerPlayers();
		for (IPlayer player : players) {
			if (player.getDimension() == this.getDimensionID()) {
				player.continueSounds();
			}
		}
	}

	public void stopSounds() {
		IPlayer[] players = getAllServerPlayers();
		for (IPlayer player : players) {
			if (player.getDimension() == this.getDimensionID()) {
				player.stopSounds();
			}
		}
	}

	public IEntity getEntityByID(int id){
		return NpcAPI.Instance().getIEntity(world.getEntityByID(id));
	}

	public boolean spawnEntityInWorld(IEntity entity){
		return world.spawnEntityInWorld(entity.getMCEntity());
	}

	public boolean spawnEntityInWorld(IEntity entity, double x, double y, double z){
		entity.setPosition(x, y, z);
		return this.spawnEntityInWorld(entity);
	}

	public boolean spawnEntityInWorld(IEntity entity, IPos pos) {
		return this.spawnEntityInWorld(entity, pos.getX(), pos.getY(), pos.getZ());
	}

	public IPlayer getClosestPlayerToEntity(IEntity entity, double range){
		return (IPlayer) NpcAPI.Instance().getIEntity(world.getClosestPlayerToEntity(entity.getMCEntity(), range));
	}

	public IPlayer getClosestPlayer(double x, double y, double z, double range){
		return (IPlayer) NpcAPI.Instance().getIEntity(world.getClosestPlayer(x,y,z, range));
	}

	public IPlayer getClosestPlayer(IPos pos, double range){
		return this.getClosestPlayer(pos.getX(),pos.getY(),pos.getZ(), range);
	}

	public IPlayer getClosestVulnerablePlayerToEntity(IEntity entity, double range){
		return (IPlayer) NpcAPI.Instance().getIEntity(world.getClosestVulnerablePlayerToEntity(entity.getMCEntity(), range));
	}

	public IPlayer getClosestVulnerablePlayer(double x, double y, double z, double range){
		return (IPlayer) NpcAPI.Instance().getIEntity(world.getClosestVulnerablePlayer(x,y,z, range));
	}

	public IPlayer getClosestVulnerablePlayer(IPos pos, double range){
		return this.getClosestVulnerablePlayer(pos.getX(),pos.getY(),pos.getZ(), range);
	}

	public int countEntities(IEntity entity){
		return world.countEntities(entity.getMCEntity().getClass());
	}

	public IEntity[] getLoadedEntities() {
		ArrayList<IEntity> list = new ArrayList<>();
		for (Object obj : world.loadedEntityList) {
			list.add(NpcAPI.Instance().getIEntity((Entity) obj));
		}

		return list.toArray(new IEntity[0]);
	}

	public IEntity[] getEntitiesNear(IPos position, double range) {
		ArrayList<IEntity> list = new ArrayList<>();

		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(
				position.getX() - range, position.getY() - range, position.getZ() - range,
				position.getX() + range, position.getY() + range, position.getZ() + range));
		for(Entity entity : entities){
			list.add(NpcAPI.Instance().getIEntity(entity));
		}

		list.sort((e1, e2) -> {
			double dist1 = e1.getPosition().distanceTo(position);
			double dist2 = e2.getPosition().distanceTo(position);

			if (dist1 > dist2) {
				return 1;
			} else if (dist1 < dist2) {
				return -1;
			}
			return 0;
		});

		return list.toArray(new IEntity[0]);
	}

	public IEntity[] getEntitiesNear(double x, double y, double z, double range) {
		return getEntitiesNear(NpcAPI.Instance().getIPos(x,y,z), range);
	}

	public void setTileEntity(int x, int y, int z, ITileEntity tileEntity){
		world.setTileEntity(x,y,z,tileEntity.getMCTileEntity());
	}

	public void setTileEntity(IPos pos, ITileEntity tileEntity){
		this.setTileEntity(pos.getX(),pos.getY(),pos.getZ(),tileEntity);
	}

	public void removeTileEntity(int x, int y, int z){
		world.removeTileEntity(x,y,z);
	}

	public void removeTileEntity(IPos pos){
		this.removeTileEntity(pos.getX(),pos.getY(),pos.getZ());
	}

	public boolean isBlockFullCube(int x, int y, int z){
		return world.func_147469_q(x,y,z);
	}

	public boolean isBlockFullCube(IPos pos){
		return this.isBlockFullCube(pos.getX(),pos.getY(),pos.getZ());
	}

	public long getSeed(){
		return world.getSeed();
	}

	public void setSpawnLocation(int x, int y, int z){
		world.setSpawnLocation(x,y,z);
	}

	public void setSpawnLocation(IPos pos){
		this.setSpawnLocation(pos.getX(),pos.getY(),pos.getZ());
	}

	public boolean canLightningStrikeAt(int x, int y, int z){
		return world.canLightningStrikeAt(x,y,z);
	}

	public boolean canLightningStrikeAt(IPos pos){
		return this.canLightningStrikeAt(pos.getX(),pos.getY(),pos.getZ());
	}

	public boolean isBlockHighHumidity(int x, int y, int z){
		return world.isBlockHighHumidity(x,y,z);
	}

	public boolean isBlockHighHumidity(IPos pos){
		return this.canLightningStrikeAt(pos.getX(),pos.getY(),pos.getZ());
	}

	/**
	 * @param x World position x
	 * @param y World position y
	 * @param z World position z
	 * @return Text from signs
	 * @since 1.7.10d
	 */
	public String getSignText(int x, int y, int z){
		TileEntity tile = world.getTileEntity(x, y, z);

		if(tile instanceof TileBigSign)
			return ((TileBigSign)tile).getText();

		if(tile instanceof TileEntitySign){
			TileEntitySign tileSign = (TileEntitySign)tile;
			String s = tileSign.signText[0] + "\n";
			s += tileSign.signText[1] + "\n";
			s += tileSign.signText[2] + "\n";
			s += tileSign.signText[3];
			return s;
		}

		return null;
	}
	public String getSignText(IPos pos) {
		return this.getSignText(pos.getX(),pos.getY(),pos.getZ());
	}


	public boolean setBlock(int x, int y, int z, IBlock block){
		if(block == null || block.getMCBlock().isAir(world, x, y, z)){
			this.removeBlock(x, y, z);
			return false;
		}
		return world.setBlock(x, y, z, block.getMCBlock());
	}

	public boolean setBlock(IPos pos, IBlock block){
		return this.setBlock(pos.getX(),pos.getY(),pos.getZ(),block);
	}

	/**
	 * @param x World position x
	 * @param y World position y
	 * @param z World position z
	 * @param item The block to be set
	 */
	public boolean setBlock(int x, int y, int z, IItemStack item){
		if(item == null){
			this.removeBlock(x, y, z);
			return false;
		}
		Block block = Block.getBlockFromItem(item.getMCItemStack().getItem());
		if(block == null || block == Blocks.air)
			return false;

		return world.setBlock(x, y, z, block);
	}

	public boolean setBlock(IPos pos, IItemStack itemStack){
		return this.setBlock(pos.getX(),pos.getY(),pos.getZ(),itemStack);
	}

	/**
	 * @param x World position x
	 * @param y World position y
	 * @param z World position z
	 */
	public void removeBlock(int x, int y, int z){
		world.setBlock(x, y, z, Blocks.air);
	}

	public void removeBlock(IPos pos){
		this.removeBlock(pos.getX(),pos.getY(),pos.getZ());
	}

	public boolean isPlaceCancelled(int posX, int posY, int posZ) {
		IBlock block = this.getBlock(posX,posY,posZ);
		if (block == null) {
			return false;
		}

		Block mcBlock = block.getMCBlock();
		int metadata = this.getBlockMetadata(posX,posY,posZ);

		FakePlayer fakePlayer = new FakePlayer(this.world, EntityNPCInterface.chateventProfile);
		IItemStack stack = NpcAPI.Instance().createItem("minecraft:stone",0,1);
		fakePlayer.setCurrentItemOrArmor(0, stack.getMCItemStack());

		final BlockEvent.PlaceEvent placeEvent = new BlockEvent.PlaceEvent(
				new BlockSnapshot(this.world, (int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ), mcBlock, metadata),
				Blocks.air, fakePlayer);

		MinecraftForge.EVENT_BUS.post(placeEvent);

		return placeEvent.isCanceled();
	}

	public boolean isPlaceCancelled(IPos pos) {
		return this.isPlaceCancelled(pos.getX(),pos.getY(),pos.getZ());
	}

	public boolean isBreakCancelled(int posX, int posY, int posZ) {
		IBlock block = this.getBlock(posX,posY,posZ);
		if (block == null) {
			return false;
		}

		Block mcBlock = block.getMCBlock();
		int metadata = this.getBlockMetadata(posX,posY,posZ);

		FakePlayer fakePlayer = new FakePlayer(this.world, EntityNPCInterface.chateventProfile);
		IItemStack stack = NpcAPI.Instance().createItem("minecraft:stone",0,1);
		fakePlayer.setCurrentItemOrArmor(0, stack.getMCItemStack());

		final BlockEvent.BreakEvent placeEvent = new BlockEvent.BreakEvent(posX, posY, posZ, world,
				mcBlock, metadata, fakePlayer);

		MinecraftForge.EVENT_BUS.post(placeEvent);

		return placeEvent.isCanceled();
	}

	public boolean isBreakCancelled(IPos pos) {
		return this.isBreakCancelled(pos.getX(),pos.getY(),pos.getZ());
	}

	public MovingObjectPosition rayCast(Vec3 startVec, Vec3 endVec, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision)
	{
		if (!Double.isNaN(startVec.xCoord) && !Double.isNaN(startVec.yCoord) && !Double.isNaN(startVec.zCoord))
		{
			if (!Double.isNaN(endVec.xCoord) && !Double.isNaN(endVec.yCoord) && !Double.isNaN(endVec.zCoord))
			{
				int endX = MathHelper.floor_double(endVec.xCoord);
				int endY = MathHelper.floor_double(endVec.yCoord);
				int endZ = MathHelper.floor_double(endVec.zCoord);
				int l = MathHelper.floor_double(startVec.xCoord);
				int i1 = MathHelper.floor_double(startVec.yCoord);
				int j1 = MathHelper.floor_double(startVec.zCoord);
				Block block = this.world.getBlock(l, i1, j1);
				int k1 = this.world.getBlockMetadata(l, i1, j1);

				if (block.canCollideCheck(k1, false))
				{
					MovingObjectPosition movingobjectposition = block.collisionRayTrace(this.world, l, i1, j1, startVec, endVec);

					if (movingobjectposition != null)
					{
						return movingobjectposition;
					}
				}

				k1 = 200;

				while (k1-- >= 0)
				{
					if (Double.isNaN(startVec.xCoord) || Double.isNaN(startVec.yCoord) || Double.isNaN(startVec.zCoord))
					{
						return null;
					}

					if (l == endX && i1 == endY && j1 == endZ)
					{
						return null;
					}

					boolean flag6 = true;
					boolean flag3 = true;
					boolean flag4 = true;
					double d0 = 999.0D;
					double d1 = 999.0D;
					double d2 = 999.0D;

					if (endX > l)
					{
						d0 = (double)l + 1.0D;
					}
					else if (endX < l)
					{
						d0 = (double)l + 0.0D;
					}
					else
					{
						flag6 = false;
					}

					if (endY > i1)
					{
						d1 = (double)i1 + 1.0D;
					}
					else if (endY < i1)
					{
						d1 = (double)i1 + 0.0D;
					}
					else
					{
						flag3 = false;
					}

					if (endZ > j1)
					{
						d2 = (double)j1 + 1.0D;
					}
					else if (endZ < j1)
					{
						d2 = (double)j1 + 0.0D;
					}
					else
					{
						flag4 = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
					double d6 = endVec.xCoord - startVec.xCoord;
					double d7 = endVec.yCoord - startVec.yCoord;
					double d8 = endVec.zCoord - startVec.zCoord;

					if (flag6)
					{
						d3 = (d0 - startVec.xCoord) / d6;
					}

					if (flag3)
					{
						d4 = (d1 - startVec.yCoord) / d7;
					}

					if (flag4)
					{
						d5 = (d2 - startVec.zCoord) / d8;
					}

					byte b0;

					if (d3 < d4 && d3 < d5)
					{
						if (endX > l)
						{
							b0 = 4;
						}
						else
						{
							b0 = 5;
						}

						startVec.xCoord = d0;
						startVec.yCoord += d7 * d3;
						startVec.zCoord += d8 * d3;
					}
					else if (d4 < d5)
					{
						if (endY > i1)
						{
							b0 = 0;
						}
						else
						{
							b0 = 1;
						}

						startVec.xCoord += d6 * d4;
						startVec.yCoord = d1;
						startVec.zCoord += d8 * d4;
					}
					else
					{
						if (endZ > j1)
						{
							b0 = 2;
						}
						else
						{
							b0 = 3;
						}

						startVec.xCoord += d6 * d5;
						startVec.yCoord += d7 * d5;
						startVec.zCoord = d2;
					}

					Vec3 vec32 = Vec3.createVectorHelper(startVec.xCoord, startVec.yCoord, startVec.zCoord);
					l = (int)(vec32.xCoord = (double)MathHelper.floor_double(startVec.xCoord));

					if (b0 == 5)
					{
						--l;
						++vec32.xCoord;
					}

					i1 = (int)(vec32.yCoord = (double)MathHelper.floor_double(startVec.yCoord));

					if (b0 == 1)
					{
						--i1;
						++vec32.yCoord;
					}

					j1 = (int)(vec32.zCoord = (double)MathHelper.floor_double(startVec.zCoord));

					if (b0 == 3)
					{
						--j1;
						++vec32.zCoord;
					}

					Block block1 = this.world.getBlock(l, i1, j1);
					int l1 = this.world.getBlockMetadata(l, i1, j1);

					MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(this.world, l, i1, j1, startVec, endVec);
					if (movingobjectposition1 != null)
					{
						if (block1.canCollideCheck(l1, false) && stopOnBlock ||
							block1.getMaterial().isLiquid() && stopOnLiquid ||
							!(block1 instanceof BlockAir) && block1.getCollisionBoundingBoxFromPool(this.world,l,i1,j1) == null && !stopOnCollision)
						{
							return movingobjectposition1;
						}
					}
				}

				return null;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	public IPos rayCastPos(double[] startPos, double[] lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		if (startPos.length != 3 || lookVector.length != 3) {
			return null;
		}

		Vec3 startVec = Vec3.createVectorHelper(startPos[0], startPos[1], startPos[2]);
		Vec3 endVec = startVec.addVector(lookVector[0]*maxDistance,lookVector[1]*maxDistance,lookVector[2]*maxDistance);

		IPos endPos = NpcAPI.Instance().getIPos(endVec.xCoord,endVec.yCoord,endVec.zCoord);
		if (!stopOnBlock && !stopOnLiquid && !stopOnCollision) {
			return endPos;
		}

		MovingObjectPosition mob = this.rayCast(startVec,endVec,stopOnBlock,stopOnLiquid,stopOnCollision);
		return mob != null ? NpcAPI.Instance().getIPos(mob.blockX,mob.blockY,mob.blockZ) : endPos;
	}

	public IPos rayCastPos(double[] startPos, double[] lookVector, int maxDistance) {
		return rayCastPos(startPos,lookVector,maxDistance,true, false, false);
	}

	public IPos rayCastPos(IPos startPos, IPos lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		return rayCastPos(new double[] {startPos.getX(), startPos.getY(), startPos.getZ()}, lookVector.normalizeDouble(), maxDistance, stopOnBlock, stopOnLiquid, stopOnCollision);
	}

	public IPos rayCastPos(IPos startPos, IPos lookVector, int maxDistance) {
		return rayCastPos(new double[] {startPos.getX(), startPos.getY(), startPos.getZ()}, lookVector.normalizeDouble(), maxDistance, true, false, false);
	}

	public IBlock rayCastBlock(double[] startPos, double[] lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		return getBlock(rayCastPos(startPos,lookVector,maxDistance,stopOnBlock,stopOnLiquid,stopOnCollision));
	}

	public IBlock rayCastBlock(double[] startPos, double[] lookVector, int maxDistance) {
		return rayCastBlock(startPos, lookVector, maxDistance, true, false, false);
	}

	public IBlock rayCastBlock(IPos startPos, IPos lookVector, int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		return rayCastBlock(new double[] {startPos.getX(), startPos.getY(), startPos.getZ()}, lookVector.normalizeDouble(), maxDistance, stopOnBlock, stopOnLiquid, stopOnCollision);
	}

	public IBlock rayCastBlock(IPos startPos, IPos lookVector, int maxDistance) {
		return rayCastBlock(new double[] {startPos.getX(), startPos.getY(), startPos.getZ()}, lookVector.normalizeDouble(), maxDistance);
	}

	public IPos getNearestAir(IPos pos, int maxHeight) {
		if (pos == null) return null;
		IPos currentPos = pos;
		IBlock block = null; int rep = 0;
		while (rep++ < maxHeight) {
			//check +x
			currentPos = currentPos.add(1, 0, 0);
			block = getBlock(currentPos);
			if (block == null) break;
			//check -x
			currentPos = currentPos.add(-2, 0, 0);
			block = getBlock(currentPos);
			if (block == null) break;
			//check +z
			currentPos = currentPos.add(1, 0, 1);
			block = getBlock(currentPos);
			if (block == null) break;
			//check -z
			currentPos = currentPos.add(0, 0, -2);
			block = getBlock(currentPos);
			if (block == null) break;
			//check up 1
			currentPos = currentPos.add(0, 1, 1);
			block = getBlock(currentPos);
			if (block == null) break;
		}
		return currentPos;
	}

	public IEntity[] rayCastEntities(double[] startPos, double[] lookVector,
										  int maxDistance, double offset, double range,
										  boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		return rayCastEntities(null,startPos,lookVector,maxDistance,offset,range,stopOnBlock,stopOnLiquid,stopOnCollision);
	}

	public IEntity[] rayCastEntities(IEntity[] ignoreEntities, double[] startPos, double[] lookVector,
									 int maxDistance, double offset, double range,
									 boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		if (ignoreEntities == null) {
			ignoreEntities = new IEntity[0];
		}

		Vec3 startVec = Vec3.createVectorHelper(startPos[0], startPos[1], startPos[2]);
		Vec3 endVec = startVec.addVector(lookVector[0]*maxDistance,lookVector[1]*maxDistance,lookVector[2]*maxDistance);
		startVec = startVec.addVector(lookVector[0]*offset, lookVector[1]*offset, lookVector[2]*offset);

		LinkedHashSet<IEntity> ignoredEntitiesSet = new LinkedHashSet<>();
		Collections.addAll(ignoredEntitiesSet, ignoreEntities);

		Set<IEntity<?>> entities = this.rayCastEntities(ignoredEntitiesSet,startVec,endVec,range,stopOnBlock,stopOnLiquid,stopOnCollision);
		return entities.toArray(new IEntity[0]);
	}

	public Set<IEntity<?>> rayCastEntities(LinkedHashSet<IEntity> ignoredEntitiesSet, Vec3 startVec, Vec3 endVec, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision)
	{
		LinkedHashSet<IEntity<?>> entities = new LinkedHashSet<>();

		if (!Double.isNaN(startVec.xCoord) && !Double.isNaN(startVec.yCoord) && !Double.isNaN(startVec.zCoord))
		{
			if (!Double.isNaN(endVec.xCoord) && !Double.isNaN(endVec.yCoord) && !Double.isNaN(endVec.zCoord))
			{
				int endX = MathHelper.floor_double(endVec.xCoord);
				int endY = MathHelper.floor_double(endVec.yCoord);
				int endZ = MathHelper.floor_double(endVec.zCoord);
				int l = MathHelper.floor_double(startVec.xCoord);
				int i1 = MathHelper.floor_double(startVec.yCoord);
				int j1 = MathHelper.floor_double(startVec.zCoord);
				Block block = this.world.getBlock(l, i1, j1);
				int k1 = this.world.getBlockMetadata(l, i1, j1);

				IEntity<?>[] surrounding = this.getEntitiesNear(l,i1,j1,range);
				for (IEntity<?> entity : surrounding) {
					if (!ignoredEntitiesSet.contains(entity)) {
						entities.add(entity);
					}
				}

				if (block.canCollideCheck(k1, false))
				{
					MovingObjectPosition movingobjectposition = block.collisionRayTrace(this.world, l, i1, j1, startVec, endVec);

					if (movingobjectposition != null)
					{
						return entities;
					}
				}

				k1 = 200;

				while (k1-- >= 0)
				{
					if (Double.isNaN(startVec.xCoord) || Double.isNaN(startVec.yCoord) || Double.isNaN(startVec.zCoord))
					{
						return entities;
					}

					if (l == endX && i1 == endY && j1 == endZ)
					{
						return entities;
					}

					boolean flag6 = true;
					boolean flag3 = true;
					boolean flag4 = true;
					double d0 = 999.0D;
					double d1 = 999.0D;
					double d2 = 999.0D;

					if (endX > l)
					{
						d0 = (double)l + 1.0D;
					}
					else if (endX < l)
					{
						d0 = (double)l + 0.0D;
					}
					else
					{
						flag6 = false;
					}

					if (endY > i1)
					{
						d1 = (double)i1 + 1.0D;
					}
					else if (endY < i1)
					{
						d1 = (double)i1 + 0.0D;
					}
					else
					{
						flag3 = false;
					}

					if (endZ > j1)
					{
						d2 = (double)j1 + 1.0D;
					}
					else if (endZ < j1)
					{
						d2 = (double)j1 + 0.0D;
					}
					else
					{
						flag4 = false;
					}

					double d3 = 999.0D;
					double d4 = 999.0D;
					double d5 = 999.0D;
					double d6 = endVec.xCoord - startVec.xCoord;
					double d7 = endVec.yCoord - startVec.yCoord;
					double d8 = endVec.zCoord - startVec.zCoord;

					if (flag6)
					{
						d3 = (d0 - startVec.xCoord) / d6;
					}

					if (flag3)
					{
						d4 = (d1 - startVec.yCoord) / d7;
					}

					if (flag4)
					{
						d5 = (d2 - startVec.zCoord) / d8;
					}

					byte b0;

					if (d3 < d4 && d3 < d5)
					{
						if (endX > l)
						{
							b0 = 4;
						}
						else
						{
							b0 = 5;
						}

						startVec.xCoord = d0;
						startVec.yCoord += d7 * d3;
						startVec.zCoord += d8 * d3;
					}
					else if (d4 < d5)
					{
						if (endY > i1)
						{
							b0 = 0;
						}
						else
						{
							b0 = 1;
						}

						startVec.xCoord += d6 * d4;
						startVec.yCoord = d1;
						startVec.zCoord += d8 * d4;
					}
					else
					{
						if (endZ > j1)
						{
							b0 = 2;
						}
						else
						{
							b0 = 3;
						}

						startVec.xCoord += d6 * d5;
						startVec.yCoord += d7 * d5;
						startVec.zCoord = d2;
					}

					Vec3 vec32 = Vec3.createVectorHelper(startVec.xCoord, startVec.yCoord, startVec.zCoord);
					l = (int)(vec32.xCoord = (double)MathHelper.floor_double(startVec.xCoord));

					if (b0 == 5)
					{
						--l;
						++vec32.xCoord;
					}

					i1 = (int)(vec32.yCoord = (double)MathHelper.floor_double(startVec.yCoord));

					if (b0 == 1)
					{
						--i1;
						++vec32.yCoord;
					}

					j1 = (int)(vec32.zCoord = (double)MathHelper.floor_double(startVec.zCoord));

					if (b0 == 3)
					{
						--j1;
						++vec32.zCoord;
					}

					Block block1 = this.world.getBlock(l, i1, j1);
					int l1 = this.world.getBlockMetadata(l, i1, j1);

					IEntity<?>[] surroundingEntities = this.getEntitiesNear(l,i1,j1,range);
					for (IEntity<?> entity : surroundingEntities) {
						if (!ignoredEntitiesSet.contains(entity)) {
							entities.add(entity);
						}
					}

					MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(this.world, l, i1, j1, startVec, endVec);
					if (movingobjectposition1 != null)
					{
						if (block1.canCollideCheck(l1, false) && stopOnBlock ||
								block1.getMaterial().isLiquid() && stopOnLiquid ||
								!(block1 instanceof BlockAir) && block1.getCollisionBoundingBoxFromPool(this.world,l,i1,j1) == null && !stopOnCollision)
						{
							return entities;
						}
					}
				}

				return entities;
			}
			else
			{
				return entities;
			}
		}
		else
		{
			return entities;
		}
	}

	public IEntity[] rayCastEntities(IPos startPos, IPos lookVector,
									 int maxDistance, double offset, double range,
									 boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		return rayCastEntities(new double[] {startPos.getX(), startPos.getY(), startPos.getZ()}, lookVector.normalizeDouble(), maxDistance, offset, range, stopOnBlock, stopOnLiquid, stopOnCollision);
	}

	public IEntity[] rayCastEntities(double[] startPos, double[] lookVector,
									 int maxDistance, double offset, double range) {
		return rayCastEntities(startPos, lookVector, maxDistance, offset, range, true, false, true);
	}

	public IEntity[] rayCastEntities(IPos startPos, IPos lookVector,
									 int maxDistance, double offset, double range) {
		return rayCastEntities(new double[] {startPos.getX(), startPos.getY(), startPos.getZ()}, lookVector.normalizeDouble(), maxDistance, offset, range, true, false, true);
	}

	@Deprecated
	/**
	 * Deprecated. Use NpcAPI.Instance().getPlayer(name) instead.
	 */
	public IPlayer getPlayer(String name){
		EntityPlayer player = world.getPlayerEntityByName(name);
		if(player == null)
			return null;
		return (IPlayer) NpcAPI.Instance().getIEntity(player);
	}

	public IPlayer getPlayerByUUID(String uuid){
		return (IPlayer) NpcAPI.Instance().getIEntity(world.func_152378_a(UUID.fromString(uuid)));
	}

	/**
	 * @param time The world time to be set
	 */
	public void setTime(long time){
		world.setWorldTime(time);
	}

	/**
	 * @return Whether or not its daytime
	 */
	public boolean isDay(){
		return world.getWorldTime() % 24000 < 12000;
	}

	/**
	 * @return Whether or not its currently raining
	 */
	public boolean isRaining(){
		return world.getWorldInfo().isRaining();
	}

	/**
	 * @param bo Set if it's raining
	 */
	public void setRaining(boolean bo){
		world.getWorldInfo().setRaining(bo);
	}

	/**
	 * @param x The x position
	 * @param y The y position
	 * @param z The z position
	 */
	public void thunderStrike(double x, double y, double z){
        world.addWeatherEffect(new EntityLightningBolt(world, x, y, z));
	}

	public void thunderStrike(IPos pos){
		this.thunderStrike(pos.getX(),pos.getY(),pos.getZ());
	}

	/**
	 * Sends a packet from the server to the client everytime its called. Probably should not use this too much.
	 * @param particle Particle name. Particle name list: http://minecraft.wiki/w/Particles
	 * @param x The x position
	 * @param y The y position
	 * @param z The z position
	 * @param dx Usually used for the x motion
	 * @param dy Usually used for the y motion
	 * @param dz Usually used for the z motion
	 * @param speed Speed of the particles, usually between 0 and 1
	 * @param count Particle count
	 */
	public void spawnParticle(String particle, double x, double y, double z, double dx, double dy, double dz, double speed, int count){
		world.func_147487_a(particle, x, y, z, count, dx, dy, dz, speed);
	}

	public void spawnParticle(String particle, IPos pos, double dx, double dy, double dz, double speed, int count){
		this.spawnParticle(particle, pos.getX(), pos.getY(), pos.getZ(), dx, dy, dz, speed, count);
	}

	/**
	 * @param id The items name
	 * @param damage The damage value
	 * @param size The number of items in the item
	 * @return Returns the item
	 */
	public IItemStack createItem(String id, int damage, int size){
		return NpcAPI.Instance().createItem(id,damage,size);
	}

	/**
	 * @param directory The particle's texture directory. Use only forward slashes when writing a directory. Example: "customnpcs:textures/particle/bubble.png"
	 * @return Returns ScriptParticle object
	 */
	@Deprecated
	public IParticle createEntityParticle(String directory){
		return NpcAPI.Instance().createEntityParticle(directory);
	}

	/**
	 * @param key Get temp data for this key
	 * @return Returns the stored temp data
	 */
	public Object getTempData(String key){
		return tempData.get(key);
	}

	/**
	 * Tempdata gets cleared when the server restarts. All worlds share the same temp data.
	 * @param key The key for the data stored
	 * @param value The data stored
	 */
	public void setTempData(String key, Object value){
		tempData.put(key, value);
	}

	/**
	 * @param key The key thats going to be tested against the temp data
	 * @return Whether or not temp data containes the key
	 */
	public boolean hasTempData(String key){
		return tempData.containsKey(key);
	}

	/**
	 * @param key The key for the temp data to be removed
	 */
	public void removeTempData(String key){
		tempData.remove(key);
	}

	/**
	 * Removes all tempdata
	 */
	public void clearTempData(){
		tempData.clear();
	}

	public String[] getTempDataKeys() {
		return tempData.keySet().toArray(new String[0]);
	}

	/**
	 * @param key The key of the data to be returned
	 * @return Returns the stored data
	 */
	public Object getStoredData(String key){
		NBTTagCompound compound = ScriptController.Instance.compound;
		if(!compound.hasKey(key))
			return null;
		NBTBase base = compound.getTag(key);
		if(base instanceof NBTPrimitive)
			return ((NBTPrimitive)base).func_150286_g();
		return ((NBTTagString)base).func_150285_a_();
	}

	/**
	 * Stored data persists through world restart. Unlike tempdata only Strings and Numbers can be saved
	 * @param key The key for the data stored
	 * @param value The data stored. This data can be either a Number or a String. Other data is not stored
	 */
	public void setStoredData(String key, Object value){
		NBTTagCompound compound = ScriptController.Instance.compound;
		if(value instanceof Number)
			compound.setDouble(key, ((Number) value).doubleValue());
		else if(value instanceof String)
			compound.setString(key, (String)value);
		ScriptController.Instance.shouldSave = true;
	}

	/**
	 * @param key The key of the data to be checked
	 * @return Returns whether or not the stored data contains the key
	 */
	public boolean hasStoredData(String key){
		return ScriptController.Instance.compound.hasKey(key);
	}

	/**
	 * @param key The key of the data to be removed
	 */
	public void removeStoredData(String key){
		ScriptController.Instance.compound.removeTag(key);
		ScriptController.Instance.shouldSave = true;
	}

	/**
	 * Remove all stored data
	 */
	public void clearStoredData(){
		ScriptController.Instance.compound = new NBTTagCompound();
		ScriptController.Instance.shouldSave = true;
	}

	public String[] getStoredDataKeys() {
		NBTTagCompound compound = ScriptController.Instance.compound;
		if (compound != null) {
			Set keySet = compound.func_150296_c();
			List<String> list = new ArrayList<>();
			for(Object o : keySet){
				list.add((String) o);
			}
			String[] array = list.toArray(new String[list.size()]);
			return array;
		}
		return new String[0];
	}

	/**
	 * @param x Position x
	 * @param y Position y
	 * @param z Position z
	 * @param range Range of the explosion
	 * @param fire Whether or not the explosion does fire damage
	 * @param grief Whether or not the explosion does damage to blocks
	 */
	public void explode(double x, double y, double z, float range, boolean fire, boolean grief){
		world.newExplosion(null, x, y, z, range, fire, grief);
	}

	public void explode(IPos pos, float range, boolean fire, boolean grief){
		this.explode(pos.getX(),pos.getY(),pos.getZ(),range,fire,grief);
	}

	@Deprecated
	public IPlayer[] getAllServerPlayers(){
		List<EntityPlayer> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		IPlayer[] arr = new IPlayer[list.size()];
		for(int i = 0; i < list.size(); i++){
			arr[i] = (IPlayer) NpcAPI.Instance().getIEntity(list.get(i));
		}

		return arr;
	}

	@Deprecated
	public String[] getPlayerNames() {
		IPlayer[] players = getAllServerPlayers();
		String[] names = new String[players.length];
		for (int i = 0; i < names.length; ++i) names[i] = players[i].getDisplayName();
		return names;
	}

	/**
	 * @since 1.7.10c
	 * @param x Position x
	 * @param z Position z
	 * @return Returns the name of the biome
	 */
	public String getBiomeName(int x, int z){
		return world.getBiomeGenForCoords(x, z).biomeName;
	}

	public String getBiomeName(IPos pos){
		return this.getBiomeName(pos.getX(),pos.getZ());
	}

	/**
	 * Lets you spawn a server side cloned entity
	 * @param x The x position the clone will be spawned at
	 * @param y The y position the clone will be spawned at
	 * @param z The z position the clone will be spawned at
	 * @param tab The tab in which the clone is
	 * @param name Name of the cloned entity
	 * @return Returns the entity which was spawned
	 */
	public IEntity spawnClone(int x, int y, int z, int tab, String name, boolean ignoreProtection){
		NBTTagCompound compound = ServerCloneController.Instance.getCloneData(null, name, tab);
		if(compound == null)
			return null;
		Entity entity;
		if (!ignoreProtection) {
			entity = NoppesUtilServer.spawnCloneWithProtection(compound, x, y, z, world);
		} else {
			entity = NoppesUtilServer.spawnClone(compound, x, y, z, world);
		}
		return entity == null ? null : NpcAPI.Instance().getIEntity(entity);
	}

	public IEntity spawnClone(IPos pos, int tab, String name, boolean ignoreProtection) {
		return this.spawnClone(pos.getX(),pos.getY(),pos.getZ(),tab,name,ignoreProtection);
	}

	public IEntity spawnClone(int x, int y, int z, int tab, String name) {
		return this.spawnClone(x,y,z,tab,name,true);
	}

	public IEntity spawnClone(IPos pos, int tab, String name) {
		return this.spawnClone(pos.getX(),pos.getY(),pos.getZ(),tab,name);
	}

	public IScoreboard getScoreboard(){
		return new ScriptScoreboard();
	}

	/**
	 * @since 1.7.10c
	 * Expert use only
	 * @return Returns minecraft world object
	 */
	public WorldServer getMCWorld(){
		return this.world;
	}

	public int getDimensionID(){
		return world.provider.dimensionId;
	}

	public String toString() {
		return "DIM" + this.getDimensionID();
	}
}
