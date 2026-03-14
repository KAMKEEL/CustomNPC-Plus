package kamkeel.npcs.controllers.data.ability.data;

import kamkeel.npcs.controllers.data.ability.enums.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyAnchorData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import noppes.npcs.api.INbt;

/**
 * Per-projectile data: anchor point + optional color override.
 * All projectiles share the primary EnergyDisplayData; this class
 * allows individual projectiles to override just the inner/outer colors.
 */
public class ProjectileData {
    public EnergyAnchorData anchor;
    public boolean colorOverride = false;
    public int innerColor = 0xFFFFFF;
    public int outerColor = 0x8888FF;

    public ProjectileData(AnchorPoint defaultAnchor) {
        this.anchor = new EnergyAnchorData(defaultAnchor);
    }

    /**
     * Returns the display data to use for this projectile.
     * If colorOverride is false, returns the primary display data directly.
     * If colorOverride is true, returns a copy with overridden inner/outer colors.
     */
    public EnergyDisplayData resolveDisplay(EnergyDisplayData primary) {
        // Always return a copy so entity mutations never bleed back into the ability template.
        EnergyDisplayData resolved = primary.copy();
        if (colorOverride) {
            resolved.innerColor = this.innerColor;
            resolved.outerColor = this.outerColor;
        }
        return resolved;
    }

    public void writeNBT(INbt nbt) {
        // OLD: anchor.writeNBT(new NBTWrapper(nbt));
        anchor.writeNBT(nbt);
        nbt.setBoolean("colorOverride", colorOverride);
        if (colorOverride) {
            nbt.setInteger("overrideInnerColor", innerColor);
            nbt.setInteger("overrideOuterColor", outerColor);
        }
    }

    public void readNBT(INbt nbt) {
        // OLD: anchor.readNBT(new NBTWrapper(nbt));
        anchor.readNBT(nbt);
        colorOverride = nbt.getBoolean("colorOverride");
        if (colorOverride) {
            innerColor = nbt.getInteger("overrideInnerColor");
            outerColor = nbt.getInteger("overrideOuterColor");
        }
    }

    public ProjectileData copy() {
        ProjectileData copy = new ProjectileData(anchor.anchorPoint);
        copy.anchor = anchor.copy();
        copy.colorOverride = colorOverride;
        copy.innerColor = innerColor;
        copy.outerColor = outerColor;
        return copy;
    }
}
