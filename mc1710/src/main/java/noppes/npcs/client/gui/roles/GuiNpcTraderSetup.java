package noppes.npcs.client.gui.roles;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.TraderMarketSavePacket;
import kamkeel.npcs.network.packets.request.role.RoleSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import org.lwjgl.opengl.GL11;

public class GuiNpcTraderSetup extends GuiContainerNPCInterface2 implements ITextfieldListener {

    private final ResourceLocation slot = new ResourceLocation("customnpcs", "textures/gui/slot.png");
    private RoleTrader role;

    // Currency cost textfield IDs start at 100 (100-117 for slots 0-17)
    private static final int CURRENCY_FIELD_ID_START = 100;

    public GuiNpcTraderSetup(EntityNPCInterface npc, ContainerNPCTraderSetup container) {
        super(npc, container);
        ySize = 220;
        menuYOffset = 10;
        role = container.role;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        setBackground("tradersetup.png");

        // Linked Market name
        addLabel(new GuiNpcLabel(0, "role.marketname", guiLeft + 214, guiTop + 150));
        addTextField(new GuiNpcTextField(0, this, guiLeft + 214, guiTop + 160, 180, 20, role.marketName));

        // Settings button (opens SubGuiNpcTraderSettings)
        addButton(new GuiNpcButton(1, guiLeft + 214, guiTop + 184, 88, 20, "gui.settings"));
        getButton(1).setHoverText("gui.settings.hover");

        // Stock Options button (opens SubGuiNpcTraderStock)
        addButton(new GuiNpcButton(2, guiLeft + 306, guiTop + 184, 88, 20, "stock.options"));
        getButton(2).setHoverText("stock.options.hover");

        // Currency cost textfields for each slot (FIRST - before item slots)
        // Column width: 130px for better spacing
        for (int i = 0; i < 18; i++) {
            int x = guiLeft + i % 3 * 130 + 15;  // First position after "$" label
            int y = guiTop + i / 3 * 22 + 14;
            long cost = role.getCurrencyCost(i);
            GuiNpcTextField field = new GuiNpcTextField(CURRENCY_FIELD_ID_START + i, this, x, y, 36, 16, cost > 0 ? "" + cost : "");
            field.setMaxStringLength(8);
            field.setIntegersOnly();
            addTextField(field);
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        guiTop += 10;
        super.drawScreen(i, j, f);
        guiTop -= 10;
    }

    @Override
    public void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 1) {
            // Open Settings SubGui
            setSubGui(new SubGuiNpcTraderSettings(role));
        }
        if (guibutton.id == 2) {
            // Open Stock Options SubGui
            setSubGui(new SubGuiNpcTraderStock(role));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        super.drawGuiContainerBackgroundLayer(f, i, j);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        // Column width: 130px for better spacing
        for (int slot = 0; slot < 18; slot++) {
            int x = guiLeft + slot % 3 * 130 + 7;
            int y = guiTop + slot / 3 * 22 + 4;

            // "$" label for currency cost
            fontRendererObj.drawString("$", x, y + 5, CustomNpcResourceListener.DefaultTextColor);

            // Currency textfield is at x + 8 (drawn by textfield system)

            // Item Cost 1 slot
            mc.renderEngine.bindTexture(this.slot);
            GL11.glColor4f(1, 1, 1, 1);
            drawTexturedModalRect(x + 50, y, 0, 0, 18, 18);

            // Item Cost 2 slot
            drawTexturedModalRect(x + 68, y, 0, 0, 18, 18);

            // "=" sign
            fontRendererObj.drawString("=", x + 88, y + 5, CustomNpcResourceListener.DefaultTextColor);

            // Output slot
            mc.renderEngine.bindTexture(this.slot);
            GL11.glColor4f(1, 1, 1, 1);
            drawTexturedModalRect(x + 104, y, 0, 0, 18, 18);
        }
    }

    @Override
    public void save() {
        PacketClient.sendClient(new TraderMarketSavePacket(role.marketName, false));
        PacketClient.sendClient(new RoleSavePacket(role.writeToNBT(new NBTTagCompound())));
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        int id = guiNpcTextField.id;

        // Handle market name field
        if (id == 0) {
            String name = guiNpcTextField.getText();
            if (!name.equalsIgnoreCase(role.marketName)) {
                role.marketName = name;
                PacketClient.sendClient(new TraderMarketSavePacket(role.marketName, true));
            }
            return;
        }

        // Handle currency cost fields (IDs 100-117)
        if (id >= CURRENCY_FIELD_ID_START && id < CURRENCY_FIELD_ID_START + 18) {
            int slot = id - CURRENCY_FIELD_ID_START;
            long cost = guiNpcTextField.getInteger();
            role.setCurrencyCost(slot, cost);
        }
    }
}
