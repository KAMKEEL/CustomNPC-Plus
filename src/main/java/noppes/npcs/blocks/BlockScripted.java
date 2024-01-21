package noppes.npcs.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.constants.EnumGuiType;

import java.util.Random;

public class BlockScripted extends BlockContainer {
    public static final AxisAlignedBB AABB = AxisAlignedBB.getBoundingBox(0.001f, 0.001f, 0.001f, 0.998f, 0.998f, 0.998f);
    public static final AxisAlignedBB AABB_EMPTY = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

    public BlockScripted() {
        super(Material.rock);
        setStepSound(soundTypeStone);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileScripted();
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        if (tile != null && tile.isPassible)
            return AABB_EMPTY;
        return AABB;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        if (tile != null && tile.isPassible)
            return AABB_EMPTY;
        return AABB;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote)
            return true;
        ItemStack currentItem = player.inventory.getCurrentItem();
        if (currentItem != null && (currentItem.getItem() == CustomItems.wand || currentItem.getItem() == CustomItems.scripter)) {
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.ScriptBlock, null, x, y, z);
            return true;
        }
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        return !EventHooks.onScriptBlockInteract(tile, player, side, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
        if (entity instanceof EntityPlayer && !world.isRemote) {
            NoppesUtilServer.sendOpenGui((EntityPlayer) entity, EnumGuiType.ScriptBlock, null, x, y, z);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entityIn) {
        if (world.isRemote)
            return;
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        EventHooks.onScriptBlockCollide(tile, entityIn);
    }

    @Override
    public void fillWithRain(World world, int x, int y, int z) {
        if (world.isRemote)
            return;
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        EventHooks.onScriptBlockRainFill(tile);
    }


    @Override
    public void onFallenUpon(World world, int x, int y, int z, Entity entity, float fallDistance) {
        if (world.isRemote)
            return;
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        fallDistance = EventHooks.onScriptBlockFallenUpon(tile, entity, fallDistance);
        super.onFallenUpon(world, x, y, z, entity, fallDistance);
    }

    @Override
    public boolean canRenderInPass(int pass) {
        return false;
    }

    @Override
    public int getRenderType() {
        return 0;
    }
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote)
            return;
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        EventHooks.onScriptBlockClicked(tile, player);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        if (!world.isRemote) {
            TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
            EventHooks.onScriptBlockBreak(tile);
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (!world.isRemote) {
            TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
            if (EventHooks.onScriptBlockHarvest(tile, player))
                return false;
        }
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public Item getItemDropped(int i, Random rand, int fortune) {
        return null;
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
        if (!world.isRemote) {
            TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
            if (EventHooks.onScriptBlockExploded(tile))
                return;
        }
        super.onBlockExploded(world, x, y, z, explosion);
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }


    @Override
    public int isProvidingWeakPower(IBlockAccess worldIn, int x, int y, int z, int side) {
        return this.isProvidingStrongPower(worldIn, x, y, z, side);
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        return ((TileScripted) world.getTileEntity(x, y, z)).activePowering;
    }

    @Override
    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
        return ((TileScripted) world.getTileEntity(x, y, z)).isLadder;
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        return ((TileScripted) world.getTileEntity(x, y, z)).blockHardness;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        if (tile == null)
            return 0;
        return tile.lightValue;
    }

    @Override
    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
        return ((TileScripted) world.getTileEntity(x, y, z)).blockResistance;
    }

    @Override
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        TileScripted tile = (TileScripted) world.getTileEntity(x, y, z);
        EventHooks.onScriptBlockNeighborChanged(tile, tileX, tileY, tileZ);

        int power = 0;
        for (EnumFacing enumfacing : EnumFacing.values()) {
            int p = world.isBlockProvidingPowerTo(x + enumfacing.getFrontOffsetX(),
                    y + enumfacing.getFrontOffsetZ(), z + enumfacing.getFrontOffsetZ(), enumfacing.ordinal());
            if (p > power)
                power = p;
        }
        if (tile.prevPower != power && tile.powering <= 0) {
            tile.newPower = power;
        }
    }
}
