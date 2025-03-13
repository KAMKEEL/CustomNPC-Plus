package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.keys.KeyPreset;
import noppes.npcs.client.gui.util.animation.keys.KeyPresetManager;
import noppes.npcs.client.utils.Color;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;

public class OverlayKeyPresetViewer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("customnpcs:textures/gui/keypreset_highres.png");

    public int startX, startY, endX, endY, width, height;
    public int mouseX, mouseY;
    public boolean showOverlay;

    public int elementSpacing = 5, yStartSpacing = 5;
    public float scale = 0.75f;
    public Scrollable scroll = new Scrollable();

    public KeyPresetManager manager;
    public OverlayButton viewButton = new OverlayButton();
    public LinkedList<PresetElement> list = new LinkedList<>();

    private final FontRenderer font = Minecraft.getMinecraft().fontRenderer;

    public OverlayKeyPresetViewer(KeyPresetManager manager) {
        this.manager = manager;

        manager.keys.forEach((key) -> {
            PresetElement element = new PresetElement(key);
            list.add(element);
        });
    }

    public void initGui(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.width = endX - startX;
        this.height = endY - startY;
        scroll.init();
    }

    public void draw(int mouseX, int mouseY, int wheel) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        boolean aboveButton = viewButton.isMouseAbove(mouseX, mouseY);
        boolean aboveOverlay = isMouseAbove(mouseX, mouseY);

        if (aboveButton)
            showOverlay = true;

        if (showOverlay) {
            drawOverlay(wheel);

            if (!aboveOverlay && !aboveButton)
                showOverlay = false;
        }

        viewButton.drawButton();
    }

    public void drawOverlay(int wheel) {
        scroll.update(wheel);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiUtil.drawGradientRect(startX, startY, endX, endY, 0x88000000, 0x88303030);
        if (scroll.maxScroll > 0)
            scroll.drawBar();

        GuiUtil.setScissorClip(startX, startY, scroll.maxScroll > 0 ? width - scroll.barWidth : width, height);
        GL11.glPushMatrix();
        GL11.glTranslatef((startX + 2), startY + yStartSpacing - scroll.scrollY, 0);

        for (int i = 0, height = 0; i < list.size(); i++) {
            PresetElement element = list.get(i);
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 1);
            GL11.glTranslatef(0, Math.round(height / scale), 0);
            element.drawDescription();
            GL11.glPopMatrix();

            element.drawBox(height);
            height += element.getHeight();
        }

        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void keyTyped(char c, int i) {
        if (!showOverlay)
            return;

        list.forEach((element) -> {
            if (element.isEditing)
                element.keyTyped(i);
        });
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!showOverlay)
            return;

        list.forEach((element) -> {
            if (!element.isMouseAboveBox(mouseX, mouseY)) {
                if (element.isEditing)
                    element.cancelEdit();
            } else
                element.boxClicked(button);

            if (element.isMouseAboveReset(mouseX, mouseY) && !element.key.isDefault()) {
                element.key.defaultState.writeTo(element.key.currentState);
                manager.save();
            }

        });
    }

    public boolean isMouseAbove(int mouseX, int mouseY) {
        if (scroll.isMouseDragging)
            return true;

        return mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY;
    }

    public int getWidth() {
        return (int) (width / scale);
    }

    public class Scrollable {
        private float scrollY, targetScrollY, maxScroll;
        private int totalHeight, scrollbarHeight, barWidth = 2;
        private float heightFactor, scrollFactor;

        private boolean isMouseDragging;
        private int startDragY;

        public Scrollable() {
        }

        public void init() {
            totalHeight = 0;
            for (PresetElement element : list)
                totalHeight += element.getHeight();

            totalHeight -= yStartSpacing;
            heightFactor = height / totalHeight;
            scrollbarHeight = Math.max((int) (height * heightFactor), 20);
            scrollFactor = (height - yStartSpacing - scrollbarHeight) / maxScroll;

            scroll.maxScroll = Math.max(0, totalHeight - height);
        }

        public void update(int wheel) {
            if (wheel != 0 && isMouseAbove(mouseX, mouseY))
                targetScrollY = ValueUtil.clamp(targetScrollY - wheel / 15, 0, maxScroll);


            if (Mouse.isButtonDown(0)) {
                if (!isMouseDragging && isMouseAboveBar(mouseX, mouseY)) {
                    isMouseDragging = true;
                    startDragY = (int) (mouseY - scrollY * scrollFactor);
                }
            } else
                isMouseDragging = false;

            if (isMouseDragging)
                scrollY = targetScrollY = ValueUtil.clamp((mouseY - startDragY) / scrollFactor, 0, maxScroll);

            if (scrollY != targetScrollY) {
                scrollY = ValueUtil.lerp(scrollY, targetScrollY, 0.1f);

                if (Math.abs(scrollY - targetScrollY) < 0.001) // Snap to exact target value
                    scrollY = targetScrollY;
            }

            scrollY = ValueUtil.clamp(scrollY, 0, maxScroll);
        }

        public void drawBar() {
            GL11.glPushMatrix();
            GL11.glTranslatef(endX - barWidth - 2, startY + yStartSpacing + scrollY * scrollFactor, 0);
            GuiUtil.drawRectD(0, 0, barWidth, scrollbarHeight, (isMouseAboveBar(mouseX, mouseY) || isMouseDragging) ? 0xffbababa : 0xff767676);
            GL11.glPopMatrix();
        }

        public boolean isMouseAboveBar(int mouseX, int mouseY) {
            int barX = endX - barWidth - 2;
            int barY = (int) (startY + yStartSpacing + scrollY * scrollFactor);

            return mouseX >= barX && mouseX < barX + barWidth && mouseY >= barY && mouseY < barY + scrollbarHeight;
        }
    }

    public class PresetElement {
        public KeyPreset key;
        public boolean isEditing;
        public float boxScreenX, boxScreenY;

        public KeyPreset.KeyState newState = new KeyPreset.KeyState();

        public PresetElement(KeyPreset key) {
            this.key = key;
        }

        public void drawDescription() {
            String name = String.format("> %s", key.name);
            font.drawString(name, 0, 0, 0xffffff);

            if (!key.shouldConflict)
                font.drawString("(Conflict-free)", font.getStringWidth(name) + 5, 0, 0xface40);

            String description = String.format("- %s", key.description);
            font.drawSplitString(description, 9, font.FONT_HEIGHT, getMaxStringWidth() - 9, 0x888888);
        }

        public void drawBox(int offsetY) {
            ////////////////////////////////////////////
            ////////////////////////////////////////////
            //Box texture
            Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
            GL11.glPushMatrix();
            GL11.glColor4f(1, 1, 1, 1);
            float boxScaleX = 1.75f;
            float boxWidth = 32 * boxScaleX;
            boxScreenX = getMaxStringWidth() * scale + 8; //remove scaling from maxStringWidth
            GL11.glScalef(boxScaleX, 1, 0);

            if (isEditing)
                new Color(0x4772b3, 1).glColor();
            else if (isMouseAboveBox(mouseX, mouseY))
                new Color(0x656565, 1).glColor();
            else
                new Color(0x545454, 1).glColor();
            GuiUtil.drawTexturedModalRect(boxScreenX / boxScaleX, boxScreenY = offsetY - 5, 32, 20, 0, 492);
            GL11.glPopMatrix();

            //Reset box
            boolean isDefault = key.isDefault();
            float resetWidth = 10;
            float resetScreenX = boxScreenX + boxWidth + 2;

            if (isMouseAboveReset(mouseX, mouseY) && !isDefault)
                new Color(0x656565, 1).glColor();
            else
                new Color(0x545454, 1).glColor();
            GuiUtil.drawTexturedModalRect(resetScreenX, boxScreenY, 10, 20, 33, 492);

            ////////////////////////////////////////////
            ////////////////////////////////////////////
            //Key name
            String name = getBoxKeyName();
            float nameWidth = font.getStringWidth(name);
            float nameScale = scale;
            float nameBoxRatio = nameWidth * scale / boxWidth;

            GL11.glPushMatrix();
            if (nameBoxRatio > 0.9) {
                nameScale /= nameWidth / boxWidth / 1.2f;
                GL11.glTranslatef(0, nameBoxRatio * 0.5f, 0);
            }

            float nameX = boxScreenX + boxWidth / 2 - (nameWidth / 2 * nameScale);
            float nameY = offsetY + 11.5f - (font.FONT_HEIGHT / 2);
            GL11.glScalef(nameScale, nameScale, 1);
            GL11.glTranslatef(nameX / nameScale, nameY / nameScale, 1);
            font.drawStringWithShadow(name, 0, 0, conflicts() ? 0xff5555 : 0xffffffff);
            GL11.glPopMatrix();

            //Reset letter
            char letter = 'X';
            float letterX = resetScreenX + (resetWidth / 2) - (font.getCharWidth(letter) / 2 * scale) + 0.25f;
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 1);
            GL11.glTranslatef(letterX / scale, nameY / scale, 1);
            font.drawStringWithShadow(String.valueOf(letter), 0, 0, isDefault ? 0xff8a8a8a : 0xffffffff);
            GL11.glPopMatrix();

            ////////////////////////////////////////////
            ////////////////////////////////////////////
        }

        public void boxClicked(int button) {
            if (isEditing)
                setKey(-100 + button);
            else if (button == 0)
                isEditing = true;
        }

        public void keyTyped(int typedKey) {
            if (typedKey == 1) {
                setKey(0);
                return;
            }

            newState.setState(newState.keyCode, KeyPreset.isCtrlKeyDown(), KeyPreset.isAltKeyDown(), KeyPreset.isShiftKeyDown());
            if (KeyPreset.isNotCtrlAltShift(typedKey))
                setKey(typedKey);
        }

        public void setKey(int keyCode) {
            if (keyCode == 0)
                newState.clear();

            newState.keyCode = keyCode;
            newState.writeTo(key.currentState);
            cancelEdit();
            manager.save();
        }

        public void cancelEdit() {
            newState.clear();
            isEditing = false;
        }

        public boolean conflicts() {
            return list.stream().anyMatch(element -> key.shouldConflict && element.key.shouldConflict && element.key != this.key && element.key.equals(this.key));
        }

        public String getBoxKeyName() {
            if (isEditing) {
                String modifiers = "";
                if (KeyPreset.isCtrlKeyDown())
                    modifiers += "CTRL ";
                if (KeyPreset.isAltKeyDown())
                    modifiers += "ALT ";
                if (KeyPreset.isShiftKeyDown())
                    modifiers += "SHIFT ";

                return !modifiers.isEmpty() ? modifiers : "Press a key";
            }

            return key.currentState.getName();
        }

        public boolean isMouseAboveBox(int mouseX, int mouseY) {
            mouseX -= startX + 2;
            mouseY -= startY + 5 - scroll.scrollY;

            float boxScaleX = 1.75f;
            float screenY = boxScreenY + 10;

            return mouseX >= boxScreenX && mouseX < boxScreenX + 32 * boxScaleX && mouseY >= screenY && mouseY < screenY + 10;
        }

        public boolean isMouseAboveReset(int mouseX, int mouseY) {
            mouseX -= startX + 2;
            mouseY -= startY + 5 - scroll.scrollY;

            float boxScaleX = 1.75f;
            float resetWidth = 10, boxWidth = 32 * boxScaleX;
            float screenX = boxScreenX + boxWidth + 2;
            float screenY = boxScreenY + 10;

            return mouseX >= screenX && mouseX < screenX + resetWidth && mouseY >= screenY && mouseY < screenY + 10;
        }

        public int getHeight() {
            List<String> wrappedDescription = font.listFormattedStringToWidth(String.format("- %s", key.description), getMaxStringWidth());

            int nameHeight = font.FONT_HEIGHT;
            int translations = font.FONT_HEIGHT;
            int descriptionHeight = wrappedDescription.size() * (font.FONT_HEIGHT);

            return Math.round((nameHeight + descriptionHeight + translations) * scale) + elementSpacing;
        }

        public int getMaxStringWidth() {
            return (int) (getWidth() * 2.5f / 4);
        }
    }

    public class OverlayButton {
        public int startX, startY, endX, width, height;
        public int textureWidth = 252, textureHeight = 109;
        public float scale = 0.25f;

        public OverlayButton() {
        }

        public void initGui(int endX, int startY) {
            this.endX = endX;
            this.startY = startY;

            startX = (int) (endX - textureWidth * scale / 2);
        }

        public void drawButton() {
            Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);

            double screenX = startX / scale, screenY = startY / scale;
            int oldMinFilter = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            GL11.glPushMatrix();
            float color = 0.55f;
            GL11.glColor4f(color, color, color, 1);
            GL11.glScalef(scale, scale, 1);
            GuiUtil.drawTexturedModalRect(screenX, screenY, textureWidth, textureHeight, 0, 0);
            GL11.glPopMatrix();

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, oldMinFilter);
        }

        public boolean isMouseAbove(int mouseX, int mouseY) {
            return mouseX >= startX && mouseX < startX + textureWidth * scale / 2 && mouseY >= startY - 4 && mouseY < startY + textureHeight * scale / 2;
        }
    }
}
