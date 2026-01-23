package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.AbilitiesGetAllPacket;
import kamkeel.npcs.network.packets.request.ability.AbilitiesNpcGetPacket;
import kamkeel.npcs.network.packets.request.ability.AbilitiesNpcSavePacket;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * GUI for managing NPC abilities.
 * Shows list of assigned abilities with add/remove/edit controls.
 */
public class GuiNPCAbilities extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener, IGuiData, ITextfieldListener, ISubGuiListener, IAbilityConfigCallback {

    private GuiCustomScroll availableTypesScroll;
    private GuiCustomScroll npcAbilitiesScroll;

    // All available ability types (typeId -> index)
    private final HashMap<String, Integer> allAbilityTypes = new HashMap<>();

    // Display name to typeId mapping for the available types scroll
    private final HashMap<String, String> displayNameToTypeId = new HashMap<>();

    // NPC's current abilities
    private final List<Ability> npcAbilities = new ArrayList<>();

    // Settings
    private boolean abilitiesEnabled = false;
    private int globalCooldown = 20;

    private String search = "";
    private int selectedAbilityIndex = -1;

    public GuiNPCAbilities(EntityNPCInterface npc) {
        super(npc);
        PacketClient.sendClient(new AbilitiesGetAllPacket());
        PacketClient.sendClient(new AbilitiesNpcGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;

        // Enabled checkbox
        addLabel(new GuiNpcLabel(100, "gui.enabled", guiLeft + 5, y + 5));
        addButton(new GuiNpcButton(100, guiLeft + 55, y, 50, 20, new String[]{"gui.no", "gui.yes"}, abilitiesEnabled ? 1 : 0));

        // Global cooldown (spaced out since combat only is removed)
        addLabel(new GuiNpcLabel(101, "ability.globalCooldown", guiLeft + 140, y + 5));
        GuiNpcTextField cooldownField = new GuiNpcTextField(101, this, fontRendererObj, guiLeft + 230, y, 50, 20, String.valueOf(globalCooldown));
        cooldownField.setIntegersOnly();
        cooldownField.setMinMaxDefault(0, 10000, 20);
        addTextField(cooldownField);

        y += 28;

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
            npcAbilitiesScroll.setSize(160, 130);
        } else {
            npcAbilitiesScroll.setSize(160, 130);
        }
        npcAbilitiesScroll.guiLeft = guiLeft + 210;
        npcAbilitiesScroll.guiTop = y + 12;
        updateNpcAbilitiesList();
        addScroll(npcAbilitiesScroll);

        // Center buttons: Add/Remove
        int centerX = guiLeft + 158;
        addButton(new GuiNpcButton(70, centerX, y + 40, 40, 20, ">>>")); // Add
        addButton(new GuiNpcButton(71, centerX, y + 62, 40, 20, "<<<")); // Remove

        // Load button (under the add/remove arrows)
        addButton(new GuiNpcButton(75, centerX, y + 90, 40, 20, "gui.load"));

        // Right side buttons: Edit and Up/Down carrots with Save button
        addButton(new GuiNpcButton(72, guiLeft + 210, y + 145, 55, 20, "gui.edit"));
        getButton(72).setEnabled(selectedAbilityIndex >= 0);

        // Up/Down carrot buttons
        addButton(new GuiNpcButton(73, guiLeft + 270, y + 145, 20, 20, "<"));
        addButton(new GuiNpcButton(74, guiLeft + 292, y + 145, 20, 20, ">"));
        getButton(73).setEnabled(selectedAbilityIndex > 0);
        getButton(74).setEnabled(selectedAbilityIndex >= 0 && selectedAbilityIndex < npcAbilities.size() - 1);

        // Save button (to the right of carrots, only enabled when ability is selected)
        addButton(new GuiNpcButton(76, guiLeft + 320, y + 145, 50, 20, "gui.save"));
        getButton(76).setEnabled(selectedAbilityIndex >= 0);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Enabled toggle
        if (id == 100) {
            abilitiesEnabled = ((GuiNpcButton) guibutton).getValue() == 1;
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
                        // Don't set custom name - let it default to type name
                        npcAbilities.add(newAbility);
                        // Auto-select the newly added ability
                        selectedAbilityIndex = npcAbilities.size() - 1;
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
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcAbilities.size()) {
                npcAbilities.remove(selectedAbilityIndex);
                selectedAbilityIndex = -1;
                updateNpcAbilitiesList();
                initGui();
                save();
            }
            return;
        }

