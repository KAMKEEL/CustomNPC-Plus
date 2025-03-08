package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.faction.FactionGetPacket;
import kamkeel.npcs.network.packets.request.faction.FactionRemovePacket;
import kamkeel.npcs.network.packets.request.faction.FactionSavePacket;
import kamkeel.npcs.network.packets.request.faction.FactionsGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiNpcFactionPoints;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.*;

public class GuiNPCManageFactions extends GuiNPCInterface2 implements IScrollData,ICustomScrollListener,ITextfieldListener, IGuiData, ISubGuiListener, GuiYesNoCallback
{
    private GuiCustomScroll scrollFactions;
    private HashMap<String,Integer> data = new HashMap<String,Integer>();
    private Faction faction = new Faction();
    private String selected = null;
    private String search = "";

    public GuiNPCManageFactions(EntityNPCInterface npc)
    {
        super(npc);
        PacketClient.sendClient(new FactionsGetPacket());
    }

    public void initGui()
    {
        super.initGui();

        this.addButton(new GuiNpcButton(0,guiLeft + 368, guiTop + 8, 45, 20, "gui.add"));
        this.addButton(new GuiNpcButton(1,guiLeft + 368, guiTop + 32, 45, 20, "gui.remove"));

        if(scrollFactions == null){
            scrollFactions = new GuiCustomScroll(this,0, 0);
            scrollFactions.setSize(143, 185);
        }
        scrollFactions.guiLeft = guiLeft + 220;
        scrollFactions.guiTop = guiTop + 4;
        addScroll(scrollFactions);
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 4 + 3 + 185, 143, 20, search));

        if (faction.id == -1)
            return;

        this.addTextField(new GuiNpcTextField(0, this, guiLeft + 40, guiTop + 4, 136, 20, faction.name));
        getTextField(0).setMaxStringLength(20);
        addLabel(new GuiNpcLabel(0,"gui.name", guiLeft + 8, guiTop + 9));

        addLabel(new GuiNpcLabel(10,"ID", guiLeft + 178, guiTop + 4));
        addLabel(new GuiNpcLabel(11, faction.id + "", guiLeft + 178, guiTop + 14));

        String color = Integer.toHexString(faction.color);
        while(color.length() < 6)
            color = "0" + color;
        addButton(new GuiNpcButton(10, guiLeft + 40, guiTop + 26, 60, 20, color));
        addLabel(new GuiNpcLabel(1,"gui.color", guiLeft + 8, guiTop + 31));
        getButton(10).setTextColor(faction.color);

        addLabel(new GuiNpcLabel(2,"faction.points", guiLeft + 105, guiTop + 31));
        this.addButton(new GuiNpcButton(2,guiLeft + 156, guiTop + 26, 45, 20, "selectServer.edit"));

        int y = guiTop + 48;

        addLabel(new GuiNpcLabel(3,"faction.hidden", guiLeft + 8, y + 5));
        this.addButton(new GuiNpcButton(3,guiLeft + 100, y, 45, 20, new String[]{"gui.no","gui.yes"},faction.hideFaction?1:0));

        y += 23;

        addLabel(new GuiNpcLabel(4,"faction.attacked", guiLeft + 8, y + 5));
        this.addButton(new GuiNpcButton(4,guiLeft + 100, y, 45, 20, new String[]{"gui.no","gui.yes"},faction.getsAttacked?1:0));

        y += 23;

        addLabel(new GuiNpcLabel(5,"faction.passive", guiLeft + 8, y + 5));
        this.addButton(new GuiNpcButton(5,guiLeft + 100, y, 45, 20, new String[]{"gui.no","gui.yes"},faction.isPassive?1:0));
        getButton(5).setHoverText("faction.passive.hover");

        addLabel(new GuiNpcLabel(6,"faction.hostiles", guiLeft + 8, guiTop + 120));

        ArrayList<String> hostileList = new ArrayList<String>(scrollFactions.getList());
        hostileList.remove(faction.name);

        HashSet<String> set = new HashSet<String>();
        for(String s : data.keySet()){
            if(!s.equals(faction.name) && faction.attackFactions.contains(data.get(s)))
                set.add(s);
        }

        GuiCustomScroll scrollHostileFactions = new GuiCustomScroll(this,1,true);
        scrollHostileFactions.setSize(163, 78);
        scrollHostileFactions.guiLeft = guiLeft + 4;
        scrollHostileFactions.guiTop = guiTop + 134;
        scrollHostileFactions.setList(hostileList);
        scrollHostileFactions.setSelectedList(set);
        addScroll(scrollHostileFactions);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0){
            save();
            String name = "New";
            while(data.containsKey(name))
                name += "_";
            Faction faction = new Faction(-1, name, 0x00FF00, 1000);

            NBTTagCompound compound = new NBTTagCompound();
            faction.writeNBT(compound);
            PacketClient.sendClient(new FactionSavePacket(compound));
        }
        if(button.id == 1){
            if(data.containsKey(scrollFactions.getSelected())) {
                GuiYesNo guiyesno = new GuiYesNo(this, scrollFactions.getSelected(), StatCollector.translateToLocal("gui.delete"), 1);
                displayGuiScreen(guiyesno);
            }
        }
        if(button.id == 2){
            this.setSubGui(new SubGuiNpcFactionPoints(faction));
        }
        if(button.id == 3){
            faction.hideFaction = button.getValue() == 1;
        }
        if(button.id == 4){
            faction.getsAttacked = button.getValue() == 1;
        }
        if(button.id == 4){
            faction.isPassive = button.getValue() == 1;
        }
        if(button.id == 10){
            this.setSubGui(new SubGuiColorSelector(faction.color));
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        this.faction = new Faction();
        faction.readNBT(compound);

        setSelected(faction.name);
        initGui();
    }

    @Override
    public void keyTyped(char c, int i)
    {
        super.keyTyped(c, i);
        if(getTextField(55) != null){
            if(getTextField(55).isFocused()){
                if(search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scrollFactions.resetScroll();
                scrollFactions.setList(getSearchList());
            }
        }
    }

    private List<String> getSearchList(){
        if(search.isEmpty()){
            return new ArrayList<String>(this.data.keySet());
        }
        List<String> list = new ArrayList<String>();
        for(String name : this.data.keySet()){
            if(name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        String name = scrollFactions.getSelected();
        this.data = data;
        scrollFactions.setList(getSearchList());

        if(name != null)
            scrollFactions.setSelected(name);
    }

    @Override
    public void setSelected(String selected) {
        this.selected = selected;
        scrollFactions.setSelected(selected);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if(guiCustomScroll.id == 0)
        {
            save();
            selected = scrollFactions.getSelected();
            FactionGetPacket.getFaction(data.get(selected));
        }
        else if(guiCustomScroll.id == 1)
        {
            HashSet<Integer> set = new HashSet<Integer>();
            for(String s : guiCustomScroll.getSelectedList()){
                if(data.containsKey(s))
                    set.add(data.get(s));
            }
            faction.attackFactions = set;
            save();
        }
    }

    public void save() {
        if(selected != null && data.containsKey(selected) && faction != null){
            NBTTagCompound compound = new NBTTagCompound();
            faction.writeNBT(compound);

            PacketClient.sendClient(new FactionSavePacket(compound));
        }
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        if(faction.id == -1)
            return;

        if(guiNpcTextField.id == 0) {
            String name = guiNpcTextField.getText();
            if(!name.isEmpty() && !data.containsKey(name)){
                String old = faction.name;
                data.remove(faction.name);
                faction.name = name;
                data.put(faction.name, faction.id);
                selected = name;
                scrollFactions.replace(old,faction.name);
            }
        } else if(guiNpcTextField.id == 1) {
            int color = 0;
            try{
                color = Integer.parseInt(guiNpcTextField.getText(),16);
            }
            catch(NumberFormatException e){
                color = 0;
            }
            faction.color = color;
            guiNpcTextField.setTextColor(faction.color);
        }

    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if(subgui instanceof SubGuiColorSelector){
            faction.color = ((SubGuiColorSelector)subgui).color;
            initGui();
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if(!result)
            return;
        if(id == 1) {
            if(data.containsKey(scrollFactions.getSelected())) {
                PacketClient.sendClient(new FactionRemovePacket(data.get(selected)));
                scrollFactions.clear();
                faction = new Faction();
                initGui();
            }
        }
    }

}
