package noppes.npcs.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.blocks.tiles.TileLamp;
import noppes.npcs.blocks.tiles.TileVariant;

public class BlockLantern extends BlockLightable {

    public BlockLantern(boolean lit) {
        super(Blocks.iron_block, lit);
        setBlockBounds(0.3f, 0, 0.3f, 0.7f, 0.6f, 0.7f);
    }

    @Override
    public int maxRotation() {
        return 8;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntity tileentity = world.getTileEntity(x, y, z);
        if (!(tileentity instanceof TileVariant)) {
            super.setBlockBoundsBasedOnState(world, x, y, z);
            return;
        }
        TileVariant tile = (TileVariant) tileentity;
        if (tile.variant == 2) {
            float xOffset = 0;
            float yOffset = 0;
            if (tile.rotation == 0)
                yOffset = 0.2f;
            else if (tile.rotation == 4)
                yOffset = -0.2f;
            else if (tile.rotation == 6)
                xOffset = 0.2f;
            else if (tile.rotation == 2)
                xOffset = -0.2f;

            setBlockBounds(0.3f + xOffset, 0.2f, 0.3f + yOffset, 0.7f + xOffset, 0.7f, 0.7f + yOffset);
        } else
            setBlockBounds(0.3f, 0, 0.3f, 0.7f, 0.6f, 0.7f);

    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int meta) {
        return side;
    }

    @Override
    public void onPostBlockPlaced(World world, int x, int y, int z, int meta) {
        TileLamp tile = (TileLamp) world.getTileEntity(x, y, z);
        if (meta == 1)
            tile.variant = 0;
        else if (meta == 0)
            tile.variant = 1;
        else {
            tile.variant = 2;
            if (meta == 2)
                tile.rotation = 0;
            else if (meta == 3)
                tile.rotation = 4;
            else if (meta == 4)
                tile.rotation = 6;
            else if (meta == 5)
                tile.rotation = 2;
        }
        world.setBlockMetadataWithNotify(x, y, z, 0, 4);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister) {
        this.blockIcon = par1IconRegister.registerIcon(this.getTextureName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int meta) {
        return this.blockIcon;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileLamp();
    }

    @Override
    public Block unlitBlock() {
        return CustomItems.lantern_unlit;
    }

    @Override
    public Block litBlock() {
        return CustomItems.lantern;
    }
}
