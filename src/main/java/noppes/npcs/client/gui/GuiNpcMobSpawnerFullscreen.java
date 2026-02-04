package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.clone.CloneAllTagsPacket;
import kamkeel.npcs.network.packets.request.clone.CloneFolderCrudPacket;
import kamkeel.npcs.network.packets.request.clone.CloneFolderListPacket;
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
import noppes.npcs.client.gui.util.GuiDirectory;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IClonerGui;
import noppes.npcs.client.gui.util.IGuiData;
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

public class GuiNpcMobSpawnerFullscreen extends GuiDirectory implements IGuiData, IClonerGui {

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

    // Move workflow state
    private int movePhase = 0; // 0=off, 1=selecting clones, 2=selecting destination
    private HashSet<String> moveSelection = new HashSet<>();

    // ==================== CONSTRUCTOR ====================
    public GuiNpcMobSpawnerFullscreen(int posX, int posY, int posZ) {
        super();
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;

        // Configure GuiDirectory layout percentages
        leftPanelPercent  = 0.15f;
        rightPanelPercent = 0.08f;
        minLeftPanelW     = 120;
        minRightPanelW    = 62;

        PacketClient.sendClient(new CloneAllTagsPacket());
        if (GuiNpcMobSpawner.showingClones == 2) {
            PacketClient.sendClient(new CloneFolderListPacket());
        }
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

    // ==================== GuiDirectory HOOKS ====================
    @Override
    public void initGui() {
        // Save scroll positions before super.initGui() clears everything
        GuiNpcMobSpawner.savedContentScrollY = scroll.scrollY;
        GuiNpcMobSpawner.savedNavScrollY = navScroll.scrollY;

        super.initGui();
    }

    @Override
    protected void drawPanels() {
        if (GuiNpcMobSpawner.showingClones < 3) {
            super.drawPanels();
        } else {
            // Filters page: only draw top bar, no side panels
        }
    }

    @Override
    protected void initTopBar(int topBtnY) {
        int topBtnW = 55;
        int x = originX + 2;

        GuiNpcButton clonesBtn = new GuiNpcButton(3, x, topBtnY, topBtnW, btnH, "spawner.clones");
        clonesBtn.enabled = GuiNpcMobSpawner.showingClones != 0;
        addButton(clonesBtn);
        x += topBtnW + 2;

        GuiNpcButton serverBtn = new GuiNpcButton(5, x, topBtnY, topBtnW, btnH, "gui.server");
        serverBtn.enabled = GuiNpcMobSpawner.showingClones != 2;
        addButton(serverBtn);
        x += topBtnW + 2;

        GuiNpcButton entitiesBtn = new GuiNpcButton(4, x, topBtnY, topBtnW, btnH, "spawner.entities");
        entitiesBtn.enabled = GuiNpcMobSpawner.showingClones != 1;
        addButton(entitiesBtn);

        // Top bar right side
        int closeX = originX + usableW - btnH - 2;
        addButton(new GuiNpcButton(17, closeX, topBtnY, btnH, btnH, "X"));
        int minimizeX = closeX - btnH - 2;
        addButton(new GuiNpcButton(61, minimizeX, topBtnY, btnH, btnH, "-"));
        int filterBtnW = 50;
        int filterX = minimizeX - filterBtnW - 2;
        GuiNpcButton filterBtn = new GuiNpcButton(16, filterX, topBtnY, filterBtnW, btnH, "gui.filters");
        filterBtn.enabled = GuiNpcMobSpawner.showingClones != 3;
        addButton(filterBtn);

        // Clone search field in top bar (only when not on filters page)
        if (GuiNpcMobSpawner.showingClones < 3) {
            int searchX = x + topBtnW + 6;
            int searchW = filterX - searchX - 4;
            if (searchW > 20) {
                addTextField(new GuiNpcTextField(1, this, fontRendererObj,
                    searchX, topBtnY, searchW, btnH, GuiNpcMobSpawner.search));
            }
        }

        // Disable mode buttons during move
        if (movePhase > 0) {
            clonesBtn.enabled = false;
            serverBtn.enabled = false;
            entitiesBtn.enabled = false;
            filterBtn.enabled = false;
        }
    }

    @Override
    protected void initLeftPanel() {
        if (GuiNpcMobSpawner.showingClones >= 3) return;

        boolean folderSelected = GuiNpcMobSpawner.activeFolder != null;
        int bottomRows = folderSelected ? 3 : 2;
        int bottomH = bottomRows * (btnH + gap);
        int navH = contentH - bottomH;
        buildNavList();

        int savedNavScroll = GuiNpcMobSpawner.savedNavScrollY;
        navScroll.clear();
        navScroll.colors.clear();
        navScroll.setSize(leftPanelW, navH);
        navScroll.guiLeft = originX;
        navScroll.guiTop = contentY;
        navScroll.setListWithIcons(navNames, navIcons);
        navScroll.setSelected(getNavSelection());
        navScroll.scrollY = Math.min(savedNavScroll, Math.max(0, navScroll.maxScrollY));

        // Phase 2: color nav items orange (selectable) or red (current location, not selectable)
        if (movePhase == 2) {
            String currentNav = getNavSelection();
            for (String name : navNames) {
                navScroll.colors.put(name, name.equals(currentNav) ? 0xFF5555 : 0xFFAA00);
            }
        }

        addScroll(navScroll);

        // Nav search field below the scroll
        int navSearchY = contentY + navH + gap;
        addTextField(new GuiNpcTextField(3, this, fontRendererObj,
            originX, navSearchY, leftPanelW, btnH, navSearch));

        // Add folder button
        int addFolderY = navSearchY + btnH + gap;
        GuiNpcButton addFolderBtn = new GuiNpcButton(40, originX, addFolderY, leftPanelW, btnH, "+ Add Folder");
        if (movePhase > 0) addFolderBtn.enabled = false;
        addButton(addFolderBtn);

        // Rename/Delete folder buttons (only when a folder is selected)
        if (folderSelected) {
            int folderCrudY = addFolderY + btnH + gap;
            int halfW = (leftPanelW - gap) / 2;
            GuiNpcButton renameBtn = new GuiNpcButton(41, originX, folderCrudY, halfW, btnH, "Rename");
            if (movePhase > 0) renameBtn.enabled = false;
            addButton(renameBtn);
            GuiNpcButton delBtn = new GuiNpcButton(42, originX + halfW + gap, folderCrudY, halfW, btnH, "gui.remove");
            if (ClientCloneController.Instance != null) {
                List<String> clones = ClientCloneController.Instance.getClones(GuiNpcMobSpawner.activeFolder);
                delBtn.enabled = clones == null || clones.isEmpty();
            }
            if (movePhase > 0) delBtn.enabled = false;
            addButton(delBtn);
        }
    }

    @Override
    protected void initCenterPanel() {
        if (GuiNpcMobSpawner.showingClones >= 3) {
            showFiltersPage();
            return;
        }

        int savedScroll = GuiNpcMobSpawner.savedContentScrollY;
        scroll.clear();
        scroll.setSize(contentW, contentH);
        scroll.guiLeft = contentX;
        scroll.guiTop = contentY;
        addScroll(scroll);

        if (GuiNpcMobSpawner.showingClones == 0 || GuiNpcMobSpawner.showingClones == 2) {
            // Always clear colors — only phase 2 re-populates them
            scroll.colors.clear();

            // Multi-select mode during move phase 1
            if (movePhase == 1) {
                scroll.multipleSelection = true;
                scroll.setSelectable(true);
                scroll.setSelectedList(moveSelection);
            } else if (movePhase == 2) {
                // Phase 2: show selection as yellow, freeze scroll
                scroll.multipleSelection = true;
                scroll.setSelectedList(moveSelection);
                scroll.setSelectable(false);
            } else {
                scroll.multipleSelection = false;
                scroll.setSelectable(true);
            }

            showClones();

            // Phase 2: color selected clones yellow
            if (movePhase == 2) {
                for (String name : moveSelection) {
                    scroll.colors.put(name, 0xFFFF55);
                }
            }

            // Restore scroll position after list is populated
            scroll.scrollY = Math.min(savedScroll, Math.max(0, scroll.maxScrollY));
        } else {
            showEntities();
            scroll.scrollY = Math.min(savedScroll, Math.max(0, scroll.maxScrollY));
        }
    }

    @Override
    protected void initRightPanel(int startY) {
        if (GuiNpcMobSpawner.showingClones >= 3) return;

        int btnY = startY;
        int btnGap = 4;

        GuiNpcButton spawnBtn = new GuiNpcButton(1, rightX, btnY, rightPanelW, btnH, "item.monsterPlacer.name");
        if (movePhase > 0) spawnBtn.enabled = false;
        addButton(spawnBtn);
        btnY += btnH + btnGap;

        GuiNpcButton spawnerBtn = new GuiNpcButton(2, rightX, btnY, rightPanelW, btnH, "spawner.mobspawner");
        if (movePhase > 0) spawnerBtn.enabled = false;
        addButton(spawnerBtn);
        btnY += btnH + btnGap;

        if (GuiNpcMobSpawner.showingClones == 0 || GuiNpcMobSpawner.showingClones == 2) {
            GuiNpcButton removeBtn = new GuiNpcButton(6, rightX, btnY, rightPanelW, btnH, "gui.remove");
            if (movePhase > 0) removeBtn.enabled = false;
            addButton(removeBtn);
            btnY += btnH + btnGap;

            // Move button
            String moveLabel = movePhase > 0 ? "Moving" : "Move";
            addButton(new GuiNpcButton(50, rightX, btnY, rightPanelW, btnH, moveLabel));
            btnY += btnH + btnGap;

            // Confirm button (only visible during phase 1)
            if (movePhase == 1) {
                GuiNpcButton confirmBtn = new GuiNpcButton(51, rightX, btnY, rightPanelW, btnH, "Confirm");
                confirmBtn.enabled = !moveSelection.isEmpty();
                addButton(confirmBtn);
            }
        }
    }

    // ==================== OVERLAY DRAWING ====================
    @Override
    protected void drawOverlay(int mouseX, int mouseY, float partialTicks) {
        if (movePhase == 1) {
            String text = "Select Clones to Move";
            int textW = fontRendererObj.getStringWidth(text);
            fontRendererObj.drawStringWithShadow(text, width / 2 - textW / 2, height - pad - 10, 0x55FF55);
        } else if (movePhase == 2) {
            String text = "Select a Tab or Folder";
            int textW = fontRendererObj.getStringWidth(text);
            fontRendererObj.drawStringWithShadow(text, width / 2 - textW / 2, height - pad - 10, 0xFF5555);
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

        GuiNpcMobSpawner.activeFolder = selected;
        GuiNpcMobSpawner.activeTab = -1;
        initGui();
    }

    // ==================== FILTERS PAGE ====================
    private void showFiltersPage() {
        int scrollW = Math.min(usableW / 2 - gap * 2, 240);
        int scrollX = originX + gap;
        int scrollTopY = contentY + 16;
        int scrollH = contentH - 16 - btnH - gap * 2;

        addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("cloner.tagFilters"),
            scrollX + 2, contentY + 4, 0xFFFFFF));

        filterScroll.clear();
        filterScroll.setSize(scrollW, scrollH);
        filterScroll.guiLeft = scrollX;
        filterScroll.guiTop = scrollTopY;
        filterScroll.multipleSelection = true;
        filterScroll.setSelectedList(GuiNpcMobSpawner.tagFilters);
        addScroll(filterScroll);

        addTextField(new GuiNpcTextField(2, this, fontRendererObj,
            scrollX, scrollTopY + scrollH + gap, scrollW, btnH, GuiNpcMobSpawner.tagSearch));

        // Settings column
        int col2X = scrollX + scrollW + gap * 4;
        int controlW = 90;
        int controlX = col2X + 80;
        int rowH = 28;
        int rowY = scrollTopY;

        addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("cloner.tagVisibility"),
            col2X, rowY + 5, 0xFFFFFF));
        addButton(new GuiNpcButton(12, controlX, rowY, controlW, btnH,
            new String[]{"display.show", "display.all", "display.hide"}, GuiNpcMobSpawner.displayTags));

        rowY += rowH;
        addLabel(new GuiNpcLabel(3, StatCollector.translateToLocal("filter.contains"),
            col2X, rowY + 5, 0xFFFFFF));
        addButton(new GuiNpcButton(13, controlX, rowY, controlW, btnH,
            new String[]{"filter.any", "filter.all", "filter.notany", "filter.notall"}, GuiNpcMobSpawner.filterCondition));

        rowY += rowH + 4;
        addButton(new GuiNpcButton(11, col2X, rowY, controlW + 80, btnH, "gui.deselectAll"));

        rowY += rowH + 12;
        addLabel(new GuiNpcLabel(4, StatCollector.translateToLocal("cloner.order"),
            col2X, rowY + 5, 0xFFFFFF));
        addButton(new GuiNpcButton(14, controlX, rowY, controlW, btnH,
            new String[]{"cloner.ascending", "cloner.descending"}, GuiNpcMobSpawner.ascending));

        rowY += rowH;
        addLabel(new GuiNpcLabel(5, StatCollector.translateToLocal("cloner.type"),
            col2X, rowY + 5, 0xFFFFFF));
        addButton(new GuiNpcButton(15, controlX, rowY, controlW, btnH,
            new String[]{"cloner.name", "cloner.date"}, GuiNpcMobSpawner.sortType));

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
        // Phase 2: ignore clicks on the center clone scroll
        if (guiCustomScroll.id == 0 && movePhase == 2) {
            return;
        }

        if (guiCustomScroll.id == 0 && movePhase == 1) {
            moveSelection = scroll.getSelectedList();
            GuiNpcButton confirmBtn = getButton(51);
            if (confirmBtn != null) {
                confirmBtn.enabled = !moveSelection.isEmpty();
            }
            return;
        }

        if (guiCustomScroll.id == 2) {
            String selected = navScroll.getSelected();
            if (selected == null) return;

            if (movePhase == 2) {
                // Cannot move to the same location
                if (selected.equals(getNavSelection())) {
                    return;
                }
                // Selecting destination from nav
                if (selected.startsWith("Tab ")) {
                    try {
                        int tabNum = Integer.parseInt(selected.substring(4));
                        setSubGui(new SubGuiMoveConfirm(moveSelection.size(), tabNum, null));
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    setSubGui(new SubGuiMoveConfirm(moveSelection.size(), -1, selected));
                }
                return;
            }

            if (movePhase == 1) {
                movePhase = 0;
                moveSelection.clear();
            }

            handleNavClick(selected);
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
    }

    // ==================== KEYBOARD ====================
    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);

        GuiNpcTextField searchField = getTextField(1);
        if (searchField != null) {
            String newText = searchField.getText().toLowerCase();
            if (!GuiNpcMobSpawner.search.equals(newText)) {
                GuiNpcMobSpawner.search = newText;
                scroll.setList(getSearchList(), GuiNpcMobSpawner.ascending == 0, GuiNpcMobSpawner.sortType == 0);
            }
        }

        GuiNpcTextField tagSearchField = getTextField(2);
        if (tagSearchField != null) {
            String newText = tagSearchField.getText().toLowerCase();
            if (!GuiNpcMobSpawner.tagSearch.equals(newText)) {
                GuiNpcMobSpawner.tagSearch = newText;
                filterScroll.setList(getTagList());
            }
        }

        GuiNpcTextField navSearchField = getTextField(3);
        if (navSearchField != null) {
            String newNavSearch = navSearchField.getText().toLowerCase();
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

        if (id == 3) {
            movePhase = 0;
            moveSelection.clear();
            GuiNpcMobSpawner.savedContentScrollY = 0;
            GuiNpcMobSpawner.savedNavScrollY = 0;
            GuiNpcMobSpawner.showingClones = 0;
            loadFolderNames();
            initGui();
        }
        if (id == 4) {
            movePhase = 0;
            moveSelection.clear();
            GuiNpcMobSpawner.savedContentScrollY = 0;
            GuiNpcMobSpawner.savedNavScrollY = 0;
            GuiNpcMobSpawner.showingClones = 1;
            initGui();
        }
        if (id == 5) {
            movePhase = 0;
            moveSelection.clear();
            GuiNpcMobSpawner.savedContentScrollY = 0;
            GuiNpcMobSpawner.savedNavScrollY = 0;
            GuiNpcMobSpawner.showingClones = 2;
            PacketClient.sendClient(new CloneFolderListPacket());
            initGui();
        }
        if (id == 16) {
            movePhase = 0;
            moveSelection.clear();
            GuiNpcMobSpawner.savedContentScrollY = 0;
            GuiNpcMobSpawner.savedNavScrollY = 0;
            GuiNpcMobSpawner.showingClones = 3;
            initGui();
        }
        if (id == 17) {
            close();
        }

        // Minimize — return to normal GUI
        if (id == 61) {
            GuiNpcMobSpawner.isFullscreen = false;
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
                int prevSelected = scroll.selected;
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
                initGui();
                if (scroll.list != null && !scroll.list.isEmpty()) {
                    scroll.selected = Math.min(prevSelected, scroll.list.size() - 1);
                } else {
                    scroll.selected = -1;
                }
            }
        }

        // Move toggle
        if (id == 50) {
            if (movePhase == 0) {
                movePhase = 1;
                moveSelection.clear();
            } else {
                movePhase = 0;
                moveSelection.clear();
            }
            initGui();
        }
        // Confirm move selection
        if (id == 51) {
            if (movePhase == 1 && !moveSelection.isEmpty()) {
                movePhase = 2;
                initGui();
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
        // Rename folder
        if (id == 41) {
            if (GuiNpcMobSpawner.activeFolder != null)
                setSubGui(new SubGuiCloneFolderName(GuiNpcMobSpawner.activeFolder));
        }
        // Delete folder
        if (id == 42) {
            if (GuiNpcMobSpawner.activeFolder != null) {
                if (GuiNpcMobSpawner.showingClones == 2) {
                    PacketClient.sendClient(new CloneFolderCrudPacket(
                        CloneFolderCrudPacket.ACTION_DELETE, GuiNpcMobSpawner.activeFolder));
                } else if (ClientCloneController.Instance != null) {
                    ClientCloneController.Instance.deleteFolder(GuiNpcMobSpawner.activeFolder);
                    loadFolderNames();
                }
                GuiNpcMobSpawner.activeFolder = null;
                GuiNpcMobSpawner.activeTab = 1;
                initGui();
            }
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

        if (subgui instanceof SubGuiMoveConfirm) {
            SubGuiMoveConfirm confirm = (SubGuiMoveConfirm) subgui;
            if (confirm.confirmed && !moveSelection.isEmpty()) {
                int fromTab = GuiNpcMobSpawner.activeFolder != null ? -1 : GuiNpcMobSpawner.activeTab;
                String fromFolder = GuiNpcMobSpawner.activeFolder;
                int toTab = confirm.destTab;
                String toFolder = confirm.destFolder;

                for (String cloneName : moveSelection) {
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
                }
            }
            movePhase = 0;
            moveSelection.clear();
            initGui();
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
