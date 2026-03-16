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
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import noppes.npcs.api.ability.type.IAbilityGuard;
import java.util.List;

/**
 * Guard ability: Defensive stance that reduces incoming damage.
 */
public class AbilityGuard extends AbilityDefend implements IAbilityGuard {

    private float damageReduction = 0.5f;

    public AbilityGuard() {
        this.typeId = "ability.cnpc.guard";
        this.name = "Guard";
        this.cooldownTicks = 0;
        this.allowedBy = UserType.BOTH;

        this.activeAnimationName = "Ability_Guard_Active";
        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/guard.png")
        };
    }

    // ═══════════════════════════════════════════════════════════════════
    // DEFEND HOOKS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Guard accepts ALL damage types (melee, projectile, magic, fire, IExplosion).
     */
    @Override
    protected boolean isValidDamageSource(IDamageSource source) {
        return true;
    }

    @Override
    protected float performDefend(IEntityLivingBase attacker, float amount) {
        return Math.max(0, amount * (1.0f - damageReduction));
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void writeSubTypeNBT(INbt nbt) {
        nbt.setFloat("damageReduction", damageReduction);
    }

    @Override
    protected void readSubTypeNBT(INbt nbt) {
        this.damageReduction = nbt.hasKey("damageReduction") ? nbt.getFloat("damageReduction") : 0.5f;
    }

    // ═══════════════════════════════════════════════════════════════════
    // GUI
    // ═══════════════════════════════════════════════════════════════════

    @ClientOnly
    @Override
    protected void getTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.floatField("ability.damageReduction", this::getDamageReduction, this::setDamageReduction));

        FieldDef.modifyVisibility(defs, "ability.windUpAnimation", () -> false);
        FieldDef.modifyVisibility(defs, "ability.windUpSound", () -> false);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public float getDamageReduction() {
        return damageReduction;
    }

    @Override
    public void setDamageReduction(float damageReduction) {
        this.damageReduction = damageReduction;
    }
}
