package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.data.LinkedItem;

public class ScriptLinkedItem extends ScriptCustomizableItem {
    private final LinkedItem linkedItem;

    public ScriptLinkedItem(ItemStack item) {
        super(item);
        String linkedItemName = item.getTagCompound().getString(LinkedItem.LINKED_NBT_TAG);
        this.linkedItem = LinkedItemController.Instance().get(linkedItemName);

        this.itemDisplay.texture = null;
        this.itemDisplay.translateX = null;
        this.itemDisplay.translateY = null;
        this.itemDisplay.translateZ = null;
        this.itemDisplay.itemColor = null;
        this.itemDisplay.scaleX = null;
        this.itemDisplay.scaleY = null;
        this.itemDisplay.scaleZ = null;
        this.itemDisplay.rotationX = null;
        this.itemDisplay.rotationY = null;
        this.itemDisplay.rotationZ = null;
        this.itemDisplay.rotationXRate = null;
        this.itemDisplay.rotationYRate = null;
        this.itemDisplay.rotationZRate = null;
        this.itemDisplay.durabilityShow = null;
        this.itemDisplay.durabilityColor = null;
    }

    public LinkedItem getLinkedItem() {
        return this.linkedItem;
    }

    @Override
    public int getMaxStackSize() {
        return this.linkedItem.stackSize;
    }

    @Override
    public int getArmorType() {
        return this.linkedItem.armorType;
    }

    @Override
    public boolean isTool() {
        return this.linkedItem.isTool;
    }

    @Override
    public boolean isNormalItem() {
        return this.linkedItem.isNormalItem;
    }

    @Override
    public int getDigSpeed() {
        return this.linkedItem.digSpeed;
    }

    @Override
    public double getDurabilityValue() {
        return this.linkedItem.durabilityValue;
    }

    @Override
    public int getMaxItemUseDuration() {
        return this.linkedItem.maxItemUseDuration;
    }

    @Override
    public int getItemUseAction() {
        return this.linkedItem.itemUseAction;
    }

    @Override
    public int getEnchantability() {
        return this.linkedItem.enchantability;
    }

    public String getTexture() {
        return this.itemDisplay.texture == null ? this.linkedItem.display.texture : this.itemDisplay.texture;
    }

    public Boolean getDurabilityShow() {
        return this.itemDisplay.durabilityShow != null ? this.itemDisplay.durabilityShow : this.linkedItem.display.durabilityShow;
    }

    public Integer getDurabilityColor() {
        return this.itemDisplay.durabilityColor != null ? this.itemDisplay.durabilityColor : this.linkedItem.display.durabilityColor;
    }

    public Integer getColor() {
        return this.itemDisplay.itemColor != null ? this.itemDisplay.itemColor : this.linkedItem.display.itemColor;
    }

    public Float getRotationX() {
        return this.itemDisplay.rotationX != null ? this.itemDisplay.rotationX : this.linkedItem.display.rotationX;
    }

    public Float getRotationY() {
        return this.itemDisplay.rotationY != null ? this.itemDisplay.rotationY : this.linkedItem.display.rotationY;
    }

    public Float getRotationZ() {
        return this.itemDisplay.rotationZ != null ? this.itemDisplay.rotationZ : this.linkedItem.display.rotationZ;
    }

    public Float getRotationXRate() {
        return this.itemDisplay.rotationXRate != null ? this.itemDisplay.rotationXRate : this.linkedItem.display.rotationXRate;
    }

    public Float getRotationYRate() {
        return this.itemDisplay.rotationYRate != null ? this.itemDisplay.rotationYRate : this.linkedItem.display.rotationYRate;
    }

    public Float getRotationZRate() {
        return this.itemDisplay.rotationZRate != null ? this.itemDisplay.rotationZRate : this.linkedItem.display.rotationZRate;
    }

    public Float getScaleX() {
        return this.itemDisplay.scaleX != null ? this.itemDisplay.scaleX : this.linkedItem.display.scaleX;
    }

    public Float getScaleY() {
        return this.itemDisplay.scaleY != null ? this.itemDisplay.scaleY : this.linkedItem.display.scaleY;
    }

    public Float getScaleZ() {
        return this.itemDisplay.scaleZ != null ? this.itemDisplay.scaleZ : this.linkedItem.display.scaleZ;
    }

    public Float getTranslateX() {
        return this.itemDisplay.translateX != null ? this.itemDisplay.translateX : this.linkedItem.display.translateX;
    }

    public Float getTranslateY() {
        return this.itemDisplay.translateY != null ? this.itemDisplay.translateY : this.linkedItem.display.translateY;
    }

    public Float getTranslateZ() {
        return this.itemDisplay.translateZ != null ? this.itemDisplay.translateZ : this.linkedItem.display.translateZ;
    }
}
