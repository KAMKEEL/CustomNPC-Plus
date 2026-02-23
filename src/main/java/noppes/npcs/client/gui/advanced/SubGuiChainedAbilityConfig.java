package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityAction;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.data.entry.ChainedAbilityEntry;
import kamkeel.npcs.controllers.data.ability.data.AbilityIconData;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
import kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitySavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.builder.GuiFieldBuilder;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.*;

import java.util.ArrayList;
import noppes.npcs.client.gui.util.IChainedAbilityConfigCallback;


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static kamkeel.npcs.controllers.data.ability.conditions.AbilityCondition.MAX_CONDITIONS;

/**
 * SubGui for editing a {@link ChainedAbility}.
 * Core tabs: General, Entries, Conditions.
 * Additional tabs are injected dynamically by {@link IChainedAbilityFieldProvider}s.
 */
public class SubGuiChainedAbilityConfig extends SubGuiInterface implements ITextfieldListener, ISubGuiListener, IAbilityConfigCallback {

    // ── Declarative field ID ranges ──────────────────────────────────────────
    private static final int DECLARATIVE_ID_START = 1000;
    private static final int CLEAR_ID_START = 2000;
    private static final int LABEL_ID_START = 3000;

    // ── Tab constants (core tabs) ────────────────────────────────────────────
    private static final int TAB_GENERAL = 0;
    private static final int TAB_ENTRIES = 1;
    private static final int TAB_TARGET = 2;
    private static final int CORE_TAB_COUNT = 3;

    // ── Tab button IDs ────────────────────────────────────────────────────────
    private static final int BTN_TAB_GENERAL = 90;
    private static final int BTN_TAB_ENTRIES = 91;
    private static final int BTN_TAB_TARGET = 92;
    private static final int BTN_TAB_EXTRA_BASE = 93;
    private static final int BTN_CLOSE = -1000;

    // ── Entries tab: per-entry ID encoding ─────────────────────────────────────
    // Actual ID = ENTRY_BASE + i * ENTRY_STRIDE + offset
    // offset 0 = name/select button, 1 = delay text field, 2 = up, 3 = down, 4 = delete
    private static final int ENTRY_BASE = 100;
    private static final int ENTRY_END = 200;
    private static final int ENTRY_STRIDE = 10;
    private static final int BTN_ADD_ENTRY = 200;

    // ── Conditions tab: per-condition ID encoding ─────────────────────────────
    // Actual ID = COND_BASE + i * COND_STRIDE + offset
    // offset 0 = name/click, 1 = edit, 2 = delete
    private static final int COND_BASE = 50;
    private static final int COND_STRIDE = 10;
    private static final int COND_END = COND_BASE + MAX_CONDITIONS * COND_STRIDE; // 50 + N*10
    private static final int BTN_ADD_COND = COND_END;

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int L_LABEL_X = 5;
    private static final int ROW_H = 24;
    private static final int MAX_ENTRIES = 10;

    private final ChainedAbility chain;
    private final IChainedAbilityConfigCallback callback;
    private final boolean npcContext;
    private final boolean readOnlyEntries;
    private final List<AbilityAction> npcSlots;
    private int activeTab = TAB_GENERAL;

    private List<FieldDef> fieldDefs;
    private List<ChainedAbilityEntry> entries;
    private List<AbilityCondition> conditions;

    // Dynamic tabs injected by field providers (e.g., "Icon")
    private List<String> extraTabs = new ArrayList<>();

    private GuiFieldBuilder builder;
    private float[] tabScrollY;

    // Entry editing
    private int editingEntryIndex = -1;
    private String pendingTypeId;
    private boolean editingParentAbility = false;

    // Condition editing
    private int editingConditionIndex = -1;

    // Save only on explicit X button, not ESC
    private boolean saveOnClose = false;

    // Track consumed NPC slot indices for deferred removal on save
    private final List<Integer> consumedSlotIndices = new ArrayList<>();

    public SubGuiChainedAbilityConfig(ChainedAbility chain, IChainedAbilityConfigCallback callback) {
        this(chain, callback, false, null, false);
    }

    public SubGuiChainedAbilityConfig(ChainedAbility chain, IChainedAbilityConfigCallback callback,
                                      boolean npcContext, List<AbilityAction> npcSlots) {
        this(chain, callback, npcContext, npcSlots, false);
    }

