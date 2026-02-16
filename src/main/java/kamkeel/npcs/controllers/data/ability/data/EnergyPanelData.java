package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Groups panel-specific properties for energy wall/shield abilities.
 * Handles dimensions, positioning, and launch behavior.
 */
public class EnergyPanelData {
    public float panelWidth = 3.0f;
    public float panelHeight = 3.0f;
    public float heightOffset = 0.0f;

    // Launch properties (Wall only)
    public boolean launching = false;
    public float launchSpeed = 0.5f;
    public float launchDamage = 8.0f;
    public float launchKnockback = 2.0f;

    public EnergyPanelData() {}

    public EnergyPanelData(float panelWidth, float panelHeight, float heightOffset) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.heightOffset = heightOffset;
    }

    // ==================== GETTERS & SETTERS ====================

    public float getPanelWidth() { return panelWidth; }
    public void setPanelWidth(float panelWidth) { this.panelWidth = Math.max(0.5f, panelWidth); }

    public float getPanelHeight() { return panelHeight; }
    public void setPanelHeight(float panelHeight) { this.panelHeight = Math.max(0.5f, panelHeight); }

    public float getHeightOffset() { return heightOffset; }
    public void setHeightOffset(float heightOffset) { this.heightOffset = heightOffset; }

    public boolean isLaunching() { return launching; }
    public void setLaunching(boolean launching) { this.launching = launching; }

    public float getLaunchSpeed() { return launchSpeed; }
    public void setLaunchSpeed(float launchSpeed) { this.launchSpeed = Math.max(0.1f, launchSpeed); }

    public float getLaunchDamage() { return launchDamage; }
    public void setLaunchDamage(float launchDamage) { this.launchDamage = Math.max(0, launchDamage); }

    public float getLaunchKnockback() { return launchKnockback; }
    public void setLaunchKnockback(float launchKnockback) { this.launchKnockback = Math.max(0, launchKnockback); }

    // ==================== NBT ====================

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("panelWidth", panelWidth);
        nbt.setFloat("panelHeight", panelHeight);
        nbt.setFloat("panelHeightOffset", heightOffset);
        nbt.setBoolean("panelLaunching", launching);
        nbt.setFloat("panelLaunchSpeed", launchSpeed);
        nbt.setFloat("panelLaunchDamage", launchDamage);
        nbt.setFloat("panelLaunchKnockback", launchKnockback);
    }

    public void readNBT(NBTTagCompound nbt) {
        panelWidth = nbt.hasKey("panelWidth") ? nbt.getFloat("panelWidth") : 3.0f;
        panelHeight = nbt.hasKey("panelHeight") ? nbt.getFloat("panelHeight") : 3.0f;
        heightOffset = nbt.getFloat("panelHeightOffset");
        launching = nbt.getBoolean("panelLaunching");
        launchSpeed = nbt.hasKey("panelLaunchSpeed") ? nbt.getFloat("panelLaunchSpeed") : 0.5f;
        launchDamage = nbt.hasKey("panelLaunchDamage") ? nbt.getFloat("panelLaunchDamage") : 8.0f;
        launchKnockback = nbt.hasKey("panelLaunchKnockback") ? nbt.getFloat("panelLaunchKnockback") : 2.0f;
    }

    public EnergyPanelData copy() {
        EnergyPanelData copy = new EnergyPanelData(panelWidth, panelHeight, heightOffset);
        copy.launching = launching;
        copy.launchSpeed = launchSpeed;
        copy.launchDamage = launchDamage;
        copy.launchKnockback = launchKnockback;
        return copy;
    }
}
