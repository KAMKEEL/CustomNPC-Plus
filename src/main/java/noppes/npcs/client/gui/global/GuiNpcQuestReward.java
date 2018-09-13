package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.controllers.Quest;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class GuiNpcQuestReward extends GuiContainerNPCInterface implements ITextfieldListener
{
	private Quest quest;
	private ResourceLocation resource;
    public GuiNpcQuestReward(EntityNPCInterface npc,ContainerNpcQuestReward container){
        super(npc,container);
        this.quest = GuiNPCManageQuest.quest;
        resource = getResource("questreward.png");
    }
    public void initGui(){
        super.initGui();
        addLabel(new GuiNpcLabel(0,"quest.randomitem", guiLeft + 4, guiTop + 4));
        addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 14, 60, 20, new String[]{"gui.no", "gui.yes"}, quest.randomReward?1:0));
        
        addButton(new GuiNpcButton(5, guiLeft, guiTop+ ySize, 98, 20, "gui.back"));
        
        addLabel(new GuiNpcLabel(1, "quest.exp", guiLeft + 4, guiTop + 45));
        addTextField(new GuiNpcTextField(0, this, this.fontRendererObj, guiLeft + 4, guiTop + 55, 60, 20, quest.rewardExp + ""));
        getTextField(0).numbersOnly = true;
        getTextField(0).setMinMaxDefault(0, 99999, 0);
    }

    public void actionPerformed(GuiButton guibutton){
		int id = guibutton.id;
        if(id == 5){
        	NoppesUtil.openGUI(player,GuiNPCManageQuest.Instance);
        }
        if(id == 0){
        	quest.randomReward = ((GuiNpcButton)guibutton).getValue() == 1;
        }
    }

    public void onGuiClosed(){
    }
    
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
    	super.drawGuiContainerBackgroundLayer(f, i, j);
    }
    
	@Override
	public void save() {
		
	}
	
	@Override
	public void unFocused(GuiNpcTextField textfield) {
		quest.rewardExp = textfield.getInteger();
	}
}
