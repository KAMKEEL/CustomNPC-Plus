package noppes.npcs.client.gui.player.inventory;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.AbilityHotbarData;
import noppes.npcs.controllers.data.PlayerData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SubGui for selecting an ability to assign to a hotbar slot.
 * Uses ability keys internally to avoid duplicate display name collisions.
 */
public class SubGuiSelectAbilityForSlot extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {
    private LinkedHashMap<String, String> keyToDisplay = new LinkedHashMap<>();
    private List<String> scrollKeys = new ArrayList<>();
    private GuiCustomScroll scrollAbilities;
    private int selectedIndex = -1;
    private String search = "";

    public boolean confirmed = false;
    public String selectedAbilityKey = null;
    public int slotIndex;
    public boolean removeAbility = false;

    public SubGuiSelectAbilityForSlot(int slotIndex) {
        this.slotIndex = slotIndex;
        this.closeOnEsc = true;
        this.drawDefaultBackground = true;
        xSize = 256 + 10;
        this.setBackground("menubg.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        guiTop = (this.height - ySize) / 2 + 10;

        if (scrollAbilities == null) {
            scrollAbilities = new GuiCustomScroll(this, 0, 0);
            scrollAbilities.setSize(177, 185);
        }

        scrollAbilities.guiLeft = guiLeft + 4;
        scrollAbilities.guiTop = guiTop + 4;
        addScroll(scrollAbilities);

        loadAbilities();
        scrollAbilities.setList(getSearchList());

        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 4, guiTop + 192, 177, 20, search));

        addButton(new GuiNpcButton(0, guiLeft + 183, guiTop + 4, 79, 20, "gui.add"));
        addButton(new GuiNpcButton(1, guiLeft + 183, guiTop + 26, 79, 20, "gui.cancel"));
        addButton(new GuiNpcButton(2, guiLeft + 183, guiTop + 88, 79, 20, "gui.remove"));
    }

    private void loadAbilities() {
        keyToDisplay.clear();
        PlayerData playerData = ClientCacheHandler.playerData;
        if (playerData == null || playerData.abilityData == null) {
            return;
        }

        List<String> abilities = playerData.abilityData.getUnlockedAbilityList();
        for (String key : abilities) {
            String displayName;
            if (key.startsWith(AbilityHotbarData.CHAIN_PREFIX)) {
                String chainKey = key.substring(AbilityHotbarData.CHAIN_PREFIX.length());
                ChainedAbility chain = AbilityController.Instance != null ?
                    AbilityController.Instance.resolveChainedAbility(chainKey) : null;
                displayName = chain != null ? "\u00A76\u2726 " + chain.getDisplayName() : key;
            } else {
                Ability ability = AbilityController.Instance != null ?
                    AbilityController.Instance.resolveAbility(key) : null;
                displayName = ability != null ? ability.getDisplayName() : key;
            }
            keyToDisplay.put(key, displayName);
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        int id = button.id;

        if (id == 0 && selectedIndex >= 0 && selectedIndex < scrollKeys.size()) {
            confirmed = true;
            selectedAbilityKey = scrollKeys.get(selectedIndex);
            this.close();
        }
        if (id == 1) {
            this.close();
        }
        if (id == 2) {
            this.removeAbility = true;
            this.close();
        }
    }

    @Override
    public void customScrollClicked(int i, int i1, int i2, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll == scrollAbilities) {
            selectedIndex = scrollAbilities.selected;
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll == scrollAbilities && selectedIndex >= 0 && selectedIndex < scrollKeys.size()) {
            confirmed = true;
            selectedAbilityKey = scrollKeys.get(selectedIndex);
            this.close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scrollAbilities.resetScroll();
                selectedIndex = -1;
                scrollAbilities.setList(getSearchList());
            }
        }
    }

    private List<String> getSearchList() {
        scrollKeys.clear();
        List<String> displayNames = new ArrayList<>();

        for (Map.Entry<String, String> entry : keyToDisplay.entrySet()) {
            String displayName = entry.getValue();
            if (search.isEmpty() || displayName.toLowerCase().contains(search)) {
                scrollKeys.add(entry.getKey());
                displayNames.add(displayName);
            }
        }
        return displayNames;
    }
}
