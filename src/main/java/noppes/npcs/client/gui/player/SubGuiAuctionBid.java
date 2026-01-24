package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Sub GUI for placing a bid on an auction.
 * Shows minimum bid, player balance, and input field.
 */
public class SubGuiAuctionBid extends SubGuiInterface implements ITextfieldListener {
    private static final int TXT_BID = 1;
    private static final int BTN_BID = 10;
    private static final int BTN_CANCEL = 11;

    private final String listingId;
    private final long minimumBid;
    private final long playerBalance;
    private long bidAmount;
    private boolean successful = false;
    private String errorMessage = null;

    public SubGuiAuctionBid(String listingId, long minimumBid, long playerBalance) {
        this.listingId = listingId;
        this.minimumBid = minimumBid;
        this.playerBalance = playerBalance;
        this.bidAmount = minimumBid;

        setBackground("menubg.png");
        xSize = 200;
        ySize = 120;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        int centerX = guiLeft + xSize / 2;
        int y = guiTop + 15;
        String currencyName = AuctionClientConfig.getCurrencyName();

        // Title
        addLabel(new GuiNpcLabel(0, "auction.bid.title", centerX - 50, y, 0xFFFFFF));
        y += 18;

        // Minimum bid info
        String minText = StatCollector.translateToLocal("auction.bid.minimum")
            .replace("%s", EnumChatFormatting.GOLD + formatCurrency(minimumBid) + " " + currencyName);
        addLabel(new GuiNpcLabel(1, minText, guiLeft + 15, y, 0xAAAAAA));
        y += 14;

        // Your balance
        String balanceText = StatCollector.translateToLocal("auction.bid.yourBalance")
            .replace("%s", EnumChatFormatting.WHITE + formatCurrency(playerBalance) + " " + currencyName);
        addLabel(new GuiNpcLabel(2, balanceText, guiLeft + 15, y, 0xAAAAAA));
        y += 18;

        // Bid amount field
        addLabel(new GuiNpcLabel(3, "auction.bid.yourBid", guiLeft + 15, y + 4, 0xFFFFFF));
        GuiNpcTextField bidField = new GuiNpcTextField(TXT_BID, this, fontRendererObj,
            guiLeft + 80, y, 100, 18, String.valueOf(minimumBid));
        bidField.setIntegersOnly();
        addTextField(bidField);
        y += 28;

        // Error message area (empty initially)
        addLabel(new GuiNpcLabel(4, "", guiLeft + 15, y, 0xFF5555));
        y += 16;

        // Buttons
        int btnWidth = 70;
        int btnSpacing = 20;
        int totalBtnWidth = btnWidth * 2 + btnSpacing;
        int btnX = centerX - totalBtnWidth / 2;

        addButton(new GuiNpcButton(BTN_BID, btnX, y, btnWidth, 20, "auction.bid.placeBid"));
        addButton(new GuiNpcButton(BTN_CANCEL, btnX + btnWidth + btnSpacing, y, btnWidth, 20, "gui.cancel"));
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == TXT_BID) {
            try {
                bidAmount = Long.parseLong(textfield.getText());
            } catch (NumberFormatException e) {
                bidAmount = 0;
            }
            errorMessage = null;
            updateErrorLabel();
        }
    }

    @Override
    public void buttonEvent(GuiButton button) {
        switch (button.id) {
            case BTN_BID:
                placeBid();
                break;
            case BTN_CANCEL:
                close();
                break;
        }
    }

    private void placeBid() {
        // Get latest value from text field
        GuiNpcTextField bidField = getTextField(TXT_BID);
        if (bidField != null) {
            try {
                bidAmount = Long.parseLong(bidField.getText());
            } catch (NumberFormatException e) {
                bidAmount = 0;
            }
        }

        // Validate
        if (bidAmount < minimumBid) {
            errorMessage = StatCollector.translateToLocal("auction.error.bidTooLow")
                .replace("%s", formatCurrency(minimumBid));
            updateErrorLabel();
            return;
        }

        if (bidAmount > playerBalance) {
            errorMessage = StatCollector.translateToLocal("auction.error.notEnoughCurrency");
            updateErrorLabel();
            return;
        }

        // Send bid packet
        AuctionActionPacket.placeBid(listingId, bidAmount);
        successful = true;
        close();
    }

    private void updateErrorLabel() {
        GuiNpcLabel errorLabel = getLabel(4);
        if (errorLabel != null) {
            errorLabel.label = errorMessage != null ? EnumChatFormatting.RED + errorMessage : "";
        }
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
