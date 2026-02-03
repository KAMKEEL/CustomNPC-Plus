package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.client.gui.builder.ColumnHint;
import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import noppes.npcs.api.ability.type.IAbilityHeavyHit;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Arrays;
import java.util.List;

/**
 * Heavy Hit ability: Single-target melee attack with optional stun.
 * Deals high damage to one target and can stun them.
 */
public class AbilityHeavyHit extends Ability implements IAbilityHeavyHit {

    private float damage = 8.0f;
    private float knockback = 2.0f;

    public AbilityHeavyHit() {
        this.typeId = "ability.cnpc.heavy_hit";
        this.name = "Heavy Hit";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 3.0f;
        this.minRange = 0.0f;
        this.lockMovement = LockMovementType.NO;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.telegraphType = TelegraphType.POINT;
        this.showTelegraph = false;
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
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        if (world.isRemote || target == null) {
            signalCompletion();
            return;
        }

        // Apply damage with scripted event support
        boolean wasHit = applyAbilityDamage(caster, target, damage, knockback);

        // Apply effects if hit wasn't cancelled
        if (wasHit) {
            applyEffects(target);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Just enough delay to see the active animation
        if (tick == 10)
            signalCompletion();
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 8.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 2.0f;
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

    @SideOnly(Side.CLIENT)
    @Override
    public List<FieldDef> getFieldDefinitions() {
        return Arrays.asList(
            FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage).column(ColumnHint.LEFT),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback).column(ColumnHint.RIGHT),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        );
    }
}
