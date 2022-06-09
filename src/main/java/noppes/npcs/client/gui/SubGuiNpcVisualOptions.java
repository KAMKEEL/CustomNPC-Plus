package noppes.npcs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNPCManageDialogs;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogImage;

import java.util.ArrayList;
import java.util.Collections;

public class SubGuiNpcVisualOptions extends SubGuiInterface implements ISubGuiListener, ITextfieldListener, ICustomScrollListener {
    private final Dialog dialog;
    public GuiScreen parent2;

    private GuiMenuTopButton[] topButtons;
    private int activeMenu = 1;

    private GuiCustomScroll imageScroll;
    private int selected = -1;
    private int lastColorClicked = -1;

    public SubGuiNpcVisualOptions(Dialog dialog, GuiScreen parent) {
        this.parent2 = parent;
        this.dialog = dialog;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    public void initGui()
    {
        super.initGui();
        int y = guiTop - 10;

        GuiMenuTopButton close = new GuiMenuTopButton(0,guiLeft + xSize - 22, guiTop - 17, "X");
        GuiMenuTopButton general =  new GuiMenuTopButton(1, guiLeft + 4, guiTop - 17, "General");
        GuiMenuTopButton spacing =  new GuiMenuTopButton(2, general.xPosition + general.getWidth(), guiTop - 17, "Spacing");
        GuiMenuTopButton images =  new GuiMenuTopButton(3, spacing.xPosition + spacing.getWidth(), guiTop - 17, "Images");
        topButtons = new GuiMenuTopButton[]{general,images,spacing,close};
        for(GuiMenuTopButton button : topButtons)
            button.active = button.id == activeMenu;

        if (activeMenu == 1) {
            addButton(new GuiNpcButtonYesNo(10, guiLeft + 120, y += 22, dialog.hideNPC));
            addLabel(new GuiNpcLabel(10, "dialog.hideNPC", guiLeft + 4, y + 5));

            addButton(new GuiNpcButtonYesNo(11, guiLeft + 120, y += 22, dialog.showWheel));
            addLabel(new GuiNpcLabel(11, "dialog.showWheel", guiLeft + 4, y + 5));

            addButton(new GuiNpcButtonYesNo(12, guiLeft + 120, y += 22, dialog.darkenScreen));
            addLabel(new GuiNpcLabel(12, "dialog.darkenScreen", guiLeft + 4, y + 5));

            addButton(new GuiNpcButton(13, guiLeft + 120, y += 22, 50, 20, new String[]{"dialog.instant", "dialog.gradual"}, dialog.renderGradual ? 1 : 0));
            addLabel(new GuiNpcLabel(13, "dialog.renderType", guiLeft + 4, y + 5));

            addButton(new GuiNpcButton(14, guiLeft + 120, y += 22, 50, 20, new String[]{"display.show", "display.hide"}, dialog.showPreviousBlocks ? 1 : 0));
            addLabel(new GuiNpcLabel(14, "dialog.previousBlocks", guiLeft + 4, y + 5));

            if (dialog.renderGradual) {
                addTextField(new GuiNpcTextField(15, this, guiLeft + 80, y += 22, 165, 20, dialog.textSound));
                addLabel(new GuiNpcLabel(15, "dialog.textSound", guiLeft + 4, y + 5));

                addTextField(new GuiNpcTextField(16, this, guiLeft + 125, y += 22, 40, 20, String.valueOf(dialog.textPitch)));
                addLabel(new GuiNpcLabel(16, "dialog.textPitch", guiLeft + 4, y + 5));
                getTextField(16).floatsOnly = true;
            }
        } else if (activeMenu == 2) {
            addTextField(new GuiNpcTextField(10, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.textWidth)));
            addTextField(new GuiNpcTextField(11, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.textHeight)));
            addLabel(new GuiNpcLabel(12, "gui.widthHeight", guiLeft + 4, y + 5));

            addTextField(new GuiNpcTextField(12, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.textOffsetX)));
            addTextField(new GuiNpcTextField(13, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.textOffsetY)));
            addLabel(new GuiNpcLabel(15, "dialog.textOffset", guiLeft + 4, y + 5));

            addTextField(new GuiNpcTextField(14, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.titleOffsetX)));
            addTextField(new GuiNpcTextField(15, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.titleOffsetY)));
            addLabel(new GuiNpcLabel(18, "dialog.titleOffset", guiLeft + 4, y + 5));

            addTextField(new GuiNpcTextField(16, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.optionOffsetX)));
            addTextField(new GuiNpcTextField(17, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.optionOffsetY)));
            addLabel(new GuiNpcLabel(21, "dialog.optionOffset", guiLeft + 4, y + 5));

            addTextField(new GuiNpcTextField(18, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.optionSpaceX)));
            addTextField(new GuiNpcTextField(19, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.optionSpaceY)));
            addLabel(new GuiNpcLabel(24, "dialog.optionSpacing", guiLeft + 4, y + 5));

            for (int i = 10; i < 20; i++) {
                GuiNpcTextField textField = this.getTextField(i);
                textField.integersOnly = true;
            }
        } else if (activeMenu == 3) {
            if (imageScroll == null) {
                imageScroll = new GuiCustomScroll(this, 0, false);
                imageScroll.guiLeft = guiLeft + xSize;
                imageScroll.guiTop = guiTop + 25;
                imageScroll.setSize(50, this.ySize - 25);
            }

            updateScrollData();

            this.addButton(new GuiNpcButton(1, guiLeft + xSize + 2, guiTop + 5, 20, 20, "+"));
            if (getSelectedImage() != null) {
                DialogImage dialogImage = getSelectedImage();
                this.addButton(new GuiNpcButton(2, guiLeft + xSize + 24, guiTop + 5, 20, 20, "-"));

                addTextField(new GuiNpcTextField(10, this, guiLeft + 17, y += 25, 30, 20, String.valueOf(dialogImage.id)));
                addTextField(new GuiNpcTextField(11, this, guiLeft + 92, y, 150, 20, dialogImage.texture));
                addLabel(new GuiNpcLabel(10, "gui.id", guiLeft + 4, y + 5));
                addLabel(new GuiNpcLabel(11, "display.texture", guiLeft + 50, y + 5));
                getTextField(10).integersOnly = true;

                addTextField(new GuiNpcTextField(12, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialogImage.x)));
                addTextField(new GuiNpcTextField(13, this, guiLeft + 165, y, 40, 20, String.valueOf(dialogImage.y)));
                addLabel(new GuiNpcLabel(12, "gui.position", guiLeft + 4, y + 5));
                getTextField(12).integersOnly = true;
                getTextField(13).integersOnly = true;

                addTextField(new GuiNpcTextField(14, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialogImage.width)));
                addTextField(new GuiNpcTextField(15, this, guiLeft + 165, y, 40, 20, String.valueOf(dialogImage.height)));
                addLabel(new GuiNpcLabel(13, "gui.widthHeight", guiLeft + 4, y + 5));
                getTextField(14).integersOnly = true;
                getTextField(15).integersOnly = true;

                addTextField(new GuiNpcTextField(16, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialogImage.textureX)));
                addTextField(new GuiNpcTextField(17, this, guiLeft + 165, y, 40, 20, String.valueOf(dialogImage.textureY)));
                addLabel(new GuiNpcLabel(14, "dialog.textureOffset", guiLeft + 4, y + 5));
                getTextField(16).integersOnly = true;
                getTextField(17).integersOnly = true;

                String color = Integer.toHexString(dialogImage.color);
                while(color.length() < 6)
                    color = 0 + color;
                addButton(new GuiNpcButton(18, guiLeft + 35, y += 25, 60, 20, color));
                addLabel(new GuiNpcLabel(15, "gui.color", guiLeft + 4, y + 5));
                getButton(18).setTextColor(dialogImage.color);

                String selectedColor = Integer.toHexString(dialogImage.selectedColor);
                while(selectedColor.length() < 6)
                    selectedColor = 0 + selectedColor;
                addButton(new GuiNpcButton(19, guiLeft + 180, y, 60, 20, selectedColor));
                addLabel(new GuiNpcLabel(16, "dialog.selectedColor", guiLeft + 100, y + 5));
                getButton(19).setTextColor(dialogImage.selectedColor);

                addTextField(new GuiNpcTextField(20, this, guiLeft + 35, y += 25, 60, 20, String.valueOf(dialogImage.scale)));
                addTextField(new GuiNpcTextField(21, this, guiLeft + 180, y, 60, 20, String.valueOf(dialogImage.alpha)));
                addLabel(new GuiNpcLabel(17, "model.scale", guiLeft + 4, y + 5));
                addLabel(new GuiNpcLabel(18, "display.alpha", guiLeft + 100, y + 5));
                getTextField(20).floatsOnly = true;
                getTextField(21).floatsOnly = true;

                addTextField(new GuiNpcTextField(22, this, guiLeft + 47, y += 25, 60, 20, String.valueOf(dialogImage.rotation)));
                addLabel(new GuiNpcLabel(19, "movement.rotation", guiLeft + 4, y + 5));
                getTextField(22).floatsOnly = true;

                addButton(new GuiNpcButton(23, guiLeft + 180, y, 60, 20, new String[]{"gui.default","gui.text","gui.option"}, dialogImage.imageType));
                addLabel(new GuiNpcLabel(20, "dialog.imageType", guiLeft + 115, y + 5));

                if (dialogImage.imageType == 0) {
                    addButton(new GuiNpcButton(24, guiLeft + 180, y += 25, 60, 20,
                            new String[]{"display.topLeft", "display.topCenter", "display.topRight",
                                         "display.left", "display.center", "display.right",
                                         "display.botLeft", "display.botCenter", "display.botRight"},
                            dialogImage.alignment));
                    addLabel(new GuiNpcLabel(21, "display.alignment", guiLeft + 115, y + 5));
                }
            }
        }
    }

    public void updateScrollData() {
        ArrayList<Integer> ids = new ArrayList<>(dialog.dialogImages.keySet());
        Collections.sort(ids);
        ArrayList<String> strings = new ArrayList<>();
        for (int i : ids) {
            strings.add(String.valueOf(i));
        }
        imageScroll.setList(strings);
        addScroll(imageScroll);
    }

    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i,j,k);
        if (k == 0) {
            Minecraft mc = Minecraft.getMinecraft();
            for (GuiMenuTopButton button : topButtons) {
                if (button.mousePressed(mc, i, j)) {
                    topButtonPressed(button);
                }
            }
        }
    }

    private void topButtonPressed(GuiMenuTopButton button) {
        Minecraft mc = Minecraft.getMinecraft();
        NoppesUtil.clickSound();

        int id = button.id;
        if(id == 0){
            close();
            if(parent2 != null)
                NoppesUtil.openGUI(player, parent2);
            return;
        }
        save();

        activeMenu = id;
        initGui();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        GuiNpcButton button = (GuiNpcButton) guibutton;

        if (activeMenu == 1) {
            if (button.id == 10) {
                dialog.hideNPC = button.getValue() == 1;
            }
            if (button.id == 11) {
                dialog.showWheel = button.getValue() == 1;
            }
            if (button.id == 12) {
                dialog.darkenScreen = button.getValue() == 1;
            }
            if (button.id == 13) {
                dialog.renderGradual = button.getValue() == 1;
            }
            if (button.id == 14) {
                dialog.showPreviousBlocks = button.getValue() == 1;
            }
        }

        if (activeMenu == 3) {
            if (button.id == 1) {
                int addId = 0;
                if (selected != -1 && imageScroll.getSelected() != null) {
                    DialogImage selectedImage = dialog.dialogImages.get(Integer.valueOf(imageScroll.getSelected()));
                    ArrayList<Integer> keys = new ArrayList<>(dialog.dialogImages.keySet());
                    int keyIndex = keys.indexOf(selectedImage.id);
                    do {
                        addId = keys.get(keyIndex) + 1;
                        keyIndex++;
                    } while (dialog.dialogImages.containsKey(addId));
                } else if (dialog.dialogImages.size() > 0) {
                    addId = (Integer) dialog.dialogImages.keySet().toArray()[dialog.dialogImages.size() - 1] + 1;
                }

                dialog.dialogImages.put(addId, new DialogImage(addId));
                updateScrollData();
                selected = imageScroll.getList().indexOf(String.valueOf(addId));
            }
            if (button.id == 2) {
                if (imageScroll.getSelected() != null) {
                    dialog.dialogImages.remove(Integer.valueOf(imageScroll.getSelected()));
                    selected--;
                }
            }
            if (button.id == 18) {
                if (getSelectedImage() != null) {
                    DialogImage dialogImage = getSelectedImage();
                    setSubGui(new SubGuiColorSelector(dialogImage.color));
                    lastColorClicked = 0;
                }
            }
            if (button.id == 19) {
                if (getSelectedImage() != null) {
                    DialogImage dialogImage = getSelectedImage();
                    setSubGui(new SubGuiColorSelector(dialogImage.selectedColor));
                    lastColorClicked = 1;
                }
            }
            if (button.id == 23) {
                if (getSelectedImage() != null) {
                    DialogImage dialogImage = getSelectedImage();
                    dialogImage.imageType = button.getValue();
                }
            }
            if (button.id == 24) {
                if (getSelectedImage() != null) {
                    DialogImage dialogImage = getSelectedImage();
                    dialogImage.alignment = button.getValue();
                }
            }
        }

        initGui();
    }

    @Override
    public void drawScreen(int i, int j, float f){
        super.drawScreen(i,j,f);
        for(GuiMenuTopButton button: topButtons)
            button.drawButton(mc, i, j);
    }


    @Override
    public void subGuiClosed(SubGuiInterface subgui){
        if (getSelectedImage() != null) {
            DialogImage dialogImage = getSelectedImage();
            if (lastColorClicked == 0) {
                dialogImage.color = ((SubGuiColorSelector) subgui).color;
            } else if (lastColorClicked == 1) {
                dialogImage.selectedColor = ((SubGuiColorSelector) subgui).color;
            }
            initGui();
        }
        save();
    }

    public void save() {
        GuiNpcTextField.unfocus();
        if(dialog.id >= 0)
            Client.sendData(EnumPacketServer.DialogSave, ((GuiNPCManageDialogs)parent2).category.id, dialog.writeToNBT(new NBTTagCompound()));
    }

    private DialogImage getSelectedImage() {
        if (selected > -1) {
            return dialog.dialogImages.get((dialog.dialogImages.keySet().toArray(new Integer[0]))[selected]);
        } else {
            return null;
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (activeMenu == 1) {
            if (textfield.id == 15) {
                dialog.textSound = textfield.getText();
            }
            if (textfield.id == 16) {
                dialog.textPitch = textfield.getFloat();
            }
        }

        if (activeMenu == 2) {
            int i = textfield.getInteger();

            if (textfield.id == 10) {
                dialog.textWidth = i;
            }
            if (textfield.id == 11) {
                dialog.textHeight = i;
            }

            if (textfield.id == 12) {
                dialog.textOffsetX = i;
            }
            if (textfield.id == 13) {
                dialog.textOffsetY = i;
            }

            if (textfield.id == 14) {
                dialog.titleOffsetX = i;
            }
            if (textfield.id == 15) {
                dialog.titleOffsetY = i;
            }

            if (textfield.id == 14) {
                dialog.optionOffsetX = i;
            }
            if (textfield.id == 15) {
                dialog.optionOffsetY = i;
            }

            if (textfield.id == 14) {
                dialog.optionSpaceX = i;
            }
            if (textfield.id == 15) {
                dialog.optionSpaceY = i;
            }
        }

        if (activeMenu == 3) {
            DialogImage dialogImage = getSelectedImage();
            if (dialogImage == null) {
                return;
            }

            if (textfield.id == 10) {
                dialogImage.id = textfield.getInteger();
            }
            if (textfield.id == 11) {
                dialogImage.texture = textfield.getText();
            }
            if (textfield.id == 12) {
                dialogImage.x = textfield.getInteger();
            }
            if (textfield.id == 13) {
                dialogImage.y = textfield.getInteger();
            }
            if (textfield.id == 14) {
                dialogImage.width = textfield.getInteger();
            }
            if (textfield.id == 15) {
                dialogImage.height = textfield.getInteger();
            }
            if (textfield.id == 16) {
                dialogImage.textureX = textfield.getInteger();
            }
            if (textfield.id == 17) {
                dialogImage.textureY = textfield.getInteger();
            }
            if (textfield.id == 20) {
                dialogImage.scale = textfield.getFloat();
            }
            if (textfield.id == 21) {
                dialogImage.alpha = textfield.getFloat();
            }
            if (textfield.id == 22) {
                dialogImage.rotation = textfield.getFloat();
            }
        }

        save();
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if(guiCustomScroll.id == 0 && imageScroll != null) {
            selected = guiCustomScroll.selected;
            initGui();
        }
    }
}