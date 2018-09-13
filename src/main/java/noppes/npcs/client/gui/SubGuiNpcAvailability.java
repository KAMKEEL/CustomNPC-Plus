package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNPCQuestSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.Availability;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.controllers.Faction;
import noppes.npcs.controllers.Quest;

public class SubGuiNpcAvailability extends SubGuiInterface implements ITextfieldListener, GuiSelectionListener, IGuiData 
{
	private Availability availabitily;
	private int slot = 0;
	
    public SubGuiNpcAvailability(Availability availabitily)
    {
    	this.availabitily = availabitily;
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
    }

    public void initGui()
    {
        super.initGui();
        addLabel(new GuiNpcLabel(1,"availability.available", guiLeft, guiTop + 4));
        getLabel(1).center(xSize);

        this.addButton(new GuiNpcButton(0, guiLeft + 34, guiTop + 12, 180, 20, "availability.selectdialog"));
        this.addButton(new GuiNpcButton(1, guiLeft + 34, guiTop + 35, 180, 20, "availability.selectquest"));

        
        this.addButton(new GuiNpcButton(20, guiLeft + 4, guiTop + 104, 50, 20, new String[]{"availability.always","availability.is","availability.isnot"},availabitily.factionAvailable.ordinal()));
        this.addButton(new GuiNpcButton(22, guiLeft + 56, guiTop + 104, 60, 20, new String[]{"faction.friendly","faction.neutral","faction.unfriendly"},availabitily.factionStance.ordinal()));
    	this.addButton(new GuiNpcButton(21, guiLeft + 118, guiTop + 104, 110, 20, "availability.selectfaction"));
    	getButton(21).setEnabled(availabitily.factionAvailable != EnumAvailabilityFactionType.Always);
    	getButton(22).setEnabled(availabitily.factionAvailable != EnumAvailabilityFactionType.Always);
    	this.addButton(new GuiNpcButton(23, guiLeft + 230, guiTop + 104,20, 20, "X"));
    	
        this.addButton(new GuiNpcButton(24, guiLeft + 4, guiTop + 126, 50, 20, new String[]{"availability.always","availability.is","availability.isnot"},availabitily.faction2Available.ordinal()));
        this.addButton(new GuiNpcButton(27, guiLeft + 56, guiTop + 126, 60, 20, new String[]{"faction.friendly","faction.neutral","faction.unfriendly"},availabitily.faction2Stance.ordinal()));
    	this.addButton(new GuiNpcButton(25, guiLeft + 118, guiTop + 126, 110, 20, "availability.selectfaction"));
    	getButton(25).setEnabled(availabitily.faction2Available != EnumAvailabilityFactionType.Always);
    	getButton(27).setEnabled(availabitily.faction2Available != EnumAvailabilityFactionType.Always);
    	this.addButton(new GuiNpcButton(26, guiLeft + 230, guiTop + 126,20, 20, "X"));

        addLabel(new GuiNpcLabel(50,"availability.daytime", guiLeft + 4 , guiTop + 153));
    	this.addButton(new GuiNpcButton(50, guiLeft + 50, guiTop + 148,150, 20, new String[]{"availability.wholeday","availability.night","availability.day"},availabitily.daytime.ordinal()));

        addLabel(new GuiNpcLabel(51,"availability.minlevel", guiLeft + 4 , guiTop + 175));
    	this.addTextField(new GuiNpcTextField(51, this, fontRendererObj, guiLeft + 50, guiTop + 170,90, 20, availabitily.minPlayerLevel + ""));
    	this.getTextField(51).numbersOnly = true;
    	this.getTextField(51).setMinMaxDefault(0, Integer.MAX_VALUE, 0);
    	
    	this.addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 192,98, 20, "gui.done"));
    	
    	updateGuiButtons();
    }


    private void updateGuiButtons() {
		if(availabitily.factionId >= 0){
			Client.sendData(EnumPacketServer.FactionGet, availabitily.factionId);
		}
		if(availabitily.faction2Id >= 0){
			Client.sendData(EnumPacketServer.FactionGet, availabitily.faction2Id);
		}
	}

    @Override
	protected void actionPerformed(GuiButton guibutton){
    	GuiNpcButton button = (GuiNpcButton) guibutton;

        if(button.id == 0){
        	setSubGui(new SubGuiNpcAvailabilityDialog(availabitily));
        }
        if(button.id == 1){
        	setSubGui(new SubGuiNpcAvailabilityQuest(availabitily));
        }

        if(button.id == 20){
        	availabitily.setFactionAvailability(button.getValue());
        	if(availabitily.factionAvailable == EnumAvailabilityFactionType.Always)
        		availabitily.factionId = -1;
        	initGui();
        }
        if(button.id == 24){
        	availabitily.setFaction2Availability(button.getValue());
        	if(availabitily.faction2Available == EnumAvailabilityFactionType.Always)
        		availabitily.faction2Id = -1;
        	initGui();
        }
        if(button.id == 21){
        	slot = 1;
        	GuiNPCFactionSelection gui = new GuiNPCFactionSelection(npc, getParent(), availabitily.factionId);
        	gui.listener = this;
        	NoppesUtil.openGUI(player, gui);
        }
        if(button.id == 25){
        	slot = 2;
        	GuiNPCFactionSelection gui = new GuiNPCFactionSelection(npc, getParent(), availabitily.faction2Id);
        	gui.listener = this;
        	NoppesUtil.openGUI(player, gui);
        }
        if(button.id == 22){
        	availabitily.setFactionAvailabilityStance(button.getValue());
        }

        if(button.id == 27){
        	availabitily.setFaction2AvailabilityStance(button.getValue());
        }
        if(button.id == 23){
        	availabitily.factionId = -1;
    		getButton(21).setDisplayText("availability.selectfaction");
        }
        if(button.id == 26){
        	availabitily.faction2Id = -1;
    		getButton(25).setDisplayText("availability.selectfaction");
        }
        if(button.id == 50){
        	availabitily.daytime = EnumDayTime.values()[button.getValue()];
        }
        if(button.id == 66){
    		close();
        }
    }

	@Override
	public void selected(int id, String name) {
		if(slot == 1)
			availabitily.factionId = id;
		if(slot == 2)
			availabitily.faction2Id = id;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if(compound.hasKey("Slot")){
			Faction faction = new Faction();
			faction.readNBT(compound);
			if(availabitily.factionId == faction.id)
				getButton(21).setDisplayText(faction.name);
			if(availabitily.faction2Id == faction.id)
				getButton(25).setDisplayText(faction.name);
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if(textfield.id == 51)
			availabitily.minPlayerLevel = textfield.getInteger();
	}

}
