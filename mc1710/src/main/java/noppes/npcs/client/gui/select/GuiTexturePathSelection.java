package noppes.npcs.client.gui.select;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.renderer.ImageData;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Texture path selector SubGui with a texture preview panel instead of NPC rendering.
 * Reuses the cached texture data from {@link GuiTextureSelection}.
 */
public class GuiTexturePathSelection extends SubGuiInterface implements ICustomScrollListener {

    private final String up = "..<" + StatCollector.translateToLocal("gui.up") + ">..";
    private GuiCustomScroll scrollCategories;
    private GuiCustomScroll scrollTextures;

    private String location = "";
    private String selectedDomain;
    public ResourceLocation selectedResource;

    private final HashMap<String, List<GuiTextureSelection.TextureData>> domains = new HashMap<>();
    private final HashMap<String, GuiTextureSelection.TextureData> textures = new HashMap<>();

    private String catSearch = "";
    private String texSearch = "";

    // Preview
    private static final int PREVIEW_SIZE = 90;

    public GuiTexturePathSelection(String texture) {
        drawDefaultBackground = false;
        title = "";
        setBackground("menubg.png");
        xSize = 366;
        ySize = 226;

        // Populate from GuiTextureSelection's cache (trigger a scan if needed)
        long now = System.currentTimeMillis();
        if (GuiTextureSelection.cachedTextures != null && !GuiTextureSelection.cachedTextures.isEmpty()
                && now - GuiTextureSelection.lastCacheTime() < GuiTextureSelection.CACHE_DURATION_MS) {
            domains.putAll(GuiTextureSelection.cachedDomains());
        } else {
            // Force a scan by creating a temporary GuiTextureSelection
            GuiTextureSelection temp = new GuiTextureSelection(null, null);
            domains.putAll(GuiTextureSelection.cachedDomains());
        }

        if (texture != null && !texture.isEmpty()) {
            selectedResource = new ResourceLocation(texture);
            selectedDomain = selectedResource.getResourceDomain();
            if (!domains.containsKey(selectedDomain)) {
                selectedDomain = null;
            }
            int i = selectedResource.getResourcePath().lastIndexOf('/');
            location = selectedResource.getResourcePath().substring(0, i + 1);
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        if (selectedDomain != null) {
            title = selectedDomain + ":" + location;
        } else {
            title = "";
        }

        // Buttons
        this.addButton(new GuiNpcButton(2, guiLeft + xSize - 46, guiTop + ySize - 28, 42, 20, "gui.done"));
        this.addButton(new GuiNpcButton(1, guiLeft + xSize - 92, guiTop + ySize - 28, 42, 20, "gui.cancel"));

        // Category scroll
        if (scrollCategories == null) {
            scrollCategories = new GuiCustomScroll(this, 0);
            scrollCategories.setSize(90, 163);
        }
        if (selectedDomain == null) {
            scrollCategories.setList(getCatSearch());
        } else {
            List<String> list = new ArrayList<>();
            list.add(up);
            List<GuiTextureSelection.TextureData> data = domains.get(selectedDomain);
            if (data != null) {
                for (GuiTextureSelection.TextureData td : data) {
                    if (location.isEmpty() || td.path.startsWith(location) && !td.path.equals(location)) {
                        String path = td.path.substring(location.length());
                        int i = path.indexOf('/');
                        if (i < 0) continue;
                        path = path.substring(0, i);
                        if (!path.isEmpty() && !list.contains(path)) {
                            list.add(path);
                        }
                    }
                }
            }
            scrollCategories.setList(list);
        }
        scrollCategories.guiLeft = guiLeft + 4;
        scrollCategories.guiTop = guiTop + 14;
        this.addScroll(scrollCategories);
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 4, guiTop + 179, 90, 20, catSearch));

