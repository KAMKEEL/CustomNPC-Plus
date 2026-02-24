package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilityPanel;
import noppes.npcs.api.entity.IEnergyPanel;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergyPanel<T extends EntityAbilityPanel> extends ScriptEnergyBarrier<T> implements IEnergyPanel {

    public ScriptEnergyPanel(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_PANEL;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_PANEL || super.typeOf(type);
    }

    @Override
    public int getBarrierType() {
        return 1;
    }

    // ==================== PANEL-SPECIFIC ====================

    public float getPanelWidth() {
        return entity.getPanelData().panelWidth;
    }

    public void setPanelWidth(float width) {
        entity.getPanelData().setPanelWidth(width);
    }

    public float getPanelHeight() {
        return entity.getPanelData().panelHeight;
    }

    public void setPanelHeight(float height) {
        entity.getPanelData().setPanelHeight(height);
    }

    public float getPanelYaw() {
        return entity.getPanelYaw();
    }

    public int getPanelMode() {
        return entity.getMode().ordinal();
    }

    public boolean isLaunched() {
        return entity.getMode() == EntityAbilityPanel.PanelMode.LAUNCHED;
    }
}