    public SubGuiChainedAbilityConfig(ChainedAbility chain, IChainedAbilityConfigCallback callback,
                                      boolean npcContext, List<AbilityAction> npcSlots, boolean readOnlyEntries) {
        this.chain = chain;
        this.callback = callback;
        this.npcContext = npcContext;
        this.readOnlyEntries = readOnlyEntries;
        this.npcSlots = npcSlots;
        this.entries = new ArrayList<>(chain.getEntries());
        this.conditions = new ArrayList<>(chain.getConditions());

        buildFieldDefs();

        setBackground("menubg.png", 217);
        xSize = 356;
        ySize = 200;
    }

    private void buildFieldDefs() {
        fieldDefs = new ArrayList<>();

        fieldDefs.add(FieldDef.stringField("gui.name", chain::getName, chain::setName).tab("General"));
        fieldDefs.add(FieldDef.stringField("gui.displayName", chain::getRawDisplayName, chain::setDisplayName).tab("General"));
        // Computed "Valid For" label (derived from child abilities)
        fieldDefs.add(FieldDef.labelField("ability.validFor", () -> {
            UserType ut = computeAllowedBy();
            return "\u00A7e" + StatCollector.translateToLocal("ability.userType." + ut.name());
        }).tab("General"));
        fieldDefs.add(FieldDef.boolField("gui.enabled", chain::isEnabled, chain::setEnabled).tab("General"));
        fieldDefs.add(FieldDef.intField("ability.weight", chain::getWeight, chain::setWeight).range(1, 100).tab("General"));
        fieldDefs.add(FieldDef.boolField("ability.windUpAll", chain::isWindUpAll, chain::setWindUpAll).tab("General"));
        fieldDefs.add(FieldDef.intField("ability.cooldown", chain::getCooldownTicks, chain::setCooldownTicks).range(0, 12000).tab("General"));

        // ── Target tab ───────────────────────────────────────────────
        fieldDefs.add(FieldDef.row(
            FieldDef.floatField("ability.minRange", chain::getMinRange, chain::setMinRange).range(0, 64),
            FieldDef.floatField("ability.maxRange", chain::getMaxRange, chain::setMaxRange).range(0, 64)
        ).tab("Target"));

        // ── Icon tab ──────────────────────────────────────────────
        AbilityIconData chainIcon = AbilityIconData.fromChainedAbility(chain);
        fieldDefs.add(FieldDef.stringField("gui.texture", chainIcon::getTexture, chainIcon::setTexture).tab("Icon"));
        fieldDefs.add(FieldDef.section("ability.icon.section.uv").tab("Icon"));
        fieldDefs.add(FieldDef.intField("ability.icon.x", chainIcon::getIconX, chainIcon::setIconX).tab("Icon").range(0, 4096));
        fieldDefs.add(FieldDef.intField("ability.icon.y", chainIcon::getIconY, chainIcon::setIconY).tab("Icon").range(0, 4096));
        fieldDefs.add(FieldDef.section("gui.size").tab("Icon"));
        fieldDefs.add(FieldDef.intField("gui.width", chainIcon::getWidth, chainIcon::setWidth).tab("Icon").range(1, 256));
        fieldDefs.add(FieldDef.intField("gui.height", chainIcon::getHeight, chainIcon::setHeight).tab("Icon").range(1, 256));
        fieldDefs.add(FieldDef.floatField("gui.scale", chainIcon::getScale, chainIcon::setScale).tab("Icon").range(0.1f, 10.0f));

        // External field providers (e.g., DBC Addon injecting a "DBC" tab)
        if (AbilityController.Instance != null) {
            for (IChainedAbilityFieldProvider provider : AbilityController.Instance.getChainedFieldProviders()) {
                provider.addFieldDefinitions(chain, fieldDefs);
            }
        }

        // Collect extra tab names from provider-injected fields
        Set<String> tabNames = new LinkedHashSet<>();
        for (FieldDef def : fieldDefs) {
            String tab = def.getTab();
            if (tab != null && !tab.isEmpty() && !"General".equals(tab) && !"Target".equals(tab)) {
                tabNames.add(tab);
            }
        }
        extraTabs = new ArrayList<>(tabNames);

        // Size scroll array for all tabs
        tabScrollY = new float[CORE_TAB_COUNT + extraTabs.size()];
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COMPUTED USERTYPE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Compute the allowed UserType from the current (possibly modified) entries list.
     * Mirrors ChainedAbility.getAllowedBy() but uses the local entries.
     */
    private UserType computeAllowedBy() {
        if (entries.isEmpty()) return UserType.BOTH;
        boolean allAllowPlayer = true;
        boolean allAllowNpc = true;
        for (ChainedAbilityEntry entry : entries) {
            Ability a = entry.resolve();
            if (a == null) continue;
            UserType ut = a.getAllowedBy();
            if (!ut.allowsPlayer()) allAllowPlayer = false;
            if (!ut.allowsNpc()) allAllowNpc = false;
        }
        if (allAllowPlayer && allAllowNpc) return UserType.BOTH;
        if (allAllowPlayer) return UserType.PLAYER_ONLY;
        if (allAllowNpc) return UserType.NPC_ONLY;
        return UserType.NPC_ONLY;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INIT GUI
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void initGui() {
        // Commit any focused text field before rebuilding
        GuiNpcTextField.unfocus();

        // Save scroll position
        GuiScrollWindow oldSw = getScrollableGui(0);
        if (oldSw != null && activeTab < tabScrollY.length) {
            tabScrollY[activeTab] = oldSw.nextScrollY;
        }

        super.initGui();

        // Tab buttons — core tabs
        GuiMenuTopButton generalTab = new GuiMenuTopButton(BTN_TAB_GENERAL, guiLeft + 4, guiTop - 17, "menu.general");
        generalTab.active = (activeTab == TAB_GENERAL);
        addTopButton(generalTab);

        GuiMenuTopButton entriesTab = new GuiMenuTopButton(BTN_TAB_ENTRIES, generalTab, "ability.entries");
        entriesTab.active = (activeTab == TAB_ENTRIES);
        addTopButton(entriesTab);

        GuiMenuTopButton targetTab = new GuiMenuTopButton(BTN_TAB_TARGET, entriesTab, "script.target");
        targetTab.active = (activeTab == TAB_TARGET);
        addTopButton(targetTab);

        // Dynamic extra tabs from field providers
        GuiMenuTopButton prevTab = targetTab;
        for (int i = 0; i < extraTabs.size(); i++) {
            int tabIndex = CORE_TAB_COUNT + i;
            GuiMenuTopButton extraTab = new GuiMenuTopButton(BTN_TAB_EXTRA_BASE + i, prevTab, extraTabs.get(i));
            extraTab.active = (activeTab == tabIndex);
            addTopButton(extraTab);
            prevTab = extraTab;
        }

        GuiMenuTopButton closeBtn = new GuiMenuTopButton(BTN_CLOSE, guiLeft + xSize - 22, guiTop - 17, "X");
        addTopButton(closeBtn);

        // Scroll window dimensions
        int swX = guiLeft + 4;
        int swY = guiTop + 5;
        int swW = xSize - 8;
        int swH = ySize - 10;

        if (activeTab == TAB_GENERAL) {
            buildFieldDefTab("General", swX, swY, swW, swH);
        } else if (activeTab == TAB_ENTRIES) {
            buildEntriesTab(swX, swY, swW, swH);
        } else if (activeTab == TAB_TARGET) {
            buildTargetTab(swX, swY, swW, swH);
        } else if (activeTab >= CORE_TAB_COUNT) {
            int extraIndex = activeTab - CORE_TAB_COUNT;
            if (extraIndex < extraTabs.size()) {
                buildFieldDefTab(extraTabs.get(extraIndex), swX, swY, swW, swH);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FIELDDEF-BASED TAB (General + dynamic provider tabs)
    // ═══════════════════════════════════════════════════════════════════════════

    private void buildFieldDefTab(String tabName, int swX, int swY, int swW, int swH) {
        List<FieldDef> tabFields = new ArrayList<>();
        for (FieldDef def : fieldDefs) {
            if (tabName.equals(def.getTab()) && def.isVisible()) {
                tabFields.add(def);
            }
        }

        builder = new GuiFieldBuilder(this, fontRendererObj);
        builder.startIds(DECLARATIVE_ID_START, CLEAR_ID_START, LABEL_ID_START);
        builder.startY(5);

        GuiScrollWindow sw = builder.buildScrollWindow(tabFields, swX, swY, swW, swH);
        restoreScroll(sw);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ENTRIES TAB
    // ═══════════════════════════════════════════════════════════════════════════

    private void buildEntriesTab(int swX, int swY, int swW, int swH) {
        GuiScrollWindow sw = new GuiScrollWindow(this, swX, swY, swW, swH, 0);
        sw.backgroundColor = 0x88000000;
        addScrollableGui(0, sw);

        int y = 5;
        int labelCounter = 0;

        for (int i = 0; i < entries.size() && i < MAX_ENTRIES; i++) {
            ChainedAbilityEntry entry = entries.get(i);

            // Index label
            sw.addLabel(new GuiNpcLabel(labelCounter++, (i + 1) + ".", L_LABEL_X, y + 5, 0xFFFFFF));

            // Ability reference/inline button
            String btnLabel;
            if (entry.isInline()) {
                Ability a = entry.getInlineAbility();
                btnLabel = a != null ? a.getDisplayName() : "\u00A7cEmpty";
            } else {
                String refName = entry.getAbilityReference();
                if (refName == null || refName.isEmpty()) {
                    btnLabel = StatCollector.translateToLocal("gui.select") + "...";
                } else {
                    Ability resolved = entry.resolve();
                    btnLabel = "\u00A7e> " + (resolved != null ? resolved.getDisplayName() : refName);
                }
            }
            GuiNpcButton entryBtn = new GuiNpcButton(ENTRY_BASE + i * ENTRY_STRIDE, L_LABEL_X + 15, y, 140, 20, btnLabel);
            if (readOnlyEntries) {
                entryBtn.setEnabled(false);
            }
            sw.addButton(entryBtn);

            if (!readOnlyEntries) {
                // Check if this entry is concurrent-active (delay disabled)
                Ability resolvedAbility = entry.resolve();
                boolean isConcurrentActive = resolvedAbility != null
                    && resolvedAbility.isConcurrentCapable()
                    && entry.isConcurrentEnabled();

                // Delay label + field
                sw.addLabel(new GuiNpcLabel(labelCounter++, "ability.delay", L_LABEL_X + 160, y + 5,
                    isConcurrentActive ? 0x555555 : 0xAAAAAA));
                GuiNpcTextField delayField = new GuiNpcTextField(ENTRY_BASE + i * ENTRY_STRIDE + 1, this, fontRendererObj,
                    L_LABEL_X + 195, y, 40, 20, String.valueOf(entry.getDelayTicks()));
                delayField.setIntegersOnly();
                delayField.setMinMaxDefault(0, 6000, 0);
                if (isConcurrentActive) {
                    delayField.setEnabled(false);
                }
                sw.addTextField(delayField);

                // Up button
                if (i > 0) {
                    sw.addButton(new GuiNpcButton(ENTRY_BASE + i * ENTRY_STRIDE + 2, L_LABEL_X + 240, y, 20, 20, "\u2191"));
                }

                // Down button
                if (i < entries.size() - 1) {
                    sw.addButton(new GuiNpcButton(ENTRY_BASE + i * ENTRY_STRIDE + 3, L_LABEL_X + 263, y, 20, 20, "\u2193"));
                }

                // Concurrent toggle (only shown if resolved ability is concurrent-capable, never for first entry)
                if (i > 0 && resolvedAbility != null && resolvedAbility.isConcurrentCapable()) {
                    String cLabel = entry.isConcurrentEnabled() ? "\u00A7aC" : "\u00A77C";
                    GuiNpcButton cBtn = new GuiNpcButton(ENTRY_BASE + i * ENTRY_STRIDE + 5, L_LABEL_X + 288, y, 20, 20, cLabel);
                    cBtn.setHoverText("ability.hover.concurrent");
                    sw.addButton(cBtn);
                }

                // Delete button
                sw.addButton(new GuiNpcButton(ENTRY_BASE + i * ENTRY_STRIDE + 4, L_LABEL_X + 313, y, 20, 20, "X"));
            } else {
                // Read-only: show delay as label only
                int delay = entry.getDelayTicks();
                if (delay > 0) {
                    sw.addLabel(new GuiNpcLabel(labelCounter++, delay + "t", L_LABEL_X + 165, y + 5, 0x888888));
                }
            }

            y += ROW_H;
        }

        // Add entry button (hidden in read-only mode)
        if (!readOnlyEntries && entries.size() < MAX_ENTRIES) {
            sw.addButton(new GuiNpcButton(BTN_ADD_ENTRY, L_LABEL_X, y, 80, 20, "gui.add"));
            y += ROW_H;
        }

        sw.maxScrollY = Math.max(y - swH, 0);
        restoreScroll(sw);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TARGET TAB (FieldDef fields + conditions list)
    // ═══════════════════════════════════════════════════════════════════════════

    private void buildTargetTab(int swX, int swY, int swW, int swH) {
        // Build FieldDef fields (ranges, Valid For) first
        List<FieldDef> tabFields = new ArrayList<>();
        for (FieldDef def : fieldDefs) {
            if ("Target".equals(def.getTab()) && def.isVisible()) {
                tabFields.add(def);
            }
        }

        builder = new GuiFieldBuilder(this, fontRendererObj);
        builder.startIds(DECLARATIVE_ID_START, CLEAR_ID_START, LABEL_ID_START);
        builder.startY(5);

        GuiScrollWindow sw = builder.buildScrollWindow(tabFields, swX, swY, swW, swH);

        // Append conditions list below the FieldDef fields
        int y = builder.getLastBuildY();
        int condY = renderConditions(sw, y, builder.getNextLabelId());
        sw.maxScrollY = Math.max(condY - swH, 0);

        restoreScroll(sw);
    }

    private int renderConditions(GuiScrollWindow sw, int y, int labelCounter) {
        y += 3;
        sw.addLabel(new GuiNpcLabel(labelCounter, "ability.conditions", L_LABEL_X, y + 2, 0xFFFF55));
        y += 15;

        for (int i = 0; i < conditions.size() && i < MAX_CONDITIONS; i++) {
            AbilityCondition cond = conditions.get(i);
            String condName = getConditionDisplayName(cond);
            sw.addButton(new GuiNpcButton(COND_BASE + i * COND_STRIDE, L_LABEL_X, y, 140, 20, condName));
            sw.addButton(new GuiNpcButton(COND_BASE + i * COND_STRIDE + 1, L_LABEL_X + 145, y, 40, 20, "gui.edit"));
            sw.addButton(new GuiNpcButton(COND_BASE + i * COND_STRIDE + 2, L_LABEL_X + 190, y, 20, 20, "X"));
            y += 22;
        }

        if (conditions.size() < MAX_CONDITIONS) {
            sw.addButton(new GuiNpcButton(BTN_ADD_COND, L_LABEL_X, y, 50, 20, "gui.add"));
            y += ROW_H;
        }

        return y;
    }

    private String getConditionDisplayName(AbilityCondition cond) {
        if (cond == null) return "None";
        String name = cond.getName();
        return StatCollector.translateToLocal(name);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SCROLL RESTORE
    // ═══════════════════════════════════════════════════════════════════════════

    private void restoreScroll(GuiScrollWindow sw) {
        if (activeTab < tabScrollY.length) {
            float restored = Math.min(tabScrollY[activeTab], sw.maxScrollY);
            sw.nextScrollY = restored;
            sw.scrollY = restored;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUTTON EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Tab switching — core tabs
        if (id == BTN_TAB_GENERAL) {
            activeTab = TAB_GENERAL;
            initGui();
            return;
        }
        if (id == BTN_TAB_ENTRIES) {
            activeTab = TAB_ENTRIES;
            initGui();
            return;
        }
        if (id == BTN_TAB_TARGET) {
            activeTab = TAB_TARGET;
            initGui();
            return;
        }
        if (id == BTN_CLOSE) {
            saveOnClose = true;
            close();
            return;
        }

        // Tab switching — extra tabs
        if (id >= BTN_TAB_EXTRA_BASE && id < BTN_TAB_EXTRA_BASE + extraTabs.size()) {
            activeTab = CORE_TAB_COUNT + (id - BTN_TAB_EXTRA_BASE);
            initGui();
            return;
        }

        // Entries tab
        if (activeTab == TAB_ENTRIES) {
            if (handleEntryButton(id)) return;
        }

        // Target tab (includes conditions)
        if (activeTab == TAB_TARGET) {
            if (handleConditionButton(id)) return;
        }

        // FieldDef-based tabs (General + extra tabs) — declarative field handling
        if (isFieldDefTab() && builder != null) {
            if (builder.handleButtonEvent(id, guibutton)) {
                if (!hasSubGui()) {
                    initGui();
                }
                return;
            }
        }
    }

    /**
     * Whether the current active tab uses the declarative FieldDef system.
     */
    private boolean isFieldDefTab() {
        return activeTab == TAB_GENERAL || activeTab == TAB_TARGET || activeTab >= CORE_TAB_COUNT;
    }

    private boolean handleEntryButton(int id) {
        // Add entry
        if (id == BTN_ADD_ENTRY) {
            if (entries.size() < MAX_ENTRIES) {
                // Both contexts use entry source selector; hide NPC option in global context
                setSubGui(new SubGuiChainedEntrySource(npcContext));
            }
            return true;
        }

        // Per-entry buttons (ENTRY_BASE to ENTRY_END)
        if (id >= ENTRY_BASE && id < ENTRY_END) {
            int entryIndex = (id - ENTRY_BASE) / ENTRY_STRIDE;
            int action = (id - ENTRY_BASE) % ENTRY_STRIDE;

            if (entryIndex < 0 || entryIndex >= entries.size()) return false;

            switch (action) {
                case 0: // Entry name button — reference: clone/modify dialog; inline: edit config
                    ChainedAbilityEntry clickedEntry = entries.get(entryIndex);
                    if (clickedEntry.isReference()) {
                        // Built-in abilities are always references — no editing allowed
                        Ability refResolved = clickedEntry.resolve();
                        if (refResolved != null && refResolved.isBuiltIn()) return true;
                        editingEntryIndex = entryIndex;
                        setSubGui(new SubGuiAbilityEditMode());
                    } else if (clickedEntry.isInline() && clickedEntry.getInlineAbility() != null) {
                        Ability inlineAbility = clickedEntry.getInlineAbility();
                        if (inlineAbility.isBuiltIn()) return true;
                        editingEntryIndex = entryIndex;
                        setSubGui(new SubGuiAbilityConfig(inlineAbility, this));
                    }
                    return true;
                case 2: // Up
                    if (entryIndex > 0) {
                        // Commit any focused delay field before reordering
                        GuiNpcTextField.unfocus();
                        ChainedAbilityEntry entry = entries.remove(entryIndex);
                        entries.add(entryIndex - 1, entry);
                        // Can't be concurrent at position 0
                        if (entryIndex - 1 == 0) {
                            entry.setConcurrentEnabled(false);
                        }
                        initGui();
                    }
                    return true;
                case 3: // Down
                    if (entryIndex < entries.size() - 1) {
                        // Commit any focused delay field before reordering
                        GuiNpcTextField.unfocus();
                        ChainedAbilityEntry entry = entries.remove(entryIndex);
                        entries.add(entryIndex + 1, entry);
                        initGui();
                    }
                    return true;
                case 4: // Delete
                    // Commit any focused delay field before removing
                    GuiNpcTextField.unfocus();
                    entries.remove(entryIndex);
                    initGui();
                    return true;
                case 5: // Concurrent toggle
                    ChainedAbilityEntry toggleEntry = entries.get(entryIndex);
                    boolean newState = !toggleEntry.isConcurrentEnabled();
                    toggleEntry.setConcurrentEnabled(newState);
                    if (newState) {
                        toggleEntry.setDelayTicks(0);
                    }
                    initGui();
                    return true;
            }
        }

        return false;
    }

    private boolean handleConditionButton(int id) {
        if (id >= COND_BASE && id < COND_END) {
            int condIndex = (id - COND_BASE) / COND_STRIDE;
            int action = (id - COND_BASE) % COND_STRIDE;
            if (action == 0 || action == 1) {
                if (condIndex < conditions.size()) {
                    editingConditionIndex = condIndex;
                    setSubGui(new SubGuiConditionEdit(conditions.get(condIndex)));
                }
            } else if (action == 2) {
                if (condIndex < conditions.size()) {
                    conditions.remove(condIndex);
                    initGui();
                }
            }
            return true;
        }
        if (id == BTN_ADD_COND) {
            if (conditions.size() < MAX_CONDITIONS) {
                editingConditionIndex = conditions.size();
                setSubGui(new SubGuiConditionEdit(null));
            }
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEXT FIELD EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void unFocused(GuiNpcTextField textField) {
        int id = textField.id;

        // FieldDef-based tabs — declarative fields
        if (isFieldDefTab() && builder != null && id >= DECLARATIVE_ID_START) {
            if (builder.handleTextFieldEvent(id, textField)) {
                initGui();
            }
            return;
        }

        // Entries tab - delay fields (ENTRY_BASE + i * ENTRY_STRIDE + 1)
        if (activeTab == TAB_ENTRIES && id >= ENTRY_BASE + 1 && id < ENTRY_END) {
            int entryIndex = (id - ENTRY_BASE - 1) / ENTRY_STRIDE;
            if (entryIndex >= 0 && entryIndex < entries.size()) {
                entries.get(entryIndex).setDelayTicks(textField.getInteger());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SUB GUI CLOSED
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        // FieldDef-based tabs — declarative sub-gui handling
        if (isFieldDefTab() && builder != null) {
            if (builder.handleSubGuiClosed(subgui)) {
                initGui();
                return;
            }
        }

        if (subgui instanceof SubGuiAbilitySelect) {
            handleAbilitySelectClosed((SubGuiAbilitySelect) subgui);
            initGui();
            return;
        }
        if (subgui instanceof SubGuiChainedEntrySource) {
            handleEntrySourceClosed((SubGuiChainedEntrySource) subgui);
            return;
        }
        if (subgui instanceof SubGuiNpcSlotPicker) {
            handleSlotPickerClosed((SubGuiNpcSlotPicker) subgui);
            initGui();
            return;
        }
        if (subgui instanceof SubGuiAbilityVariantSelect) {
            handleVariantSelectClosed((SubGuiAbilityVariantSelect) subgui);
            if (!hasSubGui()) initGui();
            return;
        }
        if (subgui instanceof SubGuiAbilityTypeSelect) {
            handleTypeSelectClosed((SubGuiAbilityTypeSelect) subgui);
            if (!hasSubGui()) initGui();
            return;
        }
        if (subgui instanceof SubGuiAbilityEditMode) {
            handleEditModeClosed((SubGuiAbilityEditMode) subgui);
            if (!hasSubGui()) initGui();
            return;
        }
        if (subgui instanceof SubGuiAbilityConfig) {
            editingEntryIndex = -1;
            editingParentAbility = false;
            initGui();
            return;
        }
        if (subgui instanceof SubGuiConditionEdit) {
            handleConditionEditClosed((SubGuiConditionEdit) subgui);
            initGui();
            return;
        }
    }

    private void handleAbilitySelectClosed(SubGuiAbilitySelect gui) {
        String selectedName = gui.getSelectedName();
        if (selectedName != null) {
            if (editingEntryIndex >= 0 && editingEntryIndex < entries.size()) {
                int delay = entries.get(editingEntryIndex).getDelayTicks();
                entries.set(editingEntryIndex, ChainedAbilityEntry.reference(selectedName, delay));
            } else {
                entries.add(ChainedAbilityEntry.reference(selectedName, 0));
            }
        }
        editingEntryIndex = -1;
    }

    private void handleEntrySourceClosed(SubGuiChainedEntrySource gui) {
        int source = gui.getResult();
        if (source == SubGuiChainedEntrySource.SOURCE_NPC_SLOTS) {
            setSubGui(new SubGuiNpcSlotPicker(npcSlots));
        } else if (source == SubGuiChainedEntrySource.SOURCE_LOAD_PRESET) {
            setSubGui(new SubGuiAbilitySelect(SubGuiAbilitySelect.FILTER_CUSTOM_ONLY));
        } else if (source == SubGuiChainedEntrySource.SOURCE_CREATE_NEW) {
            setSubGui(new SubGuiAbilityTypeSelect());
        } else if (source == SubGuiChainedEntrySource.SOURCE_BUILT_IN) {
            setSubGui(new SubGuiAbilitySelect(SubGuiAbilitySelect.FILTER_BUILTIN_ONLY));
        }
        // Don't initGui — we're opening another SubGui
    }

    private void handleSlotPickerClosed(SubGuiNpcSlotPicker gui) {
        int slotIndex = gui.getSelectedIndex();
        if (slotIndex >= 0 && npcSlots != null && slotIndex < npcSlots.size()) {
            AbilityAction consumed = npcSlots.get(slotIndex);
            Ability ability = consumed.getAbility();
            if (ability != null) {
                if (consumed.getSlotType() == AbilityAction.SlotType.INLINE_ABILITY) {
                    entries.add(ChainedAbilityEntry.inline(ability, 0));
                } else {
                    entries.add(ChainedAbilityEntry.reference(consumed.getReferenceId(), 0));
                }
                consumedSlotIndices.add(slotIndex);
            }
        }
    }

    private void handleTypeSelectClosed(SubGuiAbilityTypeSelect gui) {
        String typeId = gui.getSelectedTypeId();
        if (typeId != null) {
            List<AbilityVariant> variants = AbilityController.Instance.getVariantsForType(typeId);
            if (variants.size() > 1) {
                pendingTypeId = typeId;
                setSubGui(new SubGuiAbilityVariantSelect(variants));
                return;
            }
            Ability newAbility = AbilityController.Instance.create(typeId);
            if (newAbility != null && !newAbility.isBuiltIn()) {
                if (variants.size() == 1) {
                    variants.get(0).apply(newAbility);
                }
                newAbility.setId(java.util.UUID.randomUUID().toString());
                entries.add(ChainedAbilityEntry.inline(newAbility, 0));
                editingEntryIndex = entries.size() - 1;
                setSubGui(new SubGuiAbilityConfig(newAbility, this));
            }
        }
    }

    private void handleVariantSelectClosed(SubGuiAbilityVariantSelect gui) {
        int idx = gui.getSelectedIndex();
        if (idx >= 0 && pendingTypeId != null) {
            Ability newAbility = AbilityController.Instance.create(pendingTypeId);
            if (newAbility != null && !newAbility.isBuiltIn()) {
                gui.getVariants().get(idx).apply(newAbility);
                newAbility.setId(java.util.UUID.randomUUID().toString());
                entries.add(ChainedAbilityEntry.inline(newAbility, 0));
                editingEntryIndex = entries.size() - 1;
                pendingTypeId = null;
                setSubGui(new SubGuiAbilityConfig(newAbility, this));
                return;
            }
        }
        pendingTypeId = null;
    }

    private void handleEditModeClosed(SubGuiAbilityEditMode gui) {
        int mode = gui.getResult();
        if (mode < 0 || editingEntryIndex < 0 || editingEntryIndex >= entries.size()) {
            editingEntryIndex = -1;
            return;
        }

        ChainedAbilityEntry entry = entries.get(editingEntryIndex);

        // Block all editing for built-in abilities
        Ability preCheck = entry.resolve();
        if (preCheck != null && preCheck.isBuiltIn()) {
            editingEntryIndex = -1;
            return;
        }

        if (mode == SubGuiAbilityEditMode.MODE_CLONE_MODIFY) {
            if (entry.convertToInline()) {
                Ability a = entry.getInlineAbility();
                if (a != null && !a.isBuiltIn()) {
                    a.setId(java.util.UUID.randomUUID().toString());
                    setSubGui(new SubGuiAbilityConfig(a, this));
                    return;
                }
            }
            editingEntryIndex = -1;
        } else if (mode == SubGuiAbilityEditMode.MODE_MODIFY_PARENT) {
            Ability resolved = entry.resolve();
            if (resolved != null && !resolved.isBuiltIn()) {
                editingParentAbility = true;
                setSubGui(resolved.createConfigGui(this));
                return;
            }
            editingEntryIndex = -1;
        }
    }

    private void handleConditionEditClosed(SubGuiConditionEdit gui) {
        AbilityCondition result = gui.getResult();
        if (result != null && editingConditionIndex >= 0) {
            if (editingConditionIndex < conditions.size()) {
                conditions.set(editingConditionIndex, result);
            } else {
                conditions.add(result);
            }
        }
        editingConditionIndex = -1;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CLOSE / APPLY
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void onAbilitySaved(Ability ability) {
        if (editingParentAbility) {
            // Persist the global parent ability to the server
            PacketClient.sendClient(new CustomAbilitySavePacket(ability.writeNBT()));
            editingParentAbility = false;
            return;
        }
        // Inline ability mutated in-place; entry already holds the reference
    }

    @Override
    public void close() {
        if (saveOnClose) {
            GuiNpcTextField.unfocus();
            applyToChain();
            callback.onChainedAbilitySaved(chain);
        }
        super.close();
    }


    private void applyToChain() {
        // Entries
        chain.getEntries().clear();
        for (ChainedAbilityEntry entry : entries) {
            chain.addEntry(entry);
        }

        // Conditions
        chain.getConditions().clear();
        for (AbilityCondition c : conditions) {
            chain.getConditions().add(c);
        }

        // Remove consumed NPC slots (descending order to preserve indices)
        if (npcSlots != null && !consumedSlotIndices.isEmpty()) {
            Collections.sort(consumedSlotIndices, Collections.reverseOrder());
            for (int idx : consumedSlotIndices) {
                if (idx < npcSlots.size()) {
                    npcSlots.remove(idx);
                }
            }
        }
    }
}
