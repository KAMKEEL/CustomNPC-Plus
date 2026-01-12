package kamkeel.npcs.controllers.data.ability;

public enum AbilityPhase {
    IDLE,       // Not executing
    WINDUP,     // Charging up (telegraph visible)
    ACTIVE,     // Executing (damage happens)
    RECOVERY    // Finishing animation
}
