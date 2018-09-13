package noppes.npcs.client.gui.advanced;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.TransformData;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCNightSetup extends GuiNPCInterface2 implements IGuiData
{
	private TransformData data;
	
    public GuiNPCNightSetup(EntityNPCInterface npc)
    {
    	super(npc);
    	data = npc.transform;
    	Client.sendData(EnumPacketServer.TransformGet);
    }

    public void initGui()
    {
        super.initGui();

        this.addLabel(new GuiNpcLabel(0, "menu.display", guiLeft + 4, guiTop + 25));
        this.addButton(new GuiNpcButton(0, guiLeft + 104, guiTop + 20, 50, 20, new String[]{"gui.no","gui.yes"}, data.hasDisplay?1:0));

        this.addLabel(new GuiNpcLabel(1, "menu.stats", guiLeft + 4, guiTop + 47));
        this.addButton(new GuiNpcButton(1, guiLeft + 104, guiTop + 42, 50, 20, new String[]{"gui.no","gui.yes"}, data.hasStats?1:0));

        this.addLabel(new GuiNpcLabel(2, "menu.ai", guiLeft + 4, guiTop + 69));
        this.addButton(new GuiNpcButton(2, guiLeft + 104, guiTop + 64, 50, 20, new String[]{"gui.no","gui.yes"}, data.hasAi?1:0));

        this.addLabel(new GuiNpcLabel(3, "menu.inventory", guiLeft + 4, guiTop + 91));
        this.addButton(new GuiNpcButton(3, guiLeft + 104, guiTop + 86, 50, 20, new String[]{"gui.no","gui.yes"}, data.hasInv?1:0));

        this.addLabel(new GuiNpcLabel(4, "menu.advanced", guiLeft + 4, guiTop + 113));
        this.addButton(new GuiNpcButton(4, guiLeft + 104, guiTop + 108, 50, 20, new String[]{"gui.no","gui.yes"}, data.hasAdvanced?1:0));
        
        this.addLabel(new GuiNpcLabel(5, "role.name", guiLeft + 4, guiTop + 135));
        this.addButton(new GuiNpcButton(5, guiLeft + 104, guiTop + 130, 50, 20, new String[]{"gui.no","gui.yes"}, data.hasRole?1:0));
        
        this.addLabel(new GuiNpcLabel(6, "job.name", guiLeft + 4, guiTop + 157));
        this.addButton(new GuiNpcButton(6, guiLeft + 104, guiTop + 152, 50, 20, new String[]{"gui.no","gui.yes"}, data.hasJob?1:0));
    
        this.addLabel(new GuiNpcLabel(10, "advanced.editingmode", guiLeft + 170, guiTop + 9));
        this.addButton(new GuiNpcButton(10, guiLeft + 244, guiTop + 4, 50, 20, new String[]{"gui.no","gui.yes"}, data.editingModus?1:0));
    
        if(data.editingModus){
        	this.addButton(new GuiNpcButton(11, guiLeft + 170, guiTop + 34, "advanced.loadday"));
        	this.addButton(new GuiNpcButton(12, guiLeft + 170, guiTop + 56, "advanced.loadnight"));
        }
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
    	GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0)
        	data.hasDisplay = button.getValue() == 1;
        if(button.id == 1)
        	data.hasStats = button.getValue() == 1;
        if(button.id == 2)
        	data.hasAi = button.getValue() == 1;
        if(button.id == 3)
        	data.hasInv = button.getValue() == 1;
        if(button.id == 4)
        	data.hasAdvanced = button.getValue() == 1;
        if(button.id == 5)
        	data.hasRole = button.getValue() == 1;
        if(button.id == 6)
        	data.hasJob = button.getValue() == 1;

        if(button.id == 10){
        	data.editingModus = button.getValue() == 1;
        	save();
        	initGui();
        }
        if(button.id == 11){
        	Client.sendData(EnumPacketServer.TransformLoad, false);
        }
        if(button.id == 12){
        	Client.sendData(EnumPacketServer.TransformLoad, true);
        }
    }
    
		
	public void save() {
		Client.sendData(EnumPacketServer.TransformSave, data.writeOptions(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		data.readOptions(compound);
		initGui();
	}
}
