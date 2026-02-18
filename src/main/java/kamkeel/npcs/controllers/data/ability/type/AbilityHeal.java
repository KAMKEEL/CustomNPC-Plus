package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.api.ability.type.IAbilityHeal;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Heal ability: Restore health to self or allies.
 * Can heal a fixed amount or percentage of max health.
 */
public class AbilityHeal extends Ability implements IAbilityHeal {

    // Type-specific parameters
    private int durationTicks = 60;
    private float healAmount = 10.0f;
    private float healPercent = 0.0f;
    private boolean healSelf = true;
    private boolean healAllies = false;
    private float healRadius = 0.0f;
    private boolean instantHeal = true;

    // Runtime state
    private transient List<EntityLivingBase> healedAllies;

    private List<EntityLivingBase> getHealedAllies() {
        if (healedAllies == null) {
            healedAllies = new ArrayList<>();
        }
        return healedAllies;
    }

    public AbilityHeal() {
        this.typeId = "ability.cnpc.heal";
        this.name = "Heal";
        this.targetingMode = TargetingMode.SELF;
        this.lockMovement = LockMovementType.WINDUP;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        // No telegraph for heal - it's a self/ally buff
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasDamage() {
        return false;
    }

    @Override
    public boolean allowBurst() {
        return false;
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.SELF};
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        if (caster.worldObj.isRemote && !isPreview()) return;

        if (!isPreview()) {
            getHealedAllies().clear();

            // Always find allies if we're healing them (needed for both instant and HoT)
            if (healAllies && healRadius > 0) {
                findAlliesInRadius(caster, caster.worldObj);
            }

            if (instantHeal) {
                // Instant heal - apply all healing now
                if (healSelf) {
                    healEntity(caster);
                    spawnHealParticles(caster.worldObj, caster);
                }

                for (EntityLivingBase ally : getHealedAllies()) {
                    healEntity(ally);
                    spawnHealParticles(caster.worldObj, ally);
                }
            }
        }

        if (instantHeal) {
            // Instant heal completes immediately
            signalCompletion();
        }
        // If not instant, HoT healing is applied in onActiveTick
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if ((caster.worldObj.isRemote && !isPreview()) || instantHeal) return;

        // Heal over time - distribute heal across duration ticks
        if (tick % 10 == 0) {
            // Calculate tick-based heal for fixed amount
            float tickHeal = (healAmount / (float) durationTicks) * 10;

            if (healSelf) {
                float selfTickHeal = tickHeal;
                // Add percentage-based heal portion
                if (healPercent > 0) {
                    selfTickHeal += (caster.getMaxHealth() * healPercent / (float) durationTicks) * 10;
                }
                caster.heal(selfTickHeal);
                if (tick % 20 == 0) {
                    spawnHealParticles(caster.worldObj, caster);
                }
            }

            if (healAllies && healRadius > 0) {
                for (EntityLivingBase ally : getHealedAllies()) {
                    if (!ally.isDead) {
                        float allyTickHeal = tickHeal;
                        // Add percentage-based heal portion for each ally
                        if (healPercent > 0) {
                            allyTickHeal += (ally.getMaxHealth() * healPercent / (float) durationTicks) * 10;
                        }
                        ally.heal(allyTickHeal);
                        if (tick % 20 == 0) {
                            spawnHealParticles(caster.worldObj, ally);
                        }
                    }
                }
            }
        }

        // Check if heal duration has ended
        if (tick >= durationTicks) {
            signalCompletion();
        }
    }

    /**
     * Find all allies in radius and add them to healedAllies list.
     */
    private void findAlliesInRadius(EntityLivingBase caster, World world) {
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
            caster.posX - healRadius, caster.posY - 2, caster.posZ - healRadius,
            caster.posX + healRadius, caster.posY + 3, caster.posZ + healRadius
        );

