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

    // Default icon layers (extracted from ability)
    private Ability.DefaultIconLayer[] defaultLayers = null;
    private int defaultIconWidth = 48;
    private int defaultIconHeight = 48;

    private AbilityIcon(AbilityIconData data) {
        this.data = data;
        this.width = data.width;
        this.height = data.height;
    }

    public static AbilityIcon fromAbility(Ability ability) {
        if (ability != null) {
            AbilityIcon icon = new AbilityIcon(AbilityIconData.fromAbility(ability));
            icon.defaultLayers = ability.getDefaultIconLayers();
            icon.defaultIconWidth = ability.getDefaultIconWidth();
            icon.defaultIconHeight = ability.getDefaultIconHeight();
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

        if (data.isEnabled()) {
            // Path 1: Custom icon — draw each active layer bottom-to-top
            GL11.glScalef(data.scale, data.scale, 1);
            boolean drewAny = false;
            for (int i = 0; i < data.getLayerCount(); i++) {
                AbilityIconData.Layer layer = data.getLayer(i);
                if (!layer.hasTexture()) continue;
                ImageData imageData = ClientCacheHandler.getImageData(layer.texture);
                if (imageData != null && imageData.imageLoaded()) {
                    int tint = layer.tintColor;
                    float r = ((tint >> 16) & 0xFF) / 255f;
                    float g = ((tint >> 8) & 0xFF) / 255f;
                    float b = (tint & 0xFF) / 255f;
                    GL11.glColor4f(r, g, b, alpha);
                    renderEngine.bindTexture(imageData.getLocation());
                    Tessellator t = getLayerTessellator(imageData, layer, i == 0 ? state : 0);
                    t.draw();
                    drewAny = true;
                }
            }
            if (!drewAny) {
                GL11.glColor4f(1, 1, 1, alpha);
                renderEngine.bindTexture(FALLBACK_TEXTURE);
                getFallbackTessellator().draw();
            }
        } else if (hasDefaultLayers()) {
            // Path 2: Default layers — draw each layer with its dynamic color
            for (Ability.DefaultIconLayer defLayer : defaultLayers) {
                if (defLayer.texture == null || defLayer.texture.isEmpty()) continue;
                ImageData imageData = ClientCacheHandler.getImageData(defLayer.texture);
                if (imageData != null && imageData.imageLoaded()) {
                    int color = defLayer.getColor();
                    float r = ((color >> 16) & 0xFF) / 255f;
                    float g = ((color >> 8) & 0xFF) / 255f;
                    float b = (color & 0xFF) / 255f;
                    GL11.glColor4f(r, g, b, alpha);
                    renderEngine.bindTexture(imageData.getLocation());
                    getDefaultIconTessellator().draw();
                }
            }
        } else {
            // Path 3: No custom icon, no default layers — generic fallback
            GL11.glColor4f(1, 1, 1, alpha);
            renderEngine.bindTexture(FALLBACK_TEXTURE);
            getFallbackTessellator().draw();
        }

        GL11.glPopMatrix();
    }

    private boolean hasDefaultLayers() {
        return defaultLayers != null && defaultLayers.length > 0;
    }

    private Tessellator getLayerTessellator(ImageData imageData, AbilityIconData.Layer layer, int state) {
        float hw = data.width / 2f;
        float hh = data.height / 2f;

        float texW = imageData.getTotalWidth();
        float texH = imageData.getTotalHeight();

        // State UV overrides only apply to layer 0
        int ix = (state > 0) ? data.getIconXForState(state) : layer.iconX;
        int iy = (state > 0) ? data.getIconYForState(state) : layer.iconY;

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
        return data.isEnabled() || hasDefaultLayers();
    }

    /**
     * Returns the approximate visual size (max dimension) that draw() produces.
     * Custom icon: max(width,height) * scale. Default icon: max(defaultW,defaultH). Fallback: 32.
     */
    public float getDrawSize() {
        if (data.isEnabled()) {
            return Math.max(width, height) * data.scale;
        }
        if (hasDefaultLayers()) {
            return Math.max(defaultIconWidth, defaultIconHeight);
        }
        return 32f;
    }
}
