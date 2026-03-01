package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergySweeper;

public class ScriptEnergySweeper<T extends EntityEnergySweeper> extends ScriptEnergyAbility<T> {
    public ScriptEnergySweeper(T entity) {
        super(entity);
    }
}
