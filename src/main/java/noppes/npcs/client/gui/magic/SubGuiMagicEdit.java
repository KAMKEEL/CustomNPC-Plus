package noppes.npcs.client.gui.magic;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.magic.MagicSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Magic;

public class SubGuiMagicEdit extends SubGuiInterface implements ITextfieldListener {

    private Magic magic;

    public SubGuiMagicEdit(Magic magic) {
        this.magic = magic;
        setBackground("menubg.png");
        xSize = 360;
        ySize = 240;
    }

    @Override
    public void initGui() {
        super.initGui();
        // Display the Magic's ID
        addLabel(new GuiNpcLabel(0, "ID: " + magic.id, guiLeft + 4, guiTop + 4));

        // Edit Magic Name
        addLabel(new GuiNpcLabel(1, "Name:", guiLeft + 4, guiTop + 28));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 60, guiTop + 24, 200, 20, magic.name));

        // Edit Magic Display Name
        addLabel(new GuiNpcLabel(2, "Display Name:", guiLeft + 4, guiTop + 52));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 90, guiTop + 48, 200, 20, magic.displayName));

        // Button to edit interactions
        addButton(new GuiNpcButton(10, guiLeft + 4, guiTop + 80, 120, 20, "Edit Interactions"));

        // Done button to save changes
        addButton(new GuiNpcButton(99, guiLeft + 4, guiTop + 110, 60, 20, "Done"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            setSubGui(new SubGuiMagicInteractionsEdit(magic));
        }
        if (button.id == 99) {
            // Update magic fields and send save packet
            magic.name = getTextField(1).getText();
            magic.displayName = getTextField(2).getText();
            NBTTagCompound compound = new NBTTagCompound();
            magic.writeNBT(compound);
            PacketClient.sendClient(new MagicSavePacket(compound));
            close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {}
}
