package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ICompatibilty;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.api.handler.data.*;

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

	public int color = 0xe0e0e0;
	public int titleColor = 0xe0e0e0;

	public boolean hideNPC = false;
	public boolean showWheel = false;
	public boolean disableEsc = false;
	public boolean darkenScreen = true;
	public boolean showOptionLine = true;

	public boolean renderGradual = false;
	public boolean showPreviousBlocks = true;

	public String textSound = "minecraft:random.wood_click";
	public float textPitch = 1.0F;

	public int textWidth = 300;
	public int textHeight = 400;

	public int titlePos;
	public int textOffsetX, textOffsetY;
	public int titleOffsetX, titleOffsetY;

	public int optionSpaceX, optionSpaceY;
	public int optionOffsetX, optionOffsetY;

	public float npcScale = 1.0F;
	public int npcOffsetX, npcOffsetY;

	public HashMap<Integer, IDialogImage> dialogImages = new HashMap<>();
	
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

		NBTTagList images = compound.getTagList("Images", 10);
		HashMap<Integer,IDialogImage> newImages = new HashMap<>();
		for(int i = 0; i < images.tagCount(); i++){
			NBTTagCompound imageCompound = images.getCompoundTagAt(i);
			int id = imageCompound.getInteger("ID");
			DialogImage image = new DialogImage(id);
			image.readNBT(imageCompound);
			newImages.put(id, image);
		}
		this.dialogImages = newImages;

		color = compound.getInteger("Color");
		titleColor = compound.getInteger("TitleColor");
		renderGradual = compound.getBoolean("RenderGradual");
		showPreviousBlocks = compound.getBoolean("PreviousBlocks");
		showOptionLine = compound.getBoolean("ShowOptionLine");
		textSound = compound.getString("TextSound");
		textPitch = compound.getFloat("TextPitch");
		textWidth = compound.getInteger("TextWidth");
		textHeight = compound.getInteger("TextHeight");
		textOffsetX = compound.getInteger("TextOffsetX");
		textOffsetY = compound.getInteger("TextOffsetY");
		titlePos = compound.getInteger("TitlePos");
		titleOffsetX = compound.getInteger("TitleOffsetX");
		titleOffsetY = compound.getInteger("TitleOffsetY");
		optionOffsetX = compound.getInteger("OptionOffsetX");
		optionOffsetY = compound.getInteger("OptionOffsetY");
		optionSpaceX = compound.getInteger("OptionSpaceX");
		optionSpaceY = compound.getInteger("OptionSpaceY");
		npcScale = compound.getFloat("NPCScale");
		npcOffsetX = compound.getInteger("NPCOffsetX");
		npcOffsetY = compound.getInteger("NPCOffsetY");

		if (!compound.hasKey("PreviousBlocks"))
			showPreviousBlocks = true;
		if (!compound.hasKey("TextSound"))
			textSound = "minecraft:random.wood_click";
		if (!compound.hasKey("TextPitch"))
			textPitch = 1.0F;
		if (!compound.hasKey("TextWidth"))
			textWidth = 300;
		if (!compound.hasKey("TextHeight"))
			textHeight = 400;
		if (!compound.hasKey("ShowOptionLine"))
			showOptionLine = true;
		if (!compound.hasKey("NPCScale"))
			npcScale = 1.0F;
		if (!compound.hasKey("Color"))
			color = 0xe0e0e0;
		if (!compound.hasKey("TitleColor"))
			titleColor = 0xe0e0e0;

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

		compound.setInteger("Color", color);
		compound.setInteger("TitleColor", titleColor);
		compound.setBoolean("RenderGradual", renderGradual);
		compound.setBoolean("PreviousBlocks", showPreviousBlocks);
		compound.setBoolean("ShowOptionLine", showOptionLine);
		compound.setString("TextSound", textSound);
		compound.setFloat("TextPitch", textPitch);
		compound.setInteger("TextWidth", textWidth);
		compound.setInteger("TextHeight", textHeight);
		compound.setInteger("TextOffsetX", textOffsetX);
		compound.setInteger("TextOffsetY", textOffsetY);
		compound.setInteger("TitlePos", titlePos);
		compound.setInteger("TitleOffsetX", titleOffsetX);
		compound.setInteger("TitleOffsetY", titleOffsetY);
		compound.setInteger("OptionOffsetX", optionOffsetX);
		compound.setInteger("OptionOffsetY", optionOffsetY);
		compound.setInteger("OptionSpaceX", optionSpaceX);
		compound.setInteger("OptionSpaceY", optionSpaceY);
		compound.setFloat("NPCScale", npcScale);
		compound.setInteger("NPCOffsetX", npcOffsetX);
		compound.setInteger("NPCOffsetY", npcOffsetY);

		NBTTagList images = new NBTTagList();
		for (IDialogImage dialogImage : dialogImages.values()) {
			NBTTagCompound imageCompound = ((DialogImage) dialogImage).writeToNBT(new NBTTagCompound());
			images.appendTag(imageCompound);
		}
		compound.setTag("Images",images);

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
		dialog.color = color;
		dialog.titleColor = titleColor;
		dialog.hideNPC = hideNPC;
		dialog.showWheel = showWheel;
		dialog.disableEsc = disableEsc;
		dialog.darkenScreen = darkenScreen;
		dialog.renderGradual = renderGradual;
		dialog.showPreviousBlocks = showPreviousBlocks;
		dialog.showOptionLine = showOptionLine;
		dialog.textSound = textSound;
		dialog.textPitch = textPitch;
		dialog.textWidth = textWidth;
		dialog.textHeight = textHeight;
		dialog.textOffsetX = textOffsetX;
		dialog.textOffsetY = textOffsetY;
		dialog.titlePos = titlePos;
		dialog.titleOffsetX = titleOffsetX;
		dialog.titleOffsetY = titleOffsetY;
		dialog.optionOffsetX = optionOffsetX;
		dialog.optionOffsetY = optionOffsetY;
		dialog.optionSpaceX = optionSpaceX;
		dialog.optionSpaceY = optionSpaceY;
		dialog.npcScale = npcScale;
		dialog.npcOffsetX = npcOffsetX;
		dialog.npcOffsetY = npcOffsetY;
		dialog.dialogImages = dialogImages;
		
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

	public void setColor(int color) {
		this.color = color;
	}
	public int getColor() {
		return this.color;
	}

	public void setTitleColor(int titleColor) {
		this.titleColor = titleColor;
	}
	public int getTitleColor() {
		return titleColor;
	}

	public void renderGradual(boolean gradual) {
		this.renderGradual = gradual;
	}
	public boolean renderGradual() {
		return renderGradual;
	}

	public void showPreviousBlocks(boolean show) {
		this.showPreviousBlocks = show;
	}
	public boolean showPreviousBlocks() {
		return showPreviousBlocks;
	}

	public void showOptionLine(boolean show) {
		this.showOptionLine = show;
	}
	public boolean showOptionLine() {
		return showOptionLine;
	}

	public void setTextSound(String textSound) {
		this.textSound = textSound;
	}
	public String getTextSound() {
		return textSound;
	}

	public void setTextPitch(float textPitch) {
		this.textPitch = textPitch;
	}
	public float getTextPitch() {
		return textPitch;
	}

	public void setTitlePos(int pos) {
		this.titlePos = pos;
	}
	public int getTitlePos() {
		return this.titlePos;
	}

	public void setNPCScale(float scale) {
		this.npcScale = scale;
	}
	public float getNpcScale() {
		return npcScale;
	}

	public void setNpcOffset(int offsetX, int offsetY) {
		this.npcOffsetX = offsetX;
		this.npcOffsetY = offsetY;
	}
	public int getNpcOffsetX() {
		return npcOffsetX;
	}
	public int getNpcOffsetY() {
		return npcOffsetY;
	}

	public void textWidthHeight(int textWidth, int textHeight) {
		this.textWidth = textWidth;
		this.textHeight = textHeight;
	}
	public int getTextWidth() {
		return textWidth;
	}
	public int setTextHeight() {
		return textHeight;
	}

	public void setTextOffset(int offsetX, int offsetY) {
		this.textOffsetX = offsetX;
		this.textOffsetY = offsetY;
	}
	public int getTextOffsetX() {
		return textOffsetX;
	}
	public int getTextOffsetY() {
		return textOffsetY;
	}

	public void setTitleOffset(int offsetX, int offsetY) {
		this.titleOffsetX = offsetX;
		this.titleOffsetY = offsetY;
	}
	public int getTitleOffsetX() {
		return titleOffsetX;
	}
	public int getTitleOffsetY() {
		return titleOffsetY;
	}

	public void setOptionOffset(int offsetX, int offsetY) {
		this.optionOffsetX = offsetX;
		this.optionOffsetY = offsetY;
	}
	public int getOptionOffsetX() {
		return optionOffsetX;
	}
	public int getOptionOffsetY() {
		return optionOffsetY;
	}

	public void setOptionSpacing(int spaceX, int spaceY) {
		this.optionSpaceX = spaceX;
		this.optionSpaceY = spaceY;
	}
	public int getOptionSpaceX() {
		return optionSpaceX;
	}
	public int getOptionSpaceY() {
		return optionSpaceY;
	}

	public void addImage(int id, IDialogImage image) {
		if (dialogImages.size() >= CustomNpcs.DialogImageLimit) {
			return;
		}

		((DialogImage) image).id = id;
		dialogImages.put(id, image);
	}
	public IDialogImage getImage(int id) {
		return dialogImages.get(id);
	}
	public IDialogImage createImage() {
		return new DialogImage();
	}
	public IDialogImage[] getImages() {
		return (new ArrayList<>(dialogImages.values())).toArray(new IDialogImage[0]);
	}
	public boolean hasImage(int id) {
		return dialogImages.containsKey(id);
	}
	public void removeImage(int id) {
		dialogImages.remove(id);
	}
	public void clearImages() {
		dialogImages.clear();
	}
}
