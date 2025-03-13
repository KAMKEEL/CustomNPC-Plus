package noppes.npcs.blocks;

import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.data.gui.GuiOpenBookPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.blocks.tiles.TileBook;

public class BlockBook extends BlockRotated {

    public BlockBook() {
        super(Blocks.planks);
        setBlockBounds(0, 0, 0, 1, 0.2f, 1);
    }

    @Override
    public boolean onBlockActivated(World par1World, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (par1World.isRemote)
            return true;
        TileEntity tile = par1World.getTileEntity(i, j, k);
        if (!(tile instanceof TileBook))
            return false;
        ItemStack currentItem = player.inventory.getCurrentItem();
        if (currentItem != null && currentItem.getItem() == CustomItems.wand && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BOOK)) {
            ((TileBook) tile).book.func_150996_a(Items.writable_book);
        }
        PacketHandler.Instance.sendToPlayer(new GuiOpenBookPacket(i, j, k, ((TileBook) tile).book.writeToNBT(new NBTTagCompound())), (EntityPlayerMP) player);
        return true;
    }

    @Override
    public String getUnlocalizedName() {
        return "item.book";
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileBook();
    }
}
