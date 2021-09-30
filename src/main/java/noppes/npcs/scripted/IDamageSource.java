//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import net.minecraft.util.DamageSource;
import noppes.npcs.scripted.entity.IEntity;

public interface IDamageSource {
    String getType();

    boolean isUnblockable();

    boolean isProjectile();

    IEntity getTrueSource();

    IEntity getImmediateSource();

    DamageSource getMCDamageSource();
}
