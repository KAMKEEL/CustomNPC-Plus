package noppes.npcs.client.gui.mainmenu;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuAIGetPacket;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuAISavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.DataAI;
import noppes.npcs.client.gui.SubGuiNpcMovement;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumCombatPolicy;
import noppes.npcs.constants.EnumNavType;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcAI extends GuiNPCInterface2 implements ITextfieldListener, IGuiData {
    private final DataAI ai;

    public GuiNpcAI(EntityNPCInterface npc) {
        super(npc, 6);
        ai = npc.ais;
        PacketClient.sendClient(new MainmenuAIGetPacket());
    }

    public void initGui() {
        super.initGui();
        addLabel(new GuiNpcLabel(0, "ai.enemyresponse", guiLeft + 5, guiTop + 17));
        addButton(new GuiNpcButton(0, guiLeft + 86, guiTop + 10, 60, 20, new String[]{"gui.retaliate", "gui.panic", "gui.retreat", "gui.nothing"}, npc.ais.onAttack));
        addLabel(new GuiNpcLabel(1, "ai.door", guiLeft + 5, guiTop + 40));
        addButton(new GuiNpcButton(1, guiLeft + 86, guiTop + 35, 60, 20, new String[]{"gui.break", "gui.open", "gui.disabled"}, npc.ais.doorInteract));
        addLabel(new GuiNpcLabel(12, "ai.swim", guiLeft + 5, guiTop + 65));
        addButton(new GuiNpcButton(7, guiLeft + 86, guiTop + 60, 60, 20, new String[]{"gui.no", "gui.yes"}, npc.ais.canSwim ? 1 : 0));
        addLabel(new GuiNpcLabel(13, "ai.shelter", guiLeft + 5, guiTop + 90));
        addButton(new GuiNpcButton(9, guiLeft + 86, guiTop + 85, 60, 20, new String[]{"gui.darkness", "gui.sunlight", "gui.disabled"}, npc.ais.findShelter));
        addLabel(new GuiNpcLabel(14, "ai.clearlos", guiLeft + 5, guiTop + 115));
        addButton(new GuiNpcButton(10, guiLeft + 86, guiTop + 110, 60, 20, new String[]{"gui.no", "gui.yes"}, npc.ais.directLOS ? 1 : 0));
        addLabel(new GuiNpcLabel(18, "ai.sprint", guiLeft + 5, guiTop + 140));
        addButton(new GuiNpcButton(16, guiLeft + 86, guiTop + 135, 60, 20, new String[]{"gui.no", "gui.yes"}, npc.ais.canSprint ? 1 : 0));

        addLabel(new GuiNpcLabel(10, "ai.avoidwater", guiLeft + 150, guiTop + 17));
        addButton(new GuiNpcButton(5, guiLeft + 230, guiTop + 10, 60, 20, new String[]{"gui.no", "gui.yes"}, npc.ais.avoidsWater ? 1 : 0));
        addLabel(new GuiNpcLabel(11, "ai.return", guiLeft + 150, guiTop + 40));
        addButton(new GuiNpcButton(6, guiLeft + 230, guiTop + 35, 60, 20, new String[]{"gui.no", "gui.yes"}, npc.ais.returnToStart ? 1 : 0));
        addLabel(new GuiNpcLabel(17, "ai.leapattarget", guiLeft + 150, guiTop + 65));
        addButton(new GuiNpcButton(15, guiLeft + 230, guiTop + 60, 60, 20, new String[]{"gui.no", "ai.jump", "ai.pounce"}, npc.ais.leapType));
        addLabel(new GuiNpcLabel(15, "ai.indirect", guiLeft + 150, guiTop + 90));
        addButton(new GuiNpcButton(13, guiLeft + 230, guiTop + 85, 60, 20, new String[]{"gui.no", "gui.whendistant", "gui.whenhidden"}, ai.canFireIndirect));
        addLabel(new GuiNpcLabel(16, "ai.rangemelee", guiLeft + 150, guiTop + 115));
        addButton(new GuiNpcButton(14, guiLeft + 230, guiTop + 110, 60, 20, new String[]{npc.inventory.getProjectile() == null ? "gui.no" : "gui.always", "gui.untilclose", "gui.whenavailable"}, ai.useRangeMelee));
        if (ai.useRangeMelee >= 1) {
            addLabel(new GuiNpcLabel(20, "gui.minrange", guiLeft + 300, guiTop + 115));
            addTextField(new GuiNpcTextField(6, this, fontRendererObj, guiLeft + 380, guiTop + 110, 30, 20, ai.distanceToMelee + ""));
            getTextField(6).integersOnly = true;
            getTextField(6).setMinMaxDefault(1, npc.stats.aggroRange, 5);
        }
        addLabel(new GuiNpcLabel(19, "ai.tacticalvariant", guiLeft + 150, guiTop + 140));
        addButton(new GuiNpcButton(17, guiLeft + 230, guiTop + 135, 60, 20, EnumNavType.names(), ai.tacticalVariant.ordinal()));
        if (ai.tacticalVariant != EnumNavType.Default && ai.tacticalVariant != EnumNavType.None) {
            String label = "";
            switch (ai.tacticalVariant) {
                case Surround:
                    label = "gui.orbitdistance";
                    break;
                case HitNRun:
                    label = "gui.fightifthisclose";
                    break;
                case Ambush:
                    label = "gui.ambushdistance";
                    break;
                case Stalk:
                    label = "gui.ambushdistance";
                    break;
                default:
                    label = "gui.engagedistance";
            }
            addLabel(new GuiNpcLabel(21, label, guiLeft + 300, guiTop + 140));
            addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft + 380, guiTop + 135, 30, 20, ai.tacticalRadius + ""));
            getTextField(3).integersOnly = true;
            getTextField(3).setMinMaxDefault(1, npc.stats.aggroRange, 5);
        }
        addLabel(new GuiNpcLabel(25, "ai.combatpolicy", guiLeft + 150, guiTop + 165));
        addButton(new GuiNpcButton(25, guiLeft + 230, guiTop + 160, 60, 20, EnumCombatPolicy.names(), ai.combatPolicy.ordinal()));
        if (ai.combatPolicy == EnumCombatPolicy.Stubborn) {
            String label = "";
            label = "gui.combatchance";
            addLabel(new GuiNpcLabel(21, label, guiLeft + 300, guiTop + 165));
            addTextField(new GuiNpcTextField(4, this, fontRendererObj, guiLeft + 380, guiTop + 160, 30, 20, ai.tacticalChance + ""));
            getTextField(4).integersOnly = true;
            getTextField(4).setMinMaxDefault(1, 100, 5);
        } else if (ai.combatPolicy == EnumCombatPolicy.Tactical) {
            addButton(new GuiNpcButton(40, guiLeft + 295, guiTop + 160, 60, 20, new String[]{"stats.normal", "stats.reverse"}, npc.ais.tacticalChance > 50 ? 1 : 0));
        }

        getButton(17).setEnabled(this.ai.onAttack == 0);
        getButton(15).setEnabled(this.ai.onAttack == 0);
        getButton(25).setEnabled(this.ai.onAttack == 0);
        getButton(13).setEnabled(this.npc.inventory.getProjectile() != null);
        getButton(14).setEnabled(this.npc.inventory.getProjectile() != null);
        getButton(10).setEnabled(ai.tacticalVariant != EnumNavType.Stalk && ai.tacticalVariant != EnumNavType.None);

        addLabel(new GuiNpcLabel(2, "ai.movement", guiLeft + 4, guiTop + 165));
        addButton(new GuiNpcButton(2, guiLeft + 86, guiTop + 160, 60, 20, "selectServer.edit"));
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 3) {
            ai.tacticalRadius = textfield.getInteger();
        }
        if (textfield.id == 4) {
            ai.tacticalChance = textfield.getInteger();
        }
        if (textfield.id == 6) {
            ai.distanceToMelee = textfield.getInteger();
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 0) {
            ai.onAttack = button.getValue();
            initGui();
        } else if (button.id == 1) {
            ai.doorInteract = button.getValue();
        } else if (button.id == 2) {
            setSubGui(new SubGuiNpcMovement(ai));
        } else if (button.id == 5) {
            npc.setAvoidWater(button.getValue() == 1);
        } else if (button.id == 6) {
            ai.returnToStart = (button.getValue() == 1);
        } else if (button.id == 7) {
            ai.canSwim = (button.getValue() == 1);
        } else if (button.id == 9) {
            ai.findShelter = button.getValue();
        } else if (button.id == 10) {
            ai.directLOS = (button.getValue() == 1);
        } else if (button.id == 13) {
            ai.canFireIndirect = button.getValue();
        } else if (button.id == 14) {
            ai.useRangeMelee = button.getValue();
            initGui();
        } else if (button.id == 15) {
            ai.leapType = (byte) button.getValue();
        } else if (button.id == 16) {
            ai.canSprint = (button.getValue() == 1);
        } else if (button.id == 17) {
            ai.tacticalVariant = EnumNavType.values()[button.getValue()];
            ai.directLOS = EnumNavType.values()[button.getValue()] != EnumNavType.Stalk && this.ai.directLOS;
            initGui();
        } else if (button.id == 25) {
            ai.combatPolicy = EnumCombatPolicy.values()[button.getValue()];
            initGui();
        } else if (button.id == 40) {
            int val = button.getValue();
            if (val == 0) {
                ai.tacticalChance = 0;
            } else {
                ai.tacticalChance = 100;
            }
            initGui();
        }
    }

    @Override
    public void save() {
        PacketClient.sendClient(new MainmenuAISavePacket(ai.writeToNBT(new NBTTagCompound())));
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        ai.readToNBT(compound);
        initGui();
    }

}
