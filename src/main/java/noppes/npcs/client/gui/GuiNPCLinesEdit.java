package noppes.npcs.client.gui;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Line;
import noppes.npcs.controllers.Lines;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCLinesEdit extends GuiNPCInterface2 implements IGuiData
{
	private Lines lines;
	private GuiNpcTextField field;
	private GuiNpcSoundSelection gui;
	
	public GuiNPCLinesEdit(EntityNPCInterface npc, Lines lines)
    {
    	super(npc);
    	this.lines = lines;
    	Client.sendData(EnumPacketServer.MainmenuAdvancedGet);
    }

    public void initGui()
    {
        super.initGui();
        for(int i = 0; i < 8; i++){
        	String text = "";
        	String sound = "";
        	if(lines.lines.containsKey(i)){
        		Line line = lines.lines.get(i);
        		text = line.text;
        		sound = line.sound;
        	}
        	addTextField(new GuiNpcTextField(i,this, fontRendererObj, guiLeft + 4, guiTop + 4 + i * 24, 200, 20,text));
        	addTextField(new GuiNpcTextField(i + 8,this, fontRendererObj, guiLeft + 208, guiTop + 4 + i * 24, 146, 20,sound));
        	addButton(new GuiNpcButton(i, guiLeft + 358, guiTop + 4 + i * 24, 60, 20, "mco.template.button.select"));
        }
        
    }

	protected void actionPerformed(GuiButton guibutton)
    {
    	GuiNpcButton button = (GuiNpcButton) guibutton;
    	field = getTextField(button.id + 8);
    	NoppesUtil.openGUI(player, gui = new GuiNpcSoundSelection(npc, this, field.getText()));
    }

	@Override
	public void elementClicked() {
		field.setText(gui.getSelected());
		saveLines();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		npc.advanced.readToNBT(compound);
		initGui();
	}
	
	private void saveLines(){
		HashMap<Integer,Line> lines = new HashMap<Integer,Line>();
    	for(int i = 0; i < 8; i++){
    		GuiNpcTextField tf = getTextField(i);
    		GuiNpcTextField tf2 = getTextField(i + 8);
    		if(!tf.isEmpty()){
        		Line line = new Line();
    			line.text = tf.getText();
    			line.sound = tf2.getText();
        		lines.put(i, line);
    		}
    		
    	}
    	this.lines.lines = lines;
	}
	
	public void save() {
		saveLines();
		Client.sendData(EnumPacketServer.MainmenuAdvancedSave, npc.advanced.writeToNBT(new NBTTagCompound()));
	}


}
