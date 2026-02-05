package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.ability.AbilitySlot;
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
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.*;

/**
 * GUI for managing NPC abilities.
 * Shows list of assigned abilities with add/remove/edit controls.
 */
public class GuiNPCAbilities extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener, IGuiData, ITextfieldListener, ISubGuiListener, IAbilityConfigCallback {

    private GuiCustomScroll availableTypesScroll;
    private GuiCustomScroll npcAbilitiesScroll;

    // All available ability types (typeId -> index)
    private final HashMap<String, Integer> allAbilityTypes = new HashMap<>();
    private final HashMap<String, Integer> filteredAbilityTypes = new HashMap<>();

    // Display name to typeId mapping for the available types scroll
    private final HashMap<String, String> displayNameToTypeId = new HashMap<>();

    // NPC's current ability slots
    private final List<AbilitySlot> npcSlots = new ArrayList<>();

    // Settings
    private boolean abilitiesEnabled = false;
    private int minCooldown = 20;
    private int maxCooldown = 60;

    private String search = "";
    private int selectedAbilityIndex = -1;

    // Existing preset names for duplicate checking
    private final Set<String> existingPresetNames = new HashSet<>();

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
                case CNPC: return "CustomNPCs";
                case MODDED:
                    if (Register.isEmpty("ability"))
                        return "modded";

                    String namespace = Register.REGISTERED_NAMESPACES.get("ability").get(modIndex);
                    String displayName = Register.NAMESPACE_DISPLAY_NAMES.get(namespace);
                    return displayName != null ? displayName : namespace;
                case ALL: return "filter.all";
                default: return name();
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;

        addButton(new GuiNpcButton(50, guiLeft + 5, y, 140, 20, scrollType.toString()));

        // Cooldown range: Min Cooldown and Max Cooldown (spaced apart more)
        addLabel(new GuiNpcLabel(101, "ability.minCooldown", guiLeft + 210, y + 5));
        GuiNpcTextField minField = new GuiNpcTextField(101, this, fontRendererObj, guiLeft + 260, y, 40, 20, "" + minCooldown);
        minField.setIntegersOnly();
        minField.setMinMaxDefault(0, 10000, 20);
        addTextField(minField);

        addLabel(new GuiNpcLabel(102, "ability.maxCooldown", guiLeft + 320, y + 5));
        GuiNpcTextField maxField = new GuiNpcTextField(102, this, fontRendererObj, guiLeft + 370, y, 40, 20, "" + maxCooldown);
        maxField.setIntegersOnly();
        maxField.setMinMaxDefault(0, 10000, 60);
        addTextField(maxField);

        y += 28;

        // Enabled checkbox
        addButton(new GuiNpcButton(100, guiLeft + 334, y + 145, 76, 20, new String[]{"gui.disabled", "gui.enabled"}, abilitiesEnabled ? 1 : 0));
        getButton(100).packedFGColour = abilitiesEnabled ? 0x00FF00 : 0xFF0000;

        // Left scroll: available ability types
        addLabel(new GuiNpcLabel(1, "ability.availableTypes", guiLeft + 5, y));
        if (availableTypesScroll == null) {
            availableTypesScroll = new GuiCustomScroll(this, 0);
            availableTypesScroll.setSize(140, 130);
        }
        availableTypesScroll.guiLeft = guiLeft + 5;
        availableTypesScroll.guiTop = y + 12;
        availableTypesScroll.setList(getFilteredTypeList());
        addScroll(availableTypesScroll);

        // Search bar for types
        addTextField(new GuiNpcTextField(4, this, fontRendererObj, guiLeft + 5, y + 145, 140, 18, search));

