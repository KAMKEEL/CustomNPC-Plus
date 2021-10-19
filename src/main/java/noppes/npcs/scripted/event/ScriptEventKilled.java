package noppes.npcs.scripted.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.entity.ScriptLivingBase;

public class ScriptEventKilled extends ScriptEvent {

    private ScriptLivingBase source;
    private DamageSource damagesource;

    public ScriptEventKilled(EntityLivingBase target, DamageSource damagesource) {
        this.damagesource = damagesource;
        this.source = (ScriptLivingBase) ScriptController.Instance.getScriptForEntity(target);
    }

    /**
     * @return The source of the damage
     */
    public ScriptLivingBase getSource() {
        return source;
    }

    /**
     * @return Returns the damage type
     */
    public String getType() {
        return damagesource.damageType;
    }
}
