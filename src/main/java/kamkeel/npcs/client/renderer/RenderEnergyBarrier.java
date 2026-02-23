package kamkeel.npcs.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.entity.EntityAbilityBarrier;

/**
 * Intermediate renderer base for energy barrier entities (Dome, Panel).
 * Provides shared hit flash alpha computation.
 */
@SideOnly(Side.CLIENT)
public abstract class RenderEnergyBarrier extends RenderEnergyAbility {

    /**
     * Compute the alpha boost from a hit flash effect.
     * Flash value decays from 4 to 0, producing alpha from 0.3 to 0.
     */
    protected float computeFlashAlpha(EntityAbilityBarrier barrier) {
        byte flash = barrier.getHitFlash();
        if (flash <= 0) return 0.0f;
        return flash / 4.0f * 0.3f;
    }
}
