package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.tags.TagsGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.data.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

/**
 * Reusable SubGui for selecting tags on any taggable object.
 * Pass in the current tagUUIDs, get back the modified set on close.
 */
public class SubGuiTagSelect extends SubGuiInterface implements IScrollData, ICustomScrollListener, ITextfieldListener {
    private GuiCustomScroll scrollAllTags;
    private GuiCustomScroll scrollSelectedTags;
    private final ArrayList<String> allTagNames = new ArrayList<>();
    private final ArrayList<String> selectedTagNames = new ArrayList<>();
    private final HashSet<UUID> tagUUIDs;
    private String search = "";

    public SubGuiTagSelect(HashSet<UUID> tagUUIDs) {
        this.tagUUIDs = tagUUIDs;
        setBackground("menubg.png");
        closeOnEsc = true;
        xSize = 430;
        ySize = 220;

        // Populate selected tag names from UUIDs
        TagController tc = TagController.getInstance();
        if (tc != null) {
            for (UUID uuid : tagUUIDs) {
                Tag tag = tc.getTagFromUUID(uuid);
                if (tag != null) {
                    selectedTagNames.add(tag.name);
                }
            }
        }

        PacketClient.sendClient(new TagsGetPacket());
    }

    public void initGui() {
        super.initGui();

        addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("tags.allTags"), guiLeft + 22, guiTop + 11));
        if (scrollAllTags == null) {
            scrollAllTags = new GuiCustomScroll(this, 0);
            scrollAllTags.setSize(150, 155);
        }
        scrollAllTags.guiLeft = guiLeft + 20;
        scrollAllTags.guiTop = guiTop + 24;
        this.addScroll(scrollAllTags);
        addTextField(new GuiNpcTextField(4, this, fontRendererObj, guiLeft + 20, guiTop + 24 + 160, 150, 20, search));

        addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("tags.selectedTags"), guiLeft + 252, guiTop + 11));
        if (scrollSelectedTags == null) {
            scrollSelectedTags = new GuiCustomScroll(this, 1);
            scrollSelectedTags.setSize(150, 180);
        }
        scrollSelectedTags.guiLeft = guiLeft + 250;
        scrollSelectedTags.guiTop = guiTop + 24;
        scrollSelectedTags.setList(selectedTagNames);
        this.addScroll(scrollSelectedTags);

        addButton(new GuiNpcButton(10, guiLeft + 185, guiTop + 90, 55, 20, ">"));
        addButton(new GuiNpcButton(11, guiLeft + 185, guiTop + 112, 55, 20, "<"));
        addButton(new GuiNpcButton(12, guiLeft + 185, guiTop + 140, 55, 20, ">>"));
        addButton(new GuiNpcButton(13, guiLeft + 185, guiTop + 162, 55, 20, "<<"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 10 && scrollAllTags.hasSelected() && !selectedTagNames.contains(scrollAllTags.getSelected())) {
            selectedTagNames.add(scrollAllTags.getSelected());
        }
        if (guibutton.id == 12) {
            selectedTagNames.clear();
            selectedTagNames.addAll(allTagNames);
        }
        if (guibutton.id == 11 && scrollSelectedTags.hasSelected()) {
            selectedTagNames.remove(scrollSelectedTags.getSelected());
        }
        if (guibutton.id == 13) {
            selectedTagNames.clear();
        }
        initGui();
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        allTagNames.clear();
        allTagNames.addAll(data.keySet());
        allTagNames.sort(String.CASE_INSENSITIVE_ORDER);
        scrollAllTags.setList(getSearchList());
        initGui();
    }

    @Override
    public void save() {
        tagUUIDs.clear();
        TagController tc = TagController.getInstance();
        if (tc != null) {
            for (String name : selectedTagNames) {
                Tag tag = tc.getTagFromName(name);
                if (tag != null) {
                    tagUUIDs.add(tag.uuid);
                }
            }
        }
    }

    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if (k == 0 && scrollAllTags != null)
            scrollAllTags.mouseClicked(i, j, k);
    }

    @Override
    public void setSelected(String selected) {
        if (scrollAllTags != null)
            scrollAllTags.setSelected(selected);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
    }

    public void keyTyped(char c, int i) {
        if (i == 1) {
            close();
            return;
        }
        super.keyTyped(c, i);
        if (getTextField(4) != null) {
            if (search.equals(getTextField(4).getText()))
                return;
            search = getTextField(4).getText().toLowerCase();
            scrollAllTags.setList(getSearchList());
        }
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<>(allTagNames);
        }
        List<String> list = new ArrayList<>();
        for (String name : allTagNames) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }
}
