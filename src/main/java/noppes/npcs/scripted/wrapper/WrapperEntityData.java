//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.wrapper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.LogWriter;
import noppes.npcs.scripted.*;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityProjectile;

public class WrapperEntityData{
    public IEntity base;
    private static final ResourceLocation key = new ResourceLocation("customnpcs", "entitydata");

    public WrapperEntityData(IEntity base) {
        this.base = base;
    }

    public static IEntity get(Entity entity) {
        if (entity == null) {
            return null;
        } else {
            WrapperEntityData data = new WrapperEntityData(new ScriptEntity(entity));
            if (data == null) {
                return getData(entity).base;
            } else {
                return data.base;
            }
        }
    }

    private static WrapperEntityData getData(Entity entity) {
        if (entity != null && entity.worldObj != null && !entity.worldObj.isRemote) {
            if (entity instanceof EntityPlayerMP) {
                return new WrapperEntityData(new ScriptPlayer((EntityPlayerMP)entity));
            } else if (PixelmonHelper.isPixelmon(entity)) {
                return new WrapperEntityData(new ScriptPixelmon((EntityTameable)entity));
            } else if (entity instanceof EntityAnimal) {
                return new WrapperEntityData(new ScriptAnimal((EntityAnimal)entity));
            } else if (entity instanceof EntityMob) {
                return new WrapperEntityData(new ScriptMonster((EntityMob)entity));
            } else if (entity instanceof EntityLiving) {
                return new WrapperEntityData(new ScriptLiving((EntityLiving)entity));
            } else if (entity instanceof EntityLivingBase) {
                return new WrapperEntityData(new ScriptLivingBase((EntityLivingBase)entity));
            }
        }
        return null;
    }
}
