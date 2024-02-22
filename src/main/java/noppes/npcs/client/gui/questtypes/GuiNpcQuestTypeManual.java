package noppes.npcs.client.gui.questtypes;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestManual;

public class GuiNpcQuestTypeManual extends SubGuiInterface implements ITextfieldListener
{
	private QuestManual quest;

    public GuiNpcQuestTypeManual(EntityNPCInterface npc, Quest q, GuiScreen parent) {
    	this.npc = npc;
    	title = "Quest Manual Setup";
    	quest = (QuestManual) q.questInterface;

		setBackground("largebg.png");
		bgScale = 1.7F;
		bgScaleX = 1.1F;

		xSize = 300;
		ySize = 300;
		closeOnEsc = true;
	}

	public void initGui() {
		super.initGui();

		int i = 0;
		guiTop -= 20;

		addLabel(new GuiNpcLabel(0, "Type a name for each objective here and defined the amount requied to complete.", guiLeft + 4, guiTop + 20));

		for (String name : quest.objectives.keySet()) {
			this.addTextField(new GuiNpcTextField(i, this, fontRendererObj, guiLeft + 4, guiTop + 40 + i * 22, 180, 20, name));
			this.addTextField(new GuiNpcTextField(i + 12, this, fontRendererObj, guiLeft + 186, guiTop + 40 + i * 22, 24, 20, quest.objectives.get(name) + ""));
			this.getTextField(i+12).integersOnly = true;
			this.getTextField(i+12).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
			i++;
		}
		
		for(;i < 12; i++){
			this.addTextField(new GuiNpcTextField(i, this, fontRendererObj, guiLeft + 4, guiTop + 40 + i * 22, 180, 20, ""));
			this.addTextField(new GuiNpcTextField(i + 12, this, fontRendererObj, guiLeft + 186, guiTop + 40 + i * 22, 24, 20, "1"));
			this.getTextField(i+12).integersOnly = true;
			this.getTextField(i+12).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
		}

		guiTop += 50;
		this.addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 310, 98, 20, "gui.back"));

		guiTop -= 50;
	}

	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 0) {
			close();
		}
	}
    public void mouseClicked(int i, int j, int k)
    {
    	super.mouseClicked(i, j, k);
    }

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
        saveObjectives();
	}

	private void saveObjectives(){
		HashMap<String,Integer> map = new HashMap<String,Integer>();
        
		for(int i = 0; i< 12; i++){
			String name = getTextField(i).getText();
			
            if(name.isEmpty()) {
                continue;
            }

			map.put(name, getTextField(i+12).getInteger());
		}

		quest.objectives = map;
	}
}
