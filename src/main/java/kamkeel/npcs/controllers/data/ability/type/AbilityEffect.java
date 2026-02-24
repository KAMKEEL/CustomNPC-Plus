package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.effect.AbilityCustomEffect;
import kamkeel.npcs.controllers.data.ability.data.entry.AbilityEffectActionEntry;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import noppes.npcs.api.ability.type.IAbilityEffect;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Effect ability: Apply healing, potion effects, custom effects, and mod-registered
 * effect actions to self or nearby entities.
 * <p>
 * Supports:
 * <ul>
 *   <li>SELF or AOE_SELF targeting modes</li>
 *   <li>AOE target filtering: ALLIES, ENEMIES, or ALL</li>
 *   <li>Healing (fixed amount + percentage)</li>
 *   <li>Potion effects via base Ability.effects list</li>
 *   <li>Custom effects from CustomEffectController (player-only)</li>
 *   <li>Mod-registered effect actions via IEffectAction registry</li>
 *   <li>Burst system for repeated reapplication (auras, debuffs)</li>
 *   <li>Concurrent execution alongside other abilities in chains</li>
 * </ul>
 */
public class AbilityEffect extends Ability implements IAbilityEffect {

    // Type-specific parameters
    private int durationTicks = 60;
    private float healAmount = 10.0f;
    private float healPercent = 0.0f;
    private boolean includeSelf = true;
    private float radius = 8.0f;
    private boolean instantHeal = true;
    private TargetFilter targetFilter = TargetFilter.ALLIES;

    // Custom effects (from CustomEffectController — player-only)
    private List<AbilityCustomEffect> customEffects = new ArrayList<>();

    // Mod-registered effect actions
    private List<AbilityEffectActionEntry> effectActions = new ArrayList<>();

    // Runtime state
    private transient List<EntityLivingBase> affectedEntities;

    private List<EntityLivingBase> getAffectedEntities() {
        if (affectedEntities == null) {
            affectedEntities = new ArrayList<>();
        }
        return affectedEntities;
    }

    public AbilityEffect() {
        this.typeId = "ability.cnpc.effect";
        this.name = "Effect";
        this.targetingMode = TargetingMode.SELF;
        this.lockMovement = LockMode.WINDUP;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    // ==================== ABILITY TYPE OVERRIDES ====================

    @Override
    public boolean hasDamage() {
        return false;
    }

    @Override
    public boolean allowBurst() {
        return true;
    }

    @Override
    public boolean isTargetingModeLocked() {
        return false;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.SELF, TargetingMode.AOE_SELF};
    }

    @Override
    public boolean isConcurrentCapable() {
        return true;
    }

    @Override
    public float getTelegraphRadius() {
        return targetingMode == TargetingMode.AOE_SELF && radius > 0 ? radius : 0;
    }

    // ==================== EXECUTION ====================

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        if (caster.worldObj.isRemote && !isPreview()) return;

        if (!isPreview()) {
            getAffectedEntities().clear();

            if (targetingMode == TargetingMode.AOE_SELF && radius > 0) {
                findEntitiesInRadius(caster, caster.worldObj);
            } else {
                if (includeSelf) {
                    getAffectedEntities().add(caster);
                }
            }

            if (instantHeal) {
                for (EntityLivingBase entity : getAffectedEntities()) {
                    healEntity(caster, entity);
                    applyAllEffects(caster, entity);
                    spawnHealParticles(caster.worldObj, entity);
                }
            }
        }

        if (instantHeal) {
            signalCompletion();
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if ((caster.worldObj.isRemote && !isPreview()) || instantHeal) return;

        if (tick % 10 == 0) {
            float tickHealFixed = (healAmount / (float) durationTicks) * 10;

            for (EntityLivingBase entity : getAffectedEntities()) {
                if (entity.isDead) continue;

                float totalTickHeal = tickHealFixed;
                if (healPercent > 0) {
                    totalTickHeal += (entity.getMaxHealth() * healPercent / (float) durationTicks) * 10;
                }

                if (totalTickHeal > 0) {
                    if (!AbilityController.Instance.fireOnAbilityHeal(this, caster, entity, totalTickHeal)) {
                        entity.heal(totalTickHeal);
                    }
                }

                if (tick % 20 == 0) {
                    applyAllEffects(caster, entity);
                    spawnHealParticles(caster.worldObj, entity);
                }
            }
        }

        if (tick >= durationTicks) {
            signalCompletion();
            return;
        }

        // Early completion if all affected entities are dead
        boolean allDead = true;
        for (EntityLivingBase entity : getAffectedEntities()) {
            if (!entity.isDead) {
                allDead = false;
                break;
            }
        }
        if (!getAffectedEntities().isEmpty() && allDead) {
            signalCompletion();
        }
    }

