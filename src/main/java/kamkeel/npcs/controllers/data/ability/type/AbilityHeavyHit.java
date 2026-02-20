package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetFilter;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.type.IAbilityHeavyHit;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;

/**
 * Heavy Hit ability: AOE rectangle melee attack in front of the caster.
 * Deals high damage to all entities within a rectangular zone and can apply effects.
 * Has a LINE telegraph showing the hit area during windup, making it dodgeable.
 */
public class AbilityHeavyHit extends Ability implements IAbilityHeavyHit {

    private float damage = 8.0f;
    private float knockback = 2.0f;
    private float hitLength = 4.0f;   // How far in front of the caster the hit reaches
    private float hitWidth = 3.0f;    // How wide to each side (total width = hitWidth * 2)
    private int hitDelayTicks = 0;
    private int activeDisplayTicks = 10; // How long active animation plays before completing

    public AbilityHeavyHit() {
        this.typeId = "ability.cnpc.heavy_hit";
        this.name = "Heavy Hit";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 5.0f;
        this.minRange = 0.0f;
        this.lockMovement = LockMovementType.WINDUP;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpSound = "random.anvil_use";
        this.activeSound = "random.anvil_land";
        this.windUpAnimationName = "Ability_HeavyHit_Windup";
        this.activeAnimationName = "Ability_HeavyHit_Active";
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AOE_SELF};
    }

    @Override
    public float getTelegraphLength() {
        return hitLength;
    }

    @Override
    public float getTelegraphWidth() {
        return hitWidth * 2.0f; // Total width (hitWidth is distance to each side)
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        if (caster.worldObj.isRemote && !isPreview()) {
            signalCompletion();
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (isPreview()) {
            if (tick >= (activeDisplayTicks + hitDelayTicks))
                signalCompletion();

            return;
        }

        if (tick >= (activeDisplayTicks + hitDelayTicks)) {
            signalCompletion();
            return;
        }

        if (tick == hitDelayTicks) {
            // Calculate forward and right vectors from caster yaw
            float yawRad = (float) Math.toRadians(caster.rotationYaw);
            double forwardX = -Math.sin(yawRad);
            double forwardZ = Math.cos(yawRad);
            double rightX = forwardZ;   // perpendicular right
            double rightZ = -forwardX;

            // Search area: AABB that encompasses the rectangle
            float searchDist = Math.max(hitLength, hitWidth) + 1.0f;
            @SuppressWarnings("unchecked")
            List<Entity> entities = caster.worldObj.getEntitiesWithinAABBExcludingEntity(
                caster, caster.boundingBox.expand(searchDist, 2, searchDist));

            boolean anyHit = false;
            for (Entity entity : entities) {
                if (!(entity instanceof EntityLivingBase) || entity == caster) continue;
                EntityLivingBase livingTarget = (EntityLivingBase) entity;
                if (!AbilityTargetHelper.shouldAffect(caster, livingTarget, TargetFilter.ENEMIES, false)) continue;

                double dx = livingTarget.posX - caster.posX;
                double dz = livingTarget.posZ - caster.posZ;

                // Project onto forward direction (must be in front, within hitLength)
                double forwardDist = dx * forwardX + dz * forwardZ;
                if (forwardDist < 0 || forwardDist > hitLength) continue;

                // Project onto right direction (must be within hitWidth to each side)
                double sideDist = dx * rightX + dz * rightZ;
                if (Math.abs(sideDist) > hitWidth) continue;

                // Line-of-sight check: skip targets behind solid blocks or enemy barriers
                if (!hasLineOfSight(caster.worldObj, caster, livingTarget)) continue;
                if (isBlockedByBarrier(caster.worldObj, caster, livingTarget)) continue;

                // Entity is within the rectangle - apply damage
                boolean wasHit = applyAbilityDamage(caster, livingTarget, damage, knockback);
                if (wasHit) {
                    applyEffects(livingTarget);
                    anyHit = true;
                }
            }

            // Play hit sound even if nothing was hit (the attack still happens)
            if (!anyHit) {
                caster.worldObj.playSoundAtEntity(caster, "random.anvil_land", 0.5f, 1.2f);
            }
        }
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("hitLength", hitLength);
        nbt.setFloat("hitWidth", hitWidth);
        nbt.setInteger("activeDisplayTicks", activeDisplayTicks);
        nbt.setInteger("hitDelayTicks", hitDelayTicks);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.damage = nbt.getFloat("damage");
        this.knockback = nbt.getFloat("knockback");
        this.hitLength = nbt.getFloat("hitLength");
        this.hitWidth = nbt.getFloat("hitWidth");
        this.hitDelayTicks = nbt.getInteger("hitDelayTicks");
        this.activeDisplayTicks = nbt.getInteger("activeDisplayTicks");
    }

    // Getters & Setters
    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    public float getHitLength() {
        return hitLength;
    }

    public void setHitLength(float hitLength) {
        this.hitLength = hitLength;
    }

    public float getHitWidth() {
        return hitWidth;
    }

    public void setHitWidth(float hitWidth) {
        this.hitWidth = hitWidth;
    }

    public int getActiveDisplayTicks() {
        return activeDisplayTicks;
    }

    public void setActiveDisplayTicks(int activeDisplayTicks) {
        this.activeDisplayTicks = activeDisplayTicks;
    }

    public int getHitDelayTicks() {
        return hitDelayTicks;
    }

    public void setHitDelayTicks(int hitDelayTicks) {
        this.hitDelayTicks = hitDelayTicks;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback)
            ),
            FieldDef.section("ability.section.hitZone"),
            FieldDef.row(
                FieldDef.floatField("ability.hitLength", this::getHitLength, this::setHitLength),
                FieldDef.floatField("ability.hitWidth", this::getHitWidth, this::setHitWidth)
            ),
            FieldDef.section("ability.section.timing"),
            FieldDef.row(
                FieldDef.intField("ability.hitDelayTicks", this::getHitDelayTicks, this::setHitDelayTicks).min(0),
                FieldDef.intField("ability.activeDisplayTicks", this::getActiveDisplayTicks, this::setActiveDisplayTicks).range(1, 200)
            ),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));
    }
}