        // Texture scroll
        if (scrollTextures == null) {
            scrollTextures = new GuiCustomScroll(this, 1);
            scrollTextures.setSize(160, 163);
        }
        if (selectedDomain != null) {
            textures.clear();
            List<GuiTextureSelection.TextureData> data = domains.get(selectedDomain);
            List<String> allList = new ArrayList<>();
            String loc = location;
            if (scrollCategories.hasSelected() && !scrollCategories.getSelected().equals(up)) {
                loc += scrollCategories.getSelected() + '/';
            }
            if (data != null) {
                for (GuiTextureSelection.TextureData td : data) {
                    if (td.path.equals(loc) && !allList.contains(td.name)) {
                        allList.add(td.name);
                        textures.put(td.name, td);
                    }
                }
            }
            scrollTextures.setList(filterTexSearch(allList));
        }
        if (selectedResource != null) {
            scrollTextures.setSelected(selectedResource.getResourcePath());
        }
        scrollTextures.guiLeft = guiLeft + 95;
        scrollTextures.guiTop = guiTop + 14;
        this.addScroll(scrollTextures);
        addTextField(new GuiNpcTextField(66, this, fontRendererObj, guiLeft + 95, guiTop + 179, 160, 20, texSearch));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawTexturePreview();
    }

    private void drawTexturePreview() {
        int previewX = guiLeft + 260;
        int previewY = guiTop + 14;

        // Draw border/background for preview area
        drawRect(previewX - 1, previewY - 1, previewX + PREVIEW_SIZE + 1, previewY + PREVIEW_SIZE + 1, 0xFFA0A0A0);
        drawRect(previewX, previewY, previewX + PREVIEW_SIZE, previewY + PREVIEW_SIZE, 0xFF000000);

        if (selectedResource == null) {
            String none = StatCollector.translateToLocal("gui.none");
            int w = fontRendererObj.getStringWidth(none);
            fontRendererObj.drawString(none, previewX + (PREVIEW_SIZE - w) / 2, previewY + PREVIEW_SIZE / 2 - 4, 0xFF808080);
            return;
        }

        ImageData imageData = ClientCacheHandler.getImageData(selectedResource.toString());
        if (imageData == null || !imageData.imageLoaded()) {
            String loading = "...";
            int w = fontRendererObj.getStringWidth(loading);
            fontRendererObj.drawString(loading, previewX + (PREVIEW_SIZE - w) / 2, previewY + PREVIEW_SIZE / 2 - 4, 0xFF808080);
            return;
        }

        int texW = imageData.getTotalWidth();
        int texH = imageData.getTotalHeight();
        if (texW <= 0 || texH <= 0) return;

        // Scale to fit within PREVIEW_SIZE while maintaining aspect ratio
        float scale = Math.min((float) PREVIEW_SIZE / texW, (float) PREVIEW_SIZE / texH);
        int drawW = (int) (texW * scale);
        int drawH = (int) (texH * scale);
        int drawX = previewX + (PREVIEW_SIZE - drawW) / 2;
        int drawY = previewY + (PREVIEW_SIZE - drawH) / 2;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        imageData.bindTexture();
        drawScaledTexture(drawX, drawY, drawW, drawH);

        // Draw dimensions label below preview
        String dims = texW + " x " + texH;
        int dw = fontRendererObj.getStringWidth(dims);
        fontRendererObj.drawString(dims, previewX + (PREVIEW_SIZE - dw) / 2, previewY + PREVIEW_SIZE + 4, 0xFFCCCCCC);

        // Draw resource path below dimensions
        String path = selectedResource.toString();
        if (fontRendererObj.getStringWidth(path) > PREVIEW_SIZE + 4) {
            // Truncate from the left
            while (fontRendererObj.getStringWidth("..." + path) > PREVIEW_SIZE + 4 && path.length() > 1) {
                path = path.substring(1);
            }
            path = "..." + path;
        }
        int pw = fontRendererObj.getStringWidth(path);
        fontRendererObj.drawString(path, previewX + (PREVIEW_SIZE - pw) / 2, previewY + PREVIEW_SIZE + 15, 0xFF999999);
    }

    private void drawScaledTexture(int x, int y, int width, int height) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + height, zLevel, 0.0, 1.0);
        tess.addVertexWithUV(x + width, y + height, zLevel, 1.0, 1.0);
        tess.addVertexWithUV(x + width, y, zLevel, 1.0, 0.0);
        tess.addVertexWithUV(x, y, zLevel, 0.0, 0.0);
        tess.draw();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);
        if (guibutton.id == 1) {
            // Cancel - clear selection
            selectedResource = null;
        }
        close();
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll == scrollTextures) {
            if (scroll.id == 1) {
                GuiTextureSelection.TextureData data = textures.get(scroll.getSelected());
                if (data != null) {
                    selectedResource = new ResourceLocation(selectedDomain, data.absoluteName);
                }
            }
        } else {
            initGui();
            scrollTextures.resetScroll();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll == scrollCategories) {
            if (selectedDomain == null) {
                selectedDomain = selection;
            } else if (selection.equals(up)) {
                int i = location.lastIndexOf('/', location.length() - 2);
                if (i < 0) {
                    if (location.isEmpty()) {
                        selectedDomain = null;
                    }
                    location = "";
                } else {
                    location = location.substring(0, i + 1);
                }
            } else {
                location = location + selection + '/';
            }
            scrollCategories.selected = -1;
            scrollTextures.selected = -1;
            initGui();
        } else {
            // Double-click on texture = confirm
            close();
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null && getTextField(55).isFocused()) {
            if (!catSearch.equals(getTextField(55).getText())) {
                catSearch = getTextField(55).getText().toLowerCase();
                scrollCategories.resetScroll();
                if (selectedDomain == null) {
                    scrollCategories.setList(getCatSearch());
                }
            }
        }
        if (getTextField(66) != null && getTextField(66).isFocused()) {
            if (!texSearch.equals(getTextField(66).getText())) {
                texSearch = getTextField(66).getText().toLowerCase();
                scrollTextures.resetScroll();
                initGui();
            }
        }
    }

    private List<String> getCatSearch() {
        if (catSearch.isEmpty()) {
            return new ArrayList<>(domains.keySet());
        }
        List<String> list = new ArrayList<>();
        for (String name : domains.keySet()) {
            if (name.toLowerCase().contains(catSearch))
                list.add(name);
        }
        return list;
    }

    private List<String> filterTexSearch(List<String> source) {
        if (texSearch.isEmpty()) {
            return source;
        }
        List<String> list = new ArrayList<>();
        for (String name : source) {
            if (name.toLowerCase().contains(texSearch))
                list.add(name);
        }
        return list;
    }
}
