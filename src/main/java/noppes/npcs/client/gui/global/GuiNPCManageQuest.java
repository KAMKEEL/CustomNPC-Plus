package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.quest.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.SubGuiNpcQuest;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageQuest extends GuiNPCInterface2 implements IScrollGroup, IScrollData, ISubGuiListener, ICustomScrollListener, IGuiData, GuiYesNoCallback {

    private GuiCustomScroll catScroll;
    public GuiCustomScroll questScroll;

    private String prevCatName = "";
    private String prevQuestName = "";

    public QuestCategory category = new QuestCategory();
    public static Quest quest = new Quest();
    public String nextQuestName = "";

    private HashMap<String, Integer> catData = new HashMap<String, Integer>();
    public HashMap<String, Integer> questData = new HashMap<String, Integer>();

    private String catSearch = "";
    private String questSearch = "";

    public static GuiScreen Instance;

    // Divider variables
    private boolean isResizing = false;
    private int initialDragX = 0;
    private int dividerOffset = 143;
    private final int dividerWidth = 5;
    private final int minScrollWidth = 50;
    private int dividerLineHeight = 20;
    private int dividerLineYOffset = 0;

    public GuiNPCManageQuest(EntityNPCInterface npc) {
        super(npc);
        Instance = this;
        quest = new Quest();
        PacketClient.sendClient(new QuestCategoriesGetPacket());
    }

    public void initGui() {
        super.initGui();

        // Define overall horizontal region.
        int regionLeft = guiLeft + 64;
        int regionRight = guiLeft + 355;
        int dividerX = regionLeft + dividerOffset;

        // Left scroll (catScroll)
        if (catScroll == null) {
            catScroll = new GuiCustomScroll(this, 0, 0);
        }
        catScroll.guiLeft = regionLeft;
        catScroll.guiTop = guiTop + 4;
        catScroll.setSize(dividerX - regionLeft, 185);
        this.addScroll(catScroll);

        // Right scroll (questScroll)
        if (questScroll == null) {
            questScroll = new GuiCustomScroll(this, 1, 0);
        }
        questScroll.guiLeft = dividerX + dividerWidth;
        questScroll.guiTop = guiTop + 4;
        questScroll.setSize(regionRight - (dividerX + dividerWidth), 185);
        this.addScroll(questScroll);

        // Adjust text fields:
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, regionLeft, guiTop + 4 + 3 + 185, dividerX - regionLeft, 20, catSearch));
        addTextField(new GuiNpcTextField(66, this, fontRendererObj, dividerX + dividerWidth, guiTop + 4 + 3 + 185, regionRight - (dividerX + dividerWidth), 20, questSearch));

        this.addButton(new GuiNpcButton(44, guiLeft + 3, guiTop + 8, 58, 20, "gui.categories"));
        getButton(44).setEnabled(false);
        this.addButton(new GuiNpcButton(4, guiLeft + 3, guiTop + 38, 58, 20, "gui.add"));
        this.addButton(new GuiNpcButton(5, guiLeft + 3, guiTop + 61, 58, 20, "gui.remove"));
        this.addButton(new GuiNpcButton(6, guiLeft + 3, guiTop + 94, 58, 20, "gui.edit"));

        this.addButton(new GuiNpcButton(33, guiLeft + 358, guiTop + 8, 58, 20, "quest.quests"));
        getButton(33).setEnabled(false);
        this.addButton(new GuiNpcButton(0, guiLeft + 358, guiTop + 94, 58, 20, "gui.edit"));
        this.addButton(new GuiNpcButton(1, guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
        this.addButton(new GuiNpcButton(2, guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));
        this.addButton(new GuiNpcButton(3, guiLeft + 358, guiTop + 117, 58, 20, "gui.copy"));

        if (quest != null) {
            if (quest.id != -1) {
                addLabel(new GuiNpcLabel(0, "ID", guiLeft + 358, guiTop + 4 + 3 + 185));
                addLabel(new GuiNpcLabel(1, quest.id + "", guiLeft + 358, guiTop + 4 + 3 + 195));
            }
        }

        updateButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!hasSubGui()) {
            int regionLeft = guiLeft + 64;
            int dividerX = regionLeft + dividerOffset;
            int regionTop = guiTop + 4;
            int regionHeight = 185;
            int handleTop = regionTop + (regionHeight - dividerLineHeight) / 2 + dividerLineYOffset;
            drawRect(dividerX + 1, handleTop, dividerX + dividerWidth - 1, handleTop + dividerLineHeight, 0xFF707070);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!hasSubGui()) {
            int regionLeft = guiLeft + 64;
            int dividerX = regionLeft + dividerOffset;
            int regionTop = guiTop + 4;
            int regionHeight = 185;
            int handleTop = regionTop + (regionHeight - dividerLineHeight) / 2 + dividerLineYOffset;
            int handleBottom = handleTop + dividerLineHeight;
            if (mouseX >= dividerX && mouseX <= dividerX + dividerWidth &&
                mouseY >= handleTop && mouseY <= handleBottom) {
                isResizing = true;
                resizingActive = true;
                initialDragX = mouseX;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isResizing) {
            int dx = mouseX - initialDragX;
            initialDragX = mouseX;
            dividerOffset += dx;
            int regionLeft = guiLeft + 64;
            int regionRight = guiLeft + 355;
            int minOffset = minScrollWidth;
            int maxOffset = (regionRight - regionLeft) - dividerWidth - minScrollWidth;
            if (dividerOffset < minOffset) {
                dividerOffset = minOffset;
            }
            if (dividerOffset > maxOffset) {
                dividerOffset = maxOffset;
            }
            int dividerX = regionLeft + dividerOffset;
            catScroll.setSize(dividerX - regionLeft, 185);
            questScroll.guiLeft = dividerX + dividerWidth;
            questScroll.setSize(regionRight - (dividerX + dividerWidth), 185);
            if (getTextField(55) != null) {
                getTextField(55).width = dividerX - regionLeft;
            }
            if (getTextField(66) != null) {
                getTextField(66).width = regionRight - (dividerX + dividerWidth);
                getTextField(66).xPosition = dividerX + dividerWidth;
            }
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (isResizing) {
            isResizing = false;
            resizingActive = false;
            return;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (catSearch.equals(getTextField(55).getText()))
                    return;
                catSearch = getTextField(55).getText().toLowerCase();
                catScroll.resetScroll();
                catScroll.setList(getCatSearch());
            }
        }
        if (getTextField(66) != null) {
            if (getTextField(66).isFocused()) {
                if (questSearch.equals(getTextField(66).getText()))
                    return;
                questSearch = getTextField(66).getText().toLowerCase();
                questScroll.resetScroll();
                questScroll.setList(getQuestSearch());
            }
        }
    }

    public void resetQuestList() {
        if (questScroll != null) {
            questSearch = "";
            if (getTextField(66) != null) {
                getTextField(66).setText("");
            }
            questScroll.setList(getQuestSearch());
        }
    }

    private List<String> getCatSearch() {
        if (catSearch.isEmpty()) {
            return new ArrayList<String>(this.catData.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : this.catData.keySet()) {
            if (name.toLowerCase().contains(catSearch))
                list.add(name);
        }
        return list;
    }

    private List<String> getQuestSearch() {
        if (category != null) {
            if (category.id < 0) {
                return new ArrayList<String>();
            }
        } else {
            return new ArrayList<String>();
        }
        if (questSearch.isEmpty()) {
            return new ArrayList<String>(this.questData.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : this.questData.keySet()) {
            if (name.toLowerCase().contains(questSearch))
                list.add(name);
        }
        return list;
    }

    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        // Edit Category
        if (id == 6) {
            if (category != null && category.id > -1) {
                setSubGui(new SubGuiEditText(category.title));
            } else {
                getCategory(false);
            }
        }
        // Add Category
        if (id == 4) {
            String name = "New";
            while (catData.containsKey(name))
                name += "_";
            if (catScroll != null) {
                setPrevCatName(name);
            }
            QuestCategory category = new QuestCategory();
            category.title = name;
            PacketClient.sendClient(new QuestCategorySavePacket(category.writeNBT(new NBTTagCompound())));
        }
        // Remove Category
        if (id == 5) {
            if (catData.containsKey(catScroll.getSelected())) {
                GuiYesNo guiyesno = new GuiYesNo(this, catScroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 5);
                displayGuiScreen(guiyesno);
            }
        }
        if (category != null && category.id >= 0) {
            // Add Quest
            if (id == 1) {
                String name = "New";
                while (questData.containsKey(name))
                    name += "_";
                if (questScroll != null) {
                    setPrevQuestName(name);
                }
                Quest quest = new Quest();
                quest.title = name;
                PacketClient.sendClient(new QuestSavePacket(category.id, quest.writeToNBT(new NBTTagCompound()), true));
            }
            // Remove Quest
            if (id == 2) {
                if (questData.containsKey(questScroll.getSelected())) {
                    GuiYesNo guiyesno = new GuiYesNo(this, questScroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 2);
                    displayGuiScreen(guiyesno);
                }
            }
            // Edit Quest
            if (id == 0) {
                if (questData.containsKey(questScroll.getSelected()) && quest != null && quest.id >= 0) {
                    setSubGui(new SubGuiNpcQuest(this, quest, category.id));
                }
            }
            // Clone Quest
            if (id == 3) {
                if (questData.containsKey(questScroll.getSelected()) && quest != null && quest.id >= 0) {
                    String name = quest.title;
                    while (questData.containsKey(name))
                        name += "_";
                    if (questScroll != null) {
                        setPrevQuestName(name);
                    }
                    Quest quest = new Quest();
                    quest.readNBTPartial(this.quest.writeToNBT(new NBTTagCompound()));
                    quest.title = name;
                    PacketClient.sendClient(new QuestSavePacket(category.id, quest.writeToNBT(new NBTTagCompound()), true));
                }
            }
        }
        updateButtons();
    }

    public void updateButtons() {
        boolean enabled = category != null;
        if (enabled) {
            if (!(category.id >= 0)) {
                enabled = false;
            }
        }
        boolean questEnabled = questData != null;
        if (questEnabled) {
            if (quest == null || !(quest.id >= 0)) {
                questEnabled = false;
            }
        }
        getButton(6).setEnabled(enabled);
        getButton(1).setEnabled(enabled);
        getButton(2).setEnabled(enabled);
        getButton(0).setEnabled(enabled && questEnabled);
        getButton(3).setEnabled(enabled && questEnabled);
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("NextQuestId")) {
            quest.readNBT(compound);
            setPrevQuestName(quest.title);
            if (compound.hasKey("NextQuestTitle")) {
                nextQuestName = compound.getString("NextQuestTitle");
            } else {
                nextQuestName = "";
            }
        } else {
            category.readNBT(compound);
            setPrevCatName(category.title);
            PacketClient.sendClient(new QuestsGetPacket(category.id, true));
            resetQuestList();
        }
        initGui();
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiEditText) {
            if (!((SubGuiEditText) subgui).cancelled) {
                if (category != null && category.id > -1) {
                    String name = ((SubGuiEditText) subgui).text;
                    if (name != null && !name.equalsIgnoreCase(category.title)) {
                        if (!(name.isEmpty() || catData.containsKey(name))) {
                            String old = category.title;
                            catData.remove(category.title);
                            category.title = name;
                            catData.put(category.title, category.id);
                            catScroll.replace(old, category.title);
                        }
                        saveType(false);
                    }
                }
            }
            clearCategory();
        }
        if (subgui instanceof SubGuiNpcQuest) {
            saveType(true);
        }
    }

    public void setPrevCatName(String selectedCat) {
        prevCatName = selectedCat;
        this.catScroll.setSelected(prevCatName);
    }

    public void setPrevQuestName(String selectedQuest) {
        prevQuestName = selectedQuest;
        this.questScroll.setSelected(prevQuestName);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            getCategory(false);
        }
        if (guiCustomScroll.id == 1) {
            getQuest(false);
        }
    }

    public void getCategory(boolean override) {
        if (catScroll.selected != -1) {
            String selected = catScroll.getSelected();
            if (!selected.equals(prevCatName) || override) {
                category = new QuestCategory();
                questScroll.selected = -1;
                questScroll.resetScroll();
                questSearch = "";
                quest = null;
                getTextField(66).setText("");
                PacketClient.sendClient(new QuestCategoryGetPacket(catData.get(selected)));
                setPrevCatName(selected);
            }
        }
    }

    public void getQuest(boolean override) {
        if (questScroll.selected != -1) {
            String selected = questScroll.getSelected();
            if (!selected.equals(prevQuestName) || override) {
                quest = new Quest();
                QuestGetPacket.getQuest(questData.get(selected));
                setPrevQuestName(selected);
            }
        }
    }

    public void clearCategory() {
        catScroll.setList(getCatSearch());
        catScroll.selected = -1;
        prevCatName = "";
        category = new QuestCategory();
        this.questData.clear();
        resetQuestList();
    }

    public void saveType(boolean saveQuest) {
        if (saveQuest) {
            if (questScroll.selected != -1 && quest.id >= 0) {
                if (catScroll.selected != -1 && category.id >= 0) {
                    PacketClient.sendClient(new QuestSavePacket(category.id, quest.writeToNBT(new NBTTagCompound()), true));
                }
            }
        } else {
            if (catScroll.selected != -1 && category.id >= 0)
                PacketClient.sendClient(new QuestCategorySavePacket(category.writeNBT(new NBTTagCompound())));
        }
    }

    public void save() {}

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data) {
        String name = catScroll.getSelected();
        this.catData = data;
        catScroll.setList(getCatSearch());
        if (name != null) {
            catScroll.setSelected(name);
            getCategory(false);
        } else {
            catScroll.setSelected(prevCatName);
            getCategory(true);
        }
        initGui();
    }

    @Override
    public void setSelected(String selected) {}

    @Override
    public void setScrollGroup(Vector<String> list, HashMap<String, Integer> data) {
        String name = questScroll.getSelected();
        this.questData = data;
        questScroll.setList(getQuestSearch());
        if (name != null) {
            questScroll.setSelected(name);
            getQuest(false);
        } else {
            questScroll.setSelected(prevQuestName);
            getQuest(true);
        }
        initGui();
    }

    @Override
    public void setSelectedGroup(String selected) {}

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result)
            return;
        if (id == 5) {
            if (catData.containsKey(catScroll.getSelected())) {
                PacketClient.sendClient(new QuestCategoryRemovePacket(category.id));
                clearCategory();
            }
        }
        if (id == 2) {
            PacketClient.sendClient(new QuestRemovePacket(quest.id, true));
            quest = new Quest();
            questData.clear();
        }
        updateButtons();
    }
}
