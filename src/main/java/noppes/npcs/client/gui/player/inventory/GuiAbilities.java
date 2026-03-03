package noppes.npcs.client.gui.player.inventory;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.data.IAbilityAction;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.player.ability.AbilityHotbarSavePacket;
import kamkeel.npcs.network.packets.player.ability.AbilityHotbarSelectPacket;
import kamkeel.npcs.network.packets.player.ability.AbilityTogglePacket;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.hud.ability.AbilityIcon;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.AbilityHotbarData;
import noppes.npcs.controllers.data.PlayerData;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.AbstractTab;

import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.controllers.data.MagicEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GuiAbilities extends GuiCNPCInventory implements ISubGuiListener {
    private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/standardbg.png");

    // Sub-tab: 0 = Abilities, 1 = Toggles
    private int subTab = 0;
    private static final int BTN_TAB_ABILITIES = -200;
    private static final int BTN_TAB_TOGGLES = -201;

    // ═══ Panel Bounds (relative to guiLeft/guiTop) ═══
    // Selection panel (RIGHT side - icon grid)
    private int selLeft = 140;
    private int selTop = 4;
    private int selRight = 316;
    private int selBottom = 150;

    // Information panel (LEFT side - ability details)
    private int infoLeft = 5;
    private int infoTop = 4;
    private int infoRight = 137;
    private int infoBottom = 150;

    // Hotbar panel (BOTTOM)
    private int hbLeft = 5;
    private int hbTop = 153;
    private int hbRight = 316;
    private int hbBottom = 190;

    // ═══ Grid Settings ═══
    private int gridPad = 3;
    private int gridCellSize = 22;
    private int gridCols = 7;

    // ═══ Hotbar Settings ═══
    private int hbSlotSize = 18;
    private int hbSlotGap = 2;
    private int hbSlots = AbilityHotbarData.TOTAL_SLOTS;

    // ═══ Info Panel Settings ═══
    private int infoPad = 5;
    private int infoIconSize = 40;

    // ═══ Drag Settings ═══
    private int dragThreshold = 3;

    // ═══ Filtered ability data ═══
    private List<String> filteredKeys = new ArrayList<>();
    private List<String> filteredDisplayNames = new ArrayList<>();
    private List<AbilityIcon> filteredIcons = new ArrayList<>();
    private List<Ability> filteredAbilities = new ArrayList<>();
    private List<IAbilityAction> filteredActions = new ArrayList<>();

    // ═══ Grid state ═══
    private int scrollRow = 0;
    private int hoveredGridIndex = -1;
    private float[] gridHoverScale;
    private int selectedIndex = -1;

    // ═══ Detail panel cache ═══
    private AbilityIcon cachedDetailIcon = null;
    private int cachedDetailIndex = -1;

    // ═══ Info panel scroll ═══
    private int infoScrollOffset = 0;
    private int infoContentHeight = 0;
    private int lastInfoIndex = -1;

    // ═══ Hotbar state ═══
    private int hoveredSlotIndex = -1;
    private AbilityIcon[] hotbarIcons = new AbilityIcon[AbilityHotbarData.TOTAL_SLOTS];

    // ═══ Drag state ═══
    private boolean isDragging = false;
    private boolean dragPending = false;
    private String draggedKey = null;
    private AbilityIcon draggedIcon = null;
    private int dragSourceSlot = -1;
    private int dragStartX, dragStartY;
    private int dragMouseX, dragMouseY;

    // ═══ Close sync ═══
    private String initialSelectedKey = null;

    // ═══ Toggle double-tap ═══
    private long lastToggleClickTime = 0;
    private int lastToggleClickIndex = -1;

    public GuiAbilities() {
        super();
        xSize = 280;
        ySize = 180;

        hbSlotSize = 23;
        gridCellSize = 24;
        gridCols = 7;
    }

    @Override
    public void initGui() {
        super.initGui();

        GuiMenuTopButton tabAbilities = new GuiMenuTopButton(BTN_TAB_ABILITIES, guiLeft + xSize - 10, guiTop - 17, "gui.abilities");
        tabAbilities.active = (subTab == 0);
        addTopButton(tabAbilities);

        GuiMenuTopButton tabToggles = new GuiMenuTopButton(BTN_TAB_TOGGLES, "gui.toggles", tabAbilities);
        tabToggles.active = (subTab == 1);
        addTopButton(tabToggles);

        buildFilteredList();
        updateHotbarIcons();

        // Capture initial selected key for close sync
        if (initialSelectedKey == null) {
            PlayerData playerData = ClientCacheHandler.playerData;
            if (playerData != null && playerData.abilityData != null) {
                initialSelectedKey = playerData.abilityData.getSelectedAbilityKey();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Data Building
    // ═══════════════════════════════════════════════════════════════════

    private void buildFilteredList() {
        filteredKeys.clear();
        filteredDisplayNames.clear();
        filteredIcons.clear();
        filteredAbilities.clear();
        filteredActions.clear();
        cachedDetailIcon = null;
        cachedDetailIndex = -1;

        PlayerData playerData = ClientCacheHandler.playerData;
        if (playerData == null || playerData.abilityData == null) return;

        List<String> abilities = playerData.abilityData.getUnlockedAbilityList();

        // Build temporary lists before sorting
        List<String> tempKeys = new ArrayList<>();
        List<String> tempNames = new ArrayList<>();
        List<AbilityIcon> tempIcons = new ArrayList<>();
        List<Ability> tempAbilities = new ArrayList<>();
        List<IAbilityAction> tempActions = new ArrayList<>();

        for (String key : abilities) {
            Ability ability = null;
            IAbilityAction action = null;

            if (key.startsWith(AbilityHotbarData.CHAIN_PREFIX)) {
                String chainKey = key.substring(AbilityHotbarData.CHAIN_PREFIX.length());
                if (AbilityController.Instance != null) {
                    ChainedAbility chain = AbilityController.Instance.resolveChainedAbility(chainKey);
                    action = chain;
                }
            } else if (AbilityController.Instance != null) {
                ability = AbilityController.Instance.resolveAbility(key);
                action = ability;
            }

            boolean isToggle = ability != null && ability.isToggleable();
            if (subTab == 0 && isToggle) continue;
            if (subTab == 1 && !isToggle) continue;

            String displayName;
            if (action instanceof ChainedAbility) {
                displayName = "\u00A76\u2726 " + ((ChainedAbility) action).getDisplayName();
            } else if (ability != null) {
                displayName = ability.getDisplayName();
            } else {
                displayName = key;
            }

            tempKeys.add(key);
            tempNames.add(displayName);
            tempIcons.add(AbilityIcon.fromAction(action));
            tempAbilities.add(ability);
            tempActions.add(action);
        }

        // Sort: toggles tab sorts enabled first, then alphabetically
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < tempNames.size(); i++) indices.add(i);
        final boolean sortTogglesFirst = (subTab == 1);
        Collections.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if (sortTogglesFirst && playerData != null && playerData.abilityData != null) {
                    boolean aToggled = playerData.abilityData.isAbilityToggled(tempKeys.get(a));
                    boolean bToggled = playerData.abilityData.isAbilityToggled(tempKeys.get(b));
                    if (aToggled != bToggled) return aToggled ? -1 : 1;
                }
                return stripFormatting(tempNames.get(a)).compareToIgnoreCase(stripFormatting(tempNames.get(b)));
            }
        });

        for (int idx : indices) {
            filteredKeys.add(tempKeys.get(idx));
            filteredDisplayNames.add(tempNames.get(idx));
            filteredIcons.add(tempIcons.get(idx));
            filteredAbilities.add(tempAbilities.get(idx));
            filteredActions.add(tempActions.get(idx));
        }

        gridHoverScale = new float[filteredKeys.size()];
        Arrays.fill(gridHoverScale, 1.0f);
        scrollRow = 0;
        selectedIndex = -1;
    }

    private String stripFormatting(String s) {
        return s.replaceAll("\u00A7.", "").trim();
    }

    private void updateHotbarIcons() {
        PlayerData playerData = ClientCacheHandler.playerData;
        for (int i = 0; i < hbSlots; i++) {
            hotbarIcons[i] = null;
            if (playerData == null || playerData.hotbarData == null) continue;
            AbilityHotbarData slotData = playerData.hotbarData.getSlot(i);
            if (slotData == null || slotData.isEmpty()) continue;

            if (AbilityController.Instance != null) {
                if (slotData.isChainKey()) {
                    ChainedAbility chain = AbilityController.Instance.resolveChainedAbility(slotData.getResolveKey());
                    if (chain != null) hotbarIcons[i] = AbilityIcon.fromChainedAbility(chain);
                } else {
                    Ability ability = AbilityController.Instance.resolveAbility(slotData.abilityKey);
                    if (ability != null) hotbarIcons[i] = AbilityIcon.fromAbility(ability);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Grid Positioning Helpers
    // ═══════════════════════════════════════════════════════════════════

    private int getGridVisibleRows() {
        int usableHeight = (selBottom - selTop) - gridPad * 2;
        return usableHeight / gridCellSize;
    }

    private int getGridStartX() {
        int panelWidth = selRight - selLeft;
        int usableWidth = panelWidth - gridPad * 2;
        int gridWidth = gridCols * gridCellSize;
        int offsetX = (usableWidth - gridWidth) / 2;
        return guiLeft + selLeft + gridPad + offsetX;
    }

    private int getGridStartY() {
        int panelHeight = selBottom - selTop;
        int usableHeight = panelHeight - gridPad * 2;
        int visibleRows = getGridVisibleRows();
        int gridHeight = visibleRows * gridCellSize;
        int offsetY = (usableHeight - gridHeight) / 2;
        return guiTop + selTop + gridPad + offsetY;
    }

    private int getMaxScrollRow() {
        int totalRows = (filteredKeys.size() + gridCols - 1) / gridCols;
        return Math.max(0, totalRows - getGridVisibleRows());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Drawing
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 252, 195);
        drawTexturedModalRect(guiLeft + 252, guiTop, 188, 0, 67, 195);
        super.drawScreen(mouseX, mouseY, partialTicks);

        updateHoverState(mouseX, mouseY);
        updateHoverScales();

        // Panel backgrounds
        drawGradientRect(guiLeft + selLeft, guiTop + selTop, guiLeft + selRight, guiTop + selBottom, 0xC0101010, 0xC0101010);
        drawGradientRect(guiLeft + infoLeft, guiTop + infoTop, guiLeft + infoRight, guiTop + infoBottom, 0xC0101010, 0xC0101010);
        drawGradientRect(guiLeft + hbLeft, guiTop + hbTop, guiLeft + hbRight, guiTop + hbBottom, 0xC0101010, 0xC0101010);

        // RIGHT panel: Selection grid (with scissor clipping)
        drawIconGrid(mouseX, mouseY);

        // LEFT panel: Ability information
        drawInfoPanel();

        // BOTTOM panel: Hotbar
        drawHotbarSection(mouseX, mouseY);

        // Drag ghost
        if (isDragging && draggedIcon != null) {
            drawDragGhost(mouseX, mouseY);
        }

        // Tooltip (last - renders on top)
        drawTooltip(mouseX, mouseY);
    }

    private void drawIconGrid(int mouseX, int mouseY) {
        int visibleRows = getGridVisibleRows();
        int gridStartX = getGridStartX();
        int gridStartY = getGridStartY();
        int startIndex = scrollRow * gridCols;

        // GL Scissor to clip grid content within the selection panel
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();
        int scissorX = (guiLeft + selLeft) * scaleFactor;
        int scissorY = mc.displayHeight - (guiTop + selBottom) * scaleFactor;
        int scissorW = (selRight - selLeft) * scaleFactor;
        int scissorH = (selBottom - selTop) * scaleFactor;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        int maxVisibleCount = gridCols * visibleRows;
        for (int vi = 0; vi < maxVisibleCount; vi++) {
            int absIndex = startIndex + vi;
            if (absIndex >= filteredKeys.size()) break;

            int col = vi % gridCols;
            int row = vi / gridCols;
            int cellX = gridStartX + col * gridCellSize;
            int cellY = gridStartY + row * gridCellSize;

            boolean isHovered = (hoveredGridIndex == absIndex) && !isDragging;
            boolean isSelected = (selectedIndex == absIndex);
            boolean isDragSource = isDragging && dragSourceSlot == -1 && draggedKey != null
                && draggedKey.equals(filteredKeys.get(absIndex));

            // Cell background
            int bgColor;
            if (isDragSource) bgColor = 0x40333333;
            else if (isSelected) bgColor = 0x60505070;
            else if (isHovered) bgColor = 0x60404060;
            else bgColor = 0x40202030;
            drawRect(cellX, cellY, cellX + gridCellSize - 1, cellY + gridCellSize - 1, bgColor);

            // Cell border
            int borderColor;
            if (isSelected) borderColor = 0xAA9999DD;
            else if (isHovered) borderColor = 0xAA8888CC;
            else borderColor = 0x40404060;
            drawHorizontalLine(cellX, cellX + gridCellSize - 2, cellY, borderColor);
            drawHorizontalLine(cellX, cellX + gridCellSize - 2, cellY + gridCellSize - 2, borderColor);
            drawVerticalLine(cellX, cellY, cellY + gridCellSize - 2, borderColor);
            drawVerticalLine(cellX + gridCellSize - 2, cellY, cellY + gridCellSize - 2, borderColor);

            GL11.glColor4f(1, 1, 1, 1);

            // Icon (state-aware for multi-state toggles)
            AbilityIcon icon = filteredIcons.get(absIndex);
            if (icon != null) {
                float hoverScale = gridHoverScale[absIndex];
                float targetPixels = (gridCellSize - 4) * hoverScale;
                float drawSize = icon.getDrawSize();
                float iconScale = targetPixels / drawSize;

                int iconCenterX = cellX + (gridCellSize - 1) / 2;
                int iconCenterY = cellY + (gridCellSize - 1) / 2;

                int cellToggleState = 0;
                Ability cellAbility = filteredAbilities.get(absIndex);
                PlayerData cellPlayerData = ClientCacheHandler.playerData;
                if (cellAbility != null && cellAbility.isToggleable() && cellPlayerData != null && cellPlayerData.abilityData != null) {
                    cellToggleState = cellPlayerData.abilityData.getToggleState(filteredKeys.get(absIndex));
                }

                GL11.glPushMatrix();
                GL11.glTranslatef(iconCenterX, iconCenterY, 0);
                GL11.glScalef(iconScale, iconScale, 1);

                float alpha = isDragSource ? 0.3f : 1.0f;
                GL11.glColor4f(1, 1, 1, alpha);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                icon.draw(cellToggleState);
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glPopMatrix();
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Restore GL state
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);

        // Scroll indicator (drawn outside scissor)
        int totalRows = (filteredKeys.size() + gridCols - 1) / gridCols;
        if (totalRows > visibleRows) {
            FontRenderer fr = mc.fontRenderer;
            String scrollText = (scrollRow + 1) + "/" + (totalRows - visibleRows + 1);
            int textX = guiLeft + selRight - fr.getStringWidth(scrollText) - 4;
            int textY = guiTop + selBottom + 1;
            fr.drawStringWithShadow(scrollText, textX, textY, 0x888888);
        }
    }

    private void drawInfoPanel() {
        int infoIndex = hoveredGridIndex >= 0 ? hoveredGridIndex : selectedIndex;
        if (infoIndex < 0 || infoIndex >= filteredKeys.size()) return;

        // Reset scroll when ability changes
        if (infoIndex != lastInfoIndex) {
            infoScrollOffset = 0;
            lastInfoIndex = infoIndex;
        }

        FontRenderer fr = mc.fontRenderer;
        int panelInnerLeft = guiLeft + infoLeft + infoPad;
        int panelInnerTop = guiTop + infoTop + infoPad;
        int panelInnerWidth = (infoRight - infoLeft) - infoPad * 2;
        int panelHeight = infoBottom - infoTop;

        // Cached icon for detail preview
        if (cachedDetailIndex != infoIndex) {
            cachedDetailIcon = filteredIcons.get(infoIndex);
            cachedDetailIndex = infoIndex;
        }

        Ability ability = filteredAbilities.get(infoIndex);

        // Get toggle state for icon rendering and state display
        int toggleState = 0;
        PlayerData togglePlayerData = ClientCacheHandler.playerData;
        if (ability != null && ability.isToggleable() && togglePlayerData != null && togglePlayerData.abilityData != null) {
            toggleState = togglePlayerData.abilityData.getToggleState(filteredKeys.get(infoIndex));
        }

        // GL Scissor to clip info panel content
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();
        int scissorX = (guiLeft + infoLeft) * scaleFactor;
        int scissorY = mc.displayHeight - (guiTop + infoBottom) * scaleFactor;
        int scissorW = (infoRight - infoLeft) * scaleFactor;
        int scissorH = panelHeight * scaleFactor;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        int y = panelInnerTop - infoScrollOffset;

        // Preview icon (centered, state-aware for multi-state toggles)
        if (cachedDetailIcon != null) {
            float drawSize = cachedDetailIcon.getDrawSize();
            float iconScale = (float) infoIconSize / drawSize;

            int iconCX = panelInnerLeft + panelInnerWidth / 2;
            int iconCY = y + infoIconSize / 2 + 4;

            GL11.glPushMatrix();
            GL11.glTranslatef(iconCX, iconCY, 0);
            GL11.glScalef(iconScale, iconScale, 1);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1, 1, 1, 1);
            cachedDetailIcon.draw(toggleState);
            GL11.glPopMatrix();

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);

            y += infoIconSize + 12;
        }

        // Ability name (centered, wrapped)
        String name = filteredDisplayNames.get(infoIndex);
        List<String> nameLines = fr.listFormattedStringToWidth(name, panelInnerWidth);
        for (String line : nameLines) {
            int lineX = panelInnerLeft + (panelInnerWidth - fr.getStringWidth(line)) / 2;
            fr.drawStringWithShadow(line, lineX, y, 0xFFFFFF);
            y += fr.FONT_HEIGHT + 1;
        }
        y += 4;

        if (ability != null) {
            // Type info
            String typeId = ability.getTypeId();
            if (typeId != null && !typeId.isEmpty()) {
                String translated = StatCollector.translateToLocal(typeId);
                if (!translated.equals(typeId)) {
                    int textW = fr.getStringWidth(translated);
                    fr.drawStringWithShadow("\u00A77" + translated, panelInnerLeft + (panelInnerWidth - textW) / 2, y, 0xAAAAAA);
                    y += fr.FONT_HEIGHT + 3;
                }
            }

            // Toggle info
            if (ability.isToggleable()) {
                String toggleText = "[" + StatCollector.translateToLocal("gui.toggle") + "]";
                int textW = fr.getStringWidth(toggleText);
                fr.drawStringWithShadow("\u00A7a" + toggleText, panelInnerLeft + (panelInnerWidth - textW) / 2, y, 0x55FF55);
                y += fr.FONT_HEIGHT + 3;
            }

            // Toggle state
            if (ability.isToggleable()) {
                String stateText;
                String prefix;
                if (toggleState > 0) {
                    String stateLabel = ability.getToggleStateLabel(toggleState);
                    stateText = stateLabel != null ? stateLabel : StatCollector.translateToLocal("gui.toggle.active");
                    prefix = "\u00A72";
                } else {
                    stateText = StatCollector.translateToLocal("gui.toggle.inactive");
                    prefix = "\u00A78";
                }
                int textW = fr.getStringWidth(stateText);
                fr.drawStringWithShadow(prefix + stateText, panelInnerLeft + (panelInnerWidth - textW) / 2, y, toggleState > 0 ? 0x55FF55 : 0x888888);
                y += fr.FONT_HEIGHT + 3;
            }

            // Cooldown info — yellow if per-ability cooldown
            if (ability.getCooldownTicks() > 0) {
                float seconds = ability.getCooldownTicks() / 20.0f;
                String cdText = StatCollector.translateToLocal("ability.cooldown") + ": " + String.format("%.1f", seconds) + "s";
                int textW2 = fr.getStringWidth(cdText);
                boolean perAbility = ability.isPerAbilityCooldown();
                String cdColor = perAbility ? "\u00A7e" : "\u00A77";
                int cdColorInt = perAbility ? 0xFFFF55 : 0xAAAAAA;
                fr.drawStringWithShadow(cdColor + cdText, panelInnerLeft + (panelInnerWidth - textW2) / 2, y, cdColorInt);
                y += fr.FONT_HEIGHT + 3;

                if (perAbility) {
                    String perText = "[" + StatCollector.translateToLocal("ability.perAbilityCooldown") + "]";
                    int ptW = fr.getStringWidth(perText);
                    fr.drawStringWithShadow("\u00A7e" + perText, panelInnerLeft + (panelInnerWidth - ptW) / 2, y, 0xFFFF55);
                    y += fr.FONT_HEIGHT + 3;
                }
            }

            // Damage info
            float baseDamage = ability.getDisplayDamage();
            if (baseDamage > 0) {
                float displayDamage = baseDamage;
                if (AbilityController.Instance != null && mc.thePlayer != null) {
                    displayDamage = AbilityController.Instance.fireModifyProjectileDamage(ability, mc.thePlayer, baseDamage);
                }
                String label = ability.isDisplayDamageDPS()
                    ? StatCollector.translateToLocal("gui.dps")
                    : StatCollector.translateToLocal("ability.preview.damage");
                String dmgText = label + ": " + String.format("%.1f", displayDamage);
                int textW = fr.getStringWidth(dmgText);
                fr.drawStringWithShadow("\u00A7c" + dmgText, panelInnerLeft + (panelInnerWidth - textW) / 2, y, 0xFF5555);
                y += fr.FONT_HEIGHT + 3;
            }

            // Barrier health
            float baseHealth = ability.getDisplayBarrierHealth();
            if (baseHealth > 0) {
                float displayHealth = baseHealth;
                if (AbilityController.Instance != null && mc.thePlayer != null) {
                    displayHealth = AbilityController.Instance.fireModifyBarrierHealth(ability, mc.thePlayer, baseHealth);
                }
                String healthText = StatCollector.translateToLocal("ability.preview.health") + ": " + String.format("%.0f", displayHealth);
                int textW = fr.getStringWidth(healthText);
                fr.drawStringWithShadow("\u00A7a" + healthText, panelInnerLeft + (panelInnerWidth - textW) / 2, y, 0x55FF55);
                y += fr.FONT_HEIGHT + 3;
            }

            // Barrier reflection
            if (ability.isDisplayReflect()) {
                String reflectText = StatCollector.translateToLocal("ability.reflect") + ": " + String.format("%.0f%%", ability.getDisplayReflectStrength());
                int textW = fr.getStringWidth(reflectText);
                fr.drawStringWithShadow("\u00A7d" + reflectText, panelInnerLeft + (panelInnerWidth - textW) / 2, y, 0xFF55FF);
                y += fr.FONT_HEIGHT + 3;
            }

            // Barrier absorbing
            if (ability.isDisplayAbsorbing()) {
                String absorbText = "[" + StatCollector.translateToLocal("ability.absorbing") + "]";
                int textW = fr.getStringWidth(absorbText);
                fr.drawStringWithShadow("\u00A7b" + absorbText, panelInnerLeft + (panelInnerWidth - textW) / 2, y, 0x55FFFF);
                y += fr.FONT_HEIGHT + 3;
            }

            // Magic info
            if (ability.hasMagic()) {
                y += 2;
                MagicData magicData = ability.getMagicData();
                if (magicData.isEmpty()) {
                    String magicText = StatCollector.translateToLocal("ability.preview.magic.casters");
                    int textW = fr.getStringWidth(magicText);
                    fr.drawStringWithShadow("\u00A79" + magicText, panelInnerLeft + (panelInnerWidth - textW) / 2, y, 0x5555FF);
                    y += fr.FONT_HEIGHT + 3;
                } else {
                    for (Map.Entry<Integer, MagicEntry> entry : magicData.getMagics().entrySet()) {
                        Magic magic = MagicController.getInstance() != null
                            ? MagicController.getInstance().getMagic(entry.getKey()) : null;
                        String magicName = magic != null ? magic.getDisplayName() : "Magic #" + entry.getKey();
                        String splitText = magicName + ": " + Math.round(entry.getValue().split * 100) + "%";
                        int textW = fr.getStringWidth(splitText);
                        fr.drawStringWithShadow("\u00A79" + splitText, panelInnerLeft + (panelInnerWidth - textW) / 2, y, 0x5555FF);
                        y += fr.FONT_HEIGHT + 1;
                    }
                    y += 2;
                }
            }
        }

        // Track content height for scroll limits
        infoContentHeight = (y + infoScrollOffset) - panelInnerTop + infoPad * 2;

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Scroll indicator
        if (infoContentHeight > panelHeight) {
            int maxScroll = Math.max(1, infoContentHeight - panelHeight);
            if (infoScrollOffset > maxScroll) infoScrollOffset = maxScroll;
            float scrollPercent = (float) infoScrollOffset / maxScroll;
            int barHeight = panelHeight - 4;
            int thumbHeight = Math.max(8, barHeight * panelHeight / infoContentHeight);
            int thumbY = guiTop + infoTop + 2 + (int) ((barHeight - thumbHeight) * scrollPercent);
            int barX = guiLeft + infoRight - 3;
            drawRect(barX, guiTop + infoTop + 2, barX + 2, guiTop + infoTop + 2 + barHeight, 0x40FFFFFF);
            drawRect(barX, thumbY, barX + 2, thumbY + thumbHeight, 0xA0FFFFFF);
        }
    }

    private void drawHotbarSection(int mouseX, int mouseY) {
        FontRenderer fr = mc.fontRenderer;

        // Get selected ability key for yellow border
        PlayerData selPlayerData = ClientCacheHandler.playerData;
        String selectedKey = null;
        if (selPlayerData != null && selPlayerData.abilityData != null) {
            selectedKey = selPlayerData.abilityData.getSelectedAbilityKey();
        }

        // Hotbar slots centered within the panel
        int panelWidth = hbRight - hbLeft;
        int panelHeight = hbBottom - hbTop;
        int totalSlotsWidth = hbSlots * (hbSlotSize + hbSlotGap) - hbSlotGap;
        int hotbarStartX = guiLeft + hbLeft + (panelWidth - totalSlotsWidth) / 2;
        int hotbarStartY = guiTop + hbTop + (panelHeight - hbSlotSize) / 2;

        for (int i = 0; i < hbSlots; i++) {
            int sx = hotbarStartX + i * (hbSlotSize + hbSlotGap);
            int sy = hotbarStartY;

            boolean isHovered = (hoveredSlotIndex == i);
            boolean isDragHover = isDragging && isHovered;

            // Check if this slot's ability is the currently selected one
            boolean isSelectedSlot = false;
            if (selectedKey != null && !selectedKey.isEmpty() && selPlayerData.hotbarData != null) {
                AbilityHotbarData slotData = selPlayerData.hotbarData.getSlot(i);
                if (slotData != null && !slotData.isEmpty() && selectedKey.equals(slotData.abilityKey)) {
                    isSelectedSlot = true;
                }
            }

            // Slot background
            int bgColor = isDragHover ? 0x80606030 : (isSelectedSlot ? 0x60505020 : (isHovered ? 0x60505050 : 0x50303030));
            drawRect(sx, sy, sx + hbSlotSize, sy + hbSlotSize, bgColor);

            // Slot border - yellow for selected, drag color, hover color, or default
            int borderColor;
            if (isDragHover) borderColor = 0xFFCCCC44;
            else if (isSelectedSlot) borderColor = 0xFFCCCC00;
            else if (isHovered) borderColor = 0xAA888888;
            else borderColor = 0x60505050;
            drawHorizontalLine(sx, sx + hbSlotSize - 1, sy, borderColor);
            drawHorizontalLine(sx, sx + hbSlotSize - 1, sy + hbSlotSize - 1, borderColor);
            drawVerticalLine(sx, sy, sy + hbSlotSize - 1, borderColor);
            drawVerticalLine(sx + hbSlotSize - 1, sy, sy + hbSlotSize - 1, borderColor);

            GL11.glColor4f(1, 1, 1, 1);

            // Icon or slot number
            AbilityIcon icon = hotbarIcons[i];
            boolean isSourceSlot = isDragging && dragSourceSlot == i;

            if (icon != null && !isSourceSlot) {
                int iconCX = sx + hbSlotSize / 2;
                int iconCY = sy + hbSlotSize / 2;
                float targetPixels = hbSlotSize - 4;
                float drawSize = icon.getDrawSize();
                float s = targetPixels / drawSize;

                // Compute toggle state for this hotbar slot's ability
                int hbToggleState = 0;
                if (selPlayerData != null && selPlayerData.abilityData != null && selPlayerData.hotbarData != null) {
                    AbilityHotbarData slotData = selPlayerData.hotbarData.getSlot(i);
                    if (slotData != null && !slotData.isEmpty()) {
                        hbToggleState = selPlayerData.abilityData.getToggleState(slotData.abilityKey);
                    }
                }

                GL11.glPushMatrix();
                GL11.glTranslatef(iconCX, iconCY, 0);
                GL11.glScalef(s, s, 1);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                icon.draw(hbToggleState);
                GL11.glPopMatrix();
                GL11.glColor4f(1, 1, 1, 1);
            } else {
                String slotLabel = String.valueOf(i + 1);
                int labelColor = isSourceSlot ? 0x333333 : 0x666666;
                int lx = sx + (hbSlotSize - fr.getStringWidth(slotLabel)) / 2;
                int ly = sy + (hbSlotSize - fr.FONT_HEIGHT) / 2 + 1;
                fr.drawString(slotLabel, lx, ly, labelColor);
            }
        }

        // Restore GL
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private void drawDragGhost(int mouseX, int mouseY) {
        if (draggedIcon == null) return;

        float targetPixels = gridCellSize;
        float drawSize = draggedIcon.getDrawSize();
        float ghostScale = targetPixels / drawSize;

        GL11.glPushMatrix();
        GL11.glTranslatef(mouseX, mouseY, 100);
        GL11.glScalef(ghostScale, ghostScale, 1);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 0.8f);
        draggedIcon.draw(0);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private void drawTooltip(int mouseX, int mouseY) {
        if (isDragging) return;

        if (hoveredGridIndex >= 0 && hoveredGridIndex < filteredDisplayNames.size()) {
            List<String> lines = new ArrayList<>();
            lines.add(filteredDisplayNames.get(hoveredGridIndex));

            Ability ability = filteredAbilities.get(hoveredGridIndex);
            if (ability != null) {
                String typeId = ability.getTypeId();
                if (typeId != null && !typeId.isEmpty()) {
                    String translated = StatCollector.translateToLocal(typeId);
                    if (!translated.equals(typeId)) {
                        lines.add("\u00A77" + translated);
                    }
                }
                if (ability.isToggleable()) {
                    lines.add("\u00A7a[" + StatCollector.translateToLocal("gui.toggle") + "]");
                }
                if (ability.getCooldownTicks() > 0) {
                    float seconds = ability.getCooldownTicks() / 20.0f;
                    lines.add("\u00A77" + StatCollector.translateToLocal("ability.cooldown") + ": " + String.format("%.1f", seconds) + "s");
                }
            }

            drawHoveringText(lines, mouseX, mouseY, mc.fontRenderer);
            GL11.glDisable(GL11.GL_LIGHTING);
        }

        if (hoveredSlotIndex >= 0 && hoveredSlotIndex < hbSlots) {
            PlayerData playerData = ClientCacheHandler.playerData;
            if (playerData != null && playerData.hotbarData != null) {
                AbilityHotbarData slotData = playerData.hotbarData.getSlot(hoveredSlotIndex);
                if (slotData != null && !slotData.isEmpty()) {
                    String name = getHotbarSlotDisplayName(slotData);
                    if (name != null) {
                        List<String> lines = new ArrayList<>();
                        lines.add(name);
                        lines.add("\u00A78Right-click to remove");
                        drawHoveringText(lines, mouseX, mouseY, mc.fontRenderer);
                        GL11.glDisable(GL11.GL_LIGHTING);
                    }
                }
            }
        }
    }

    private String getHotbarSlotDisplayName(AbilityHotbarData slotData) {
        if (AbilityController.Instance == null) return null;
        if (slotData.isChainKey()) {
            ChainedAbility chain = AbilityController.Instance.resolveChainedAbility(slotData.getResolveKey());
            return chain != null ? chain.getDisplayName() : null;
        }
        Ability ability = AbilityController.Instance.resolveAbility(slotData.abilityKey);
        return ability != null ? ability.getDisplayName() : null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Hover & Animation
    // ═══════════════════════════════════════════════════════════════════

    private void updateHoverState(int mouseX, int mouseY) {
        hoveredGridIndex = -1;
        hoveredSlotIndex = -1;

        // Grid hit test (RIGHT panel - selection)
        int visibleRows = getGridVisibleRows();
        int gridStartX = getGridStartX();
        int gridStartY = getGridStartY();
        int startIndex = scrollRow * gridCols;

        if (mouseX >= guiLeft + selLeft && mouseX < guiLeft + selRight
            && mouseY >= guiTop + selTop && mouseY < guiTop + selBottom) {
            for (int vi = 0; vi < gridCols * visibleRows; vi++) {
                int absIndex = startIndex + vi;
                if (absIndex >= filteredKeys.size()) break;

                int col = vi % gridCols;
                int row = vi / gridCols;
                int cellX = gridStartX + col * gridCellSize;
                int cellY = gridStartY + row * gridCellSize;

                if (mouseX >= cellX && mouseX < cellX + gridCellSize - 1
                    && mouseY >= cellY && mouseY < cellY + gridCellSize - 1) {
                    hoveredGridIndex = absIndex;
                    break;
                }
            }
        }

        // Hotbar hit test
        int panelWidth = hbRight - hbLeft;
        int panelHeight = hbBottom - hbTop;
        int totalSlotsWidth = hbSlots * (hbSlotSize + hbSlotGap) - hbSlotGap;
        int hotbarStartX = guiLeft + hbLeft + (panelWidth - totalSlotsWidth) / 2;
        int hotbarStartY = guiTop + hbTop + (panelHeight - hbSlotSize) / 2;

        for (int i = 0; i < hbSlots; i++) {
            int sx = hotbarStartX + i * (hbSlotSize + hbSlotGap);
            if (mouseX >= sx && mouseX < sx + hbSlotSize
                && mouseY >= hotbarStartY && mouseY < hotbarStartY + hbSlotSize) {
                hoveredSlotIndex = i;
                break;
            }
        }
    }

    private void updateHoverScales() {
        if (gridHoverScale == null) return;
        for (int i = 0; i < gridHoverScale.length; i++) {
            float target = (i == hoveredGridIndex && !isDragging) ? 1.12f : 1.0f;
            gridHoverScale[i] += (target - gridHoverScale[i]) * 0.3f;
            if (Math.abs(gridHoverScale[i] - target) < 0.005f) {
                gridHoverScale[i] = target;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Mouse Handlers
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hasSubGui()) {
            super.mouseClicked(mouseX, mouseY, button);
            return;
        }

        // Right-click on hotbar slot -> clear
        if (button == 1 && hoveredSlotIndex >= 0) {
            saveHotbarSlot(hoveredSlotIndex, "");
            updateHotbarIcons();
            return;
        }

        // Left-click: start potential drag or select
        if (button == 0) {
            if (hoveredGridIndex >= 0 && hoveredGridIndex < filteredKeys.size()) {
                // Toggle double-tap detection on toggle tab
                if (subTab == 1) {
                    long now = System.currentTimeMillis();
                    if (lastToggleClickIndex == hoveredGridIndex && (now - lastToggleClickTime) < 400) {
                        String key = filteredKeys.get(hoveredGridIndex);
                        Ability ability = filteredAbilities.get(hoveredGridIndex);
                        if (ability != null && ability.isToggleable()) {
                            PacketHandler.Instance.sendToServer(new AbilityTogglePacket(key));
                            lastToggleClickIndex = -1;
                            lastToggleClickTime = 0;
                            // Rebuild list after a short delay to reflect new state
                            // For now just select and return - the list rebuilds on next initGui
                            selectedIndex = hoveredGridIndex;
                            cachedDetailIcon = null;
                            cachedDetailIndex = -1;
                            return;
                        }
                    }
                    lastToggleClickIndex = hoveredGridIndex;
                    lastToggleClickTime = now;
                }

                selectedIndex = hoveredGridIndex;
                cachedDetailIcon = null;
                cachedDetailIndex = -1;
                dragPending = true;
                draggedKey = filteredKeys.get(hoveredGridIndex);
                draggedIcon = filteredIcons.get(hoveredGridIndex);
                dragSourceSlot = -1;
                dragStartX = mouseX;
                dragStartY = mouseY;
                return;
            }

            if (hoveredSlotIndex >= 0 && hotbarIcons[hoveredSlotIndex] != null) {
                PlayerData playerData = ClientCacheHandler.playerData;
                if (playerData != null && playerData.hotbarData != null) {
                    AbilityHotbarData slotData = playerData.hotbarData.getSlot(hoveredSlotIndex);
                    if (slotData != null && !slotData.isEmpty()) {
                        dragPending = true;
                        draggedKey = slotData.abilityKey;
                        draggedIcon = hotbarIcons[hoveredSlotIndex];
                        dragSourceSlot = hoveredSlotIndex;
                        dragStartX = mouseX;
                        dragStartY = mouseY;
                        return;
                    }
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        if (dragPending && !isDragging) {
            int dx = mouseX - dragStartX;
            int dy = mouseY - dragStartY;
            if (dx * dx + dy * dy > dragThreshold * dragThreshold) {
                isDragging = true;
            }
        }
        if (isDragging) {
            dragMouseX = mouseX;
            dragMouseY = mouseY;
        }

        super.mouseClickMove(mouseX, mouseY, button, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (isDragging && draggedKey != null) {
            updateHoverState(mouseX, mouseY);

            if (hoveredSlotIndex >= 0) {
                if (dragSourceSlot >= 0) {
                    swapHotbarSlots(dragSourceSlot, hoveredSlotIndex);
                } else {
                    saveHotbarSlot(hoveredSlotIndex, draggedKey);
                }
                updateHotbarIcons();
            } else if (dragSourceSlot >= 0 && hoveredGridIndex >= 0) {
                saveHotbarSlot(dragSourceSlot, "");
                updateHotbarIcons();
            }
        }

        isDragging = false;
        dragPending = false;
        draggedKey = null;
        draggedIcon = null;
        dragSourceSlot = -1;

        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

            if (mouseX >= guiLeft + infoLeft && mouseX < guiLeft + infoRight
                && mouseY >= guiTop + infoTop && mouseY < guiTop + infoBottom) {
                // Info panel scroll
                int panelHeight = infoBottom - infoTop;
                int maxInfoScroll = Math.max(0, infoContentHeight - panelHeight);
                if (wheel > 0) {
                    infoScrollOffset = Math.max(0, infoScrollOffset - 10);
                } else {
                    infoScrollOffset = Math.min(maxInfoScroll, infoScrollOffset + 10);
                }
            } else {
                int maxScroll = getMaxScrollRow();
                if (wheel > 0) {
                    scrollRow = Math.max(0, scrollRow - 1);
                } else {
                    scrollRow = Math.min(maxScroll, scrollRow + 1);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Actions & Network
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn instanceof AbstractTab) return;

        if (btn.id <= -100 && btn.id > -200) {
            super.actionPerformed(btn);
            return;
        }

        if (btn.id == BTN_TAB_ABILITIES) {
            subTab = 0;
            initGui();
            return;
        }
        if (btn.id == BTN_TAB_TOGGLES) {
            subTab = 1;
            initGui();
            return;
        }
    }

    private void saveHotbarSlot(int slotIndex, String abilityKey) {
        PlayerData playerData = ClientCacheHandler.playerData;
        if (playerData != null && playerData.hotbarData != null) {
            AbilityHotbarData slot = playerData.hotbarData.getSlot(slotIndex);
            if (slot != null) {
                slot.abilityKey = abilityKey;
            }
        }

        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound slotTag = new NBTTagCompound();
        slotTag.setInteger("slot", slotIndex);
        slotTag.setString("abilityKey", abilityKey != null ? abilityKey : "");
        compound.setTag("AbilityHotbar" + slotIndex, slotTag);
        PacketHandler.Instance.sendToServer(new AbilityHotbarSavePacket(slotIndex, compound));
    }

    private void swapHotbarSlots(int fromSlot, int toSlot) {
        if (fromSlot == toSlot) return;

        PlayerData playerData = ClientCacheHandler.playerData;
        if (playerData == null || playerData.hotbarData == null) return;

        AbilityHotbarData fromData = playerData.hotbarData.getSlot(fromSlot);
        AbilityHotbarData toData = playerData.hotbarData.getSlot(toSlot);
        if (fromData == null || toData == null) return;

        String fromKey = fromData.abilityKey;
        String toKey = toData.abilityKey;

        saveHotbarSlot(fromSlot, toKey != null ? toKey : "");
        saveHotbarSlot(toSlot, fromKey != null ? fromKey : "");
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Send selection sync if changed
        PlayerData playerData = ClientCacheHandler.playerData;
        if (playerData != null && playerData.abilityData != null) {
            String currentKey = playerData.abilityData.getSelectedAbilityKey();
            boolean changed = (initialSelectedKey == null && currentKey != null && !currentKey.isEmpty())
                || (initialSelectedKey != null && !initialSelectedKey.equals(currentKey));
            if (changed) {
                PacketHandler.Instance.sendToServer(new AbilityHotbarSelectPacket(currentKey != null ? currentKey : ""));
            }
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (i == 1 || isInventoryKey(i)) {
            close();
        }
    }

    @Override
    public void save() {
    }
}
