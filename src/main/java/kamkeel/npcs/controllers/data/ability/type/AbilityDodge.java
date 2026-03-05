package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import noppes.npcs.controllers.AnimationController;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.api.ability.type.IAbilityDodge;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.ArrayList;
import java.util.List;

/**
 * Dodge ability: Completely cancels incoming attacks and plays a random dodge animation.
 */
public class AbilityDodge extends AbilityDefend implements IAbilityDodge {

    private int dodgeAnimation1Id = -1;
    private String dodgeAnimation1Name = "";
    private int dodgeAnimation2Id = -1;
    private String dodgeAnimation2Name = "";
    private int dodgeAnimation3Id = -1;
    private String dodgeAnimation3Name = "";

    public AbilityDodge() {
        this.typeId = "ability.cnpc.dodge";
        this.name = "Dodge";
        this.cooldownTicks = 0;
        this.windUpTicks = 10;
        this.allowedBy = UserType.BOTH;

        this.activeAnimationName = "";
        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/guard.png")
        };
    }

    // ═══════════════════════════════════════════════════════════════════
    // DEFEND HOOKS
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected float performDefend(EntityLivingBase attacker, float amount) {
        return 0;
    }

    @Override
    protected Animation getDefendAnimation() {
        return getRandomDodgeAnimation();
    }

    /**
     * Returns a random dodge animation from the configured slots (non-empty only).
     */
    public Animation getRandomDodgeAnimation() {
        if (AnimationController.Instance == null) return null;

        List<Animation> available = new ArrayList<>();
        Animation a1 = getDodgeAnimation(dodgeAnimation1Id, dodgeAnimation1Name);
        Animation a2 = getDodgeAnimation(dodgeAnimation2Id, dodgeAnimation2Name);
        Animation a3 = getDodgeAnimation(dodgeAnimation3Id, dodgeAnimation3Name);
        if (a1 != null) available.add(a1);
        if (a2 != null) available.add(a2);
        if (a3 != null) available.add(a3);

        if (available.isEmpty()) return null;
        return available.get((int) (Math.random() * available.size()));
    }

    private Animation getDodgeAnimation(int id, String name) {
        if (id >= 0) {
            return (Animation) AnimationController.Instance.get(id);
        }
        if (name != null && !name.isEmpty()) {
            return (Animation) AnimationController.Instance.get(name, true);
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void writeSubTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("dodgeAnimation1Id", dodgeAnimation1Id);
        nbt.setString("dodgeAnimation1Name", resolveAnimationName(dodgeAnimation1Id, dodgeAnimation1Name));
        nbt.setInteger("dodgeAnimation2Id", dodgeAnimation2Id);
        nbt.setString("dodgeAnimation2Name", resolveAnimationName(dodgeAnimation2Id, dodgeAnimation2Name));
        nbt.setInteger("dodgeAnimation3Id", dodgeAnimation3Id);
        nbt.setString("dodgeAnimation3Name", resolveAnimationName(dodgeAnimation3Id, dodgeAnimation3Name));
    }

    @Override
    protected void readSubTypeNBT(NBTTagCompound nbt) {
        this.dodgeAnimation1Id = nbt.getInteger("dodgeAnimation1Id");
        this.dodgeAnimation1Name = nbt.getString("dodgeAnimation1Name");
        this.dodgeAnimation2Id = nbt.getInteger("dodgeAnimation2Id");
        this.dodgeAnimation2Name = nbt.getString("dodgeAnimation2Name");
        this.dodgeAnimation3Id = nbt.getInteger("dodgeAnimation3Id");
        this.dodgeAnimation3Name = nbt.getString("dodgeAnimation3Name");
    }

    // ═══════════════════════════════════════════════════════════════════
    // GUI
    // ═══════════════════════════════════════════════════════════════════

    @SideOnly(Side.CLIENT)
    @Override
    protected void getTypeDefinitions(List<FieldDef> defs) {
        FieldDef.insertAfter(defs, "ability.dazedAnimation",
            FieldDef.section("ability.section.dodgeAnimations").tab("Effects"));

        FieldDef.insertAfter(defs, "ability.section.dodgeAnimations",
            FieldDef.animSubGui("ability.dodgeAnimation1",
                    this::getDodgeAnimation1Id, this::setDodgeAnimation1Id,
                    this::getDodgeAnimation1Name, this::setDodgeAnimation1Name)
                .tab("Effects"));

        FieldDef.insertAfter(defs, "ability.dodgeAnimation1",
            FieldDef.animSubGui("ability.dodgeAnimation2",
                    this::getDodgeAnimation2Id, this::setDodgeAnimation2Id,
                    this::getDodgeAnimation2Name, this::setDodgeAnimation2Name)
                .tab("Effects"));

        FieldDef.insertAfter(defs, "ability.dodgeAnimation2",
            FieldDef.animSubGui("ability.dodgeAnimation3",
                    this::getDodgeAnimation3Id, this::setDodgeAnimation3Id,
                    this::getDodgeAnimation3Name, this::setDodgeAnimation3Name)
                .tab("Effects"));

        FieldDef.modifyVisibility(defs, "ability.windUpAnimation", () -> false);
        FieldDef.modifyVisibility(defs, "ability.windUpSound", () -> false);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public int getDodgeAnimation1Id() {
        return dodgeAnimation1Id;
    }

    @Override
    public void setDodgeAnimation1Id(int animationId) {
        this.dodgeAnimation1Id = animationId;
    }

    public String getDodgeAnimation1Name() {
        return dodgeAnimation1Name;
    }

    public void setDodgeAnimation1Name(String name) {
        this.dodgeAnimation1Name = name != null ? name : "";
    }

    @Override
    public int getDodgeAnimation2Id() {
        return dodgeAnimation2Id;
    }

    @Override
    public void setDodgeAnimation2Id(int animationId) {
        this.dodgeAnimation2Id = animationId;
    }

    public String getDodgeAnimation2Name() {
        return dodgeAnimation2Name;
    }

    public void setDodgeAnimation2Name(String name) {
        this.dodgeAnimation2Name = name != null ? name : "";
    }

    @Override
    public int getDodgeAnimation3Id() {
        return dodgeAnimation3Id;
    }

    @Override
    public void setDodgeAnimation3Id(int animationId) {
        this.dodgeAnimation3Id = animationId;
    }

    public String getDodgeAnimation3Name() {
        return dodgeAnimation3Name;
    }

    public void setDodgeAnimation3Name(String name) {
        this.dodgeAnimation3Name = name != null ? name : "";
    }
}
