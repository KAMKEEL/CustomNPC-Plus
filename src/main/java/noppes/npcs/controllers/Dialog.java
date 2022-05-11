package noppes.npcs.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.ICompatibilty;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.interfaces.handler.data.*;

public class Dialog implements ICompatibilty, IDialog {
	public int version = VersionCompatibility.ModRev;
	public int id = -1;
	public String title = "";
	public String text = "";
	public int quest = -1;
	public DialogCategory category;
	public HashMap<Integer,DialogOption> options = new HashMap<Integer,DialogOption>();
	public Availability availability = new Availability();
	public FactionOptions factionOptions = new FactionOptions();
	public String sound;
	public String command = "";
	public PlayerMail mail = new PlayerMail();
	
	public boolean hideNPC = false;
	public boolean showWheel = false;
	public boolean disableEsc = false;
	public boolean darkenScreen = true;
	
	public boolean hasDialogs(EntityPlayer player) {
		for(DialogOption option: options.values())
			if(option != null && option.optionType == EnumOptionType.DialogOption && option.hasDialog() && option.isAvailable(player))
				return true;
		return false;
	}

	public void readNBT(NBTTagCompound compound) {
		id = compound.getInteger("DialogId");
		readNBTPartial(compound);
	}
	public void readNBTPartial(NBTTagCompound compound) {
    	version = compound.getInteger("ModRev");
		VersionCompatibility.CheckAvailabilityCompatibility(this, compound);
		
    	title = compound.getString("DialogTitle");
    	text = compound.getString("DialogText");
    	quest = compound.getInteger("DialogQuest");
    	sound = compound.getString("DialogSound");
		command = compound.getString("DialogCommand");
		mail.readNBT(compound.getCompoundTag("DialogMail"));

		hideNPC = compound.getBoolean("DialogHideNPC");
		if(compound.hasKey("DialogShowWheel"))
			showWheel = compound.getBoolean("DialogShowWheel");
		else
			showWheel = true;
		disableEsc = compound.getBoolean("DialogDisableEsc");

		if(compound.hasKey("DialogDarkScreen")) {
			darkenScreen = compound.getBoolean("DialogDarkScreen");
		}
		else {
			darkenScreen = true;
		}


		NBTTagList options = compound.getTagList("Options", 10);
		HashMap<Integer,DialogOption> newoptions = new HashMap<Integer,DialogOption>();
		for(int iii = 0; iii < options.tagCount();iii++){
            NBTTagCompound option = options.getCompoundTagAt(iii);
            int opslot = option.getInteger("OptionSlot");
            DialogOption dia = new DialogOption();
            dia.readNBT(option.getCompoundTag("Option"));
            newoptions.put(opslot, dia);
		}
		this.options = newoptions;

    	availability.readFromNBT(compound);
    	factionOptions.readFromNBT(compound);
	}


	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
    	compound.setInteger("DialogId", id);
		return writeToNBTPartial(compound);
	}
	
	public NBTTagCompound writeToNBTPartial(NBTTagCompound compound) {
		compound.setString("DialogTitle", title);
		compound.setString("DialogText", text);
		compound.setInteger("DialogQuest", quest);
		compound.setString("DialogCommand", command);
		compound.setTag("DialogMail", mail.writeNBT());
		compound.setBoolean("DialogHideNPC", hideNPC);
		compound.setBoolean("DialogShowWheel", showWheel);
		compound.setBoolean("DialogDisableEsc", disableEsc);

		compound.setBoolean("DialogDarkScreen", darkenScreen);
		
		if(sound != null && !sound.isEmpty())
			compound.setString("DialogSound", sound);

		NBTTagList options = new NBTTagList();
		for(int opslot : this.options.keySet()){
			NBTTagCompound listcompound = new NBTTagCompound();
			listcompound.setInteger("OptionSlot", opslot);
			listcompound.setTag("Option", this.options.get(opslot).writeNBT());
			options.appendTag(listcompound);
		}
		compound.setTag("Options", options);
		
    	availability.writeToNBT(compound);
    	factionOptions.writeToNBT(compound);
		compound.setInteger("ModRev", version);
		return compound;
	}

	public boolean hasQuest() {
		return getQuest() != null;
	}

	public boolean hasOtherOptions() {
		for(DialogOption option: options.values())
			if(option != null && option.optionType != EnumOptionType.Disabled)
				return true;
		return false;
	}
	
	public Dialog copy(EntityPlayer player) {
		Dialog dialog = new Dialog();
		dialog.id = id;
		dialog.text = text;
		dialog.title = title;
		dialog.category = category;
		dialog.quest = quest;
		dialog.sound = sound;
		dialog.mail = mail;
		dialog.command = command;
		dialog.hideNPC = hideNPC;
		dialog.showWheel = showWheel;
		dialog.disableEsc = disableEsc;
		dialog.darkenScreen = darkenScreen;
		
		for(int slot : options.keySet()){
			DialogOption option = options.get(slot);
			if(option.optionType == EnumOptionType.DialogOption && (!option.hasDialog() || !option.isAvailable(player)))
				continue;
			dialog.options.put(slot, option);
		}
		return dialog;
	}
	@Override
	public int getVersion() {
		return version;
	}
	@Override
	public void setVersion(int version) {
		this.version = version;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.title;
	}

	public List<IDialogOption> getOptions() {
		return new ArrayList(this.options.values());
	}

	public IDialogOption getOption(int slot) {
		IDialogOption option = (IDialogOption)this.options.get(slot);
		if (option == null) {
			throw new CustomNPCsException("There is no DialogOption for slot: " + slot, new Object[0]);
		} else {
			return option;
		}
	}

	public IAvailability getAvailability() {
		return this.availability;
	}

	public IDialogCategory getCategory() {
		return this.category;
	}

	public void save() {
		DialogController.instance.saveDialog(this.category.id, this);
	}

	public void setName(String name) {
		this.title = name;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setQuest(IQuest quest) {
		if (quest == null) {
			this.quest = -1;
		} else {
			if (quest.getId() < 0) {
				throw new CustomNPCsException("Quest id is lower than 0", new Object[0]);
			}

			this.quest = quest.getId();
		}

	}

	public Quest getQuest() {
		return QuestController.instance == null ? null : (Quest)QuestController.instance.quests.get(this.quest);
	}

	public String getCommand() {
		return this.command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setDarkenScreen(boolean darkenScreen) {
		this.darkenScreen = darkenScreen;
	}
	public boolean getDarkenScreen() {
		return this.darkenScreen;
	}

	public void setDisableEsc(boolean disableEsc) {
		this.disableEsc = disableEsc;
	}
	public boolean getDisableEsc() {
		return this.disableEsc;
	}

	public void setShowWheel(boolean showWheel) {
		this.showWheel = showWheel;
	}
	public boolean getShowWheel() {
		return this.showWheel;
	}

	public void setHideNPC(boolean hideNPC) {
		this.hideNPC = hideNPC;
	}
	public boolean getHideNPC() {
		return this.hideNPC;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}
	public String getSound() {
		return this.sound;
	}
}
