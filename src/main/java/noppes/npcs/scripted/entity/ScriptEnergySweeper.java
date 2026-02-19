package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilitySweeper;

public class ScriptEnergySweeper<T extends EntityAbilitySweeper> extends ScriptEnergyAbility<T> {
    public ScriptEnergySweeper(T entity) {
        super(entity);
    }
}
