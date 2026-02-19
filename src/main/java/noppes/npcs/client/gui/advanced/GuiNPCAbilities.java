package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityAction;
import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import kamkeel.npcs.controllers.data.ability.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.ChainedAbilityEntry;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.AbilitiesGetAllPacket;
import kamkeel.npcs.network.packets.request.ability.AbilitiesNpcGetPacket;
import kamkeel.npcs.network.packets.request.ability.AbilitiesNpcSavePacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitiesGetPacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitySavePacket;
import kamkeel.npcs.util.Register;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.util.IChainedAbilityConfigCallback;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

/**
 * GUI for managing NPC abilities.
 * Shows list of assigned abilities with add/remove/edit controls.
 */
public class GuiNPCAbilities extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener, IGuiData, ITextfieldListener, ISubGuiListener, IAbilityConfigCallback, IChainedAbilityConfigCallback {

    // ── Button IDs ────────────────────────────────────────────────────────────
    private static final int BTN_SCROLL_TYPE = 50;
    private static final int BTN_ADD_ABILITY = 70;
    private static final int BTN_REMOVE = 71;
    private static final int BTN_EDIT = 72;
    private static final int BTN_MOVE_UP = 73;
    private static final int BTN_MOVE_DOWN = 74;
    private static final int BTN_LOAD = 75;
    private static final int BTN_SAVE_PRESET = 76;
    private static final int BTN_TOGGLE_SLOT = 77;
    private static final int BTN_ADD_CHAIN = 78;
    private static final int BTN_ENABLED = 100;

    // ── Scroll IDs ────────────────────────────────────────────────────────────
    private static final int SCROLL_TYPES = 0;
    private static final int SCROLL_NPC = 1;

    // ── TextField / Label IDs ─────────────────────────────────────────────────
    private static final int TF_MIN_COOLDOWN = 101;
    private static final int TF_MAX_COOLDOWN = 102;
    private static final int TF_SEARCH = 4;
    private static final int LBL_TYPES = 1;
    private static final int LBL_NPC = 2;
    private static final int LBL_MIN_COOLDOWN = 101;
    private static final int LBL_MAX_COOLDOWN = 102;

    // ── Scroll lists ──────────────────────────────────────────────────────────
    private GuiCustomScroll availableTypesScroll;
    private GuiCustomScroll npcAbilitiesScroll;

    // ── Available type data ───────────────────────────────────────────────────
    private final HashMap<String, Integer> allAbilityTypes = new HashMap<>();
    private final HashMap<String, Integer> filteredAbilityTypes = new HashMap<>();
    private final HashMap<String, String> displayNameToTypeId = new HashMap<>();

    // ── NPC slot data ─────────────────────────────────────────────────────────
    private final List<AbilityAction> npcSlots = new ArrayList<>();
    private final List<int[]> rowMapping = new ArrayList<>(); // [slotIdx, entryIdx], entryIdx=-1 = header/standalone

    // ── NPC settings ──────────────────────────────────────────────────────────
    private boolean abilitiesEnabled = false;
    private int minCooldown = 20;
    private int maxCooldown = 60;

    // ── Selection state ───────────────────────────────────────────────────────
    private String search = "";
    private int selectedAbilityIndex = -1;
    private int selectedSlotIndex = -1;
    private int selectedEntryIndex = -1;

    // ── Pending: chain editing ────────────────────────────────────────────────
    private ChainedAbility pendingChain = null;
    private int pendingChainSlotIdx = -1;

    // ── Pending: inline chain-entry editing ──────────────────────────────────
    private boolean editingChainEntry = false;
    private int editChainSlotIdx = -1;
    private int editChainEntryIdx = -1;
    private boolean editingChainEntryParent = false;

    // ── Pending: save-as-preset ───────────────────────────────────────────────
    private int pendingSaveSlotIdx = -1;
    private int pendingSaveEntryIdx = -1; // -1 for standalone ability
    private final Set<String> existingPresetNames = new HashSet<>();

    // ── Pending: variant selection ────────────────────────────────────────────
    private String pendingTypeId = null;

    // ── Static view state (persists across GUI opens) ─────────────────────────
    public static int modIndex = 0;
    public static ScrollType scrollType = ScrollType.CNPC;

    public GuiNPCAbilities(EntityNPCInterface npc) {
        super(npc);
        PacketClient.sendClient(new AbilitiesGetAllPacket());
        PacketClient.sendClient(new AbilitiesNpcGetPacket());
        PacketClient.sendClient(new CustomAbilitiesGetPacket());
    }

    public enum ScrollType {
        CNPC,
        MODDED,
        ALL;

