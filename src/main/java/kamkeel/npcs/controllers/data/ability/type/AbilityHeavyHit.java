package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import kamkeel.npcs.controllers.data.ability.Ability;
import noppes.npcs.client.gui.advanced.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityHeavyHit;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Heavy Hit ability: Single-target melee attack with optional stun.
 * Deals high damage to one target and can stun them.
 */
public class AbilityHeavyHit extends Ability {

    private float damage = 15.0f;
    private float knockback = 2.0f;
    private float knockbackUp = 0.3f;
    private int stunTicks = 40;

    public AbilityHeavyHit() {
        this.typeId = "cnpc:heavy_hit";
        this.name = "Heavy Hit";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 3.0f;
        this.minRange = 0.0f;
        this.lockMovement = true;
        this.cooldownTicks = 80;
        this.windUpTicks = 30;
        this.activeTicks = 5;
        this.recoveryTicks = 20;
        this.telegraphType = TelegraphType.POINT;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(
            IAbilityConfigCallback callback) {
        return new SubGuiAbilityHeavyHit(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AGGRO_TARGET };
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        if (world.isRemote || target == null) return;

        // Apply damage with scripted event support
        boolean wasHit = applyAbilityDamage(npc, target, damage, knockback, knockbackUp);

        // Apply stun (slowness + weakness) if the hit wasn't cancelled
        if (wasHit && stunTicks > 0) {
            target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, stunTicks, 10));
            target.addPotionEffect(new PotionEffect(Potion.weakness.id, stunTicks, 2));
        }
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Heavy Hit is instant, no active tick needed
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
        nbt.setInteger("stunTicks", stunTicks);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 15.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 2.0f;
        this.knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.3f;
        this.stunTicks = nbt.hasKey("stunTicks") ? nbt.getInteger("stunTicks") : 40;
    }

    // Getters & Setters
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getKnockback() { return knockback; }
    public void setKnockback(float knockback) { this.knockback = knockback; }

    public float getKnockbackUp() { return knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { this.knockbackUp = knockbackUp; }

    public int getStunTicks() { return stunTicks; }
    public void setStunTicks(int stunTicks) { this.stunTicks = stunTicks; }
}
