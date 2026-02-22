package noppes.npcs.client.gui.hud.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.IAbilityAction;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.player.ability.AbilityHotbarSelectPacket;
import net.minecraft.client.Minecraft;
import noppes.npcs.client.ClientProxy;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.hud.HudComponent;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.controllers.data.AbilityHotbarData;
import noppes.npcs.controllers.data.PlayerAbilityHotbarData;
import noppes.npcs.controllers.data.PlayerData;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class AbilityHotbarComponent extends HudComponent {
    public static final int TOTAL_SLOTS = AbilityHotbarData.TOTAL_SLOTS;

    private static final int BASE_SLOT_SIZE = 24;
    private static final int BASE_SPACING = 28;

    private AbilityHotbarSlotRenderer[] slots = new AbilityHotbarSlotRenderer[TOTAL_SLOTS];

    // Filtered carousel: only filled slots + one deselect entry
    // Each entry is a raw slot index (0-11), or -1 for the deselect slot
    private int[] activeIndices = new int[0];
    private int activeCenter = 0;

    private float scrollOffset = 0f;
    private long scrollStartTime = 0;
    private static final long SCROLL_DURATION = 150;

    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 500;

    // Debounce: delay sending selection packet
    private String pendingSelectKey = null;
    private long pendingSelectTime = 0;
    private static final long SELECT_DEBOUNCE = 300;

    // Grace period: don't let server state override local state after user interaction
    private long lastUserCycleTime = 0;
    private static final long USER_CYCLE_GRACE = 1000;

    // Fade: when ShowAlways=false, fade in/out when HUD key is held
    private float fadeAlpha = 0f;
    private long lastFadeTime = 0;
    private static final float FADE_SPEED = 3f; // per second — ~333ms full transition

    private Minecraft mc;

    public AbilityHotbarComponent(Minecraft mc) {
        this.mc = mc;
        updateOverlayDimensions();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            slots[i] = new AbilityHotbarSlotRenderer(i);
        }
        load();
    }

    private int getEffectiveVisibleSlots() {
        int configMax = ConfigClient.AbilityHotbarVisibleSlots;
        if (configMax != 3 && configMax != 5 && configMax != 7) configMax = 5;
        int filledCount = activeIndices.length - 1; // subtract deselect slot
        if (filledCount < 0) filledCount = 0;
        if (filledCount < 5) return Math.min(configMax, 3);
        if (filledCount < 7) return Math.min(configMax, 5);
        return configMax;
    }

    private void updateOverlayDimensions() {
        boolean isHorizontal = ConfigClient.AbilityHotbarHorizontal;
        int visibleSlots = getEffectiveVisibleSlots();
        int span = BASE_SPACING * (visibleSlots - 1) + BASE_SLOT_SIZE;
        int thick = BASE_SLOT_SIZE + 4;
        if (isHorizontal) {
            overlayWidth = span;
            overlayHeight = thick;
        } else {
            overlayWidth = thick;
            overlayHeight = span;
        }
    }

    @Override
    public void loadData(NBTTagCompound compound) {
    }

    @Override
    public void load() {
        posX = ConfigClient.AbilityHotbarX;
        posY = ConfigClient.AbilityHotbarY;
        scale = ConfigClient.AbilityHotbarScale;
        enabled = ConfigClient.AbilityHotbarEnabled;
    }

    @Override
    public void save() {
        ConfigClient.AbilityHotbarX = posX;
        ConfigClient.AbilityHotbarXProperty.set((double) posX);

        ConfigClient.AbilityHotbarY = posY;
        ConfigClient.AbilityHotbarYProperty.set((double) posY);

        ConfigClient.AbilityHotbarScale = scale;
        ConfigClient.AbilityHotbarScaleProperty.set(scale);

        ConfigClient.AbilityHotbarEnabled = enabled;
        ConfigClient.AbilityHotbarEnabledProperty.set(enabled);

        ConfigClient.AbilityHotbarHorizontalProperty.set(ConfigClient.AbilityHotbarHorizontal);
        ConfigClient.AbilityHotbarAltTextureProperty.set(ConfigClient.AbilityHotbarAltTexture);
        ConfigClient.AbilityHotbarTextPositionProperty.set(ConfigClient.AbilityHotbarTextPosition);
        ConfigClient.AbilityHotbarVisibleSlotsProperty.set(ConfigClient.AbilityHotbarVisibleSlots);
        ConfigClient.AbilityHotbarShowAlwaysProperty.set(ConfigClient.AbilityHotbarShowAlways);
        ConfigClient.AbilityHotbarTextVisibilityProperty.set(ConfigClient.AbilityHotbarTextVisibility);

        if (ConfigClient.config.hasChanged()) {
            ConfigClient.config.save();
        }
    }

    @Override
    public void renderOnScreen(float partialTicks) {
        if (!enabled || isEditting) return;
        if (mc.thePlayer == null || mc.currentScreen != null) return;
        if (!hasAnyAbilities()) return;

        // Fade logic: when ShowAlways=false, only show while HUD key is held
        boolean showAlways = ConfigClient.AbilityHotbarShowAlways;
        long now = Minecraft.getSystemTime();
        if (!showAlways) {
            boolean hudKeyHeld = isHudKeyHeld();
            float dt = lastFadeTime > 0 ? (now - lastFadeTime) / 1000f : 0f;
            dt = Math.min(dt, 0.1f); // cap to avoid jumps after pauses
            lastFadeTime = now;
            if (hudKeyHeld || pendingSelectKey != null || scrollOffset != 0f) {
                fadeAlpha = Math.min(1f, fadeAlpha + FADE_SPEED * dt);
            } else {
                fadeAlpha = Math.max(0f, fadeAlpha - FADE_SPEED * dt);
            }
            if (fadeAlpha <= 0f) return;
        } else {
            fadeAlpha = 1f;
            lastFadeTime = now;
        }

        long currentTime = Minecraft.getSystemTime();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            updateAbilities();
            lastUpdateTime = currentTime;
        }

        // Flush debounced selection packet
        if (pendingSelectKey != null && currentTime - pendingSelectTime >= SELECT_DEBOUNCE) {
            PacketHandler.Instance.sendToServer(new AbilityHotbarSelectPacket(pendingSelectKey));
            pendingSelectKey = null;
        }

        updateScrollAnimation();
        updateOverlayDimensions();

        if (activeIndices.length == 0) return;

        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        boolean isHorizontal = ConfigClient.AbilityHotbarHorizontal;
        float effectiveScale = getEffectiveScale(sr);
        int visibleSlots = getEffectiveVisibleSlots();
        int half = visibleSlots / 2;

        int actualX = (int) (posX / 100.0f * sr.getScaledWidth());
        int actualY = (int) (posY / 100.0f * sr.getScaledHeight());

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(effectiveScale, effectiveScale, 1);

        int centerX = overlayWidth / 2;
        int centerY = overlayHeight / 2;

        // Compute text visibility: 0=Shown, 1=Hidden, 2=Held
        boolean showText;
        int textVis = ConfigClient.AbilityHotbarTextVisibility;
        if (textVis == 1) {
            showText = false;
        } else if (textVis == 2) {
            showText = isHudKeyHeld();
        } else {
            showText = true;
        }

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            slots[i].updateNameFade();
        }

        for (int offset = -half; offset <= half; offset++) {
            int activeIdx = ((activeCenter + offset) % activeIndices.length + activeIndices.length) % activeIndices.length;
            int rawSlotIndex = activeIndices[activeIdx];

            float visualPos = offset + scrollOffset;
            float absDist = Math.abs(visualPos);

            float slotScale;
            if (absDist <= 1f) {
                slotScale = 1.0f - 0.25f * absDist;
            } else {
                slotScale = 0.75f - 0.15f * (absDist - 1f);
            }
            slotScale = Math.max(0.1f, slotScale);

            int scaledSize = Math.max(1, (int) (BASE_SLOT_SIZE * slotScale));

            int cx, cy;
            if (isHorizontal) {
                cx = centerX + (int) (visualPos * BASE_SPACING);
                cy = centerY;
            } else {
                cx = centerX;
                cy = centerY + (int) (visualPos * BASE_SPACING);
            }

            float maxDist = half + 0.5f;
            if (absDist > maxDist) continue;

            float edgeAlpha = absDist > half - 0.5f ? 1f - (absDist - (half - 0.5f)) : 1f;
            float slotAlpha = edgeAlpha * fadeAlpha;
            boolean isCenter = (offset == 0 && scrollOffset == 0f);

            // Compute per-slot cooldown progress (all slots, not just center)
            float slotCooldown = 0f;
            if (rawSlotIndex >= 0 && slots[rawSlotIndex].abilityKey != null) {
                slotCooldown = getCooldownProgressForSlot(rawSlotIndex);
            }

            if (rawSlotIndex == -1) {
                // Deselect slot — draw empty marker
                drawDeselectSlot(cx, cy, scaledSize, isCenter, slotAlpha);
            } else {
                slots[rawSlotIndex].drawCarousel(mc, sr, slotCooldown, cx, cy, scaledSize, isCenter, slotAlpha, showText);
            }
        }

        GL11.glPopMatrix();
    }

    private void drawDeselectSlot(int cx, int cy, int size, boolean isCenter, float alpha) {
        float radius = size / 2f;
        float baseAlpha = isCenter ? 0.9f : 0.6f;
        float a = baseAlpha * alpha;
        boolean altTexture = ConfigClient.AbilityHotbarAltTexture;

        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, 0);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (isCenter) {
            GL11.glColor4f(0.5f, 0.2f, 0.2f, a);
        } else {
            GL11.glColor4f(0.3f, 0.3f, 0.3f, a);
        }

        if (altTexture) {
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-radius, -radius);
            GL11.glVertex2f(radius, -radius);
            GL11.glVertex2f(radius, radius);
            GL11.glVertex2f(-radius, radius);
            GL11.glEnd();
        } else {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glVertex2f(0, 0);
            for (int i = 0; i <= 32; i++) {
                double angle = 2 * Math.PI * i / 32;
                GL11.glVertex2f((float) (Math.cos(angle) * radius), (float) (Math.sin(angle) * radius));
            }
            GL11.glEnd();
        }

        // Draw X mark
        if (isCenter) {
            GL11.glColor4f(0.8f, 0.4f, 0.4f, alpha);
        } else {
            GL11.glColor4f(0.5f, 0.5f, 0.5f, alpha * 0.8f);
        }
        float xSize = radius * 0.4f;
        GL11.glLineWidth(2.0f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(-xSize, -xSize);
        GL11.glVertex2f(xSize, xSize);
        GL11.glVertex2f(-xSize, xSize);
        GL11.glVertex2f(xSize, -xSize);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    @Override
    public void renderEditing() {
        isEditting = true;
        updateOverlayDimensions();

        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        float effectiveScale = getEffectiveScale(sr);
        boolean isHorizontal = ConfigClient.AbilityHotbarHorizontal;
        FontRenderer fr = mc.fontRenderer;
        int visibleSlots = getEffectiveVisibleSlots();
        int half = visibleSlots / 2;

        int actualX = (int) (posX / 100.0f * sr.getScaledWidth());
        int actualY = (int) (posY / 100.0f * sr.getScaledHeight());

        GL11.glPushMatrix();
        GL11.glTranslatef(actualX, actualY, 0);
        GL11.glScalef(effectiveScale, effectiveScale, 1);

        int centerX = overlayWidth / 2;
        int centerY = overlayHeight / 2;

        int[] slotCx = new int[visibleSlots];
        int[] slotCy = new int[visibleSlots];
        int[] slotSizes = new int[visibleSlots];

        for (int offset = -half; offset <= half; offset++) {
            float absDist = Math.abs(offset);
            float slotScale;
            if (absDist <= 1f) {
                slotScale = 1.0f - 0.25f * absDist;
            } else {
                slotScale = 0.75f - 0.15f * (absDist - 1f);
            }
            slotScale = Math.max(0.1f, slotScale);
            int scaledSize = Math.max(1, (int) (BASE_SLOT_SIZE * slotScale));

            int cx, cy;
            if (isHorizontal) {
                cx = centerX + offset * BASE_SPACING;
                cy = centerY;
            } else {
                cx = centerX;
                cy = centerY + offset * BASE_SPACING;
            }

            int idx = offset + half;
            slotCx[idx] = cx;
            slotCy[idx] = cy;
            slotSizes[idx] = scaledSize;

            float radius = scaledSize / 2f;
            boolean altTexture = ConfigClient.AbilityHotbarAltTexture;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            float alpha = offset == 0 ? 0.9f : 0.6f;
            if (offset == 0) {
                GL11.glColor4f(0.8f, 0.8f, 0.2f, alpha);
            } else {
                GL11.glColor4f(0.4f, 0.4f, 0.4f, alpha);
            }

            if (altTexture) {
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(cx - radius, cy - radius);
                GL11.glVertex2f(cx + radius, cy - radius);
                GL11.glVertex2f(cx + radius, cy + radius);
                GL11.glVertex2f(cx - radius, cy + radius);
                GL11.glEnd();
            } else {
                GL11.glBegin(GL11.GL_TRIANGLE_FAN);
                GL11.glVertex2f(cx, cy);
                for (int i = 0; i <= 32; i++) {
                    double angle = 2 * Math.PI * i / 32;
                    GL11.glVertex2f(cx + (float) (Math.cos(angle) * radius), cy + (float) (Math.sin(angle) * radius));
                }
                GL11.glEnd();
            }
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        for (int i = 0; i < visibleSlots; i++) {
            String label = String.valueOf(i + 1);
            float labelScale = slotSizes[i] / 24.0f;
            if (labelScale < 0.4f) continue;

            int textColor = (i == half) ? 0xFFFFFFFF : 0xAABBBBBB;
            int textW = fr.getStringWidth(label);

            GL11.glPushMatrix();
            GL11.glTranslatef(slotCx[i], slotCy[i], 0);
            GL11.glScalef(labelScale, labelScale, 1);
            fr.drawString(label, -textW / 2, -fr.FONT_HEIGHT / 2, textColor);
            GL11.glPopMatrix();
        }

        boolean altTexture = ConfigClient.AbilityHotbarAltTexture;
        String modeLabel = altTexture ? "Square" : "Circle";
        int modeLabelW = fr.getStringWidth(modeLabel);
        fr.drawStringWithShadow(modeLabel, overlayWidth / 2 - modeLabelW / 2, overlayHeight + 2, 0xAA888888);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.2f, 0.8f, 0.2f, 0.8f);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(overlayWidth, 0);
        GL11.glVertex2f(overlayWidth, overlayHeight);
        GL11.glVertex2f(0, overlayHeight);
        GL11.glEnd();

        GL11.glColor4f(0.8f, 0.8f, 0.8f, 0.9f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(overlayWidth - 10, overlayHeight - 10);
        GL11.glVertex2f(overlayWidth, overlayHeight - 10);
        GL11.glVertex2f(overlayWidth, overlayHeight);
        GL11.glVertex2f(overlayWidth - 10, overlayHeight);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);

        GL11.glPopMatrix();
    }

    @Override
    public void addEditorButtons(List<GuiButton> buttonList) {
        super.addEditorButtons(buttonList);
        String orientLabel = ConfigClient.AbilityHotbarHorizontal ? "Horizontal" : "Vertical";
        buttonList.add(new GuiButton(1, 0, 0, 120, 20, orientLabel));

        String texLabel = ConfigClient.AbilityHotbarAltTexture ? "Square" : "Circle";
        buttonList.add(new GuiButton(2, 0, 0, 120, 20, texLabel));

        buttonList.add(new GuiButton(3, 0, 0, 120, 20, getTextPositionLabel()));
        buttonList.add(new GuiButton(4, 0, 0, 120, 20, "Slots: " + ConfigClient.AbilityHotbarVisibleSlots));
        buttonList.add(new GuiButton(5, 0, 0, 120, 20, ConfigClient.AbilityHotbarShowAlways ? "Show: Always" : "Show: Hold Key"));
        buttonList.add(new GuiButton(6, 0, 0, 120, 20, getTextVisibilityLabel()));
    }

    @Override
    public void onEditorButtonPressed(GuiButton button) {
        if (button.id == 1) {
            ConfigClient.AbilityHotbarHorizontal = !ConfigClient.AbilityHotbarHorizontal;
            button.displayString = ConfigClient.AbilityHotbarHorizontal ? "Horizontal" : "Vertical";
            updateOverlayDimensions();
        } else if (button.id == 2) {
            ConfigClient.AbilityHotbarAltTexture = !ConfigClient.AbilityHotbarAltTexture;
            button.displayString = ConfigClient.AbilityHotbarAltTexture ? "Square" : "Circle";
        } else if (button.id == 3) {
            // Toggle between 1 (Above/Left) and 2 (Below/Right)
            ConfigClient.AbilityHotbarTextPosition = ConfigClient.AbilityHotbarTextPosition == 1 ? 2 : 1;
            button.displayString = getTextPositionLabel();
        } else if (button.id == 4) {
            int current = ConfigClient.AbilityHotbarVisibleSlots;
            if (current <= 3) ConfigClient.AbilityHotbarVisibleSlots = 5;
            else if (current <= 5) ConfigClient.AbilityHotbarVisibleSlots = 7;
            else ConfigClient.AbilityHotbarVisibleSlots = 3;
            button.displayString = "Slots: " + ConfigClient.AbilityHotbarVisibleSlots;
            updateOverlayDimensions();
        } else if (button.id == 5) {
            ConfigClient.AbilityHotbarShowAlways = !ConfigClient.AbilityHotbarShowAlways;
            button.displayString = ConfigClient.AbilityHotbarShowAlways ? "Show: Always" : "Show: Hold Key";
        } else if (button.id == 6) {
            ConfigClient.AbilityHotbarTextVisibility = (ConfigClient.AbilityHotbarTextVisibility + 1) % 3;
            button.displayString = getTextVisibilityLabel();
        } else {
            super.onEditorButtonPressed(button);
        }
    }

    private String getTextPositionLabel() {
        boolean h = ConfigClient.AbilityHotbarHorizontal;
        switch (ConfigClient.AbilityHotbarTextPosition) {
            case 1: return h ? "Text: Above" : "Text: Left";
            case 2: return h ? "Text: Below" : "Text: Right";
            default: return h ? "Text: Below" : "Text: Right";
        }
    }

    private String getTextVisibilityLabel() {
        switch (ConfigClient.AbilityHotbarTextVisibility) {
            case 0: return "Text: Shown";
            case 1: return "Text: Hidden";
            case 2: return "Text: Held";
            default: return "Text: Shown";
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Ability Update & Scroll
    // ═══════════════════════════════════════════════════════════════════

    private void updateAbilities() {
        PlayerData playerData = ClientCacheHandler.playerData;
        if (playerData == null) return;

        PlayerAbilityHotbarData hotbarData = playerData.hotbarData;
        if (hotbarData == null) return;

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            AbilityHotbarData slotData = hotbarData.slots[i];
            String key = slotData.isEmpty() ? null : slotData.abilityKey;
            Ability ability = null;
            IAbilityAction action = null;
            if (key != null && AbilityController.Instance != null) {
                if (slotData.isChainKey()) {
                    action = AbilityController.Instance.resolveChainedAbility(slotData.getResolveKey());
                } else {
                    ability = AbilityController.Instance.resolveAbility(key);
                    action = ability;
                }
                if (ability == null && action == null) key = null;
            }
            slots[i].setAbility(key, ability, action);
        }

        // Rebuild active indices: filled slots + one deselect slot (-1)
        ArrayList<Integer> active = new ArrayList<>();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (slots[i].abilityKey != null) {
                active.add(i);
            }
        }
        active.add(-1); // Deselect slot at the end
        activeIndices = new int[active.size()];
        for (int i = 0; i < active.size(); i++) {
            activeIndices[i] = active.get(i);
        }

        // Clamp activeCenter
        if (activeCenter >= activeIndices.length) {
            activeCenter = 0;
        }

        // Don't override local selection state while user is scrolling, debounce is pending,
        // or within the grace period after the last user interaction.
        // This prevents the server state from fighting the local carousel position.
        long now = Minecraft.getSystemTime();
        if (pendingSelectKey != null || scrollOffset != 0f || (now - lastUserCycleTime < USER_CYCLE_GRACE)) {
            return;
        }

        // Sync selected state and find which active index is selected
        if (playerData.abilityData != null) {
            String selectedKey = playerData.abilityData.getSelectedAbilityKey();
            boolean foundSelected = false;
            for (int i = 0; i < TOTAL_SLOTS; i++) {
                AbilityHotbarData slotData = hotbarData.slots[i];
                boolean sel = selectedKey != null && !slotData.isEmpty()
                    && selectedKey.equals(slotData.abilityKey);
                slots[i].setSelectedState(sel);
                if (sel) {
                    // Find this slot's position in activeIndices
                    for (int j = 0; j < activeIndices.length; j++) {
                        if (activeIndices[j] == i) {
                            activeCenter = j;
                            foundSelected = true;
                            break;
                        }
                    }
                }
            }
            if (!foundSelected) {
                // No ability selected on server — center on deselect slot
                for (int j = 0; j < activeIndices.length; j++) {
                    if (activeIndices[j] == -1) {
                        activeCenter = j;
                        break;
                    }
                }
            }
        }
    }

    private boolean isHudKeyHeld() {
        if (ClientProxy.AbilityHudKey == null) return false;
        int keyCode = ClientProxy.AbilityHudKey.getKeyCode();
        return keyCode != 0 && org.lwjgl.input.Keyboard.isKeyDown(keyCode);
    }

    public boolean hasAnyAbilities() {
        PlayerData playerData = ClientCacheHandler.playerData;
        if (playerData == null || playerData.hotbarData == null) return false;
        return playerData.hotbarData.hasAnyAbilities();
    }

    /**
     * Get cooldown progress for a hotbar slot.
     * Per-ability cooldown abilities use their own cooldown; others use global.
     * @return 1.0 = fully on cooldown, 0.0 = ready
     */
    private float getCooldownProgressForSlot(int slotIndex) {
        PlayerData playerData = ClientCacheHandler.playerData;
        if (playerData == null || playerData.abilityData == null) return 0f;

        String key = slots[slotIndex].abilityKey;
        Ability ability = slots[slotIndex].ability;

        if (ability != null && ability.isPerAbilityCooldown()) {
            return playerData.abilityData.getPerAbilityCooldownProgress(key);
        } else {
            return playerData.abilityData.getGlobalCooldownProgress();
        }
    }

    public void onCycleNext() {
        cycle(1);
    }

    public void onCyclePrev() {
        cycle(-1);
    }

    private void cycle(int direction) {
        if (!enabled) return;
        if (mc.thePlayer == null || mc.currentScreen != null) return;
        if (activeIndices.length == 0) return;

        activeCenter = ((activeCenter + direction) % activeIndices.length + activeIndices.length) % activeIndices.length;
        scrollOffset = direction;
        long now = Minecraft.getSystemTime();
        scrollStartTime = now;
        lastUserCycleTime = now;

        int rawSlotIndex = activeIndices[activeCenter];

        // Update selection states locally
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            slots[i].setSelectedState(i == rawSlotIndex);
        }

        // Debounce: queue the selection, don't send immediately
        String keyToSend = rawSlotIndex >= 0 && slots[rawSlotIndex].abilityKey != null
            ? slots[rawSlotIndex].abilityKey : "";
        pendingSelectKey = keyToSend;
        pendingSelectTime = Minecraft.getSystemTime();
    }

    private void updateScrollAnimation() {
        if (scrollOffset == 0f) return;

        long now = Minecraft.getSystemTime();
        float t = (float) (now - scrollStartTime) / SCROLL_DURATION;
        t = Math.min(t, 1f);

        float eased = easeOutCubic(t);
        float initialOffset = scrollOffset < 0 ? -1f : 1f;
        scrollOffset = initialOffset * (1f - eased);

        if (t >= 1f) scrollOffset = 0f;
    }

    private float easeOutCubic(float x) {
        float v = 1 - x;
        return 1 - v * v * v;
    }

    public void refresh() {
        updateAbilities();
        lastUpdateTime = Minecraft.getSystemTime();
    }
}
