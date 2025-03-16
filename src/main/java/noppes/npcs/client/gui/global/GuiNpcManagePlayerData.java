package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.playerdata.PlayerDataGetNamesPacket;
import kamkeel.npcs.network.packets.request.playerdata.PlayerDataMapRegenPacket;
import kamkeel.npcs.network.packets.request.playerdata.PlayerDataRemovePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.SubGuiPlayerData;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNpcManagePlayerData extends GuiNPCInterface2 implements GuiYesNoCallback, IScrollData, ICustomScrollListener, ISubGuiListener {

    private GuiCustomScroll playerScroll;
    private String selectedPlayer = null;
    private HashMap<String, Integer> playerData = new HashMap<>();
    private String search = "";

    public GuiNpcManagePlayerData(EntityNPCInterface npcInterface) {
        super(npcInterface);
        // Request the complete list of players.
        PacketClient.sendClient(new PlayerDataGetNamesPacket(EnumPlayerData.Players, ""));
    }

    @Override
    public void initGui() {
        super.initGui();
        if (playerScroll == null) {
            playerScroll = new GuiCustomScroll(this, 0, 0);
        }
        playerScroll.guiLeft = guiLeft + 4;
        playerScroll.guiTop = guiTop + 16;
        playerScroll.setSize(303, 175);
        addScroll(playerScroll);

        addLabel(new GuiNpcLabel(0, StatCollector.translateToLocal("All Players"), guiLeft + 10, guiTop + 6));

        addButton(new GuiNpcButton(0, guiLeft + 313, guiTop + 10, 98, 20, "gui.remove"));
        addButton(new GuiNpcButton(1, guiLeft + 313, guiTop + 32, 98, 20, "gui.view"));
        addButton(new GuiNpcButton(2, guiLeft + 313, guiTop + 54, 98, 20, "gui.playerMap"));

        addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 4, guiTop + 193, 303, 20, search));

        playerScroll.setList(getSearchList());
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
        playerScroll.drawScreen(i, j, f);
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        ;
        super.mouseClicked(i, j, k);
        if (k == 0 && playerScroll != null)
            playerScroll.mouseClicked(i, j, k);
    }

    @Override
    public void save() {
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        String newText = getTextField(0).getText();
        if (!search.equals(newText)) {
            search = newText.toLowerCase();
            playerScroll.setList(getSearchList());
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty())
            return new ArrayList<>(playerData.keySet());
        List<String> list = new ArrayList<>();
        for (String name : playerData.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        int id = button.id;
        if (id == 0) {
            if (selectedPlayer != null) {
                displayGuiScreen(new GuiYesNo(this, selectedPlayer, StatCollector.translateToLocal("gui.deleteconfirm"), 0));
            }
        }
        if (id == 1) {
            if (selectedPlayer != null) {
                setSubGui(new SubGuiPlayerData(selectedPlayer));
            }
        }
        if (id == 2) {
            displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("gui.warning"), StatCollector.translateToLocal("gui.regenconfirm"), 1));
        }
    }

    @Override
    public void confirmClicked(boolean confirm, int id) {
        if (confirm) {
            if (id == 0) {
                PacketClient.sendClient(new PlayerDataRemovePacket(EnumPlayerData.Players, selectedPlayer, null));
                playerData.remove(selectedPlayer);
                selectedPlayer = null;
                playerScroll.setList(getSearchList());
            }
            if (id == 1) {
                PacketClient.sendClient(new PlayerDataMapRegenPacket());
                close();
            }
        } else {
            displayGuiScreen(this);
        }
    }

    // IScrollData interface.
    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        playerData.clear();
        playerData.putAll(data);
        playerScroll.setList(getSearchList());
    }

    // ICustomScrollListener interface.
    @Override
    public void setSelected(String selected) {
        selectedPlayer = selected;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        selectedPlayer = scroll.getSelected();
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }


    public void subGuiClosed(SubGuiInterface subgui) {
    }
}
