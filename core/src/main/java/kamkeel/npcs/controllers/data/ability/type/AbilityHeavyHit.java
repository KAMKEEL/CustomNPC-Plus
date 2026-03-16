package kamkeel.npcs.controllers.data.ability.type;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import noppes.npcs.api.ability.type.IAbilityHeavyHit;
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
        this.lockMovement = LockMode.WINDUP;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpSound = "random.anvil_use";
        this.activeSound = "random.anvil_land";
        this.windUpAnimationName = "Ability_HeavyHit_Windup";
        this.activeAnimationName = "Ability_HeavyHit_Active";
        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/heavy_hit.png"),
            new DefaultIconLayer("customnpcs:textures/gui/ability/heavy_hit_overlay.png",
                this::getActiveColor)
        };
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
    public void onExecute(IEntityLivingBase caster, IEntityLivingBase target) {
        if (caster.worldObj.isRemote && !isPreview()) {
            signalCompletion();
        }
    }

    @Override
    public void onActiveTick(IEntityLivingBase caster, IEntityLivingBase target, int tick) {
        if (isPreview()) {
            if (tick >= (activeDisplayTicks + hitDelayTicks))
                signalCompletion();

            return;
        }

        if (tick >= (activeDisplayTicks + hitDelayTicks)) {
            signalCompletion();
            return;
        }

        if (tick == hitDelayTicks && !caster.worldObj.isRemote) {
            // Calculate forward and right vectors from caster yaw
            float yawRad = (float) Math.toRadians(caster.rotationYaw);
            double forwardX = -Math.sin(yawRad);
            double forwardZ = Math.cos(yawRad);
            double rightX = forwardZ;   // perpendicular right
            double rightZ = -forwardX;

            // Search area: AABB that encompasses the rectangle
            float searchDist = Math.max(hitLength, hitWidth) + 1.0f;
            @SuppressWarnings("unchecked")
            List<IEntity> entities = caster.worldObj.getEntitiesWithinAABBExcludingEntity(
                caster, caster.boundingBox.expand(searchDist, 2, searchDist));

            boolean anyHit = false;
            for (IEntity IEntity : entities) {
                if (!(IEntity instanceof IEntityLivingBase) || IEntity == caster) continue;
                IEntityLivingBase livingTarget = (IEntityLivingBase) IEntity;
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

                // IEntity is within the rectangle - apply damage
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
    public void writeTypeNBT(INbt nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("hitLength", hitLength);
        nbt.setFloat("hitWidth", hitWidth);
        nbt.setInteger("activeDisplayTicks", activeDisplayTicks);
        nbt.setInteger("hitDelayTicks", hitDelayTicks);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
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

    @Override
    public float getDisplayDamage() { return damage; }

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

    @ClientOnly
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
