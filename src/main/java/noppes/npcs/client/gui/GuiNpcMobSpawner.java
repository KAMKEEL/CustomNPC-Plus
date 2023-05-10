package noppes.npcs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.controllers.ClientTagMapController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.controllers.data.TagMap;

import java.lang.reflect.Modifier;
import java.util.*;

public class GuiNpcMobSpawner extends GuiNPCInterface implements IGuiData {
	public static HashMap<UUID, Tag> tags = new HashMap<>();
	public static HashMap<String, UUID> tagNames = new HashMap<>();
	public static HashSet<String> tagFilters = new HashSet<>();
	public static byte displayTags = 0;

	// 0 - Any, 1 - All, 2 - Not Any, 3 - Not All
	public static byte filterCondition = 0;

	public static byte ascending = 0;

	// 0 - By Name, 1 - By Date
	public static byte sortType = 0;

	public TagMap tagMap;

	private final GuiCustomScrollCloner scroll = new GuiCustomScrollCloner(this,0);
	private final GuiCustomScroll filterScroll  = new GuiCustomScroll(this,1);

	private int posX,posY,posZ;

	private List<String> list;
	private List<String> tagList;

	private static int showingClones = 0;

	private static String search = "";
	private static String tagSearch = "";

	private int activeTab =  1;

	public GuiNpcMobSpawner(int i, int j, int k) {
		super();
		xSize = 354;
		posX = i;
		posY = j;
		posZ = k;

		this.closeOnEsc = true;

		setBackground("menubg.png");
		Client.sendData(EnumPacketServer.CloneAllTags, activeTab);
	}
	public void initGui()
	{
		super.initGui();
		guiTop += 10;
		guiLeft += 30;

		scroll.clear();
		scroll.setSize(293, 188);
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 26;
		filterScroll.clear();
		filterScroll.setSize(140, 166);
		filterScroll.guiLeft = guiLeft + 4;
		filterScroll.guiTop = guiTop + 19;
		filterScroll.multipleSelection = true;
		filterScroll.setSelectedList(tagFilters);

		GuiMenuTopButton button;
		addTopButton(button = new GuiMenuTopButton(3,guiLeft + 4, guiTop - 17, "spawner.clones"));
		button.active = showingClones == 0;
		addTopButton(button = new GuiMenuTopButton(5, button, "gui.server"));
		button.active = showingClones == 2;
		addTopButton(button = new GuiMenuTopButton(4, button, "spawner.entities"));
		button.active = showingClones == 1;
		addTopButton(button = new GuiMenuTopButton(16, guiLeft + (xSize - 67), guiTop - 17, "gui.filters"));
		button.active = showingClones == 3;
		addTopButton(button = new GuiMenuTopButton(17, guiLeft + (xSize - 22), guiTop - 17, "X"));

		if (showingClones < 3){
			addScroll(scroll);
			addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 4, guiTop + 4, 293, 20, search));
			addButton(new GuiNpcButton(1, guiLeft + 298, guiTop + 6, 52, 20, "item.monsterPlacer.name"));
			addButton(new GuiNpcButton(2, guiLeft + 298, guiTop + 140, 52, 20, "spawner.mobspawner"));

