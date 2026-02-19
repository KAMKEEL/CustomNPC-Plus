package kamkeel.npcs.controllers.data.ability;

import net.minecraft.entity.EntityLivingBase;

/**
 * Wraps a concurrent ability execution with its own independent lifecycle.
 * Concurrent abilities skip animations, sounds, and movement/rotation locks.
 * They run their execute/tick logic in parallel with the primary ability.
 * <p>
 * Used by the chain system when an entry is concurrent-capable and concurrent-enabled.
 */
public class ConcurrentSlot {

    private final Ability ability;
    private boolean started = false;
    private boolean completed = false;

    public ConcurrentSlot(Ability ability) {
        this.ability = ability;
    }

    public Ability getAbility() {
        return ability;
    }

    public boolean isCompleted() {
        return completed;
    }

    /**
     * Start the concurrent ability. Skips windup — goes straight to ACTIVE.
     * Called once when the slot is activated.
     */
    public void start(EntityLivingBase caster, EntityLivingBase target) {
        // Skip windup for concurrent execution — fire immediately
        ability.setWindUpTicks(0);
        ability.start(target);
        started = true;

        // Ability should now be in ACTIVE phase (0 windup → immediate)
        if (ability.getPhase() == AbilityPhase.ACTIVE) {
            ability.onExecute(caster, target);
        }

        // Check if ability completed during onExecute (instant abilities like signalCompletion)
        if (ability.getPhase() == AbilityPhase.IDLE) {
            completed = true;
        }
    }

    /**
     * Tick the concurrent ability. Called every game tick from AbstractDataAbilities.
     * Handles phase transitions including burst delay → re-execute cycles.
     */
    public void tick(EntityLivingBase caster, EntityLivingBase target) {
        if (completed || !started) return;

        AbilityPhase oldPhase = ability.getPhase();
        boolean phaseChanged = ability.tick();
        AbilityPhase newPhase = ability.getPhase();

        switch (newPhase) {
            case ACTIVE:
                if (phaseChanged && oldPhase == AbilityPhase.BURST_DELAY) {
                    // Re-entering ACTIVE after burst delay → re-execute (burst re-fire)
                    ability.onExecute(caster, target);
                    if (ability.getPhase() == AbilityPhase.IDLE) {
                        completed = true;
                        return;
                    }
                }
                // Normal active tick
                ability.onActiveTick(caster, target, ability.getCurrentTick());
                if (ability.getPhase() == AbilityPhase.IDLE) {
                    completed = true;
                }
                break;

            case BURST_DELAY:
                // Waiting between bursts — nothing to do, tick() handles countdown
                break;

            case WINDUP:
                // Burst replay with windup — for concurrent, skip it by advancing
                // (windUpTicks was set to 0 at start, but burst replay may re-enter WINDUP)
                // The next tick() call will transition to ACTIVE
                break;

            case IDLE:
                completed = true;
                break;

            case DAZED:
                // Concurrent abilities don't get dazed — treat as completed
                completed = true;
                break;
        }
    }

    /**
     * Force cleanup on interrupt. Called when the NPC is interrupted or combat ends.
     */
    public void interrupt() {
        if (!completed) {
            ability.interrupt();
            completed = true;
        }
    }
}
