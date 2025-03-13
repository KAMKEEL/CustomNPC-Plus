package noppes.npcs.client.gui.mainmenu;

import kamkeel.npcs.addon.client.DBCClient;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuStatsGetPacket;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuStatsSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.DataStats;
import noppes.npcs.client.gui.*;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcStats extends GuiNPCInterface2 implements ITextfieldListener, IGuiData {
    private final DataStats stats;

    public GuiNpcStats(EntityNPCInterface npc) {
        super(npc, 2);
        stats = npc.stats;

        PacketClient.sendClient(new MainmenuStatsGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(0, "stats.health", guiLeft + 5, y + 5));
        addTextField(new GuiNpcTextField(0, this, guiLeft + 82, y, 185, 18, String.format("%.0f", stats.maxHealth)));
        getTextField(0).doublesOnly = true;
        getTextField(0).setMinMaxDefaultDouble(0, Double.MAX_VALUE, 20);

        addLabel(new GuiNpcLabel(1, "stats.aggro", guiLeft + 275, y + 5));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 355, y, 56, 18, stats.aggroRange + ""));
        getTextField(1).integersOnly = true;
        getTextField(1).setMinMaxDefault(1, 96, 2); // Doesn't work past 32 technically (MC Limitations)

        addButton(new GuiNpcButton(0, guiLeft + 82, y += 22, 56, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(2, "stats.respawn", guiLeft + 5, y + 5));

        this.addButton(new GuiNpcButton(2, guiLeft + 82, y += 22, 56, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(5, "stats.meleeproperties", guiLeft + 5, y + 5));

        this.addButton(new GuiNpcButton(3, guiLeft + 82, y += 22, 56, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(6, "stats.rangedproperties", guiLeft + 5, y + 5));
        this.addButton(new GuiNpcButton(9, guiLeft + 217, y, 56, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(7, "stats.projectileproperties", guiLeft + 140, y + 5));
        addTextField(new GuiNpcTextField(14, this, guiLeft + 355, y, 56, 20, String.format("%.0f", stats.healthRegen)).setFloatsOnly());
        addLabel(new GuiNpcLabel(14, "stats.regenhealth", guiLeft + 275, y + 5));

        this.addButton(new GuiNpcButton(15, guiLeft + 82, y += 34, 56, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(15, "potion.resistance", guiLeft + 5, y + 5));
        this.addButton(new GuiNpcButton(21, guiLeft + 217, y, 56, 20, new String[]{"gui.no", "gui.yes"}, this.stats.resistances.disableDamage ? 1 : 0));
        addLabel(new GuiNpcLabel(21, "stats.disabledamage", guiLeft + 140, y + 5));
        addTextField(new GuiNpcTextField(16, this, guiLeft + 355, y, 56, 20, String.format("%.0f", stats.combatRegen)).setFloatsOnly());
        addLabel(new GuiNpcLabel(16, "stats.combatregen", guiLeft + 275, y + 5));

        addButton(new GuiNpcButton(4, guiLeft + 82, y += 34, 56, 20, new String[]{"gui.no", "gui.yes"}, npc.isImmuneToFire() ? 1 : 0));
        addLabel(new GuiNpcLabel(10, "stats.fireimmune", guiLeft + 5, y + 5));
        addButton(new GuiNpcButton(5, guiLeft + 217, y, 56, 20, new String[]{"stats.never", "stats.inWater", "stats.inAir"}, stats.drowningType));
        addLabel(new GuiNpcLabel(11, "stats.candrown", guiLeft + 140, y + 5));
        addButton(new GuiNpcButton(23, guiLeft + 358, y, 56, 20, new String[]{"display.all", "gui.none", "NPCs", "Players", "Both"}, stats.collidesWith));
        addLabel(new GuiNpcLabel(23, "stats.collides", guiLeft + 275, y + 5));

        addButton(new GuiNpcButton(6, guiLeft + 82, y += 22, 56, 20, new String[]{"gui.no", "gui.yes"}, stats.burnInSun ? 1 : 0));
        addLabel(new GuiNpcLabel(12, "stats.burninsun", guiLeft + 5, y + 5));
        addButton(new GuiNpcButton(7, guiLeft + 217, y, 56, 20, new String[]{"gui.no", "gui.yes"}, stats.noFallDamage ? 1 : 0));
        addLabel(new GuiNpcLabel(13, "stats.nofalldamage", guiLeft + 140, y + 5));
        addLabel(new GuiNpcLabel(22, "ai.cobwebAffected", guiLeft + 275, y + 5));
        addButton(new GuiNpcButton(22, guiLeft + 358, y, 56, 20, new String[]{"gui.no", "gui.yes"}, npc.stats.ignoreCobweb ? 0 : 1));

        addButton(new GuiNpcButtonYesNo(17, guiLeft + 82, y += 22, 56, 20, stats.potionImmune));
        addLabel(new GuiNpcLabel(17, "stats.potionImmune", guiLeft + 5, y + 5));
        addButton(new GuiNpcButtonYesNo(18, guiLeft + 217, y, 56, 20, stats.attackInvisible));
        addLabel(new GuiNpcLabel(18, "stats.attackInvisible", guiLeft + 140, y + 5));
        addLabel(new GuiNpcLabel(34, "stats.creaturetype", guiLeft + 275, y + 5));
        addButton(new GuiNpcButton(8, guiLeft + 358, y, 56, 20, new String[]{"stats.normal", "stats.undead", "stats.arthropod"}, stats.creatureType.ordinal()));

        DBCClient.Instance.showDBCStatButtons(this, npc);
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 0) {
            stats.maxHealth = Math.floor(Double.parseDouble(textfield.getText()));
            npc.heal((float) (stats.maxHealth));
        } else if (textfield.id == 1) {
            stats.aggroRange = textfield.getInteger();
        } else if (textfield.id == 14) {
            stats.healthRegen = (float) (Math.floor(Float.parseFloat(textfield.getText())));
        } else if (textfield.id == 16) {
            stats.combatRegen = (float) (Math.floor(Float.parseFloat(textfield.getText())));
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 0) {
            setSubGui(new SubGuiNpcRespawn(this.stats));
        } else if (button.id == 2) {
            setSubGui(new SubGuiNpcMeleeProperties(this.stats));
        } else if (button.id == 3) {
            setSubGui(new SubGuiNpcRangeProperties(this.stats));
        } else if (button.id == 4) {
            npc.setImmuneToFire(button.getValue() == 1);
        } else if (button.id == 5) {
            npc.stats.drowningType = button.getValue();
        } else if (button.id == 6) {
            stats.burnInSun = button.getValue() == 1;
        } else if (button.id == 7) {
            stats.noFallDamage = button.getValue() == 1;
        } else if (button.id == 8) {
            stats.creatureType = EnumCreatureAttribute.values()[button.getValue()];
        } else if (button.id == 9) {
            setSubGui(new SubGuiNpcProjectiles(this.stats));
        } else if (button.id == 15) {
            setSubGui(new SubGuiNpcResistanceProperties(this.stats.resistances));
        } else if (button.id == 17) {
            stats.potionImmune = ((GuiNpcButtonYesNo) guibutton).getBoolean();
        } else if (button.id == 18) {
            stats.potionImmune = ((GuiNpcButtonYesNo) guibutton).getBoolean();
        } else if (button.id == 21) {
            // Magic Button
            stats.resistances.disableDamage = ((GuiNpcButton) guibutton).getValue() == 1;
        } else if (button.id == 22) {
            stats.ignoreCobweb = (button.getValue() == 0);
        } else if (button.id == 23) {
            stats.collidesWith = button.getValue();
        }

        // Button 300
        if (guibutton instanceof GuiNpcButton)
            DBCClient.Instance.showDBCStatActionPerformed(this, (GuiNpcButton) guibutton);
    }

    @Override
    public void save() {
        PacketClient.sendClient(new MainmenuStatsSavePacket(stats.writeToNBT(new NBTTagCompound())));
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        stats.readToNBT(compound);
        initGui();
    }
}
