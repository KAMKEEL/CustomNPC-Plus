package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.clone.CloneAllTagsPacket;
import kamkeel.npcs.network.packets.request.clone.CloneFolderListPacket;
import kamkeel.npcs.network.packets.request.clone.CloneListPacket;
import kamkeel.npcs.network.packets.request.clone.CloneRemovePacket;
import kamkeel.npcs.network.packets.request.clone.CloneTagListPacket;
import kamkeel.npcs.network.packets.request.clone.MobSpawnerPacket;
import kamkeel.npcs.network.packets.request.clone.SpawnMobPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.controllers.ClientTagMapController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiCustomScrollCloner;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IClonerGui;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.controllers.data.TagMap;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiNpcMobSpawner extends GuiNPCInterface implements IGuiData, ICustomScrollListener, IClonerGui {

    // ==================== STATIC STATE (persists across GUI opens, shared with fullscreen) ====================
    public static boolean isFullscreen = false;
    public static int activeTab = 1;
    public static String activeFolder = null;
    public static int showingClones = 0; // 0=Clones, 1=Entities, 2=Server, 3=Filters
    public static String search = "";
    public static String tagSearch = "";

    public static HashMap<UUID, Tag> tags = new HashMap<>();
    public static HashMap<String, UUID> tagNames = new HashMap<>();
    public static HashSet<String> tagFilters = new HashSet<>();
    public static byte displayTags = 0;
    // 0 - Any, 1 - All, 2 - Not Any, 3 - Not All
    public static byte filterCondition = 0;
    public static byte ascending = 0;
    // 0 - By Name, 1 - By Date
    public static byte sortType = 0;

    // Scroll position preservation (shared with fullscreen)
    public static int savedContentScrollY = 0;
    public static int savedNavScrollY = 0;

    // ==================== INSTANCE STATE ====================
    public TagMap tagMap;
    private final GuiCustomScrollCloner scroll = new GuiCustomScrollCloner(this, 0);
    private final GuiCustomScroll filterScroll = new GuiCustomScroll(this, 1);

    public int posX, posY, posZ;
    private List<String> list;
    private List<String> rawList;
    private List<String> tagList;

    // ==================== CONSTRUCTOR ====================
    public GuiNpcMobSpawner(int i, int j, int k) {
        super();
        posX = i;
        posY = j;
        posZ = k;
        closeOnEsc = true;
        xSize = 354;
        setBackground("menubg.png");
        PacketClient.sendClient(new CloneAllTagsPacket());
        if (showingClones == 2) {
            PacketClient.sendClient(new CloneFolderListPacket());
        }

        // Minimized view doesn't support folders — ensure we're on a tab
        if (activeFolder != null) {
            if (activeTab < 1 || activeTab > 15) {
                activeTab = 1;
            }
            activeFolder = null;
        }
    }

    // ==================== IClonerGui ====================
    @Override
    public int getShowingClones() {
        return showingClones;
    }

    @Override
    public HashMap<UUID, Tag> getTags() {
        return tags;
    }

    @Override
    public TagMap getTagMap() {
        return tagMap;
    }

    // ==================== INIT GUI ====================
    public void initGui() {
        // Save scroll position before clear
        savedContentScrollY = scroll.scrollY;

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

        // Top buttons — build right-to-left to avoid overlap
        GuiMenuTopButton closeBtn = new GuiMenuTopButton(17, guiLeft + (xSize - 22), guiTop - 17, "X");
        addTopButton(closeBtn);

        // Position "+" dynamically to the left of "X"
        int plusX = closeBtn.xPosition - (fontRendererObj.getStringWidth("+") + 12) - 2;
        GuiMenuTopButton fullscreenBtn = new GuiMenuTopButton(61, plusX, guiTop - 17, "+");
        addTopButton(fullscreenBtn);

        // Position "Filters" dynamically to the left of "+"
        int filterBtnW = fontRendererObj.getStringWidth(StatCollector.translateToLocal("gui.filters")) + 12;
        int filterX = fullscreenBtn.xPosition - filterBtnW - 2;
        GuiMenuTopButton filterBtn = new GuiMenuTopButton(16, filterX, guiTop - 17, "gui.filters");
        filterBtn.active = showingClones == 3;
        addTopButton(filterBtn);

        // Mode buttons from left
        GuiMenuTopButton btn;
        addTopButton(btn = new GuiMenuTopButton(3, guiLeft + 4, guiTop - 17, "spawner.clones"));
        btn.active = showingClones == 0;
        addTopButton(btn = new GuiMenuTopButton(5, btn, "gui.server"));
        btn.active = showingClones == 2;
        addTopButton(btn = new GuiMenuTopButton(4, btn, "spawner.entities"));
        btn.active = showingClones == 1;

        if (showingClones < 3) {
            addScroll(scroll);
            addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 4, guiTop + 4, 293, 20, search));

            addButton(new GuiNpcButton(1, guiLeft + 298, guiTop + 6, 52, 20, "item.monsterPlacer.name"));
            addButton(new GuiNpcButton(2, guiLeft + 298, guiTop + 140, 52, 20, "spawner.mobspawner"));

            if (showingClones == 0 || showingClones == 2) {
                addButton(new GuiNpcButton(6, guiLeft + 298, guiTop + 190, 52, 20, "gui.remove"));

                initTabSideButtons();
                showClones();

                // Restore scroll position
                scroll.scrollY = Math.min(savedContentScrollY, Math.max(0, scroll.maxScrollY));
            } else {
                showEntities();
                scroll.scrollY = Math.min(savedContentScrollY, Math.max(0, scroll.maxScrollY));
            }
        } else {
            showFiltersPage();
        }
    }

    private void initTabSideButtons() {
        // Tabs 1-5: full width (70px)
        addSideButton(new GuiMenuSideButton(21, guiLeft - 70, guiTop + 2, 70, 22, "1"));
        addSideButton(new GuiMenuSideButton(22, guiLeft - 70, guiTop + 23, 70, 22, "2"));
        addSideButton(new GuiMenuSideButton(23, guiLeft - 70, guiTop + 44, 70, 22, "3"));
        addSideButton(new GuiMenuSideButton(24, guiLeft - 70, guiTop + 65, 70, 22, "4"));
        addSideButton(new GuiMenuSideButton(25, guiLeft - 70, guiTop + 86, 70, 22, "5"));
        // Tabs 6-15: half width (35px), 2-column grid
        addSideButton(new GuiMenuSideButton(26, guiLeft - 70, guiTop + 107, 35, 22, "6"));
        addSideButton(new GuiMenuSideButton(27, guiLeft - 35, guiTop + 107, 35, 22, "7"));
        addSideButton(new GuiMenuSideButton(28, guiLeft - 70, guiTop + 128, 35, 22, "8"));
        addSideButton(new GuiMenuSideButton(29, guiLeft - 35, guiTop + 128, 35, 22, "9"));
        addSideButton(new GuiMenuSideButton(30, guiLeft - 70, guiTop + 149, 35, 22, "10"));
        addSideButton(new GuiMenuSideButton(31, guiLeft - 35, guiTop + 149, 35, 22, "11"));
        addSideButton(new GuiMenuSideButton(32, guiLeft - 70, guiTop + 170, 35, 22, "12"));
        addSideButton(new GuiMenuSideButton(33, guiLeft - 35, guiTop + 170, 35, 22, "13"));
        addSideButton(new GuiMenuSideButton(34, guiLeft - 70, guiTop + 191, 35, 22, "14"));
        addSideButton(new GuiMenuSideButton(35, guiLeft - 35, guiTop + 191, 35, 22, "15"));

        if (activeTab >= 1 && activeTab <= 15) {
            GuiMenuSideButton active = getSideButton(20 + activeTab);
            if (active != null) active.active = true;
        }
    }

    // ==================== FILTERS PAGE ====================
    private void showFiltersPage() {
        int baseY = guiTop;

        addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("cloner.tagFilters"),
            guiLeft + 7, baseY + 7));

        filterScroll.clear();
        filterScroll.setSize(140, 166);
        filterScroll.guiLeft = guiLeft + 4;
        filterScroll.guiTop = baseY + 19;
        filterScroll.multipleSelection = true;
        filterScroll.setSelectedList(tagFilters);
        addScroll(filterScroll);

        addTextField(new GuiNpcTextField(2, this, fontRendererObj,
            guiLeft + 4, baseY + 190, 140, 20, tagSearch));

        addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("cloner.tagVisibility"),
            guiLeft + 150, baseY + 27));
        addButton(new GuiNpcButton(12, guiLeft + 215, baseY + 20,
            new String[]{"display.show", "display.all", "display.hide"}, displayTags));

        addLabel(new GuiNpcLabel(3, StatCollector.translateToLocal("filter.contains"),
            guiLeft + 150, baseY + 50));
        addButton(new GuiNpcButton(13, guiLeft + 215, baseY + 43,
            new String[]{"filter.any", "filter.all", "filter.notany", "filter.notall"}, filterCondition));

        addButton(new GuiNpcButton(11, guiLeft + 150, baseY + 66, 130, 20, "gui.deselectAll"));

        addLabel(new GuiNpcLabel(4, StatCollector.translateToLocal("cloner.order"),
            guiLeft + 150, baseY + 112));
        addButton(new GuiNpcButton(14, guiLeft + 215, baseY + 105,
            new String[]{"cloner.ascending", "cloner.descending"}, ascending));

        addLabel(new GuiNpcLabel(5, StatCollector.translateToLocal("cloner.type"),
            guiLeft + 150, baseY + 135));
        addButton(new GuiNpcButton(15, guiLeft + 215, baseY + 128,
            new String[]{"cloner.name", "cloner.date"}, sortType));

        getButton(12).width = 65;
        getButton(12).height = 20;
        getButton(13).width = 65;
        getButton(13).height = 20;
        getButton(14).width = 65;
        getButton(14).height = 20;
        getButton(15).width = 65;
        getButton(15).height = 20;

        filterScroll.setList(getTagList());
    }

    // ==================== CLONE / ENTITY DISPLAY ====================
    private void showClones() {
        if (showingClones == 2) {
            PacketClient.sendClient(new CloneTagListPacket(activeTab));
            PacketClient.sendClient(new CloneListPacket(activeTab));
            return;
        }

        if (sortType == 0) {
            this.list = ClientCloneController.Instance.getClones(activeTab);
        } else {
            this.list = ClientCloneController.Instance.getClonesDate(activeTab);
        }

        this.tagMap = ClientTagMapController.Instance.getTagMap(activeTab);
        populateRawListWithTags();
        scroll.setList(getSearchList(), ascending == 0, sortType == 0);
    }

    private void showEntities() {
        Map<?, ?> data = EntityList.stringToClassMapping;
        ArrayList<String> list = new ArrayList<String>();
        for (Object name : data.keySet()) {
            Class<?> c = (Class<?>) data.get(name);
            try {
                if (EntityLiving.class.isAssignableFrom(c)
                    && c.getConstructor(new Class[]{World.class}) != null
                    && !Modifier.isAbstract(c.getModifiers()))
                    list.add(name.toString());
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
            }
        }
        this.list = list;
        this.rawList = new ArrayList<String>(this.list);
        scroll.setList(getSearchList(), ascending == 0, sortType == 0);
    }

    private void populateRawListWithTags() {
        this.rawList = new ArrayList<>(this.list);
        if (this.tagMap != null) {
            if (displayTags == 0 || displayTags == 1) {
                for (int i = 0; i < this.list.size(); i++) {
                    StringBuilder npcName = new StringBuilder(list.get(i));
                    if (this.tagMap.hasClone(list.get(i))) {
                        for (UUID tagUUID : this.tagMap.getUUIDsList(list.get(i))) {
                            Tag tag = tags.get(tagUUID);
                            if (tag != null) {
                                if (displayTags == 1 || !tag.getIsHidden()) {
                                    npcName.append(" [" + tag.name + "]");
                                }
                            }
                        }
                    }
                    this.rawList.set(i, npcName.toString());
                }
            }
        }
    }

    private NBTTagCompound getCompound() {
        String sel = scroll.getSelected();
        if (sel == null)
            return null;

        if (showingClones == 0) {
            return ClientCloneController.Instance.getCloneData(player, sel, activeTab);
        } else {
            Entity entity = EntityList.createEntityByName(sel, Minecraft.getMinecraft().theWorld);
            if (entity == null)
                return null;
            NBTTagCompound compound = new NBTTagCompound();
            entity.writeToNBTOptional(compound);
            return compound;
        }
    }

    // ==================== SEARCH / FILTER ====================
    private List<String> getSearchList() {
        if (this.list == null) {
            this.list = new ArrayList<String>();
        }
        if (this.rawList == null) {
            this.rawList = new ArrayList<String>();
        }

        // No Filters - or - Entities
        if (tagFilters.size() == 0 || showingClones == 1) {
            if (search.isEmpty())
                return new ArrayList<String>(this.list);
            else {
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < this.list.size(); i++) {
                    if (this.rawList.get(i).toLowerCase().contains(search)) {
                        list.add(this.list.get(i));
                    }
                }
                return list;
            }
        }

        // With Filters
        if (search.isEmpty()) {
            List<String> list = new ArrayList<String>();
            for (String name : this.list) {
                if (tagMap.hasClone(name)) {
                    if (meetsCondition(name)) {
                        list.add(name);
                    }
                }
            }
            return list;
        }

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < this.list.size(); i++) {
            if (this.rawList.get(i).toLowerCase().contains(search)) {
                String npcName = this.list.get(i);
                if (tagMap.hasClone(npcName)) {
                    if (meetsCondition(npcName)) {
                        list.add(npcName);
                    }
                }
            }
        }
        return list;
    }

    private boolean meetsCondition(String name) {
        if (tagMap == null) {
            return true;
        }
        boolean conditionMet = true;
        boolean allRequirement = filterCondition == 1 || filterCondition == 3;

        for (String tagName : tagFilters) {
            UUID tagUUID = tagNames.get(tagName);
            if (tagUUID != null) {
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

    private List<String> getTagList() {
        if (this.tagList == null) {
            this.tagList = new ArrayList<String>();
        }
        if (tagSearch.isEmpty())
            return new ArrayList<String>(tagNames.keySet());

        List<String> list = new ArrayList<String>();
        for (String name : this.tagList) {
            if (name.toLowerCase().contains(tagSearch))
                list.add(name);
        }
        return list;
    }

    // ==================== MOUSE / KEYBOARD EVENTS ====================
    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
    }

    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);

        GuiNpcTextField searchField = getTextField(1);
        if (searchField != null) {
            String newText = searchField.getText().toLowerCase();
            if (!search.equals(newText)) {
                search = newText;
                scroll.setList(getSearchList(), ascending == 0, sortType == 0);
            }
        }

        GuiNpcTextField tagSearchField = getTextField(2);
        if (tagSearchField != null) {
            String newText = tagSearchField.getText().toLowerCase();
            if (!tagSearch.equals(newText)) {
                tagSearch = newText;
                filterScroll.setList(getTagList());
            }
        }
    }

    // ==================== BUTTON ACTIONS ====================
    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;

        // Tab side buttons (IDs 21-35)
        if (id >= 21 && id <= 35) {
            activeTab = id - 20;
            activeFolder = null;
            initGui();
            return;
        }

        // Mode buttons
        if (id == 3) {
            savedContentScrollY = 0;
            showingClones = 0;
            initGui();
        }
        if (id == 4) {
            savedContentScrollY = 0;
            showingClones = 1;
            initGui();
        }
        if (id == 5) {
            savedContentScrollY = 0;
            showingClones = 2;
            PacketClient.sendClient(new CloneFolderListPacket());
            initGui();
        }
        if (id == 16) {
            savedContentScrollY = 0;
            showingClones = 3;
            initGui();
        }
        if (id == 17) {
            close();
        }

        // Open fullscreen GUI
        if (id == 61) {
            isFullscreen = true;
            Minecraft.getMinecraft().displayGuiScreen(
                new GuiNpcMobSpawnerFullscreen(posX, posY, posZ));
        }

        // Spawn
        if (id == 1) {
            if (showingClones == 2) {
                String sel = scroll.getSelected();
                if (sel == null) return;
                SpawnMobPacket.Server(posX, posY, posZ, sel, activeTab);
                close();
            } else {
                NBTTagCompound compound = getCompound();
                if (compound == null) return;
                SpawnMobPacket.Client(posX, posY, posZ, compound);
                close();
            }
        }

        // Mob Spawner
        if (id == 2) {
            if (showingClones == 2) {
                String sel = scroll.getSelected();
                if (sel == null) return;
                MobSpawnerPacket.Server(posX, posY, posZ, sel, activeTab);
                close();
            } else {
                NBTTagCompound compound = getCompound();
                if (compound == null) return;
                MobSpawnerPacket.Client(posX, posY, posZ, compound);
                close();
            }
        }

        // Remove
        if (id == 6) {
            if (scroll.getSelected() != null) {
                int prevSelected = scroll.selected;
                if (showingClones == 2) {
                    PacketClient.sendClient(new CloneRemovePacket(activeTab, scroll.getSelected()));
                    return;
                }
                ClientCloneController.Instance.removeClone(scroll.getSelected(), activeTab);
                initGui();
                if (scroll.list != null && !scroll.list.isEmpty()) {
                    scroll.selected = Math.min(prevSelected, scroll.list.size() - 1);
                } else {
                    scroll.selected = -1;
                }
            }
        }

        // Filter controls
        if (id == 11) {
            tagFilters = new HashSet<>();
            filterScroll.setSelectedList(tagFilters);
            initGui();
        }
        if (id == 12) {
            displayTags = (byte) ((GuiNpcButton) guibutton).getValue();
        }
        if (id == 13) {
            filterCondition = (byte) ((GuiNpcButton) guibutton).getValue();
        }
        if (id == 14) {
            ascending = (byte) ((GuiNpcButton) guibutton).getValue();
        }
        if (id == 15) {
            sortType = (byte) ((GuiNpcButton) guibutton).getValue();
        }
    }

    // ==================== DATA FROM SERVER ====================
    @Override
    public void save() {
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("CloneFolders")) {
            // Minimized doesn't use folders, but still handle the packet
            initGui();
        } else if (compound.hasKey("CloneTags")) {
            this.tagMap = new TagMap(activeTab);
            NBTTagCompound cloneTags = compound.getCompoundTag("CloneTags");
            this.tagMap.readNBT(cloneTags);
        } else if (compound.hasKey("AllTags")) {
            NBTTagList validTags = compound.getTagList("AllTags", 10);
            if (validTags != null) {
                HashMap<UUID, Tag> tagsUpdate = new HashMap<>();
                HashMap<String, UUID> tagNamesUpdate = new HashMap<>();
                for (int j = 0; j < validTags.tagCount(); j++) {
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
        } else {
            List<String> list = new ArrayList<String>();
            if (sortType == 1) {
                NBTTagList nbtlist = compound.getTagList("ListDate", 8);
                for (int i = 0; i < nbtlist.tagCount(); i++) {
                    list.add(nbtlist.getStringTagAt(i));
                }
            } else {
                NBTTagList nbtlist = compound.getTagList("List", 8);
                for (int i = 0; i < nbtlist.tagCount(); i++) {
                    list.add(nbtlist.getStringTagAt(i));
                }
            }

            this.list = list;
            populateRawListWithTags();
            scroll.setList(getSearchList(), ascending == 0, sortType == 0);
        }
    }
}
