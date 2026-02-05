package kamkeel.npcs.client.gui.modern;

import kamkeel.npcs.client.gui.components.ModernButton;
import kamkeel.npcs.client.gui.components.ModernTextField;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Modern styled color picker SubGui.
 * Features color wheel selection, hex input field, and color preview swatch.
 */
public class ModernColorPicker extends ModernSubGuiInterface {

    private static final ResourceLocation COLOR_WHEEL = new ResourceLocation("customnpcs:textures/gui/color.png");

    // Current selected color (RGB without alpha)
    public int color;

    // Components
    private ModernTextField hexField;
    private ModernButton doneBtn;
    private ModernButton cancelBtn;

    // Color wheel position
    private int wheelX, wheelY;
    private int wheelSize = 130;  // Larger wheel

    // Preview swatch position
    private int previewX, previewY;
    private int previewSize = 24;  // Slightly smaller to fit better

    // Button IDs
    private static final int ID_DONE = 100;
    private static final int ID_CANCEL = 101;

    public ModernColorPicker(int initialColor) {
        this.color = initialColor & 0xFFFFFF; // Strip alpha
        xSize = 200;  // Increased for comfortable layout
        ySize = 250;  // Increased for proper spacing between content and buttons
        setHeaderTitle("Color Picker");
    }

    @Override
    public void initGui() {
        super.initGui();

        // Calculate positions with more top padding
        int contentY = getContentY() + 12;
        int centerX = guiLeft + xSize / 2;

        // Color wheel centered
        wheelX = centerX - wheelSize / 2;
        wheelY = contentY;

        // Preview swatch and hex field below wheel with proper spacing
        int fieldRowY = wheelY + wheelSize + 14;
        previewX = guiLeft + 16;  // Fixed left margin
        previewY = fieldRowY;

        // Hex field next to preview
        hexField = new ModernTextField(0, previewX + previewSize + 12, fieldRowY, 100, 18);
        hexField.setMaxLength(6);
        hexField.setPlaceholder("RRGGBB");
        hexField.setText(getHexString());

        // Buttons at bottom with proper margin
        int btnY = guiTop + ySize - 32;
        int btnWidth = 70;  // Slightly wider buttons
        int totalBtnWidth = btnWidth * 2 + 10;
        int btnStartX = centerX - totalBtnWidth / 2;

        cancelBtn = new ModernButton(ID_CANCEL, btnStartX, btnY, btnWidth, 20, "Cancel");
        doneBtn = new ModernButton(ID_DONE, btnStartX + btnWidth + 10, btnY, btnWidth, 20, "Done");

        // Use accent color for Done button
        doneBtn.setBackgroundColor(ModernColors.ACCENT_BLUE);
    }

    @Override
    protected void drawContent(int mouseX, int mouseY, float partialTicks) {
        // Draw color wheel
        mc.getTextureManager().bindTexture(COLOR_WHEEL);
        GL11.glColor4f(1, 1, 1, 1);
        drawTexturedModalRect(wheelX, wheelY, 0, 0, wheelSize, wheelSize);

        // Draw preview swatch with border
        drawRect(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, ModernColors.PANEL_BORDER);
        drawRect(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000 | color);

        // Draw hex label
        fontRendererObj.drawString("#", hexField.getX() - 10, hexField.getY() + 5, ModernColors.TEXT_LIGHT);

        // Draw hex field
        hexField.draw(mouseX, mouseY);

        // Draw buttons
        doneBtn.drawButton(mc, mouseX, mouseY);
        cancelBtn.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        hexField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        // Check hex field click
        hexField.mouseClicked(mouseX, mouseY, button);

        // Check color wheel click
        if (button == 0 && isInsideWheel(mouseX, mouseY)) {
            pickColorFromWheel(mouseX, mouseY);
        }

        // Check button clicks
        if (doneBtn.mousePressed(mc, mouseX, mouseY)) {
            close();
            return;
        }
        if (cancelBtn.mousePressed(mc, mouseX, mouseY)) {
            // Reset color to original? For now just close
            close();
            return;
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        super.actionPerformed(btn);
        // Buttons handled in mouseClicked
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        // Handle hex field input
        if (hexField.isFocused()) {
            String prev = hexField.getText();
            hexField.keyTyped(typedChar, keyCode);
            String newText = hexField.getText();

            // Only allow hex characters
            if (!newText.equals(prev)) {
                String filtered = filterHex(newText);
                if (!filtered.equals(newText)) {
                    hexField.setText(filtered);
                } else {
                    // Update color if valid
                    updateColorFromHex();
                }
            }
        }

        // Handle ESC
        super.keyTyped(typedChar, keyCode);
    }

    /**
     * Filter input to only allow hex characters.
     */
    private String filterHex(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toUpperCase().toCharArray()) {
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Update color value from hex field text.
     */
    private void updateColorFromHex() {
        String hex = hexField.getText();
        if (hex.isEmpty()) return;

        try {
            color = Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            // Invalid hex, ignore
        }
    }

    /**
     * Check if mouse is inside the color wheel area.
     */
    private boolean isInsideWheel(int mouseX, int mouseY) {
        return mouseX >= wheelX && mouseX < wheelX + wheelSize &&
               mouseY >= wheelY && mouseY < wheelY + wheelSize;
    }

    /**
     * Pick color from the wheel texture at clicked position.
     */
    private void pickColorFromWheel(int mouseX, int mouseY) {
        InputStream stream = null;
        try {
            IResource resource = mc.getResourceManager().getResource(COLOR_WHEEL);
            BufferedImage image = ImageIO.read(stream = resource.getInputStream());

            // Scale mouse position to texture coordinates
            // The texture is assumed to be a certain size, compute ratio
            int texX = (mouseX - wheelX) * image.getWidth() / wheelSize;
            int texY = (mouseY - wheelY) * image.getHeight() / wheelSize;

            // Clamp to texture bounds
            texX = Math.max(0, Math.min(texX, image.getWidth() - 1));
            texY = Math.max(0, Math.min(texY, image.getHeight() - 1));

            color = image.getRGB(texX, texY) & 0xFFFFFF;
            hexField.setText(getHexString());

        } catch (IOException e) {
            // Ignore errors
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Get color as hex string (6 chars, uppercase).
     */
    private String getHexString() {
        String hex = Integer.toHexString(color).toUpperCase();
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return hex;
    }

    /**
     * Get the selected color.
     */
    public int getColor() {
        return color;
    }
}
