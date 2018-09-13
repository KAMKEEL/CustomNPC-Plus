package noppes.npcs.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import noppes.npcs.entity.EntityProjectile;

public interface IProjectileCallback {

	boolean onImpact(EntityProjectile entityProjectile,
			EntityLivingBase entity, ItemStack itemstack);

}