			if(showingClones == 0 || showingClones == 2){

				addSideButton(new GuiMenuSideButton(21,guiLeft - 70, this.guiTop + 2, 70,22, "1"));
				addSideButton(new GuiMenuSideButton(22,guiLeft - 70, this.guiTop + 23, 70,22, "2"));
				addSideButton(new GuiMenuSideButton(23,guiLeft - 70, this.guiTop + 44, 70,22, "3"));
				addSideButton(new GuiMenuSideButton(24,guiLeft - 70, this.guiTop + 65, 70,22, "4"));
				addSideButton(new GuiMenuSideButton(25,guiLeft - 70, this.guiTop + 86, 70,22, "5"));
				addSideButton(new GuiMenuSideButton(26,guiLeft - 70, this.guiTop + 107, 35,22, "6"));
				addSideButton(new GuiMenuSideButton(27,guiLeft - 35, this.guiTop + 107, 35,22, "7"));
				addSideButton(new GuiMenuSideButton(28,guiLeft - 70, this.guiTop + 128, 35,22, "8"));
				addSideButton(new GuiMenuSideButton(29,guiLeft - 35, this.guiTop + 128, 35,22, "9"));
				addSideButton(new GuiMenuSideButton(30,guiLeft - 70, this.guiTop + 149, 35,22, "10"));
				addSideButton(new GuiMenuSideButton(31,guiLeft - 35, this.guiTop + 149, 35,22, "11"));
				addSideButton(new GuiMenuSideButton(32,guiLeft - 70, this.guiTop + 170, 35,22, "12"));
				addSideButton(new GuiMenuSideButton(33,guiLeft - 35, this.guiTop + 170, 35,22, "13"));
				addSideButton(new GuiMenuSideButton(34,guiLeft - 70, this.guiTop + 191, 35,22, "14"));
				addSideButton(new GuiMenuSideButton(35,guiLeft - 35, this.guiTop + 191, 35,22, "15"));

				addButton(new GuiNpcButton(6, guiLeft + 298, guiTop + 190, 52, 20, "gui.remove"));

				getSideButton(20 + activeTab).active = true;
				showClones();
			}
			else {
				showEntities();
			}
		} else {
			// Show Filters
			addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("cloner.tagFilters"), guiLeft + 7, guiTop + 7));
			addScroll(filterScroll);
			addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 4, guiTop + 190, 140, 20, tagSearch));

			addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("cloner.tagVisibility"), guiLeft + 150, guiTop + 27));
			addButton(new GuiNpcButton(12, guiLeft + 215, guiTop + 20, new String[]{"display.show", "display.all", "display.hide"}, displayTags));

			addLabel(new GuiNpcLabel(3, StatCollector.translateToLocal("filter.contains"), guiLeft + 150, guiTop + 50));
			addButton(new GuiNpcButton(13, guiLeft + 215, guiTop + 43, new String[]{"filter.any", "filter.all", "filter.notany", "filter.notall"}, filterCondition));

			addButton(new GuiNpcButton(11, guiLeft + 150, guiTop + 66, 130, 20, "gui.deselectAll"));


			addLabel(new GuiNpcLabel(4, StatCollector.translateToLocal("cloner.order"), guiLeft + 150, guiTop + 112));
			addButton(new GuiNpcButton(14, guiLeft + 215, guiTop + 105, new String[]{"cloner.ascending", "cloner.descending"}, ascending));

			addLabel(new GuiNpcLabel(5, StatCollector.translateToLocal("cloner.type"), guiLeft + 150, guiTop + 135));
			addButton(new GuiNpcButton(15, guiLeft + 215, guiTop + 128, new String[]{"cloner.name", "cloner.date"}, sortType));


			getButton(12).width = 65;
			getButton(12).height = 20;

			getButton(13).width = 65;
			getButton(13).height = 20;

			getButton(14).width = 65;
			getButton(14).height = 20;

			getButton(15).width = 65;
			getButton(15).height = 20;

			showFilters();
		}
	}

	public int getShowingClones() {return showingClones; }

	public void showFilters(){
		filterScroll.setList(getTagList());
	}

	private void showEntities() {
		Map<?,?> data = EntityList.stringToClassMapping;
		ArrayList<String> list = new ArrayList<String>();
		for(Object name : data.keySet()){
			Class<?> c = (Class<?>) data.get(name);
			try {
				if(EntityLiving.class.isAssignableFrom(c) && c.getConstructor(new Class[] {World.class}) != null && !Modifier.isAbstract(c.getModifiers()))
					list.add(name.toString());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
			}
		}
		this.list = list;
		scroll.setList(getSearchList(), ascending == 0, sortType == 0);
	}
	private void showClones() {
		if(showingClones == 2){
			Client.sendData(EnumPacketServer.CloneTagList, activeTab);
			Client.sendData(EnumPacketServer.CloneList, activeTab);
			return;
		}

		if(sortType == 0){
			this.list = ClientCloneController.Instance.getClones(activeTab);
		}
		else {
			this.list = ClientCloneController.Instance.getClonesDate(activeTab);
		}

		this.tagMap = ClientTagMapController.Instance.getTagMap(activeTab);
		scroll.setList(getSearchList(), ascending == 0, sortType == 0);
	}
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		if(getTextField(1) != null){
			if(search.equals(getTextField(1).getText()))
				return;
			search = getTextField(1).getText().toLowerCase();
			scroll.setList(getSearchList(), ascending == 0, sortType == 0);
		}
		if(getTextField(2) != null){
			if(tagSearch.equals(getTextField(2).getText()))
				return;
			tagSearch = getTextField(2).getText().toLowerCase();
			filterScroll.setList(getTagList());
		}

	}

	private List<String> getSearchList(){
		if(this.list == null){
			this.list = new ArrayList<String>();
		}

		// No Filters - or - Entities
		if(tagFilters.size() == 0 || showingClones == 1){
			// No Search
			if(search.isEmpty())
				return new ArrayList<String>(this.list);
			// Yes Search
			else {
				List<String> list = new ArrayList<String>();
				for(String name : this.list){
					if(name.toLowerCase().contains(search))
						list.add(name);
				}
				return list;
			}
		}

		////////////////////////
		//// WITH FILTERS

		// No Search
		if(search.isEmpty()){
			List<String> list = new ArrayList<String>();
			for(String name : this.list){
				if(tagMap.hasClone(name)){
					if(meetsCondition(name)){
						list.add(name);
					}
				}
			}
			return list;
		}

		// Yes Search
		List<String> list = new ArrayList<String>();
		for(String name : this.list){
			if(name.toLowerCase().contains(search)) {
				if(tagMap.hasClone(name)){
					if(meetsCondition(name)){
						list.add(name);
					}
				}
			}
		}
		return list;
	}

	private boolean meetsCondition(String name){
		if(tagMap == null){
			return true;
		}
		boolean conditionMet = true;
		boolean allRequirement = filterCondition == 1 || filterCondition == 3;

		for(String tagName : tagFilters){
			UUID tagUUID = tagNames.get(tagName);
			if(tagUUID != null) {
				boolean hasTag = tagMap.hasTag(name, tagUUID);
				if (!allRequirement) {
					if (!hasTag && filterCondition == 0) {
						conditionMet = false;
						break;
					} else if (hasTag && filterCondition == 2) {
						conditionMet = false;
						break;
					}
				} else {
					if (!hasTag && filterCondition == 1) {
						conditionMet = false;
						break;
					} else if (hasTag && filterCondition == 3) {
						conditionMet = false;
						break;
					}
				}
			}
		}

		return conditionMet;
	}

	private List<String> getTagList(){
		if(this.tagList == null){
			this.tagList = new ArrayList<String>();
		}
		// No Search // No Filters - or - Entities
		if(tagSearch.isEmpty())
			return new ArrayList<String>(tagNames.keySet());
			// Yes Search // No Filters - or - Entities

		List<String> list = new ArrayList<String>();
		for(String name : this.tagList){
			if(name.toLowerCase().contains(tagSearch))
				list.add(name);
		}
		return list;
	}

	private NBTTagCompound getCompound(){
		String sel = scroll.getSelected();
		if(sel == null)
			return null;

		if(showingClones == 0){
			return ClientCloneController.Instance.getCloneData(player, sel, activeTab);
		}
		else{
			Entity entity = EntityList.createEntityByName(sel, Minecraft.getMinecraft().theWorld);
			if(entity == null)
				return null;
			NBTTagCompound compound = new NBTTagCompound();
			entity.writeToNBTOptional(compound);
			return compound;
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton){
		int id = guibutton.id;
		if(id == 0){
			close();
		}
		if(id == 1){
			if(showingClones == 2){
				String sel = scroll.getSelected();
				if(sel == null)
					return;
				Client.sendData(EnumPacketServer.SpawnMob, true, posX, posY, posZ, sel, activeTab);
				close();
			}
			else{
				NBTTagCompound compound = getCompound();
				if(compound == null)
					return;
				Client.sendData(EnumPacketServer.SpawnMob, false, posX, posY, posZ, compound);
				close();
			}
		}
		if(id == 2){
			if(showingClones == 2){
				String sel = scroll.getSelected();
				if(sel == null)
					return;
				Client.sendData(EnumPacketServer.MobSpawner, true, posX, posY, posZ, sel, activeTab);
				close();
			}
			else{
				NBTTagCompound compound = getCompound();
				if(compound == null)
					return;
				Client.sendData(EnumPacketServer.MobSpawner, false, posX, posY, posZ, compound);
				close();

			}
		}
		if(id == 3){
			showingClones = 0;
			initGui();
		}
		if(id == 4){
			showingClones = 1;
			initGui();
		}
		if(id == 5){
			showingClones = 2;
			initGui();
		}
		if(id == 16){
			showingClones = 3;
			initGui();
		}
		if(id == 17){
			close();
		}
		if(id == 6){
			if(scroll.getSelected() != null){
				if(showingClones == 2){
					Client.sendData(EnumPacketServer.CloneRemove, activeTab, scroll.getSelected());
					return;
				}
				ClientCloneController.Instance.removeClone(scroll.getSelected(), activeTab);
				scroll.selected = -1;
				initGui();
			}
		}
		if (id == 11) {
			tagFilters = new HashSet<>();
			filterScroll.setSelectedList(tagFilters);
			initGui();
		}
		if (id == 12) {
			GuiNpcButton button = (GuiNpcButton) guibutton;
			displayTags = (byte) button.getValue();
		}
		if (id == 13) {
			GuiNpcButton button = (GuiNpcButton) guibutton;
			filterCondition = (byte) button.getValue();
		}
		if (id == 14) {
			GuiNpcButton button = (GuiNpcButton) guibutton;
			ascending = (byte) button.getValue();
		}
		if (id == 15) {
			GuiNpcButton button = (GuiNpcButton) guibutton;
			sortType = (byte) button.getValue();
		}
		if(id > 20){
			activeTab = id - 20;
			initGui();
		}
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("CloneTags")) {
			this.tagMap = new TagMap(activeTab);
			NBTTagCompound cloneTags = compound.getCompoundTag("CloneTags");
			this.tagMap.readNBT(cloneTags);
		}
		else if (compound.hasKey("AllTags")) {
			NBTTagList validTags = compound.getTagList("AllTags", 10);
			if(validTags != null){
				HashMap<UUID, Tag> tagsUpdate = new HashMap<>();
				HashMap<String, UUID> tagNamesUpdate = new HashMap<>();
				for(int j = 0; j < validTags.tagCount(); j++)
				{
					NBTTagCompound tagStructure = validTags.getCompoundTagAt(j);
					Tag tag = new Tag();
					tag.readNBT(tagStructure);
					tagsUpdate.put(tag.uuid, tag);
					tagNamesUpdate.put(tag.name, tag.uuid);
				}
				tags = tagsUpdate;
				tagNames = tagNamesUpdate;
				this.tagList = new ArrayList<String>(tagNames.keySet());
				filterScroll.setList(getTagList());
			}
		}
		else {
			List<String> list = new ArrayList<String>();
			if(sortType == 1){
				NBTTagList nbtlist = compound.getTagList("ListDate", 8);
				for(int i = 0; i < nbtlist.tagCount(); i++){
					list.add(nbtlist.getStringTagAt(i));
				}
			}
			else {
				NBTTagList nbtlist = compound.getTagList("List", 8);
				for(int i = 0; i < nbtlist.tagCount(); i++){
					list.add(nbtlist.getStringTagAt(i));
				}
			}

			this.list = list;
			scroll.setList(getSearchList(), ascending == 0, sortType == 0);
		}
	}

}