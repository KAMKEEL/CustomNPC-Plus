package kamkeel.npcs.wrapper.platform;

import kamkeel.npcs.platform.entity.IStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.wrapper.nbt.NBTWrapper;

/**
 * MC 1.7.10 implementation of {@link IStack}.
 * Wraps a raw {@link ItemStack} instance.
 */
public class StackWrapper implements IStack {

    private final ItemStack stack;

    public StackWrapper(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public String getItemId() {
        if (stack == null || stack.getItem() == null) return "minecraft:air";
        return Item.itemRegistry.getNameForObject(stack.getItem());
    }

    @Override
    public int getCount() {
        return stack != null ? stack.stackSize : 0;
    }

    @Override
    public void setCount(int count) {
        if (stack != null) stack.stackSize = count;
    }

    @Override
    public int getDamage() {
        return stack != null ? stack.getItemDamage() : 0;
    }

    @Override
    public void setDamage(int dmg) {
        if (stack != null) stack.setItemDamage(dmg);
    }

    @Override
    public INBTCompound getTag() {
        if (stack == null || stack.stackTagCompound == null) return null;
        return new NBTWrapper(stack.stackTagCompound);
    }

    @Override
    public void setTag(INBTCompound tag) {
        if (stack != null) {
            stack.stackTagCompound = tag != null ? ((NBTWrapper) tag).getMCTag() : null;
        }
    }

    @Override
    public boolean isEmpty() {
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }

    @Override
    public IStack copy() {
        if (stack == null) return new StackWrapper(null);
        return new StackWrapper(stack.copy());
    }

    @Override
    public Object getHandle() {
        return stack;
    }
}
