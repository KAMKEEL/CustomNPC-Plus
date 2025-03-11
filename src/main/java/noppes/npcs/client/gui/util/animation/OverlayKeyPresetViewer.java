package noppes.npcs.client.gui.util.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiUtil;
import noppes.npcs.client.gui.util.animation.keys.KeyPreset;
import noppes.npcs.client.gui.util.animation.keys.KeyPresetManager;
import noppes.npcs.client.utils.Color;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;

public class OverlayKeyPresetViewer {
    public int startX, startY, endX, endY, width, height;
    public boolean showOverlay;

    public int elementSpacing = 5;
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

        int totalHeight = 0;
        for (PresetElement element : list)
            totalHeight += element.getHeight();

        scroll.maxScroll = Math.max(0, totalHeight - height);
    }

    public void draw(int mouseX, int mouseY, float partialTicks, int wheel) {
        boolean aboveButton = viewButton.isMouseAbove(mouseX, mouseY);
        boolean aboveOverlay = isMouseAbove(mouseX, mouseY);

        //     if (aboveButton)
        showOverlay = true;

        if (showOverlay) {
            drawOverlay(mouseX, mouseY, partialTicks, wheel);

            if (!aboveOverlay && !aboveButton)
                showOverlay = false;
        }

        viewButton.drawButton();
    }

    public void drawOverlay(int mouseX, int mouseY, float partialTicks, int wheel) {
        if (wheel != 0 && isMouseAbove(mouseX, mouseY))
            scroll.scroll(wheel);
        scroll.update();

        GuiUtil.drawGradientRect(startX, startY, endX, endY, 0x88000000, 0x88303030);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiUtil.setScissorClip(startX, startY, scroll.maxScroll > 0 ? width - 2 : width, height);

        GL11.glPushMatrix();
        GL11.glTranslatef((startX + 2), startY + 5 - scroll.scrollY, 0);

        for (int i = 0, height = 0; i < list.size(); i++) {
            PresetElement element = list.get(i);
            GL11.glPushMatrix();
            GL11.glScalef(scale, scale, 1);
            GL11.glTranslatef(0, Math.round(height / scale), 0);
            element.drawText();
            GL11.glPopMatrix();

            element.drawBox(mouseX, mouseY, height);
            height += element.getHeight();
        }

        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void keyTyped(char c, int i) {
        list.forEach((element) -> {
            if (element.boxFocused)
                element.keyTyped(c, i);
        });
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        list.forEach((element) -> {
            if (element.isMouseAboveBox(mouseX, mouseY)) {
                element.mouseClicked(mouseX, mouseY, button);
            }
        });
    }

    public boolean isMouseAbove(int mouseX, int mouseY) {
        return mouseX >= startX && mouseX < endX && mouseY >= startY && mouseY < endY;
    }

    public int getWidth() {
        return (int) (width / scale);
    }

    public class Scrollable {
        private float scrollY, targetScrollY, maxScroll;

        public Scrollable() {
        }

        public void update() {
            if (scrollY != targetScrollY) {
                scrollY = ValueUtil.lerp(scrollY, targetScrollY, 0.05f);

                if (Math.abs(scrollY - targetScrollY) < 0.001) // Snap to exact target value
                    scrollY = targetScrollY;
            }

            scrollY = ValueUtil.clamp(scrollY, 0, maxScroll);
        }

        public void scroll(int wheel) {
            targetScrollY = ValueUtil.clamp(targetScrollY - wheel / 10, 0, maxScroll);
        }
    }

    public class PresetElement {
        public KeyPreset key;
        public boolean boxFocused;
        public float boxScreenY;

        public KeyPreset.KeyState oldState = new KeyPreset.KeyState();

        public PresetElement(KeyPreset key) {
            this.key = key;
        }

        public int getHeight() {
            List<String> wrappedDescription = font.listFormattedStringToWidth(String.format("- %s", key.description), getMaxStringWidth());

            int nameHeight = font.FONT_HEIGHT;
            int translations = font.FONT_HEIGHT;
            int descriptionHeight = wrappedDescription.size() * (font.FONT_HEIGHT);

            return Math.round((nameHeight + descriptionHeight + translations) * scale) + elementSpacing;
        }

        public void drawText() {
            String name = String.format("> %s", key.name);
            font.drawString(name, 0, 0, 0xffffff);

            String description = String.format("- %s", key.description);
            font.drawSplitString(description, 9, font.FONT_HEIGHT, getMaxStringWidth() - 9, 0x888888);
        }

        public void drawBox(int mouseX, int mouseY, int offsetY) {
            ////////////////////////////////////////////
            ////////////////////////////////////////////
            //Box texture
            Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
            GL11.glPushMatrix();
            GL11.glColor4f(1, 1, 1, 1);
            float boxScaleX = 1.75f;
            float screenX = getMaxStringWidth() * scale + 10; //remove scaling from maxStringWidth
            GL11.glScalef(boxScaleX, 1, 0);

            if (boxFocused)
                new Color(0x4772b3, 1).glColor();
            else if (isMouseAboveBox(mouseX, mouseY))
                new Color(0x656565, 1).glColor();
            else
                new Color(0x545454, 1).glColor();
            GuiUtil.drawTexturedModalRect(screenX / boxScaleX, boxScreenY = offsetY - 5, 64, 20, 0, 492);
            GL11.glPopMatrix();

            ////////////////////////////////////////////
            ////////////////////////////////////////////
            //Key name
            String name = getName();
            float boxWidth = 32 * boxScaleX, nameWidth = font.getStringWidth(name);
            float nameScale = scale;
            float nameBoxRatio = nameWidth * scale / boxWidth;

            GL11.glPushMatrix();
            if (nameBoxRatio > 0.9) {
                nameScale /= nameWidth / boxWidth / 1.2f;
                GL11.glTranslatef(0, nameBoxRatio * 0.5f, 0);
            }

            float nameX = screenX + boxWidth / 2 - (nameWidth / 2 * nameScale);
            float nameY = offsetY + 11.5f - (font.FONT_HEIGHT / 2);

            GL11.glScalef(nameScale, nameScale, 1);
            GL11.glTranslatef(nameX / nameScale, nameY / nameScale, 1);
            font.drawStringWithShadow(name, 0, 0, conflicts() ? 0xff5555 : 0xffffffff);
            GL11.glPopMatrix();

            ////////////////////////////////////////////
            ////////////////////////////////////////////
        }

        public boolean conflicts() {
            return list.stream().anyMatch(element -> element.key != this.key && element.key.equals(this.key));
        }

        public boolean isMouseAboveBox(int mouseX, int mouseY) {
            mouseX -= startX + 2;
            mouseY -= startY + 5 - scroll.scrollY;

            float boxScaleX = 1.75f;
            float screenX = (getMaxStringWidth() * scale + 10);
            float screenY = boxScreenY + 10;

            return mouseX >= screenX && mouseX < screenX + 32 * boxScaleX && mouseY >= screenY && mouseY < screenY + 10;
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {
            if (button == 0) {
                boxFocused = true;
                oldState.saveState(key);
                key.clear();
            } else if (button == 1) {
                if (boxFocused) {
                    boxFocused = false;
                    oldState.loadState(key);
                } else
                    key.defaultState.loadState(key);
            } else
                setKey(-100 + button, true);
        }

        public void keyTyped(char c, int keyCode) {
            if (keyCode == 1) {
                boxFocused = false;
                oldState.loadState(key);
                return;
            }

            key.hasCtrl = KeyPreset.isCtrlKeyDown();
            key.hasShift = KeyPreset.isShiftKeyDown();
            key.hasAlt = KeyPreset.isAltKeyDown();
            if (KeyPreset.isNotCtrlShiftAlt(keyCode)) {
                setKey(keyCode, false);
            }
        }

        public void setKey(int keyCode, boolean isMouse) {
            key.keyCode = keyCode;
            key.isMouseKey = isMouse;
            boxFocused = false;
            manager.save();
        }

        public String getName() {
            if (boxFocused) {
                String modifiers = "";
                if (KeyPreset.isCtrlKeyDown())
                    modifiers += "CTRL ";
                if (KeyPreset.isAltKeyDown())
                    modifiers += "ALT ";
                if (KeyPreset.isShiftKeyDown())
                    modifiers += "SHIFT ";

                return !modifiers.isEmpty() ? modifiers : "Press a key";
            }

            return key.getKeyName();
        }

        public int getMaxStringWidth() {
            return (int) (getWidth() * 2.5f / 4);
        }
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation("customnpcs:textures/gui/keypreset_highres.png");

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
