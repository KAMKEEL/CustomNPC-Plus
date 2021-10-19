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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.scripted.interfaces.*;
import noppes.npcs.util.LRUHashMap;

public class ScriptBlock implements IBlock {
    private static final Map<String, ScriptBlock> blockCache = new LRUHashMap(400);
    protected final IWorld world;
    protected final Block block;
    protected final BlockPos pos;
    protected final ScriptBlockPos bPos;
    protected TileEntity tile;

    public ScriptBlock(World world, Block block, BlockPos pos) {
        this.world = NpcAPI.Instance().getIWorld((WorldServer)world);
        this.block = block;
        this.pos = pos;
        this.bPos = new ScriptBlockPos(pos);
    }

    public IPos getPos() {
        return this.bPos;
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

    public ScriptBlock setBlock(String name) {
        Block block = (Block)Block.blockRegistry.getObject(new ResourceLocation(name));
        if(block == null) {
            return this;
        } else {
            this.world.getMCWorld().setBlock(getX(),getY(),getZ(),block);
            return new ScriptBlock(this.world.getMCWorld(), block, this.pos);
        }
    }

    public ScriptBlock setBlock(IBlock block) {
        this.world.getMCWorld().setBlock(getX(),getY(),getZ(),block.getMCBlock());
        return new ScriptBlock(this.world.getMCWorld(), block.getMCBlock(), this.pos);
    }

    public boolean isContainer() {
        return this.tile != null && this.tile instanceof IInventory?((IInventory)this.tile).getSizeInventory() > 0:false;
    }

    public IContainer getContainer() {
        if(!this.isContainer()) {
            throw new CustomNPCsException("This block is not a container", new Object[0]);
        } else {
            return NpcAPI.Instance().getIContainer((IInventory)this.tile);
        }
    }

    public String getName() {
        return Block.blockRegistry.getNameForObject(this.block) + "";
    }

    public String getDisplayName() {
        return this.tile == null?this.getName():this.tile.blockType.getItemIconName();
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

    public TileEntity getMCTileEntity() {
        return this.tile;
    }

    public INbt getTileEntityNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        this.tile.writeToNBT(compound);
        return NpcAPI.Instance().getINbt(compound);
    }
}
