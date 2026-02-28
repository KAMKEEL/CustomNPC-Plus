package kamkeel.npcs.controllers.data.ability.data;

import kamkeel.npcs.controllers.data.ability.Ability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Icon data for abilities, stored in the ability's customData NBT under "AbilityIcon".
 * Supports up to 3 layers, each with its own texture, UV coordinates, and tint color.
 * Width, height, and scale are shared across all layers.
 */
public class AbilityIconData {
    public static final String NBT_KEY = "AbilityIcon";
    public static final int MAX_LAYERS = 3;

    private final NBTTagCompound customData;

    // Shared across all layers
    public int width = 32;
    public int height = 32;
    public float scale = 1.0f;

    /** Whether a custom icon override is active. Default false = use type's default icon. */
    private boolean enabled = false;

    /** Number of active layers (1-3). */
    private int layerCount = 1;

    /** Layer data (always 3 slots allocated). */
    private Layer[] layers = { new Layer(), new Layer(), new Layer() };

    /** Per-state icon UV overrides for layer 0 (0-indexed, maps to toggle states 1, 2, ...). */
    private int[][] stateIcons = null;

    // ═══════════════════════════════════════════════════════════════════
    // LAYER INNER CLASS
    // ═══════════════════════════════════════════════════════════════════

    public static class Layer {
        public String texture = "";
        public int iconX = 0;
        public int iconY = 0;
        public int tintColor = 0xFFFFFF;

        public Layer() {}

        public boolean hasTexture() {
            return texture != null && !texture.isEmpty();
        }

        public void writeToNBT(NBTTagCompound nbt) {
            nbt.setString("Texture", texture);
            nbt.setInteger("IconX", iconX);
            nbt.setInteger("IconY", iconY);
            nbt.setInteger("TintColor", tintColor);
        }