        // Right scroll: NPC's abilities (widened to use available space)
        addLabel(new GuiNpcLabel(2, "ability.npcAbilities", guiLeft + 210, y));
        if (npcAbilitiesScroll == null) {
            npcAbilitiesScroll = new GuiCustomScroll(this, 1);
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
        addButton(new GuiNpcButton(70, centerX, y + 15, 40, 20, ">>>")); // Add
        addButton(new GuiNpcButton(71, centerX, y + 37, 40, 20, "<<<")); // Remove
        getButton(71).setEnabled(selectedAbilityIndex >= 0);

        // Load button (under the add/remove arrows)
        addButton(new GuiNpcButton(75, centerX, y + 60, 40, 20, "gui.load"));

        // On/Off toggle for selected ability (under Load button)
        if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
            Ability selectedAbility = npcSlots.get(selectedAbilityIndex).getAbility();
            GuiNpcButton toggleBtn = new GuiNpcButton(77, centerX, y + 112, 40, 20,
                new String[]{"gui.off", "gui.on"}, selectedAbility.isEnabled() ? 1 : 0);
            addButton(toggleBtn);
        }

        // Right side buttons: Edit and Up/Down carrots with Save button
        addButton(new GuiNpcButton(72, guiLeft + 210, y + 145, 55, 20, "gui.edit"));
        getButton(72).setEnabled(selectedAbilityIndex >= 0);

        // Up/Down carrot buttons
        addButton(new GuiNpcButton(73, guiLeft + 270, y + 145, 20, 20, "<"));
        addButton(new GuiNpcButton(74, guiLeft + 292, y + 145, 20, 20, ">"));
        getButton(73).setEnabled(selectedAbilityIndex > 0);
        getButton(74).setEnabled(selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size() - 1);

        // Save button (to the right of carrots, only enabled when ability is selected)
        addButton(new GuiNpcButton(76, centerX, y + 82, 40, 20, "gui.save"));
        getButton(76).setEnabled(selectedAbilityIndex >= 0);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Enabled toggle
        if (id == 100) {
            abilitiesEnabled = ((GuiNpcButton) guibutton).getValue() == 1;
            initGui();
            save();
            return;
        }

        // Add ability
        if (id == 70) {
            if (availableTypesScroll.hasSelected()) {
                String displayName = availableTypesScroll.getSelected();
                String typeId = displayNameToTypeId.get(displayName);
                if (typeId != null) {
                    Ability newAbility = AbilityController.Instance.create(typeId);
                    if (newAbility != null) {
                        newAbility.setId(UUID.randomUUID().toString());
                        npcSlots.add(AbilitySlot.inline(newAbility));
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

        // Remove ability
        if (id == 71) {
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
                npcSlots.remove(selectedAbilityIndex);
                selectedAbilityIndex = -1;
                updateNpcAbilitiesList();
                initGui();
                save();
            }
            return;
        }

        // Edit ability
        if (id == 72) {
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
                AbilitySlot slot = npcSlots.get(selectedAbilityIndex);
                if (slot.isReference()) {
                    // Reference slot - ask clone-and-modify vs modify-parent
                    setSubGui(new SubGuiAbilityEditMode());
                } else {
                    Ability ability = slot.getAbility();
                    if (ability != null) {
                        setSubGui(ability.createConfigGui(this));
                    }
                }
            }
            return;
        }

        // Move up (carrot left = earlier in list)
        if (id == 73) {
            if (selectedAbilityIndex > 0) {
                AbilitySlot temp = npcSlots.get(selectedAbilityIndex);
                npcSlots.set(selectedAbilityIndex, npcSlots.get(selectedAbilityIndex - 1));
                npcSlots.set(selectedAbilityIndex - 1, temp);
                selectedAbilityIndex--;
                updateNpcAbilitiesList();
                selectAbilityByIndex(selectedAbilityIndex);
                initGui();
                save();
            }
            return;
        }

        // Move down (carrot right = later in list)
        if (id == 74) {
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size() - 1) {
                AbilitySlot temp = npcSlots.get(selectedAbilityIndex);
                npcSlots.set(selectedAbilityIndex, npcSlots.get(selectedAbilityIndex + 1));
                npcSlots.set(selectedAbilityIndex + 1, temp);
                selectedAbilityIndex++;
                updateNpcAbilitiesList();
                selectAbilityByIndex(selectedAbilityIndex);
                initGui();
                save();
            }
            return;
        }

        // Load ability (opens load dialog to add ability directly to NPC)
        if (id == 75) {
            setSubGui(new SubGuiAbilityLoad(this));
            return;
        }

        // Save selected ability as custom preset
        if (id == 76) {
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
                Ability abilityToSave = npcSlots.get(selectedAbilityIndex).getAbility();
                if (abilityToSave != null) {
                    if (abilityToSave.getName() == null || abilityToSave.getName().isEmpty()) {
                        setSubGui(abilityToSave.createConfigGui(this));
                    } else {
                        setSubGui(new SubGuiAbilitySaveConfirm(abilityToSave, null, existingPresetNames));
                    }
                }
            }
            return;
        }