        @SuppressWarnings("unchecked")
        List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);

        for (Entity entity : entities) {
            if (!(entity instanceof EntityLivingBase)) continue;
            if (entity == caster) continue;

            EntityLivingBase living = (EntityLivingBase) entity;

            // Only heal other NPCs (allies)
            if (living instanceof EntityNPCInterface) {
                float dist = caster.getDistanceToEntity(living);
                if (dist <= healRadius) {
                    getHealedAllies().add(living);
                }
            }
        }
    }

    private void healEntity(EntityLivingBase entity) {
        float totalHeal = healAmount;
        if (healPercent > 0) {
            totalHeal += entity.getMaxHealth() * healPercent;
        }
        entity.heal(totalHeal);
    }

    private void spawnHealParticles(World world, EntityLivingBase entity) {
        for (int i = 0; i < 10; i++) {
            double offsetX = (world.rand.nextDouble() - 0.5) * entity.width;
            double offsetY = world.rand.nextDouble() * entity.height;
            double offsetZ = (world.rand.nextDouble() - 0.5) * entity.width;
            world.spawnParticle("happyVillager",
                entity.posX + offsetX,
                entity.posY + offsetY,
                entity.posZ + offsetZ,
                0, 0.1, 0);
        }
    }

    @Override
    public void cleanup() {
        getHealedAllies().clear();
    }

    @Override
    public float getTelegraphRadius() {
        return healRadius > 0 ? healRadius : 0;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setFloat("healAmount", healAmount);
        nbt.setFloat("healPercent", healPercent);
        nbt.setBoolean("healSelf", healSelf);
        nbt.setBoolean("healAllies", healAllies);
        nbt.setFloat("healRadius", healRadius);
        nbt.setBoolean("instantHeal", instantHeal);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.durationTicks = nbt.getInteger("durationTicks");
        this.healAmount = nbt.getFloat("healAmount");
        this.healPercent = nbt.getFloat("healPercent");
        this.healSelf = nbt.getBoolean("healSelf");
        this.healAllies = nbt.getBoolean("healAllies");
        this.healRadius = nbt.getFloat("healRadius");
        this.instantHeal = nbt.getBoolean("instantHeal");
    }

    // Getters & Setters
    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    public float getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(float healAmount) {
        this.healAmount = healAmount;
    }

    public float getHealPercent() {
        return healPercent;
    }

    public void setHealPercent(float healPercent) {
        this.healPercent = healPercent;
    }

    public boolean isHealSelf() {
        return healSelf;
    }

    public void setHealSelf(boolean healSelf) {
        this.healSelf = healSelf;
    }

    public boolean isHealAllies() {
        return healAllies;
    }

    public void setHealAllies(boolean healAllies) {
        this.healAllies = healAllies;
    }

    public float getHealRadius() {
        return healRadius;
    }

    public void setHealRadius(float healRadius) {
        this.healRadius = healRadius;
    }

    public boolean isInstantHeal() {
        return instantHeal;
    }

    public void setInstantHeal(boolean instantHeal) {
        this.instantHeal = instantHeal;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.boolField("ability.instantHeal", this::isInstantHeal, this::setInstantHeal)
                .hover("ability.hover.instant"),
            FieldDef.intField("ability.duration", this::getDurationTicks, this::setDurationTicks)
                .range(1, 1000).visibleWhen(() -> !this.isInstantHeal()),
            FieldDef.section("ability.section.healing"),
            FieldDef.row(
                FieldDef.floatField("ability.healAmount", this::getHealAmount, this::setHealAmount),
                FieldDef.floatField("ability.healPercent", this::getHealPercent, this::setHealPercent)
            ),
            FieldDef.floatField("ability.healRadius", this::getHealRadius, this::setHealRadius),
            FieldDef.row(
                FieldDef.boolField("ability.healSelf", this::isHealSelf, this::setHealSelf)
                    .hover("ability.hover.healSelf"),
                FieldDef.boolField("ability.healAllies", this::isHealAllies, this::setHealAllies)
                    .hover("ability.hover.healAllies")
            )
        ));
    }
}
