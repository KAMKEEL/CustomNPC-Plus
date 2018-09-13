package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.global.GuiNPCQuestSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobConversation;
import noppes.npcs.roles.JobConversation.ConversationLine;

public class GuiNpcConversation extends GuiNPCInterface2 implements ITextfieldListener, GuiSelectionListener
{	
	private JobConversation job;
	
	private int slot = -1;
	private GuiNPCQuestSelection questSelection;
	
    public GuiNpcConversation(EntityNPCInterface npc){
    	super(npc);    	
    	job = (JobConversation) npc.jobInterface;
    }

    public void initGui(){
    	super.initGui();

    	addLabel(new GuiNpcLabel(40, "gui.name", guiLeft + 40, guiTop + 4));
    	addLabel(new GuiNpcLabel(41, "gui.name", guiLeft + 240, guiTop + 4));
    	
    	addLabel(new GuiNpcLabel(42, "conversation.delay", guiLeft + 164, guiTop + 4));
    	addLabel(new GuiNpcLabel(43, "conversation.delay", guiLeft + 364, guiTop + 4));

		for (int i = 0; i < 14; i++) {
    		ConversationLine line = job.getLine(i);
			int offset = i >=7 ?200:0;
			this.addLabel(new GuiNpcLabel(i, "" + (i + 1), guiLeft + 5 + offset - (i > 8?6:0), guiTop + 18 + i % 7 * 22));
			this.addTextField(new GuiNpcTextField(i, this, this.fontRendererObj, guiLeft + 13 + offset, guiTop + 13 +  i % 7 * 22, 100, 20, line.npc));
			this.addButton(new GuiNpcButton(i, guiLeft + 115 + offset, guiTop + 13 + i % 7 * 22, 46, 20, "conversation.line"));
			
			if(i > 0){
				this.addTextField(new GuiNpcTextField(i + 14, this, this.fontRendererObj, guiLeft + 164 + offset, guiTop + 13 + i % 7 * 22, 30, 20, line.delay + ""));
				this.getTextField(i + 14).numbersOnly = true;
				this.getTextField(i + 14).setMinMaxDefault(5, 1000, 40);
			}
		}
		addLabel(new GuiNpcLabel(50, "conversation.delay", guiLeft + 202, guiTop + 175));
		addTextField(new GuiNpcTextField(50, this, fontRendererObj, guiLeft + 260, guiTop + 170, 40, 20, job.generalDelay + ""));
		getTextField(50).numbersOnly = true;
		getTextField(50).setMinMaxDefault(10, 1000000, 12000);

		addLabel(new GuiNpcLabel(54, "gui.range", guiLeft + 202, guiTop + 196));
		addTextField(new GuiNpcTextField(54, this, fontRendererObj, guiLeft + 260, guiTop + 191, 40, 20, job.range + ""));
		getTextField(54).numbersOnly = true;
		getTextField(54).setMinMaxDefault(4, 60, 20);
		
		addLabel(new GuiNpcLabel(51, "quest.quest", guiLeft + 13, guiTop + 175));
		String title = job.questTitle;
		if(title.isEmpty())
			title = "gui.select";
		addButton(new GuiNpcButton(51, guiLeft + 70, guiTop + 170, 100, 20, title));
		addButton(new GuiNpcButton(52, guiLeft + 171, guiTop + 170, 20, 20, "X"));

		addLabel(new GuiNpcLabel(53, "availability.name", guiLeft + 13, guiTop + 196));
		addButton(new GuiNpcButton(53, guiLeft + 110, guiTop + 191, 60, 20, "selectServer.edit"));

		addButton(new GuiNpcButton(55, guiLeft + 310, guiTop + 181, 96, 20, new String[]{"gui.always", "gui.playernearby"}, job.mode));
    }

    protected void actionPerformed(GuiButton guibutton){
    	GuiNpcButton button = (GuiNpcButton) guibutton;
    	if(button.id >= 0 && button.id < 14){
    		slot = button.id;
    		ConversationLine line = job.getLine(slot);
    		setSubGui(new SubGuiNpcConversationLine(line.text, line.sound));
    	}
    	if(button.id == 51){
			NoppesUtil.openGUI(player, questSelection = new GuiNPCQuestSelection(npc, this, job.quest));
    	}
    	if(button.id == 52){
    		job.quest = -1;
    		job.questTitle = "";
    		initGui();
    	}
    	if(button.id == 53){
			setSubGui(new SubGuiNpcAvailability(job.availability));
    	}
    	if(button.id == 55){
    		job.mode = button.getValue();
    	}
    }
	@Override
	public void selected(int ob, String name) {
		job.quest = ob;
		job.questTitle = questSelection.getSelected();
		initGui();
	}

    @Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		if(gui instanceof SubGuiNpcConversationLine){
	    	SubGuiNpcConversationLine sub = (SubGuiNpcConversationLine) gui;
			ConversationLine line = job.getLine(slot);
			line.text = sub.line;
			line.sound = sub.sound;
		}
	}
	
    @Override
	public void save() {
    	Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
    	if(textfield.id >= 0 && textfield.id < 14){
			ConversationLine line = job.getLine(textfield.id);
			line.npc = textfield.getText();
    	}
    	if(textfield.id >= 14 && textfield.id < 28){
			ConversationLine line = job.getLine(textfield.id - 14);
			line.delay = textfield.getInteger();
    	}
    	if(textfield.id == 50){
    		job.generalDelay = textfield.getInteger();
    	}
    	if(textfield.id == 54){
    		job.range = textfield.getInteger();
    	}
	}


}
