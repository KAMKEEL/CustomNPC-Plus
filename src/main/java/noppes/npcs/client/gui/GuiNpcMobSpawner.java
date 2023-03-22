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

public class GuiNpcMobSpawner extends GuiNPCInterface implements IScrollData,IGuiData {
	public static HashMap<UUID, String> allTags = new HashMap<>();
	public static HashSet<UUID> filter = new HashSet<>();
	public static boolean showHidden = false;
	public TagMap tagMap;

	private final GuiCustomScrollCloner scroll = new GuiCustomScrollCloner(this,0);
	private final GuiCustomScroll filterScroll  = new GuiCustomScroll(this,1);

	private int posX,posY,posZ;

	private List<String> list;

	private static int showingClones = 0;

	private static String search = "";

	private int activeTab =  1;

	public GuiNpcMobSpawner(int i, int j, int k) {
		super();
		xSize = 354;
		posX = i;
		posY = j;
		posZ = k;

		this.closeOnEsc = true;

		setBackground("menubg.png");
		Client.sendData(EnumPacketServer.TagsGet);
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
		filterScroll.setSize(140, 188);
		filterScroll.guiLeft = guiLeft + 4;
		filterScroll.guiTop = guiTop + 19;
		filterScroll.setList(new ArrayList<>(allTags.values()));

//		HashSet<String> set = new HashSet<String>();
//		for(String s : data.keySet()){
//			if(!s.equals(faction.name) && faction.attackFactions.contains(data.get(s)))
//				set.add(s);
//		}

		filterScroll.multipleSelection = true;
		filterScroll.setSelectedList(new HashSet());

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
			addButton(new GuiNpcButton(2, guiLeft + 298, guiTop + 100, 52, 20, "spawner.mobspawner"));

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
			addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("menu.tags"), guiLeft + 7, guiTop + 7));
			addScroll(filterScroll);

			addButton(new GuiNpcButton(10, guiLeft + 150, guiTop + 20, 120, 20, "gui.selectAll"));
			addButton(new GuiNpcButton(11, guiLeft + 150, guiTop + 43, 120, 20, "gui.deselectAll"));
			addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("tags.taglessEntries") + ":", guiLeft + 150, guiTop + 72));
			addButton(new GuiNpcButton(12, guiLeft + 240, guiTop + 65, new String[]{"display.show", "display.hide"}, showHidden ? 0 : 1));
			getButton(12).width = 60;
			getButton(12).height = 20;
		}
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
		scroll.setList(getSearchList());
	}
	private void showClones() {
		Client.sendData(EnumPacketServer.CloneTagList, activeTab);
		if(showingClones == 2){
			Client.sendData(EnumPacketServer.CloneList, activeTab);
			return;
		}
		ArrayList<String> list = new ArrayList<String>();
		this.list = ClientCloneController.Instance.getClones(activeTab);
		this.tagMap = ClientTagMapController.Instance.getTagMap(activeTab);
		scroll.setList(getSearchList());
	}
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		if(getTextField(1) != null){
			if(search.equals(getTextField(1).getText()))
				return;
			search = getTextField(1).getText().toLowerCase();
		}
		scroll.setList(getSearchList());
	}

	private List<String> getSearchList(){
		// No Search // No Filters
 		if(search.isEmpty() && filter.size() == 0)
			return new ArrayList<String>(list);
		// Yes Search // No Filters
		else if(!search.isEmpty() && filter.size() == 0){
			List<String> list = new ArrayList<String>();
			for(String name : this.list){
				if(name.toLowerCase().contains(search))
					list.add(name);
			}
			return list;
		}
		// No Search // Yes Filters
		else if(search.isEmpty() && filter.size() > 0){
			List<String> list = new ArrayList<String>();
			for(String name : this.list){
				if(name.toLowerCase().contains(search))
					list.add(name);
			}
		}
		// Yes Search // Yes Filters
		List<String> list = new ArrayList<String>();
		for(String name : this.list){
			if(name.toLowerCase().contains(search)){
				list.add(name);
			}
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
		if (id == 10) {
			HashSet<String> hashSet = new HashSet<>(allTags.values());
			filterScroll.setSelectedList(hashSet);
		}
		if (id == 11) {
			filterScroll.setSelectedList(new HashSet<>());
		}
		if (id == 12) {
			showHidden = !showHidden;
		}
		if(id > 20){
			activeTab = id - 20;
			initGui();
		}
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data)
	{
		System.out.println("------- LIST");
		System.out.println(list);
		System.out.println("------- DATA");
		System.out.println(data);
		for(String s : list){
			allTags.put(UUID.randomUUID(), s);
		}
	}

	@Override
	public void setSelected(String selected) {
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub

	}
	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("CloneTags")) {
			tagMap = new TagMap(activeTab);
			NBTTagCompound cloneTags = compound.getCompoundTag("CloneTags");
			tagMap.readNBT(cloneTags);
		} else {
			NBTTagList nbtlist = compound.getTagList("List", 8);
			List<String> list = new ArrayList<String>();
			for(int i = 0; i < nbtlist.tagCount(); i++){
				list.add(nbtlist.getStringTagAt(i));
			}
			this.list = list;
			scroll.setList(getSearchList());
		}
	}

}