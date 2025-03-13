package noppes.npcs.scripted.scoreboard;

import net.minecraft.scoreboard.ScoreObjective;
import noppes.npcs.api.scoreboard.IScoreboardObjective;

public class ScriptScoreboardObjective implements IScoreboardObjective {
    private final ScoreObjective objective;

    public ScriptScoreboardObjective(ScoreObjective objective) {
        this.objective = objective;
    }

    /**
     * @return Returns objective name
     */
    public String getName() {
        return objective.getName();
    }

    /**
     * @return Returns display name
     */
    public String getDisplayName() {
        return objective.getDisplayName();
    }

    /**
     * @param name Name used for display (1-32 chars)
     * @since 1.7.10c
     */
    public void setDisplayName(String name) {
        if (name.length() > 0 && name.length() <= 32)
            objective.setDisplayName(name);
    }

    /**
     * @return Returns the criteria string
     * @since 1.7.10c
     */
    public String getCriteria() {
        return objective.getCriteria().func_96636_a();
    }

    /**
     * @return Return whether or not the objective value can be changed. E.g. player health can't be changed
     */
    public boolean isReadyOnly() {
        return objective.getCriteria().isReadOnly();
    }

}