        public void readFromNBT(NBTTagCompound nbt) {
            texture = nbt.getString("Texture");
            iconX = nbt.getInteger("IconX");
            iconY = nbt.getInteger("IconY");
            tintColor = nbt.hasKey("TintColor") ? nbt.getInteger("TintColor") : 0xFFFFFF;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONSTRUCTION
    // ═══════════════════════════════════════════════════════════════════

    private AbilityIconData(NBTTagCompound customData) {
        this.customData = customData;
    }

    /**
     * Create an AbilityIconData instance from a raw customData compound.
     * Reads existing values if present, otherwise uses defaults.
     */
    public static AbilityIconData fromCustomData(NBTTagCompound customData) {
        AbilityIconData icon = new AbilityIconData(customData);
        if (customData.hasKey(NBT_KEY)) {
            icon.readFromNBT(customData.getCompoundTag(NBT_KEY));
        }
        return icon;
    }

    /**
     * Create an AbilityIconData instance from an ability's customData.
     */
    public static AbilityIconData fromAbility(Ability ability) {
        return fromCustomData(ability.getCustomData());
    }

    /**
     * Create an AbilityIconData instance from a chained ability's customData.
     */
    public static AbilityIconData fromChainedAbility(ChainedAbility chain) {
        return fromCustomData(chain.getCustomData());
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Write current state back to the customData NBT.
     */
    public void save() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        customData.setTag(NBT_KEY, tag);
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("Enabled", enabled);
        nbt.setInteger("Width", width);
        nbt.setInteger("Height", height);
        nbt.setFloat("Scale", scale);
        nbt.setInteger("LayerCount", layerCount);

        NBTTagList layerList = new NBTTagList();
        for (int i = 0; i < MAX_LAYERS; i++) {
            NBTTagCompound layerNBT = new NBTTagCompound();
            layers[i].writeToNBT(layerNBT);
            layerList.appendTag(layerNBT);
        }
        nbt.setTag("Layers", layerList);

        if (stateIcons != null && stateIcons.length > 0) {
            NBTTagList list = new NBTTagList();
            for (int[] pair : stateIcons) {
                NBTTagCompound comp = new NBTTagCompound();
                comp.setInteger("IconX", pair[0]);
                comp.setInteger("IconY", pair[1]);
                list.appendTag(comp);
            }
            nbt.setTag("StateIcons", list);
        }
    }

    public void readFromNBT(NBTTagCompound nbt) {
        enabled = nbt.getBoolean("Enabled");
        width = nbt.getInteger("Width");
        if (width <= 0) width = 32;
        height = nbt.getInteger("Height");
        if (height <= 0) height = 32;
        scale = nbt.getFloat("Scale");
        if (scale <= 0) scale = 1.0f;

        layerCount = nbt.getInteger("LayerCount");
        if (layerCount < 1) layerCount = 1;
        if (layerCount > MAX_LAYERS) layerCount = MAX_LAYERS;

        if (nbt.hasKey("Layers")) {
            NBTTagList layerList = nbt.getTagList("Layers", 10);
            for (int i = 0; i < MAX_LAYERS; i++) {
                layers[i] = new Layer();
                if (i < layerList.tagCount()) {
                    layers[i].readFromNBT(layerList.getCompoundTagAt(i));
                }
            }
        }

        if (nbt.hasKey("StateIcons")) {
            NBTTagList list = nbt.getTagList("StateIcons", 10);
            stateIcons = new int[list.tagCount()][2];
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound comp = list.getCompoundTagAt(i);
                stateIcons[i][0] = comp.getInteger("IconX");
                stateIcons[i][1] = comp.getInteger("IconY");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // LAYER API
    // ═══════════════════════════════════════════════════════════════════

    public int getLayerCount() {
        return layerCount;
    }

    public void setLayerCount(int count) {
        this.layerCount = Math.max(1, Math.min(MAX_LAYERS, count));
        save();
    }

    public Layer getLayer(int index) {
        return (index >= 0 && index < MAX_LAYERS) ? layers[index] : layers[0];
    }

    public void setLayerTexture(int index, String tex) {
        if (index >= 0 && index < MAX_LAYERS) {
            layers[index].texture = tex != null ? tex : "";
            save();
        }
    }

    public void setLayerIconX(int index, int x) {
        if (index >= 0 && index < MAX_LAYERS) {
            layers[index].iconX = Math.max(0, x);
            save();
        }
    }

    public void setLayerIconY(int index, int y) {
        if (index >= 0 && index < MAX_LAYERS) {
            layers[index].iconY = Math.max(0, y);
            save();
        }
    }

    public void setLayerTintColor(int index, int color) {
        if (index >= 0 && index < MAX_LAYERS) {
            layers[index].tintColor = color & 0xFFFFFF;
            save();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SHARED PROPERTY GETTERS/SETTERS (with auto-save)
    // ═══════════════════════════════════════════════════════════════════

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = Math.max(1, width);
        save();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(1, height);
        save();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = Math.max(0.1f, scale);
        save();
    }

    // ═══════════════════════════════════════════════════════════════════
    // PER-STATE ICON OVERRIDES (applies to layer 0)
    // ═══════════════════════════════════════════════════════════════════

    public void setStateIcons(int[][] stateIcons) {
        this.stateIcons = stateIcons;
        save();
    }

    /**
     * Get iconX for a specific toggle state (layer 0).
     * Falls back to layer 0 iconX if no state override exists.
     * @param state 1-indexed toggle state (0 = off/default)
     */
    public int getIconXForState(int state) {
        if (state > 0 && stateIcons != null && state - 1 < stateIcons.length) {
            return stateIcons[state - 1][0];
        }
        return layers[0].iconX;
    }

    /**
     * Get iconY for a specific toggle state (layer 0).
     * Falls back to layer 0 iconY if no state override exists.
     * @param state 1-indexed toggle state (0 = off/default)
     */
    public int getIconYForState(int state) {
        if (state > 0 && stateIcons != null && state - 1 < stateIcons.length) {
            return stateIcons[state - 1][1];
        }
        return layers[0].iconY;
    }
}
