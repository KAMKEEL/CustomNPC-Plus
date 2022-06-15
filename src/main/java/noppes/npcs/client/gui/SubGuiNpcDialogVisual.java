package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNPCManageDialogs;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogImage;

import java.util.ArrayList;
import java.util.Collections;

public class SubGuiNpcDialogVisual extends SubGuiInterface implements ISubGuiListener, ITextfieldListener, ICustomScrollListener {
    private final Dialog dialog;
    public GuiScreen parent2;

    private GuiMenuTopButton[] topButtons;
    private int activeMenu = 1;

    private GuiCustomScroll imageScroll;
    private int selected = -1;
    private int lastColorClicked = -1;
    private int changedId = -1;

    public SubGuiNpcDialogVisual(Dialog dialog, GuiScreen parent) {
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
        for(GuiMenuTopButton button : topButtons) {
            button.active = button.id == activeMenu;
            addButton(button);
        }

        if (activeMenu == 1) {
            addButton(new GuiNpcButtonYesNo(10, guiLeft + 120, y += 22, dialog.hideNPC));
            addLabel(new GuiNpcLabel(10, "dialog.hideNPC", guiLeft + 4, y + 5));

            addButton(new GuiNpcButtonYesNo(11, guiLeft + 120, y += 22, dialog.showWheel));
            addLabel(new GuiNpcLabel(11, "dialog.showWheel", guiLeft + 4, y + 5));

            addButton(new GuiNpcButtonYesNo(12, guiLeft + 120, y += 22, dialog.darkenScreen));
            addLabel(new GuiNpcLabel(12, "dialog.darkenScreen", guiLeft + 4, y + 5));

            addButton(new GuiNpcButton(13, guiLeft + 120, y += 22, 50, 20, new String[]{"dialog.instant", "dialog.gradual"}, dialog.renderGradual ? 1 : 0));
            addLabel(new GuiNpcLabel(13, "dialog.renderType", guiLeft + 4, y + 5));

            addButton(new GuiNpcButton(14, guiLeft + 120, y += 22, 50, 20, new String[]{"display.hide", "display.show"}, dialog.showPreviousBlocks ? 1 : 0));
            addLabel(new GuiNpcLabel(14, "dialog.previousBlocks", guiLeft + 4, y + 5));

            addButton(new GuiNpcButton(15, guiLeft + 120, y += 22, 50, 20, new String[]{"display.hide", "display.show"}, dialog.showOptionLine ? 1 : 0));
            addLabel(new GuiNpcLabel(15, "dialog.optionLine", guiLeft + 4, y + 5));

            String color = Integer.toHexString(dialog.color);
            while (color.length() < 6) {
                color = 0 + color;
            }
            addButton(new GuiNpcButton(16, guiLeft + 35, y += 25, 60, 20, color));
            addLabel(new GuiNpcLabel(16, "gui.color", guiLeft + 4, y + 5));
            getButton(16).setTextColor(dialog.color);

            color = Integer.toHexString(dialog.titleColor);
            while (color.length() < 6) {
                color = 0 + color;
            }
            addButton(new GuiNpcButton(17, guiLeft + 157, y, 60, 20, color));
            addLabel(new GuiNpcLabel(17, "dialog.titleColor", guiLeft + 100, y + 5));
            getButton(17).setTextColor(dialog.titleColor);

            addTextField(new GuiNpcTextField(18, this, guiLeft + 80, y += 22, 165, 20, dialog.textSound));
            addLabel(new GuiNpcLabel(18, "dialog.textSound", guiLeft + 4, y + 5));
            getTextField(18).setVisible(dialog.renderGradual);
            getLabel(18).enabled = dialog.renderGradual;

            addTextField(new GuiNpcTextField(19, this, guiLeft + 125, y += 22, 40, 20, String.valueOf(dialog.textPitch)));
            addLabel(new GuiNpcLabel(19, "dialog.textPitch", guiLeft + 4, y + 5));
            getTextField(19).floatsOnly = true;
            getTextField(19).setVisible(dialog.renderGradual);
            getLabel(19).enabled = dialog.renderGradual;
        } else if (activeMenu == 2) {
            addButton(new GuiNpcButton(10, guiLeft + 70, y += 22, 50, 20, new String[]{"gui.text", "gui.option", "display.fixed"}, dialog.titlePos));
            addLabel(new GuiNpcLabel(10, "dialog.titlePos", guiLeft + 4, y + 5));

            addTextField(new GuiNpcTextField(11, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.textWidth)));
            addTextField(new GuiNpcTextField(12, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.textHeight)));
            addLabel(new GuiNpcLabel(12, "gui.widthHeight", guiLeft + 4, y + 5));
            getTextField(11).integersOnly = true;
            getTextField(11).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            getTextField(12).integersOnly = true;
            getTextField(12).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

