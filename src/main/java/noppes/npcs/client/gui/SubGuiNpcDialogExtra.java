package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Availability;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.controllers.PlayerMail;

public class SubGuiNpcDialogExtra extends SubGuiInterface implements ISubGuiListener
{
	private Dialog dialog;
	private int slot = 0;
	public GuiScreen parent2;
	
    public SubGuiNpcDialogExtra(Dialog dialog, GuiScreen parent)
    {
    	this.parent2 = parent;
    	this.dialog = dialog;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui()
    {
        super.initGui();        
        int y = guiTop + 4;
        
		addButton(new GuiNpcButton(13, guiLeft + 4, y, 164, 20, "mailbox.setup"));
		addButton(new GuiNpcButton(14, guiLeft + 170, y, 20, 20, "X"));
		if(!dialog.mail.subject.isEmpty())
			getButton(13).setDisplayText(dialog.mail.subject);

		addButton(new GuiNpcButton(10, guiLeft + 120, y += 22, 50, 20, "selectServer.edit"));
		addLabel(new GuiNpcLabel(10, "advMode.command", guiLeft + 4, y + 5));

		addButton(new GuiNpcButtonYesNo(11, guiLeft + 120, y += 22, dialog.hideNPC));
		addLabel(new GuiNpcLabel(11, "dialog.hideNPC", guiLeft + 4, y + 5));

		addButton(new GuiNpcButtonYesNo(12, guiLeft + 120, y += 22, dialog.showWheel));
		addLabel(new GuiNpcLabel(12, "dialog.showWheel", guiLeft + 4, y + 5));
		
		addButton(new GuiNpcButtonYesNo(15, guiLeft + 120, y += 22, dialog.disableEsc));
		addLabel(new GuiNpcLabel(15, "dialog.disableEsc", guiLeft + 4, y + 5));
    	
    	this.addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 192,98, 20, "gui.done"));
    	
    }

    @Override
	protected void actionPerformed(GuiButton guibutton){
    	GuiNpcButton button = (GuiNpcButton) guibutton;

    	if(button.id == 10){
    		setSubGui(new SubGuiNpcCommand(dialog.command));
    	}
    	if(button.id == 11){
    		dialog.hideNPC = button.getValue() == 1;
    	}
    	if(button.id == 12){
    		dialog.showWheel = button.getValue() == 1;
    	}
    	if(button.id == 15){
    		dialog.disableEsc = button.getValue() == 1;
    	}
    	if(button.id == 13){
			setSubGui(new SubGuiMailmanSendSetup(dialog.mail, getParent()));
    	}
        if(button.id == 14){
        	dialog.mail = new PlayerMail();
        	initGui();
        }
        
        if(button.id == 66)
        {
    		close();
        	if(parent2 != null)
        		NoppesUtil.openGUI(player, parent2);
        }
    }

	
	@Override
	public void subGuiClosed(SubGuiInterface subgui){
		if(subgui instanceof SubGuiNpcCommand){
			dialog.command = ((SubGuiNpcCommand) subgui).command;
		}
	}
}
