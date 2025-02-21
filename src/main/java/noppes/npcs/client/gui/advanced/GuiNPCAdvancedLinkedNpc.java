package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.linked.LinkedGetAllPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedSetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCAdvancedLinkedNpc extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener{
	private GuiCustomScroll scroll;
	private List<String> data = new ArrayList<String>();

	public static GuiScreen Instance;

    public GuiNPCAdvancedLinkedNpc(EntityNPCInterface npc){
    	super(npc);
    	Instance = this;
        LinkedGetAllPacket.GetNPCs();
    }

    @Override
    public void initGui(){
        super.initGui();

       	this.addButton(new GuiNpcButton(1,guiLeft + 358, guiTop + 38, 58, 20, "gui.clear"));

        if(scroll == null){
	        scroll = new GuiCustomScroll(this,0);
	        scroll.setSize(143, 208);
        }
        scroll.guiLeft = guiLeft + 137;
        scroll.guiTop = guiTop + 4;
        scroll.setSelected(npc.linkedName);
        scroll.setList(data);
        this.addScroll(scroll);
    }

    @Override
	public void buttonEvent(GuiButton button){
        if(button.id == 1){
            PacketClient.sendClient(new LinkedSetPacket(""));
        }

    }

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data = new ArrayList<String>(list);
		initGui();
	}

	@Override
	public void setSelected(String selected) {
		scroll.setSelected(selected);
	}

	@Override
	public void save() {

	}

	@Override
	public void customScrollClicked(int i, int j, int k,
			GuiCustomScroll guiCustomScroll) {
        PacketClient.sendClient(new LinkedSetPacket(guiCustomScroll.getSelected()));
	}

}
