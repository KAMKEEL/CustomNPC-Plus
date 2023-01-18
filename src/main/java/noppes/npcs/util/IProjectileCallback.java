package noppes.npcs.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import noppes.npcs.entity.EntityProjectile;

public interface IProjectileCallback {

	boolean onImpact(EntityProjectile entityProjectile,
			EntityLivingBase entity, ItemStack itemstack);

}
