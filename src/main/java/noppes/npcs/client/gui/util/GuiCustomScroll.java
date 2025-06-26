package noppes.npcs.client.gui.util;

import kamkeel.npcs.util.TextSplitter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static noppes.npcs.client.gui.util.GuiNPCInterface.resizingActive;

public class GuiCustomScroll extends GuiScreen {
    public static final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/misc.png");
    public List<String> list;
    public final HashMap<String, Integer> colors = new HashMap<>();

    public int id;
    public int guiLeft = 0;
    public int guiTop = 0;
    public int xSize, ySize;
    public int selected;
    protected HashSet<String> selectedList;
    protected int hover;
    protected int oldHover;
    private int hoverCount = 0;
    protected boolean hoverableText = false;
    public int listHeight;
    protected int scrollY;
    protected int maxScrollY;
    protected int scrollHeight;
    private boolean isScrolling;
    public boolean multipleSelection = false;
    private ICustomScrollListener listener;
    private boolean isSorted = true;
    public boolean visible = true;
    private boolean selectable = true;
    private boolean hasSubGUI = false;

    private int lastClickedItem;
    private long lastClickedTime = 0;

    public GuiCustomScroll(GuiScreen parent, int id) {
        width = 176;
        height = 166;
        xSize = 176;
        ySize = 159;
        selected = -1;
        hover = -1;
        selectedList = new HashSet<String>();
        listHeight = 0;
        scrollY = 0;
        scrollHeight = 0;
        isScrolling = false;
        if (parent instanceof ICustomScrollListener)
            listener = (ICustomScrollListener) parent;
        this.list = new ArrayList<String>();
        this.id = id;
    }

    public GuiCustomScroll(GuiScreen parent, int id, boolean multipleSelection) {
        this(parent, id);
        this.multipleSelection = multipleSelection;
    }

    public GuiCustomScroll(GuiScreen parent, int id, int allowHover) {
        this(parent, id);
        this.hoverableText = true;
    }

    public void setSize(int x, int y) {
        ySize = y;
        xSize = x;
        listHeight = 14 * list.size();

        if (listHeight > 0)
            scrollHeight = (int) (((double) (ySize - 8) / (double) listHeight) * (ySize - 8));
        else
            scrollHeight = Integer.MAX_VALUE;

        maxScrollY = listHeight - (ySize - 8) - 1;
    }

    public void drawScreen(int i, int j, float f, int mouseScrolled) {
        if (!visible)
            return;

        drawGradientRect(guiLeft, guiTop, xSize + guiLeft, ySize + guiTop, 0xc0101010, 0xd0101010);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);

        if (scrollHeight < ySize - 8) {
            drawScrollBar();
        }
        GL11.glPushMatrix();
        GL11.glRotatef(180F, 1.0F, 0.0F, 0.0F);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(guiLeft, guiTop, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (selectable)
            hover = getMouseOver(i, j);

        drawItems();

        if (oldHover != hover) {
            oldHover = hover;
            hoverCount = 0;
        } else {
            if (hoverCount < 110)
                hoverCount++;
        }

        if (!hasSubGUI && hover != -1 && hoverableText && hoverCount > 100) {
            String displayString = StatCollector.translateToLocal(list.get(hover));
            GL11.glColor4f(1, 1, 1, 1);
            List<String> lines = TextSplitter.splitText(displayString, 30);
            super.drawHoveringText(lines, i - guiLeft, j - guiTop, this.fontRendererObj);
            GL11.glDisable(GL11.GL_LIGHTING);
        }

        GL11.glPopMatrix();
        if (scrollHeight < ySize - 8) {
            i -= guiLeft;
            j -= guiTop;
            if (Mouse.isButtonDown(0) && !resizingActive) {
                if (i >= xSize - 9 && i < xSize - 4 && j >= 4 && j < ySize) {
                    isScrolling = true;
                }
            } else
                isScrolling = false;

            if (isScrolling) {
                scrollY = (((j - 8) * listHeight) / (ySize - 8)) - (scrollHeight);
                if (scrollY < 0) {
                    scrollY = 0;
                }
                if (scrollY > maxScrollY) {
                    scrollY = maxScrollY;
                }
            }

            if (mouseScrolled != 0) {
                scrollY += mouseScrolled > 0 ? -14 : 14;
                if (scrollY > maxScrollY)
                    scrollY = maxScrollY;
                if (scrollY < 0)
                    scrollY = 0;
            }
        }
    }

