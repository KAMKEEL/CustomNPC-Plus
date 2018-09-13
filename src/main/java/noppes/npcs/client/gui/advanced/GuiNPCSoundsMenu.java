package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNpcSoundSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCSoundsMenu extends GuiNPCInterface2 implements ITextfieldListener
{
	private GuiNpcSoundSelection gui;
	private GuiNpcTextField selectedField;
    public GuiNPCSoundsMenu(EntityNPCInterface npc){
    	super(npc);
    }

    public void initGui()
    {
        super.initGui();
        addLabel(new GuiNpcLabel(0,"advanced.idlesound", guiLeft + 5, guiTop + 20));
        addTextField(new GuiNpcTextField(0,this, fontRendererObj, guiLeft + 80, guiTop + 15, 200, 20, npc.advanced.idleSound));
        addButton(new GuiNpcButton(0, guiLeft + 290, guiTop + 15, 80, 20, "gui.selectSound"));
                
        addLabel(new GuiNpcLabel(2,"advanced.angersound", guiLeft + 5, guiTop + 45));
        addTextField(new GuiNpcTextField(2,this, fontRendererObj, guiLeft + 80, guiTop + 40, 200, 20, npc.advanced.angrySound));
        addButton(new GuiNpcButton(2, guiLeft + 290, guiTop + 40, 80, 20, "gui.selectSound"));
        
        addLabel(new GuiNpcLabel(3,"advanced.hurtsound", guiLeft + 5, guiTop + 70));
        addTextField(new GuiNpcTextField(3,this, fontRendererObj, guiLeft + 80, guiTop + 65, 200, 20, npc.advanced.hurtSound));
        addButton(new GuiNpcButton(3, guiLeft + 290, guiTop + 65, 80, 20, "gui.selectSound"));
        
        addLabel(new GuiNpcLabel(4,"advanced.deathsound", guiLeft + 5, guiTop + 95));
        addTextField(new GuiNpcTextField(4,this, fontRendererObj, guiLeft + 80, guiTop + 90, 200, 20, npc.advanced.deathSound));
        addButton(new GuiNpcButton(4, guiLeft + 290, guiTop + 90, 80, 20, "gui.selectSound"));
        
        addLabel(new GuiNpcLabel(5,"advanced.stepsound", guiLeft + 5, guiTop + 120));
        addTextField(new GuiNpcTextField(5,this, fontRendererObj, guiLeft + 80, guiTop + 115, 200, 20, npc.advanced.stepSound));
        addButton(new GuiNpcButton(5, guiLeft + 290, guiTop + 115, 80, 20, "gui.selectSound"));

        addLabel(new GuiNpcLabel(6,"advanced.haspitch", guiLeft + 5, guiTop + 150));
        addButton(new GuiNpcButton(6, guiLeft + 120, guiTop + 145, 80, 20, new String[]{"gui.no","gui.yes"}, npc.advanced.disablePitch?0:1));
    
    }
    public void buttonEvent(GuiButton button){

		if(button.id == 6)
			npc.advanced.disablePitch =((GuiNpcButton)button).getValue() == 0;
		else{
	    	selectedField = getTextField(button.id);
	    	NoppesUtil.openGUI(player, gui = new GuiNpcSoundSelection(npc, this, selectedField.getText()));
		}
    	
    }
	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if(textfield.id == 0)
			npc.advanced.idleSound = textfield.getText();
		if(textfield.id == 2)
			npc.advanced.angrySound = textfield.getText();
		if(textfield.id == 3)
			npc.advanced.hurtSound = textfield.getText();
		if(textfield.id == 4)
			npc.advanced.deathSound = textfield.getText();
		if(textfield.id == 5)
			npc.advanced.stepSound = textfield.getText();
	}
	
	@Override
	public void elementClicked(){
		selectedField.setText(gui.getSelected());
		unFocused(selectedField);
	}
	
	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAdvancedSave, npc.advanced.writeToNBT(new NBTTagCompound()));
	}

}
