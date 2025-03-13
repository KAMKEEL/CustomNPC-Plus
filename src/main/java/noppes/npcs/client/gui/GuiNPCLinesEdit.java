package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuAdvancedGetPacket;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuAdvancedSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashMap;

public class GuiNPCLinesEdit extends GuiNPCInterface2 implements IGuiData, ISubGuiListener, ITextfieldListener {
    private final Lines lines;
    private int selectedId = -1;
    private GuiSoundSelection gui;

    public GuiNPCLinesEdit(EntityNPCInterface npc, Lines lines) {
        super(npc);
        this.lines = lines;

        PacketClient.sendClient(new MainmenuAdvancedGetPacket());
    }

    public void initGui() {
        super.initGui();
        for (int i = 0; i < 8; i++) {
            String text = "";
            String sound = "";
            if (lines.lines.containsKey(i)) {
                Line line = lines.lines.get(i);
                text = line.getText();
                sound = line.getSound();
            }
            addTextField(new GuiNpcTextField(i, this, fontRendererObj, guiLeft + 4, guiTop + 4 + i * 24, 200, 20, text));
            addTextField(new GuiNpcTextField(i + 8, this, fontRendererObj, guiLeft + 208, guiTop + 4 + i * 24, 146, 20, sound));
            addButton(new GuiNpcButton(i, guiLeft + 358, guiTop + 4 + i * 24, 60, 20, "gui.select"));
        }
    }

    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        selectedId = button.id + 8;
        setSubGui(new GuiSoundSelection(getTextField(selectedId).getText()));
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        npc.advanced.readToNBT(compound);
        initGui();
    }

    private void saveLines() {
        HashMap<Integer, Line> lines = new HashMap<Integer, Line>();
        for (int i = 0; i < 8; i++) {
            GuiNpcTextField tf = getTextField(i);
            GuiNpcTextField tf2 = getTextField(i + 8);
            if (!tf.isEmpty() || !tf2.isEmpty()) {
                Line line = new Line();
                line.setText(tf.getText());
                line.setSound(tf2.getText());
                lines.put(i, line);
            }

        }
        this.lines.lines = lines;
    }

    public void save() {
        saveLines();

        PacketClient.sendClient(new MainmenuAdvancedSavePacket(npc.advanced.writeToNBT(new NBTTagCompound())));
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        GuiSoundSelection gss = (GuiSoundSelection) subgui;
        if (gss.selectedResource != null) {
            getTextField(selectedId).setText(gss.selectedResource.toString());
            saveLines();
            initGui();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        saveLines();
        initGui();
    }
}
