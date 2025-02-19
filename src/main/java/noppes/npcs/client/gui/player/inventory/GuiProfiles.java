package noppes.npcs.client.gui.player.inventory;

import kamkeel.npcs.controllers.data.InfoEntry;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.controllers.data.Slot;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.linked.LinkedNPCAddPacket;
import kamkeel.npcs.network.packets.request.profile.ProfileGetInfoPacket;
import kamkeel.npcs.network.packets.request.profile.ProfileGetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.AbstractTab;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GuiProfiles extends GuiCNPCInventory implements ISubGuiListener, ICustomScrollListener, IGuiData, GuiYesNoCallback {

	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/standardbg.png");
    private GuiCustomScroll scroll;
    public HashMap<String, Integer> data = new HashMap<>();
    private String selected = null;
    private Profile profile;
    private Slot slot;
    private HashMap<Integer, List<InfoEntry>> slotInfoMap = new HashMap<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private int scrollY = 0; // Vertical scrolling position
    private int maxScrollY = 0; // Maximum scroll limit

    private boolean draggingScrollbar = false; // Track if user is dragging the scrollbar
    private int lastMouseY = 0; // Last recorded mouse Y position when dragging

    public GuiProfiles() {
		super();
		xSize = 280;
		ySize = 180;
        this.drawDefaultBackground = false;
        title = "";
        PacketClient.sendClient(new ProfileGetPacket());
        PacketClient.sendClient(new ProfileGetInfoPacket());
	}

	@Override
    public void initGui()
    {
		super.initGui();

        int y = guiTop + 4;
        this.addButton(new GuiNpcButton(1,guiLeft + 4, y += 144, 60, 20, "gui.change"));
        this.addButton(new GuiNpcButton(2,guiLeft + 4 + 63, y, 60, 20, "gui.rename"));
        this.addButton(new GuiNpcButton(3,guiLeft + 4 + 63, y += 22, 60, 20, "gui.remove"));
        this.addButton(new GuiNpcButton(4,guiLeft + 4, y, 60, 20, "gui.create"));

        if(scroll == null){
            scroll = new GuiCustomScroll(this,0,0);
            scroll.setSize(123, 141);
        }
        scroll.guiLeft = guiLeft + 4;
        scroll.guiTop = guiTop + 4;
        scroll.setList(new ArrayList<String>(this.data.keySet()));
        this.addScroll(scroll);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        if(guibutton instanceof AbstractTab)
            return;

        if (guibutton.id <= -100) {
            super.actionPerformed(guibutton);
            return;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 252, 195);
        drawTexturedModalRect(guiLeft + 252, guiTop, 188, 0, 67, 195);

        super.drawScreen(mouseX, mouseY, f);
        if (hasSubGui()) return;

        renderScrollableScreen(mouseX, mouseY);
    }

    /**
     * Renders the scrollable area inside the background box.
     */
    private void renderScrollableScreen(int mouseX, int mouseY) {
        drawGradientRect(guiLeft + 133, guiTop + 4, guiLeft + xSize + 33, guiTop + 24, 0xC0101010, 0xC0101010);
        drawHorizontalLine(guiLeft + 133, guiLeft + xSize + 33, guiTop + 25, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
        drawGradientRect(guiLeft + 133, guiTop + 27, guiLeft + xSize + 33, guiTop + ySize + 9, 0xA0101010, 0xA0101010);

        if (this.profile != null && slot != null) {

            String topBarText = slot.getName();
            int textWidth = fontRendererObj.getStringWidth(topBarText);
            int barCenterX = guiLeft + 133 + ((xSize + 33 - 133) / 2);
            int textX = barCenterX - (textWidth / 2);
            fontRendererObj.drawString(topBarText, textX, guiTop + 10, 0xFFFFFF, true);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int scaleFactor = sr.getScaleFactor();

            // Define the SCISSOR RECTANGLE to restrict drawing to only within the background box
            int scissorX = (guiLeft + 133) * scaleFactor;
            int scissorY = (sr.getScaledHeight() - (guiTop + ySize + 6)) * scaleFactor;
            int scissorW = (xSize + 33 - 133) * scaleFactor;
            int scissorH = (ySize + 4 - 27) * scaleFactor; // The height from 27 to ySize+9
            GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

            GL11.glPushMatrix();
            GL11.glTranslatef(0, -scrollY, 0); // Apply Scroll to Info

            int spacing = 14;
            int y = guiTop + 30;
            int xLabel = guiLeft + 136;
            int boxRightX = guiLeft + xSize + 30;

            String label, result;
            int labelColor, resultColor, resultWidth, resultX;

            label = StatCollector.translateToLocal("profile.lastLoaded");
            result = String.valueOf(DATE_FORMAT.format(new Date(slot.getLastLoaded())));
            labelColor = 0xff9c2b;
            resultColor = 0xffdb59;
            fontRendererObj.drawString(label, xLabel, y, labelColor, false);
            resultWidth = fontRendererObj.getStringWidth(result);
            resultX = boxRightX - resultWidth - 5;
            fontRendererObj.drawString(result, resultX, y, resultColor, false);
            y += spacing;

            if(slot.isTemporary()){
                label = StatCollector.translateToLocal("profile.temporary");
                result = (String.valueOf(slot.isTemporary())).toUpperCase();
                labelColor = 0xfc60dd;
                resultColor = 0x35fc81;
                fontRendererObj.drawString(label, xLabel, y, labelColor, false);
                resultWidth = fontRendererObj.getStringWidth(result);
                resultX = boxRightX - resultWidth - 5;
                fontRendererObj.drawString(result, resultX, y, resultColor, false);
                y += spacing;
            }

            if (slotInfoMap.containsKey(slot.getId())) {
                List<InfoEntry> infoEntries = slotInfoMap.get(slot.getId());
                for (InfoEntry entry : infoEntries) {
                    label = StatCollector.translateToLocal(entry.getLabel());
                    result = StatCollector.translateToLocal(entry.getResult());
                    labelColor = entry.getLabelColor();
                    resultColor = entry.getResultColor();

                    // Draw Label
                    fontRendererObj.drawString(label, xLabel, y, labelColor, false);

                    // Calculate Result X Position (Right-Aligned)
                    resultWidth = fontRendererObj.getStringWidth(result);
                    resultX = boxRightX - resultWidth - 5;

                    // Draw Result at Right-Aligned Position
                    fontRendererObj.drawString(result, resultX, y, resultColor, false);

                    // Move to next line
                    y += spacing;
                }
            }

            maxScrollY = Math.max(0, y - (guiTop + ySize - 35));
        }

        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw scrollbar only if needed
        if (maxScrollY > 0) {
            drawScrollbar();
        }
    }

    /**
     * Handles mouse scroll input **only inside the background box**.
     */
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        if(isMouseOverScrollBox(mouseX, mouseY)){
            int delta = Mouse.getEventDWheel();
            if (delta != 0) {
                scrollY -= delta / 7;
                if (scrollY < 0) scrollY = 0;
                if (scrollY > maxScrollY) scrollY = maxScrollY;
            }
        }

        if (draggingScrollbar) {
            if (Mouse.isButtonDown(0)) {
                int mouseDiff = mouseY - lastMouseY;
                scrollY += (mouseDiff * maxScrollY) / (ySize - 70);
                scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
                lastMouseY = mouseY;
            } else {
                draggingScrollbar = false;
            }
        }
    }

    /**
     * Draws the vertical scrollbar (only if needed).
     */
    private void drawScrollbar() {
        int scrollbarX = guiLeft + xSize + 30;
        int scrollbarY = guiTop + 27;
        int scrollbarHeight = ySize - 18;
        int thumbHeight = Math.max(10, (scrollbarHeight * scrollbarHeight) / (maxScrollY + scrollbarHeight));
        int thumbY = scrollbarY + ((scrollY * (scrollbarHeight - thumbHeight)) / maxScrollY);

        drawRect(scrollbarX, scrollbarY, scrollbarX + 5, scrollbarY + scrollbarHeight, 0xFF333333);
        drawRect(scrollbarX, thumbY, scrollbarX + 5, thumbY + thumbHeight, 0xFFAAAAAA);
    }

    /**
     * Checks if the mouse is inside the background box for scrolling.
     */
    private boolean isMouseOverScrollBox(int mouseX, int mouseY) {
        return (mouseX >= guiLeft + 133 && mouseX <= guiLeft + xSize + 33 &&
            mouseY >= guiTop + 27 && mouseY <= guiTop + ySize + 9);
    }

    @Override
    public void keyTyped(char c, int i)
    {
        if (i == 1 || (!hasSubGui() && isInventoryKey(i)))
        {
            close();
        }
    }
	@Override
	public void save() {}

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result)
            return;
        if (id == 1) {
            if (data.containsKey(scroll.getSelected())) {

                // DELETE PROFILE
                // PacketClient.sendClient(new LinkedNPCRemovePacket(scroll.getSelected()));

                initGui();
            }
        }
        if(id == 1){
            if (data.containsKey(scroll.getSelected())) {

                // RENAME PROFILE
                // PacketClient.sendClient(new LinkedItemRemovePacket(data.get(scroll.getSelected())));

                initGui();
            }
        }
        if(id == 1){
            // CREATE PROFILE
            // PacketClient.sendClient(new LinkedItemRemovePacket(data.get(scroll.getSelected())));

            initGui();
        }
        if(id == 1){
            // CHANGE PROFILE
            // PacketClient.sendClient(new LinkedItemRemovePacket(data.get(scroll.getSelected())));

            initGui();
        }
    }

    public int getStringWidthWithoutColor(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§') {
                if (i < text.length() - 1) {
                    i += 1;
                }
            } else {
                // If not a color code, calculate the width
                width += Minecraft.getMinecraft().fontRenderer.getCharWidth(c);
            }
        }
        return width;
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if(subgui instanceof SubGuiEditText){
            if(!((SubGuiEditText)subgui).cancelled){
                PacketClient.sendClient(new LinkedNPCAddPacket(((SubGuiEditText)subgui).text));
            }
        }
    }

    public boolean isMouseOverRenderer(int x, int y) {
        return x >= guiLeft + 10 && x <= guiLeft + 10 + 200 && y >= guiTop + 6 && y <= guiTop + 6 + 204;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            slot = null;
            selected = scroll.getSelected();
            if (profile != null && selected != null && !selected.isEmpty()){
                for(Slot checkSlot : profile.slots.values()){
                    if(checkSlot.getId() == data.get(selected)){
                        slot = checkSlot;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {}

    public void setGuiData(NBTTagCompound compound) {
        if(compound.hasKey("PROFILE")){
            // Load Profile
            this.profile = new Profile(mc.thePlayer, compound);
            this.data = new HashMap<>();
            String colorCode = "\u00A7e";
            for(Slot slot1 : profile.slots.values()){
                String name = slot1.getId() + " - " + slot1.getName();
                if(profile.currentID == slot1.getId())
                    name = colorCode + name;
                this.data.put(name, slot1.getId());
            }
        } else if(compound.hasKey("PROFILE_INFO")){
            slotInfoMap = ProfileGetInfoPacket.readSlotsFromNBT(compound);
        }
        initGui();
    }

    /**
     * Handles mouse clicks for the scrollbar.
     */
    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (button == 0) { // Left mouse button
            if (isMouseOverScrollbar(mouseX, mouseY)) {
                int scrollbarY = guiTop + 27;
                int scrollbarHeight = ySize - 35;
                int thumbHeight = Math.max(10, (scrollbarHeight * scrollbarHeight) / (maxScrollY + scrollbarHeight));
                int thumbY = scrollbarY + ((scrollY * (scrollbarHeight - thumbHeight)) / maxScrollY);

                // If clicking on scrollbar thumb, start dragging
                if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                    draggingScrollbar = true;
                    lastMouseY = mouseY;
                } else {
                    // Click anywhere on scrollbar background to jump position
                    scrollY = ((mouseY - scrollbarY) * maxScrollY) / scrollbarHeight;
                    scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
                }
            }
        }
    }

    /**
     * Handles mouse dragging while clicking.
     */
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (draggingScrollbar) {
            int mouseDiff = mouseY - lastMouseY;
            scrollY += (mouseDiff * maxScrollY) / (ySize - 70);
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            lastMouseY = mouseY;
        }
    }

    /**
     * Handles mouse release.
     */
    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        if (state == 0) {
            draggingScrollbar = false;
        }
    }

    private boolean isMouseOverScrollbar(int mouseX, int mouseY) {
        int scrollbarX = guiLeft + xSize + 30;
        int scrollbarY = guiTop + 27;
        int scrollbarHeight = ySize - 35;
        return (mouseX >= scrollbarX && mouseX <= scrollbarX + 5 &&
            mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight);
    }
}
