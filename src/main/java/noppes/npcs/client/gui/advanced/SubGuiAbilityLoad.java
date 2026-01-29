package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.SavedAbilitiesGetPacket;
import kamkeel.npcs.network.packets.request.ability.SavedAbilityGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Dialog for loading a saved ability preset.
 * Uses packet-based communication for proper client-server architecture.
 */
public class SubGuiAbilityLoad extends SubGuiInterface implements ICustomScrollListener, IScrollData, IGuiData {

    private final SubGuiAbilityConfig parentConfig;
    private final GuiNPCAbilities parentAbilities;
    private String selectedName = null;
    private HashMap<String, Integer> abilityData = new HashMap<>();
    private GuiCustomScroll scroll;

    // Track if we're waiting for a load
    private boolean waitingForLoad = false;

    public SubGuiAbilityLoad(SubGuiAbilityConfig parentConfig) {
        this.parentConfig = parentConfig;
        this.parentAbilities = null;

        setBackground("menubg.png");
        xSize = 220;
        ySize = 180;

        // Request ability list from server
        PacketClient.sendClient(new SavedAbilitiesGetPacket());
    }

    public SubGuiAbilityLoad(GuiNPCAbilities parentAbilities) {
        this.parentConfig = null;
        this.parentAbilities = parentAbilities;

        setBackground("menubg.png");
        xSize = 220;
        ySize = 180;

        // Request ability list from server
        PacketClient.sendClient(new SavedAbilitiesGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 5;

        addLabel(new GuiNpcLabel(0, "ability.load.select", guiLeft + 10, y));
        y += 14;

        // Scroll list of saved abilities
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(200, 110);
        }
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = y;
        scroll.setList(getFilteredList());
        if (selectedName != null && abilityData.containsKey(selectedName)) {
            scroll.setSelected(selectedName);
        }
        addScroll(scroll);

        y += 115;

        // Buttons
        addButton(new GuiNpcButton(0, guiLeft + 10, y, 95, 20, "gui.load"));
        addButton(new GuiNpcButton(2, guiLeft + 115, y, 95, 20, "gui.cancel"));

        // Disable load if nothing selected
        getButton(0).setEnabled(selectedName != null && !selectedName.isEmpty());
    }

    private List<String> getFilteredList() {
        return new ArrayList<>(abilityData.keySet());
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 0 && selectedName != null) {
            // Request ability data from server
            waitingForLoad = true;
            PacketClient.sendClient(new SavedAbilityGetPacket(selectedName));
        } else if (id == 2) {
            close();
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0) {
            selectedName = scroll.getSelected();
            initGui();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll.id == 0 && selection != null && !selection.isEmpty()) {
            // Double-click to load - request from server
            selectedName = selection;
            waitingForLoad = true;
            PacketClient.sendClient(new SavedAbilityGetPacket(selection));
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.ABILITIES) {
            String name = scroll != null ? scroll.getSelected() : null;
            this.abilityData = data;
            if (scroll != null) {
                scroll.setList(getFilteredList());
                if (name != null && abilityData.containsKey(name)) {
                    scroll.setSelected(name);
                }
            }
            initGui();
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        // Received ability data from server
        if (waitingForLoad) {
            waitingForLoad = false;
            Ability loaded = AbilityController.Instance.fromNBT(compound);
            if (loaded != null) {
                if (parentConfig != null) {
                    parentConfig.loadAbility(loaded);
                } else if (parentAbilities != null) {
                    parentAbilities.loadAbility(loaded);
                }
            }
            close();
        }
    }

    @Override
    public void setSelected(String selected) {
        this.selectedName = selected;
        if (scroll != null) {
            scroll.setSelected(selected);
        }
    }
}