            addTextField(new GuiNpcTextField(13, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.textOffsetX)));
            addTextField(new GuiNpcTextField(14, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.textOffsetY)));
            addLabel(new GuiNpcLabel(15, "dialog.textOffset", guiLeft + 4, y + 5));
            getTextField(13).integersOnly = true;
            getTextField(13).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            getTextField(14).integersOnly = true;
            getTextField(14).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

            addTextField(new GuiNpcTextField(15, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.titleOffsetX)));
            addTextField(new GuiNpcTextField(16, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.titleOffsetY)));
            addLabel(new GuiNpcLabel(18, "dialog.titleOffset", guiLeft + 4, y + 5));
            getTextField(15).integersOnly = true;
            getTextField(15).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            getTextField(16).integersOnly = true;
            getTextField(16).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

            addTextField(new GuiNpcTextField(17, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.optionOffsetX)));
            addTextField(new GuiNpcTextField(18, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.optionOffsetY)));
            addLabel(new GuiNpcLabel(21, "dialog.optionOffset", guiLeft + 4, y + 5));
            getTextField(17).integersOnly = true;
            getTextField(17).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            getTextField(18).integersOnly = true;
            getTextField(18).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

            addTextField(new GuiNpcTextField(19, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.optionSpaceX)));
            addTextField(new GuiNpcTextField(20, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.optionSpaceY)));
            addLabel(new GuiNpcLabel(24, "dialog.optionSpacing", guiLeft + 4, y + 5));
            getTextField(19).integersOnly = true;
            getTextField(19).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            getTextField(20).integersOnly = true;
            getTextField(20).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

            addTextField(new GuiNpcTextField(21, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.npcOffsetX)));
            addTextField(new GuiNpcTextField(22, this, guiLeft + 165, y, 40, 20, String.valueOf(dialog.npcOffsetY)));
            addLabel(new GuiNpcLabel(25, "dialog.npcOffset", guiLeft + 4, y + 5));
            getTextField(21).integersOnly = true;
            getTextField(21).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            getTextField(21).setVisible(!dialog.hideNPC);
            getTextField(22).integersOnly = true;
            getTextField(22).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            getTextField(22).setVisible(!dialog.hideNPC);
            getLabel(25).enabled = !dialog.hideNPC;

            addTextField(new GuiNpcTextField(23, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialog.npcScale)));
            addLabel(new GuiNpcLabel(26, "dialog.npcScale", guiLeft + 4, y + 5));
            getTextField(23).floatsOnly = true;
            getTextField(23).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
            getTextField(23).setVisible(!dialog.hideNPC);
            getLabel(26).enabled = !dialog.hideNPC;
        } else if (activeMenu == 3) {
            if (imageScroll == null) {
                imageScroll = new GuiCustomScroll(this, 0, false);
            }
            imageScroll.guiLeft = guiLeft + xSize;
            imageScroll.guiTop = guiTop + 25;
            imageScroll.setSize(50, this.ySize - 25);

            updateScrollData();

            addButton(new GuiNpcButton(1, guiLeft + xSize + 2, guiTop + 5, 20, 20, "+"));
            if (getSelectedImage() != null) {
                DialogImage dialogImage = getSelectedImage();
                addButton(new GuiNpcButton(2, guiLeft + xSize + 24, guiTop + 5, 20, 20, "-"));

                addTextField(new GuiNpcTextField(10, this, guiLeft + 17, y += 25, 30, 20, String.valueOf(dialogImage.id)));
                addTextField(new GuiNpcTextField(11, this, guiLeft + 92, y, 150, 20, dialogImage.texture));
                addLabel(new GuiNpcLabel(10, "gui.id", guiLeft + 4, y + 5));
                addLabel(new GuiNpcLabel(11, "display.texture", guiLeft + 50, y + 5));
                getTextField(10).integersOnly = true;

                addTextField(new GuiNpcTextField(12, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialogImage.x)));
                addTextField(new GuiNpcTextField(13, this, guiLeft + 165, y, 40, 20, String.valueOf(dialogImage.y)));
                addLabel(new GuiNpcLabel(12, "gui.position", guiLeft + 4, y + 5));
                getTextField(12).integersOnly = true;
                getTextField(12).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
                getTextField(13).integersOnly = true;
                getTextField(13).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

                addTextField(new GuiNpcTextField(14, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialogImage.width)));
                addTextField(new GuiNpcTextField(15, this, guiLeft + 165, y, 40, 20, String.valueOf(dialogImage.height)));
                addLabel(new GuiNpcLabel(13, "gui.widthHeight", guiLeft + 4, y + 5));
                getTextField(14).integersOnly = true;
                getTextField(14).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
                getTextField(15).integersOnly = true;
                getTextField(15).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

                addTextField(new GuiNpcTextField(16, this, guiLeft + 120, y += 25, 40, 20, String.valueOf(dialogImage.textureX)));
                addTextField(new GuiNpcTextField(17, this, guiLeft + 165, y, 40, 20, String.valueOf(dialogImage.textureY)));
                addLabel(new GuiNpcLabel(14, "dialog.textureOffset", guiLeft + 4, y + 5));
                getTextField(16).integersOnly = true;
                getTextField(16).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
                getTextField(17).integersOnly = true;
                getTextField(17).setMinMaxDefault(-Integer.MAX_VALUE, Integer.MAX_VALUE, 0);

                String color = Integer.toHexString(dialogImage.color);
                while(color.length() < 6)
                    color = 0 + color;
                addButton(new GuiNpcButton(18, guiLeft + 35, y += 25, 60, 20, color));
                addLabel(new GuiNpcLabel(15, "gui.color", guiLeft + 4, y + 5));
                getButton(18).setTextColor(dialogImage.color);

                String selectedColor = Integer.toHexString(dialogImage.selectedColor);
                while (selectedColor.length() < 6)
                    selectedColor = 0 + selectedColor;
                addButton(new GuiNpcButton(19, guiLeft + 180, y, 60, 20, selectedColor));
                addLabel(new GuiNpcLabel(16, "dialog.selectedColor", guiLeft + 100, y + 5));
                getButton(19).setTextColor(dialogImage.selectedColor);
                getButton(19).setEnabled(dialogImage.imageType == 2);
                getButton(19).setVisible(dialogImage.imageType == 2);
                getLabel(16).enabled = dialogImage.imageType == 2;

                addTextField(new GuiNpcTextField(20, this, guiLeft + 33, y += 25, 30, 20, String.valueOf(dialogImage.scale)));
                addTextField(new GuiNpcTextField(21, this, guiLeft + 102, y, 30, 20, String.valueOf(dialogImage.alpha)));
                addTextField(new GuiNpcTextField(22, this, guiLeft + 183, y, 45, 20, String.valueOf(dialogImage.rotation)));
                addLabel(new GuiNpcLabel(17, "model.scale", guiLeft + 4, y + 5));
                addLabel(new GuiNpcLabel(18, "display.alpha", guiLeft + 72, y + 5));
                addLabel(new GuiNpcLabel(19, "movement.rotation", guiLeft + 140, y + 5));
                getTextField(20).floatsOnly = true;
                getTextField(21).floatsOnly = true;
                getTextField(22).floatsOnly = true;

                addButton(new GuiNpcButton(23, guiLeft + 35, y += 25, 60, 20, new String[]{"gui.screen", "gui.text", "gui.option"}, dialogImage.imageType));
                addLabel(new GuiNpcLabel(20, "gui.type", guiLeft + 4, y + 5));

                addButton(new GuiNpcButton(24, guiLeft + 160, y, 60, 20,
                        new String[]{"display.topLeft", "display.topCenter", "display.topRight",
                                     "display.left", "display.center", "display.right",
                                     "display.botLeft", "display.botCenter", "display.botRight"},
                        dialogImage.alignment));
                addLabel(new GuiNpcLabel(21, "display.alignment", guiLeft + 110, y + 5));
                getButton(24).setEnabled(dialogImage.imageType == 0);
                getButton(24).setVisible(dialogImage.imageType == 0);
                getLabel(21).enabled = dialogImage.imageType == 0;
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
        imageScroll.setUnsortedList(strings);

        if (changedId != -1) {
            selected = imageScroll.getList().indexOf(String.valueOf(changedId));
            imageScroll.setSelected(String.valueOf(changedId));
            changedId = -1;
        }

        addScroll(imageScroll);
    }

    private void topButtonPressed(GuiMenuTopButton button) {
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

        if (guibutton instanceof GuiMenuTopButton) {
            topButtonPressed((GuiMenuTopButton) guibutton);
        }

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
            if (button.id == 15) {
                dialog.showOptionLine = button.getValue() == 1;
            }
            if (button.id == 16) {
                setSubGui(new SubGuiColorSelector(dialog.color));
                lastColorClicked = 0;
            }
            if (button.id == 17) {
                setSubGui(new SubGuiColorSelector(dialog.titleColor));
                lastColorClicked = 1;
            }
        }

        if (activeMenu == 2) {
            if (button.id == 10) {
                dialog.titlePos = button.getValue();
            }
        }

        if (activeMenu == 3) {
            if (button.id == 1) {
                if (dialog.dialogImages.size() >= CustomNpcs.DialogImageLimit) {
                    return;
                }

                int addId = 0;
                if (selected != -1 && imageScroll.getSelected() != null) {
                    DialogImage selectedImage = (DialogImage) dialog.dialogImages.get(Integer.valueOf(imageScroll.getSelected()));
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
        if (activeMenu == 1) {
            if (lastColorClicked == 0) {
                dialog.color = ((SubGuiColorSelector) subgui).color;
            } else if (lastColorClicked == 1) {
                dialog.titleColor = ((SubGuiColorSelector) subgui).color;
            }
            initGui();
        }
        if (activeMenu == 3) {
            if (getSelectedImage() != null) {
                DialogImage dialogImage = getSelectedImage();
                if (lastColorClicked == 0) {
                    dialogImage.color = ((SubGuiColorSelector) subgui).color;
                } else if (lastColorClicked == 1) {
                    dialogImage.selectedColor = ((SubGuiColorSelector) subgui).color;
                }
                initGui();
            }
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
            return (DialogImage) dialog.dialogImages.get((dialog.dialogImages.keySet().toArray(new Integer[0]))[selected]);
        } else {
            return null;
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (activeMenu == 1) {
            if (textfield.id == 18) {
                dialog.textSound = textfield.getText();
            }
            if (textfield.id == 19) {
                dialog.textPitch = textfield.getFloat();
            }
        }

        if (activeMenu == 2) {
            if (textfield.id == 11) {
                dialog.textWidth = textfield.getInteger();
            }
            if (textfield.id == 12) {
                dialog.textHeight = textfield.getInteger();
            }

            if (textfield.id == 13) {
                dialog.textOffsetX = textfield.getInteger();
            }
            if (textfield.id == 14) {
                dialog.textOffsetY = textfield.getInteger();
            }

            if (textfield.id == 15) {
                dialog.titleOffsetX = textfield.getInteger();
            }
            if (textfield.id == 16) {
                dialog.titleOffsetY = textfield.getInteger();
            }

            if (textfield.id == 17) {
                dialog.optionOffsetX = textfield.getInteger();
            }
            if (textfield.id == 18) {
                dialog.optionOffsetY = textfield.getInteger();
            }

            if (textfield.id == 19) {
                dialog.optionSpaceX = textfield.getInteger();
            }
            if (textfield.id == 20) {
                dialog.optionSpaceY = textfield.getInteger();
            }

            if (textfield.id == 21) {
                dialog.npcOffsetX = textfield.getInteger();
            }
            if (textfield.id == 22) {
                dialog.npcOffsetY = textfield.getInteger();
            }
            if (textfield.id == 23) {
                dialog.npcScale = textfield.getFloat();
            }
        }

        if (activeMenu == 3) {
            DialogImage dialogImage = getSelectedImage();
            if (dialogImage == null) {
                return;
            }

            if (textfield.id == 10) {
                if (imageScroll.getList().contains(String.valueOf(textfield.getInteger()))) {
                    textfield.setText(String.valueOf(dialogImage.id));
                    return;
                }

                dialog.dialogImages.remove(dialogImage.id);
                dialogImage.id = textfield.getInteger();
                dialog.dialogImages.put(dialogImage.id, dialogImage);

                changedId = dialogImage.id;
                updateScrollData();
                initGui();
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
