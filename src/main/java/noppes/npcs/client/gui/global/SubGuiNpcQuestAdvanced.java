package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiMailmanSendSetup;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.constants.EnumStandingType;
import noppes.npcs.controllers.PlayerMail;
import noppes.npcs.controllers.Quest;

public class SubGuiNpcQuestAdvanced extends SubGuiInterface implements ITextfieldListener 
{
	private Quest quest;
	private GuiNPCManageQuest parent;
	
    public SubGuiNpcQuestAdvanced(Quest quest, GuiNPCManageQuest parent){
    	this.quest = quest;
    	this.parent = parent;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui(){
        super.initGui();
		addLabel(new GuiNpcLabel(10, "faction.options", guiLeft + 4, guiTop + 17));
		addButton(new GuiNpcButton(10, guiLeft + 120, guiTop + 12, 50, 20, "selectServer.edit"));

		addButton(new GuiNpcButton(13, guiLeft + 4, guiTop + 35, 164, 20, "mailbox.setup"));
		addButton(new GuiNpcButton(14, guiLeft + 170, guiTop + 35, 20, 20, "X"));
		if(!quest.mail.subject.isEmpty())
			getButton(13).setDisplayText(quest.mail.subject);
		
		addButton(new GuiNpcButton(11, guiLeft + 4, guiTop + 58, 164, 20, "quest.next"));
		addButton(new GuiNpcButton(12, guiLeft + 170, guiTop + 58, 20, 20, "X"));
		if(!quest.nextQuestTitle.isEmpty())
			getButton(11).setDisplayText(quest.nextQuestTitle);

		addLabel(new GuiNpcLabel(9, "advMode.command", guiLeft + 4, guiTop + 86));
		addButton(new GuiNpcButton(9, guiLeft + 120, guiTop + 81, 50, 20, "selectServer.edit"));

    	addButton(new GuiNpcButton(66, guiLeft + 190, guiTop + 190, 60, 20, "gui.done"));
    }

	protected void actionPerformed(GuiButton guibutton){
		GuiNpcButton button = (GuiNpcButton) guibutton;
    	if(button.id == 9){
    		parent.setSubGui(new SubGuiNpcCommand(quest.command));
    	}
        if(button.id == 10)
        {	
        	parent.setSubGui(new SubGuiNpcFactionOptions(quest.factionOptions));
        }
        
        if(button.id == 11 && quest.id >= 0){
			NoppesUtil.openGUI(player, new GuiNPCQuestSelection(npc, getParent(), quest.nextQuestid));
        }
        
        if(button.id == 12 && quest.id >= 0){
        	quest.nextQuestid = -1;
        	initGui();
        }
    	if(button.id == 13){
    		parent.setSubGui(new SubGuiMailmanSendSetup(quest.mail, getParent()));
    	}
        if(button.id == 14){
        	quest.mail = new PlayerMail();
        	initGui();
        }
        if(button.id == 66){
        	close();
        }
    }

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		
	}

}
