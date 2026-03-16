package kamkeel.npcs.controllers.data.ability.type.energy;

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
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyPanelData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import java.util.Arrays;
import java.util.List;

/**
 * Energy Wall ability: Spawns a placeable flat energy barrier.
 * Can float above ground (configurable height offset).
 * Optional launching mode fires the wall forward with damage/knockback.
 */
public class AbilityWall extends AbilityBarrier {

    private final EnergyPanelData panelData;

    public AbilityWall() {
        super(
            new EnergyDisplayData(0x44FFCC, 0x22CCAA, true, 0.3f, 0.4f, 0.5f, 0.0f),
            new EnergyBarrierData(80.0f, true, 160, true)
        );
        this.panelData = new EnergyPanelData(4.0f, 3.0f, 0.0f);
        this.typeId = "ability.cnpc.wall";
        this.name = "Wall";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 15.0f;
        this.minRange = 2.0f;
        this.cooldownTicks = 80;
        this.windUpTicks = 20;
        this.lockMovement = LockMode.WINDUP;
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpAnimationName = "";
        this.activeAnimationName = "";

        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/wall.png"),
            new DefaultIconLayer("customnpcs:textures/gui/ability/wall_overlay.png",
                () -> isOuterColorEnabled() ? getOuterColor() : getInnerColor())
        };
    }

    @Override
    public boolean hasDamage() {
        return panelData.launching; // Only has damage when launching
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityEnergyBarrier createBarrierEntity(IEntityLivingBase caster, IEntityLivingBase target) {
        // Place wall between caster and target
        double placeX, placeY, placeZ;
        float yaw;

        if (target != null) {
            double dx = target.posX - caster.posX;
            double dz = target.posZ - caster.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < 0.01) {
                // Target at same position — fall through to look-direction placement
                float yawRad = (float) Math.toRadians(caster.rotationYaw);
                placeX = caster.posX + (-Math.sin(yawRad) * 3.0);
                placeY = caster.posY;
                placeZ = caster.posZ + (Math.cos(yawRad) * 3.0);
                yaw = caster.rotationYaw;
            } else {
                double placeDist = Math.min(dist * 0.5, 5.0);
                placeX = caster.posX + (dx / dist) * placeDist;
                placeY = caster.posY;
                placeZ = caster.posZ + (dz / dist) * placeDist;
                yaw = (float) (Math.atan2(-dx, dz) * 180.0 / Math.PI);
            }
        } else {
            float yawRad = (float) Math.toRadians(caster.rotationYaw);
            placeX = caster.posX + (-Math.sin(yawRad) * 3.0);
            placeY = caster.posY;
            placeZ = caster.posZ + (Math.cos(yawRad) * 3.0);
            yaw = caster.rotationYaw;
        }

        placeX += offsetX;
        placeY += offsetY;
        placeZ += offsetZ;

        EntityEnergyPanel.PanelMode mode = panelData.launching
            ? EntityEnergyPanel.PanelMode.LAUNCHED
            : EntityEnergyPanel.PanelMode.PLACED;

        EnergyPanelData data = panelData.copy();
        data.heightOffset = 0.0f; // Y offset now handled by base class via placeY

        EntityEnergyPanel panel = new EntityEnergyPanel(
            caster.worldObj, caster,
            placeX, placeY, placeZ, yaw, mode,
            displayData.copy(), lightningData.copy(), barrierData.copy(), data
        );
        panel.setSourceAbility(this);
        return panel;
    }

    @Override
    public float getTelegraphRadius() {
        return Math.max(panelData.panelWidth, panelData.panelHeight) * 0.5f;
    }

    // ==================== VARIANTS ====================

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.static", a -> {
                a.setName("Static Wall");
            }),
            new AbilityVariant("ability.variant.launched", a -> {
                AbilityWall wall = (AbilityWall) a;
                a.setName("Launched Wall");
                wall.panelData.launching = true;
                wall.panelData.launchSpeed = 0.6f;
                wall.panelData.launchDamage = 10.0f;
                wall.panelData.launchKnockback = 3.0f;
            })
        );
    }

    // ==================== TYPE-SPECIFIC NBT ====================

    @Override
    protected void writeBarrierTypeNBT(INbt nbt) {
        panelData.writeNBT(new NBTWrapper(nbt));
    }

    @Override
    protected void readBarrierTypeNBT(INbt nbt) {
        panelData.readNBT(new NBTWrapper(nbt));
        // Migrate legacy heightOffset → base class offsetY
        if (!nbt.hasKey("barrierOffsetY") && panelData.heightOffset != 0.0f) {
            offsetY = panelData.heightOffset;
            panelData.heightOffset = 0.0f;
        }
    }

    // ==================== GETTERS & SETTERS ====================

    public EnergyPanelData getPanelData() {
        return panelData;
    }

    public float getPanelWidth() {
        return panelData.panelWidth;
    }

    public void setPanelWidth(float width) {
        panelData.setPanelWidth(width);
    }

    public float getPanelHeight() {
        return panelData.panelHeight;
    }

    public void setPanelHeight(float height) {
        panelData.setPanelHeight(height);
    }

    public boolean isLaunching() {
        return panelData.launching;
    }

    public void setLaunching(boolean launching) {
        panelData.launching = launching;
    }

    public float getLaunchSpeed() {
        return panelData.launchSpeed;
    }

    public void setLaunchSpeed(float speed) {
        panelData.setLaunchSpeed(speed);
    }

    public float getLaunchDamage() {
        return panelData.launchDamage;
    }

    public void setLaunchDamage(float damage) {
        panelData.setLaunchDamage(damage);
    }

    @Override
    public float getDisplayDamage() { return panelData.launching ? panelData.launchDamage : 0; }

    public float getLaunchKnockback() {
        return panelData.launchKnockback;
    }

    public void setLaunchKnockback(float kb) {
        panelData.setLaunchKnockback(kb);
    }

    // ==================== TYPE-SPECIFIC GUI ====================

    @ClientOnly
    @Override
    protected void addBarrierTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.section("ability.section.panel"));
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.width", this::getPanelWidth, this::setPanelWidth).range(0.5f, 100.0f),
            FieldDef.floatField("gui.height", this::getPanelHeight, this::setPanelHeight).range(0.5f, 100.0f)
        ));
        defs.add(FieldDef.section("ability.section.launch"));
        defs.add(FieldDef.boolField("gui.enabled", this::isLaunching, this::setLaunching)
            .hover("ability.hover.launching"));
        defs.add(FieldDef.row(
            FieldDef.floatField("stats.speed", this::getLaunchSpeed, this::setLaunchSpeed)
                .visibleWhen(this::isLaunching),
            FieldDef.floatField("enchantment.damage", this::getLaunchDamage, this::setLaunchDamage)
                .visibleWhen(this::isLaunching)
        ));
        defs.add(FieldDef.floatField("ability.knockback", this::getLaunchKnockback, this::setLaunchKnockback)
            .visibleWhen(this::isLaunching));
    }
}
