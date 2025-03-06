package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiToggleButton extends GuiNpcButton {

    // Fixed dimensions: 40x20 (even if other values are passed)
    private static final int FIXED_WIDTH = 40;
    private static final int FIXED_HEIGHT = 20;

    private boolean toggled;
    // Animation progress: 0.0 = OFF (knob left), 1.0 = ON (knob right)
    private float animProgress;

    // Optional off icon (if not set, the super class icon is used)
    private ResourceLocation textureOff = null;
    private int textureOffPosX = 0;
    private int textureOffPosY = 0;
    // Off icon always 16x16
    private static final int ICON_SIZE = 16;

    /**
     * Basic constructor. Uses the super class’s icon as the ON texture.
     * Note: The button is fixed at 40×20.
     */
    public GuiToggleButton(int id, int x, int y, boolean initialState) {
        super(id, x, y, FIXED_WIDTH, FIXED_HEIGHT, "");
        this.toggled = initialState;
        this.animProgress = initialState ? 1.0f : 0.0f;
    }

    /**
     * Constructor with both an ON and OFF icon.
     * The ON icon is taken from the super class’s icon.
     */
    public GuiToggleButton(int id, int x, int y, boolean initialState,
                           ResourceLocation textureOn, ResourceLocation textureOff) {
        super(id, x, y, FIXED_WIDTH, FIXED_HEIGHT, "");
        this.setIconTexture(textureOn);
        this.toggled = initialState;
        this.animProgress = initialState ? 1.0f : 0.0f;
        this.textureOff = textureOff;
    }

    // Off icon setters
    public GuiToggleButton setTextureOff(ResourceLocation texture) {
        this.textureOff = texture;
        return this;
    }

    public GuiToggleButton setTextureOffPos(int posX, int posY) {
        this.textureOffPosX = posX;
        this.textureOffPosY = posY;
        return this;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = super.mousePressed(mc, mouseX, mouseY);
        if (pressed) {
            toggled = !toggled;
        }
        return pressed;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible)
            return;

        FontRenderer fontrenderer = mc.fontRenderer;
        // Bind vanilla button texture.
        mc.getTextureManager().bindTexture(buttonTextures);
        GL11.glColor4f(1F, 1F, 1F, 1F);

        // Determine hover state (vanilla style)
        this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition &&
            mouseX < this.xPosition + FIXED_WIDTH && mouseY < this.yPosition + FIXED_HEIGHT;
        int k = this.getHoverState(this.field_146123_n);

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Update animation progress smoothly toward the target state.
        float target = toggled ? 1.0f : 0.0f;
        animProgress += (target - animProgress) * 0.2f;
        if (Math.abs(target - animProgress) < 0.01f)
            animProgress = target;

        // Draw the two halves using the vanilla button drawing code.
        // Left half:
        GL11.glColor4f(1F, 1F, 1F, 1F);
        this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, FIXED_WIDTH / 2, FIXED_HEIGHT);
        // Right half:
        this.drawTexturedModalRect(this.xPosition + FIXED_WIDTH / 2, this.yPosition, 200 - FIXED_WIDTH / 2, 46 + k * 20, FIXED_WIDTH / 2, FIXED_HEIGHT);

        // Determine which half is the non-head (static background) area.
        // When toggled ON, the knob (head) is on the right so non-head is the left half.
        // When toggled OFF, the knob is on the left so non-head is the right half.
        int nonHeadX, nonHeadWidth, nonHeadTexX;
        if (animProgress > 0.5f) {
            // ON: knob on right, darken left half.
            nonHeadX = this.xPosition;
            nonHeadWidth = FIXED_WIDTH / 2;
            nonHeadTexX = 0;
        } else {
            // OFF: knob on left, darken right half.
            nonHeadX = this.xPosition + FIXED_WIDTH / 2;
            nonHeadWidth = FIXED_WIDTH / 2;
            nonHeadTexX = 200 - FIXED_WIDTH / 2;
        }

        GL11.glColor4f(0.6f, 0.6f, 0.6f, 1.0F);
        this.drawTexturedModalRect(nonHeadX, this.yPosition, nonHeadTexX, 46 + k * 20, nonHeadWidth, FIXED_HEIGHT);

        // Now draw the knob ("head") that slides.
        // Knob width equals half of the button (20 pixels).
        int knobWidth = FIXED_WIDTH / 2;
        // The knob slides from xPosition (OFF) to xPosition + (FIXED_WIDTH - knobWidth) (ON).
        int knobX = this.xPosition + (int)((FIXED_WIDTH - knobWidth) * animProgress);
        int knobY = this.yPosition;

        // Draw the knob with no tint.
        GL11.glColor4f(1F, 1F, 1F, 1F);
        // Draw left half of knob
        this.drawTexturedModalRect(knobX, knobY, 0, 46 + k * 20, knobWidth / 2, FIXED_HEIGHT);
        // Draw right half of knob
        this.drawTexturedModalRect(knobX + knobWidth / 2, knobY, 200 - knobWidth / 2, 46 + k * 20, knobWidth / 2, FIXED_HEIGHT);

        // Draw the icon on the knob (always 16×16, centered on the knob).
        int iconX = knobX + (knobWidth - ICON_SIZE) / 2;
        int iconY = knobY + (FIXED_HEIGHT - ICON_SIZE) / 2;
        ResourceLocation iconTex;
        int texX, texY;
        // When ON, use the super class icon; when OFF, use textureOff if provided.
        if (toggled) {
            iconTex = this.iconTexture;
            texX = this.iconPosX;
            texY = this.iconPosY;
        } else {
            if (this.textureOff != null) {
                iconTex = this.textureOff;
                texX = this.textureOffPosX;
                texY = this.textureOffPosY;
            } else {
                iconTex = this.iconTexture;
                texX = this.iconPosX;
                texY = this.iconPosY;
            }
        }
        if (iconTex != null) {
            mc.getTextureManager().bindTexture(iconTex);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            this.drawTexturedModalRect(iconX, iconY, texX, texY, ICON_SIZE, ICON_SIZE);
        }

        // Optionally, draw the centered display string (if needed).
        int textColor = 14737632;
        if (packedFGColour != 0) {
            textColor = packedFGColour;
        } else if (!this.enabled) {
            textColor = 10526880;
        } else if (this.field_146123_n) {
            textColor = 16777120;
        }
        this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + FIXED_WIDTH / 2,
            this.yPosition + (FIXED_HEIGHT - 8) / 2, textColor);
    }
}