        @Override
        public String toString() {
            switch (this) {
                case CNPC:
                    return "CustomNPCs";
                case MODDED:
                    if (Register.isEmpty("ability"))
                        return "modded";

                    String namespace = Register.REGISTERED_NAMESPACES.get("ability").get(modIndex);
                    String displayName = Register.NAMESPACE_DISPLAY_NAMES.get(namespace);
                    return displayName != null ? displayName : namespace;
                case ALL:
                    return "filter.all";
                default:
                    return name();
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;

        addButton(new GuiNpcButton(BTN_SCROLL_TYPE, guiLeft + 5, y, 140, 20, scrollType.toString()));

        // Cooldown range: Min Cooldown and Max Cooldown (spaced apart more)
        addLabel(new GuiNpcLabel(LBL_MIN_COOLDOWN, "ability.minCooldown", guiLeft + 210, y + 5));
        GuiNpcTextField minField = new GuiNpcTextField(TF_MIN_COOLDOWN, this, fontRendererObj, guiLeft + 260, y, 40, 20, "" + minCooldown);
        minField.setIntegersOnly();
        minField.setMinMaxDefault(0, 10000, 20);
        addTextField(minField);

        addLabel(new GuiNpcLabel(LBL_MAX_COOLDOWN, "ability.maxCooldown", guiLeft + 320, y + 5));
        GuiNpcTextField maxField = new GuiNpcTextField(TF_MAX_COOLDOWN, this, fontRendererObj, guiLeft + 370, y, 40, 20, "" + maxCooldown);
        maxField.setIntegersOnly();
        maxField.setMinMaxDefault(0, 10000, 60);
        addTextField(maxField);

        y += 28;

        // Enabled checkbox
        addButton(new GuiNpcButton(BTN_ENABLED, guiLeft + 334, y + 145, 76, 20, new String[]{"gui.disabled", "gui.enabled"}, abilitiesEnabled ? 1 : 0));
        getButton(BTN_ENABLED).packedFGColour = abilitiesEnabled ? 0x00FF00 : 0xFF0000;

        // Left scroll: available ability types
        addLabel(new GuiNpcLabel(LBL_TYPES, "ability.availableTypes", guiLeft + 5, y));
        if (availableTypesScroll == null) {
            availableTypesScroll = new GuiCustomScroll(this, SCROLL_TYPES);
            availableTypesScroll.setSize(140, 130);
        }
        availableTypesScroll.guiLeft = guiLeft + 5;
        availableTypesScroll.guiTop = y + 12;
        availableTypesScroll.setUnsortedList(getFilteredTypeList());
        addScroll(availableTypesScroll);

        // Search bar for types
        addTextField(new GuiNpcTextField(TF_SEARCH, this, fontRendererObj, guiLeft + 5, y + 145, 140, 18, search));

        // Right scroll: NPC's abilities (widened to use available space)
        addLabel(new GuiNpcLabel(LBL_NPC, "ability.npcAbilities", guiLeft + 210, y));
        if (npcAbilitiesScroll == null) {
            npcAbilitiesScroll = new GuiCustomScroll(this, SCROLL_NPC);
            npcAbilitiesScroll.setSize(200, 130);
        } else {
            npcAbilitiesScroll.setSize(200, 130);
        }
        npcAbilitiesScroll.guiLeft = guiLeft + 210;
        npcAbilitiesScroll.guiTop = y + 12;
        updateNpcAbilitiesList();
        addScroll(npcAbilitiesScroll);

        // Center buttons: Add/Remove
        int centerX = guiLeft + 158;
        addButton(new GuiNpcButton(BTN_ADD_ABILITY, centerX, y + 15, 40, 20, ">>>")); // Add
        addButton(new GuiNpcButton(BTN_REMOVE, centerX, y + 37, 40, 20, "<<<")); // Remove
        getButton(BTN_REMOVE).setEnabled(selectedAbilityIndex >= 0);

        // Load button (under the add/remove arrows)
        addButton(new GuiNpcButton(BTN_LOAD, centerX, y + 60, 40, 20, "gui.load"));

        // On/Off toggle for selected slot (under Load button) — chains and abilities
        if (selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size() && selectedEntryIndex == -1) {
            AbilityAction selectedSlot = npcSlots.get(selectedSlotIndex);
            boolean isEnabled = selectedSlot.isSlotEnabled();
            GuiNpcButton toggleBtn = new GuiNpcButton(BTN_TOGGLE_SLOT, centerX, y + 112, 40, 20,
                new String[]{"gui.off", "gui.on"}, isEnabled ? 1 : 0);
            addButton(toggleBtn);
        }

        // Check if selected slot is editable
        boolean selectedIsBuiltIn = false;
        boolean selectedIsChainEntry = selectedEntryIndex >= 0;
        boolean selectedIsRefChainEntry = false;
        if (selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size()) {
            AbilityAction sel = npcSlots.get(selectedSlotIndex);
            if (!selectedIsChainEntry && !sel.isChain()) {
                Ability a = sel.getAbility();
                if (a != null && a.isBuiltIn()) selectedIsBuiltIn = true;
            }
            // Chain entries referencing built-in abilities are not editable
            if (selectedIsChainEntry && sel.isChain()) {
                ChainedAbility biChain = sel.isInlineChain() ? sel.getInlineChain() : sel.getChainedAbility();
                if (biChain != null && selectedEntryIndex < biChain.getEntries().size()) {
                    Ability resolved = biChain.getEntries().get(selectedEntryIndex).resolve();
                    if (resolved != null && resolved.isBuiltIn()) selectedIsBuiltIn = true;
                }
            }
            // Sub-entries of reference chains are not directly editable from the NPC list
            if (selectedIsChainEntry && sel.isChainReference()) {
                selectedIsRefChainEntry = true;
            }
        }

        // Right side buttons: Edit and Up/Down carrots with Save button
        addButton(new GuiNpcButton(BTN_EDIT, guiLeft + 210, y + 145, 55, 20, "gui.edit"));
        getButton(BTN_EDIT).setEnabled(selectedSlotIndex >= 0 && !selectedIsBuiltIn && !selectedIsRefChainEntry);

        // Up/Down carrot buttons
        addButton(new GuiNpcButton(BTN_MOVE_UP, guiLeft + 270, y + 145, 20, 20, "<"));
        addButton(new GuiNpcButton(BTN_MOVE_DOWN, guiLeft + 292, y + 145, 20, 20, ">"));
        getButton(BTN_MOVE_UP).setEnabled(selectedSlotIndex > 0 && selectedEntryIndex == -1);
        getButton(BTN_MOVE_DOWN).setEnabled(selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size() - 1 && selectedEntryIndex == -1);

        // Save button — enabled for inline abilities (standalone or chain entry), disabled for references/chains
        addButton(new GuiNpcButton(BTN_SAVE_PRESET, centerX, y + 82, 40, 20, "gui.save"));
        boolean canSave = false;
        if (selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size() && !selectedIsBuiltIn) {
            AbilityAction saveSlot = npcSlots.get(selectedSlotIndex);
            if (selectedEntryIndex >= 0) {
                // Chain entry: only inline entries can be saved
                ChainedAbility saveChain = saveSlot.isInlineChain() ? saveSlot.getInlineChain() : null;
                if (saveChain != null && selectedEntryIndex < saveChain.getEntries().size()) {
                    canSave = saveChain.getEntries().get(selectedEntryIndex).isInline();
                }
            } else if (!saveSlot.isChain() && !saveSlot.isReference()) {
                // Standalone inline ability
                canSave = true;
            }
        }
        getButton(BTN_SAVE_PRESET).setEnabled(canSave);

        // Add Chain button
        addButton(new GuiNpcButton(BTN_ADD_CHAIN, centerX, y + 135, 40, 20, "ability.addChain"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Enabled toggle
        if (id == BTN_ENABLED) {
            abilitiesEnabled = ((GuiNpcButton) guibutton).getValue() == 1;
            initGui();
            save();
            return;
        }

        // Add ability
        if (id == BTN_ADD_ABILITY) {
            if (availableTypesScroll.hasSelected()) {
                String displayName = availableTypesScroll.getSelected();
                String typeId = displayNameToTypeId.get(displayName);
                if (typeId != null) {
                    java.util.List<AbilityVariant> variants = AbilityController.Instance.getVariantsForType(typeId);
                    if (variants.size() > 1) {
                        pendingTypeId = typeId;
                        setSubGui(new SubGuiAbilityVariantSelect(variants));
                        return;
                    }
                    Ability newAbility = AbilityController.Instance.create(typeId);
                    if (newAbility != null) {
                        if (variants.size() == 1) {
                            variants.get(0).apply(newAbility);
                        }
                        newAbility.setId(UUID.randomUUID().toString());
                        npcSlots.add(AbilityAction.inline(newAbility));
                        selectedAbilityIndex = npcSlots.size() - 1;
                        updateNpcAbilitiesList();
                        selectAbilityByIndex(selectedAbilityIndex);
                        initGui();
                        save();
                    }
                }
            }
            return;
        }

        // Remove slot or chain entry
        if (id == BTN_REMOVE) {
            if (selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size()) {
                if (selectedEntryIndex >= 0) {
                    // Remove entry from chain — not the whole slot
                    AbilityAction slot = npcSlots.get(selectedSlotIndex);
                    ChainedAbility chain = slot.isInlineChain() ? slot.getInlineChain() : null;
                    if (chain != null && selectedEntryIndex < chain.getEntries().size()) {
                        chain.removeEntry(selectedEntryIndex);
                    }
                } else {
                    npcSlots.remove(selectedSlotIndex);
                }
                selectedSlotIndex = -1;
                selectedEntryIndex = -1;
                selectedAbilityIndex = -1;
                if (npcAbilitiesScroll != null) npcAbilitiesScroll.resetScroll();
                updateNpcAbilitiesList();
                initGui();
                save();
            }
            return;
        }

        // Edit ability or chain
        if (id == BTN_EDIT) {
            if (selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size()) {
                AbilityAction slot = npcSlots.get(selectedSlotIndex);

                if (slot.isChain() && selectedEntryIndex == -1) {
                    // Chain header selected
                    if (slot.isChainReference()) {
                        // Reference chain — offer Clone & Modify / Edit Parent
                        setSubGui(new SubGuiAbilityEditMode());
                    } else {
                        // Inline chain — open chain config directly
                        ChainedAbility chain = slot.getInlineChain();
                        if (chain != null) {
                            pendingChain = chain;
                            pendingChainSlotIdx = selectedSlotIndex;
                            setSubGui(new SubGuiChainedAbilityConfig(chain, this, true, npcSlots));
                        }
                    }
                } else if (slot.isChain() && selectedEntryIndex >= 0) {
                    // Chain entry selected
                    ChainedAbility chain = slot.isInlineChain() ? slot.getInlineChain() : slot.getChainedAbility();
                    if (chain != null && selectedEntryIndex < chain.getEntries().size()) {
                        ChainedAbilityEntry entry = chain.getEntries().get(selectedEntryIndex);
                        // Built-in abilities are always references — no editing allowed
                        Ability resolvedEntry = entry.resolve();
                        if (resolvedEntry != null && resolvedEntry.isBuiltIn()) return;

                        if (entry.isInline() && slot.isInlineChain()) {
                            // Inline entry in inline chain — edit directly
                            Ability a = entry.getInlineAbility();
                            if (a != null) {
                                editingChainEntry = true;
                                editChainSlotIdx = selectedSlotIndex;
                                editChainEntryIdx = selectedEntryIndex;
                                a.setNpcInlineEdit(true);
                                setSubGui(a.createConfigGui(this));
                            }
                        } else {
                            // Reference entry or entry in reference chain — Clone & Modify / Edit Parent
                            setSubGui(new SubGuiAbilityEditMode());
                        }
                    }
                } else if (slot.isReference()) {
                    setSubGui(new SubGuiAbilityEditMode());
                } else {
                    Ability ability = slot.getAbility();
                    if (ability != null) {
                        ability.setNpcInlineEdit(true);
                        setSubGui(ability.createConfigGui(this));
                    }
                }
            }
            return;
        }

        // Move up (carrot left = earlier in list)
        if (id == BTN_MOVE_UP) {
            if (selectedSlotIndex > 0 && selectedEntryIndex == -1) {
                AbilityAction temp = npcSlots.get(selectedSlotIndex);
                npcSlots.set(selectedSlotIndex, npcSlots.get(selectedSlotIndex - 1));
                npcSlots.set(selectedSlotIndex - 1, temp);
                selectedSlotIndex--;
                selectedAbilityIndex = selectedSlotIndex;
                updateNpcAbilitiesList();
                initGui();
                save();
            }
            return;
        }

        // Move down (carrot right = later in list)
        if (id == BTN_MOVE_DOWN) {
            if (selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size() - 1 && selectedEntryIndex == -1) {
                AbilityAction temp = npcSlots.get(selectedSlotIndex);
                npcSlots.set(selectedSlotIndex, npcSlots.get(selectedSlotIndex + 1));
                npcSlots.set(selectedSlotIndex + 1, temp);
                selectedSlotIndex++;
                selectedAbilityIndex = selectedSlotIndex;
                updateNpcAbilitiesList();
                initGui();
                save();
            }
            return;
        }

        // Load ability or chain (opens type choice first)
        if (id == BTN_LOAD) {
            setSubGui(new SubGuiLoadTypeChoice());
            return;
        }

        // Save selected ability as custom preset (converts inline → reference)
        if (id == BTN_SAVE_PRESET) {
            Ability abilityToSave = null;
            pendingSaveSlotIdx = -1;
            pendingSaveEntryIdx = -1;

            if (selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size()) {
                AbilityAction saveSlot = npcSlots.get(selectedSlotIndex);

                if (selectedEntryIndex >= 0) {
                    // Saving a chain entry's inline ability
                    ChainedAbility chain = saveSlot.isInlineChain() ? saveSlot.getInlineChain() : null;
                    if (chain != null && selectedEntryIndex < chain.getEntries().size()) {
                        ChainedAbilityEntry entry = chain.getEntries().get(selectedEntryIndex);
                        if (entry.isInline()) {
                            abilityToSave = entry.getInlineAbility();
                            pendingSaveSlotIdx = selectedSlotIndex;
                            pendingSaveEntryIdx = selectedEntryIndex;
                        }
                    }
                } else if (!saveSlot.isChain() && !saveSlot.isReference()) {
                    // Saving a standalone inline ability
                    abilityToSave = saveSlot.getAbility();
                    pendingSaveSlotIdx = selectedSlotIndex;
                    pendingSaveEntryIdx = -1;
                }

                if (abilityToSave != null && !abilityToSave.isBuiltIn()) {
                    if (abilityToSave.getName() == null || abilityToSave.getName().isEmpty()) {
                        abilityToSave.setNpcInlineEdit(true);
                        setSubGui(abilityToSave.createConfigGui(this));
                    } else {
                        setSubGui(new SubGuiAbilitySaveConfirm(abilityToSave, null, existingPresetNames));
                    }
                }
            }
            return;
        }

        // Toggle selected slot on/off
        if (id == BTN_TOGGLE_SLOT) {
            if (selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size()) {
                AbilityAction slot = npcSlots.get(selectedSlotIndex);
                boolean enabled = ((GuiNpcButton) guibutton).getValue() == 1;
                slot.setEnabled(enabled);
                updateNpcAbilitiesList();
                initGui();
                save();
            }
            return;
        }

        // Add Chain button
        if (id == BTN_ADD_CHAIN) {
            ChainedAbility newChain = new ChainedAbility();
            newChain.setId(UUID.randomUUID().toString());
            newChain.setName("NewChain");
            pendingChain = newChain;
            pendingChainSlotIdx = -1;
            setSubGui(new SubGuiChainedAbilityConfig(newChain, this, true, npcSlots));
            return;
        }

        if (id == BTN_SCROLL_TYPE) {
            if (scrollType != ScrollType.MODDED) {
                ScrollType[] values = ScrollType.values();
                ScrollType next = values[(scrollType.ordinal() + 1) % values.length];
                // Skip MODDED if no namespaces are registered
                if (next == ScrollType.MODDED && Register.isEmpty("ability")) {
                    next = ScrollType.ALL;
                }
                scrollType = next;
            } else {
                List<String> list = Register.REGISTERED_NAMESPACES.get("ability");

                if (list != null && !list.isEmpty()) {
                    if (modIndex == list.size() - 1) {
                        scrollType = ScrollType.ALL;
                    } else {
                        modIndex = (modIndex + 1) % list.size();
                    }
                } else {
                    modIndex = 0;
                    scrollType = ScrollType.ALL;
                }
            }

            Map<String, Integer> dummyMap = new HashMap<>(allAbilityTypes);
            filteredAbilityTypes.clear();
            filteredAbilityTypes.putAll(getFilteredData(dummyMap));

            // Reset scroll position when changing category
            if (availableTypesScroll != null) {
                availableTypesScroll.resetScroll();
            }
        }

        initGui();
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.ABILITY_TYPES) {
            allAbilityTypes.clear();
            allAbilityTypes.putAll(data);

            filteredAbilityTypes.putAll(getFilteredData(allAbilityTypes));
            if (availableTypesScroll != null) {
                availableTypesScroll.setUnsortedList(getFilteredTypeList());
            }
        } else if (type == EnumScrollData.CUSTOM_ABILITIES) {
            existingPresetNames.clear();
            for (String key : data.keySet()) {
                int tabIndex = key.indexOf('\t');
                if (tabIndex > 0) {
                    existingPresetNames.add(key.substring(0, tabIndex));
                } else {
                    existingPresetNames.add(key);
                }
            }
        }
        initGui();
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        // Only process NPC ability data, not saved ability preset data
        // SubGuiAbilityLoad also implements IGuiData and may receive responses
        // that get forwarded here - ignore those by checking for expected keys
        if (!compound.hasKey("AbilityActions") && !compound.hasKey("Abilities")) {
            return;
        }

        abilitiesEnabled = compound.getBoolean("AbilitiesEnabled");

        minCooldown = compound.getInteger("AbilityMinCooldown");
        maxCooldown = compound.getInteger("AbilityMaxCooldown");
        if (minCooldown == 0 && maxCooldown == 0) {
            minCooldown = 20;
            maxCooldown = 60;
        }

        npcSlots.clear();
        // Support both new unified format and legacy format
        String tagName = compound.hasKey("AbilityActions") ? "AbilityActions" : "Abilities";
        NBTTagList actionList = compound.getTagList(tagName, 10);
        for (int i = 0; i < actionList.tagCount(); i++) {
            NBTTagCompound slotNBT = actionList.getCompoundTagAt(i);
            AbilityAction slot = AbilityAction.fromNBT(slotNBT);
            if (slot != null) {
                npcSlots.add(slot);
            }
        }

        initGui();
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if (k == 0 && availableTypesScroll != null) {
            availableTypesScroll.mouseClicked(i, j, k);
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(TF_SEARCH) != null && getTextField(TF_SEARCH).isFocused()) {
            if (search.equals(getTextField(TF_SEARCH).getText()))
                return;
            search = getTextField(TF_SEARCH).getText().toLowerCase();
            availableTypesScroll.setUnsortedList(getFilteredTypeList());
            availableTypesScroll.resetScroll();
        }
    }

    private HashMap<String, Integer> getFilteredData(Map<String, Integer> data) {
        HashMap<String, Integer> filteredData = new HashMap<>();
        if (scrollType == ScrollType.CNPC) {
            data.entrySet().stream()
                .filter(e -> e.getKey().startsWith("ability.cnpc."))
                .forEach(e -> filteredData.put(e.getKey(), e.getValue()));
        } else if (scrollType == ScrollType.MODDED) {
            if (!Register.isEmpty("ability")) {
                List<String> registerList = Register.REGISTERED_NAMESPACES.get("ability");

                if (!registerList.isEmpty()) {
                    String namespace = registerList.get(modIndex);

                    data.entrySet().stream()
                        .filter(e -> e.getKey().startsWith("ability." + namespace + "."))
                        .forEach(e -> filteredData.put(e.getKey(), e.getValue()));
                }
            }
        } else {
            filteredData.putAll(data);
        }

        return filteredData;
    }

    private List<String> getFilteredTypeList() {
        List<String> list = new ArrayList<>();
        displayNameToTypeId.clear();
        for (String typeId : filteredAbilityTypes.keySet()) {
            String displayName = I18n.format(typeId);
            // Search matches either the display name or the typeId (strip color codes for matching)
            String stripped = displayName.replaceAll("\u00A7.", "");
            if (search.isEmpty() || stripped.toLowerCase().contains(search) || typeId.toLowerCase().contains(search)) {
                // Yellow for concurrent-capable types, gray for other built-in types
                if (AbilityController.Instance.isConcurrentCapableType(typeId)) {
                    displayName = "\u00A7e" + displayName;
                } else if (AbilityController.Instance.isBuiltInType(typeId)) {
                    displayName = "\u00A77" + displayName;
                }
                list.add(displayName);
                displayNameToTypeId.put(displayName, typeId);
            }
        }
        Collections.sort(list, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(
            a.replaceAll("\u00A7.", ""), b.replaceAll("\u00A7.", "")));
        return list;
    }

    private void updateNpcAbilitiesList() {
        List<String> list = new ArrayList<>();
        rowMapping.clear();

        for (int i = 0; i < npcSlots.size(); i++) {
            AbilityAction slot = npcSlots.get(i);

            if (slot.isChain()) {
                // Chain header row
                list.add(getChainHeaderEntry(i, slot));
                rowMapping.add(new int[]{i, -1});

                // Chain entry rows
                ChainedAbility chain = slot.getChainedAbility();
                if (chain == null && slot.isInlineChain()) {
                    chain = slot.getInlineChain();
                }
                if (chain != null) {
                    for (int e = 0; e < chain.getEntries().size(); e++) {
                        ChainedAbilityEntry entry = chain.getEntries().get(e);
                        list.add(getChainEntryDisplay(entry, e));
                        rowMapping.add(new int[]{i, e});
                    }
                }
            } else {
                // Regular ability row
                list.add(getAbilityListEntry(i));
                rowMapping.add(new int[]{i, -1});
            }
        }

        if (npcAbilitiesScroll != null) {
            npcAbilitiesScroll.setUnsortedList(list);
        }
    }

    /**
     * Creates the display entry for an ability slot in the list.
     * Inline:    "[●] 1. Custom Name (Type)" or "[●] 1. Type"
     * Reference: "[●] 1. > Name (Type)" in yellow
     * Broken:    "[●] 1. > [Missing: id...]" in red
     */
    private String getAbilityListEntry(int index) {
        AbilityAction slot = npcSlots.get(index);
        Ability ability = slot.getAbility();

        if (ability == null) {
            String refId = slot.getReferenceId();
            String shortId = refId != null && refId.length() > 8 ? refId.substring(0, 8) + "..." : refId;
            return "\u00A7c\u25CF\u00A7r " + (index + 1) + ". \u00A7c> [Missing: " + shortId + "]\u00A7r";
        }

        String typeName = getDisplayName(ability.getTypeId());
        String customName = ability.getName();
        String colorPrefix = slot.isSlotEnabled() ? "\u00A7a\u25CF\u00A7r " : "\u00A7c\u25CF\u00A7r ";

        String nameDisplay;
        if (customName != null && !customName.isEmpty() && !customName.equals(typeName)) {
            nameDisplay = customName + " (" + typeName + ")";
        } else {
            nameDisplay = typeName;
        }

        if (slot.isReference()) {
            return colorPrefix + (index + 1) + ". \u00A7e> " + nameDisplay + "\u00A7r";
        }
        if (ability.isBuiltIn()) {
            return colorPrefix + (index + 1) + ". \u00A77" + nameDisplay + "\u00A7r";
        }
        return colorPrefix + (index + 1) + ". " + nameDisplay;
    }

    private String getChainHeaderEntry(int index, AbilityAction slot) {
        ChainedAbility chain = slot.isInlineChain() ? slot.getInlineChain() : slot.getChainedAbility();
        String chainName = chain != null ? chain.getDisplayName() : "???";
        String colorPrefix = slot.isSlotEnabled() ? "\u00A7a\u25CF\u00A7r " : "\u00A7c\u25CF\u00A7r ";
        if (slot.isChainReference()) {
            return colorPrefix + (index + 1) + ". \u00A7e> [Chain] " + chainName + "\u00A7r";
        }
        return colorPrefix + (index + 1) + ". \u00A7d[Chain] \u00A7r" + chainName;
    }

    private String getChainEntryDisplay(ChainedAbilityEntry entry, int entryIndex) {
        if (entry.isInline()) {
            Ability a = entry.getInlineAbility();
            String name = a != null ? a.getDisplayName() : "???";
            return "      \u00A78--- \u00A7r" + name;
        } else {
            String name = entry.getAbilityReference();
            if (name == null || name.isEmpty()) name = "???";
            Ability resolved = entry.resolve();
            if (resolved != null) {
                name = resolved.getDisplayName();
            }
            return "      \u00A78--- \u00A7e> " + name + "\u00A7r";
        }
    }

    private void selectAbilityByIndex(int index) {
        if (index >= 0 && index < npcSlots.size()) {
            String entry = getAbilityListEntry(index);
            if (npcAbilitiesScroll != null) {
                npcAbilitiesScroll.setSelected(entry);
            }
        }
    }

    /**
     * Gets the localized display name for an ability type.
     * The typeId IS the lang key (e.g., "ability.cnpc.slam").
     */
    private String getDisplayName(String typeId) {
        return I18n.format(typeId);
    }

    public void save() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("AbilitiesEnabled", abilitiesEnabled);
        compound.setInteger("AbilityMinCooldown", minCooldown);
        compound.setInteger("AbilityMaxCooldown", maxCooldown);

        NBTTagList actionList = new NBTTagList();
        for (AbilityAction slot : npcSlots) {
            actionList.appendTag(slot.writeNBT());
        }
        compound.setTag("AbilityActions", actionList);

        PacketClient.sendClient(new AbilitiesNpcSavePacket(compound));
    }

    /**
     * Called from SubGuiAbilityLoad when a cloned ability is loaded.
     * Adds as an inline slot.
     */
    public void loadAbility(Ability loadedAbility) {
        if (loadedAbility != null) {
            loadedAbility.setId(UUID.randomUUID().toString());
            npcSlots.add(AbilityAction.inline(loadedAbility));
            selectedAbilityIndex = npcSlots.size() - 1;
            if (npcAbilitiesScroll != null) npcAbilitiesScroll.resetScroll();
            updateNpcAbilitiesList();
            selectAbilityByIndex(selectedAbilityIndex);
            initGui();
            save();
        }
    }

    /**
     * Called from SubGuiAbilityLoad when a reference is loaded.
     * Adds as a reference slot.
     */
    public void loadAbilityReference(String referenceId) {
        if (referenceId != null && !referenceId.isEmpty()) {
            npcSlots.add(AbilityAction.abilityReference(referenceId));
            selectedAbilityIndex = npcSlots.size() - 1;
            if (npcAbilitiesScroll != null) npcAbilitiesScroll.resetScroll();
            updateNpcAbilitiesList();
            selectAbilityByIndex(selectedAbilityIndex);
            initGui();
            save();
        }
    }

    @Override
    public void setSelected(String selected) {
        if (availableTypesScroll != null) {
            availableTypesScroll.setSelected(selected);
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll == npcAbilitiesScroll) {
            int scrollIndex = npcAbilitiesScroll.selected;
            if (scrollIndex >= 0 && scrollIndex < rowMapping.size()) {
                int[] mapping = rowMapping.get(scrollIndex);
                selectedSlotIndex = mapping[0];
                selectedEntryIndex = mapping[1];
                selectedAbilityIndex = selectedSlotIndex;
            } else {
                selectedSlotIndex = -1;
                selectedEntryIndex = -1;
                selectedAbilityIndex = -1;
            }
            initGui();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll == npcAbilitiesScroll && selectedSlotIndex >= 0 && selectedSlotIndex < npcSlots.size()) {
            GuiNpcButton editBtn = getButton(BTN_EDIT);
            if (editBtn != null && editBtn.enabled) {
                buttonEvent(new GuiNpcButton(BTN_EDIT, 0, 0, 0, 0, ""));
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id == TF_MIN_COOLDOWN) {
            minCooldown = textField.getInteger();
            // Ensure min <= max
            if (minCooldown > maxCooldown) {
                maxCooldown = minCooldown;
                initGui();
            }
            save();
        } else if (textField.id == TF_MAX_COOLDOWN) {
            maxCooldown = textField.getInteger();
            // Ensure min <= max
            if (maxCooldown < minCooldown) {
                minCooldown = maxCooldown;
                initGui();
            }
            save();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiAbilityVariantSelect) {
            handleVariantSelectClosed((SubGuiAbilityVariantSelect) subgui);
            initGui();
            return;
        }
        if (subgui instanceof SubGuiChainedAbilityConfig) {
            handleChainConfigClosed();
            initGui();
            return;
        }
        if (subgui instanceof SubGuiAbilityEditMode) {
            handleEditModeClosed((SubGuiAbilityEditMode) subgui);
            return;
        }
        if (subgui instanceof SubGuiAbilitySaveConfirm) {
            handleSaveConfirmClosed((SubGuiAbilitySaveConfirm) subgui);
            initGui();
            return;
        }
        if (subgui instanceof SubGuiLoadTypeChoice) {
            handleLoadTypeChoiceClosed((SubGuiLoadTypeChoice) subgui);
            return;
        }
        if (subgui instanceof SubGuiChainSelect) {
            handleChainSelectClosed((SubGuiChainSelect) subgui);
            return;
        }
        if (subgui instanceof SubGuiAbilityConfig) {
            handleAbilityConfigClosed();
            return;
        }
    }

    private void handleVariantSelectClosed(SubGuiAbilityVariantSelect gui) {
        int idx = gui.getSelectedIndex();
        if (idx >= 0 && pendingTypeId != null) {
            Ability newAbility = AbilityController.Instance.create(pendingTypeId);
            if (newAbility != null) {
                gui.getVariants().get(idx).apply(newAbility);
                newAbility.setId(UUID.randomUUID().toString());
                npcSlots.add(AbilityAction.inline(newAbility));
                selectedAbilityIndex = npcSlots.size() - 1;
                selectedSlotIndex = selectedAbilityIndex;
                selectedEntryIndex = -1;
                updateNpcAbilitiesList();
                save();
            }
        }
        pendingTypeId = null;
    }

    private void handleChainConfigClosed() {
        if (pendingChain != null) {
            if (pendingChainSlotIdx < 0) {
                // New chain — add to NPC slots
                npcSlots.add(AbilityAction.inlineChain(pendingChain));
                selectedSlotIndex = npcSlots.size() - 1;
                selectedEntryIndex = -1;
                selectedAbilityIndex = selectedSlotIndex;
                if (npcAbilitiesScroll != null) npcAbilitiesScroll.resetScroll();
            }
            // Existing slot (pendingChainSlotIdx >= 0): already updated in-place, nothing to add
            pendingChain = null;
            pendingChainSlotIdx = -1;
        }
        updateNpcAbilitiesList();
        save();
    }

    private void handleEditModeClosed(SubGuiAbilityEditMode gui) {
        int mode = gui.getResult();
        if (mode < 0) return; // cancelled

        if (selectedSlotIndex < 0 || selectedSlotIndex >= npcSlots.size()) return;
        AbilityAction slot = npcSlots.get(selectedSlotIndex);

        if (slot.isChain() && selectedEntryIndex == -1) {
            // Chain header (must be CHAIN_REFERENCE)
            if (mode == SubGuiAbilityEditMode.MODE_CLONE_MODIFY) {
                if (slot.convertToInline()) {
                    ChainedAbility chain = slot.getInlineChain();
                    if (chain != null) {
                        pendingChain = chain;
                        pendingChainSlotIdx = selectedSlotIndex;
                        setSubGui(new SubGuiChainedAbilityConfig(chain, this, true, npcSlots));
                    }
                    updateNpcAbilitiesList();
                    save();
                }
            } else if (mode == SubGuiAbilityEditMode.MODE_MODIFY_PARENT) {
                ChainedAbility chain = slot.getChainedAbility();
                if (chain != null) {
                    pendingChain = chain;
                    pendingChainSlotIdx = selectedSlotIndex;
                    setSubGui(new SubGuiChainedAbilityConfig(chain, this, true, npcSlots));
                }
            }
        } else if (slot.isChain() && selectedEntryIndex >= 0) {
            // Chain entry reference
            // Block all editing for built-in abilities
            ChainedAbility resolveChain = slot.isInlineChain() ? slot.getInlineChain() : slot.getChainedAbility();
            if (resolveChain != null && selectedEntryIndex < resolveChain.getEntries().size()) {
                Ability entryResolved = resolveChain.getEntries().get(selectedEntryIndex).resolve();
                if (entryResolved != null && entryResolved.isBuiltIn()) return;
            }

            if (mode == SubGuiAbilityEditMode.MODE_CLONE_MODIFY) {
                if (slot.isChainReference()) {
                    slot.convertToInline();
                }
                ChainedAbility chain = slot.getInlineChain();
                if (chain != null && selectedEntryIndex < chain.getEntries().size()) {
                    ChainedAbilityEntry entry = chain.getEntries().get(selectedEntryIndex);
                    if (entry.convertToInline()) {
                        Ability a = entry.getInlineAbility();
                        if (a != null && !a.isBuiltIn()) {
                            editingChainEntry = true;
                            editChainSlotIdx = selectedSlotIndex;
                            editChainEntryIdx = selectedEntryIndex;
                            a.setNpcInlineEdit(true);
                            setSubGui(a.createConfigGui(this));
                        }
                    }
                }
                updateNpcAbilitiesList();
                save();
            } else if (mode == SubGuiAbilityEditMode.MODE_MODIFY_PARENT) {
                ChainedAbility chain = slot.isInlineChain() ? slot.getInlineChain() : slot.getChainedAbility();
                if (chain != null && selectedEntryIndex < chain.getEntries().size()) {
                    ChainedAbilityEntry entry = chain.getEntries().get(selectedEntryIndex);
                    Ability resolved = entry.resolve();
                    if (resolved != null && !resolved.isBuiltIn()) {
                        editingChainEntryParent = true;
                        setSubGui(resolved.createConfigGui(this));
                    }
                }
            }
        } else if (mode == SubGuiAbilityEditMode.MODE_CLONE_MODIFY) {
            // Standalone ability reference → convert to inline
            Ability preCheck = slot.getAbility();
            if (preCheck != null && preCheck.isBuiltIn()) return;
            if (slot.convertToInline()) {
                Ability ability = slot.getAbility();
                if (ability != null && !ability.isBuiltIn()) {
                    ability.setId(UUID.randomUUID().toString());
                    ability.setNpcInlineEdit(true);
                    setSubGui(ability.createConfigGui(this));
                }
                updateNpcAbilitiesList();
                save();
            }
        } else if (mode == SubGuiAbilityEditMode.MODE_MODIFY_PARENT) {
            Ability ability = slot.getAbility();
            if (ability != null && !ability.isBuiltIn()) {
                setSubGui(ability.createConfigGui(this));
            }
        }
    }

    private void handleSaveConfirmClosed(SubGuiAbilitySaveConfirm gui) {
        if (gui.wasSaved() && pendingSaveSlotIdx >= 0 && pendingSaveSlotIdx < npcSlots.size()) {
            if (pendingSaveEntryIdx >= 0) {
                // Chain entry: convert inline entry to reference
                AbilityAction slot = npcSlots.get(pendingSaveSlotIdx);
                ChainedAbility chain = slot.isInlineChain() ? slot.getInlineChain() : null;
                if (chain != null && pendingSaveEntryIdx < chain.getEntries().size()) {
                    ChainedAbilityEntry entry = chain.getEntries().get(pendingSaveEntryIdx);
                    Ability a = entry.getInlineAbility();
                    if (a != null && a.getName() != null && !a.getName().isEmpty()) {
                        chain.getEntries().set(pendingSaveEntryIdx,
                            ChainedAbilityEntry.reference(a.getName(), entry.getDelayTicks()));
                    }
                }
            } else {
                // Standalone: convert inline slot to reference
                AbilityAction slot = npcSlots.get(pendingSaveSlotIdx);
                Ability a = slot.getAbility();
                if (a != null && a.getName() != null && !a.getName().isEmpty()) {
                    npcSlots.set(pendingSaveSlotIdx, AbilityAction.abilityReference(a.getName()));
                }
            }
            if (npcAbilitiesScroll != null) npcAbilitiesScroll.resetScroll();
            updateNpcAbilitiesList();
            save();
        }
        pendingSaveSlotIdx = -1;
        pendingSaveEntryIdx = -1;
    }

    private void handleLoadTypeChoiceClosed(SubGuiLoadTypeChoice gui) {
        int result = gui.getResult();
        if (result == SubGuiLoadTypeChoice.RESULT_ABILITY) {
            setSubGui(new SubGuiAbilityLoad(this));
        } else if (result == SubGuiLoadTypeChoice.RESULT_CHAIN) {
            setSubGui(new SubGuiChainSelect());
        }
    }

    private void handleChainSelectClosed(SubGuiChainSelect gui) {
        String chainName = gui.getSelectedName();
        if (chainName != null) {
            loadChainReference(chainName);
        }
    }

    private void handleAbilityConfigClosed() {
        editingChainEntry = false;
        editChainSlotIdx = -1;
        editChainEntryIdx = -1;
        editingChainEntryParent = false;
        updateNpcAbilitiesList();
        save();
    }

    /**
     * Called from SubGuiAbilityConfig when ability is saved.
     */
    public void onAbilitySaved(Ability ability) {
        // Editing a global parent ability via "Modify Parent" on a chain entry
        if (editingChainEntryParent) {
            PacketClient.sendClient(new CustomAbilitySavePacket(ability.writeNBT()));
            return;
        }

        // Editing an ability inside a chain entry — already same object reference.
        // handleAbilityConfigClosed() will call updateNpcAbilitiesList() + save().
        if (editingChainEntry) {
            return;
        }

        if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
            AbilityAction slot = npcSlots.get(selectedAbilityIndex);
            if (slot.isReference()) {
                // Modify Parent on standalone reference — save to global preset
                PacketClient.sendClient(new CustomAbilitySavePacket(ability.writeNBT()));
            } else {
                // Inline ability — update slot data
                npcSlots.set(selectedAbilityIndex, AbilityAction.inline(ability));
            }
        }
        // updateNpcAbilitiesList() + save() deferred to handleAbilityConfigClosed()
    }

    @Override
    public void onChainedAbilitySaved(ChainedAbility chain) {
        // Handled in subGuiClosed — pendingChain tracks the chain being edited
    }

    /**
     * Called from SubGuiAbilityLoad when a chain reference is loaded.
     */
    public void loadChainReference(String referenceId) {
        if (referenceId != null && !referenceId.isEmpty()) {
            npcSlots.add(AbilityAction.chainReference(referenceId));
            selectedSlotIndex = npcSlots.size() - 1;
            selectedEntryIndex = -1;
            selectedAbilityIndex = selectedSlotIndex;
            if (npcAbilitiesScroll != null) npcAbilitiesScroll.resetScroll();
            updateNpcAbilitiesList();
            initGui();
            save();
        }
    }

    /**
     * Called from SubGuiAbilityLoad to load a chain as inline.
     */
    public void loadChainInline(ChainedAbility chain) {
        if (chain != null) {
            npcSlots.add(AbilityAction.inlineChain(chain));
            selectedSlotIndex = npcSlots.size() - 1;
            selectedEntryIndex = -1;
            selectedAbilityIndex = selectedSlotIndex;
            if (npcAbilitiesScroll != null) npcAbilitiesScroll.resetScroll();
            updateNpcAbilitiesList();
            initGui();
            save();
        }
    }
}
