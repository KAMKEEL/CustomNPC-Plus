package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.controllers.APIRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SubGuiAPISelect extends SubGuiInterface implements ISubGuiListener {
    private final List<String> names;
    private final List<String> urls;

    public SubGuiAPISelect() {
        LinkedHashMap<String, String> entries = APIRegistry.Instance.getEntries();
        this.names = new ArrayList<>(entries.keySet());
        this.urls = new ArrayList<>(entries.values());

        this.xSize = 180;
        this.ySize = 40 + names.size() * 24;
        this.closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        addLabel(new GuiNpcLabel(0, "gui.api", guiLeft + 10, guiTop + 10, 0xFFFFFF));

        for (int i = 0; i < names.size(); i++) {
            addButton(new GuiNpcButton(i, guiLeft + 10, guiTop + 28 + i * 24, 160, 20, names.get(i)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xC0101010, 0xC0101010);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id >= 0 && id < urls.size()) {
            setSubGui(new SubGuiConfirmLink(urls.get(id)));
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiConfirmLink && ((SubGuiConfirmLink) subgui).confirmed) {
            close();
        }
    }

    @Override
    public void save() {
    }
}
