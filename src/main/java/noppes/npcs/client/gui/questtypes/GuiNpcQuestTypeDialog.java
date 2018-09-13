package noppes.npcs.client.gui.questtypes;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNPCDialogSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestDialog;

public class GuiNpcQuestTypeDialog extends SubGuiInterface implements GuiSelectionListener, IGuiData
{
	private GuiScreen parent;
	
	private QuestDialog quest;
	
	private HashMap<Integer, String> data = new HashMap<Integer, String>();

    public GuiNpcQuestTypeDialog(EntityNPCInterface npc, Quest q, GuiScreen parent) {
    	this.npc = npc;
    	this.parent = parent;
    	title = "Quest Dialog Setup";
    	
    	quest = (QuestDialog) q.questInterface;

		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
		Client.sendData(EnumPacketServer.QuestDialogGetTitle, 
				quest.dialogs.containsKey(0)?quest.dialogs.get(0):-1, 
				quest.dialogs.containsKey(1)?quest.dialogs.get(1):-1, 
				quest.dialogs.containsKey(2)?quest.dialogs.get(2):-1);
	}

	public void initGui() {
		super.initGui();
		for (int i = 0; i < 3; i++) {
			String title = "dialog.selectoption";
			if(data.containsKey(i))
				title = data.get(i);
			this.addButton(new GuiNpcButton(i + 9, guiLeft + 10, 55 + i * 22, 20, 20, "X"));
			this.addButton(new GuiNpcButton(i + 3, guiLeft + 34, 55 + i * 22, 210, 20, title));

		}
		this.addButton(new GuiNpcButton(0, guiLeft + 150, guiTop + 190, 98, 20, "gui.back"));
		
	}

	private int selectedSlot;
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 0) {
			close();
		}
		if (button.id >= 3 && button.id < 9) {
			selectedSlot = button.id - 3;
			int id = -1;
			if(quest.dialogs.containsKey(selectedSlot))
				id = quest.dialogs.get(selectedSlot);
			GuiNPCDialogSelection gui = new GuiNPCDialogSelection(npc, parent, id);
			gui.listener = this;
			NoppesUtil.openGUI(player, gui);
		}
		if (button.id >= 9 && button.id < 15) {
			int slot = button.id - 9;
			quest.dialogs.remove(slot);
			data.remove(slot);
			save();
			initGui();
		}
	}

	public void save() {
	}

	@Override
	public void selected(int id, String name) {
		quest.dialogs.put(selectedSlot, id);
		data.put(selectedSlot, name);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		data.clear();
		if(compound.hasKey("1")){
			data.put(0, compound.getString("1"));
		}
		if(compound.hasKey("2")){
			data.put(1, compound.getString("2"));
		}
		if(compound.hasKey("3")){
			data.put(2, compound.getString("3"));
		}
		initGui();
	}

}
