package noppes.npcs.items;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.item.ScriptLinkedItem;
import org.lwjgl.opengl.GL11;

public class ItemLinked extends ItemCustomizable {

    public ItemLinked() {
        maxStackSize = 1;
        CustomNpcs.proxy.registerItem(this);
        setHasSubtypes(true);
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLivingBase, ItemStack stack){
        if (entityLivingBase.worldObj.isRemote) {
            return false;
        }

        //TODO: re-implement item attack swing hook
//        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
//        ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent((ScriptLinkedItem) istack, NpcAPI.Instance().getIEntity(entityLivingBase), 2, null);
//        return EventHooks.onScriptItemAttack((ScriptLinkedItem) istack, eve);
        return true;
    }

    public int getMaxItemUseDuration(ItemStack stack){
        return (new ScriptLinkedItem(stack)).getMaxItemUseDuration();
    }

    //TODO: Use this in existing item scripted rendering. Refactor the renderer to use the new abstract ScriptCustomizableItem class
    public static ScriptLinkedItem GetWrapper(ItemStack stack) {
        return new ScriptLinkedItem(stack);
    }

    public boolean showDurabilityBar(ItemStack stack) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof ScriptLinkedItem && (new ScriptLinkedItem(stack)).itemDisplay.durabilityShow;
    }

    public double getDurabilityForDisplay(ItemStack stack) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof ScriptLinkedItem ? 1.0D - (new ScriptLinkedItem(stack)).getDurabilityValue() : 1.0D;
    }

    public int getItemStackLimit(ItemStack stack) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof ScriptLinkedItem ? (new ScriptLinkedItem(stack)).getMaxStackSize() : super.getItemStackLimit(stack);
    }

    public boolean isItemTool(ItemStack stack)
    {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof ScriptLinkedItem ? (new ScriptLinkedItem(stack)).isTool() : super.isItemTool(stack);
    }

    public float getDigSpeed(ItemStack stack, Block block, int metadata)
    {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof ScriptLinkedItem ? (new ScriptLinkedItem(stack)).getDigSpeed() : super.getDigSpeed(stack, block, metadata);
    }

    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity)
    {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);

        if((new ScriptLinkedItem(stack)).getArmorType() == -1)
            return true;

        return istack instanceof ScriptLinkedItem ? armorType == (new ScriptLinkedItem(stack)).getArmorType() : super.isValidArmor(stack, armorType, entity);
    }

    public int getItemEnchantability(ItemStack stack)
    {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
        return istack instanceof ScriptLinkedItem ? (new ScriptLinkedItem(stack)).getEnchantability() : super.getItemEnchantability(stack);
    }

    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);

        //TODO: re-implement item attack hook
//        ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent( (ScriptLinkedItem) istack, NpcAPI.Instance().getIEntity(attacker), 1, NpcAPI.Instance().getIEntity(target));
//        return EventHooks.onScriptItemAttack((ScriptLinkedItem) istack, eve);
        return true;
    }

    public void renderOffset(ScriptLinkedItem scriptCustomItem) {
        GL11.glTranslatef(0.135F * scriptCustomItem.itemDisplay.scaleX, 0.2F * scriptCustomItem.itemDisplay.scaleY, 0.07F * scriptCustomItem.itemDisplay.scaleZ);
    }
}
