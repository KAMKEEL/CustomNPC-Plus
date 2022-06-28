package noppes.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.scripted.item.ScriptCustomItem;

public class EntityItemScripted extends EntityItem {
    private final ScriptCustomItem customItem;

    public EntityItemScripted(World p_i1709_1_, double p_i1709_2_, double p_i1709_4_, double p_i1709_6_, ItemStack p_i1710_8_) {
        super(p_i1709_1_, p_i1709_2_, p_i1709_4_, p_i1709_6_, p_i1710_8_);
        this.customItem = ItemScripted.GetWrapper(this.getEntityItem());
        float entitySize = (float)(Math.sqrt(Math.pow(customItem.scaleX,2) + Math.pow(customItem.scaleZ,2)));
        this.width = 0.4F * entitySize;
        this.height = 0.4F * customItem.scaleY;
        this.delayBeforeCanPickup = 40;
    }

    @Override
    public boolean canBePushed() {
        if (this.delayBeforeCanPickup > 0) {
            return false;
        }
        return super.canBePushed();
    }
}
