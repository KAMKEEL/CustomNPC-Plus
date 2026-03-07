package noppes.npcs.client.gui.hud.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.data.IAbilityAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.controllers.data.PlayerData;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class AbilityHotbarSlotRenderer extends Gui {
    private static final int CIRCLE_SEGMENTS = 32;
    private static final long NAME_FADE_DURATION = 180;

    public int index;
    public String abilityKey = null;
    public Ability ability = null;
    public IAbilityAction action = null;
    private AbilityIcon icon = null;

    public boolean isSelected = false;
    public float nameAlpha = 0f;
    private boolean nameFadingIn = false;
    private long nameFadeStartTime = 0;

    public AbilityHotbarSlotRenderer(int index) {
        this.index = index;
    }

    public void setAbility(String key, Ability ability, IAbilityAction action) {
        this.abilityKey = key;
        this.ability = ability;
        this.action = action;
        if (ability != null) {
            this.icon = AbilityIcon.fromAbility(ability);
        } else if (action instanceof ChainedAbility) {
            this.icon = AbilityIcon.fromChainedAbility((ChainedAbility) action);
        } else {
            this.icon = null;
        }
    }

    public void drawCarousel(Minecraft mc, ScaledResolution sr, float cooldownProgress,
                             int cx, int cy, int size, boolean isCenter, float slotAlpha,
                             boolean showText, boolean activePhase) {
        float nameAlpha = this.nameAlpha;
        boolean isHorizontal = ConfigClient.AbilityHotbarHorizontal;
        if (size <= 0 || slotAlpha <= 0) return;
        float radius = size / 2f;
        float baseAlpha = isCenter ? 0.9f : 0.6f;
        float alpha = baseAlpha * slotAlpha;

        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0);

        boolean altTexture = ConfigClient.AbilityHotbarAltTexture;

        float fillR;
        float fillG;
        float fillB;
        float outlineR;
        float outlineG;
        float outlineB;

        if (activePhase) {
            if (isCenter) {
                fillR = 0.95f;
                fillG = 0.26f;
                fillB = 0.22f;
                outlineR = 1.0f;
                outlineG = 0.52f;
                outlineB = 0.28f;
            } else {
                fillR = 0.36f;
                fillG = 0.14f;
                fillB = 0.14f;
                outlineR = 0.65f;
                outlineG = 0.22f;
                outlineB = 0.20f;
            }
        } else if (isCenter) {
            fillR = 0.8f;
            fillG = 0.8f;
            fillB = 0.2f;
            outlineR = 0.8f;
            outlineG = 0.8f;
            outlineB = 0.2f;
        } else {
            fillR = 0.4f;
            fillG = 0.4f;
            fillB = 0.4f;
            outlineR = 1f;
            outlineG = 1f;
            outlineB = 1f;
        }

        if (altTexture) {
            float baseR = activePhase ? 0.30f : 0.6f;
            float baseG = activePhase ? 0.12f : 0.6f;
            float baseB = activePhase ? 0.12f : 0.6f;
            drawRoundedRect(-radius, -radius, radius, radius, baseR, baseG, baseB, 0.75f * alpha, false);
            drawRoundedRect(-radius, -radius, radius, radius, fillR, fillG, fillB, alpha * 0.8f, true);
        } else {
            drawCircle(radius, outlineR, outlineG, outlineB, 0.66f * alpha, false);
            drawCircle(radius, fillR, fillG, fillB, alpha * 0.8f, true);
        }

        // Toggle active indicator: subtle green outline
        int toggleState = getToggleState();
        if (toggleState > 0) {
            float glowAlpha = 0.45f * slotAlpha;
            if (altTexture) {
                drawRoundedRect(-radius, -radius, radius, radius, 0.2f, 0.85f, 0.3f, glowAlpha, true);
            } else {
                drawCircle(radius, 0.2f, 0.85f, 0.3f, glowAlpha, true);
            }
        }

        // Ability icon
        if (icon != null) {
            GL11.glPushMatrix();
            float targetSize = size * 0.55f;
            float iconNaturalSize = Math.max(icon.width, icon.height);
            if (iconNaturalSize <= 0) iconNaturalSize = 16f;
            float iconScale = targetSize / iconNaturalSize;
            GL11.glScalef(iconScale, iconScale, 1);
            icon.draw(toggleState, slotAlpha);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glPopMatrix();
        }

        // Cooldown overlay (all slots)
        if (cooldownProgress > 0 && abilityKey != null) {
            drawCooldownOverlay(radius, cooldownProgress, altTexture, slotAlpha);
        }

        GL11.glPopMatrix();

        // Ability name on center slot (respects text position and visibility config)
        int textPos = ConfigClient.AbilityHotbarTextPosition;
        if (showText && isCenter && action != null && nameAlpha > 0) {
            FontRenderer fr = mc.fontRenderer;
            String name = getAbilityName();
            if (name != null && !name.isEmpty()) {
                int a = (int) (nameAlpha * 255) & 0xFF;
                int nameColor = (a << 24) | (getNameColor() & 0x00FFFFFF);
                int nameX, nameY;
                if (isHorizontal) {
                    nameX = cx - fr.getStringWidth(name) / 2;
                    if (textPos == 1) {
                        nameY = cy - (int) radius - fr.FONT_HEIGHT - 2; // Above
                    } else {
                        nameY = cy + (int) radius + 2; // Below
                    }
                } else {
                    nameY = cy - fr.FONT_HEIGHT / 2;
                    if (textPos == 1) {
                        nameX = cx - (int) radius - fr.getStringWidth(name) - 4; // Left
                    } else {
                        nameX = cx + (int) radius + 4; // Right
                    }
                }
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor4f(1, 1, 1, nameAlpha);
                fr.drawStringWithShadow(name, nameX, nameY, nameColor);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glColor4f(1, 1, 1, 1);
            }
        }
    }

    private void drawRoundedRect(float x1, float y1, float x2, float y2,
                                 float r, float g, float b, float a, boolean outline) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);

        if (outline) {
            GL11.glLineWidth(2.0f);
            GL11.glBegin(GL11.GL_LINE_LOOP);
        } else {
            GL11.glBegin(GL11.GL_QUADS);
        }

        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x1, y2);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private void drawCircle(float radius, float r, float g, float b, float a, boolean outline) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);

        if (outline) {
            GL11.glLineWidth(1.5f);
            GL11.glBegin(GL11.GL_LINE_LOOP);
        } else {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glVertex2f(0, 0);
        }

        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / CIRCLE_SEGMENTS;
            GL11.glVertex2f((float) (Math.cos(angle) * radius), (float) (Math.sin(angle) * radius));
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor4f(1, 1, 1, 1);
    }

    /**
     * Draw a cooldown overlay on the slot.
     * progress: 1.0 = fully on cooldown (just started), 0.0 = ready
     * Renders a filled shape covering the entire slot that transitions from red to green.
     */
    private void drawCooldownOverlay(float radius, float progress, boolean altTexture, float slotAlpha) {
        progress = Math.min(1f, Math.max(0f, progress));
        if (progress <= 0f) return;

        // Color: red (progress=1.0) → orange → yellow-green → green (progress→0.0)
        float cr, cg, cb;
        if (progress > 0.5f) {
            float t = (progress - 0.5f) * 2f; // 1.0 at progress=1.0, 0.0 at progress=0.5
            cr = 0.8f;
            cg = 0.15f + 0.25f * (1f - t); // 0.15 → 0.4
            cb = 0.1f;
        } else if (progress > 0.2f) {
            float t = (progress - 0.2f) / 0.3f; // 1.0 at 0.5, 0.0 at 0.2
            cr = 0.8f * t + 0.3f * (1f - t); // 0.8 → 0.3
            cg = 0.4f * t + 0.7f * (1f - t); // 0.4 → 0.7
            cb = 0.1f;
        } else {
            float t = progress / 0.2f; // 1.0 at 0.2, 0.0 at 0.0
            cr = 0.3f * t + 0.1f * (1f - t); // 0.3 → 0.1
            cg = 0.7f * t + 0.7f * (1f - t); // 0.7
            cb = 0.1f + 0.1f * (1f - t); // 0.1 → 0.2
        }

        // Semi-transparent so icon is visible underneath; fade out near end
        float overlayAlpha = 0.5f * slotAlpha * Math.min(1f, progress * 3f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(cr, cg, cb, overlayAlpha);

        if (altTexture) {
            // Square slot: full filled rectangle
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-radius, -radius);
            GL11.glVertex2f(radius, -radius);
            GL11.glVertex2f(radius, radius);
            GL11.glVertex2f(-radius, radius);
            GL11.glEnd();
        } else {
            // Circle slot: full filled circle
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glVertex2f(0, 0);
            for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
                double angle = 2 * Math.PI * i / CIRCLE_SEGMENTS;
                GL11.glVertex2f((float) (Math.cos(angle) * radius), (float) (Math.sin(angle) * radius));
            }
            GL11.glEnd();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private String getAbilityName() {
        if (ability != null) {
            int state = getToggleState();
            if (state > 0) {
                String label = ability.getToggleStateLabel(state);
                if (label != null) return label;
            }
            return ability.getDisplayName();
        }
        if (action instanceof ChainedAbility) return ((ChainedAbility) action).getDisplayName();
        return action != null ? action.getName() : "";
    }

    private int getToggleState() {
        if (ability == null || !ability.isToggleable()) return 0;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return 0;
        PlayerData playerData = PlayerData.get(mc.thePlayer);
        if (playerData == null || playerData.abilityData == null) return 0;
        return playerData.abilityData.getToggleState(abilityKey);
    }

    private int getNameColor() {
        if (ability != null && ability.isToggleable()) {
            return getToggleState() > 0 ? 0xFF55FF55 : 0xFFFF5555;
        }
        return 0xFFFFFFFF;
    }

    public void setSelectedState(boolean newSelectState) {
        if (!isSelected && newSelectState) {
            nameFadingIn = true;
            nameFadeStartTime = Minecraft.getSystemTime();
        }
        if (isSelected && !newSelectState) {
            nameFadingIn = false;
            nameFadeStartTime = Minecraft.getSystemTime();
        }
        isSelected = newSelectState;
    }

    public void updateNameFade() {
        long now = Minecraft.getSystemTime();
        float t = (float) (now - nameFadeStartTime) / NAME_FADE_DURATION;
        t = Math.min(1f, Math.max(0f, t));
        if (nameFadingIn) {
            nameAlpha = t;
        } else {
            nameAlpha = 1f - t;
        }
    }
}
