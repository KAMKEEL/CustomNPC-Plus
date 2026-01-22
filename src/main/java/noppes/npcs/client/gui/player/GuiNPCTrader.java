package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.player.GetTraderData;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiNPCTrader extends GuiContainerNPCInterface implements IGuiData {
    private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/tradersetup.png");
    private final ResourceLocation slot = new ResourceLocation("customnpcs", "textures/gui/slot.png");
    private RoleTrader role;
    private ContainerNPCTrader container;

    // Layout constants matching ContainerNPCTrader
    private static final int COLUMN_WIDTH = ContainerNPCTrader.COLUMN_WIDTH;
    private static final int COLUMN_START_X = ContainerNPCTrader.COLUMN_START_X;
    private static final int ROW_HEIGHT = ContainerNPCTrader.ROW_HEIGHT;
    private static final int ROW_START_Y = ContainerNPCTrader.ROW_START_Y;
    private static final int CURRENCY1_OFFSET = ContainerNPCTrader.CURRENCY1_OFFSET;
    private static final int CURRENCY2_OFFSET = ContainerNPCTrader.CURRENCY2_OFFSET;
    private static final int OUTPUT_OFFSET = ContainerNPCTrader.OUTPUT_OFFSET;

    // Data received from server via IGuiData
    private long playerBalance = 0;
    private int[] availableStock = new int[18];
    private long[] currencyCost = new long[18];
    private boolean stockEnabled = false;

    // Client-side countdown: target time when stock resets (System.currentTimeMillis())
    private long resetTargetTime = -1;

    // Current hover status for bottom display
    private String statusMessage = "";
    private int statusColor = 0xFFFFFF;

    public GuiNPCTrader(EntityNPCInterface npc, ContainerNPCTrader container) {
        super(npc, container);
        this.container = container;
        role = (RoleTrader) npc.roleInterface;
        closeOnEsc = true;
        ySize = 216;
        xSize = 256;
        this.title = "";

        // Initialize stock to unlimited
        for (int i = 0; i < 18; i++) {
            availableStock[i] = Integer.MAX_VALUE;
            currencyCost[i] = 0;
        }

        // Request trader data from server (balance, stock, currency costs)
        PacketClient.sendClient(new GetTraderData());
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        playerBalance = compound.getLong("Balance");
        stockEnabled = compound.getBoolean("StockEnabled");

        // Calculate target time for client-side countdown
        long remainingMillis = compound.getLong("ResetTimeMillis");
        if (remainingMillis > 0) {
            resetTargetTime = System.currentTimeMillis() + remainingMillis;
        } else {
            resetTargetTime = -1;
        }

        int[] stock = compound.getIntArray("Stock");
        if (stock != null && stock.length == 18) {
            availableStock = stock;
        }

        for (int i = 0; i < 18; i++) {
            currencyCost[i] = compound.getLong("Cost" + i);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
        // Reset status each frame
        statusMessage = "";
        statusColor = 0xFFFFFF;

        this.drawWorldBackground(0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw "Trader" label in TOP LEFT (above the GUI)
        String traderLabel = StatCollector.translateToLocal("role.trader");
        fontRendererObj.drawString(traderLabel, guiLeft + 4, guiTop - 8, 0xFFFFFF);

        // Draw reset timer in TOP RIGHT (above the GUI)
        if (stockEnabled && resetTargetTime > 0) {
            String timerText = getResetCountdownText();
            int timerWidth = fontRendererObj.getStringWidth(timerText);
            int timerColor = timerText.equals(StatCollector.translateToLocal("trader.reopentrader")) ? 0xFFAA00 : 0xFFFFFF;
            fontRendererObj.drawString(timerText, guiLeft + xSize - timerWidth - 4, guiTop - 8, timerColor);
        }

        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_LIGHTING);

        // Draw trade slots (3 columns x 6 rows, 80px column width)
        for (int slotIdx = 0; slotIdx < 18; slotIdx++) {
            int col = slotIdx % 3;
            int row = slotIdx / 3;
            int x = guiLeft + COLUMN_START_X + col * COLUMN_WIDTH;
            int y = guiTop + ROW_START_Y + row * ROW_HEIGHT;

            // Get item currencies
            ItemStack item = role.inventoryCurrency.items.get(slotIdx);
            ItemStack item2 = role.inventoryCurrency.items.get(slotIdx + 18);
            if (item == null) {
                item = item2;
                item2 = null;
            }
            if (item != null && item2 != null && NoppesUtilPlayer.compareItems(item, item2, role.ignoreDamage, role.ignoreNBT)) {
                item = item.copy();
                item.stackSize += item2.stackSize;
                item2 = null;
            }

            ItemStack sold = role.inventorySold.items.get(slotIdx);

            // Draw slot backgrounds (18x18 each)
            GL11.glColor4f(1, 1, 1, 1);
            mc.renderEngine.bindTexture(this.slot);

            // Currency slot 1
            drawTexturedModalRect(x + CURRENCY1_OFFSET, y, 0, 0, 18, 18);
            // Currency slot 2
            drawTexturedModalRect(x + CURRENCY2_OFFSET, y, 0, 0, 18, 18);
            // Output slot
            drawTexturedModalRect(x + OUTPUT_OFFSET, y, 0, 0, 18, 18);

            // "=" sign between currency and output
            fontRendererObj.drawString("=", x + 40, y + 5, CustomNpcResourceListener.DefaultTextColor);

            // Draw currency items (centered: +1 offset for 16x16 item in 18x18 slot)
            if (sold != null) {
                RenderHelper.enableGUIStandardItemLighting();

                // Draw secondary currency (slot 1)
                if (item2 != null) {
                    itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, item2, x + CURRENCY1_OFFSET + 1, y + 1);
                    itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, item2, x + CURRENCY1_OFFSET + 1, y + 1);
                }

                // Draw primary currency (slot 2)
                if (item != null) {
                    GL11.glColor4f(1, 1, 1, 1);
                    itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine, item, x + CURRENCY2_OFFSET + 1, y + 1);
                    itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine, item, x + CURRENCY2_OFFSET + 1, y + 1);
                }

                RenderHelper.disableStandardItemLighting();

                // Draw stock count to the RIGHT of the trade (after output slot)
                if (stockEnabled && availableStock[slotIdx] < Integer.MAX_VALUE) {
                    String stockText = String.valueOf(availableStock[slotIdx]);
                    int stockColor = availableStock[slotIdx] > 0 ? 0x00AA00 : 0xAA0000;
                    fontRendererObj.drawString(stockText, x + OUTPUT_OFFSET + 20, y + 5, stockColor);
                }

                // Draw dark overlay for out of stock items
                if (stockEnabled && availableStock[slotIdx] <= 0) {
                    drawGradientRect(x + OUTPUT_OFFSET + 1, y + 1, x + OUTPUT_OFFSET + 17, y + 17, 0xC0101010, 0xC0101010);
                }
            }
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();

        super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);

        // Draw player balance (BOTTOM RIGHT, below GUI)
        int infoY = guiTop + ySize + 2;
        String balanceText = StatCollector.translateToLocal("trader.balance") + ": $" + formatCurrency(playerBalance);
        int balanceWidth = fontRendererObj.getStringWidth(balanceText);
        fontRendererObj.drawString(balanceText, guiLeft + xSize - balanceWidth - 4, infoY, 0x36e82a);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        for (int slotIdx = 0; slotIdx < 18; slotIdx++) {
            int col = slotIdx % 3;
            int row = slotIdx / 3;
            int x = COLUMN_START_X + col * COLUMN_WIDTH;
            int y = ROW_START_Y + row * ROW_HEIGHT;

            ItemStack item = role.inventoryCurrency.items.get(slotIdx);
            ItemStack item2 = role.inventoryCurrency.items.get(slotIdx + 18);
            if (item == null) {
                item = item2;
                item2 = null;
            }
            if (item != null && item2 != null && NoppesUtilPlayer.compareItems(item, item2, role.ignoreDamage, role.ignoreNBT)) {
                item = item.copy();
                item.stackSize += item2.stackSize;
                item2 = null;
            }

            ItemStack sold = role.inventorySold.items.get(slotIdx);
            if (sold == null) {
                continue;
            }

            // Check if hovering over output slot (18x18 area)
            if (this.func_146978_c(x + OUTPUT_OFFSET, y, 18, 18, mouseX, mouseY)) {
                updateSlotStatus(slotIdx, item, item2, x, y);
            }

            // Tooltips for currency items
            if (this.func_146978_c(x + CURRENCY1_OFFSET, y, 18, 18, mouseX, mouseY) && item2 != null) {
                this.renderToolTip(item2, mouseX - guiLeft, mouseY - guiTop);
            }
            if (this.func_146978_c(x + CURRENCY2_OFFSET, y, 18, 18, mouseX, mouseY) && item != null) {
                this.renderToolTip(item, mouseX - guiLeft, mouseY - guiTop);
            }
        }

        // Draw status message in BOTTOM LEFT (in foreground coordinates)
        if (!statusMessage.isEmpty()) {
            fontRendererObj.drawString(statusMessage, 4, ySize + 4, statusColor);
        }
    }

    /**
     * Update status message when hovering over a trade slot
     */
    private void updateSlotStatus(int slotIdx, ItemStack item, ItemStack item2, int x, int y) {
        long cost = currencyCost[slotIdx];
        String costSuffix = cost > 0 ? " ($" + formatCurrency(cost) + ")" : "";

        // Check conditions in order of priority
        if (stockEnabled && availableStock[slotIdx] <= 0) {
            // Out of stock
            statusMessage = StatCollector.translateToLocal("trader.outofstock") + costSuffix;
            statusColor = 0xDD0000;
        } else if (cost > 0 && playerBalance < cost) {
            // Can't afford currency cost
            statusMessage = StatCollector.translateToLocal("trader.insufficient.currency") + costSuffix;
            statusColor = 0xDD0000;
        } else if (!container.canBuy(slotIdx, player)) {
            // Missing item currency - highlight which ones
            GL11.glTranslatef(0, 0, 300);
            if (item != null && !NoppesUtilPlayer.compareItems(player, item, role.ignoreDamage, role.ignoreNBT)) {
                this.drawGradientRect(x + CURRENCY2_OFFSET, y, x + CURRENCY2_OFFSET + 18, y + 18, 0x70771010, 0x70771010);
            }
            if (item2 != null && !NoppesUtilPlayer.compareItems(player, item2, role.ignoreDamage, role.ignoreNBT)) {
                this.drawGradientRect(x + CURRENCY1_OFFSET, y, x + CURRENCY1_OFFSET + 18, y + 18, 0x70771010, 0x70771010);
            }
            GL11.glTranslatef(0, 0, -300);
            statusMessage = StatCollector.translateToLocal("trader.unavailable") + costSuffix;
            statusColor = 0xDD0000;
        } else if (!container.isSlotEnabled(slotIdx, player)) {
            // Slot disabled
            statusMessage = StatCollector.translateToLocal("trader.slotdisabled") + costSuffix;
            statusColor = 0xFF4000;
        } else {
            // Can buy!
            statusMessage = StatCollector.translateToLocal("trader.available") + costSuffix;
            statusColor = 0x00DD00;
        }
    }

    /**
     * Get the countdown text for stock reset timer.
     * Calculated client-side each frame for smooth countdown.
     */
    private String getResetCountdownText() {
        if (resetTargetTime <= 0) {
            return "";
        }

        long remaining = resetTargetTime - System.currentTimeMillis();
        if (remaining <= 0) {
            return StatCollector.translateToLocal("trader.reopentrader");
        }

        // Format as HH:MM:SS or D days HH:MM:SS
        long seconds = (remaining / 1000) % 60;
        long minutes = (remaining / 60000) % 60;
        long hours = (remaining / 3600000) % 24;
        long days = remaining / 86400000;

        StringBuilder sb = new StringBuilder();
        sb.append(StatCollector.translateToLocal("trader.restock")).append(": ");

        if (days > 0) {
            sb.append(days).append("d ");
        }
        sb.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        return sb.toString();
    }

    /**
     * Format currency for display (e.g., 1000 -> "1,000")
     */
    private String formatCurrency(long amount) {
        if (amount < 1000) {
            return String.valueOf(amount);
        }
        StringBuilder sb = new StringBuilder();
        String str = String.valueOf(amount);
        int count = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                sb.insert(0, ',');
            }
            sb.insert(0, str.charAt(i));
            count++;
        }
        return sb.toString();
    }

    @Override
    public void save() {
    }
}
