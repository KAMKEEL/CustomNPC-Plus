package kamkeel.npcs.controllers.data.ability;

/**
 * Tracks the state of a single active toggle for an entity.
 * Stored in AbstractDataAbilities.activeToggles map.
 */
public class ToggleEntry {
    private final Ability ability;
    private int tickCount;

    public ToggleEntry(Ability ability) {
        this.ability = ability;
        this.tickCount = 0;
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
}
