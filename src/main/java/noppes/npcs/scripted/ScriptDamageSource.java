package noppes.npcs.scripted;

import net.minecraft.util.DamageSource;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.entity.IEntity;

public class ScriptDamageSource implements IDamageSource {
    private final DamageSource source;

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
        return NpcAPI.Instance().getIEntity(this.source.getEntity());
    }

    public IEntity getImmediateSource() {
        return NpcAPI.Instance().getIEntity(this.source.getSourceOfDamage());
    }
}
