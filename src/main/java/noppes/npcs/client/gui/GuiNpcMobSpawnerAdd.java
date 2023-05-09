package noppes.npcs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.controllers.data.TagMap;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.*;

public class GuiNpcMobSpawnerAdd extends GuiNPCInterface implements GuiYesNoCallback, IGuiData, ISubGuiListener {
	
	private Entity toClone;
	private NBTTagCompound compound;
	private static boolean serverSide = false;
	private static int tab = 1;
	public boolean isNPC = false;

	// Selected Tags to Add
	public static HashSet<String> addTags;
	public static ArrayList<String> allTags = new ArrayList<>();
	public static HashMap<String, UUID> tagMap = new HashMap<>();

	public GuiNpcMobSpawnerAdd(NBTTagCompound compound){
		this.toClone = EntityList.createEntityFromNBT(compound, Minecraft.getMinecraft().theWorld);
		this.compound = compound;
		if(toClone instanceof EntityNPCInterface){
			isNPC = true;
		}

		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		if(addTags == null){
			addTags  = new HashSet<>();
		}
		if(isNPC){
			Client.sendData(EnumPacketServer.CloneAllTagsShort);
		}
	}

	@Override
	public void initGui(){
		super.initGui();
		String name = toClone.getCommandSenderName();
		addLabel(new GuiNpcLabel(0, "Save as", guiLeft + 4, guiTop + 6));
		addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 4, guiTop + 18, 200, 20, name));
		

		addLabel(new GuiNpcLabel(1, "Tab", guiLeft + 10, guiTop + 50));
		addButton(new GuiButtonBiDirectional(2, guiLeft + 40, guiTop + 45, 60, 20, new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"}, tab - 1));

		addButton(new GuiNpcButton(3, guiLeft + 4, guiTop + 95, new String[]{"Client side", "Server side"}, serverSide?1:0));
		addLabel(new GuiNpcLabel(1, "Tab", guiLeft + 10, guiTop + 50));

		addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 70, 80, 20, "gui.save"));
		addButton(new GuiNpcButton(1, guiLeft + 86, guiTop + 70, 80, 20, "gui.cancel"));

		if(isNPC){
			addButton(new GuiNpcButton(4, guiLeft + 4, guiTop + 120, 99, 20, "cloner.wandTags"));
			addButton(new GuiNpcButton(5, guiLeft + 106, guiTop + 120, 99, 20, "cloner.npcTags"));
			if(addTags.size() > 0){
				addLabel(new GuiNpcLabel(6, "cloner.wandtagsapplied", guiLeft + 10, guiTop + 160));
			}
		}
	}

	public void buttonEvent(GuiButton guibutton) {
		int id = guibutton.id;
		if(id == 0){
			String name = getTextField(0).getText();
			if(name.isEmpty())
				return;
			int tab = ((GuiNpcButton)guibutton).getValue() + 1;
			if(!serverSide){
				if(ClientCloneController.Instance.getCloneData(null, name, tab) != null)
					displayGuiScreen(new GuiYesNo(this, "Warning", "You are about to overwrite a clone", 1));
				else
					confirmClicked(true, 0);
			}
			else
				Client.sendData(EnumPacketServer.ClonePreSave, name, tab);
		}
		if(id == 1){
			close();
		}
		if(id == 2){
			tab = ((GuiNpcButton)guibutton).getValue() + 1;
		}
		if(id == 3){
			serverSide = ((GuiNpcButton)guibutton).getValue() == 1;
		}
		if (id == 4) {
			if(isNPC){
				this.setSubGui(new SubGuiClonerQuickTags(this));
			}
		}
		if (id == 5) {
			if(isNPC){
				this.setSubGui(new SubGuiClonerNPCTags((EntityNPCInterface) toClone));
			}
		}
	}


	@Override
    public void confirmClicked(boolean confirm, int id){
		if(confirm){
			String name = getTextField(0).getText();
			NBTTagCompound extraTags = new NBTTagCompound();
			if(isNPC && addTags.size() > 0){
				extraTags = setTempTags();
			}
			if(!serverSide){
				ClientCloneController.Instance.addClone(compound, name, tab, extraTags);
			}
			else{
				Client.sendData(EnumPacketServer.CloneSave, name, tab, extraTags);
			}

			close();
		}
		else
			displayGuiScreen(this);
    }


	@Override
	public void save() {
		
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if(compound.hasKey("NameExists")){
			if(compound.getBoolean("NameExists"))
				displayGuiScreen(new GuiYesNo(this, "Warning", "You are about to overwrite a clone", 1));
			else
				confirmClicked(true, 0);
		}
		else if (compound.hasKey("ShortTags")) {
			NBTTagList validTags = compound.getTagList("ShortTags", 10);
			tagMap.clear();
			allTags.clear();
			if(validTags != null){
				for(int j = 0; j < validTags.tagCount(); j++)
				{
					NBTTagCompound tagStructure = validTags.getCompoundTagAt(j);
					Tag tag = new Tag();
					tag.readShortNBT(tagStructure);
					tagMap.put(tag.name, tag.uuid);
				}
				allTags.addAll(tagMap.keySet());
				allTags.sort(String.CASE_INSENSITIVE_ORDER);
			}
			initGui();
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		initGui();
	}

	public NBTTagCompound setTempTags(){
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		NBTTagList nbtTagList = new NBTTagList();
		for (String name : addTags) {
			if(tagMap.containsKey(name)){
				nbtTagList.appendTag(new NBTTagString(tagMap.get(name).toString()));
			}
			else {
				addTags.remove(name);
			}
		}
		nbtTagCompound.setTag("TempTagUUIDs", nbtTagList);

		return nbtTagCompound;
	}

}
