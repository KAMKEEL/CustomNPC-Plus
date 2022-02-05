package noppes.npcs.controllers;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.ICompatibilty;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.constants.EnumOptionType;

public class Dialog implements ICompatibilty {
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
	public Quest getQuest() {
		return QuestController.instance.quests.get(quest);
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
	
}
