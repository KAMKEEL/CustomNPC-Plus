package noppes.npcs.blocks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.blocks.tiles.TileChair;
import noppes.npcs.blocks.tiles.TileVariant;
import noppes.npcs.entity.EntityChairMount;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.items.ItemNpcTool;

import java.util.List;

public class BlockChair extends BlockRotated {

    public BlockChair() {
        super(Blocks.planks);
        setBlockBounds(0.1f, 0, 0.1f, 0.9f, 1, 0.9f);
    }

    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
        super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLivingBase, par6ItemStack);
        par1World.setBlockMetadataWithNotify(par2, par3, par4, par6ItemStack.getItemDamage(), 2);
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileChair && ((TileChair) te).isPushed()) {
            // Use the rotation to set a vertical slab hitbox on the pushed side.
            int rotation = ((TileVariant) te).rotation;
            switch (rotation) {
                case 0: // North: slab from z to z+0.3.
                    return AxisAlignedBB.getBoundingBox(x + 0.1, y, z, x + 0.9, y + 1, z + 0.3);
                case 1: // East: slab from x+0.7 to x+1.
                    return AxisAlignedBB.getBoundingBox(x + 0.7, y, z + 0.1, x + 1.0, y + 1, z + 0.9);
                case 2: // South: slab from z+0.7 to z+1.
                    return AxisAlignedBB.getBoundingBox(x + 0.1, y, z + 0.7, x + 0.9, y + 1, z + 1.0);
                case 3: // West: slab from x to x+0.3.
                    return AxisAlignedBB.getBoundingBox(x, y, z + 0.1, x + 0.3, y + 1, z + 0.9);
                default:
                    return AxisAlignedBB.getBoundingBox(x + 0.1, y, z + 0.1, x + 0.9, y + 1, z + 0.9);
            }
        }
        // Default collision box (half-height) when not pushed.
        return AxisAlignedBB.getBoundingBox(x + 0.1, y, z + 0.1, x + 0.9, y + 0.5, z + 0.9);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntity tileentity = world.getTileEntity(x, y, z);
        if (!(tileentity instanceof TileChair)) {
            super.setBlockBoundsBasedOnState(world, x, y, z);
            return;
        }
        TileChair tile = (TileChair) tileentity;
        if (tile.isPushed()) {
            // Pushed state: set a thicker vertical slab on the pushed side.
            switch (tile.rotation) {
                case 0: // North.
                    setBlockBounds(0.1f, 0, 0, 0.9f, 1.0f, 0.3f);
                    break;
                case 1: // East.
                    setBlockBounds(0.7f, 0, 0.1f, 1.0f, 1.0f, 0.9f);
                    break;
                case 2: // South.
                    setBlockBounds(0.1f, 0, 0.7f, 0.9f, 1.0f, 1.0f);
                    break;
                case 3: // West.
                    setBlockBounds(0, 0, 0.1f, 0.3f, 1.0f, 0.9f);
                    break;
                default:
                    setBlockBounds(0.1f, 0, 0.1f, 0.9f, 1.0f, 0.9f);
                    break;
            }
        } else {
            // Not pushed: default chair bounds.
            setBlockBounds(0.1f, 0, 0.1f, 0.9f, 1.0f, 0.9f);
        }
    }


    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
        par3List.add(new ItemStack(par1, 1, 2));
        par3List.add(new ItemStack(par1, 1, 3));
        par3List.add(new ItemStack(par1, 1, 4));
        par3List.add(new ItemStack(par1, 1, 5));
    }

    @Override
    public int damageDropped(int par1) {
        return par1;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileChair();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        ItemStack item = player.inventory.getCurrentItem();
        if (item != null && item.getItem() != null && item.getItem() instanceof ItemNpcTool)
            return false;

        return MountBlock(world, x, y, z, player);
    }

    public static boolean MountBlock(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote)
            return true;

        // Do not allow mounting if the player is holding the NpcTool.
        if (player.getCurrentEquippedItem() != null &&
            player.getCurrentEquippedItem().getItem() instanceof noppes.npcs.items.ItemNpcTool) {
            return false;
        }

        // Check if the chair is pushed.
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileChair && ((TileChair) te).isPushed()) {
            return false;
        }

        // Existing check to prevent mounting if an entity is already there.
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class,
            AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
        for (Entity entity : list) {
            if (entity instanceof EntityChairMount || entity instanceof EntityCustomNpc)
                return false;
        }
        EntityChairMount mount = new EntityChairMount(world);
        mount.setPosition(x + 0.5f, y, z + 0.5f);
        world.spawnEntityInWorld(mount);
        player.mountEntity(mount);
        return true;
    }
}
