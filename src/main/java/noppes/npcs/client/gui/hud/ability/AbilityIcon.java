package noppes.npcs.client.gui.hud.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.AbilityIconData;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.data.IAbilityAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.renderer.ImageData;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class AbilityIcon extends Gui {
    private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation("customnpcs", "textures/gui/ability_fallback.png");

    // Backed by AbilityIconData
    private final AbilityIconData data;
    public int width;
    public int height;

    // Default icon info (extracted from ability)
    private String defaultTexture = "";
    private String[] defaultStateTextures = null;
    private int defaultIconWidth = 48;
    private int defaultIconHeight = 48;
    private Ability sourceAbility = null;

    private AbilityIcon(AbilityIconData data) {
        this.data = data;
        this.width = data.width;
        this.height = data.height;
    }

    public static AbilityIcon fromAbility(Ability ability) {
        if (ability != null) {
            AbilityIcon icon = new AbilityIcon(AbilityIconData.fromAbility(ability));
            icon.defaultTexture = ability.getDefaultIconTexture();
            icon.defaultStateTextures = ability.getDefaultIconStateTextures();
            icon.defaultIconWidth = ability.getDefaultIconWidth();
            icon.defaultIconHeight = ability.getDefaultIconHeight();
            icon.sourceAbility = ability;
            return icon;
        }
        return fromDefaults();
    }

    public static AbilityIcon fromChainedAbility(ChainedAbility chain) {
        if (chain != null) {
            return new AbilityIcon(AbilityIconData.fromChainedAbility(chain));
        }
        return fromDefaults();
    }

    public static AbilityIcon fromAction(IAbilityAction action) {
        if (action instanceof Ability) return fromAbility((Ability) action);
        if (action instanceof ChainedAbility) return fromChainedAbility((ChainedAbility) action);
        return fromDefaults();
    }

    private static AbilityIcon fromDefaults() {
        return new AbilityIcon(AbilityIconData.fromCustomData(new NBTTagCompound()));
    }

    public void draw() {
        draw(0);
    }

    public void draw(int state) {
        draw(state, 1.0f);
    }

    public void draw(int state, float alpha) {
        TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator t;

        if (data.isEnabled() && data.hasTexture()) {
            // Path 1: Custom icon enabled with texture — draw with manual tintColor
            ImageData imageData = ClientCacheHandler.getImageData(data.texture);
            if (imageData != null && imageData.imageLoaded()) {
                int tint = data.getTintColor();
                float r = ((tint >> 16) & 0xFF) / 255f;
                float g = ((tint >> 8) & 0xFF) / 255f;
                float b = (tint & 0xFF) / 255f;
                GL11.glColor4f(r, g, b, alpha);
                GL11.glScalef(data.scale, data.scale, 1);
                renderEngine.bindTexture(imageData.getLocation());
                t = getTessellator(imageData, state);
                t.draw();
            } else {
                GL11.glColor4f(1, 1, 1, alpha);
                renderEngine.bindTexture(FALLBACK_TEXTURE);
                t = getFallbackTessellator();
                t.draw();
            }
        } else if (hasDefaultTexture()) {
            // Path 2: No custom icon, but type has a default icon — draw with auto-color tint
            String texturePath = resolveDefaultTexture(state);
            ImageData defaultImageData = ClientCacheHandler.getImageData(texturePath);
            if (defaultImageData != null && defaultImageData.imageLoaded()) {
                int autoColor = sourceAbility != null ? sourceAbility.getDefaultIconColor() : 0xFFFFFF;
                float r = ((autoColor >> 16) & 0xFF) / 255f;
                float g = ((autoColor >> 8) & 0xFF) / 255f;
                float b = (autoColor & 0xFF) / 255f;
                GL11.glColor4f(r, g, b, alpha);
                renderEngine.bindTexture(defaultImageData.getLocation());
                t = getDefaultIconTessellator();
                t.draw();
            } else {
                // Default texture not found — use fallback at default icon dimensions with auto-color tint
                int autoColor = sourceAbility != null ? sourceAbility.getDefaultIconColor() : 0xFFFFFF;
                float r = ((autoColor >> 16) & 0xFF) / 255f;
                float g = ((autoColor >> 8) & 0xFF) / 255f;
                float b = (autoColor & 0xFF) / 255f;
                GL11.glColor4f(r, g, b, alpha);
                renderEngine.bindTexture(FALLBACK_TEXTURE);
                t = getDefaultIconTessellator();
                t.draw();
            }
        } else {
            // Path 3: No custom icon, no default icon — generic fallback with auto-color tint if available
            int autoColor = sourceAbility != null ? sourceAbility.getDefaultIconColor() : 0xFFFFFF;
            float r = ((autoColor >> 16) & 0xFF) / 255f;
            float g = ((autoColor >> 8) & 0xFF) / 255f;
            float b = (autoColor & 0xFF) / 255f;
            GL11.glColor4f(r, g, b, alpha);
            renderEngine.bindTexture(FALLBACK_TEXTURE);
            t = getFallbackTessellator();
            t.draw();
        }

        GL11.glPopMatrix();
    }

    /**
     * Resolve the default texture path for a given toggle state.
     * Falls back to the base defaultTexture if no state-specific texture exists.
     */
    private String resolveDefaultTexture(int state) {
        if (state > 0 && defaultStateTextures != null && state - 1 < defaultStateTextures.length) {
            return defaultStateTextures[state - 1];
        }
        return defaultTexture;
    }

    private boolean hasDefaultTexture() {
        return defaultTexture != null && !defaultTexture.isEmpty();
    }

    private Tessellator getTessellator(ImageData imageData, int state) {
        float hw = data.width / 2f;
        float hh = data.height / 2f;

        float texW = imageData.getTotalWidth();
        float texH = imageData.getTotalHeight();

        int ix = data.getIconXForState(state);
        int iy = data.getIconYForState(state);

        float u1 = ix / texW;
        float v1 = iy / texH;
        float u2 = (ix + data.width) / texW;
        float v2 = (iy + data.height) / texH;

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(-hw, hh, zLevel, u1, v2);
        t.addVertexWithUV(hw, hh, zLevel, u2, v2);
        t.addVertexWithUV(hw, -hh, zLevel, u2, v1);
        t.addVertexWithUV(-hw, -hh, zLevel, u1, v1);
        return t;
    }

    private Tessellator getDefaultIconTessellator() {
        float hw = defaultIconWidth / 2f;
        float hh = defaultIconHeight / 2f;

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(-hw, hh, zLevel, 0, 1);
        t.addVertexWithUV(hw, hh, zLevel, 1, 1);
        t.addVertexWithUV(hw, -hh, zLevel, 1, 0);
        t.addVertexWithUV(-hw, -hh, zLevel, 0, 0);
        return t;
    }

    private Tessellator getFallbackTessellator() {
        float hw = 16f;
        float hh = 16f;

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(-hw, hh, zLevel, 0, 1);
        t.addVertexWithUV(hw, hh, zLevel, 1, 1);
        t.addVertexWithUV(hw, -hh, zLevel, 1, 0);
        t.addVertexWithUV(-hw, -hh, zLevel, 0, 0);
        return t;
    }

    public boolean hasTexture() {
        return (data.isEnabled() && data.hasTexture()) || hasDefaultTexture();
    }

    /**
     * Returns the approximate visual size (max dimension) that draw() produces.
     * Custom icon: max(width,height) * scale. Default icon: max(defaultW,defaultH). Fallback: 32.
     */
    public float getDrawSize() {
        if (data.isEnabled() && data.hasTexture()) {
            return Math.max(width, height) * data.scale;
        }
        if (hasDefaultTexture()) {
            return Math.max(defaultIconWidth, defaultIconHeight);
        }
        return 32f;
    }
}
