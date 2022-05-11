//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.util.DamageSource;

public interface IDamageSource {
    String getType();

    boolean isUnblockable();

    boolean isProjectile();

    IEntity getTrueSource();

    IEntity getImmediateSource();

    DamageSource getMCDamageSource();
}
