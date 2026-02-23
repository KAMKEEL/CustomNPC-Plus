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
import java.util.HashMap;
import java.util.List;

/**
 * SubGui for selecting an ability to assign to a hotbar slot.
 */
public class SubGuiSelectAbilityForSlot extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {
    private HashMap<String, String> displayToKey = new HashMap<>();
    private GuiCustomScroll scrollAbilities;
    private String selected = null;
    private String search = "";

    public boolean confirmed = false;
    public String selectedAbilityKey = null;
    public int slotIndex;
    public boolean removeAbility = false;

    public SubGuiSelectAbilityForSlot(int slotIndex) {
        this.slotIndex = slotIndex;
        this.closeOnEsc = true;
        this.drawDefaultBackground = true;
        guiLeft -= 10;
        xSize = 256 + 10;
        this.setBackground("menubg.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        guiTop += 10;

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
        displayToKey.clear();
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
            displayToKey.put(displayName, key);
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        int id = button.id;

        if (id == 0 && selected != null) {
            confirmed = true;
            selectedAbilityKey = displayToKey.get(selected);
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
        if (guiCustomScroll == scrollAbilities)
            selected = scrollAbilities.getSelected();
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll == scrollAbilities && selection != null) {
            confirmed = true;
            selectedAbilityKey = displayToKey.get(selection);
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
                scrollAbilities.setList(getSearchList());
            }
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<>(displayToKey.keySet());
        }

        List<String> list = new ArrayList<>();
        for (String name : displayToKey.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }
}
