package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

/**
 * Small SubGui for entering auction search text.
 */
public class SubGuiAuctionSearch extends SubGuiInterface implements ITextfieldListener {
    private static final int TXT_SEARCH = 0;
    private static final int BTN_SUBMIT = 1;
    private static final int BTN_CLEAR = 2;
    private static final int BTN_CLOSE = 3;

    private String searchText;

    public SubGuiAuctionSearch(String currentSearchText) {
        this.searchText = currentSearchText != null ? currentSearchText : "";
        setBackground("menubg.png");
        xSize = 180;
        ySize = 80;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Close button
        addButton(new GuiNpcButton(BTN_CLOSE, guiLeft + xSize - 22, guiTop + 4, 18, 18, "X"));

        // Search label
        addLabel(new GuiNpcLabel(0, "auction.filter.search", guiLeft + 10, guiTop + 10, 0xFFFFFF));

        // Search text field
        addTextField(new GuiNpcTextField(TXT_SEARCH, this, fontRendererObj, guiLeft + 10, guiTop + 25, 160, 18, searchText));

        // Submit and Clear buttons
        addButton(new GuiNpcButton(BTN_SUBMIT, guiLeft + 10, guiTop + 50, 70, 20, "gui.apply"));
        addButton(new GuiNpcButton(BTN_CLEAR, guiLeft + 85, guiTop + 50, 70, 20, "gui.clear"));
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == TXT_SEARCH) {
            searchText = textfield.getText();
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == BTN_SUBMIT) {
            GuiNpcTextField field = getTextField(TXT_SEARCH);
            if (field != null) {
                searchText = field.getText();
            }
            // Notify parent and close
            if (parent instanceof GuiAuctionListing) {
                ((GuiAuctionListing) parent).onSearchSubmit(searchText);
            }
            close();
        } else if (guibutton.id == BTN_CLEAR) {
            searchText = "";
            GuiNpcTextField field = getTextField(TXT_SEARCH);
            if (field != null) {
                field.setText("");
            }
            // Notify parent and close
            if (parent instanceof GuiAuctionListing) {
                ((GuiAuctionListing) parent).onSearchSubmit("");
            }
            close();
        } else if (guibutton.id == BTN_CLOSE) {
            close();
        }
    }

    public String getSearchText() {
        return searchText;
    }
}
