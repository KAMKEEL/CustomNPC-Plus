package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.tags.TagsGetPacket;
import kamkeel.npcs.network.packets.request.tags.TagsNpcGetPacket;
import kamkeel.npcs.network.packets.request.tags.TagSetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SubGuiClonerNPCTags extends SubGuiInterface implements IGuiData,IScrollData {
    private GuiCustomScroll scrollTags;
    private GuiCustomScroll npcTags;
    private final ArrayList<String> allTags = new ArrayList<>();
    private final ArrayList<String> tagNames = new ArrayList<>();
    private String search = "";
    private final EntityNPCInterface npc;
    private GuiNpcMobSpawnerAdd mobSpawnerAdd;

    public SubGuiClonerNPCTags(EntityNPCInterface npc, GuiNpcMobSpawnerAdd guiNpcMobSpawnerAdd)
    {
        this.parent = guiNpcMobSpawnerAdd;
        this.mobSpawnerAdd = guiNpcMobSpawnerAdd;
        PacketClient.sendClient(new TagsGetPacket());
        PacketClient.sendClient(new TagsNpcGetPacket());
        setBackground("menubg.png");
        xSize = 305;
        ySize = 220;
        this.npc = npc;
        closeOnEsc = true;
    }

    public void initGui()
    {
        super.initGui();

        addLabel(new GuiNpcLabel(5, npc.display.name + " " + StatCollector.translateToLocal("tags.tags"), guiLeft + 10, guiTop + 8));

        addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("tags.allTags"), guiLeft + 10, guiTop + 22));
        if(scrollTags == null){
            scrollTags = new GuiCustomScroll(this,0);
            scrollTags.setSize(110, 145);
        }
        scrollTags.guiLeft = guiLeft + 10;
        scrollTags.guiTop = guiTop + 34;
        this.addScroll(scrollTags);
        addTextField(new GuiNpcTextField(4, this, fontRendererObj, guiLeft + 10, guiTop + 24 + 160, 110, 20, search));

        addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("tags.selectedTags"), guiLeft + 185, guiTop + 22));
        if(npcTags == null){
            npcTags = new GuiCustomScroll(this,1);
            npcTags.setSize(110, 170);
        }
        npcTags.guiLeft = guiLeft + 185;
        npcTags.guiTop = guiTop + 34;
        npcTags.setList(tagNames);
        this.addScroll(npcTags);

        addButton(new GuiNpcButton(66, guiLeft + 125, guiTop + 34, 55, 20, "gui.save"));

        addButton(new GuiNpcButton(10, guiLeft + 125, guiTop + 90, 55, 20, ">"));
        addButton(new GuiNpcButton(11, guiLeft + 125, guiTop + 112, 55, 20, "<"));

        addButton(new GuiNpcButton(12, guiLeft + 125, guiTop + 140, 55, 20, ">>"));
        addButton(new GuiNpcButton(13, guiLeft + 125, guiTop + 162, 55, 20, "<<"));

    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 10 && scrollTags.hasSelected() && !tagNames.contains(scrollTags.getSelected())) {
            tagNames.add(scrollTags.getSelected());
        }
        if (guibutton.id == 12) {
            tagNames.clear();
            tagNames.addAll(allTags);
        }
        if (guibutton.id == 11 && npcTags.hasSelected()) {
            tagNames.remove(npcTags.getSelected());
        }
        if (guibutton.id == 13) {
            tagNames.clear();
        }
        if (guibutton.id == 66) {
            close();
        }
        initGui();
    }

    @Override
    public void close(){
        super.close();
        save();
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data)
    {
        allTags.addAll(data.keySet());
        allTags.sort(String.CASE_INSENSITIVE_ORDER);
        scrollTags.setList(allTags);
        initGui();
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        NBTTagList tagList = compound.getTagList("TagNames",8);
        tagNames.clear();
        for (int i = 0; i < tagList.tagCount(); i++) {
            tagNames.add(tagList.getStringTagAt(i));
        }
    }

    public void mouseClicked(int i, int j, int k)
    {
        super.mouseClicked(i, j, k);
        if(k == 0 && scrollTags != null)
            scrollTags.mouseClicked(i, j, k);
    }

    @Override
    public void setSelected(String selected) {
        scrollTags.setSelected(selected);
    }

    @Override
    public void save() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        NBTTagList tagList = new NBTTagList();
        NBTTagList UUIDTagList = new NBTTagList();
        for (String string : this.tagNames) {
            tagList.appendTag(new NBTTagString(string));
            if(GuiNpcMobSpawnerAdd.tagMap.containsKey(string))
                UUIDTagList.appendTag(new NBTTagString(GuiNpcMobSpawnerAdd.tagMap.get(string).toString()));
        }

        GuiNpcMobSpawnerAdd.tagsCompound = UUIDTagList;
        tagCompound.setTag("TagNames",tagList);
        PacketClient.sendClient(new TagSetPacket(tagCompound));
    }

    public void keyTyped(char c, int i)
    {
        super.keyTyped(c, i);
        if(getTextField(4) != null){
            if(search.equals(getTextField(4).getText()))
                return;
            search = getTextField(4).getText().toLowerCase();
            scrollTags.setList(getSearchList());
        }
    }

    private List<String> getSearchList(){
        if(search.isEmpty()){
            return new ArrayList<String>(allTags);
        }
        List<String> list = new ArrayList<String>();
        for(String name : this.allTags){
            if(name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }
}
