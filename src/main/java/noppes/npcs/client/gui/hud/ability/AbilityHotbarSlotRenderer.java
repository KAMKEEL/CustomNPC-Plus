package noppes.npcs.client.gui.hud.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.IAbilityAction;
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
                             int cx, int cy, int size, boolean isCenter, float slotAlpha) {
        float nameAlpha = this.nameAlpha;
        boolean isHorizontal = ConfigClient.AbilityHotbarHorizontal;
        if (size <= 0 || slotAlpha <= 0) return;
        float radius = size / 2f;
        float baseAlpha = isCenter ? 0.9f : 0.6f;
        float alpha = baseAlpha * slotAlpha;

        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0);

        boolean altTexture = ConfigClient.AbilityHotbarAltTexture;

        if (altTexture) {
            drawRoundedRect(-radius, -radius, radius, radius, 0.6f, 0.6f, 0.6f, 0.75f * alpha, false);
            if (isCenter) {
                drawRoundedRect(-radius, -radius, radius, radius, 0.8f, 0.8f, 0.2f, alpha, true);
            } else {
                drawRoundedRect(-radius, -radius, radius, radius, 0.4f, 0.4f, 0.4f, alpha * 0.8f, true);
            }
        } else {
            if (isCenter) {
                drawCircle(radius, 0.8f, 0.8f, 0.2f, 0.66f * alpha, false);
                drawCircle(radius, 0.8f, 0.8f, 0.2f, alpha, true);
            } else {
                drawCircle(radius, 1f, 1f, 1f, 0.66f * alpha, false);
                drawCircle(radius, 0.4f, 0.4f, 0.4f, alpha * 0.8f, true);
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
            GL11.glColor4f(1, 1, 1, slotAlpha);
            icon.draw(getToggleState());
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glPopMatrix();
        }

        // Cooldown arc
        if (cooldownProgress > 0 && abilityKey != null) {
            drawCooldownArc(radius, cooldownProgress);
        }

        GL11.glPopMatrix();

        // Ability name on center slot (respects text position config)
        int textPos = ConfigClient.AbilityHotbarTextPosition;
        if (textPos != 0 && isCenter && action != null && nameAlpha > 0) {
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

    private void drawCooldownArc(float radius, float progress) {
        progress = Math.min(1, Math.max(0, progress));
        if (progress <= 0) return;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.1f, 0.1f, 0.1f, 0.7f);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(0, 0);
        int steps = (int) (CIRCLE_SEGMENTS * progress) + 1;
        for (int i = 0; i <= steps; i++) {
            double angle = -Math.PI / 2 + 2 * Math.PI * i * progress / steps;
            GL11.glVertex2f((float) (Math.cos(angle) * radius), (float) (Math.sin(angle) * radius));
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
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