        // Edit ability
        if (id == 72) {
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcAbilities.size()) {
                Ability ability = npcAbilities.get(selectedAbilityIndex);
                setSubGui(ability.createConfigGui(this));
            }
            return;
        }

        // Move up (carrot left = earlier in list)
        if (id == 73) {
            if (selectedAbilityIndex > 0) {
                Ability temp = npcAbilities.get(selectedAbilityIndex);
                npcAbilities.set(selectedAbilityIndex, npcAbilities.get(selectedAbilityIndex - 1));
                npcAbilities.set(selectedAbilityIndex - 1, temp);
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
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcAbilities.size() - 1) {
                Ability temp = npcAbilities.get(selectedAbilityIndex);
                npcAbilities.set(selectedAbilityIndex, npcAbilities.get(selectedAbilityIndex + 1));
                npcAbilities.set(selectedAbilityIndex + 1, temp);
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

        // Save selected ability
        if (id == 76) {
            if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcAbilities.size()) {
                Ability abilityToSave = npcAbilities.get(selectedAbilityIndex);
                if (abilityToSave.getName() == null || abilityToSave.getName().isEmpty()) {
                    // Need a name to save - open edit dialog first
                    setSubGui(abilityToSave.createConfigGui(this));
                } else {
                    setSubGui(new SubGuiAbilitySaveConfirm(abilityToSave));
                }
            }
            return;
        }

        initGui();
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.ABILITY_TYPES) {
            allAbilityTypes.clear();
            allAbilityTypes.putAll(data);
            if (availableTypesScroll != null) {
                availableTypesScroll.setList(getFilteredTypeList());
            }
        }
        initGui();
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        abilitiesEnabled = compound.getBoolean("AbilitiesEnabled");
        globalCooldown = compound.hasKey("AbilityGlobalCooldown") ? compound.getInteger("AbilityGlobalCooldown") : 20;

        npcAbilities.clear();
        NBTTagList abilityList = compound.getTagList("Abilities", 10);
        for (int i = 0; i < abilityList.tagCount(); i++) {
            NBTTagCompound abilityNBT = abilityList.getCompoundTagAt(i);
            Ability ability = AbilityController.Instance.fromNBT(abilityNBT);
            if (ability != null) {
                npcAbilities.add(ability);
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

    private List<String> getFilteredTypeList() {
        List<String> list = new ArrayList<>();
        displayNameToTypeId.clear();
        for (String typeId : allAbilityTypes.keySet()) {
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
        for (int i = 0; i < npcAbilities.size(); i++) {
            list.add(getAbilityListEntry(i));
        }
        if (npcAbilitiesScroll != null) {
            npcAbilitiesScroll.setList(list);
        }
    }

    /**
     * Creates the display entry for an ability in the list.
     * Format: "[●] 1. Custom Name (Type)" or "[●] 1. Type" if no custom name
     * Circle is green (§a) for enabled, red (§c) for disabled
     */
    private String getAbilityListEntry(int index) {
        Ability ability = npcAbilities.get(index);
        String typeName = getDisplayName(ability.getTypeId());
        String customName = ability.getName();

        // Color prefix: green circle for enabled, red for disabled
        String colorPrefix = ability.isEnabled() ? "\u00A7a\u25CF\u00A7r " : "\u00A7c\u25CF\u00A7r ";

        if (customName != null && !customName.isEmpty() && !customName.equals(typeName)) {
            return colorPrefix + (index + 1) + ". " + customName + " (" + typeName + ")";
        }
        return colorPrefix + (index + 1) + ". " + typeName;
    }

    private void selectAbilityByIndex(int index) {
        if (index >= 0 && index < npcAbilities.size()) {
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
        compound.setInteger("AbilityGlobalCooldown", globalCooldown);

        NBTTagList abilityList = new NBTTagList();
        for (Ability ability : npcAbilities) {
            abilityList.appendTag(ability.writeNBT());
        }
        compound.setTag("Abilities", abilityList);

        PacketClient.sendClient(new AbilitiesNpcSavePacket(compound));
    }

    /**
     * Called from SubGuiAbilityLoad when a saved ability is loaded.
     * Adds the ability to the NPC's ability list.
     */
    public void loadAbility(Ability loadedAbility) {
        if (loadedAbility != null) {
            loadedAbility.setId(UUID.randomUUID().toString());
            npcAbilities.add(loadedAbility);
            // Auto-select the newly added ability
            selectedAbilityIndex = npcAbilities.size() - 1;
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
        if (scroll == npcAbilitiesScroll && selectedAbilityIndex >= 0 && selectedAbilityIndex < npcAbilities.size()) {
            Ability ability = npcAbilities.get(selectedAbilityIndex);
            setSubGui(ability.createConfigGui(this));
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id == 101) {
            globalCooldown = textField.getInteger();
            save();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiAbilityConfig) {
            // Ability was saved in the SubGui, refresh list
            updateNpcAbilitiesList();
            save();
        }
    }

    /**
     * Called from SubGuiAbilityConfig when ability is saved.
     */
    public void onAbilitySaved(Ability ability) {
        if (selectedAbilityIndex >= 0 && selectedAbilityIndex < npcAbilities.size()) {
            npcAbilities.set(selectedAbilityIndex, ability);
            updateNpcAbilitiesList();
            save();
        }
    }
}
