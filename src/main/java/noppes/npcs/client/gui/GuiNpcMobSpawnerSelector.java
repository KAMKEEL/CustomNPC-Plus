package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.clone.CloneListPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.CloneFolder;

import java.util.ArrayList;
import java.util.List;

public class GuiNpcMobSpawnerSelector extends SubGuiInterface implements IGuiData, ICustomScrollListener {

    private GuiCustomScroll scroll;
    private GuiCustomScroll navScroll;
    private List<String> list;
    private List<String> navList = new ArrayList<String>();

    private static String search = "";
    public int activeTab = 1;
    public String activeFolder = null;
    public boolean isServer = false;

    public GuiNpcMobSpawnerSelector() {
        super();
        xSize = 256;
        this.closeOnEsc = true;
        setBackground("menubg.png");
    }


    public void initGui() {
        super.initGui();
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(165, 188);
        } else
            scroll.clear();
        scroll.guiLeft = guiLeft + 4;
        scroll.guiTop = guiTop + 26;
        addScroll(scroll);

        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 4, guiTop + 4, 165, 20, search));

        addButton(new GuiNpcButton(0, guiLeft + 171, guiTop + 80, 80, 20, "gui.done"));
        addButton(new GuiNpcButton(1, guiLeft + 171, guiTop + 103, 80, 20, "gui.cancel"));

        // Navigation scroll (tabs + folders) replacing hardcoded side buttons
        buildNavList();
        if (navScroll == null) {
            navScroll = new GuiCustomScroll(this, 1);
        } else {
            navScroll.clear();
        }
        navScroll.setSize(90, 210);
        navScroll.guiLeft = guiLeft - 94;
        navScroll.guiTop = guiTop + 2;
        navScroll.setList(navList);
        navScroll.setSelected(getNavSelection());
        addScroll(navScroll);

        showClones();
    }

    private void buildNavList() {
        navList.clear();
        for (int i = 1; i <= 15; i++) {
            navList.add("Tab " + i);
        }
        if (ClientCloneController.Instance != null) {
            for (CloneFolder folder : ClientCloneController.Instance.getFolderList()) {
                navList.add(folder.name);
            }
        }
    }

    private String getNavSelection() {
        if (activeFolder != null) return activeFolder;
        if (activeTab >= 1 && activeTab <= 15) return "Tab " + activeTab;
        return "Tab 1";
    }

    public String getSelected() {
        return scroll.getSelected();
    }

    private void showClones() {
        if (isServer) {
            if (activeFolder != null) {
                PacketClient.sendClient(new CloneListPacket(activeFolder));
            } else {
                PacketClient.sendClient(new CloneListPacket(activeTab));
            }
            return;
        }

        if (activeFolder != null && ClientCloneController.Instance != null) {
            this.list = new ArrayList<String>(ClientCloneController.Instance.getClones(activeFolder));
        } else if (ClientCloneController.Instance != null) {
            this.list = new ArrayList<String>(ClientCloneController.Instance.getClones(activeTab));
        } else {
            this.list = new ArrayList<String>();
        }
        scroll.setList(getSearchList());
    }

    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);

        if (search.equals(getTextField(1).getText()))
            return;
        search = getTextField(1).getText().toLowerCase();
        scroll.setList(getSearchList());
    }

    private List<String> getSearchList() {
        if (list == null) list = new ArrayList<String>();
        if (search.isEmpty())
            return new ArrayList<String>(list);
        List<String> list = new ArrayList<String>();
        for (String name : this.list) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    public NBTTagCompound getCompound() {
        String sel = scroll.getSelected();
        if (sel == null)
            return null;

        NBTTagCompound compound;
        if (activeFolder != null && ClientCloneController.Instance != null) {
            compound = ClientCloneController.Instance.getCloneData(player, sel, activeFolder);
        } else {
            compound = ClientCloneController.Instance.getCloneData(player, sel, activeTab);
        }
        if (compound != null) {
            compound.setString("ClonedName", sel);
        }
        return compound;
    }

    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 0) {
            close();
        }
        if (id == 1) {
            scroll.clear();
            close();
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 1) {
            String selected = navScroll.getSelected();
            if (selected == null) return;

            if (selected.startsWith("Tab ")) {
                try {
                    int tabNum = Integer.parseInt(selected.substring(4));
                    if (tabNum >= 1 && tabNum <= 15) {
                        activeTab = tabNum;
                        activeFolder = null;
                    }
                } catch (NumberFormatException ignored) {
                }
            } else {
                activeFolder = selected;
                activeTab = -1;
            }
            showClones();
        }
    }

    protected NBTTagList newDoubleNBTList(double... par1ArrayOfDouble) {
        NBTTagList nbttaglist = new NBTTagList();
        double[] adouble = par1ArrayOfDouble;
        int i = par1ArrayOfDouble.length;

        for (int j = 0; j < i; ++j) {
            double d1 = adouble[j];
            nbttaglist.appendTag(new NBTTagDouble(d1));
        }

        return nbttaglist;
    }

    @Override
    public void save() {


    }


    @Override
    public void setGuiData(NBTTagCompound compound) {
        NBTTagList nbtlist = compound.getTagList("List", 8);
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < nbtlist.tagCount(); i++) {
            list.add(nbtlist.getStringTagAt(i));
        }
        this.list = list;
        scroll.setList(getSearchList());
    }

}
