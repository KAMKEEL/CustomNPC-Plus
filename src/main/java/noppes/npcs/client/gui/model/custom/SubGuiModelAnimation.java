package noppes.npcs.client.gui.model.custom;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AnimationFileUtil;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class SubGuiModelAnimation extends SubGuiInterface implements ITextfieldListener {
    public SubGuiModelAnimation(EntityNPCInterface npc){
        this.npc = npc;
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = guiTop + 44;
        addSelectionBlock(1,y,"Animation File:",npc.display.customModelData.getAnimFile());
        addSelectionBlock(2,y+=23,"Idle:",npc.display.customModelData.getIdleAnim());
        addSelectionBlock(3,y+=23,"Walk:",npc.display.customModelData.getWalkAnim());
        addSelectionBlock(4,y+=23,"Attack:",npc.display.customModelData.getAttackAnim());
        addSelectionBlock(5,y+23,"Hurt:",npc.display.customModelData.getHurtAnim());
        addButton(new GuiNpcButton(670, width - 22, 2, 20, 20, "X"));
    }

    public void addSelectionBlock(int id, int y, String label, String value){
        addLabel(new GuiNpcLabel(id,label, guiLeft - 85, y + 5));
        addTextField(new GuiNpcTextField(id,this, fontRendererObj, guiLeft - 10, y, 200, 20, value));
        this.addButton(new GuiNpcButton(id, guiLeft + 193, y, 80, 20, "mco.template.button.select"));
    }



    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        if(button.id == 670){
            close();
        }
        if(button.id==1){
            setSubGui(new GuiStringSelection(this,"Selecting geckolib animation file:",
                    AnimationFileUtil.getAnimationFileList(), (name)-> npc.display.customModelData.setAnimFile(name)));
        }
        if(button.id==2){
            setSubGui(new GuiStringSelection(this,"Selecting geckolib idle animation:",
                    AnimationFileUtil.getAnimationList(npc.display.customModelData.getAnimFile()),
                    (name)-> npc.display.customModelData.setIdleAnim(name)));
        }
        if(button.id==3){
            setSubGui(new GuiStringSelection(this,"Selecting geckolib walk animation:",
                    AnimationFileUtil.getAnimationList(npc.display.customModelData.getAnimFile()),
                    (name)-> npc.display.customModelData.setWalkAnim(name)));
        }
        if(button.id==4){
            setSubGui(new GuiStringSelection(this,"Selecting geckolib attack animation:",
                    AnimationFileUtil.getAnimationList(npc.display.customModelData.getAnimFile()),
                    (name)-> npc.display.customModelData.setAnimFile(name)));
        }
        if(button.id==5){
            setSubGui(new GuiStringSelection(this,"Selecting geckolib hurt animation:",
                    AnimationFileUtil.getAnimationList(npc.display.customModelData.getAnimFile()),
                    (name)-> npc.display.customModelData.setHurtAnim(name)));
        }
    }

    public boolean isValidAnimFile(String name){
        return GeckoLibCache.getInstance().getAnimations().containsKey(new ResourceLocation(name));
    }

    public boolean isValidAnimation(String name){
        return AnimationFileUtil.getAnimationList(npc.display.customModelData.getAnimFile()).contains(name);
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if(textfield.id == 1 && isValidAnimFile(textfield.getText())){
            if(!textfield.isEmpty())
                npc.display.customModelData.setAnimFile(textfield.getText());
            else
                textfield.setText(npc.display.customModelData.getAnimFile());
        }
        if(textfield.id == 2 && isValidAnimation(textfield.getText())){
            if(!textfield.isEmpty())
                npc.display.customModelData.setIdleAnim(textfield.getText());
            else
                textfield.setText(npc.display.customModelData.getIdleAnim());
        }
        if(textfield.id == 3 && isValidAnimation(textfield.getText())){
            if(!textfield.isEmpty())
                npc.display.customModelData.setWalkAnim(textfield.getText());
            else
                textfield.setText(npc.display.customModelData.getWalkAnim());
        }
        if(textfield.id == 4 && isValidAnimation(textfield.getText())){
            if(!textfield.isEmpty())
                npc.display.customModelData.setAttackAnim(textfield.getText());
            else
                textfield.setText(npc.display.customModelData.getAttackAnim());
        }
        if(textfield.id == 5 && isValidAnimation(textfield.getText())){
            if(!textfield.isEmpty())
                npc.display.customModelData.setHurtAnim(textfield.getText());
            else
                textfield.setText(npc.display.customModelData.getHurtAnim());
        }
    }
}
