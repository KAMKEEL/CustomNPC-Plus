package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.tags.TagGetPacket;
import kamkeel.npcs.network.packets.request.tags.TagRemovePacket;
import kamkeel.npcs.network.packets.request.tags.TagSavePacket;
import kamkeel.npcs.network.packets.request.tags.TagsGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageTags extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener, ITextfieldListener, IGuiData, ISubGuiListener {
    private GuiCustomScroll scrollTags;
    private HashMap<String, Integer> data = new HashMap<String, Integer>();
    private Tag tag = new Tag();
    private String selected = null;
    private String search = "";

    public GuiNPCManageTags(EntityNPCInterface npc) {
        super(npc);
        PacketClient.sendClient(new TagsGetPacket());
    }

    public void initGui() {
        super.initGui();

        this.addButton(new GuiNpcButton(0, guiLeft + 368, guiTop + 8, 45, 20, "gui.add"));
        this.addButton(new GuiNpcButton(1, guiLeft + 368, guiTop + 32, 45, 20, "gui.remove"));

        if (scrollTags == null) {
            scrollTags = new GuiCustomScroll(this, 0, 0);
            scrollTags.setSize(143, 185);
        }
        scrollTags.guiLeft = guiLeft + 220;
        scrollTags.guiTop = guiTop + 4;
        addScroll(scrollTags);
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 4 + 3 + 185, 143, 20, search));

        if (tag.id == -1)
            return;

        this.addTextField(new GuiNpcTextField(0, this, guiLeft + 40, guiTop + 4, 136, 20, tag.name));
        getTextField(0).setMaxStringLength(20);
        addLabel(new GuiNpcLabel(0, "gui.name", guiLeft + 8, guiTop + 9));

        addLabel(new GuiNpcLabel(10, "ID", guiLeft + 178, guiTop + 4));
        addLabel(new GuiNpcLabel(11, tag.id + "", guiLeft + 178, guiTop + 14));

        String color = Integer.toHexString(tag.color);
        while (color.length() < 6)
            color = "0" + color;
        addButton(new GuiNpcButton(10, guiLeft + 50, guiTop + 30, 60, 20, color));
        addLabel(new GuiNpcLabel(1, "gui.color", guiLeft + 8, guiTop + 35));
        getButton(10).setTextColor(tag.color);

        addLabel(new GuiNpcLabel(3, "faction.hidden", guiLeft + 8, guiTop + 59));
        this.addButton(new GuiNpcButton(3, guiLeft + 50, guiTop + 54, 60, 20, new String[]{"gui.no", "gui.yes"}, tag.hideTag ? 1 : 0));
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scrollTags.resetScroll();
                scrollTags.setList(getSearchList());
            }
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<String>(this.data.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : this.data.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 0) {
            save();
            String name = "New";
            while (data.containsKey(name))
                name += "_";
            Tag tag = new Tag(-1, name, 0x00FF00);

            NBTTagCompound compound = new NBTTagCompound();
            tag.writeNBT(compound);
            PacketClient.sendClient(new TagSavePacket(compound));
        }
        if (button.id == 1) {
            if (data.containsKey(scrollTags.getSelected())) {
                PacketClient.sendClient(new TagRemovePacket(data.get(selected)));
                scrollTags.clear();
                tag = new Tag();
                initGui();
            }
        }
        if (button.id == 3) {
            tag.hideTag = button.getValue() == 1;
        }
        if (button.id == 10) {
            this.setSubGui(new SubGuiColorSelector(tag.color));
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        this.tag = new Tag();
        tag.readNBT(compound);

        setSelected(tag.name);
        initGui();
    }


    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        String name = scrollTags.getSelected();
        this.data = data;
        scrollTags.setList(getSearchList());

        if (name != null)
            scrollTags.setSelected(name);
    }

    @Override
    public void setSelected(String selected) {
        this.selected = selected;
        scrollTags.setSelected(selected);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            save();
            selected = scrollTags.getSelected();
            PacketClient.sendClient(new TagGetPacket(data.get(selected)));
        }
    }

    public void save() {
        if (selected != null && data.containsKey(selected) && tag != null) {
            NBTTagCompound compound = new NBTTagCompound();
            tag.writeNBT(compound);

            PacketClient.sendClient(new TagSavePacket(compound));
        }
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        if (tag.id == -1)
            return;

        if (guiNpcTextField.id == 0) {
            String name = guiNpcTextField.getText();
            if (!name.isEmpty() && !data.containsKey(name)) {
                String old = tag.name;
                data.remove(tag.name);
                tag.name = name;
                data.put(tag.name, tag.id);
                selected = name;
                scrollTags.replace(old, tag.name);
            }
        } else if (guiNpcTextField.id == 1) {
            int color = 0;
            try {
                color = Integer.parseInt(guiNpcTextField.getText(), 16);
            } catch (NumberFormatException e) {
                color = 0;
            }
            tag.color = color;
            guiNpcTextField.setTextColor(tag.color);
        }

    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector) {
            tag.color = ((SubGuiColorSelector) subgui).color;
            initGui();
        }
    }

}
