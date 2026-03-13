package kamkeel.npcs.controllers.data.ability.enums;

public enum AbilityPhase {
    IDLE,       // Not executing
    WINDUP,     // Charging up (telegraph visible)
    ACTIVE,     // Executing (damage happens)
    DAZED,      // Stunned after interrupt during WINDUP (cannot attack)
    BURST_DELAY // Waiting between burst repetitions (free movement/rotation)
}
