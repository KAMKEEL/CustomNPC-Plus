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
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyPanelData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import java.util.Arrays;
import java.util.List;

/**
 * Energy Shield ability: Spawns a flat energy panel held in front of the caster.
 * The shield follows the caster's position and rotates with their look direction.
 * Cannot be placed — it's always held.
 */
public class AbilityShield extends AbilityBarrier {

    private float shieldWidth = 2.5f;
    private float shieldHeight = 2.5f;

    public AbilityShield() {
        super(
            new EnergyDisplayData(0xFFDD44, 0xFFAA00, true, 0.3f, 0.4f, 0.5f, 0.0f),
            new EnergyBarrierData(60.0f, true, 120, true)
        );
        this.typeId = "ability.cnpc.shield";
        this.name = "Shield";
        this.targetingMode = TargetingMode.SELF;
        this.cooldownTicks = 60;
        this.windUpTicks = 10;
        this.lockMovement = LockMode.WINDUP_AND_ACTIVE;
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
        this.windUpAnimationName = "";
        this.activeAnimationName = "";
        this.allowedBy = UserType.BOTH;

        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/shield.png",
                () -> isOuterColorEnabled() ? getOuterColor() : getInnerColor())
        };
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityEnergyBarrier createBarrierEntity(IEntityLivingBase caster, IEntityLivingBase target) {
        EnergyPanelData panelData = new EnergyPanelData(shieldWidth, shieldHeight, offsetY);
        panelData.offsetX = offsetX;
        panelData.offsetZ = offsetZ;

        float frontDist = 1.5f;
        float yawRad = (float) Math.toRadians(caster.rotationYaw);
        double spawnX = caster.posX + (-Math.sin(yawRad) * frontDist) + offsetX;
        double spawnY = caster.posY + (caster.height * 0.5f);
        double spawnZ = caster.posZ + (Math.cos(yawRad) * frontDist) + offsetZ;

        EntityEnergyPanel panel = new EntityEnergyPanel(
            caster.worldObj, caster,
            spawnX, spawnY, spawnZ, caster.rotationYaw,
            EntityEnergyPanel.PanelMode.HELD,
            displayData.copy(), lightningData.copy(), barrierData.copy(), panelData
        );
        panel.setSourceAbility(this);
        return panel;
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.SELF};
    }

    @Override
    public float getTelegraphRadius() {
        return 0; // No telegraph for shield
    }

    // ==================== VARIANTS ====================

    @Override
    public List<AbilityVariant> getVariants() {
        return Arrays.asList(
            new AbilityVariant("ability.variant.small", a -> {
                AbilityShield shield = (AbilityShield) a;
                a.setName("Small Shield");
                shield.setShieldWidth(1.5f);
                shield.setShieldHeight(1.5f);
                shield.setBarrierMaxHealth(40.0f);
            }),
            new AbilityVariant("ability.variant.large", a -> {
                AbilityShield shield = (AbilityShield) a;
                a.setName("Large Shield");
                shield.setShieldWidth(4.0f);
                shield.setShieldHeight(4.0f);
                shield.setBarrierMaxHealth(120.0f);
                shield.setBarrierDuration(200);
            })
        );
    }

    // ==================== TYPE-SPECIFIC NBT ====================

    @Override
    protected void writeBarrierTypeNBT(INbt nbt) {
        nbt.setFloat("shieldWidth", shieldWidth);
        nbt.setFloat("shieldHeight", shieldHeight);
    }

    @Override
    protected void readBarrierTypeNBT(INbt nbt) {
        this.shieldWidth = nbt.hasKey("shieldWidth") ? nbt.getFloat("shieldWidth") : 2.5f;
        this.shieldHeight = nbt.hasKey("shieldHeight") ? nbt.getFloat("shieldHeight") : 2.5f;
    }

    // ==================== GETTERS & SETTERS ====================

    public float getShieldWidth() {
        return shieldWidth;
    }

    public void setShieldWidth(float width) {
        this.shieldWidth = Math.max(0.5f, width);
    }

    public float getShieldHeight() {
        return shieldHeight;
    }

    public void setShieldHeight(float height) {
        this.shieldHeight = Math.max(0.5f, height);
    }

    // ==================== TYPE-SPECIFIC GUI ====================

    @ClientOnly
    @Override
    protected void addBarrierTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.row(
            FieldDef.floatField("gui.width", this::getShieldWidth, this::setShieldWidth).range(0.5f, 100.0f),
            FieldDef.floatField("gui.height", this::getShieldHeight, this::setShieldHeight).range(0.5f, 100.0f)
        ));
    }
}
