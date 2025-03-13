package noppes.npcs.client.gui.roles;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.role.RoleCompanionUpdatePacket;
import kamkeel.npcs.network.packets.request.role.RoleSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionTalents.GuiTalent;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

import java.util.ArrayList;
import java.util.List;

public class GuiNpcCompanion extends GuiNPCInterface2 implements ITextfieldListener, ISliderListener {
    private final RoleCompanion role;
    private final List<GuiTalent> talents = new ArrayList<GuiTalent>();

    public GuiNpcCompanion(EntityNPCInterface npc) {
        super(npc);
        role = (RoleCompanion) npc.roleInterface;
    }

    @Override
    public void initGui() {
        super.initGui();
        talents.clear();
        int y = guiTop + 4;

        addButton(new GuiNpcButton(0, guiLeft + 70, y, 90, 20, new String[]{EnumCompanionStage.BABY.name, EnumCompanionStage.CHILD.name, EnumCompanionStage.TEEN.name, EnumCompanionStage.ADULT.name, EnumCompanionStage.FULLGROWN.name}, role.stage.ordinal()));
        addLabel(new GuiNpcLabel(0, "companion.stage", guiLeft + 4, y + 5));
        addButton(new GuiNpcButton(1, guiLeft + 162, y, 90, 20, "gui.update"));

        addButton(new GuiNpcButton(2, guiLeft + 70, y += 22, 90, 20, new String[]{"gui.no", "gui.yes"}, role.canAge ? 1 : 0));
        addLabel(new GuiNpcLabel(2, "companion.age", guiLeft + 4, y + 5));
        if (role.canAge) {
            addTextField(new GuiNpcTextField(2, this, guiLeft + 162, y, 140, 20, role.ticksActive + ""));
            getTextField(2).integersOnly = true;
            getTextField(2).setMinMaxDefault(0, Integer.MAX_VALUE, 0);
        }

        talents.add(new GuiTalent(role, EnumCompanionTalent.INVENTORY, guiLeft + 4, y += 26));
        addSlider(new GuiNpcSlider(this, 10, guiLeft + 30, y + 2, 100, 20, role.getExp(EnumCompanionTalent.INVENTORY) / 5000f));

        talents.add(new GuiTalent(role, EnumCompanionTalent.ARMOR, guiLeft + 4, y += 26));
        addSlider(new GuiNpcSlider(this, 11, guiLeft + 30, y + 2, 100, 20, role.getExp(EnumCompanionTalent.ARMOR) / 5000f));

        talents.add(new GuiTalent(role, EnumCompanionTalent.SWORD, guiLeft + 4, y += 26));
        addSlider(new GuiNpcSlider(this, 12, guiLeft + 30, y + 2, 100, 20, role.getExp(EnumCompanionTalent.SWORD) / 5000f));

//    	talents.add(new GuiTalent(role, EnumCompanionTalent.RANGED, guiLeft + 4, y+=26));
//    	addSlider(new GuiNpcSlider(this, 13, guiLeft + 30, y + 2, 100, 20, role.getExp(EnumCompanionTalent.RANGED)/5000f));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0) {
            GuiNpcButton button = (GuiNpcButton) guibutton;
            role.matureTo(EnumCompanionStage.values()[button.getValue()]);
            if (role.canAge)
                role.ticksActive = role.stage.matureAge;
            initGui();
        }
        if (guibutton.id == 1) {
            PacketClient.sendClient(new RoleCompanionUpdatePacket(role.stage));
        }
        if (guibutton.id == 2) {
            GuiNpcButton button = (GuiNpcButton) guibutton;
            role.canAge = button.getValue() == 1;
            initGui();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 2) {
            role.ticksActive = textfield.getInteger();
        }
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);
        for (GuiTalent talent : talents) {
            talent.drawScreen(i, j, f);
        }
    }

    @Override
    public void elementClicked() {

    }


    @Override
    public void save() {
        PacketClient.sendClient(new RoleSavePacket(role.writeToNBT(new NBTTagCompound())));
    }

    @Override
    public void mouseDragged(GuiNpcSlider slider) {
        if (slider.sliderValue <= 0) {
            slider.setString("gui.disabled");
            role.talents.remove(EnumCompanionTalent.values()[slider.id - 10]);
        } else {
            slider.displayString = (int) (slider.sliderValue * 50) * 100 + " exp";
            role.setExp(EnumCompanionTalent.values()[slider.id - 10], (int) (slider.sliderValue * 50) * 100);
        }
    }

    @Override
    public void mousePressed(GuiNpcSlider slider) {


    }

    @Override
    public void mouseReleased(GuiNpcSlider slider) {


    }


}
