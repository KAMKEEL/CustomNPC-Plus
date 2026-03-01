package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.type.IAbilityGuard;
import noppes.npcs.client.gui.builder.FieldDef;

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

    @Override
    protected float performDefend(EntityLivingBase attacker, float amount) {
        return Math.max(0, amount * (1.0f - damageReduction));
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void writeSubTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("damageReduction", damageReduction);
    }

    @Override
    protected void readSubTypeNBT(NBTTagCompound nbt) {
        this.damageReduction = nbt.hasKey("damageReduction") ? nbt.getFloat("damageReduction") : 0.5f;
    }

    // ═══════════════════════════════════════════════════════════════════
    // GUI
    // ═══════════════════════════════════════════════════════════════════

    @SideOnly(Side.CLIENT)
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
