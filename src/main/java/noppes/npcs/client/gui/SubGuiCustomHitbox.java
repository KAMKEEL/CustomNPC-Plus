package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.CustomHitboxData;

public class SubGuiCustomHitbox extends SubGuiInterface implements ITextfieldListener
{
    private CustomHitboxData hitboxData;
    public SubGuiCustomHitbox(CustomHitboxData hitboxData){
        this.hitboxData = hitboxData;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    @Override
    public void initGui(){
        super.initGui();
        addLabel(new GuiNpcLabel(0,"Custom hitbox", guiLeft + 5, guiTop + 35));
        addButton(new GuiNpcButtonYesNo(0,guiLeft + 122, guiTop + 30, 56, 20 ,hitboxData.isCustomHitbox()));
        if(hitboxData.isCustomHitbox()){
            addLabel(new GuiNpcLabel(3,"Width", guiLeft + 5, guiTop + 57));
            addTextField(new GuiNpcTextField(2,this, fontRendererObj, guiLeft + 122, guiTop + 53, 50, 18, hitboxData.getWidth() + ""));
            getTextField(2).floatsOnly = true;
            getTextField(2).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 20);

            addLabel(new GuiNpcLabel(4,"Height", guiLeft + 5, guiTop + 79));
            addTextField(new GuiNpcTextField(3,this, fontRendererObj, guiLeft + 122, guiTop + 75, 50, 18, hitboxData.getHeight() + ""));
            getTextField(3).floatsOnly = true;
            getTextField(3).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 20);
        }
        addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 190,98, 20, "gui.done"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        int id = guibutton.id;
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id == 0){
            hitboxData.setCustomHitbox(((GuiNpcButtonYesNo)button).getBoolean());
            initGui();
        }
        if(id == 66){
            close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if(textfield.id == 2){
            hitboxData.setWidth(textfield.getFloat());
        }
        if(textfield.id == 3){
            hitboxData.setHeight(textfield.getFloat());
        }
    }

}

