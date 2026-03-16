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
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.effect.AbilityCustomEffect;
import kamkeel.npcs.controllers.data.ability.data.entry.AbilityEffectActionEntry;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import noppes.npcs.api.ability.type.IAbilityEffect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Effect ability: Apply healing, IPotionType effects, custom effects, and mod-registered
 * effect actions to self or nearby entities.
 * <p>
 * Supports:
 * <ul>
 *   <li>SELF or AOE_SELF targeting modes</li>
 *   <li>AOE target filtering: ALLIES, ENEMIES, or ALL</li>
 *   <li>Healing (fixed amount + percentage)</li>
 *   <li>IPotionType effects via base Ability.effects list</li>
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
    private transient List<IEntityLivingBase> affectedEntities;

    private List<IEntityLivingBase> getAffectedEntities() {
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
        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/effect.png"),
            new DefaultIconLayer("customnpcs:textures/gui/ability/effect_overlay.png",
                this::getActiveColor)
        };
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
    public void onExecute(IEntityLivingBase caster, IEntityLivingBase target) {
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
                for (IEntityLivingBase IEntity : getAffectedEntities()) {
                    healEntity(caster, IEntity);
                    applyAllEffects(caster, IEntity);
                    spawnHealParticles(caster.worldObj, IEntity);
                }
            }
        }

        if (instantHeal) {
            signalCompletion();
        }
    }

    @Override
    public void onActiveTick(IEntityLivingBase caster, IEntityLivingBase target, int tick) {
        if ((caster.worldObj.isRemote && !isPreview()) || instantHeal) return;

        if (tick % 10 == 0) {
            float tickHealFixed = (healAmount / (float) durationTicks) * 10;

            for (IEntityLivingBase IEntity : getAffectedEntities()) {
                if (IEntity.isDead) continue;

                float totalTickHeal = tickHealFixed;
                if (healPercent > 0) {
                    totalTickHeal += (IEntity.getMaxHealth() * healPercent / (float) durationTicks) * 10;
                }

                if (totalTickHeal > 0) {
                    if (!AbilityController.Instance.fireOnAbilityHeal(this, caster, IEntity, totalTickHeal)) {
                        IEntity.heal(totalTickHeal);
                    }
                }

                if (tick % 20 == 0) {
                    applyAllEffects(caster, IEntity);
                    spawnHealParticles(caster.worldObj, IEntity);
                }
            }
        }

        if (tick >= durationTicks) {
            signalCompletion();
            return;
        }

        // Early completion if all affected entities are dead
        boolean allDead = true;
        for (IEntityLivingBase IEntity : getAffectedEntities()) {
            if (!IEntity.isDead) {
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
     * Applies all configured effect types to a single IEntity:
     * vanilla IPotionType effects, custom effects, and mod-registered effect actions.
     */
    private void applyAllEffects(IEntityLivingBase caster, IEntityLivingBase IEntity) {
        applyEffects(IEntity);
        applyCustomEffects(IEntity);
        applyEffectActions(caster, IEntity);
    }

    private void applyCustomEffects(IEntityLivingBase IEntity) {
        for (AbilityCustomEffect ce : customEffects) {
            if (ce.isValid()) {
                ce.apply(IEntity);
            }
        }
    }

    private void applyEffectActions(IEntityLivingBase caster, IEntityLivingBase IEntity) {
        for (AbilityEffectActionEntry ea : effectActions) {
            if (ea.isValid()) {
                ea.apply(caster, IEntity);
            }
        }
    }

    // ==================== TARGET GATHERING ====================

    @SuppressWarnings("unchecked")
    private void findEntitiesInRadius(IEntityLivingBase caster, IWorld IWorld) {
        IBoundingBox aabb = IBoundingBox.getBoundingBox(
            caster.posX - radius, caster.posY - 2, caster.posZ - radius,
            caster.posX + radius, caster.posY + 3, caster.posZ + radius
        );

        List<IEntity> entities = IWorld.getEntitiesWithinAABB(IEntityLivingBase.class, aabb);

        for (IEntity IEntity : entities) {
            if (!(IEntity instanceof IEntityLivingBase)) continue;
            IEntityLivingBase living = (IEntityLivingBase) IEntity;

            float dist = caster.getDistanceToEntity(living);
            if (dist > radius) continue;

            if (!AbilityTargetHelper.shouldAffect(caster, living, targetFilter, includeSelf)) continue;

            getAffectedEntities().add(living);
        }
    }

    private void healEntity(IEntityLivingBase caster, IEntityLivingBase IEntity) {
        float totalHeal = healAmount;
        if (healPercent > 0) {
            totalHeal += IEntity.getMaxHealth() * healPercent;
        }
        if (totalHeal > 0) {
            if (!AbilityController.Instance.fireOnAbilityHeal(this, caster, IEntity, totalHeal)) {
                IEntity.heal(totalHeal);
            }
        }
    }

    private void spawnHealParticles(IWorld IWorld, IEntityLivingBase IEntity) {
        for (int i = 0; i < 10; i++) {
            double offsetX = (IWorld.rand.nextDouble() - 0.5) * IEntity.width;
            double offsetY = IWorld.rand.nextDouble() * IEntity.height;
            double offsetZ = (IWorld.rand.nextDouble() - 0.5) * IEntity.width;
            IWorld.spawnParticle("happyVillager",
                IEntity.posX + offsetX,
                IEntity.posY + offsetY,
                IEntity.posZ + offsetZ,
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
    public void writeTypeNBT(INbt nbt) {
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setFloat("healAmount", healAmount);
        nbt.setFloat("healPercent", healPercent);
        nbt.setBoolean("includeSelf", includeSelf);
        nbt.setFloat("radius", radius);
        nbt.setBoolean("instantHeal", instantHeal);
        nbt.setString("targetFilter", targetFilter.name());

        // Custom effects
        INbtList ceList = new INbtList();
        for (AbilityCustomEffect ce : customEffects) {
            ceList.appendTag(ce.writeNBT());
        }
        nbt.setTag("customEffects", ceList);

        // Effect actions
        INbtList eaList = new INbtList();
        for (AbilityEffectActionEntry ea : effectActions) {
            eaList.appendTag(ea.writeNBT());
        }
        nbt.setTag("effectActions", eaList);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
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
            INbtList ceList = nbt.getTagList("customEffects", 10);
            for (int i = 0; i < ceList.tagCount(); i++) {
                AbilityCustomEffect ce = AbilityCustomEffect.fromNBT(ceList.getCompoundTagAt(i));
                if (ce.isValid()) customEffects.add(ce);
            }
        }

        // Effect actions
        effectActions.clear();
        if (nbt.hasKey("effectActions")) {
            INbtList eaList = nbt.getTagList("effectActions", 10);
            for (int i = 0; i < eaList.tagCount(); i++) {
                AbilityEffectActionEntry ea = AbilityEffectActionEntry.fromNBT(eaList.getCompoundTagAt(i));
                effectActions.add(ea);
            }
        }
    }

    // ==================== GUI ====================

    @ClientOnly
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
