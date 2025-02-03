package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.dialog.DialogNpcGetPacket;
import kamkeel.npcs.network.packets.request.dialog.DialogNpcRemovePacket;
import kamkeel.npcs.network.packets.request.dialog.DialogNpcSetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;

import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.*;

import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashMap;


public class GuiNPCDialogNpcOptions extends GuiNPCInterface2 implements GuiSelectionListener, IGuiData{
	private GuiScreen parent;
	private HashMap<Integer, DialogOption> data = new HashMap<Integer,DialogOption>();

	public GuiNPCDialogNpcOptions(EntityNPCInterface npc, GuiScreen parent) {
		super(npc);
		this.parent = parent;
		this.drawDefaultBackground = true;
        PacketClient.sendClient(new DialogNpcGetPacket());
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
		if (id >= 0 && id < 20) {
			selectedSlot = id;
			int dialogID = -1;
			if(data.containsKey(id))
				dialogID = data.get(id).dialogId;
            setSubGui(new GuiDialogSelection(dialogID));
		}
		if (id >= 20 && id < 40) {
			int slot = id - 20;
			data.remove(slot);
            PacketClient.sendClient(new DialogNpcRemovePacket(slot));
			initGui();
		}
	}

	public void save() {
		return;
	}

	@Override
	public void selected(int id, String name) {
        PacketClient.sendClient(new DialogNpcSetPacket(selectedSlot, id));
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
