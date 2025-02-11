package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.npc.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.Entity;
import net.minecraft.util.StatCollector;

import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;

import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNpcRemoteEditor extends GuiNPCInterface implements IScrollData, GuiYesNoCallback{

    private GuiCustomScroll scroll;
    private HashMap<String, Integer> data = new HashMap<String, Integer>();
    private String search = "";

	public GuiNpcRemoteEditor() {
		super();
        xSize = 256;
        setBackground("menubg.png");
        PacketClient.sendClient(new RemoteNpcsGetPacket());
        PacketClient.sendClient(new RemoteFreezeGetPacket());
	}
    public void initGui()
    {
        super.initGui();
        if(scroll == null){
	        scroll = new GuiCustomScroll(this,0, 0);
	        scroll.setSize(165, 188);
        }
        scroll.guiLeft = guiLeft + 4;
        scroll.guiTop = guiTop + 4;
        addScroll(scroll);

        String title = StatCollector.translateToLocal("remote.title");
        int x = (xSize - this.fontRendererObj.getStringWidth(title)) / 2;

        addTextField(new GuiNpcTextField(66, this, fontRendererObj, guiLeft + 4, guiTop + 5 + scroll.ySize, scroll.xSize, 20, search));

        this.addLabel(new GuiNpcLabel(0, title, guiLeft + x, guiTop - 8));

        this.addButton(new GuiNpcButton(0, guiLeft + 170, guiTop + 6,82,20, "selectServer.edit"));
        this.addButton(new GuiNpcButton(1, guiLeft + 170, guiTop + 28,82,20, "selectWorld.deleteButton"));
        this.addButton(new GuiNpcButton(2, guiLeft + 170, guiTop + 50,82,20, "remote.reset"));
		this.addButton(new GuiNpcButton(3, guiLeft + 170, guiTop + 132,82,20, "remote.freeze"));
        this.addButton(new GuiNpcButton(4, guiLeft + 170, guiTop + 72,82,20, "remote.tp"));
        this.addButton(new GuiNpcButton(5, guiLeft + 170, guiTop + 110,82,20, "remote.resetall"));
		this.addButton(new GuiNpcButton(6, guiLeft + 170, guiTop + 165,82,20, "menu.global"));
    }

    @Override
    public void confirmClicked(boolean flag, int i){
		if(flag){
            PacketClient.sendClient(new RemoteDeletePacket(data.get(scroll.getSelected())));
		}
		NoppesUtil.openGUI(player, this);
    }
	protected void actionPerformed(GuiButton guibutton)
    {
		int id = guibutton.id;
    	if(id == 3){
            PacketClient.sendClient(new RemoteFreezePacket());
    	}
    	if(id == 5){
    		for(int ids : data.values()){
                PacketClient.sendClient(new RemoteResetPacket(ids));
	    		Entity entity  = player.worldObj.getEntityByID(ids);
	    		if(entity != null && entity instanceof EntityNPCInterface)
	    			((EntityNPCInterface)entity).reset();
    		}
    	}
		if(id == 6){
			NoppesUtil.setLastNpc(null);
            PacketClient.sendClient(new RemoteGlobalMenuPacket());
		}

    	if(!data.containsKey(scroll.getSelected()))
    		return;

    	if(id == 0){
            PacketClient.sendClient(new RemoteMainMenuPacket(data.get(scroll.getSelected())));
    	}
    	if(id == 1){
            GuiYesNo guiyesno = new GuiYesNo(this, "Confirm", StatCollector.translateToLocal("gui.delete"), 0);
            displayGuiScreen(guiyesno);
    	}
    	if(id == 2){
            int selected = data.get(scroll.getSelected());
            PacketClient.sendClient(new RemoteResetPacket(selected));
    		Entity entity  = player.worldObj.getEntityByID(selected);
    		if(entity != null && entity instanceof EntityNPCInterface)
    			((EntityNPCInterface)entity).reset();
    	}
    	if(id == 4){
            PacketClient.sendClient(new RemoteTpToNpcPacket(data.get(scroll.getSelected())));
    		close();
    	}
    }

    @Override
    public void mouseClicked(int i, int j, int k)
    {
    	super.mouseClicked(i, j, k);
    	scroll.mouseClicked(i, j, k);
    }
    @Override
    public void keyTyped(char c, int i)
    {
        super.keyTyped(c, i);
        if(getTextField(66) != null){
            if(getTextField(66).isFocused()){
                if (i == 1)
                {
                    close();
                    return;
                }
                if(search.equals(getTextField(66).getText()))
                    return;
                search = getTextField(66).getText().toLowerCase();
                scroll.resetScroll();
                scroll.setList(getNPCSearch());
            }
        }
        if (i == 1)
        {
            close();
        }
    }

    private List<String> getNPCSearch(){
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
	public void save() {}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
        this.data = data;
        scroll.resetScroll();
        scroll.setList(getNPCSearch());
	}
	@Override
	public void setSelected(String selected) {
		getButton(3).setDisplayText(selected);
	}

}
