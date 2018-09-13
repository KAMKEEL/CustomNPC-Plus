
package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.Client;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;

import org.lwjgl.opengl.GL11;

public class GuiNpcTraderSetup extends GuiContainerNPCInterface2 implements ITextfieldListener{
	
	private final ResourceLocation slot = new ResourceLocation("customnpcs","textures/gui/slot.png");
	private RoleTrader role;
	
    public GuiNpcTraderSetup(EntityNPCInterface npc, ContainerNPCTraderSetup container){
        super(npc, container);
    	ySize = 220;
    	menuYOffset = 10;
    	role = container.role;
    }
    
	@Override
    public void initGui(){
    	super.initGui();
        buttonList.clear();
        setBackground("tradersetup.png");
        addLabel(new GuiNpcLabel(0, "role.marketname", guiLeft + 214, guiTop + 150));
        addTextField(new GuiNpcTextField(0, this, guiLeft + 214, guiTop + 160, 180, 20, role.marketName));

        addLabel(new GuiNpcLabel(1, "gui.ignoreDamage", guiLeft + 260, guiTop + 29));
        addButton(new GuiNpcButtonYesNo(1, guiLeft + 340, guiTop + 24, role.ignoreDamage));
        
        addLabel(new GuiNpcLabel(2, "gui.ignoreNBT", guiLeft + 260, guiTop + 51));
        addButton(new GuiNpcButtonYesNo(2, guiLeft + 340, guiTop + 46, role.ignoreNBT));
    }

	@Override
    public void drawScreen(int i, int j, float f){
    	guiTop += 10;
    	super.drawScreen(i, j, f);
    	guiTop -= 10;
    }

    @Override
    public void actionPerformed(GuiButton guibutton){
    	if(guibutton.id == 1){
    		role.ignoreDamage = ((GuiNpcButtonYesNo)guibutton).getBoolean();
    	}
    	if(guibutton.id == 2){
    		role.ignoreNBT = ((GuiNpcButtonYesNo)guibutton).getBoolean();
    	}
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
    	super.drawGuiContainerBackgroundLayer(f, i, j);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		for(int slot = 0; slot < 18; slot++){
			int x = guiLeft + slot%3 * 94 + 7;
			int y = guiTop + slot/3 * 22 + 4;

	        mc.renderEngine.bindTexture(this.slot);
	        GL11.glColor4f(1, 1, 1, 1);
	        drawTexturedModalRect(x - 1, y, 0, 0, 18, 18);
	        drawTexturedModalRect(x + 17, y, 0, 0, 18, 18);
			
            fontRendererObj.drawString("=", x + 36, y + 5, CustomNpcResourceListener.DefaultTextColor);
	        mc.renderEngine.bindTexture(this.slot);
	        GL11.glColor4f(1, 1, 1, 1);
	        drawTexturedModalRect(x + 42, y, 0, 0, 18, 18);
		}
    }
	
	@Override
	public void save() {
		Client.sendData(EnumPacketServer.TraderMarketSave, role.marketName, false);
		Client.sendData(EnumPacketServer.RoleSave, role.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		String name = guiNpcTextField.getText();
		if(!name.equalsIgnoreCase(role.marketName)){
			role.marketName = name;
			Client.sendData(EnumPacketServer.TraderMarketSave, role.marketName, true);
		}
			
	}
}
