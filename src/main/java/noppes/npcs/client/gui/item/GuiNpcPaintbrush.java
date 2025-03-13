package noppes.npcs.client.gui.item;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.item.ColorBrushPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.items.ItemNpcTool;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GuiNpcPaintbrush extends GuiNPCInterface {

    private GuiNpcTextField textfield;
    private int color = 0xffffff; // default color
    private int colorX, colorY;
    private static final ResourceLocation resource = new ResourceLocation("customnpcs:textures/gui/color.png");

    public GuiNpcPaintbrush() {
        super();
        xSize = 256;
        setBackground("menubg.png");

        ItemStack brush = this.player.getHeldItem();
        if (brush == null)
            return;

        color = ItemNpcTool.getColor(brush.getTagCompound());
    }

    @Override
    public void initGui() {
        super.initGui();

        // Move the color picker slightly more to the left.
        colorX = guiLeft + 20; // was guiLeft + 30
        colorY = guiTop + 50;

        // Add the hex color text field, also moved left accordingly.
        this.addTextField(textfield = new GuiNpcTextField(0, this, guiLeft + 43, guiTop + 20, 70, 20, getColor()));
        textfield.setTextColor(color);

        // Move the done button to the bottom right.
        int margin = 10;
        addButton(new GuiNpcButton(66, guiLeft + xSize - 55 - margin, guiTop + ySize - 20 - margin, 60, 20, "gui.done"));
    }

    public String getColor() {
        String str = Integer.toHexString(color);
        while (str.length() < 6)
            str = "0" + str;
        return str;
    }

    @Override
    public void keyTyped(char c, int i) {
        String prev = textfield.getText();
        super.keyTyped(c, i);
        String newText = textfield.getText();
        if (!newText.equals(prev)) {
            try {
                color = Integer.parseInt(newText, 16);
                textfield.setTextColor(color);
            } catch (NumberFormatException e) {
                textfield.setText(prev);
            }
        }
        if (i == 1) {
            close();
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 66) {
            close();
        }
    }

    @Override
    public void close() {
        PacketClient.sendClient(new ColorBrushPacket(this.color));
        super.close();
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if (i < colorX || i > colorX + 117 || j < colorY || j > colorY + 117)
            return;

        InputStream stream = null;
        try {
            IResource iresource = this.mc.getResourceManager().getResource(resource);
            stream = iresource.getInputStream();
            BufferedImage bufferedimage = ImageIO.read(stream);

            // Calculate the corresponding pixel on the texture (scaling factor 4)
            int imgX = (i - guiLeft - 20) * 4;
            int imgY = (j - guiTop - 50) * 4;
            color = bufferedimage.getRGB(imgX, imgY) & 0xFFFFFF;
            textfield.setTextColor(color);
            textfield.setText(getColor());
        } catch (IOException e) {
            // Optionally log the error here
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore quietly
                }
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);

        // Draw the color picker on the left side
        mc.getTextureManager().bindTexture(resource);
        GL11.glColor4f(1, 1, 1, 1);
        drawTexturedModalRect(colorX, colorY, 0, 0, 120, 120);
    }

    @Override
    public void save() {
        // Implement save functionality if needed.
    }
}
