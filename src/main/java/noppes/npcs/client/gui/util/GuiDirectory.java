package noppes.npcs.client.gui.util;

/**
 * Abstract base class for fullscreen directory-browsing GUIs.
 * Provides a configurable three-panel layout (left nav, center content, right actions)
 * with percentage-based sizing, panel backgrounds/borders, and an optional resizable divider.
 * <p>
 * Subclasses override abstract hooks to populate each panel region.
 * Designed for reuse across Cloner, Quest Manager, Dialog Manager, etc.
 */
public abstract class GuiDirectory extends GuiNPCInterface implements ICustomScrollListener, ISubGuiListener {

    // ===== LAYOUT CONFIG (subclasses may override in constructor) =====
    protected float leftPanelPercent = 0.20f;
    protected float rightPanelPercent = 0.10f;
    protected int minLeftPanelW = 80;
    protected int minRightPanelW = 40;
    protected int minCenterW = 100;

    // ===== LAYOUT CONSTANTS =====
    protected int pad = 10;
    protected int topBarH = 24;
    protected int gap = 4;
    protected int btnH = 20;

    // ===== PANEL COLORS =====
    protected int panelBg = 0xC0101010;
    protected int panelBorder = 0xFF333333;
    protected int topBarBg = 0xC0181818;

    // ===== COMPUTED LAYOUT (populated by computeLayout) =====
    protected int originX, originY, usableW, usableH;
    protected int leftPanelW, rightPanelW;
    protected int contentX, contentY, contentW, contentH;
    protected int rightX;

    // ===== OPTIONAL RESIZABLE DIVIDER (between left and center) =====
    protected boolean enableDivider = false;
    protected int dividerWidth = 5;
    protected int dividerLineHeight = 20;
    protected int minDividerPanelW = 50;
    private int dividerOffset;
    private boolean isDragging = false;
    private int dragStartX;

    // ===== CONSTRUCTOR =====
    public GuiDirectory() {
        super();
        closeOnEsc = true;
        drawDefaultBackground = true;
    }

    // ===== LAYOUT COMPUTATION =====
    protected void computeLayout() {
        originX = pad;
        originY = pad;
        usableW = width - 2 * pad;
        usableH = height - 2 * pad;

        leftPanelW = Math.max(minLeftPanelW, (int) (usableW * leftPanelPercent));
        rightPanelW = Math.max(minRightPanelW, (int) (usableW * rightPanelPercent));

        if (rightPanelPercent <= 0) {
            rightPanelW = 0;
        }

        int gapCount = rightPanelW > 0 ? 3 : 2;
        contentW = usableW - leftPanelW - rightPanelW - gapCount * gap;
        if (contentW < minCenterW) {
            contentW = minCenterW;
        }

        contentX = originX + leftPanelW + gap;
        contentY = originY + topBarH + gap;
        contentH = usableH - topBarH - gap;
        rightX = contentX + contentW + gap;

        if (enableDivider && dividerOffset == 0) {
            dividerOffset = leftPanelW;
        }
    }

    // ===== INIT GUI =====
    @Override
    public void initGui() {
        super.initGui();
        computeLayout();
        initTopBar(originY + 2);
        initLeftPanel();
        initCenterPanel();
        if (rightPanelW > 0) {
            initRightPanel(contentY + 4);
        }
    }

    // ===== DRAWING =====
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawTopBar();
        drawPanels();
        if (enableDivider) {
            drawDivider(mouseX, mouseY);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawOverlay(mouseX, mouseY, partialTicks);
    }

    protected void drawTopBar() {
        GuiUtil.drawRectD(originX, originY, originX + usableW, originY + topBarH, topBarBg);
    }

    protected void drawPanels() {
        // Left nav panel border
        GuiUtil.drawRectD(originX - 1, contentY - 1, originX + leftPanelW + 1, originY + usableH + 1, panelBorder);

        // Right action panel border (if present)
        if (rightPanelW > 0) {
            GuiUtil.drawRectD(rightX - 1, contentY - 1, rightX + rightPanelW + 1, originY + usableH + 1, panelBorder);
        }
    }

    protected void drawDivider(int mouseX, int mouseY) {
        if (!enableDivider) return;
        int divX = originX + dividerOffset;
        int regionTop = contentY;
        int regionHeight = contentH;
        int handleTop = regionTop + (regionHeight - dividerLineHeight) / 2;
        drawRect(divX + 1, handleTop, divX + dividerWidth - 1, handleTop + dividerLineHeight, 0xFF707070);
    }

    /**
     * Override for extra drawing after super.drawScreen (e.g. status text)
     */
    protected void drawOverlay(int mouseX, int mouseY, float partialTicks) {
    }

    // ===== DIVIDER MOUSE HANDLING =====
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (enableDivider && !hasSubGui()) {
            int divX = originX + dividerOffset;
            int regionTop = contentY;
            int handleTop = regionTop + (contentH - dividerLineHeight) / 2;
            int handleBottom = handleTop + dividerLineHeight;
            if (mouseX >= divX && mouseX <= divX + dividerWidth &&
                mouseY >= handleTop && mouseY <= handleBottom) {
                isDragging = true;
                resizingActive = true;
                dragStartX = mouseX;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isDragging) {
            int dx = mouseX - dragStartX;
            dragStartX = mouseX;
            dividerOffset += dx;

            int maxOffset = (usableW - (rightPanelW > 0 ? rightPanelW + gap : 0)) - dividerWidth - minDividerPanelW;
            dividerOffset = Math.max(minDividerPanelW, Math.min(dividerOffset, maxOffset));

            onDividerMoved(dividerOffset);
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (isDragging) {
            isDragging = false;
            resizingActive = false;
            return;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    /**
     * Called when divider is dragged. Override to update scroll sizes.
     */
    protected void onDividerMoved(int newOffset) {
    }

    // Accessors for divider offset
    protected int getDividerOffset() {
        return dividerOffset;
    }

    protected void setDividerOffset(int offset) {
        this.dividerOffset = offset;
    }

    // ===== ABSTRACT HOOKS (subclass must implement) =====

    /**
     * Populate top bar buttons. topBtnY is the y-coordinate for the button row.
     */
    protected abstract void initTopBar(int topBtnY);

    /**
     * Populate the left navigation panel (scrolls, search fields, buttons).
     */
    protected abstract void initLeftPanel();

    /**
     * Populate the center content panel (main scroll list).
     */
    protected abstract void initCenterPanel();

    /**
     * Populate the right action panel. startY is the first button y-coordinate.
     */
    protected abstract void initRightPanel(int startY);
}
