package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiAuctionNavButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.containers.ContainerAuctionSell;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for creating auction listings.
 * Sell slot interactions: left-click=clear, right-click=remove 1.
 * Inventory interactions: left-click=add stack, right-click=add 1.
 */
public class GuiAuctionSell extends GuiAuctionInterface implements ITextfieldListener {
    private static final ResourceLocation ICON_COIN = new ResourceLocation("customnpcs", "textures/items/npcCoinBronze.png");
    private static final ResourceLocation ICON_BAG = new ResourceLocation("customnpcs", "textures/items/npcBag.png");

    // Layout
    private int contentX = 58;
    private int contentY = 50;
    private int confirmBtnX = 200;
    private int confirmBtnY = 118;
    private int navYBuyout = 109;

    // Component IDs
    private int txtStartingPriceId = 1;
    private int txtBuyoutPriceId = 2;
    private int btnConfirmId = 10;
    private int btnAllowBuyoutId = 11;

    private final ContainerAuctionSell sellContainer;
    private long startingPrice = 0;
    private long buyoutPrice = 0;
    private boolean allowBuyout = true;
    private String errorMessage = null;

    // Buttons
    private GuiAuctionNavButton btnAllowBuyout;
    private GuiNpcLabel lblBuyout;
    private GuiNpcTextField txtBuyout;

    public GuiAuctionSell(EntityNPCInterface npc, ContainerAuctionSell container) {
        super(npc, container);
        this.sellContainer = container;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + contentY;

        // Starting Price label and field
        addLabel(new GuiNpcLabel(1, "auction.sell.startingPrice", guiLeft + contentX, y + 5, 0xFFFFFF));
        GuiNpcTextField startingField = new GuiNpcTextField(txtStartingPriceId, this, fontRendererObj,
            guiLeft + contentX + 80, y, 80, 18, startingPrice > 0 ? String.valueOf(startingPrice) : "");
        startingField.setIntegersOnly();
        addTextField(startingField);

        y += 25;

        // Buyout Price label and field (only visible when allowBuyout is true)
        lblBuyout = new GuiNpcLabel(2, "auction.sell.buyoutPrice", guiLeft + contentX, y + 5, 0xFFFFFF);
        addLabel(lblBuyout);

        txtBuyout = new GuiNpcTextField(txtBuyoutPriceId, this, fontRendererObj,
            guiLeft + contentX + 80, y, 80, 18, buyoutPrice > 0 ? String.valueOf(buyoutPrice) : "");
        txtBuyout.setIntegersOnly();
        addTextField(txtBuyout);

        // Allow Buyout toggle button (18x18 with bag icon)
        btnAllowBuyout = new GuiAuctionNavButton(btnAllowBuyoutId, guiLeft + navX, guiTop + navYBuyout,
            "auction.sell.allowBuyout", ICON_BAG);
        btnAllowBuyout.setToggle(true); // This is a toggle, not a nav button
        btnAllowBuyout.setSelected(allowBuyout);
        addButton(btnAllowBuyout);

        // Confirm button (custom textured button - using GuiAuctionNavButton)
        GuiAuctionNavButton btnConfirm = new GuiAuctionNavButton(btnConfirmId, guiLeft + confirmBtnX - 20, guiTop + confirmBtnY - 10,
            "auction.sell.confirm", ICON_COIN);
        addButton(btnConfirm);

        updateBuyoutVisibility();
        updateBuyoutTooltip();
    }

    @Override
    protected int getCurrentPage() {
        return PAGE_SELL;
    }

    /** Update visibility of buyout price field based on toggle state */
    private void updateBuyoutVisibility() {
        if (lblBuyout != null) {
            lblBuyout.enabled = allowBuyout;
        }
        if (txtBuyout != null) {
            txtBuyout.setVisible(allowBuyout);
            if (!allowBuyout) {
                txtBuyout.setText("");
                buyoutPrice = 0;
            }
        }
    }

    /** Update the buyout toggle button tooltip */
    private void updateBuyoutTooltip() {
        if (btnAllowBuyout == null) return;

        List<String> tooltip = new ArrayList<>();
        tooltip.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("auction.sell.allowBuyout"));
        tooltip.add("");
        if (allowBuyout) {
            tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.sell.buyoutEnabled"));
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.sell.buyoutEnabledDesc"));
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.sell.buyoutEnabledDesc2"));
        } else {
            tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.sell.buyoutDisabled"));
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.sell.buyoutDisabledDesc"));
        }
        tooltip.add("");
        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.sell.clickToToggle"));

