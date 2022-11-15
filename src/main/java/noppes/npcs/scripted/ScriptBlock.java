//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.api.*;
import noppes.npcs.util.LRUHashMap;

public class ScriptBlock implements IBlock {
    private static final Map<String, IBlock> blockCache = new LRUHashMap(400);
    protected IWorld world;
    protected Block block;
    protected BlockPos pos;
    protected IPos bPos;
    protected ITileEntity tile;

    public ScriptBlock(World world, Block block, BlockPos pos) {
        this.world = NpcAPI.Instance().getIWorld(world);
        this.block = block;
        this.pos = pos;
        this.bPos = NpcAPI.Instance().getIPos(pos);
        this.tile = NpcAPI.Instance().getITileEntity(world.getTileEntity(pos.getX(),pos.getY(), pos.getZ()));
    }

    public IPos getPosition() {
        return this.bPos;
    }

    public IPos getPos() {
        return this.bPos;
    }

    public boolean setPosition(IPos pos, IWorld world) {
        if (pos == null || world == null || !world.setBlock(pos, this))
            return false;

        this.world.removeBlock(this.getPos());
        this.world = world;

        this.block = world.getBlock(pos).getMCBlock();
        this.pos = pos.getMCPos();
        this.bPos = pos;
        this.tile = NpcAPI.Instance().getITileEntity(world.getMCWorld().getTileEntity(pos.getX(),pos.getY(),pos.getZ()));
        return true;
    }

    public boolean setPosition(IPos pos) {
        return this.setPosition(pos,world);
    }

    public boolean setPos(IPos pos, IWorld world) {
        return this.setPosition(pos,world);
    }

    public boolean setPos(IPos pos) {
        return this.setPosition(pos);
    }

    public boolean setPosition(int x, int y, int z, IWorld world) {
        return this.setPos(NpcAPI.Instance().getIPos(x,y,z),world);
    }

    public boolean setPosition(int x, int y, int z) {
        return this.setPos(NpcAPI.Instance().getIPos(x,y,z));
    }

    public boolean setPos(int x, int y, int z, IWorld world) {
        return this.setPosition(x,y,z, world);
    }

    public boolean setPos(int x, int y, int z) {
        return this.setPosition(x,y,z);
    }

    public int getX() {
        return this.pos.getX();
    }

    public int getY() {
        return this.pos.getY();
    }

    public int getZ() {
        return this.pos.getZ();
    }

    public void remove() {
        this.world.getMCWorld().setBlockToAir(getX(), getY(), getZ());
    }

    public boolean isAir() {
        return this.block.isAir(this.world.getMCWorld(), getX(), getY(), getZ());
    }

    public IBlock setBlock(String blockName) {
        Block block = (Block)Block.blockRegistry.getObject(new ResourceLocation(blockName));
        if(block == null) {
            return this;
        } else {
            this.world.getMCWorld().setBlock(getX(),getY(),getZ(),block);
            return NpcAPI.Instance().getIBlock(world, getX(),getY(),getZ());
        }
    }

    public IBlock setBlock(IBlock block) {
        this.world.getMCWorld().setBlock(getX(),getY(),getZ(),block.getMCBlock());
        return NpcAPI.Instance().getIBlock(world, getX(),getY(),getZ());
    }

    public boolean isContainer() {
        return this.tile != null && this.tile.getMCTileEntity() != null && this.tile.getMCTileEntity() instanceof IInventory && ((IInventory) this.tile.getMCTileEntity()).getSizeInventory() > 0;
    }

    public IContainer getContainer() {
        if(!this.isContainer()) {
            throw new CustomNPCsException("This block is not a container", new Object[0]);
        } else {
            return NpcAPI.Instance().getIContainer((IInventory)this.tile.getMCTileEntity());
        }
    }

    public String getName() {
        return Block.blockRegistry.getNameForObject(this.block) + "";
    }

    public String getDisplayName() {
        return this.tile == null || this.tile.getMCTileEntity() == null ? this.getName():this.tile.getMCTileEntity().blockType.getItemIconName();
    }

    public IWorld getWorld() {
        return this.world;
    }

    public Block getMCBlock() {
        return this.block;
    }

    public static void clearCache() {
        blockCache.clear();
    }

    public boolean hasTileEntity() {
        return this.tile != null;
    }

    public ITileEntity getTileEntity() {
        return this.tile;
    }

    public void setTileEntity(ITileEntity tileEntity){
        world.setTileEntity(pos.getX(),pos.getY(), pos.getZ(),tileEntity);
        this.tile = tileEntity;
    }

    public TileEntity getMCTileEntity() {
        if (this.tile == null)
            return null;

        return this.tile.getMCTileEntity();
    }

    public INbt getTileEntityNBT() {
        if (this.tile == null || this.tile.getMCTileEntity() == null)
            return null;

        NBTTagCompound compound = new NBTTagCompound();
        this.tile.getMCTileEntity().writeToNBT(compound);
        return NpcAPI.Instance().getINbt(compound);
    }

    public boolean canCollide(double maxVolume) {
        AxisAlignedBB alignedBB = block.getCollisionBoundingBoxFromPool(world.getMCWorld(),getX(),getY(),getZ());
        if (alignedBB == null) {
            return false;
        }
        double xEdge = alignedBB.maxX - alignedBB.minX;
        double yEdge = alignedBB.maxY - alignedBB.minY;
        double zEdge = alignedBB.maxZ - alignedBB.minZ;
        return Math.abs(xEdge * yEdge * zEdge) > maxVolume;
    }

    public boolean canCollide() {
        return canCollide(0);
    }

    public void setBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.block.setBlockBounds(minX,minY,minZ,maxX,maxY,maxZ);
    }

    public double getBlockBoundsMinX() {
        return this.block.getBlockBoundsMinX();
    }

    public double getBlockBoundsMinY() {
        return this.block.getBlockBoundsMinY();
    }

    public double getBlockBoundsMinZ() {
        return this.block.getBlockBoundsMinZ();
    }

    public double getBlockBoundsMaxX() {
        return this.block.getBlockBoundsMaxX();
    }

    public double getBlockBoundsMaxY() {
        return this.block.getBlockBoundsMaxY();
    }

    public double getBlockBoundsMaxZ() {
        return this.block.getBlockBoundsMaxZ();
    }

    public String toString() {
        return this.getName() + " @" + this.getPos() + (world == null ? "" : " in DIM" + world.getDimensionID());
    }
}
