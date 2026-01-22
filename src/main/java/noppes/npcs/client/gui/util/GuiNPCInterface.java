package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class GuiNPCInterface extends GuiScreen {
    public EntityClientPlayerMP player;
    public boolean drawDefaultBackground = true;
    public EntityNPCInterface npc;
    protected HashMap<Integer, GuiNpcButton> buttons = new HashMap<Integer, GuiNpcButton>();
    protected HashMap<Integer, GuiMenuTopButton> topbuttons = new HashMap<Integer, GuiMenuTopButton>();
    protected HashMap<Integer, GuiMenuSideButton> sidebuttons = new HashMap<Integer, GuiMenuSideButton>();
    protected HashMap<Integer, GuiNpcTextField> textfields = new HashMap<Integer, GuiNpcTextField>();
    protected HashMap<Integer, GuiNpcLabel> labels = new HashMap<Integer, GuiNpcLabel>();
    protected HashMap<Integer, GuiCustomScroll> scrolls = new HashMap<Integer, GuiCustomScroll>();
    protected HashMap<Integer, GuiNpcSlider> sliders = new HashMap<Integer, GuiNpcSlider>();
    protected HashMap<Integer, GuiScreen> extra = new HashMap<Integer, GuiScreen>();
    protected HashMap<Integer, GuiScrollWindow> scrollWindows = new HashMap<>();
    protected HashMap<Integer, GuiDiagram> diagrams = new HashMap<>();

    public static boolean resizingActive = false;

    public String title;
    private ResourceLocation background = null;
    public boolean closeOnEsc = false;
    public int guiLeft, guiTop, xSize, ySize;
    private SubGuiInterface subgui;
    public int mouseX, mouseY, mouseScroll;
    public float bgScale = 1;
    public float bgScaleX = 1;
    public float bgScaleY = 1;
    public float bgScaleZ = 1;

    public GuiNPCInterface(EntityNPCInterface npc) {
        this.player = Minecraft.getMinecraft().thePlayer;
        this.npc = npc;
        title = "";
        xSize = 200;
        ySize = 222;
    }

    public GuiNPCInterface() {
        this(null);
    }

    public void setBackground(String texture) {
        background = new ResourceLocation("customnpcs", "textures/gui/" + texture);
    }

    public ResourceLocation getResource(String texture) {
        return new ResourceLocation("customnpcs", "textures/gui/" + texture);
    }

    @Override
    public void initGui() {
        super.initGui();
        GuiNpcTextField.unfocus();
        if (subgui != null) {
            subgui.setWorldAndResolution(mc, width, height);
            subgui.initGui();
        }
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
        buttonList.clear();
        labels.clear();
        textfields.clear();
        buttons.clear();
        sidebuttons.clear();
        topbuttons.clear();
        scrolls.clear();
        sliders.clear();
        scrollWindows.clear();
        diagrams.clear();
        Keyboard.enableRepeatEvents(true);

        if (drawRenderButtons) {
            zoomIn = new GuiNpcButton(0, guiLeft + xOffsetNpc + xOffsetButton, guiTop + yOffsetNpc + yOffsetButton, 20, 20, "-");
            zoomOut = new GuiNpcButton(0, guiLeft + 22 + xOffsetNpc + xOffsetButton, guiTop + yOffsetNpc + yOffsetButton, 20, 20, "+");
            rotateLeft = new GuiNpcButton(0, guiLeft + 44 + xOffsetNpc + xOffsetButton, guiTop + yOffsetNpc + yOffsetButton, 20, 20, "<");
            rotateRight = new GuiNpcButton(0, guiLeft + 66 + xOffsetNpc + xOffsetButton, guiTop + yOffsetNpc + yOffsetButton, 20, 20, ">");
        }
    }

    @Override
    public void updateScreen() {
        if (subgui != null)
            subgui.updateScreen();
        else {
            for (GuiNpcTextField tf : textfields.values()) {
                if (tf.enabled)
                    tf.updateCursorCounter();
            }
            super.updateScreen();
        }
    }

    public void addExtra(GuiHoverText gui) {
        gui.setWorldAndResolution(mc, 350, 250);
        extra.put(gui.id, gui);
    }

    public void addScrollableGui(int id, GuiScrollWindow gui) {
        gui.setWorldAndResolution(mc, width, height);
        gui.initGui();
        scrollWindows.put(id, gui);
    }

    /**
     * Adds a GuiDiagram to the interface.
     * The diagram is initialized with the current resolution and cached.
     */
    public void addDiagram(int id, GuiDiagram diagram) {
        diagram.invalidateCache();
        diagrams.put(id, diagram);
    }

    public GuiDiagram getDiagram(int id) {
        return diagrams.get(id);
    }

    public void mouseClicked(int i, int j, int k) {
        if (subgui != null)
            subgui.mouseClicked(i, j, k);
        else {
            for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(textfields.values()))
                if (tf.enabled)
                    tf.mouseClicked(i, j, k);

            for (GuiScrollWindow guiScrollableComponent : scrollWindows.values()) {
                guiScrollableComponent.mouseClicked(i, j, k);
            }

            if (k == 0) {
                for (GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(scrolls.values())) {
                    scroll.mouseClicked(i, j, k);
                }
            }
            // Process diagram mouse clicks
            for (GuiDiagram diagram : diagrams.values()) {
                if (diagram.isWithin(i, j)) {
                    if (diagram.mouseClicked(i, j, k))
                        return;
                }
            }
            mouseEvent(i, j, k);
            vanillaMouseClicked(i, j, k);
        }
    }

    protected void vanillaMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 || mouseButton == 1) {
            for (int l = 0; l < this.buttonList.size(); ++l) {
                GuiButton guibutton = (GuiButton) this.buttonList.get(l);

                AtomicBoolean rightClicked = null;
                if (mouseButton == 1) {
                    if (guibutton instanceof GuiNpcButton && ((GuiNpcButton) guibutton).rightClickable) {
                        rightClicked = ((GuiNpcButton) guibutton).rightClicked;
                        rightClicked.set(true);
                    } else
                        continue;
                }

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
                    if (MinecraftForge.EVENT_BUS.post(event))
                        break;
                    this.selectedButton = event.button;
                    event.button.func_146113_a(this.mc.getSoundHandler());
                    this.actionPerformed(event.button);
                    if (this.equals(this.mc.currentScreen))
                        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, event.button, this.buttonList));
                }

                if (rightClicked != null)
                    rightClicked.set(false);
            }
        }
    }
    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (subgui != null) {
            subgui.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (subgui != null) {
            subgui.mouseMovedOrUp(mouseX, mouseY, state);
            return;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    public void mouseEvent(int i, int j, int k) {
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (subgui != null)
            subgui.buttonEvent(guibutton);
        else {
            buttonEvent(guibutton);
        }
    }

    public void buttonEvent(GuiButton guibutton) {
    }

    @Override
    public void keyTyped(char c, int i) {
        if (subgui != null)
            subgui.keyTyped(c, i);
        for (GuiNpcTextField tf : textfields.values())
            tf.textboxKeyTyped(c, i);

        for (GuiScrollWindow guiScrollableComponent : scrollWindows.values()) {
            guiScrollableComponent.keyTyped(c, i);
        }

        /*
         Fixes closing sub with escape closes all of its parents.
         The outermost GUI (root of all subs) closes the deepest open sub on ESC.
         */
        boolean isSub = this instanceof SubGuiInterface;

        if (closeOnEsc && !isSub && (i == 1 || !GuiNpcTextField.isFieldActive() && isInventoryKey(i))) {
            SubGuiInterface sub = getSubGui();
            if (sub != null)
                sub.close();
            else
                close();
        }
    }

    public void onGuiClosed() {
        GuiNpcTextField.unfocus();
    }

    public void close() {
        if(GuiNpcTextField.activeTextfield != null)
            GuiNpcTextField.unfocus();

        Keyboard.enableRepeatEvents(false);
        displayGuiScreen(null);
        mc.setIngameFocus();
        save();
    }

    public void addButton(GuiNpcButton button) {
        buttons.put(button.id, button);
        buttonList.add(button);
    }

    public void addTopButton(GuiMenuTopButton button) {
        topbuttons.put(button.id, button);
        buttonList.add(button);
    }

    public void addSideButton(GuiMenuSideButton button) {
        sidebuttons.put(button.id, button);
        buttonList.add(button);
    }

    public GuiNpcButton getButton(int i) {
        return buttons.get(i);
    }

    public GuiMenuSideButton getSideButton(int i) {
        return sidebuttons.get(i);
    }

    public GuiMenuTopButton getTopButton(int i) {
        return topbuttons.get(i);
    }

    public void addTextField(GuiNpcTextField tf) {
        textfields.put(tf.id, tf);
    }

    public GuiNpcTextField getTextField(int i) {
        return textfields.get(i);
    }

    public void addLabel(GuiNpcLabel label) {
        labels.put(label.id, label);
    }

    public GuiNpcLabel getLabel(int i) {
        return labels.get(i);
    }

    public GuiScrollWindow getScrollableGui(int i) {
        return scrollWindows.get(i);
    }

    public void addSlider(GuiNpcSlider slider) {
        sliders.put(slider.id, slider);
        buttonList.add(slider);
    }

    public GuiNpcSlider getSlider(int i) {
        return sliders.get(i);
    }

    public void addScroll(GuiCustomScroll scroll) {
        scroll.setWorldAndResolution(mc, 350, 250);
        scrolls.put(scroll.id, scroll);
    }

    public GuiCustomScroll getScroll(int id) {
        return scrolls.get(id);
    }

    public abstract void save();

    @Override
    public void drawScreen(int i, int j, float f) {
        mouseX = i;
        mouseY = j;
        if (drawDefaultBackground && subgui == null)
            drawDefaultBackground();

        if (background != null && mc.renderEngine != null) {
            drawBackground();
        }

        boolean subGui = hasSubGui();
        drawCenteredString(fontRendererObj, title, width / 2, guiTop + 4, 0xffffff);
        for (GuiNpcLabel label : labels.values())
            label.drawLabel(this, fontRendererObj);
        for (GuiNpcTextField tf : textfields.values()) {
            tf.drawTextBox(i, j);
        }
        for (GuiCustomScroll scroll : scrolls.values()) {
            scroll.updateSubGUI(subGui);
            scroll.drawScreen(i, j, f, !subGui && scroll.isMouseOver(i, j) ? Mouse.getDWheel() : 0);
        }
        // Draw scrollable windows.
        for (GuiScrollWindow guiScrollableComponent : scrollWindows.values()) {
            guiScrollableComponent.drawScreen(i, j, f, !subGui && guiScrollableComponent.isMouseOver(i, j) ? Mouse.getDWheel() : 0);
        }
        for (GuiDiagram diagram : diagrams.values()) {
            diagram.drawDiagram(i, j, subGui);
        }
        super.drawScreen(i, j, f);
        for (GuiCustomScroll scroll : scrolls.values())
            if (scroll.hoverableText) {
                scroll.drawHover(i, j);
            }
        for (GuiNpcButton button : buttons.values()) {
            button.updateSubGUI(subGui);
            if (!button.hoverableText.isEmpty()) {
                button.drawHover(i, j, subGui);
            }
        }

        for (GuiScreen gui : extra.values())
            gui.drawScreen(i, j, f);

        if (subgui != null) {
            subgui.drawScreen(i, j, f);
        }

        if (drawNpc)
            drawNpcWithExtras(npc, i, j, f);
    }

    protected void drawBackground() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();
        GL11.glTranslatef(guiLeft, guiTop, 0);
        GL11.glScalef(bgScale * bgScaleX, bgScale * bgScaleY, bgScale * bgScaleZ);
        mc.renderEngine.bindTexture(background);
        if (xSize > 256) {
            // GUI wider than texture: draw left portion, then right portion from texture edge
            drawTexturedModalRect(0, 0, 0, 0, 250, ySize);
            drawTexturedModalRect(250, 0, 256 - (xSize - 250), 0, xSize - 250, ySize);
        } else if (xSize < 256) {
            // GUI narrower than texture: stitch left and right edges together
            // Left portion (first half of GUI)
            int leftWidth = xSize / 2;
            // Right portion width (remaining)
            int rightWidth = xSize - leftWidth;
            // Draw left side from texture start
            drawTexturedModalRect(0, 0, 0, 0, leftWidth, ySize);
            // Draw right side from texture end (256 - rightWidth)
            drawTexturedModalRect(leftWidth, 0, 256 - rightWidth, 0, rightWidth, ySize);
        } else {
            // GUI exactly 256px: draw full texture
            drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);
        }
        GL11.glPopMatrix();
    }

    protected void drawTextBlock(String text, int x, int y, int lineWidth) {
        TextBlockClient block = new TextBlockClient(StatCollector.translateToLocal(text), lineWidth, true, player);
        for (int line = 0; line < block.lines.size(); line++) {
            String lineText = block.lines.get(line).getFormattedText();
            fontRendererObj.drawString(lineText, x, y + (line * fontRendererObj.FONT_HEIGHT), CustomNpcResourceListener.DefaultTextColor);
        }
    }

    public FontRenderer getFontRenderer() {
        return this.fontRendererObj;
    }

    public void elementClicked() {
        if (subgui != null)
            subgui.elementClicked();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void doubleClicked() {
    }

    public boolean isInventoryKey(int i) {
        return i == mc.gameSettings.keyBindInventory.getKeyCode(); // inventory key
    }

    @Override
    public void drawDefaultBackground() {
        super.drawDefaultBackground();
    }

    public void displayGuiScreen(GuiScreen gui) {
        mc.displayGuiScreen(gui);
    }

    public void setSubGui(SubGuiInterface gui) {
        subgui = gui;
        subgui.setWorldAndResolution(mc, width, height);
        subgui.parent = this;
        initGui();
    }

    public void closeSubGui(SubGuiInterface gui) {
        subgui = null;
        initGui();
    }

    public boolean hasSubGui() {
        return subgui != null;
    }

    /**
     * @return The deepest open subgui
     */
    public SubGuiInterface getSubGui() {
        SubGuiInterface sub = subgui;
        if (sub != null)
            while (sub.hasSubGui())
                sub = sub.getSubGui();
        return sub;
    }

    private GuiNpcButton rotateLeft, rotateRight, zoomOut, zoomIn;

    public int xOffsetNpc = 0, xOffsetButton = 0, xMouseRange = 50;
    public int yOffsetNpc = 0, yOffsetButton = 0, yMouseRange = 150;

    public float defaultZoom = 1, zoom = 1, rotation;
    public float minZoom = 1, maxZoom = 2.5f;

    public boolean followMouse = true, allowRotate = true, drawNPConSub;
    public boolean drawNpc, drawRenderButtons;

    public boolean isMouseOverRenderer(int x, int y) {
        if (!allowRotate) {
            return false;
        }
        // Center of the entity rendering
        int centerX = guiLeft + xOffsetNpc; // Matches l in drawScreen()
        int centerY = guiTop + yOffsetNpc; // Matches i1 in drawScreen()

        // Range from the center to start considering mouse is over renderer.
        int xRange = xMouseRange; // Horizontal range (Left and right of center)
        int yRange = yMouseRange; // Vertical range (Up and down of center)

        // Check if the mouse is within the range area
        return mouseX >= centerX - xRange && mouseX <= centerX + xRange && mouseY >= centerY - yRange && mouseY <= centerY + yRange;
    }


    public void drawNpcWithExtras(EntityLivingBase entity, int mouseX, int mouseY, float partialTicks) {
        drawNpc(entity, mouseX, mouseY, partialTicks);
    }
    public void drawNpc(EntityLivingBase entity, int mouseX, int mouseY, float partialTicks) {
        if (hasSubGui() && !drawNPConSub)
            return;


        if (drawRenderButtons) {
            rotateLeft.drawButton(mc, mouseX, mouseY);
            rotateRight.drawButton(mc, mouseX, mouseY);
            zoomOut.drawButton(mc, mouseX, mouseY);
            zoomIn.drawButton(mc, mouseX, mouseY);
        }

        if (Mouse.isButtonDown(0) && drawRenderButtons) {
            if (this.rotateLeft.mousePressed(this.mc, mouseX, mouseY)) {
                rotation += partialTicks * 1.5F;
            } else if (this.rotateRight.mousePressed(this.mc, mouseX, mouseY)) {
                rotation -= partialTicks * 1.5F;
            } else if (this.zoomOut.mousePressed(this.mc, mouseX, mouseY) && zoom < maxZoom) {
                zoom += partialTicks * 0.05F;
            } else if (this.zoomIn.mousePressed(this.mc, mouseX, mouseY) && zoom > minZoom) {
                zoom -= partialTicks * 0.05F;
            }
        }


        if (isMouseOverRenderer(mouseX, mouseY)) {
            zoom += Mouse.getDWheel() * 0.001f;
            if (Mouse.isButtonDown(0)) {
                rotation -= Mouse.getDX() * 0.75f;
            } else if (Mouse.isButtonDown(1)) {
                rotation = 0;
                zoom = defaultZoom;
            }
        }

        if (zoom > maxZoom)
            zoom = maxZoom;
        if (zoom < minZoom)
            zoom = minZoom;

        drawNpc(entity, mouseX, mouseY, xOffsetNpc, yOffsetNpc, zoom, rotation, partialTicks);
    }

    public void drawNpc(EntityLivingBase entity, int mouseX, int mouseY, int x, int y, float zoomed, float rotation, float partialTicks) {
        EntityNPCInterface npc = null;
        if (entity instanceof EntityNPCInterface)
            npc = (EntityNPCInterface) entity;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (npc != null)
            npc.isDrawn = true;
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(guiLeft + x, guiTop + y, 90F);
        float scale = 1;
        if (entity.height > 2.4)
            scale = 2 / entity.height;

        GL11.glScalef(-30 * scale * zoomed, 30 * scale * zoomed, 30 * scale * zoomed);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f7 = entity.rotationYawHead;
        float f5 = (float) (guiLeft + x) - mouseX;
        float f6 = (guiTop + y) - 50 * scale * zoomed - mouseY;
        int orientation = 0;
        if (npc != null) {
            orientation = npc.ais.orientation;
            npc.ais.orientation = (int) rotation;
        }

        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-(float) Math.atan(f6 / 400F) * 20F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = rotation;
        entity.rotationYaw = followMouse ? (float) Math.atan(f5 / 80F) * 40F + rotation : 0;
        entity.rotationPitch = followMouse ? -(float) Math.atan(f6 / 40F) * 20F : 0;
        entity.rotationYawHead = entity.rotationYaw;
        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180F;
        ClientEventHandler.renderingEntityInGUI = true;

        try {
            RenderManager.instance.renderEntityWithPosYaw(entity, 0, 0, 0, 0, 1);
        } catch (Exception e) {
        }

        ClientEventHandler.renderingEntityInGUI = false;
        entity.prevRenderYawOffset = entity.renderYawOffset = f2;
        entity.prevRotationYaw = entity.rotationYaw = f3;
        entity.prevRotationPitch = entity.rotationPitch = f4;
        entity.prevRotationYawHead = entity.rotationYawHead = f7;
        if (npc != null) {
            npc.ais.orientation = orientation;
        }
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        if (npc != null)
            npc.isDrawn = false;
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void renderHoveringText(List textLines, int x, int y, FontRenderer font) {
        this.drawHoveringText(textLines, x, y, font);
    }

    public void openLink(String link) {
        try {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
            oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new URI(link));
        } catch (Throwable throwable) {
        }
    }
}
