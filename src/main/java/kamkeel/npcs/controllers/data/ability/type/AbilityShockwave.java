package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import noppes.npcs.api.ability.type.IAbilityShockwave;

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
    private int maxTargets = 10;

    public AbilityShockwave() {
        this.typeId = "ability.cnpc.shockwave";
        this.name = "Shockwave";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 8.0f;
        this.lockMovement = LockMovementType.WINDUP_AND_ACTIVE;
        this.cooldownTicks = 0;
        this.windUpTicks = 25;
        this.telegraphType = TelegraphType.CIRCLE;
        this.windUpSound = "game.tnt.primed";
        this.activeSound = "random.explode";
        this.windUpAnimationName = "Ability_Shockwave_Windup";
        this.activeAnimationName = "Ability_Shockwave_Active";
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
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        if (!isPreview()) {
            // Shockwave is instant - apply effect immediately after windup

            // Get all entities in radius
            AxisAlignedBB box = caster.boundingBox.expand(pushRadius, pushRadius / 2, pushRadius);
            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);

            int count = 0;
            for (EntityLivingBase entity : entities) {
                if (entity == caster) continue;
                if (entity.isDead) continue;

                double dist = caster.getDistanceToEntity(entity);
                if (dist > pushRadius) continue;

                count++;
                if (count > maxTargets) break;

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
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (tick == 10)
            signalCompletion();
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("pushRadius", pushRadius);
        nbt.setFloat("pushStrength", pushStrength);
        nbt.setFloat("damage", damage);
        nbt.setInteger("maxTargets", maxTargets);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.pushRadius = nbt.getFloat("pushRadius");
        this.pushStrength = nbt.getFloat("pushStrength");
        this.damage = nbt.getFloat("damage");
        this.maxTargets = nbt.getInteger("maxTargets");
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

    public int getMaxTargets() {
        return maxTargets;
    }

    public void setMaxTargets(int maxTargets) {
        this.maxTargets = maxTargets;
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
            FieldDef.intField("ability.maxTargets", this::getMaxTargets, this::setMaxTargets),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));
    }
}
