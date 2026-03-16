package kamkeel.npcs.controllers.data.ability.data.entry;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.ability.Ability;

/**
 * Tracks the state of a single active toggle for an IEntity.
 * Stored in AbstractDataAbilities.activeToggles map.
 * <p>
 * State is 1-based: state 1 = first ON state, state 2 = second, etc.
 * State 0 means OFF (entry should not exist in the map).
 */
public class AbilityToggleEntry {
    private final Ability ability;
    private int tickCount;
    private int state;

    public AbilityToggleEntry(Ability ability, int state) {
        this.ability = ability;
        this.tickCount = 0;
        this.state = Math.max(1, state);
    }

    public Ability getAbility() {
        return ability;
    }

    public int getTickCount() {
        return tickCount;
    }

    public void incrementTick() {
        tickCount++;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = Math.max(1, state);
    }
}