    public void drawHover(int i, int j) {
        if (!visible)
            return;

        if (!hoverableText)
            return;

        GL11.glPushMatrix();
        GL11.glTranslatef(guiLeft, guiTop, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (oldHover != hover) {
            oldHover = hover;
            hoverCount = 0;
        } else {
            if (hoverCount < 110)
                hoverCount++;
        }

        if (!hasSubGUI && hover != -1 && hoverableText && hoverCount > 100) {
            String displayString = StatCollector.translateToLocal(list.get(hover));
            GL11.glColor4f(1, 1, 1, 1);
            List<String> lines = TextSplitter.splitText(displayString, 30);
            super.drawHoveringText(lines, i - guiLeft, j - guiTop, this.fontRendererObj);
            GL11.glDisable(GL11.GL_LIGHTING);
        }

        GL11.glPopMatrix();
    }

    public boolean mouseInOption(int i, int j, int k) {
        int l = 4;
        int i1 = ((14 * k + 4) - scrollY);
        return i >= l - 1 && i < l + xSize - 11 && j >= i1 - 1 && j < i1 + 8;
    }

    protected void drawItems() {
        for (int i = 0; i < list.size(); i++) {
            int j = 4;
            int k = (14 * i + 4) - scrollY;
            if (k >= 4 && k + 12 < ySize) {
                int xOffset = scrollHeight < ySize - 8 ? 0 : 10;
                String displayString = StatCollector.translateToLocal(list.get(i));

                String text = "";
                float maxWidth = (xSize + xOffset - 8) * 0.8f;
                if (fontRendererObj.getStringWidth(displayString) > maxWidth) {
                    for (int h = 0; h < displayString.length(); h++) {
                        char c = displayString.charAt(h);
                        text += c;
                        if (fontRendererObj.getStringWidth(text) > maxWidth)
                            break;
                    }
                    if (displayString.length() > text.length())
                        text += "...";
                } else
                    text = displayString;
                if ((multipleSelection && selectedList.contains(text)) || (!multipleSelection && selected == i)) {
                    drawVerticalLine(j - 2, k - 4, k + 10, 0xffffffff);
                    drawVerticalLine(j + xSize - 18 + xOffset, k - 4, k + 10, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k - 3, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k + 10, 0xffffffff);
                    fontRendererObj.drawString(text, j, k, 0xffffff);
                } else if (i == hover) {
                    fontRendererObj.drawString(text, j, k, 0x00ff00);
                } else
                    fontRendererObj.drawString(text, j, k, 0xffffff);
            }
        }

    }

    public String getSelected() {
        if (selected == -1 || selected >= list.size())
            return null;
        return list.get(selected);
    }

    private int getMouseOver(int i, int j) {
        i -= guiLeft;
        j -= guiTop;
        //fontRenderer.drawString(".", i , j, 0xffffff);
        if (i >= 4 && i < xSize - 4 && j >= 4 && j < ySize) {
            for (int j1 = 0; j1 < list.size(); j1++) {
                if (!mouseInOption(i, j, j1)) {
                    continue;
                }

                return j1;
            }

        }

        return -1;
    }

    public void mouseClicked(int i, int j, int k) {
        if (resizingActive)
            return;

        if (k != 0 || hover < 0)
            return;
        if (multipleSelection) {
            if (selectedList.contains(list.get(hover))) {
                selectedList.remove(list.get(hover));
            } else {
                selectedList.add(list.get(hover));
            }
        } else {
            if (hover >= 0)
                selected = hover;
            hover = -1;
        }

        if (listener != null) {
            long time = System.currentTimeMillis();
            listener.customScrollClicked(i, j, k, this);
            if (selected >= 0 && selected == lastClickedItem && time - lastClickedTime < 500) {
                listener.customScrollDoubleClicked(list.get(selected), this);
            }
            lastClickedTime = time;
            lastClickedItem = selected;
        }
    }

    private void drawScrollBar() {
        int i = guiLeft + xSize - 9;
        int j = guiTop + (int) (((double) scrollY / (double) listHeight) * (double) (ySize - 8)) + 4;
        int k = j;
        drawTexturedModalRect(i, k, xSize, 9, 5, 1);
        for (k++; k < (j + scrollHeight) - 1; k++) {
            drawTexturedModalRect(i, k, xSize, 10, 5, 1);
        }

        drawTexturedModalRect(i, k, xSize, 11, 5, 1);
    }

    public boolean hasSelected() {

        return selected >= 0;
    }

    public void setList(List<String> list) {
        isSorted = true;
        list.removeAll(Collections.singleton(null));
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        this.list = list;
        setSize(xSize, ySize);
    }

    public void setList(List<String> list, boolean ascending, boolean caseOrder) {
        isSorted = true;
        if (caseOrder) {
            Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        }
        if (!ascending) {
            Collections.reverse(list);
        }
        this.list = list;
        setSize(xSize, ySize);
    }

    public void setUnsortedList(List<String> list) {
        isSorted = false;
        this.list = list;
        setSize(xSize, ySize);
    }

    public void replace(String old, String name) {
        String select = getSelected();
        list.remove(old);
        list.add(name);
        if (isSorted)
            Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        if (old.equals(select))
            select = name;

        selected = list.indexOf(select);
        setSize(xSize, ySize);
    }

    public void setSelected(String name) {
        selected = list.indexOf(name);
    }

    public void clear() {
        list = new ArrayList<String>();
        selected = -1;
        scrollY = 0;
        setSize(xSize, ySize);
    }

    public List<String> getList() {
        return list;
    }

    public HashSet<String> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(HashSet<String> selectedList) {
        this.selectedList = selectedList;
    }

    public GuiCustomScroll setUnselectable() {
        selectable = false;
        return this;
    }

    public boolean isMouseOver(int x, int y) {
        return x >= this.guiLeft && x <= this.guiLeft + this.xSize && y >= this.guiTop && y <= this.guiTop + this.ySize;
    }

    public void resetScroll() {
        scrollY = 0;
    }

    public void updateSubGUI(boolean hasSubGUI) {
        this.hasSubGUI = hasSubGUI;
    }
}
