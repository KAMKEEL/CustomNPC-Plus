package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import noppes.npcs.NoppesStringUtils;

public class SubGuiConfirmLink extends SubGuiInterface {
    private final String url;
    public boolean confirmed = false;

    public SubGuiConfirmLink(String url) {
        this.url = url;
        this.xSize = 260;
        this.ySize = 120;
        this.closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        GuiNpcLabel titleLabel = new GuiNpcLabel(0, I18n.format("chat.link.confirmTrusted"), guiLeft, guiTop + 10, 0xFFFFFF);
        titleLabel.center(xSize);
        addLabel(titleLabel);

        GuiNpcLabel urlLabel = new GuiNpcLabel(1, url, guiLeft, guiTop + 28, 0xCCCCFF);
        urlLabel.center(xSize);
        addLabel(urlLabel);

        int btnWidth = 115;
        int spacing = 10;
        int totalWidth = btnWidth * 2 + spacing;
        int startX = guiLeft + (xSize - totalWidth) / 2;

        addButton(new GuiNpcButton(0, startX, guiTop + 52, btnWidth, 20, "chat.link.open"));
        addButton(new GuiNpcButton(1, startX + btnWidth + spacing, guiTop + 52, btnWidth, 20, "chat.copy"));
        addButton(new GuiNpcButton(2, guiLeft + (xSize - btnWidth) / 2, guiTop + 78, btnWidth, 20, "gui.cancel"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xC0101010, 0xC0101010);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            confirmed = true;
            openLink(url);
        } else if (guibutton.id == 1) {
            confirmed = true;
            NoppesStringUtils.setClipboardContents(url);
        }
        close();
    }

    @Override
    public void save() {
    }
}