        // Toggle selected ability on/off
        if (id == 77) {
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
                Ability ability = npcSlots.get(selectedAbilityIndex).getAbility();
                if (ability != null) {
                    ability.setEnabled(((GuiNpcButton) guibutton).getValue() == 1);
                    updateNpcAbilitiesList();
                    selectAbilityByIndex(selectedAbilityIndex);
                    initGui();
                    save();
                }
            }
            return;
        }

        if (id == 50) {
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
                availableTypesScroll.setList(getFilteredTypeList());
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
        if (!compound.hasKey("Abilities")) {
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
        NBTTagList abilityList = compound.getTagList("Abilities", 10);
        for (int i = 0; i < abilityList.tagCount(); i++) {
            NBTTagCompound abilityNBT = abilityList.getCompoundTagAt(i);
            AbilitySlot slot = AbilitySlot.fromNBT(abilityNBT);
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
        if (getTextField(4) != null && getTextField(4).isFocused()) {
            if (search.equals(getTextField(4).getText()))
                return;
            search = getTextField(4).getText().toLowerCase();
            availableTypesScroll.setList(getFilteredTypeList());
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
            // Search matches either the display name or the typeId
            if (search.isEmpty() || displayName.toLowerCase().contains(search) || typeId.toLowerCase().contains(search)) {
                list.add(displayName);
                displayNameToTypeId.put(displayName, typeId);
            }
        }
        return list;
    }

    private void updateNpcAbilitiesList() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < npcSlots.size(); i++) {
            list.add(getAbilityListEntry(i));
        }
        if (npcAbilitiesScroll != null) {
            npcAbilitiesScroll.setList(list);
        }
    }

    /**
     * Creates the display entry for an ability slot in the list.
     * Inline:    "[●] 1. Custom Name (Type)" or "[●] 1. Type"
     * Reference: "[●] 1. > Name (Type)" in yellow
     * Broken:    "[●] 1. > [Missing: id...]" in red
     */
    private String getAbilityListEntry(int index) {
        AbilitySlot slot = npcSlots.get(index);
        Ability ability = slot.getAbility();

        if (ability == null) {
            // Broken reference
            String refId = slot.getReferenceId();
            String shortId = refId != null && refId.length() > 8 ? refId.substring(0, 8) + "..." : refId;
            return "\u00A7c\u25CF\u00A7r " + (index + 1) + ". \u00A7c> [Missing: " + shortId + "]\u00A7r";
        }

        String typeName = getDisplayName(ability.getTypeId());
        String customName = ability.getName();
        String colorPrefix = ability.isEnabled() ? "\u00A7a\u25CF\u00A7r " : "\u00A7c\u25CF\u00A7r ";

        String nameDisplay;
        if (customName != null && !customName.isEmpty() && !customName.equals(typeName)) {
            nameDisplay = customName + " (" + typeName + ")";
        } else {
            nameDisplay = typeName;
        }

        if (slot.isReference()) {
            return colorPrefix + (index + 1) + ". \u00A7e> " + nameDisplay + "\u00A7r";
        }
        return colorPrefix + (index + 1) + ". " + nameDisplay;
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

        NBTTagList abilityList = new NBTTagList();
        for (AbilitySlot slot : npcSlots) {
            abilityList.appendTag(slot.writeNBT());
        }
        compound.setTag("Abilities", abilityList);

        PacketClient.sendClient(new AbilitiesNpcSavePacket(compound));
    }

    /**
     * Called from SubGuiAbilityLoad when a cloned ability is loaded.
     * Adds as an inline slot.
     */
    public void loadAbility(Ability loadedAbility) {
        if (loadedAbility != null) {
            loadedAbility.setId(UUID.randomUUID().toString());
            npcSlots.add(AbilitySlot.inline(loadedAbility));
            selectedAbilityIndex = npcSlots.size() - 1;
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
            npcSlots.add(AbilitySlot.reference(referenceId));
            selectedAbilityIndex = npcSlots.size() - 1;
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
            String selected = npcAbilitiesScroll.getSelected();
            if (selected != null) {
                // Parse index from "[●] 1. Name" format - strip color codes first
                String stripped = selected.replaceAll("\u00A7.", "").trim();
                // Now format is "● 1. Name" - find the number after the circle
                int dotIndex = stripped.indexOf(".");
                if (dotIndex > 0) {
                    try {
                        // Extract just the number portion (skip "● " prefix)
                        String numPart = stripped.substring(0, dotIndex).replaceAll("[^0-9]", "");
                        selectedAbilityIndex = Integer.parseInt(numPart) - 1;
                    } catch (NumberFormatException e) {
                        selectedAbilityIndex = -1;
                    }
                }
            }
            initGui();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll == npcAbilitiesScroll && selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
            AbilitySlot slot = npcSlots.get(selectedAbilityIndex);
            if (slot.isReference()) {
                setSubGui(new SubGuiAbilityEditMode());
            } else {
                Ability ability = slot.getAbility();
                if (ability != null) {
                    setSubGui(ability.createConfigGui(this));
                }
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id == 101) {
            minCooldown = textField.getInteger();
            // Ensure min <= max
            if (minCooldown > maxCooldown) {
                maxCooldown = minCooldown;
                initGui();
            }
            save();
        } else if (textField.id == 102) {
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
        if (subgui instanceof SubGuiAbilityEditMode) {
            int mode = ((SubGuiAbilityEditMode) subgui).getResult();
            if (mode < 0) return; // cancelled

            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
                AbilitySlot slot = npcSlots.get(selectedAbilityIndex);
                if (mode == SubGuiAbilityEditMode.MODE_CLONE_MODIFY) {
                    // Convert reference to inline, then edit
                    if (slot.convertToInline()) {
                        Ability ability = slot.getAbility();
                        if (ability != null) {
                            ability.setId(UUID.randomUUID().toString());
                            setSubGui(ability.createConfigGui(this));
                        }
                        updateNpcAbilitiesList();
                        save();
                    }
                } else if (mode == SubGuiAbilityEditMode.MODE_MODIFY_PARENT) {
                    // Edit the resolved ability directly (changes propagate to controller)
                    Ability ability = slot.getAbility();
                    if (ability != null) {
                        setSubGui(ability.createConfigGui(this));
                    }
                }
            }
        } else if (subgui instanceof SubGuiAbilityConfig) {
            updateNpcAbilitiesList();
            save();
        }
    }

    /**
     * Called from SubGuiAbilityConfig when ability is saved.
     */
    public void onAbilitySaved(Ability ability) {
        if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcSlots.size()) {
            AbilitySlot slot = npcSlots.get(selectedAbilityIndex);
            if (slot.isReference()) {
                // For reference slots, save back to controller
                PacketClient.sendClient(new CustomAbilitySavePacket(ability.writeNBT()));
            } else {
                // For inline slots, replace with the updated ability
                npcSlots.set(selectedAbilityIndex, AbilitySlot.inline(ability));
            }
            updateNpcAbilitiesList();
            save();
        }
    }
}
