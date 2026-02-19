package kamkeel.npcs.controllers.data.ability;

/**
 * Tracks the state of a single active toggle for an entity.
 * Stored in AbstractDataAbilities.activeToggles map.
 * <p>
 * State is 1-based: state 1 = first ON state, state 2 = second, etc.
 * State 0 means OFF (entry should not exist in the map).
 */
public class ToggleEntry {
    private final Ability ability;
    private int tickCount;
    private int state;

    public ToggleEntry(Ability ability, int state) {
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
