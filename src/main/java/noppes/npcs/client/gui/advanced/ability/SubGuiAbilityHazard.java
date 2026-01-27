package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.type.AbilityHazard;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;

/**
 * GUI for configuring Hazard ability type-specific settings.
 */
public class SubGuiAbilityHazard extends SubGuiAbilityConfig {

    private final AbilityHazard hazard;

    public SubGuiAbilityHazard(AbilityHazard ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.hazard = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Duration + Shape
        addLabel(new GuiNpcLabel(99, "ability.duration", labelX, y + 5));
        GuiNpcTextField durationField = createIntField(99, fieldX, y, 50, hazard.getDurationTicks());
        durationField.setMinMaxDefault(1, 2000, 100);
        addTextField(durationField);

        addLabel(new GuiNpcLabel(100, "ability.shape", col2LabelX, y + 5));
        String[] shapes = {"Circle", "Ring", "Cone"};
        GuiNpcButton shapeBtn = new GuiNpcButton(100, col2FieldX, y, 55, 20, shapes, hazard.getShape().ordinal());
        shapeBtn.setHoverText("ability.hover.shape");
        addButton(shapeBtn);

        y += 24;

        // Row 2: Placement
        addLabel(new GuiNpcLabel(101, "ability.placement", labelX, y + 5));
        String[] placements = {"Caster", "Target", "FollowCast", "FollowTgt"};
        GuiNpcButton placementBtn = new GuiNpcButton(101, fieldX, y, 70, 20, placements, hazard.getPlacement().ordinal());
        placementBtn.setHoverText("ability.hover.placement");
        addButton(placementBtn);

        y += 24;

        // Row 2: Radius + Inner Radius
        addLabel(new GuiNpcLabel(102, "ability.radius", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, hazard.getRadius()));

        addLabel(new GuiNpcLabel(103, "ability.innerRadius", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, hazard.getInnerRadius()));

        y += 24;

        // Row 3: Damage Per Second + Damage Interval
        addLabel(new GuiNpcLabel(104, "ability.dmgPerSec", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, hazard.getDamagePerSecond()));

        addLabel(new GuiNpcLabel(105, "ability.dmgInterval", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, hazard.getDamageInterval()));

        y += 24;

        // Row 4: Slowness Level + Debuff Duration
        addLabel(new GuiNpcLabel(106, "ability.slownessLvl", labelX, y + 5));
        addTextField(createIntField(106, fieldX, y, 50, hazard.getSlownessLevel()));

        addLabel(new GuiNpcLabel(107, "ability.debuffDur", col2LabelX, y + 5));
        addTextField(createIntField(107, col2FieldX, y, 50, hazard.getDebuffDuration()));

        y += 24;

        // Row 5: Poison Level + Affects Caster
        addLabel(new GuiNpcLabel(108, "ability.poisonLvl", labelX, y + 5));
        addTextField(createIntField(108, fieldX, y, 50, hazard.getPoisonLevel()));

        addLabel(new GuiNpcLabel(109, "ability.affectsCaster", col2LabelX, y + 5));
        GuiNpcButton affectsCasterBtn = new GuiNpcButton(109, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, hazard.isAffectsCaster() ? 1 : 0);
        affectsCasterBtn.setHoverText("ability.hover.affectsCaster");
        addButton(affectsCasterBtn);

        y += 24;

        // Row 6: Effects button
        addButton(new GuiNpcButton(150, labelX, y, 80, 20, "ability.effects"));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 100:
                hazard.setShape(AbilityHazard.HazardShape.values()[value]);
                break;
            case 101:
                hazard.setPlacement(AbilityHazard.PlacementMode.values()[value]);
                break;
            case 109:
                hazard.setAffectsCaster(value == 1);
                break;
            case 150:
                setSubGui(new SubGuiAbilityEffects(hazard.getEffects()));
                break;
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        super.subGuiClosed(subgui);
        if (subgui instanceof SubGuiAbilityEffects) {
            SubGuiAbilityEffects effectsGui = (SubGuiAbilityEffects) subgui;
            List<AbilityEffect> result = effectsGui.getResult();
            if (result != null) {
                hazard.setEffects(result);
            }
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 99:
                hazard.setDurationTicks(field.getInteger());
                break;
            case 102:
                hazard.setRadius(parseFloat(field, hazard.getRadius()));
                break;
            case 103:
                hazard.setInnerRadius(parseFloat(field, hazard.getInnerRadius()));
                break;
            case 104:
                hazard.setDamagePerSecond(parseFloat(field, hazard.getDamagePerSecond()));
                break;
            case 105:
                hazard.setDamageInterval(field.getInteger());
                break;
            case 106:
                hazard.setSlownessLevel(field.getInteger());
                break;
            case 107:
                hazard.setDebuffDuration(field.getInteger());
                break;
            case 108:
                hazard.setPoisonLevel(field.getInteger());
                break;
        }
    }
}
