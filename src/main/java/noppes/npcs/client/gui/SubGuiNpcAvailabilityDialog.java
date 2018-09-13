package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Availability;
import noppes.npcs.controllers.Dialog;

public class SubGuiNpcAvailabilityDialog extends SubGuiInterface implements GuiSelectionListener, IGuiData{
	private Availability availabitily;
	private int slot = 0;
	
    public SubGuiNpcAvailabilityDialog(Availability availabitily){
    	this.availabitily = availabitily;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    @Override
    public void initGui(){
        super.initGui();
        addLabel(new GuiNpcLabel(1,"availability.available", guiLeft, guiTop + 4));
        getLabel(1).center(xSize);

        int y = guiTop + 12;
        this.addButton(new GuiNpcButton(0, guiLeft + 4, y, 50, 20, new String[]{"availability.always","availability.after","availability.before"},availabitily.dialogAvailable.ordinal()));
    	this.addButton(new GuiNpcButton(10, guiLeft + 56, y, 172, 20, "availability.selectdialog"));
    	getButton(10).setEnabled(availabitily.dialogAvailable != EnumAvailabilityDialog.Always);
    	this.addButton(new GuiNpcButton(20, guiLeft + 230, y,20, 20, "X"));

    	y += 23;
        this.addButton(new GuiNpcButton(1, guiLeft + 4, y, 50, 20, new String[]{"availability.always","availability.after","availability.before"},availabitily.dialog2Available.ordinal()));
    	this.addButton(new GuiNpcButton(11, guiLeft + 56, y, 172, 20, "availability.selectdialog"));
    	getButton(11).setEnabled(availabitily.dialog2Available != EnumAvailabilityDialog.Always);
    	this.addButton(new GuiNpcButton(21, guiLeft + 230, y,20, 20, "X"));

    	y += 23;
        this.addButton(new GuiNpcButton(2, guiLeft + 4, y, 50, 20, new String[]{"availability.always","availability.after","availability.before"},availabitily.dialog3Available.ordinal()));
    	this.addButton(new GuiNpcButton(12, guiLeft + 56, y, 172, 20, "availability.selectdialog"));
    	getButton(12).setEnabled(availabitily.dialog3Available != EnumAvailabilityDialog.Always);
    	this.addButton(new GuiNpcButton(22, guiLeft + 230, y,20, 20, "X"));

    	y += 23;
        this.addButton(new GuiNpcButton(3, guiLeft + 4, y, 50, 20, new String[]{"availability.always","availability.after","availability.before"},availabitily.dialog4Available.ordinal()));
    	this.addButton(new GuiNpcButton(13, guiLeft + 56, y, 172, 20, "availability.selectdialog"));
    	getButton(13).setEnabled(availabitily.dialog4Available != EnumAvailabilityDialog.Always);
    	this.addButton(new GuiNpcButton(23, guiLeft + 230, y,20, 20, "X"));
        
    	
    	this.addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 192,98, 20, "gui.done"));
    	
    	updateGuiButtons();
    }


    private void updateGuiButtons() {
		getButton(10).setDisplayText("availability.selectdialog");
		getButton(11).setDisplayText("availability.selectdialog");
		getButton(12).setDisplayText("availability.selectdialog");
		getButton(13).setDisplayText("availability.selectdialog");
		
		if(availabitily.dialogId >= 0){
			Client.sendData(EnumPacketServer.DialogGet, availabitily.dialogId);
		}
		if(availabitily.dialog2Id >= 0){
			Client.sendData(EnumPacketServer.DialogGet, availabitily.dialog2Id);
		}
		if(availabitily.dialog3Id >= 0){
			Client.sendData(EnumPacketServer.DialogGet, availabitily.dialog3Id);
		}
		if(availabitily.dialog4Id >= 0){
			Client.sendData(EnumPacketServer.DialogGet, availabitily.dialog4Id);
		}
	}

    @Override
	protected void actionPerformed(GuiButton guibutton){
    	GuiNpcButton button = (GuiNpcButton) guibutton;

        if(button.id == 0){
        	availabitily.dialogAvailable = EnumAvailabilityDialog.values()[button.getValue()];
        	if(availabitily.dialogAvailable == EnumAvailabilityDialog.Always)
        		availabitily.dialogId = -1;
        	initGui();
        }
        if(button.id == 1){
        	availabitily.dialog2Available = EnumAvailabilityDialog.values()[button.getValue()];
        	if(availabitily.dialog2Available == EnumAvailabilityDialog.Always)
        		availabitily.dialog2Id = -1;
        	initGui();
        }
        if(button.id == 2){
        	availabitily.dialog3Available = EnumAvailabilityDialog.values()[button.getValue()];
        	if(availabitily.dialog3Available == EnumAvailabilityDialog.Always)
        		availabitily.dialog3Id = -1;
        	initGui();
        }
        if(button.id == 3){
        	availabitily.dialog4Available = EnumAvailabilityDialog.values()[button.getValue()];
        	if(availabitily.dialog4Available == EnumAvailabilityDialog.Always)
        		availabitily.dialog4Id = -1;
        	initGui();
        }
        if(button.id == 10){
        	slot = 1;
        	GuiNPCDialogSelection gui = new GuiNPCDialogSelection(npc, getParent(), availabitily.dialogId);
        	gui.listener = this;
        	NoppesUtil.openGUI(player, gui);
        }
        if(button.id == 11){
        	slot = 2;
        	GuiNPCDialogSelection gui = new GuiNPCDialogSelection(npc, getParent(), availabitily.dialog2Id);
        	gui.listener = this;
        	NoppesUtil.openGUI(player, gui);
        }
        if(button.id == 12){
        	slot = 3;
        	GuiNPCDialogSelection gui = new GuiNPCDialogSelection(npc, getParent(), availabitily.dialog3Id);
        	gui.listener = this;
        	NoppesUtil.openGUI(player, gui);
        }
        if(button.id == 13){
        	slot = 4;
        	GuiNPCDialogSelection gui = new GuiNPCDialogSelection(npc, getParent(), availabitily.dialog4Id);
        	gui.listener = this;
        	NoppesUtil.openGUI(player, gui);
        }
        if(button.id == 20){
        	availabitily.dialogId = -1;
    		getButton(10).setDisplayText("availability.selectdialog");
        }
        if(button.id == 21){
        	availabitily.dialog2Id = -1;
    		getButton(11).setDisplayText("availability.selectdialog");
        }
        if(button.id == 22){
        	availabitily.dialog3Id = -1;
    		getButton(12).setDisplayText("availability.selectdialog");
        }
        if(button.id == 23){
        	availabitily.dialog4Id = -1;
    		getButton(13).setDisplayText("availability.selectdialog");
        }
        
        if(button.id == 66){
    		close();
        }
    }

	@Override
	public void selected(int id, String name) {
		if(slot == 1)
			availabitily.dialogId = id;
		if(slot == 2)
			availabitily.dialog2Id = id;
		if(slot == 3)
			availabitily.dialog3Id = id;
		if(slot == 4)
			availabitily.dialog4Id = id;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		Dialog dialog = new Dialog();
		dialog.readNBT(compound);
		if(availabitily.dialogId == dialog.id)
			getButton(10).setDisplayText(dialog.title);
		if(availabitily.dialog2Id == dialog.id)
			getButton(11).setDisplayText(dialog.title);
		if(availabitily.dialog3Id == dialog.id)
			getButton(12).setDisplayText(dialog.title);
		if(availabitily.dialog4Id == dialog.id)
			getButton(13).setDisplayText(dialog.title);
	}

}
