package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.clone.CloneAllTagsPacket;
import kamkeel.npcs.network.packets.request.clone.CloneFolderCrudPacket;
import kamkeel.npcs.network.packets.request.clone.CloneListPacket;
import kamkeel.npcs.network.packets.request.clone.CloneMovePacket;
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
import noppes.npcs.client.gui.util.GuiCustomScrollIcons;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IClonerGui;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.CloneFolder;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.controllers.data.TagMap;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiNpcMobSpawnerFullscreen extends GuiNPCInterface implements IGuiData, ICustomScrollListener, ISubGuiListener, IClonerGui {

    // ==================== LAYOUT CONSTANTS ====================
    private static final int PAD = 10;
    private static final int LEFT_NAV_W = 120;
    private static final int RIGHT_ACT_W = 60;
    private static final int TOP_BAR_H = 22;
    private static final int GAP = 2;

    // ==================== INSTANCE STATE ====================
    public TagMap tagMap;
    private final GuiCustomScrollCloner scroll = new GuiCustomScrollCloner(this, 0);
    private final GuiCustomScroll filterScroll = new GuiCustomScroll(this, 1);
    private final GuiCustomScrollIcons navScroll = new GuiCustomScrollIcons(this, 2);

    private int posX, posY, posZ;
    private List<String> list;
    private List<String> rawList;
    private List<String> tagList;
    private List<String> folderNames = new ArrayList<>();

    // Left nav list data
    private List<String> navNames = new ArrayList<>();
    private List<Integer> navIcons = new ArrayList<>();
    private String navSearch = "";

    // ==================== CONSTRUCTOR ====================
    public GuiNpcMobSpawnerFullscreen(int posX, int posY, int posZ) {
        super();
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        closeOnEsc = true;
        drawDefaultBackground = true;
        // No setBackground() call — no menubg.png tiling
        PacketClient.sendClient(new CloneAllTagsPacket());
        loadFolderNames();
    }

    private void loadFolderNames() {
        folderNames.clear();
        if (ClientCloneController.Instance != null) {
            for (CloneFolder folder : ClientCloneController.Instance.getFolderList()) {
                folderNames.add(folder.name);
            }
        }
    }

    // ==================== IClonerGui ====================
    @Override
    public int getShowingClones() {
        return GuiNpcMobSpawner.showingClones;
    }

    @Override
    public HashMap<UUID, Tag> getTags() {
        return GuiNpcMobSpawner.tags;
    }

    @Override
    public TagMap getTagMap() {
        return tagMap;
    }

    // ==================== INIT GUI ====================
    @Override
    public void initGui() {
        super.initGui();

        int originX = PAD;
        int originY = PAD;
        int usableW = width - 2 * PAD;
        int usableH = height - 2 * PAD;

        int contentX = originX + LEFT_NAV_W + GAP;
        int contentY = originY + TOP_BAR_H + GAP;
        int contentW = usableW - LEFT_NAV_W - RIGHT_ACT_W - 3 * GAP;
        int contentH = usableH - TOP_BAR_H - GAP;
        int rightX = contentX + contentW + GAP;

        // Top bar — mode buttons
        GuiMenuTopButton btn;
        addTopButton(btn = new GuiMenuTopButton(3, originX, originY, "spawner.clones"));
        btn.active = GuiNpcMobSpawner.showingClones == 0;
        addTopButton(btn = new GuiMenuTopButton(5, btn, "gui.server"));
        btn.active = GuiNpcMobSpawner.showingClones == 2;
        addTopButton(btn = new GuiMenuTopButton(4, btn, "spawner.entities"));
        btn.active = GuiNpcMobSpawner.showingClones == 1;

        // Top bar — right side
        int topRight = originX + usableW;
        addTopButton(new GuiMenuTopButton(17, topRight - 22, originY, "X"));
        addTopButton(new GuiMenuTopButton(61, topRight - 38, originY, "-"));
        addTopButton(btn = new GuiMenuTopButton(16, topRight - 82, originY, "gui.filters"));
        btn.active = GuiNpcMobSpawner.showingClones == 3;

        if (GuiNpcMobSpawner.showingClones < 3) {
            // Search field in top bar
            addTextField(new GuiNpcTextField(1, this, fontRendererObj,
                contentX, originY + 2, contentW, 20, GuiNpcMobSpawner.search));

            // Left navigation panel
            buildNavList();
            navScroll.clear();
            navScroll.setSize(LEFT_NAV_W, contentH - 44);
            navScroll.guiLeft = originX;
            navScroll.guiTop = contentY;
            navScroll.setListWithIcons(navNames, navIcons);
            navScroll.setSelected(getNavSelection());
            addScroll(navScroll);

            // Nav search field
            addTextField(new GuiNpcTextField(3, this, fontRendererObj,
                originX, contentY + contentH - 42, LEFT_NAV_W, 20, navSearch));

            // Add folder button
            addButton(new GuiNpcButton(40, originX, contentY + contentH - 20, LEFT_NAV_W, 20, "+ Add Folder"));

            // Center content — clone/entity list
            scroll.clear();
            scroll.setSize(contentW, contentH);
            scroll.guiLeft = contentX;
            scroll.guiTop = contentY;
            addScroll(scroll);

            // Right action panel
            int btnW = RIGHT_ACT_W;
            int btnH = 20;
            int btnGap = 2;
            addButton(new GuiNpcButton(1, rightX, contentY, btnW, btnH, "item.monsterPlacer.name"));
            addButton(new GuiNpcButton(2, rightX, contentY + btnH + btnGap, btnW, btnH, "spawner.mobspawner"));

            if (GuiNpcMobSpawner.showingClones == 0 || GuiNpcMobSpawner.showingClones == 2) {
                addButton(new GuiNpcButton(6, rightX, contentY + 2 * (btnH + btnGap), btnW, btnH, "gui.remove"));
                addButton(new GuiNpcButton(50, rightX, contentY + 3 * (btnH + btnGap), btnW, btnH, "Move"));
                showClones();
            } else {
                showEntities();
            }
        } else {
            showFiltersPage();
        }
    }

    // ==================== LEFT NAVIGATION ====================
    private void buildNavList() {
        navNames.clear();
        navIcons.clear();

        String searchLower = navSearch.toLowerCase();

        for (int i = 1; i <= 15; i++) {
            String name = "Tab " + i;
            if (searchLower.isEmpty() || name.toLowerCase().contains(searchLower)) {
                navNames.add(name);
                navIcons.add(GuiCustomScrollIcons.ICON_TAB);
            }
        }
        for (String folder : folderNames) {
            if (searchLower.isEmpty() || folder.toLowerCase().contains(searchLower)) {
                navNames.add(folder);
                navIcons.add(GuiCustomScrollIcons.ICON_FOLDER);
            }
        }
    }

    private String getNavSelection() {
        if (GuiNpcMobSpawner.activeFolder != null) return GuiNpcMobSpawner.activeFolder;
        if (GuiNpcMobSpawner.activeTab >= 1 && GuiNpcMobSpawner.activeTab <= 15) {
            return "Tab " + GuiNpcMobSpawner.activeTab;
        }
        return "Tab 1";
    }

    private void handleNavClick(String selected) {
        if (selected == null) return;

        // Check if it's a tab
        if (selected.startsWith("Tab ")) {
            try {
                int tabNum = Integer.parseInt(selected.substring(4));
                if (tabNum >= 1 && tabNum <= 15) {
                    GuiNpcMobSpawner.activeTab = tabNum;
                    GuiNpcMobSpawner.activeFolder = null;
                    initGui();
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // It's a folder name
        GuiNpcMobSpawner.activeFolder = selected;
        GuiNpcMobSpawner.activeTab = -1;
        initGui();
    }

    // ==================== FILTERS PAGE ====================
    private void showFiltersPage() {
        int originX = PAD;
        int originY = PAD;
        int baseY = originY + TOP_BAR_H + GAP;

        addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("cloner.tagFilters"),
            originX + 7, baseY + 7));

        filterScroll.clear();
        filterScroll.setSize(140, 166);
        filterScroll.guiLeft = originX + 4;
        filterScroll.guiTop = baseY + 19;
        filterScroll.multipleSelection = true;
        filterScroll.setSelectedList(GuiNpcMobSpawner.tagFilters);
        addScroll(filterScroll);

        addTextField(new GuiNpcTextField(2, this, fontRendererObj,
            originX + 4, baseY + 190, 140, 20, GuiNpcMobSpawner.tagSearch));

        addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("cloner.tagVisibility"),
            originX + 150, baseY + 27));
        addButton(new GuiNpcButton(12, originX + 215, baseY + 20,
            new String[]{"display.show", "display.all", "display.hide"}, GuiNpcMobSpawner.displayTags));

        addLabel(new GuiNpcLabel(3, StatCollector.translateToLocal("filter.contains"),
            originX + 150, baseY + 50));
        addButton(new GuiNpcButton(13, originX + 215, baseY + 43,
            new String[]{"filter.any", "filter.all", "filter.notany", "filter.notall"}, GuiNpcMobSpawner.filterCondition));

        addButton(new GuiNpcButton(11, originX + 150, baseY + 66, 130, 20, "gui.deselectAll"));

        addLabel(new GuiNpcLabel(4, StatCollector.translateToLocal("cloner.order"),
            originX + 150, baseY + 112));
        addButton(new GuiNpcButton(14, originX + 215, baseY + 105,
            new String[]{"cloner.ascending", "cloner.descending"}, GuiNpcMobSpawner.ascending));

        addLabel(new GuiNpcLabel(5, StatCollector.translateToLocal("cloner.type"),
            originX + 150, baseY + 135));
        addButton(new GuiNpcButton(15, originX + 215, baseY + 128,
            new String[]{"cloner.name", "cloner.date"}, GuiNpcMobSpawner.sortType));

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
        if (GuiNpcMobSpawner.activeFolder != null) {
            showFolderClones();
            return;
        }

        if (GuiNpcMobSpawner.showingClones == 2) {
            PacketClient.sendClient(new CloneTagListPacket(GuiNpcMobSpawner.activeTab));
            PacketClient.sendClient(new CloneListPacket(GuiNpcMobSpawner.activeTab));
            return;
        }

        if (GuiNpcMobSpawner.sortType == 0) {
            this.list = ClientCloneController.Instance.getClones(GuiNpcMobSpawner.activeTab);
        } else {
            this.list = ClientCloneController.Instance.getClonesDate(GuiNpcMobSpawner.activeTab);
        }

        this.tagMap = ClientTagMapController.Instance.getTagMap(GuiNpcMobSpawner.activeTab);
        populateRawListWithTags();
        scroll.setList(getSearchList(), GuiNpcMobSpawner.ascending == 0, GuiNpcMobSpawner.sortType == 0);
    }

    private void showFolderClones() {
        if (GuiNpcMobSpawner.showingClones == 2) {
            PacketClient.sendClient(new CloneTagListPacket(GuiNpcMobSpawner.activeFolder));
            PacketClient.sendClient(new CloneListPacket(GuiNpcMobSpawner.activeFolder));
            return;
        }

        if (ClientCloneController.Instance != null) {
            if (GuiNpcMobSpawner.sortType == 0) {
                this.list = ClientCloneController.Instance.getClones(GuiNpcMobSpawner.activeFolder);
            } else {
                this.list = ClientCloneController.Instance.getClonesDate(GuiNpcMobSpawner.activeFolder);
            }
            this.tagMap = ClientTagMapController.Instance.getTagMap(GuiNpcMobSpawner.activeFolder);
        } else {
            this.list = new ArrayList<>();
            this.tagMap = null;
        }

        populateRawListWithTags();
        scroll.setList(getSearchList(), GuiNpcMobSpawner.ascending == 0, GuiNpcMobSpawner.sortType == 0);
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
        scroll.setList(getSearchList(), GuiNpcMobSpawner.ascending == 0, GuiNpcMobSpawner.sortType == 0);
    }

    private void populateRawListWithTags() {
        this.rawList = new ArrayList<>(this.list);
        if (this.tagMap != null) {
            if (GuiNpcMobSpawner.displayTags == 0 || GuiNpcMobSpawner.displayTags == 1) {
                for (int i = 0; i < this.list.size(); i++) {
                    StringBuilder npcName = new StringBuilder(list.get(i));
                    if (this.tagMap.hasClone(list.get(i))) {
                        for (UUID tagUUID : this.tagMap.getUUIDsList(list.get(i))) {
                            Tag tag = GuiNpcMobSpawner.tags.get(tagUUID);
                            if (tag != null) {
                                if (GuiNpcMobSpawner.displayTags == 1 || !tag.getIsHidden()) {
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

        if (GuiNpcMobSpawner.showingClones == 0) {
            if (GuiNpcMobSpawner.activeFolder != null && ClientCloneController.Instance != null) {
                return ClientCloneController.Instance.getCloneData(player, sel, GuiNpcMobSpawner.activeFolder);
            }
            return ClientCloneController.Instance.getCloneData(player, sel, GuiNpcMobSpawner.activeTab);
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

        if (GuiNpcMobSpawner.tagFilters.size() == 0 || GuiNpcMobSpawner.showingClones == 1) {
            if (GuiNpcMobSpawner.search.isEmpty())
                return new ArrayList<String>(this.list);
            else {
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < this.list.size(); i++) {
                    if (this.rawList.get(i).toLowerCase().contains(GuiNpcMobSpawner.search)) {
                        list.add(this.list.get(i));
                    }
                }
                return list;
            }
        }

        if (GuiNpcMobSpawner.search.isEmpty()) {
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
            if (this.rawList.get(i).toLowerCase().contains(GuiNpcMobSpawner.search)) {
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
        boolean allRequirement = GuiNpcMobSpawner.filterCondition == 1 || GuiNpcMobSpawner.filterCondition == 3;

        for (String tagName : GuiNpcMobSpawner.tagFilters) {
            UUID tagUUID = GuiNpcMobSpawner.tagNames.get(tagName);
            if (tagUUID != null) {
                boolean hasTag = tagMap.hasTag(name, tagUUID);
                if (!allRequirement) {
                    if (!hasTag && GuiNpcMobSpawner.filterCondition == 0) {
                        conditionMet = false;
                        break;
                    } else if (hasTag && GuiNpcMobSpawner.filterCondition == 2) {
                        conditionMet = false;
                        break;
                    }
                } else {
                    if (!hasTag && GuiNpcMobSpawner.filterCondition == 1) {
                        conditionMet = false;
                        break;
                    } else if (hasTag && GuiNpcMobSpawner.filterCondition == 3) {
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
        if (GuiNpcMobSpawner.tagSearch.isEmpty())
            return new ArrayList<String>(GuiNpcMobSpawner.tagNames.keySet());

        List<String> list = new ArrayList<String>();
        for (String name : this.tagList) {
            if (name.toLowerCase().contains(GuiNpcMobSpawner.tagSearch))
                list.add(name);
        }
        return list;
    }

    // ==================== SCROLL EVENTS ====================
    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 2) {
            String selected = navScroll.getSelected();
            handleNavClick(selected);
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
    }

    // ==================== KEYBOARD ====================
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(1) != null) {
            if (GuiNpcMobSpawner.search.equals(getTextField(1).getText()))
                return;
            GuiNpcMobSpawner.search = getTextField(1).getText().toLowerCase();
            scroll.setList(getSearchList(), GuiNpcMobSpawner.ascending == 0, GuiNpcMobSpawner.sortType == 0);
        }
        if (getTextField(2) != null) {
            if (GuiNpcMobSpawner.tagSearch.equals(getTextField(2).getText()))
                return;
            GuiNpcMobSpawner.tagSearch = getTextField(2).getText().toLowerCase();
            filterScroll.setList(getTagList());
        }
        if (getTextField(3) != null) {
            String newNavSearch = getTextField(3).getText().toLowerCase();
            if (!navSearch.equals(newNavSearch)) {
                navSearch = newNavSearch;
                buildNavList();
                navScroll.setListWithIcons(navNames, navIcons);
                navScroll.setSelected(getNavSelection());
            }
        }
    }

    // ==================== BUTTON ACTIONS ====================
    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;

        // Mode buttons
        if (id == 3) {
            GuiNpcMobSpawner.showingClones = 0;
            initGui();
        }
        if (id == 4) {
            GuiNpcMobSpawner.showingClones = 1;
            initGui();
        }
        if (id == 5) {
            GuiNpcMobSpawner.showingClones = 2;
            initGui();
        }
        if (id == 16) {
            GuiNpcMobSpawner.showingClones = 3;
            initGui();
        }
        if (id == 17) {
            close();
        }

        // Minimize — return to normal GUI
        if (id == 61) {
            Minecraft.getMinecraft().displayGuiScreen(
                new GuiNpcMobSpawner(posX, posY, posZ));
        }

        // Spawn
        if (id == 1) {
            if (GuiNpcMobSpawner.showingClones == 2) {
                String sel = scroll.getSelected();
                if (sel == null) return;
                if (GuiNpcMobSpawner.activeFolder != null)
                    SpawnMobPacket.ServerFolder(posX, posY, posZ, sel, GuiNpcMobSpawner.activeFolder);
                else
                    SpawnMobPacket.Server(posX, posY, posZ, sel, GuiNpcMobSpawner.activeTab);
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
            if (GuiNpcMobSpawner.showingClones == 2) {
                String sel = scroll.getSelected();
                if (sel == null) return;
                if (GuiNpcMobSpawner.activeFolder != null)
                    MobSpawnerPacket.ServerFolder(posX, posY, posZ, sel, GuiNpcMobSpawner.activeFolder);
                else
                    MobSpawnerPacket.Server(posX, posY, posZ, sel, GuiNpcMobSpawner.activeTab);
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
                if (GuiNpcMobSpawner.showingClones == 2) {
                    if (GuiNpcMobSpawner.activeFolder != null)
                        PacketClient.sendClient(new CloneRemovePacket(GuiNpcMobSpawner.activeFolder, scroll.getSelected()));
                    else
                        PacketClient.sendClient(new CloneRemovePacket(GuiNpcMobSpawner.activeTab, scroll.getSelected()));
                    return;
                }
                if (GuiNpcMobSpawner.activeFolder != null && ClientCloneController.Instance != null)
                    ClientCloneController.Instance.removeClone(scroll.getSelected(), GuiNpcMobSpawner.activeFolder);
                else
                    ClientCloneController.Instance.removeClone(scroll.getSelected(), GuiNpcMobSpawner.activeTab);
                scroll.selected = -1;
                initGui();
            }
        }

        // Move
        if (id == 50) {
            if (scroll.getSelected() != null) {
                setSubGui(new SubGuiCloneMove());
            }
        }

        // Filter controls
        if (id == 11) {
            GuiNpcMobSpawner.tagFilters = new HashSet<>();
            filterScroll.setSelectedList(GuiNpcMobSpawner.tagFilters);
            initGui();
        }
        if (id == 12) {
            GuiNpcMobSpawner.displayTags = (byte) ((GuiNpcButton) guibutton).getValue();
        }
        if (id == 13) {
            GuiNpcMobSpawner.filterCondition = (byte) ((GuiNpcButton) guibutton).getValue();
        }
        if (id == 14) {
            GuiNpcMobSpawner.ascending = (byte) ((GuiNpcButton) guibutton).getValue();
        }
        if (id == 15) {
            GuiNpcMobSpawner.sortType = (byte) ((GuiNpcButton) guibutton).getValue();
        }

        // Create folder
        if (id == 40) {
            setSubGui(new SubGuiCloneFolderName(""));
        }
    }

    // ==================== SUB GUI ====================
    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiCloneFolderName) {
            SubGuiCloneFolderName folderGui = (SubGuiCloneFolderName) subgui;
            if (folderGui.cancelled) return;

            String newName = folderGui.getFolderName();
            if (newName == null || newName.isEmpty()) return;

            if (folderGui.isRename()) {
                if (GuiNpcMobSpawner.showingClones == 2) {
                    PacketClient.sendClient(new CloneFolderCrudPacket(
                        CloneFolderCrudPacket.ACTION_RENAME, folderGui.getOriginalName(), newName));
                } else if (ClientCloneController.Instance != null) {
                    ClientCloneController.Instance.renameFolder(folderGui.getOriginalName(), newName);
                    loadFolderNames();
                }
                GuiNpcMobSpawner.activeFolder = newName;
                GuiNpcMobSpawner.activeTab = -1;
            } else {
                if (GuiNpcMobSpawner.showingClones == 2) {
                    PacketClient.sendClient(new CloneFolderCrudPacket(
                        CloneFolderCrudPacket.ACTION_CREATE, newName));
                } else if (ClientCloneController.Instance != null) {
                    ClientCloneController.Instance.createFolder(newName);
                    loadFolderNames();
                }
                GuiNpcMobSpawner.activeFolder = newName;
                GuiNpcMobSpawner.activeTab = -1;
            }
            initGui();
        }

        if (subgui instanceof SubGuiCloneMove) {
            SubGuiCloneMove moveGui = (SubGuiCloneMove) subgui;
            if (!moveGui.cancelled) {
                String cloneName = scroll.getSelected();
                if (cloneName != null) {
                    int fromTab = GuiNpcMobSpawner.activeFolder != null ? -1 : GuiNpcMobSpawner.activeTab;
                    String fromFolder = GuiNpcMobSpawner.activeFolder;
                    int toTab = moveGui.getDestTab();
                    String toFolder = moveGui.getDestFolder();

                    if (GuiNpcMobSpawner.showingClones == 2) {
                        PacketClient.sendClient(new CloneMovePacket(
                            cloneName, fromTab, fromFolder, toTab, toFolder));
                    } else if (ClientCloneController.Instance != null) {
                        if (fromFolder != null && toFolder != null) {
                            ClientCloneController.Instance.moveClone(cloneName, fromFolder, toFolder);
                        } else if (fromFolder != null) {
                            ClientCloneController.Instance.moveClone(cloneName, fromFolder, toTab);
                        } else if (toFolder != null) {
                            ClientCloneController.Instance.moveClone(cloneName, fromTab, toFolder);
                        } else {
                            ClientCloneController.Instance.moveClone(cloneName, fromTab, toTab);
                        }
                    }
                    initGui();
                }
            }
        }
    }

    // ==================== DATA FROM SERVER ====================
    @Override
    public void save() {
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("CloneFolders")) {
            NBTTagList folderList = compound.getTagList("CloneFolders", 10);
            folderNames.clear();
            for (int i = 0; i < folderList.tagCount(); i++) {
                CloneFolder folder = new CloneFolder();
                folder.readNBT(folderList.getCompoundTagAt(i));
                folderNames.add(folder.name);
            }
            initGui();
        } else if (compound.hasKey("MoveSuccess")) {
            initGui();
        } else if (compound.hasKey("CloneTags")) {
            if (GuiNpcMobSpawner.activeFolder != null) {
                this.tagMap = new TagMap(GuiNpcMobSpawner.activeFolder);
            } else {
                this.tagMap = new TagMap(GuiNpcMobSpawner.activeTab);
            }
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
                GuiNpcMobSpawner.tags = tagsUpdate;
                GuiNpcMobSpawner.tagNames = tagNamesUpdate;
                this.tagList = new ArrayList<String>(GuiNpcMobSpawner.tagNames.keySet());
                filterScroll.setList(getTagList());
            }
        } else {
            List<String> list = new ArrayList<String>();
            if (GuiNpcMobSpawner.sortType == 1) {
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
            scroll.setList(getSearchList(), GuiNpcMobSpawner.ascending == 0, GuiNpcMobSpawner.sortType == 0);
        }
    }
}
