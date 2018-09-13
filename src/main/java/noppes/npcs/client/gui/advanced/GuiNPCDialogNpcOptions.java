package noppes.npcs.client.gui.advanced;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNPCDialogSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogOption;
import noppes.npcs.entity.EntityNPCInterface;


public class GuiNPCDialogNpcOptions extends GuiNPCInterface2 implements GuiSelectionListener,IGuiData{
	private GuiScreen parent;
	private HashMap<Integer, DialogOption> data = new HashMap<Integer,DialogOption>();

	public GuiNPCDialogNpcOptions(EntityNPCInterface npc, GuiScreen parent) {
		super(npc);
		this.parent = parent;
		this.drawDefaultBackground = true;
		Client.sendData(EnumPacketServer.DialogNpcGet);
	}

	public void initGui() {
		super.initGui();
		for (int i = 0; i < 12; i++) {
			int offset = i >=6 ?200:0;
			this.addButton(new GuiNpcButton(i + 20, guiLeft + 20 + offset, guiTop + 13 + i % 6 * 22, 20, 20, "X"));
			this.addLabel(new GuiNpcLabel(i, "" + i, guiLeft + 6 + offset, guiTop + 18 + i % 6 * 22));
			
			String title = "dialog.selectoption";
			if(data.containsKey(i))
				title = data.get(i).title;
			this.addButton(new GuiNpcButton(i, guiLeft + 44 + offset, guiTop + 13 +  i % 6 * 22, 140, 20, title));

		}
	}

	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
	}
	private int selectedSlot;
	protected void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		
		if (id == 1) {
			NoppesUtil.openGUI(player, parent);
		}
		if (id >= 0 && id < 20) {
			close();
			selectedSlot = id;
			int dialogID = -1;
			if(data.containsKey(id))
				dialogID = data.get(id).dialogId;
			NoppesUtil.openGUI(player, new GuiNPCDialogSelection(npc, this, dialogID));
		}
		if (id >= 20 && id < 40) {
			int slot = id - 20;
			data.remove(slot);
			Client.sendData(EnumPacketServer.DialogNpcRemove,slot);
			initGui();
		}
	}

	public void save() {
		return;
	}

	@Override
	public void selected(int id, String name) {
		Client.sendData(EnumPacketServer.DialogNpcSet, selectedSlot, id);
	}


	@Override
	public void setGuiData(NBTTagCompound compound) {
		int pos = compound.getInteger("Position");
		
		DialogOption dialog = new DialogOption();
		dialog.readNBT(compound);
		
		data.put(pos, dialog);
		initGui();
	}

}
