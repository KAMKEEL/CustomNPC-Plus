package noppes.npcs.client.gui.roles;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.role.RoleSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleAuctioneer;

public class GuiNpcAuctioneer extends GuiNPCInterface2 implements ITextfieldListener {

    private final RoleAuctioneer role;

    public GuiNpcAuctioneer(EntityNPCInterface npc) {
        super(npc);
        this.role = npc.roleInterface instanceof RoleAuctioneer ? (RoleAuctioneer) npc.roleInterface : null;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (role == null) {
            close();
            return;
        }

        int y = guiTop + 10;

        // Left column - Tab visibility
        addLabel(new GuiNpcLabel(0, "role.auctioneer.tabs", guiLeft + 10, y, 0x404040));
        y += 18;

        addLabel(new GuiNpcLabel(1, "role.auctioneer.showBrowse", guiLeft + 10, y + 5, 0x404040));
        addButton(new GuiNpcButton(1, guiLeft + 140, y, 50, 20, new String[]{"gui.no", "gui.yes"}, role.showActiveListings ? 1 : 0));
        y += 22;

        addLabel(new GuiNpcLabel(2, "role.auctioneer.showMyListings", guiLeft + 10, y + 5, 0x404040));
        addButton(new GuiNpcButton(2, guiLeft + 140, y, 50, 20, new String[]{"gui.no", "gui.yes"}, role.showMyListings ? 1 : 0));
        y += 22;

        addLabel(new GuiNpcLabel(3, "role.auctioneer.showMyBids", guiLeft + 10, y + 5, 0x404040));
        addButton(new GuiNpcButton(3, guiLeft + 140, y, 50, 20, new String[]{"gui.no", "gui.yes"}, role.showMyBids ? 1 : 0));
        y += 22;

        addLabel(new GuiNpcLabel(4, "role.auctioneer.showClaims", guiLeft + 10, y + 5, 0x404040));
        addButton(new GuiNpcButton(4, guiLeft + 140, y, 50, 20, new String[]{"gui.no", "gui.yes"}, role.showClaims ? 1 : 0));
        y += 30;

        // Actions
        addLabel(new GuiNpcLabel(10, "role.auctioneer.actions", guiLeft + 10, y, 0x404040));
        y += 18;

        addLabel(new GuiNpcLabel(11, "role.auctioneer.allowCreate", guiLeft + 10, y + 5, 0x404040));
        addButton(new GuiNpcButton(11, guiLeft + 140, y, 50, 20, new String[]{"gui.no", "gui.yes"}, role.allowCreatingListings ? 1 : 0));
        y += 22;

        addLabel(new GuiNpcLabel(12, "role.auctioneer.allowBid", guiLeft + 10, y + 5, 0x404040));
        addButton(new GuiNpcButton(12, guiLeft + 140, y, 50, 20, new String[]{"gui.no", "gui.yes"}, role.allowBidding ? 1 : 0));
        y += 22;

        addLabel(new GuiNpcLabel(13, "role.auctioneer.allowBuyout", guiLeft + 10, y + 5, 0x404040));
        addButton(new GuiNpcButton(13, guiLeft + 140, y, 50, 20, new String[]{"gui.no", "gui.yes"}, role.allowBuyout ? 1 : 0));

        // Right column - Messages
        int rightX = guiLeft + 210;
        int rightY = guiTop + 10;

        addLabel(new GuiNpcLabel(20, "role.auctioneer.messages", rightX, rightY, 0x404040));
        rightY += 18;

        addLabel(new GuiNpcLabel(21, "role.auctioneer.welcome", rightX, rightY + 5, 0x404040));
        rightY += 18;
        GuiNpcTextField welcomeField = new GuiNpcTextField(21, this, rightX, rightY, 180, 20, role.welcomeMessage);
        welcomeField.setMaxStringLength(200);
        addTextField(welcomeField);
        rightY += 28;

        addLabel(new GuiNpcLabel(22, "role.auctioneer.noAuction", rightX, rightY + 5, 0x404040));
        rightY += 18;
        GuiNpcTextField noAuctionField = new GuiNpcTextField(22, this, rightX, rightY, 180, 20, role.noAuctionMessage);
        noAuctionField.setMaxStringLength(200);
        addTextField(noAuctionField);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (role == null) return;

        if (guibutton instanceof GuiNpcButton) {
            GuiNpcButton button = (GuiNpcButton) guibutton;
            boolean value = button.getValue() == 1;

            switch (button.id) {
                case 1:
                    role.showActiveListings = value;
                    break;
                case 2:
                    role.showMyListings = value;
                    break;
                case 3:
                    role.showMyBids = value;
                    break;
                case 4:
                    role.showClaims = value;
                    break;
                case 11:
                    role.allowCreatingListings = value;
                    break;
                case 12:
                    role.allowBidding = value;
                    break;
                case 13:
                    role.allowBuyout = value;
                    break;
            }
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (role == null) return;

        switch (textfield.id) {
            case 21:
                role.welcomeMessage = textfield.getText();
                break;
            case 22:
                role.noAuctionMessage = textfield.getText();
                break;
        }
    }

    @Override
    public void save() {
        if (role != null) {
            PacketClient.sendClient(new RoleSavePacket(role.writeToNBT(new NBTTagCompound())));
        }
    }
}
