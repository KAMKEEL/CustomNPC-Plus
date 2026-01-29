package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.Condition;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.AnimationController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.select.GuiAnimationSelection;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Base SubGui for editing ability configuration with tabbed interface.
 * Uses GuiMenuTopButton for navigation tabs.
 * <p>
 * Tabs:
 * - General: Name, Weight, Lock Movement, Interruptible, Timing
 * - Type: Type-specific settings per Ability (override in subclasses)
 * - Target: Min/Max Range, Targeting Mode, Conditions (up to 3)
 * - Effects: Sounds, Animations, Telegraph settings, Colors
 * <p>
 * Subclasses should override initTypeTab() and handleTypeButton()/handleTypeTextField()
 * to provide type-specific settings UI.
 */
public class SubGuiAbilityConfig extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {

    // Tab constants
    protected static final int TAB_GENERAL = 0;
    protected static final int TAB_TYPE = 1;
    protected static final int TAB_TARGET = 2;
    protected static final int TAB_EFFECTS = 3;
    protected static final int TAB_VISUAL = 4;

    // Core references
    protected final Ability ability;
    protected final IAbilityConfigCallback callback;
    protected int activeTab = TAB_GENERAL;

    // ═══════════════════════════════════════════════════════════════════════════
    // CACHED VALUES - General Tab
    // ═══════════════════════════════════════════════════════════════════════════
    protected String name;
    protected boolean enabled;
    protected int weight;
    protected LockMovementType lockMovement;
    protected boolean interruptible;

    // Timing
    protected int cooldownTicks;
    protected int windUpTicks;
    protected int dazedTicks;
    protected boolean syncWindupWithAnimation = true; // Sync windup ticks with animation duration

    // ═══════════════════════════════════════════════════════════════════════════
    // CACHED VALUES - Target Tab
    // ═══════════════════════════════════════════════════════════════════════════
    protected float minRange;
    protected float maxRange;
    protected TargetingMode targetingMode;
    protected List<Condition> conditions;

    // ═══════════════════════════════════════════════════════════════════════════
    // CACHED VALUES - Effects Tab
    // ═══════════════════════════════════════════════════════════════════════════
    protected String windUpSound;
    protected String activeSound;
    protected int windUpAnimationId;
    protected int activeAnimationId;
    protected String windUpAnimationName = null;
    protected String activeAnimationName = null;

    // Telegraph
    protected boolean showTelegraph;
    protected int windUpColor;
    protected int activeColor;

    // Editing state
    protected int editingColorId = 0;
    protected int editingSoundId = 0;
    protected int editingAnimationId = 0;
    protected int editingConditionIndex = -1;

    // ═══════════════════════════════════════════════════════════════════════════
    // ABILITY TYPE FEATURES
    // ═══════════════════════════════════════════════════════════════════════════
    protected final boolean supportsTelegraph;
    protected final boolean targetingModeLocked;
    protected final boolean hasTypeSettings;
    protected final boolean hasVisualSettings;

    public SubGuiAbilityConfig(Ability ability, IAbilityConfigCallback callback) {
        this.ability = ability;
        this.callback = callback;

        // Cache values from ability - General
        this.name = ability.getName() != null ? ability.getName() : "";
        this.enabled = ability.isEnabled();
        this.weight = ability.getWeight();
        this.lockMovement = ability.getLockMovement();
        this.interruptible = ability.isInterruptible();

        // Timing
        this.cooldownTicks = ability.getCooldownTicks();
        this.windUpTicks = ability.getWindUpTicks();
        this.dazedTicks = ability.getDazedTicks();

        // Target
        this.minRange = ability.getMinRange();
        this.maxRange = ability.getMaxRange();
        this.targetingMode = ability.getTargetingMode();
        this.conditions = new ArrayList<>(ability.getConditions());

        // Effects
        this.windUpSound = ability.getWindUpSound();
        this.activeSound = ability.getActiveSound();
        // Animation: check name first (built-in), then ID (user)
        this.windUpAnimationName = ability.getWindUpAnimationName();
        this.activeAnimationName = ability.getActiveAnimationName();
        this.windUpAnimationId = ability.getWindUpAnimationId();
        this.activeAnimationId = ability.getActiveAnimationId();

        // Telegraph
        this.showTelegraph = ability.isShowTelegraph();
        this.windUpColor = ability.getWindUpColor();
        this.activeColor = ability.getActiveColor();

        // Determine ability type features
        TelegraphType defaultType = ability.getTelegraphType();
        this.supportsTelegraph = defaultType != null && defaultType != TelegraphType.NONE;
        this.targetingModeLocked = ability.isTargetingModeLocked();
        this.hasTypeSettings = ability.hasTypeSettings();
        this.hasVisualSettings = hasVisualSettings();

        setBackground("menubg.png", 217);
        xSize = 356;
        ySize = 200;
    }

