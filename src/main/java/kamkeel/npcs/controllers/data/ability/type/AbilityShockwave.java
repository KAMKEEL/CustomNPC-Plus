package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityShockwave;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Shockwave ability: Pushes targets away from the caster with damage.
 * Opposite of Vortex - instant knockback rather than gradual pull.
 */
public class AbilityShockwave extends Ability {

    private float pushRadius = 8.0f;
    private float pushStrength = 1.5f;
    private float damage = 8.0f;
    private int stunDuration = 0;
    private int maxTargets = 10;

    // Runtime state
    private transient boolean executed = false;

    public AbilityShockwave() {
        this.typeId = "ability.cnpc.shockwave";
        this.name = "Shockwave";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 15.0f;
        this.lockMovement = true;
        this.cooldownTicks = 100;
        this.windUpTicks = 25;
        this.activeTicks = 5;
        this.recoveryTicks = 10;
        this.telegraphType = TelegraphType.CIRCLE;
        this.windUpSound = "random.explode";
        this.activeSound = "random.explode";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityShockwave(this, callback);
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
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        executed = false;
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (executed) return;
        executed = true;

        // Get all entities in radius
        AxisAlignedBB box = npc.boundingBox.expand(pushRadius, pushRadius / 2, pushRadius);
        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);

        int count = 0;
        for (EntityLivingBase entity : entities) {
            if (entity == npc) continue;
            if (entity.isDead) continue;

            double dist = npc.getDistanceToEntity(entity);
            if (dist > pushRadius) continue;

            count++;
            if (count > maxTargets) break;

            // Calculate push direction (away from caster)
            double dx = entity.posX - npc.posX;
            double dz = entity.posZ - npc.posZ;
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
            boolean wasHit = applyAbilityDamageWithDirection(npc, entity, damage * distFactor, finalPush, dx, dz);

            // Apply stun if hit connected
            if (wasHit && stunDuration > 0) {
                entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, stunDuration, 10));
                entity.addPotionEffect(new PotionEffect(Potion.weakness.id, stunDuration, 2));
            }
        }
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        executed = false;
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
        executed = false;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("pushRadius", pushRadius);
        nbt.setFloat("pushStrength", pushStrength);
        nbt.setFloat("damage", damage);
        nbt.setInteger("stunDuration", stunDuration);
        nbt.setInteger("maxTargets", maxTargets);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.pushRadius = nbt.hasKey("pushRadius") ? nbt.getFloat("pushRadius") : 8.0f;
        this.pushStrength = nbt.hasKey("pushStrength") ? nbt.getFloat("pushStrength") : 1.5f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 8.0f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.maxTargets = nbt.hasKey("maxTargets") ? nbt.getInteger("maxTargets") : 10;
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

    public int getStunDuration() {
        return stunDuration;
    }

    public void setStunDuration(int stunDuration) {
        this.stunDuration = stunDuration;
    }

    public int getMaxTargets() {
        return maxTargets;
    }

    public void setMaxTargets(int maxTargets) {
        this.maxTargets = maxTargets;
    }

}
