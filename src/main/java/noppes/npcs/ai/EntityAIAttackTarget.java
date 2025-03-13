package noppes.npcs.ai;

import kamkeel.npcs.addon.DBCAddon;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIAttackTarget extends EntityAIBase {
    private final World world;
    private final EntityNPCInterface npc;
    private EntityLivingBase entityTarget;

    /**
     * An amount of decrementing ticks that allows the entity to attack once the tick reaches 0.
     */
    private int attackTick;

    /**
     * The PathEntity of our entity.
     */
    private PathEntity entityPathEntity;
    private int field_75445_i;
    private boolean navOverride = false;

    public EntityAIAttackTarget(EntityNPCInterface par1EntityLiving) {
        this.attackTick = 0;
        this.npc = par1EntityLiving;
        this.world = par1EntityLiving.worldObj;
        this.setMutexBits(this.navOverride ? AiMutex.PATHING : AiMutex.LOOK + AiMutex.PASSIVE);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.npc.getAttackTarget();

        if (entitylivingbase == null) {
            return false;
        } else if (!entitylivingbase.isEntityAlive()) {
            return false;
        } else if (this.npc.inventory.getProjectile() != null && this.npc.ais.useRangeMelee == 0) {
            return false;
        }

        double var2 = this.npc.getDistanceSq(entitylivingbase.posX, entitylivingbase.boundingBox.minY, entitylivingbase.posZ);
        double var3 = this.npc.ais.distanceToMelee * this.npc.ais.distanceToMelee;

        if (this.npc.ais.useRangeMelee == 1 && var2 > var3) {
            return false;
        } else {
            this.entityTarget = entitylivingbase;
            this.npc.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(this.npc.stats.aggroRange);
            this.entityPathEntity = this.npc.getNavigator().getPathToEntityLiving(entitylivingbase);
            this.npc.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(ConfigMain.NpcNavRange);
            return this.entityPathEntity != null;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        this.entityTarget = this.npc.getAttackTarget();

        if (entityTarget == null || !entityTarget.isEntityAlive())
            return false;

        if (entityTarget instanceof EntityPlayer && DBCAddon.instance.isKO(npc, (EntityPlayer) this.entityTarget))
            return false;

        if (npc.getDistanceToEntity(entityTarget) > npc.stats.aggroRange)
            return false;
        if (this.npc.ais.useRangeMelee == 1 && npc.getDistanceSqToEntity(entityTarget) > (this.npc.ais.distanceToMelee * this.npc.ais.distanceToMelee))
            return false;

        return this.npc.isWithinHomeDistance(MathHelper.floor_double(entityTarget.posX), MathHelper.floor_double(entityTarget.posY), MathHelper.floor_double(entityTarget.posZ));
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        if (!navOverride)
            this.npc.getNavigator().setPath(this.entityPathEntity, 1.3D);
        this.field_75445_i = 0;
        if (this.npc.getRangedTask() != null && this.npc.ais.useRangeMelee == 2) {
            this.npc.getRangedTask().navOverride(true);
        }
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.entityPathEntity = null;
        this.entityTarget = null;
        this.npc.setAttackTarget(null);
        this.npc.getNavigator().clearPathEntity();
        if (this.npc.getRangedTask() != null && this.npc.ais.useRangeMelee == 2) {
            this.npc.getRangedTask().navOverride(false);
        }
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        this.npc.getLookHelper().setLookPositionWithEntity(this.entityTarget, 30.0F, 30.0F);


        if (!navOverride && --this.field_75445_i <= 0) {
            this.field_75445_i = 4 + this.npc.getRNG().nextInt(7);
            this.npc.getNavigator().tryMoveToEntityLiving(this.entityTarget, 1.3f);
        }
        this.attackTick = Math.max(this.attackTick - 1, 0);

        double distance = this.npc.getDistanceSq(this.entityTarget.posX, this.entityTarget.boundingBox.minY, this.entityTarget.posZ);
        double range = npc.stats.attackRange * npc.stats.attackRange + entityTarget.width;
        double minRange = this.npc.width * 2.0F * this.npc.width * 2.0F + entityTarget.width;
        if (minRange > range)
            range = minRange;
        if (distance <= range && (npc.canSee(this.entityTarget) || distance < minRange)) {
            if (this.attackTick <= 0) {
                this.attackTick = this.npc.stats.attackSpeed;
                if (this.npc.stats.swingWarmUp == 0)
                    this.npc.attackEntityAsMob(this.entityTarget);
                this.npc.swingItem();
            } else if (this.npc.stats.swingWarmUp > 0 && this.attackTick == this.npc.stats.attackSpeed - this.npc.stats.swingWarmUp) {
                // Perform the actual attack that deals damage
                this.npc.attackEntityAsMob(this.entityTarget);
            }
        }
    }

    public void navOverride(boolean nav) {
        this.navOverride = nav;
        this.setMutexBits(this.navOverride ? AiMutex.PATHING : AiMutex.LOOK + AiMutex.PASSIVE);
    }
}
