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

    private AbilityIcon(AbilityIconData data) {
        this.data = data;
        this.width = data.width;
        this.height = data.height;
    }

    public static AbilityIcon fromAbility(Ability ability) {
        if (ability != null) {
            return new AbilityIcon(AbilityIconData.fromAbility(ability));
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

        ImageData imageData = null;
        if (data.hasTexture()) {
            imageData = ClientCacheHandler.getImageData(data.texture);
        }

        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, alpha);

        Tessellator t;

        if (imageData == null || !imageData.imageLoaded()) {
            GL11.glScalef(1.0f, 1.0f, 1);
            renderEngine.bindTexture(FALLBACK_TEXTURE);
            t = getFallbackTessellator();
        } else {
            GL11.glScalef(data.scale, data.scale, 1);
            renderEngine.bindTexture(imageData.getLocation());
            t = getTessellator(imageData, state);
        }

        t.draw();
        GL11.glPopMatrix();
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
        return data.hasTexture();
    }

    /**
     * Returns the approximate visual size (max dimension) that draw() produces.
     * Textured: max(width,height) * scale. Fallback: 32.
     */
    public float getDrawSize() {
        if (data.hasTexture()) {
            return Math.max(width, height) * data.scale;
        }
        return 32f;
    }
}