        btnAllowBuyout.setCustomTooltip(tooltip);
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == txtStartingPriceId) {
            try {
                startingPrice = Long.parseLong(textfield.getText());
            } catch (NumberFormatException e) {
                startingPrice = 0;
            }
        } else if (textfield.id == txtBuyoutPriceId) {
            try {
                String text = textfield.getText().trim();
                buyoutPrice = text.isEmpty() ? 0 : Long.parseLong(text);
            } catch (NumberFormatException e) {
                buyoutPrice = 0;
            }
        }
        errorMessage = null;
    }

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        if (button.id == btnConfirmId) {
            createListing();
        } else if (button.id == btnAllowBuyoutId) {
            allowBuyout = !allowBuyout;
            btnAllowBuyout.setSelected(allowBuyout);
            updateBuyoutVisibility();
            updateBuyoutTooltip();
            NoppesUtil.clickSound();
        }
    }

    private void createListing() {
        // Get latest values from text fields
        GuiNpcTextField startingField = getTextField(txtStartingPriceId);
        GuiNpcTextField buyoutField = getTextField(txtBuyoutPriceId);

        if (startingField != null) {
            try {
                startingPrice = Long.parseLong(startingField.getText());
            } catch (NumberFormatException e) {
                startingPrice = 0;
            }
        }

        if (buyoutField != null && allowBuyout) {
            try {
                String text = buyoutField.getText().trim();
                buyoutPrice = text.isEmpty() ? 0 : Long.parseLong(text);
            } catch (NumberFormatException e) {
                buyoutPrice = 0;
            }
        } else {
            buyoutPrice = 0;
        }

        // Validate
        ItemStack item = sellContainer.getItemToSell();
        if (item == null) {
            errorMessage = StatCollector.translateToLocal("auction.sell.noItem");
            return;
        }

        if (startingPrice <= 0) {
            errorMessage = StatCollector.translateToLocal("auction.sell.invalidPrice");
            return;
        }

        if (allowBuyout && buyoutPrice > 0 && buyoutPrice < startingPrice) {
            errorMessage = StatCollector.translateToLocal("auction.sell.buyoutTooLow");
            return;
        }

        // Validate player has enough items
        int inventoryCount = sellContainer.countItemInInventory(item);
        if (item.stackSize > inventoryCount) {
            errorMessage = StatCollector.translateToLocal("auction.sell.notEnoughItems");
            return;
        }

        // Send create listing packet
        AuctionActionPacket.createListing(item, startingPrice, allowBuyout ? buyoutPrice : 0);

        // Clear the sell slot (server will also clear it)
        sellContainer.clearSellSlot();

        // Navigate back to listings page
        AuctionActionPacket.openPage(PAGE_LISTINGS);
    }

    @Override
    protected void drawAuctionContent(float partialTicks, int mouseX, int mouseY) {
        // Draw sell slot background (centered in content area)
        int slotX = guiLeft + contentX;
        int slotY = guiTop + contentY + 50;
        drawAuctionSlot(slotX, slotY);

        // Draw listing fee info (uses cached config from server)
        String feeText = StatCollector.translateToLocal("auction.sell.fee")
            .replace("%s", formatCurrency(AuctionClientConfig.getListingFee()) + " " + AuctionClientConfig.getCurrencyName());
        fontRendererObj.drawString(EnumChatFormatting.GRAY + feeText, guiLeft + contentX, guiTop + contentY + 77, 0xFFFFFF);

        // Draw error message if any
        if (errorMessage != null) {
            fontRendererObj.drawString(EnumChatFormatting.RED + errorMessage, guiLeft + contentX, guiTop + contentY + 95, 0xFFFFFF);
        }

        drawSellSlotIndicator(slotX, slotY);
    }

    /** Draw indicator showing staged vs available items */
    private void drawSellSlotIndicator(int slotX, int slotY) {
        ItemStack sellItem = sellContainer.getItemToSell();
        if (sellItem != null) {
            int inventoryCount = sellContainer.countItemInInventory(sellItem);
            String countText = sellItem.stackSize + "/" + inventoryCount;

            // Color: green if valid, red if over limit
            int color = sellItem.stackSize <= inventoryCount ? 0x55FF55 : 0xFF5555;
            fontRendererObj.drawStringWithShadow(countText, slotX + 18 + 4, slotY + 5, color);
        } else {
            // Draw "Place item here" hint
            String hint = StatCollector.translateToLocal("auction.sell.placeItem");
            fontRendererObj.drawString(EnumChatFormatting.GRAY + hint, slotX + 20, slotY + 5, 0xFFFFFF);
        }
    }

    /**
     * Handle sell page click behavior.
     * Sell slot: left=clear, right=remove 1.
     * Inventory: left=add stack, right=add 1.
     */
    @Override
    protected void handleMouseClick(Slot slot, int slotIndex, int mouseButton, int clickType) {
        if (slot == null) return;

        // Sell slot clicks
        if (sellContainer.isSellSlot(slotIndex)) {
            ItemStack sellItem = sellContainer.getItemToSell();
            if (sellItem != null) {
                boolean removeAll = (mouseButton == 0); // Left=clear, Right=remove 1
                sellContainer.removeFromSellSlot(removeAll);
                NoppesUtil.clickSound();
            }
            return;
        }

        // Inventory clicks
        if (sellContainer.isPlayerInventorySlot(slotIndex)) {
            ItemStack sourceStack = slot.getStack();
            if (sourceStack != null) {
                boolean fullStack = (mouseButton == 0); // Left=stack, Right=1 item
                sellContainer.addToSellSlot(slotIndex, fullStack);
                NoppesUtil.clickSound();
            }
            return;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (hasSubGui()) return;

        // Draw tooltips for buttons (icons, not items - item tooltips handled by AuctionTooltipHandler)
        drawSellPageTooltips(mouseX, mouseY);
    }

    /** Draw tooltips for sell page buttons */
    private void drawSellPageTooltips(int mouseX, int mouseY) {
        List<String> tooltip = null;

        // Check all buttons
        GuiNpcButton confirmBtn = getButton(btnConfirmId);
        if (confirmBtn instanceof GuiAuctionNavButton) {
            GuiAuctionNavButton btn = (GuiAuctionNavButton) confirmBtn;
            if (btn.isHovered()) {
                tooltip = btn.getTooltipLines();
            }
        }

        if (btnAllowBuyout != null && btnAllowBuyout.isHovered()) {
            tooltip = btnAllowBuyout.getTooltipLines();
        }

        if (tooltip != null && !tooltip.isEmpty()) {
            drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRendererObj);
        }
    }
}
