package noppes.npcs.ai.target;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.MathHelper;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Collections;
import java.util.List;

public class EntityAIClosestTarget extends EntityAITarget {
    private final Class targetClass;
    private final int targetChance;

    /**
     * Instance of EntityAINearestAttackableTargetSorter.
     */
    private final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
    private final IEntitySelector field_82643_g;
    private EntityLivingBase targetEntity;

    public EntityAIClosestTarget(EntityCreature par1EntityCreature, Class par2Class, int par3, boolean par4) {
        this(par1EntityCreature, par2Class, par3, par4, false);
    }

    public EntityAIClosestTarget(EntityCreature par1EntityCreature, Class par2Class, int par3, boolean par4, boolean par5) {
        this(par1EntityCreature, par2Class, par3, par4, par5, null);
    }

    public EntityAIClosestTarget(EntityCreature par1EntityCreature, Class par2Class, int par3, boolean par4, boolean par5, IEntitySelector par6IEntitySelector) {
        super(par1EntityCreature, par4, par5);
        this.targetClass = par2Class;
        this.targetChance = par3;
        this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(par1EntityCreature);
        this.setMutexBits(1);
        this.field_82643_g = par6IEntitySelector;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
            return false;
        } else {
            taskOwner.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(((EntityNPCInterface) taskOwner).stats.aggroRange);
            double d0 = this.getTargetDistance();
            List list = this.taskOwner.worldObj.selectEntitiesWithinAABB(this.targetClass, this.taskOwner.boundingBox.expand(d0, MathHelper.ceiling_double_int(d0 / 2.0D), d0), this.field_82643_g);
            Collections.sort(list, this.theNearestAttackableTargetSorter);
            taskOwner.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(ConfigMain.NpcNavRange);
            if (list.isEmpty()) {
                return false;
            } else {
                this.targetEntity = (EntityLivingBase) list.get(0);
                return true;
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.targetEntity);
        if (this.targetEntity instanceof EntityMob) {
            if (((EntityMob) this.targetEntity).getAttackTarget() == null) {
                ((EntityMob) this.targetEntity).setAttackTarget(this.taskOwner);
                ((EntityMob) this.targetEntity).setTarget(this.taskOwner);
            }
        }
        super.startExecuting();
    }
}
