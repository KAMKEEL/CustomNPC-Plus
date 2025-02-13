package noppes.npcs.blocks.tiles;

import net.minecraft.block.BlockColored;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.items.ItemNpcTool;
import noppes.npcs.items.ItemStaff;

import static kamkeel.npcs.util.ColorUtil.colorTableInts;

public class TileColorable extends TileVariant {

    public int color = 0xFFFFFF;

    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        color = compound.getInteger("BrushColor");
    }

    public void writeToNBT(NBTTagCompound compound){
    	super.writeToNBT(compound);
    	compound.setInteger("BrushColor", color);
    }

    public void setColor(int color){
        this.color = color;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public void changeColor(EntityPlayer player, ItemStack stack){
        if(stack == null)
            return;

        if(stack.getItem() == null)
            return;

        if(stack.getItem() == Items.dye){
            int color = colorTableInts[BlockColored.func_150031_c(stack.getItemDamage())];
            NoppesUtilServer.consumeItemStack(1, player);
            setColor(color);
        } else if(ItemNpcTool.isPaintbrush(stack)){
            int color = colorTableInts[BlockColored.func_150031_c(stack.getItemDamage())];
            NoppesUtilServer.consumeItemStack(1, player);
            setColor(color);
        }
    }

    public static boolean doNotAllowModification(ItemStack stack){
        return stack == null || stack.getItem() == null ||
            (stack.getItem() != Items.dye &&
            !(stack.getItem() instanceof ItemNpcTool));
    }
}
