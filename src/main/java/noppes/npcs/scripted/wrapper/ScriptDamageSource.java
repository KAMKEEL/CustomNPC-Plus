//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.wrapper;

import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.interfaces.IDamageSource;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.interfaces.IEntity;

public class ScriptDamageSource implements IDamageSource {
    private DamageSource source;

    public ScriptDamageSource(DamageSource source) {
        this.source = source;
    }

    public String getType() {
        return this.source.getDamageType();
    }

    public boolean isUnblockable() {
        return this.source.isUnblockable();
    }

    public boolean isProjectile() {
        return this.source.isProjectile();
    }

    public DamageSource getMCDamageSource() {
        return this.source;
    }

    public IEntity getTrueSource() {
        return ScriptController.Instance.getScriptForEntity(this.source.getEntity());
    }

    public IEntity getImmediateSource() {
        return ScriptController.Instance.getScriptForEntity(this.source.getSourceOfDamage());
    }
}
