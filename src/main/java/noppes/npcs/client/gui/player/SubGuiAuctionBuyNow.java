package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Sub GUI for confirming a buyout purchase.
 * Shows buyout price, player balance, and confirmation buttons.
 */
public class SubGuiAuctionBuyNow extends SubGuiInterface {
    private int btnConfirmId = 10;
    private int btnCancelId = 11;

    private final String listingId;
    private final long buyoutPrice;
    private final long playerBalance;
    private boolean successful = false;

    public SubGuiAuctionBuyNow(String listingId, long buyoutPrice, long playerBalance) {
        this.listingId = listingId;
        this.buyoutPrice = buyoutPrice;
        this.playerBalance = playerBalance;

        setBackground("menubg.png");
        xSize = 220;
        ySize = 130;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        int centerX = guiLeft + xSize / 2;
        int y = guiTop + 15;
        String currencyName = AuctionClientConfig.getCurrencyName();

        // Title
        String title = StatCollector.translateToLocal("auction.buyout.title");
        int titleWidth = fontRendererObj.getStringWidth(title);
        addLabel(new GuiNpcLabel(0, title, centerX - titleWidth / 2, y));
        y += 20;

        // Confirmation message
        String confirmMsg = StatCollector.translateToLocal("auction.buyout.confirm");
        int confirmWidth = fontRendererObj.getStringWidth(confirmMsg);
        addLabel(new GuiNpcLabel(1, confirmMsg, centerX - confirmWidth / 2, y));
        y += 16;

        // Buyout price
        String priceText = EnumChatFormatting.DARK_RED + formatCurrency(buyoutPrice) + " " + currencyName;
        int priceWidth = fontRendererObj.getStringWidth(priceText);
        addLabel(new GuiNpcLabel(2, priceText, centerX - priceWidth / 2, y));
        y += 20;

        // Your balance
        String balanceLabel = StatCollector.translateToLocal("auction.bid.yourBalance");
        String balanceText = balanceLabel.replace("%s", EnumChatFormatting.DARK_GREEN + formatCurrency(playerBalance) + " " + currencyName);
        addLabel(new GuiNpcLabel(3, balanceText, guiLeft + 20, y));
        y += 14;

        // Insufficient funds warning
        if (playerBalance < buyoutPrice) {
            String warning = StatCollector.translateToLocal("auction.error.notEnoughCurrency");
            addLabel(new GuiNpcLabel(4, EnumChatFormatting.RED + warning, guiLeft + 20, y, 0xFFFFFF));
        }
        y += 20;

        // Buttons
        int btnWidth = 70;
        int btnSpacing = 20;
        int totalBtnWidth = btnWidth * 2 + btnSpacing;
        int btnX = centerX - totalBtnWidth / 2;

        GuiNpcButton confirmBtn = new GuiNpcButton(btnConfirmId, btnX, y, btnWidth, 20, "gui.yes");
        confirmBtn.enabled = playerBalance >= buyoutPrice;
        addButton(confirmBtn);
        addButton(new GuiNpcButton(btnCancelId, btnX + btnWidth + btnSpacing, y, btnWidth, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiButton button) {
        if (button.id == btnConfirmId) {
            if (playerBalance >= buyoutPrice) {
                confirmPurchase();
            }
        } else if (button.id == btnCancelId) {
            close();
        }
    }

    private void confirmPurchase() {
        // Send buyout packet
        AuctionActionPacket.buyout(listingId);
        successful = true;
        close();
    }

    public boolean wasSuccessful() {
        return successful;
    }

    private String formatCurrency(long amount) {
        if (amount < 1000) return String.valueOf(amount);
        StringBuilder sb = new StringBuilder();
        String str = String.valueOf(amount);
        int count = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) sb.insert(0, ',');
            sb.insert(0, str.charAt(i));
            count++;
        }
        return sb.toString();
    }
}
