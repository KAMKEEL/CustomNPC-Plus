package noppes.npcs.client.gui.advanced.ability;

import kamkeel.npcs.controllers.data.ability.type.AbilityProjectile;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * GUI for configuring Projectile ability type-specific settings.
 */
public class SubGuiAbilityProjectile extends SubGuiAbilityConfig {

    private final AbilityProjectile projectile;

    public SubGuiAbilityProjectile(AbilityProjectile ability, IAbilityConfigCallback callback) {
        super(ability, callback);
        this.projectile = ability;
    }

    @Override
    protected void initTypeTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Speed
        addLabel(new GuiNpcLabel(100, "enchantment.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, projectile.getDamage()));

        addLabel(new GuiNpcLabel(101, "stats.speed", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, projectile.getSpeed()));

        y += 24;

        // Row 2: Knockback + Projectile Type
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, projectile.getKnockback()));

        addLabel(new GuiNpcLabel(103, "ability.projType", col2LabelX, y + 5));
        String[] projTypes = {"fireball", "arrow", "magic"};
        int typeIdx = 0;
        String currType = projectile.getProjectileType();
        for (int i = 0; i < projTypes.length; i++) {
            if (projTypes[i].equals(currType)) {
                typeIdx = i;
                break;
            }
        }
        addButton(new GuiNpcButton(103, col2FieldX, y, 60, 20, projTypes, typeIdx));

        y += 24;

        // Row 3: Explosive + Explosion Radius
        addLabel(new GuiNpcLabel(104, "ability.explosive", labelX, y + 5));
        addButton(new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, projectile.isExplosive() ? 1 : 0));

        addLabel(new GuiNpcLabel(105, "ability.explosionRad", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, projectile.getExplosionRadius()));

        y += 24;

        // Row 4: Homing + Homing Strength
        addLabel(new GuiNpcLabel(106, "ability.homing", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, projectile.isHoming() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.homingStr", col2LabelX, y + 5));
        addTextField(createFloatField(107, col2FieldX, y, 50, projectile.getHomingStrength()));
    }

    @Override
    protected void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();
        switch (id) {
            case 103:
                String[] projTypes = {"fireball", "arrow", "magic"};
                projectile.setProjectileType(projTypes[value]);
                break;
            case 104: projectile.setExplosive(value == 1); break;
            case 106: projectile.setHoming(value == 1); break;
        }
    }

    @Override
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        switch (id) {
            case 100: projectile.setDamage(parseFloat(field, projectile.getDamage())); break;
            case 101: projectile.setSpeed(parseFloat(field, projectile.getSpeed())); break;
            case 102: projectile.setKnockback(parseFloat(field, projectile.getKnockback())); break;
            case 105: projectile.setExplosionRadius(parseFloat(field, projectile.getExplosionRadius())); break;
            case 107: projectile.setHomingStrength(parseFloat(field, projectile.getHomingStrength())); break;
        }
    }
}
