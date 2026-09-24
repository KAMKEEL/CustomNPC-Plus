package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Quest;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiQuestCompletion extends GuiNPCInterface implements ITopButtonListener {

    private Quest quest;
    private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/menubg.png");

    private int posX, posY, width, height;
    private int listHeight;
    private float scrolledY = 0;
    private int startClick = -1;
    private boolean clickVerticalBar = false;
    private TextBlockClient block;

    public GuiQuestCompletion(Quest quest) {
        super();
        xSize = 176;
        ySize = 222;
        this.quest = quest;
        this.drawDefaultBackground = false;
        title = "";
    }

    @Override
    public void initGui() {
        super.initGui();

        String questTitle = quest.title;
        int left = (xSize - this.fontRendererObj.getStringWidth(questTitle)) / 2;
        this.addLabel(new GuiNpcLabel(0, questTitle, guiLeft + left, guiTop + 4));

        this.addButton(new GuiNpcButton(0, guiLeft + 38, guiTop + ySize - 24, 100, 20, StatCollector.translateToLocal("quest.complete")));

        this.posX = guiLeft + 8;
        this.posY = guiTop + 18;
        this.width = 160;
        this.height = ySize - 46;

        this.block = new TextBlockClient(quest.completeText, width - 10, true, player);
        this.listHeight = block.lines.size() * fontRendererObj.FONT_HEIGHT;
        this.scrolledY = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        drawHorizontalLine(guiLeft + 4, guiLeft + 170, guiTop + 13, +0xFF000000 + CustomNpcResourceListener.DefaultTextColor);

        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            addScrollY(dWheel < 0 ? -10 : 10);
        }

        if (Mouse.isButtonDown(0)) {
            if (clickVerticalBar) {
                if (startClick >= 0) {
                    addScrollY(startClick - (mouseY - posY));
                }
                startClick = mouseY - posY;
            } else if (hoverVerticalScrollBar(mouseX, mouseY)) {
                clickVerticalBar = true;
                startClick = mouseY - posY;
            }
        } else {
            clickVerticalBar = false;
        }

        drawQuestText();
        drawVerticalScrollBar();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawQuestText() {
        if (block == null || block.lines.isEmpty())
            return;

        int startLine = getStartLineY();
        int maxLine = height / fontRendererObj.FONT_HEIGHT + startLine;

        int lineCount = 0;
        for (int i = 0; i < block.lines.size(); i++) {
            if (lineCount >= startLine && lineCount < maxLine) {
                String text = block.lines.get(i).getFormattedText();
                int textY = posY + ((lineCount - startLine) * fontRendererObj.FONT_HEIGHT);
                fontRendererObj.drawString(text, posX, textY, CustomNpcResourceListener.DefaultTextColor);
            }
            lineCount++;
        }
    }

    private int getStartLineY() {
        if (!isScrolling())
            scrolledY = 0;
        return MathHelper.ceiling_double_int(scrolledY * listHeight / fontRendererObj.FONT_HEIGHT);
    }

    private boolean isScrolling() {
        return listHeight > height - 4;
    }

    private void addScrollY(int scrolled) {
        if (!isScrolling()) return;

        scrolledY -= 1f * scrolled / height;

        if (scrolledY < 0)
            scrolledY = 0;

        float max = 1 - 1f * (height + 2) / listHeight;
        if (scrolledY > max)
            scrolledY = max;
    }

    private boolean hoverVerticalScrollBar(int x, int y) {
        if (!isScrolling())
            return false;

        return y >= posY && y <= posY + height && x >= posX + width - 8 && x <= posX + width;
    }

    private int getVerticalBarSize() {
        return (int) (1f * height / listHeight * (height - 4));
    }

    private void drawVerticalScrollBar() {
        if (!isScrolling())
            return;

        mc.renderEngine.bindTexture(GuiCustomScroll.resource);
        int x = posX + width - 6;
        int y = (int) (posY + scrolledY * height) + 2;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int sbSize = getVerticalBarSize();

        drawTexturedModalRect(x, y, 176, 9, 5, 1);

        for (int k = 0; k < sbSize; k++) {
            drawTexturedModalRect(x, y + k + 1, 176, 10, 5, 1);
        }

        drawTexturedModalRect(x, y + sbSize + 1, 176, 11, 5, 1);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            close();
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        if (i == 1 || isInventoryKey(i)) {
            close();
        }
    }

    @Override
    public void save() {}
}
