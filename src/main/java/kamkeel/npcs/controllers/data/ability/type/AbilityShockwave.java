package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import noppes.npcs.api.ability.type.IAbilityShockwave;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.List;

/**
 * Shockwave ability: Pushes targets away from the caster with damage.
 * Opposite of Vortex - instant knockback rather than gradual pull.
 */
public class AbilityShockwave extends Ability implements IAbilityShockwave {

    private float pushRadius = 8.0f;
    private float pushStrength = 1.5f;
    private float damage = 8.0f;
    private boolean aoe = true;
    private int activeDisplayTicks = 10;

    public AbilityShockwave() {
        this.typeId = "ability.cnpc.shockwave";
        this.name = "Shockwave";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 8.0f;
        this.lockMovement = LockMode.WINDUP_AND_ACTIVE;
        this.cooldownTicks = 0;
        this.windUpTicks = 25;
        this.telegraphType = TelegraphType.CIRCLE;
        this.windUpSound = "game.tnt.primed";
        this.activeSound = "random.explode";
        this.windUpAnimationName = "Ability_Shockwave_Windup";
        this.activeAnimationName = "Ability_Shockwave_Active";
        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/shockwave.png"),
            new DefaultIconLayer("customnpcs:textures/gui/ability/shockwave_overlay.png",
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
    public float getTelegraphRadius() {
        return pushRadius;
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        if (!isPreview() && !caster.worldObj.isRemote) {
            // Shockwave is instant - apply effect immediately after windup

            // Get all entities in radius
            AxisAlignedBB box = caster.boundingBox.expand(pushRadius, pushRadius / 2, pushRadius);
            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = caster.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);

            if (aoe) {
                // ALL: push every valid enemy in range
                for (EntityLivingBase entity : entities) {
                    if (entity == caster) continue;
                    if (entity.isDead) continue;
                    if (!AbilityTargetHelper.shouldAffect(caster, entity, TargetFilter.ENEMIES, false)) continue;

                    double dist = caster.getDistanceToEntity(entity);
                    if (dist > pushRadius) continue;

                    applyShockwavePush(caster, entity, dist);
                }
            } else {
                // SINGULAR: push one target
                // NPC: push aggro target if valid, otherwise nearest enemy
                // Player: push nearest enemy in range
                if (!isPlayerCaster(caster) && target != null && !target.isDead) {
                    double dist = caster.getDistanceToEntity(target);
                    if (dist <= pushRadius && AbilityTargetHelper.shouldAffect(caster, target, TargetFilter.ENEMIES, false)) {
                        applyShockwavePush(caster, target, dist);
                        return;
                    }
                }

                // Find nearest valid enemy
                EntityLivingBase nearest = null;
                double nearestDist = Double.MAX_VALUE;
                for (EntityLivingBase entity : entities) {
                    if (entity == caster) continue;
                    if (entity.isDead) continue;
                    if (!AbilityTargetHelper.shouldAffect(caster, entity, TargetFilter.ENEMIES, false)) continue;

                    double dist = caster.getDistanceToEntity(entity);
                    if (dist <= pushRadius && dist < nearestDist) {
                        nearest = entity;
                        nearestDist = dist;
                    }
                }
                if (nearest != null) {
                    applyShockwavePush(caster, nearest, nearestDist);
                }
            }
        }
    }

    private void applyShockwavePush(EntityLivingBase caster, EntityLivingBase entity, double dist) {
        // Calculate push direction (away from caster)
        double dx = entity.posX - caster.posX;
        double dz = entity.posZ - caster.posZ;
        double len = Math.sqrt(dx * dx + dz * dz);

        if (len > 0) {
            dx /= len;
            dz /= len;
        } else {
            // Entity is directly on top of caster, push in random direction
            double angle = Math.random() * Math.PI * 2;
            dx = Math.cos(angle);
            dz = Math.sin(angle);
        }

        // Scale knockback by distance (closer = stronger)
        float distFactor = 1.0f - (float) (dist / pushRadius) * 0.5f;
        float finalPush = pushStrength * distFactor;
        // Apply damage with custom knockback direction
        boolean wasHit = applyAbilityDamageWithDirection(caster, entity, damage * distFactor, finalPush, dx, dz);

        // Apply effects if hit connected
        if (wasHit) {
            applyEffects(entity);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (tick >= activeDisplayTicks)
            signalCompletion();
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("pushRadius", pushRadius);
        nbt.setFloat("pushStrength", pushStrength);
        nbt.setFloat("damage", damage);
        nbt.setBoolean("aoe", aoe);
        nbt.setInteger("activeDisplayTicks", activeDisplayTicks);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.pushRadius = nbt.getFloat("pushRadius");
        this.pushStrength = nbt.getFloat("pushStrength");
        this.damage = nbt.getFloat("damage");
        this.aoe = !nbt.hasKey("aoe") || nbt.getBoolean("aoe");
        this.activeDisplayTicks = nbt.hasKey("activeDisplayTicks") ? nbt.getInteger("activeDisplayTicks") : 10;
    }

    // Getters & Setters
    public float getPushRadius() {
        return pushRadius;
    }

    public void setPushRadius(float pushRadius) {
        this.pushRadius = pushRadius;
    }

    public float getPushStrength() {
        return pushStrength;
    }

    public void setPushStrength(float pushStrength) {
        this.pushStrength = pushStrength;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public boolean isAoe() {
        return aoe;
    }

    public void setAoe(boolean aoe) {
        this.aoe = aoe;
    }

    public int getActiveDisplayTicks() {
        return activeDisplayTicks;
    }

    public void setActiveDisplayTicks(int activeDisplayTicks) {
        this.activeDisplayTicks = activeDisplayTicks;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
            FieldDef.section("ability.section.push"),
            FieldDef.row(
                FieldDef.floatField("gui.radius", this::getPushRadius, this::setPushRadius),
                FieldDef.floatField("gui.strength", this::getPushStrength, this::setPushStrength)
            ),
            FieldDef.section("ability.section.aoe"),
            FieldDef.boolField("gui.enabled", this::isAoe, this::setAoe)
                .hover("ability.hover.aoe"),
            FieldDef.intField("ability.activeDisplayTicks", this::getActiveDisplayTicks, this::setActiveDisplayTicks).range(1, 200),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));
    }
}
