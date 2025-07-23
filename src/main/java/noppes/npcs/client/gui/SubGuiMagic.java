package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.magic.MagicSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.global.GuiNpcManageMagic;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumTextureType;
import noppes.npcs.controllers.data.Magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubGuiMagic extends SubGuiInterface implements ITextfieldListener, ISubGuiListener, ICustomScrollListener {

    public GuiNpcManageMagic parent;
    private GuiCustomScroll allMagic;
    private GuiCustomScroll interactionsScroll;
    private Magic magic;
    private String search = "";

    private GuiNpcTextField interactionField;

    private String selectedInteraction;
    private HashMap<String, Integer> interactionNames = new HashMap<>();
    private HashMap<String, Float> interactionValues = new HashMap<>();

    public SubGuiMagic(GuiNpcManageMagic parent, Magic magic) {
        this.magic = magic;
        this.parent = parent;
        setBackground("menubg.png");
        xSize = 360;
        ySize = 216;

        closeOnEsc = true;

        processInteractions();
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 6;

        addLabel(new GuiNpcLabel(1, "gui.name", guiLeft + 4, y + 5));
        addTextField(new GuiNpcTextField(1, this, this.fontRendererObj, guiLeft + 80, y, 200, 20, magic.name));

        addLabel(new GuiNpcLabel(-10, "ID", guiLeft + 200 + 80 + 5, y + 2));
        addLabel(new GuiNpcLabel(-11, magic.id + "", guiLeft + 200 + 80 + 5, y + 12));

        y += 25;

        // Edit Magic Display Name
        addLabel(new GuiNpcLabel(2, "gui.displayName", guiLeft + 4, y + 5));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 80, y, 200, 20, magic.displayName));

        String color = Integer.toHexString(magic.color);
        while (color.length() < 6)
            color = 0 + color;
        addButton(new GuiNpcButton(12, guiLeft + 80 + 5 + 200, y, 65, 20, color));
        getButton(12).setTextColor(magic.color);

        y += 25;

        addLabel(new GuiNpcLabel(3, "display.texture", guiLeft + 4, y + 5));
        addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft + 80, y, 200, 20, magic.iconTexture));
        addButton(new GuiNpcButton(13, guiLeft + 80 + 5 + 200, y, 65, 20, EnumTextureType.names(), magic.type.ordinal()));


        // Interactions
        if (allMagic == null) {
            allMagic = new GuiCustomScroll(this, 0);
            allMagic.setSize(150, 107);
        }
        allMagic.guiLeft = guiLeft + 5;
        allMagic.guiTop = guiTop + 80;
        this.addScroll(allMagic);
        addTextField(new GuiNpcTextField(34, this, fontRendererObj, guiLeft + 5, guiTop + 30 + 160, 150, 20, search));
        allMagic.setList(getSearchList());

        if (interactionsScroll == null) {
            interactionsScroll = new GuiCustomScroll(this, 1);
            interactionsScroll.setSize(150, 107);
        }
        interactionsScroll.guiLeft = guiLeft + 200;
        interactionsScroll.guiTop = guiTop + 80;
        interactionsScroll.setList(new ArrayList<>(interactionNames.keySet()));
        this.addScroll(interactionsScroll);

        addLabel(new GuiNpcLabel(50, "magic.interaction", guiLeft + 170, guiTop + 30 + 160 + 5));
        interactionField = new GuiNpcTextField(90, this, fontRendererObj, guiLeft + 245, guiTop + 30 + 160, 45, 20, "");
        interactionField.setFloatsOnly();
        interactionField.setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0.0f);

        addTextField(interactionField);
        if (selectedInteraction != null) {
            interactionField.setText(interactionValues.get(selectedInteraction) + "");
        } else {
            interactionField.enabled = false;
        }

        addButton(new GuiNpcButton(60, guiLeft + 160, guiTop + 90, 30, 20, ">"));
        addButton(new GuiNpcButton(61, guiLeft + 160, guiTop + 112, 30, 20, "<"));

        addButton(new GuiNpcButton(62, guiLeft + 160, guiTop + 140, 30, 20, ">>"));
        addButton(new GuiNpcButton(63, guiLeft + 160, guiTop + 162, 30, 20, "<<"));

        // Done Button
        addButton(new GuiNpcButton(99, guiLeft + 303, guiTop + 30 + 160, 50, 20, "gui.done"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 99) {
            close();
        }
        if (button.id == 13) {
            magic.type = EnumTextureType.values()[((GuiNpcButton) button).getValue()];
        }
        if (button.id == 12) {
            setSubGui(new SubGuiColorSelector(magic.color));
        }
        if (button.id == 60 && allMagic.hasSelected() && !interactionNames.containsKey(allMagic.getSelected()) && this.parent.magicData.containsKey(allMagic.getSelected())) {
            interactionNames.put(allMagic.getSelected(), this.parent.magicData.get(allMagic.getSelected()));
            interactionValues.put(allMagic.getSelected(), 0.0f);
            interactionsScroll.list.add(allMagic.getSelected());
        }
        if (button.id == 61 && interactionsScroll.hasSelected()) {
            // Remove
            interactionNames.remove(interactionsScroll.getSelected());
            interactionValues.remove(interactionsScroll.getSelected());

            interactionsScroll.list.remove(interactionsScroll.selected);
            interactionsScroll.selected = -1;
            interactionField.enabled = false;
            interactionField.setText("");
        }
        if (button.id == 62) {
            // Add All
            for (String name : this.parent.magicData.keySet()) {
                if (!interactionNames.containsKey(name)) {
                    interactionNames.put(name, this.parent.magicData.get(name));
                    interactionValues.put(name, 0.0f);
                }
            }
            interactionsScroll.setList(new ArrayList<>(interactionNames.keySet()));
        }
        if (button.id == 63) {
            // Remove All
            interactionNames.clear();
            interactionValues.clear();

            interactionsScroll.setList(new ArrayList<>());
            interactionsScroll.selected = -1;
            interactionField.enabled = false;
            interactionField.setText("");
        }
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        if (guiNpcTextField.id == 1) {
            if (magic.id < 0)
                guiNpcTextField.setText("");
            else {
                String name = guiNpcTextField.getText();
                if (name.isEmpty() || ((GuiNpcManageMagic) this.parent).magicData.containsKey(name)) {
                    guiNpcTextField.setText(magic.name);
                } else if (magic.id >= 0) {
                    String old = magic.name;
                    ((GuiNpcManageMagic) this.parent).magicData.remove(old);
                    magic.name = name;
                    ((GuiNpcManageMagic) this.parent).magicData.put(magic.name, magic.id);
                    ((GuiNpcManageMagic) this.parent).rightScroll.replace(old, magic.name);
                }
            }
        }
        if (guiNpcTextField.id == 2) {
            magic.displayName = guiNpcTextField.getText();
        }
        if (guiNpcTextField.id == 3) {
            magic.iconTexture = guiNpcTextField.getText();
        }
        if (guiNpcTextField.id == 90 && selectedInteraction != null) {
            interactionValues.put(selectedInteraction, guiNpcTextField.getFloat());
        }
    }

    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if (k == 0 && allMagic != null)
            allMagic.mouseClicked(i, j, k);
    }

    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(34) != null && getTextField(34).isFocused()) {
            if (search.equals(getTextField(34).getText()))
                return;
            search = getTextField(34).getText().toLowerCase();
            allMagic.setList(getSearchList());
            allMagic.resetScroll();
        }
    }

    private List<String> getSearchList() {
        List<String> original = new ArrayList<>(this.parent.magicData.keySet());
        if (search.isEmpty()) {
            return original;
        }
        List<String> filtered = new ArrayList<>();
        for (String name : original) {
            if (name.toLowerCase().contains(search)) {
                filtered.add(name);
            }
        }
        return filtered;
    }

    public void processInteractions() {
        interactionNames.clear();
        interactionValues.clear();
        for (int id : magic.interactions.keySet()) {
            for (String name : this.parent.magicData.keySet()) {
                if (this.parent.magicData.get(name) == id) {
                    interactionNames.put(name, id);
                    interactionValues.put(name, magic.interactions.get(id));
                }
            }
        }
    }

    public void setInteractions() {
        magic.interactions.clear();
        for (String name : interactionNames.keySet()) {
            magic.interactions.put(interactionNames.get(name), interactionValues.get(name));
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector) {
            magic.color = ((SubGuiColorSelector) subgui).color;
            initGui();
        }
    }

    public void close() {
        setInteractions();
        NBTTagCompound compound = new NBTTagCompound();
        magic.writeNBT(compound);
        PacketClient.sendClient(new MagicSavePacket(compound));
        super.close();
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 1) {
            GuiNpcTextField.unfocus();
            selectedInteraction = interactionsScroll.getSelected();
            interactionField.enabled = true;
            interactionField.setText(interactionValues.get(selectedInteraction) + "");
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
    }
}
