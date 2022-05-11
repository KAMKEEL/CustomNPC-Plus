//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.util.DamageSource;
import noppes.npcs.scripted.interfaces.entity.IEntity;

public interface IDamageSource {
    String getType();

    boolean isUnblockable();

    boolean isProjectile();

    IEntity getTrueSource();

    IEntity getImmediateSource();

    DamageSource getMCDamageSource();
}
