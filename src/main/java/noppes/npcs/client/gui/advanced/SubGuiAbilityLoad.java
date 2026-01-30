package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.ability.CustomAbilitiesGetPacket;
import kamkeel.npcs.network.packets.request.ability.CustomAbilityGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * Dialog for loading a saved ability preset.
 * Shows custom abilities from the server, then asks Clone/Reference via SubGuiAbilityLoadMode.
 */
public class SubGuiAbilityLoad extends SubGuiInterface implements ICustomScrollListener, IScrollData, IGuiData, ISubGuiListener {

    private final SubGuiAbilityConfig parentConfig;
    private final GuiNPCAbilities parentAbilities;
    private String selectedDisplayName = null;
    private String selectedUuid = null;

    /** Maps display name -> UUID for custom abilities. */
    private final HashMap<String, String> displayToUuid = new HashMap<>();
    private HashMap<String, Integer> rawData = new HashMap<>();
    private GuiCustomScroll scroll;

    private boolean waitingForLoad = false;
    private int pendingLoadMode = -1;

    public SubGuiAbilityLoad(SubGuiAbilityConfig parentConfig) {
        this.parentConfig = parentConfig;
        this.parentAbilities = null;

        setBackground("menubg.png");
        xSize = 220;
        ySize = 180;

        PacketClient.sendClient(new CustomAbilitiesGetPacket());
    }

    public SubGuiAbilityLoad(GuiNPCAbilities parentAbilities) {
        this.parentConfig = null;
        this.parentAbilities = parentAbilities;

        setBackground("menubg.png");
        xSize = 220;
        ySize = 180;

        PacketClient.sendClient(new CustomAbilitiesGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 5;

        addLabel(new GuiNpcLabel(0, "ability.load.select", guiLeft + 10, y));
        y += 14;

        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(200, 110);
        }
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = y;
        scroll.setList(getFilteredList());
        if (selectedDisplayName != null) {
            scroll.setSelected(selectedDisplayName);
        }
        addScroll(scroll);

        y += 115;

        addButton(new GuiNpcButton(0, guiLeft + 10, y, 95, 20, "gui.load"));
        addButton(new GuiNpcButton(2, guiLeft + 115, y, 95, 20, "gui.cancel"));

        getButton(0).setEnabled(selectedUuid != null);
    }

    private List<String> getFilteredList() {
        return new ArrayList<>(displayToUuid.keySet());
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 0 && selectedUuid != null) {
            if (parentAbilities != null) {
                // From NPC abilities GUI - ask clone/reference
                setSubGui(new SubGuiAbilityLoadMode());
            } else {
                // From config GUI - always clone
                waitingForLoad = true;
                pendingLoadMode = SubGuiAbilityLoadMode.MODE_CLONE;
                PacketClient.sendClient(new CustomAbilityGetPacket(selectedUuid));
            }
        } else if (id == 2) {
            close();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiAbilityLoadMode) {
            int mode = ((SubGuiAbilityLoadMode) subgui).getResult();
            if (mode < 0) return; // cancelled

            if (mode == SubGuiAbilityLoadMode.MODE_REFERENCE) {
                // Reference mode - just pass the reference ID
                if (parentAbilities != null && selectedUuid != null) {
                    parentAbilities.loadAbilityReference(selectedUuid);
                }
                close();
            } else {
                // Clone mode - request full data
                waitingForLoad = true;
                pendingLoadMode = SubGuiAbilityLoadMode.MODE_CLONE;
                PacketClient.sendClient(new CustomAbilityGetPacket(selectedUuid));
            }
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0) {
            selectedDisplayName = scroll.getSelected();
            if (selectedDisplayName != null) {
                selectedUuid = displayToUuid.get(selectedDisplayName);
            }
            initGui();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll.id == 0 && selection != null && !selection.isEmpty()) {
            selectedDisplayName = selection;
            selectedUuid = displayToUuid.get(selectedDisplayName);
            if (selectedUuid != null) {
                if (parentAbilities != null) {
                    setSubGui(new SubGuiAbilityLoadMode());
                } else {
                    waitingForLoad = true;
                    pendingLoadMode = SubGuiAbilityLoadMode.MODE_CLONE;
                    PacketClient.sendClient(new CustomAbilityGetPacket(selectedUuid));
                }
            }
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        if (type == EnumScrollData.CUSTOM_ABILITIES) {
            displayToUuid.clear();
            rawData = data;
            for (String key : data.keySet()) {
                // key format: "displayName\tUUID"
                int tabIndex = key.indexOf('\t');
                if (tabIndex > 0) {
                    String displayName = key.substring(0, tabIndex);
                    String uuid = key.substring(tabIndex + 1);
                    displayToUuid.put(displayName, uuid);
                } else {
                    displayToUuid.put(key, key);
                }
            }
            if (scroll != null) {
                scroll.setList(getFilteredList());
                if (selectedDisplayName != null && displayToUuid.containsKey(selectedDisplayName)) {
                    scroll.setSelected(selectedDisplayName);
                }
            }
            initGui();
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (waitingForLoad) {
            waitingForLoad = false;
            Ability loaded = AbilityController.Instance.fromNBT(compound);
            if (loaded != null) {
                if (pendingLoadMode == SubGuiAbilityLoadMode.MODE_CLONE) {
                    // Give it a new UUID so it's a unique inline copy
                    loaded.setId(UUID.randomUUID().toString());
                }

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
        this.selectedDisplayName = selected;
        if (scroll != null) {
            scroll.setSelected(selected);
        }
    }
}