    // ==================== EFFECT APPLICATION ====================

    /**
     * Applies all configured effect types to a single entity:
     * vanilla potion effects, custom effects, and mod-registered effect actions.
     */
    private void applyAllEffects(EntityLivingBase caster, EntityLivingBase entity) {
        applyEffects(entity);
        applyCustomEffects(entity);
        applyEffectActions(caster, entity);
    }

    private void applyCustomEffects(EntityLivingBase entity) {
        for (AbilityCustomEffect ce : customEffects) {
            if (ce.isValid()) {
                ce.apply(entity);
            }
        }
    }

    private void applyEffectActions(EntityLivingBase caster, EntityLivingBase entity) {
        for (AbilityEffectActionEntry ea : effectActions) {
            if (ea.isValid()) {
                ea.apply(caster, entity);
            }
        }
    }

    // ==================== TARGET GATHERING ====================

    @SuppressWarnings("unchecked")
    private void findEntitiesInRadius(EntityLivingBase caster, World world) {
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
            caster.posX - radius, caster.posY - 2, caster.posZ - radius,
            caster.posX + radius, caster.posY + 3, caster.posZ + radius
        );

        List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);

        for (Entity entity : entities) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) entity;

            float dist = caster.getDistanceToEntity(living);
            if (dist > radius) continue;

            if (!AbilityTargetHelper.shouldAffect(caster, living, targetFilter, includeSelf)) continue;

            getAffectedEntities().add(living);
        }
    }

    private void healEntity(EntityLivingBase caster, EntityLivingBase entity) {
        float totalHeal = healAmount;
        if (healPercent > 0) {
            totalHeal += entity.getMaxHealth() * healPercent;
        }
        if (totalHeal > 0) {
            if (!AbilityController.Instance.fireOnAbilityHeal(this, caster, entity, totalHeal)) {
                entity.heal(totalHeal);
            }
        }
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

    // ==================== LIFECYCLE ====================

    @Override
    public void resetForBurst() {
        getAffectedEntities().clear();
    }

    @Override
    public void cleanup() {
        getAffectedEntities().clear();
    }

    // ==================== VARIANTS ====================

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.selfHeal", a -> {
                AbilityEffect e = (AbilityEffect) a;
                a.setName("Self Heal");
                e.setTargetingMode(TargetingMode.SELF);
                e.setIncludeSelf(true);
                e.setInstantHeal(true);
            }),
            new AbilityVariant("ability.variant.healingAura", a -> {
                AbilityEffect e = (AbilityEffect) a;
                a.setName("Healing Aura");
                e.setTargetingMode(TargetingMode.AOE_SELF);
                e.setTargetFilter(TargetFilter.ALLIES);
                e.setIncludeSelf(true);
                e.setRadius(8.0f);
                e.setBurstEnabled(true);
                e.setBurstAmount(5);
                e.setBurstDelay(20);
            }),
            new AbilityVariant("ability.variant.poisonDebuff", a -> {
                AbilityEffect e = (AbilityEffect) a;
                a.setName("Poison Debuff");
                e.setTargetingMode(TargetingMode.AOE_SELF);
                e.setTargetFilter(TargetFilter.ENEMIES);
                e.setIncludeSelf(false);
                e.setHealAmount(0);
                e.setHealPercent(0);
                e.setRadius(6.0f);
                e.setBurstEnabled(true);
                e.setBurstAmount(3);
                e.setBurstDelay(40);
            })
        );
    }

    // ==================== NBT ====================

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setFloat("healAmount", healAmount);
        nbt.setFloat("healPercent", healPercent);
        nbt.setBoolean("includeSelf", includeSelf);
        nbt.setFloat("radius", radius);
        nbt.setBoolean("instantHeal", instantHeal);
        nbt.setString("targetFilter", targetFilter.name());

        // Custom effects
        NBTTagList ceList = new NBTTagList();
        for (AbilityCustomEffect ce : customEffects) {
            ceList.appendTag(ce.writeNBT());
        }
        nbt.setTag("customEffects", ceList);

        // Effect actions
        NBTTagList eaList = new NBTTagList();
        for (AbilityEffectActionEntry ea : effectActions) {
            eaList.appendTag(ea.writeNBT());
        }
        nbt.setTag("effectActions", eaList);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.durationTicks = nbt.getInteger("durationTicks");
        this.healAmount = nbt.getFloat("healAmount");
        this.healPercent = nbt.getFloat("healPercent");
        this.includeSelf = nbt.getBoolean("includeSelf");
        this.radius = nbt.getFloat("radius");
        this.instantHeal = nbt.getBoolean("instantHeal");
        this.targetFilter = TargetFilter.fromString(nbt.getString("targetFilter"));

        // Custom effects
        customEffects.clear();
        if (nbt.hasKey("customEffects")) {
            NBTTagList ceList = nbt.getTagList("customEffects", 10);
            for (int i = 0; i < ceList.tagCount(); i++) {
                AbilityCustomEffect ce = AbilityCustomEffect.fromNBT(ceList.getCompoundTagAt(i));
                if (ce.isValid()) customEffects.add(ce);
            }
        }

        // Effect actions
        effectActions.clear();
        if (nbt.hasKey("effectActions")) {
            NBTTagList eaList = nbt.getTagList("effectActions", 10);
            for (int i = 0; i < eaList.tagCount(); i++) {
                AbilityEffectActionEntry ea = AbilityEffectActionEntry.fromNBT(eaList.getCompoundTagAt(i));
                effectActions.add(ea);
            }
        }
    }

    // ==================== GUI ====================

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
            FieldDef.section("ability.section.targeting")
                .tab("Target").visibleWhen(() -> this.targetingMode == TargetingMode.AOE_SELF),
            FieldDef.enumField("ability.targetFilter", TargetFilter.class,
                this::getTargetFilter, this::setTargetFilter)
                .tab("Target").visibleWhen(() -> this.targetingMode == TargetingMode.AOE_SELF),
            FieldDef.row(
                FieldDef.boolField("ability.includeSelf", this::isIncludeSelf, this::setIncludeSelf)
                    .visibleWhen(() -> this.targetingMode == TargetingMode.AOE_SELF),
                FieldDef.floatField("gui.radius", this::getRadius, this::setRadius)
                    .visibleWhen(() -> this.targetingMode == TargetingMode.AOE_SELF)
            ).tab("Target"),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects),
            AbilityFieldDefs.customEffectsListField("ability.customEffects", this::getCustomEffects, this::setCustomEffects),
            AbilityFieldDefs.effectActionsListField("ability.effectActions", this::getEffectActionEntries, this::setEffectActionEntries)
                .visibleWhen(() -> AbilityController.Instance.hasEffectActions())
        ));
    }

    // ==================== GETTERS & SETTERS ====================

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    @Override
    public float getHealAmount() {
        return healAmount;
    }

    @Override
    public void setHealAmount(float healAmount) {
        this.healAmount = healAmount;
    }

    @Override
    public float getHealPercent() {
        return healPercent;
    }

    @Override
    public void setHealPercent(float healPercent) {
        this.healPercent = healPercent;
    }

    @Override
    public boolean isIncludeSelf() {
        return includeSelf;
    }

    @Override
    public void setIncludeSelf(boolean includeSelf) {
        this.includeSelf = includeSelf;
    }

    @Override
    public float getRadius() {
        return radius;
    }

    @Override
    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public boolean isInstantHeal() {
        return instantHeal;
    }

    @Override
    public void setInstantHeal(boolean instantHeal) {
        this.instantHeal = instantHeal;
    }

    public TargetFilter getTargetFilter() {
        return targetFilter;
    }

    public void setTargetFilter(TargetFilter targetFilter) {
        this.targetFilter = targetFilter;
    }

    @Override
    public int getTargetFilterType() {
        return targetFilter.ordinal();
    }

    @Override
    public void setTargetFilterType(int filter) {
        TargetFilter[] values = TargetFilter.values();
        if (filter >= 0 && filter < values.length) {
            this.targetFilter = values[filter];
        }
    }

    public List<AbilityCustomEffect> getCustomEffects() {
        return customEffects;
    }

    public void setCustomEffects(List<AbilityCustomEffect> customEffects) {
        this.customEffects = customEffects != null ? customEffects : new ArrayList<>();
    }

    @Override
    public int getCustomEffectCount() {
        return customEffects.size();
    }

    public List<AbilityEffectActionEntry> getEffectActionEntries() {
        return effectActions;
    }

    public void setEffectActionEntries(List<AbilityEffectActionEntry> effectActions) {
        this.effectActions = effectActions != null ? effectActions : new ArrayList<>();
    }

    @Override
    public int getEffectActionCount() {
        return effectActions.size();
    }
}