    @Override
    public void initGui() {
        super.initGui();

        // ═══════════════════════════════════════════════════════════════════════
        // TOP TABS using GuiMenuTopButton
        // ═══════════════════════════════════════════════════════════════════════
        GuiMenuTopButton generalTab = new GuiMenuTopButton(90, guiLeft + 4, guiTop - 17, "menu.general");
        generalTab.active = (activeTab == TAB_GENERAL);
        addTopButton(generalTab);

        GuiMenuTopButton lastTab = generalTab;

        if (hasTypeSettings) {
            GuiMenuTopButton typeTab = new GuiMenuTopButton(91, lastTab, "gui.type");
            typeTab.active = (activeTab == TAB_TYPE);
            addTopButton(typeTab);
            lastTab = typeTab;
        }

        GuiMenuTopButton targetTab = new GuiMenuTopButton(92, lastTab, "script.target");
        targetTab.active = (activeTab == TAB_TARGET);
        addTopButton(targetTab);
        lastTab = targetTab;

        GuiMenuTopButton effectsTab = new GuiMenuTopButton(93, lastTab, "ability.tab.effects");
        effectsTab.active = (activeTab == TAB_EFFECTS);
        addTopButton(effectsTab);
        lastTab = effectsTab;

        if (hasVisualSettings) {
            GuiMenuTopButton visualTab = new GuiMenuTopButton(94, lastTab, "ability.tab.visual");
            visualTab.active = (activeTab == TAB_VISUAL);
            addTopButton(visualTab);
        }

        // Close button (X) in top menu bar - use -1000 to avoid any ID conflicts
        GuiMenuTopButton closeBtn = new GuiMenuTopButton(-1000, guiLeft + xSize - 22, guiTop - 17, "X");
        addTopButton(closeBtn);

        int contentY = guiTop + 5;

        // ═══════════════════════════════════════════════════════════════════════
        // TAB CONTENT
        // ═══════════════════════════════════════════════════════════════════════
        switch (activeTab) {
            case TAB_GENERAL:
                initGeneralTab(contentY);
                break;
            case TAB_TYPE:
                initTypeTab(contentY);
                break;
            case TAB_TARGET:
                initTargetTab(contentY);
                break;
            case TAB_EFFECTS:
                initEffectsTab(contentY);
                break;
            case TAB_VISUAL:
                initVisualTab(contentY);
                break;
        }

        // ═══════════════════════════════════════════════════════════════════════
        // BOTTOM ROW - Enabled toggle (only on General tab)
        // ═══════════════════════════════════════════════════════════════════════
        if (activeTab == TAB_GENERAL) {
            addLabel(new GuiNpcLabel(99, "gui.enabled", guiLeft + 8, guiTop + ySize - 18));
            GuiNpcButton enabledBtn = new GuiNpcButton(2, guiLeft + 55, guiTop + ySize - 24, 40, 20, new String[]{"gui.no", "gui.yes"}, enabled ? 1 : 0);
            enabledBtn.setTextColor(enabled ? 0x00FF00 : 0xFF0000);
            addButton(enabledBtn);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GENERAL TAB
    // ═══════════════════════════════════════════════════════════════════════════

    private void initGeneralTab(int startY) {
        int y = startY;
        int col1LabelX = guiLeft + 8;
        int col1FieldX = guiLeft + 70;
        int col2LabelX = guiLeft + 180;
        int col2FieldX = guiLeft + 248;

        // Row 1: Type (read-only) + Name
        addLabel(new GuiNpcLabel(1, "gui.type", col1LabelX, y + 5));
        addLabel(new GuiNpcLabel(2, ability.getTypeId(), col1FieldX, y + 5));

        addLabel(new GuiNpcLabel(3, "gui.name", col2LabelX, y + 5));
        GuiNpcTextField nameField = new GuiNpcTextField(1, this, fontRendererObj, col2FieldX, y, 100, 20, name);
        addTextField(nameField);

        y += 24;

        // Row 2: Weight + Lock Movement
        addLabel(new GuiNpcLabel(4, "ability.weight", col1LabelX, y + 5));
        GuiNpcTextField weightField = new GuiNpcTextField(3, this, fontRendererObj, col1FieldX, y, 40, 20, String.valueOf(weight));
        weightField.setIntegersOnly();
        weightField.setMinMaxDefault(1, 1000, 10);
        addTextField(weightField);

        addLabel(new GuiNpcLabel(5, "ability.lockMove", col2LabelX, y + 5));
        GuiNpcButton lockMoveBtn = new GuiNpcButton(16, col2FieldX, y, 60, 20, LockMovementType.getDisplayKeys(), lockMovement.ordinal());
        lockMoveBtn.setHoverText("ability.hover.lockMove");
        addButton(lockMoveBtn);

        y += 24;

        // Row 3: Interruptible + Dazed Ticks (only if interruptible)
        addLabel(new GuiNpcLabel(6, "ability.interruptible", col1LabelX, y + 5));
        GuiNpcButton interruptBtn = new GuiNpcButton(14, guiLeft + 85, y, 40, 20, new String[]{"gui.no", "gui.yes"}, interruptible ? 1 : 0);
        interruptBtn.setHoverText("ability.hover.interruptible");
        addButton(interruptBtn);

        // Only show dazed ticks if interruptible is enabled
        if (interruptible) {
            addLabel(new GuiNpcLabel(12, "ability.dazed", col2LabelX, y + 5));
            GuiNpcTextField dazedField = new GuiNpcTextField(13, this, fontRendererObj, col2FieldX, y, 40, 20, String.valueOf(dazedTicks));
            dazedField.setIntegersOnly();
            dazedField.setMinMaxDefault(0, 1000, 80);
            addTextField(dazedField);
        }

        y += 28;

        // Separator - Timing section header
        addLabel(new GuiNpcLabel(7, "ability.timing", col1LabelX, y));
        y += 14;

        // Check if windup animation is selected (either by name or ID)
        boolean hasWindupAnimation = windUpAnimationId >= 0 || (windUpAnimationName != null && !windUpAnimationName.isEmpty());

        // Row 4: Windup Ticks + Cooldown Ticks
        addLabel(new GuiNpcLabel(10, "ability.windup", col1LabelX, y + 5));

        // Only show windup textbox if sync is off or no animation selected
        if (!syncWindupWithAnimation || !hasWindupAnimation) {
            GuiNpcTextField windupField = new GuiNpcTextField(11, this, fontRendererObj, col1FieldX, y, 40, 20, String.valueOf(windUpTicks));
            windupField.setIntegersOnly();
            windupField.setMinMaxDefault(0, 1000, 20);
            addTextField(windupField);
        } else {
            // Show calculated value as label when synced
            addLabel(new GuiNpcLabel(11, String.valueOf(windUpTicks), col1FieldX + 5, y + 5));
        }

        // Sync toggle button - only show if windup animation is selected
        if (hasWindupAnimation) {
            String syncLabel = syncWindupWithAnimation ? "ability.syncOn" : "ability.syncOff";
            GuiNpcButton syncBtn = new GuiNpcButton(17, guiLeft + 115, y, 50, 20, syncLabel);
            syncBtn.setHoverText("ability.hover.sync");
            addButton(syncBtn);
        }

        addLabel(new GuiNpcLabel(13, "ability.cooldown", col2LabelX, y + 5));
        GuiNpcTextField cooldownField = new GuiNpcTextField(10, this, fontRendererObj, col2FieldX, y, 40, 20, String.valueOf(cooldownTicks));
        cooldownField.setIntegersOnly();
        cooldownField.setMinMaxDefault(0, 10000, 0);
        addTextField(cooldownField);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TYPE TAB - Override in subclasses for type-specific settings
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Override this method to render type-specific settings in the Type tab.
     * Use the helper methods createFloatField(), createIntField(), etc.
     * Button/TextField IDs should start at 100.
     *
     * @param startY The Y position to start rendering from
     */
    protected void initTypeTab(int startY) {
        // Default: show message that no type settings are available
        addLabel(new GuiNpcLabel(100, "ability.noTypeSettings", guiLeft + 8, startY + 5));
    }

    /**
     * Override this method to handle button clicks for type-specific buttons.
     * Button IDs 100+ are reserved for type-specific use.
     */
    protected void handleTypeButton(int id, GuiNpcButton button) {
        // Default: no-op
    }

    /**
     * Override this method to handle text field changes for type-specific fields.
     * Field IDs 100+ are reserved for type-specific use.
     */
    protected void handleTypeTextField(int id, GuiNpcTextField field) {
        // Default: no-op
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VISUAL TAB - Override in subclasses for visual settings (colors, etc.)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Override to return true if this ability has visual settings (colors, glow, etc.).
     * When true, a Visual tab will be shown in the GUI.
     */
    protected boolean hasVisualSettings() {
        return false;
    }

    /**
     * Override this method to render visual settings in the Visual tab.
     * Use button/text field IDs starting at 200 for visual settings.
     *
     * @param startY The Y position to start rendering from
     */
    protected void initVisualTab(int startY) {
        // Default: show message that no visual settings are available
        addLabel(new GuiNpcLabel(200, "ability.noVisualSettings", guiLeft + 8, startY + 5));
    }

    /**
     * Override this method to handle button clicks for visual-specific buttons.
     * Button IDs 200+ are reserved for visual-specific use.
     */
    protected void handleVisualButton(int id, GuiNpcButton button) {
        // Default: no-op
    }

    /**
     * Override this method to handle text field changes for visual-specific fields.
     * Field IDs 200+ are reserved for visual-specific use.
     */
    protected void handleVisualTextField(int id, GuiNpcTextField field) {
        // Default: no-op
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TARGET TAB
    // ═══════════════════════════════════════════════════════════════════════════

    private void initTargetTab(int startY) {
        int y = startY;
        int col1LabelX = guiLeft + 8;
        int col1FieldX = guiLeft + 70;
        int col2LabelX = guiLeft + 180;
        int col2FieldX = guiLeft + 248;

        // Row 1: Min Range + Max Range
        addLabel(new GuiNpcLabel(20, "ability.minRange", col1LabelX, y + 5));
        GuiNpcTextField minRangeField = new GuiNpcTextField(5, this, fontRendererObj, col1FieldX, y, 40, 20, String.valueOf((int) minRange));
        minRangeField.setIntegersOnly();
        minRangeField.setMinMaxDefault(0, 100, 0);
        addTextField(minRangeField);

        addLabel(new GuiNpcLabel(21, "ability.maxRange", col2LabelX, y + 5));
        GuiNpcTextField maxRangeField = new GuiNpcTextField(6, this, fontRendererObj, col2FieldX, y, 40, 20, String.valueOf((int) maxRange));
        maxRangeField.setIntegersOnly();
        maxRangeField.setMinMaxDefault(1, 100, 20);
        addTextField(maxRangeField);

        y += 24;

        // Row 2: Targeting Mode (if not locked)
        addLabel(new GuiNpcLabel(22, "ability.targeting", col1LabelX, y + 5));
        if (!targetingModeLocked) {
            String[] targetingModes = getAvailableTargetingModes();
            int selectedIndex = getTargetingModeIndex(targetingMode);
            GuiNpcButton targetBtn = new GuiNpcButton(4, col1FieldX, y, 90, 20, targetingModes, selectedIndex);
            targetBtn.setHoverText("ability.hover.targeting");
            addButton(targetBtn);
        } else {
            String modeName = "ability.target." + targetingMode.name().toLowerCase();
            addLabel(new GuiNpcLabel(23, modeName, col1FieldX, y + 5));
        }

        y += 28;

        // ─────────────────────────────────────────────────────────────────────
        // CONDITIONS SECTION (up to 3)
        // ─────────────────────────────────────────────────────────────────────
        addLabel(new GuiNpcLabel(24, "ability.conditions", col1LabelX, y));
        y += 14;

        // Display existing conditions (similar to Marks pattern)
        for (int i = 0; i < conditions.size() && i < 3; i++) {
            Condition cond = conditions.get(i);
            String condName = getConditionDisplayName(cond);

            // Condition type button
            addButton(new GuiNpcButton(50 + i * 10, col1LabelX, y, 140, 20, condName));

            // Edit button
            addButton(new GuiNpcButton(51 + i * 10, col1LabelX + 145, y, 40, 20, "gui.edit"));

            // Delete button
            addButton(new GuiNpcButton(52 + i * 10, col1LabelX + 190, y, 20, 20, "X"));

            y += 22;
        }

        // Add button (if less than 3 conditions)
        if (conditions.size() < 3) {
            addButton(new GuiNpcButton(80, col1LabelX, y, 50, 20, "gui.add"));
        }
    }

    private String getConditionDisplayName(Condition cond) {
        if (cond == null) return "None";
        String typeId = cond.getTypeId();
        switch (typeId) {
            case "hp_above":
                return StatCollector.translateToLocal("condition.hp_above") + " " + (int) (getConditionThreshold(cond) * 100) + "%";
            case "hp_below":
                return StatCollector.translateToLocal("condition.hp_below") + " " + (int) (getConditionThreshold(cond) * 100) + "%";
            case "target_hp_above":
                return StatCollector.translateToLocal("condition.target_hp_above") + " " + (int) (getConditionThreshold(cond) * 100) + "%";
            case "target_hp_below":
                return StatCollector.translateToLocal("condition.target_hp_below") + " " + (int) (getConditionThreshold(cond) * 100) + "%";
            case "hit_count":
                Condition.ConditionHitCount hitCount = (Condition.ConditionHitCount) cond;
                return StatCollector.translateToLocal("condition.hit_count") + ": " + hitCount.getRequiredHits() + "/" + hitCount.getWithinTicks() + "t";
            default:
                return typeId;
        }
    }

    private float getConditionThreshold(Condition cond) {
        // Read threshold from NBT since inner classes don't expose it
        NBTTagCompound nbt = cond.writeNBT();
        return nbt.hasKey("threshold") ? nbt.getFloat("threshold") : 0.5f;
    }

    private String[] getAvailableTargetingModes() {
        TargetingMode[] modes = ability.getAllowedTargetingModes();
        if (modes == null) {
            modes = TargetingMode.values();
        }
        String[] result = new String[modes.length];
        for (int i = 0; i < modes.length; i++) {
            result[i] = "ability.target." + modes[i].name().toLowerCase();
        }
        return result;
    }

    private int getTargetingModeIndex(TargetingMode mode) {
        TargetingMode[] modes = ability.getAllowedTargetingModes();
        if (modes == null) {
            return mode.ordinal();
        }
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == mode) return i;
        }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EFFECTS TAB
    // ═══════════════════════════════════════════════════════════════════════════

    private void initEffectsTab(int startY) {
        int y = startY;
        int col1LabelX = guiLeft + 8;
        int col1FieldX = guiLeft + 85;
        int col1ClearX = guiLeft + 160;
        int col2LabelX = guiLeft + 185;
        int col2FieldX = guiLeft + 255;

        // ─────────────────────────────────────────────────────────────────────
        // SOUNDS SECTION
        // ─────────────────────────────────────────────────────────────────────
        addLabel(new GuiNpcLabel(30, "advanced.sounds", col1LabelX, y));
        y += 14;

        // Wind Up Sound
        addLabel(new GuiNpcLabel(31, "ability.windUpSound", col1LabelX, y + 5));
        String windUpSoundDisplay = windUpSound != null && !windUpSound.isEmpty() ? truncateString(windUpSound, 10) : StatCollector.translateToLocal("gui.none");
        addButton(new GuiNpcButton(30, col1FieldX, y, 70, 20, windUpSoundDisplay));
        addButton(new GuiNpcButton(35, col1ClearX, y, 20, 20, "X"));

        // Active Sound
        addLabel(new GuiNpcLabel(32, "ability.activeSound", col2LabelX, y + 5));
        String activeSoundDisplay = activeSound != null && !activeSound.isEmpty() ? truncateString(activeSound, 10) : StatCollector.translateToLocal("gui.none");
        addButton(new GuiNpcButton(31, col2FieldX, y, 70, 20, activeSoundDisplay));
        addButton(new GuiNpcButton(36, col2FieldX + 75, y, 20, 20, "X"));

        y += 26;

        // ─────────────────────────────────────────────────────────────────────
        // ANIMATIONS SECTION
        // ─────────────────────────────────────────────────────────────────────
        addLabel(new GuiNpcLabel(33, "menu.animations", col1LabelX, y));
        y += 14;

        // Wind Up Animation
        addLabel(new GuiNpcLabel(34, "ability.windUpAnim", col1LabelX, y + 5));
        String windUpAnimName = getAnimationName(windUpAnimationId, true);
        addButton(new GuiNpcButton(32, col1FieldX, y, 70, 20, windUpAnimName));
        addButton(new GuiNpcButton(37, col1ClearX, y, 20, 20, "X"));

        // Active Animation
        addLabel(new GuiNpcLabel(35, "ability.activeAnim", col2LabelX, y + 5));
        String activeAnimName = getAnimationName(activeAnimationId, false);
        addButton(new GuiNpcButton(33, col2FieldX, y, 70, 20, activeAnimName));
        addButton(new GuiNpcButton(38, col2FieldX + 75, y, 20, 20, "X"));

        y += 26;

        // ─────────────────────────────────────────────────────────────────────
        // TELEGRAPH SECTION (only if supported)
        // ─────────────────────────────────────────────────────────────────────
        if (supportsTelegraph) {
            addLabel(new GuiNpcLabel(40, "ability.telegraph", col1LabelX, y));
            y += 14;

            // Show Telegraph
            addLabel(new GuiNpcLabel(41, "ability.showTelegraph", col1LabelX, y + 5));
            GuiNpcButton telegraphBtn = new GuiNpcButton(20, guiLeft + 95, y, 40, 20, new String[]{"gui.no", "gui.yes"}, showTelegraph ? 1 : 0);
            telegraphBtn.setHoverText("ability.hover.showTelegraph");
            addButton(telegraphBtn);

            // Telegraph Type (separate column)
            String typeKey = ability.getTelegraphType().name().toLowerCase();
            addLabel(new GuiNpcLabel(42, "ability.telegraphType", guiLeft + 200, y + 5));
            String telegraphLangKey = typeKey.equals("none") ? "gui.none" : "telegraph." + typeKey;
            addLabel(new GuiNpcLabel(43, telegraphLangKey, guiLeft + 280, y + 5));

            y += 24;

            // Colors - only show when telegraph is enabled
            if (showTelegraph) {
                addLabel(new GuiNpcLabel(44, "ability.windUpColor", col1LabelX, y + 5));
                String windUpHex = String.format("%06X", windUpColor & 0xFFFFFF);
                GuiNpcButton windUpColorBtn = new GuiNpcButton(22, guiLeft + 95, y, 55, 20, windUpHex);
                windUpColorBtn.setTextColor(windUpColor & 0xFFFFFF);
                addButton(windUpColorBtn);

                addLabel(new GuiNpcLabel(45, "ability.activeColor", guiLeft + 200, y + 5));
                String activeHex = String.format("%06X", activeColor & 0xFFFFFF);
                GuiNpcButton activeColorBtn = new GuiNpcButton(24, guiLeft + 280, y, 55, 20, activeHex);
                activeColorBtn.setTextColor(activeColor & 0xFFFFFF);
                addButton(activeColorBtn);
            }
        }
    }

    private String truncateString(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        int lastSlash = str.lastIndexOf('/');
        if (lastSlash >= 0 && str.length() - lastSlash <= maxLen) {
            return "..." + str.substring(lastSlash);
        }
        return "..." + str.substring(str.length() - maxLen + 3);
    }

    private String getAnimationName(int animId, boolean isWindUp) {
        String animName = isWindUp ? windUpAnimationName : activeAnimationName;
        // Check if we have a built-in animation (by name)
        if (animName != null && !animName.isEmpty()) {
            return truncateString(animName, 8);
        }
        // Check if we have a user animation (by ID)
        if (animId >= 0) {
            return "ID: " + animId;
        }
        return StatCollector.translateToLocal("gui.none");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUTTON EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Tab switching
        if (id == 90) {
            activeTab = TAB_GENERAL;
            initGui();
            return;
        }
        if (id == 91 && hasTypeSettings) {
            activeTab = TAB_TYPE;
            initGui();
            return;
        }
        if (id == 92) {
            activeTab = TAB_TARGET;
            initGui();
            return;
        }
        if (id == 93) {
            activeTab = TAB_EFFECTS;
            initGui();
            return;
        }
        if (id == 94 && hasVisualSettings) {
            activeTab = TAB_VISUAL;
            initGui();
            return;
        }

        // Enabled button (bottom left)
        if (id == 2) {
            enabled = ((GuiNpcButton) guibutton).getValue() == 1;
            initGui();
        }

        // General tab buttons
        else if (id == 14) {
            interruptible = ((GuiNpcButton) guibutton).getValue() == 1;
            initGui(); // Refresh to show/hide dazed ticks field
        } else if (id == 16) {
            lockMovement = LockMovementType.fromOrdinal(((GuiNpcButton) guibutton).getValue());
        } else if (id == 17) {
            // Toggle sync on/off
            syncWindupWithAnimation = !syncWindupWithAnimation;
            if (syncWindupWithAnimation) {
                // Calculate windup ticks from animation when turning sync on
                calculateWindupFromAnimation();
            }
            initGui();
        }

        // Target tab - Targeting mode
        else if (id == 4) {
            TargetingMode[] modes = ability.getAllowedTargetingModes();
            int idx = ((GuiNpcButton) guibutton).getValue();
            if (modes != null && idx < modes.length) {
                targetingMode = modes[idx];
            } else {
                targetingMode = TargetingMode.values()[idx];
            }
        }

        // Close button (X in top menu) - use -1000 to avoid any ID conflicts
        else if (id == -1000) {
            close();
            return;
        }

        // Target tab - Condition buttons (50-79)
        else if (id >= 50 && id < 80) {
            int condIndex = (id - 50) / 10;
            int action = (id - 50) % 10;

            if (action == 0 || action == 1) {
                // Click on condition name or Edit button - open edit subgui
                if (condIndex < conditions.size()) {
                    editingConditionIndex = condIndex;
                    setSubGui(new SubGuiConditionEdit(conditions.get(condIndex)));
                }
            } else if (action == 2) {
                // Delete button
                if (condIndex < conditions.size()) {
                    conditions.remove(condIndex);
                    initGui();
                }
            }
        }
        // Add condition button
        else if (id == 80) {
            if (conditions.size() < 3) {
                editingConditionIndex = conditions.size();
                setSubGui(new SubGuiConditionEdit(null));
            }
        }

        // Effects tab - Telegraph
        else if (id == 20) {
            showTelegraph = ((GuiNpcButton) guibutton).getValue() == 1;
            initGui();
        } else if (id == 22) {
            editingColorId = 22;
            setSubGui(new SubGuiColorSelector(windUpColor));
        } else if (id == 24) {
            editingColorId = 24;
            setSubGui(new SubGuiColorSelector(activeColor));
        }

        // Effects tab - Sound buttons
        else if (id == 30) {
            editingSoundId = 30;
            setSubGui(new GuiSoundSelection(windUpSound));
        } else if (id == 31) {
            editingSoundId = 31;
            setSubGui(new GuiSoundSelection(activeSound));
        }
        // Sound clear buttons
        else if (id == 35) {
            windUpSound = "";
            initGui();
        } else if (id == 36) {
            activeSound = "";
            initGui();
        }
        // Animation buttons
        else if (id == 32) {
            editingAnimationId = 32;
            setSubGui(new GuiAnimationSelection(windUpAnimationId, windUpAnimationName));
        } else if (id == 33) {
            editingAnimationId = 33;
            setSubGui(new GuiAnimationSelection(activeAnimationId, activeAnimationName));
        }
        // Animation clear buttons
        else if (id == 37) {
            windUpAnimationId = -1;
            windUpAnimationName = "";
            initGui();
        } else if (id == 38) {
            activeAnimationId = -1;
            activeAnimationName = "";
            initGui();
        }

        // Visual-specific buttons (200+) - delegate to subclass
        else if (id >= 200) {
            handleVisualButton(id, (GuiNpcButton) guibutton);
        }
        // Type-specific buttons (100-199) - delegate to subclass
        else if (id >= 100) {
            handleTypeButton(id, (GuiNpcButton) guibutton);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEXT FIELD EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void unFocused(GuiNpcTextField textField) {
        int id = textField.id;

        // General tab
        if (id == 1) {
            name = textField.getText();
        } else if (id == 3) {
            weight = textField.getInteger();
        }

        // Target tab
        else if (id == 5) {
            minRange = textField.getInteger();
        } else if (id == 6) {
            maxRange = textField.getInteger();
        }

        // Timing (General tab)
        else if (id == 10) {
            cooldownTicks = textField.getInteger();
        } else if (id == 11) {
            windUpTicks = textField.getInteger();
        } else if (id == 13) {
            dazedTicks = textField.getInteger();
        }

        // Visual-specific fields (200+) - delegate to subclass
        else if (id >= 200) {
            handleVisualTextField(id, textField);
        }
        // Type-specific fields (100-199) - delegate to subclass
        else if (id >= 100) {
            handleTypeTextField(id, textField);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUBGUI CLOSED HANDLER
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector) {
            SubGuiColorSelector colorSelector = (SubGuiColorSelector) subgui;
            int rgb = colorSelector.color & 0x00FFFFFF;
            if (editingColorId == 22) {
                windUpColor = 0x80000000 | rgb;
            } else if (editingColorId == 24) {
                activeColor = 0xC0000000 | rgb;
            }
            editingColorId = 0;
            initGui();
        } else if (subgui instanceof GuiSoundSelection) {
            GuiSoundSelection soundSelector = (GuiSoundSelection) subgui;
            if (soundSelector.selectedResource != null) {
                String sound = soundSelector.selectedResource.toString();
                if (editingSoundId == 30) {
                    windUpSound = sound;
                } else if (editingSoundId == 31) {
                    activeSound = sound;
                }
            }
            editingSoundId = 0;
            initGui();
        } else if (subgui instanceof GuiAnimationSelection) {
            GuiAnimationSelection animSelector = (GuiAnimationSelection) subgui;
            if (editingAnimationId == 32) {
                if (animSelector.isBuiltInSelected()) {
                    // Built-in animation selected - use name, clear ID
                    windUpAnimationName = animSelector.selectedBuiltInName;
                    windUpAnimationId = -1;
                } else {
                    // User animation selected - use ID, clear name
                    windUpAnimationId = animSelector.selectedAnimationId;
                    windUpAnimationName = "";
                }
            } else if (editingAnimationId == 33) {
                if (animSelector.isBuiltInSelected()) {
                    // Built-in animation selected - use name, clear ID
                    activeAnimationName = animSelector.selectedBuiltInName;
                    activeAnimationId = -1;
                } else {
                    // User animation selected - use ID, clear name
                    activeAnimationId = animSelector.selectedAnimationId;
                    activeAnimationName = "";
                }
            }
            editingAnimationId = 0;
            initGui();
        } else if (subgui instanceof SubGuiConditionEdit) {
            SubGuiConditionEdit condEdit = (SubGuiConditionEdit) subgui;
            Condition result = condEdit.getResult();
            if (result != null && editingConditionIndex >= 0) {
                if (editingConditionIndex < conditions.size()) {
                    conditions.set(editingConditionIndex, result);
                } else {
                    conditions.add(result);
                }
            }
            editingConditionIndex = -1;
            initGui();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CLOSE - Save before closing
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void close() {
        // Apply all cached values to the ability before closing
        applyToAbility();

        // Notify callback that ability was saved
        callback.onAbilitySaved(ability);

        // Close the GUI
        super.close();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPLY TO ABILITY
    // ═══════════════════════════════════════════════════════════════════════════

    protected void applyToAbility() {
        // General
        ability.setName(name);
        ability.setEnabled(enabled);
        ability.setWeight(weight);
        ability.setLockMovement(lockMovement);
        ability.setInterruptible(interruptible);

        // If sync is enabled and we have a windup animation, recalculate before saving
        boolean hasWindupAnimation = windUpAnimationId >= 0 || (windUpAnimationName != null && !windUpAnimationName.isEmpty());
        if (syncWindupWithAnimation && hasWindupAnimation) {
            calculateWindupFromAnimation();
        }

        // Timing
        ability.setCooldownTicks(cooldownTicks);
        ability.setWindUpTicks(windUpTicks);
        ability.setDazedTicks(dazedTicks);

        // Target
        ability.setMinRange(minRange);
        ability.setMaxRange(maxRange);
        ability.setTargetingMode(targetingMode);

        // Conditions
        ability.getConditions().clear();
        for (Condition c : conditions) {
            ability.addCondition(c);
        }

        // Effects
        ability.setWindUpSound(windUpSound);
        ability.setActiveSound(activeSound);
        ability.setWindUpAnimationId(windUpAnimationId);
        ability.setActiveAnimationId(activeAnimationId);
        ability.setWindUpAnimationName(windUpAnimationName != null ? windUpAnimationName : "");
        ability.setActiveAnimationName(activeAnimationName != null ? activeAnimationName : "");

        // Telegraph
        ability.setShowTelegraph(showTelegraph);
        ability.setWindUpColor(windUpColor);
        ability.setActiveColor(activeColor);

        // Type-specific values are applied directly in handleTypeTextField and handleTypeButton
    }

    /**
     * Called when an ability is loaded from saved presets.
     */
    public void loadAbility(Ability loadedAbility) {
        if (loadedAbility == null) return;

        // Copy all values from loaded ability to current ability
        NBTTagCompound nbt = loadedAbility.writeNBT();
        // Keep the current typeId since we can't change types mid-edit
        nbt.setString("typeId", ability.getTypeId());
        ability.readNBT(nbt);

        // Update cached values
        this.name = ability.getName() != null ? ability.getName() : "";
        this.enabled = ability.isEnabled();
        this.weight = ability.getWeight();
        this.lockMovement = ability.getLockMovement();
        this.interruptible = ability.isInterruptible();

        this.cooldownTicks = ability.getCooldownTicks();
        this.windUpTicks = ability.getWindUpTicks();
        this.dazedTicks = ability.getDazedTicks();

        this.minRange = ability.getMinRange();
        this.maxRange = ability.getMaxRange();
        this.targetingMode = ability.getTargetingMode();
        this.conditions = new ArrayList<>(ability.getConditions());

        this.showTelegraph = ability.isShowTelegraph();
        this.windUpColor = ability.getWindUpColor();
        this.activeColor = ability.getActiveColor();

        initGui();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SYNC ANIMATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Calculates windup ticks from the selected windup animation's total duration.
     */
    private void calculateWindupFromAnimation() {
        if (AnimationController.Instance == null) {
            return;
        }

        Animation animation = null;
        // Check for built-in animation (by name) first
        if (windUpAnimationName != null && !windUpAnimationName.isEmpty()) {
            animation = (Animation) AnimationController.Instance.get(windUpAnimationName);
        }
        // Fall back to user animation (by ID)
        else if (windUpAnimationId >= 0) {
            animation = (Animation) AnimationController.Instance.get(windUpAnimationId);
        }

        if (animation == null || animation.frames.isEmpty()) {
            return;
        }

        // Calculate total duration by summing all frame durations
        int totalDuration = 0;
        for (Frame frame : animation.frames) {
            totalDuration += frame.getDuration();
        }

        // Update the cached value and refresh GUI
        windUpTicks = totalDuration;
        initGui();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS FOR SUBCLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Creates a float text field for type-specific settings.
     */
    protected GuiNpcTextField createFloatField(int id, int x, int y, int width, float value) {
        GuiNpcTextField field = new GuiNpcTextField(id, this, fontRendererObj, x, y, width, 20, String.format("%.1f", value));
        return field;
    }

    /**
     * Creates an integer text field for type-specific settings.
     */
    protected GuiNpcTextField createIntField(int id, int x, int y, int width, int value) {
        GuiNpcTextField field = new GuiNpcTextField(id, this, fontRendererObj, x, y, width, 20, String.valueOf(value));
        field.setIntegersOnly();
        return field;
    }

    /**
     * Parses a float from a text field with a fallback default value.
     */
    protected float parseFloat(GuiNpcTextField field, float defaultValue) {
        try {
            return Float.parseFloat(field.getText());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the ability being edited.
     */
    protected Ability getAbility() {
        return ability;
    }
}
