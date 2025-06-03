package noppes.npcs.client.gui.mainmenu;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.jobs.JobGetPacket;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuAdvancedGetPacket;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuAdvancedSavePacket;
import kamkeel.npcs.network.packets.request.role.RoleGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.advanced.*;
import noppes.npcs.client.gui.roles.*;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcAdvanced extends GuiNPCInterface2 implements IGuiData {
    private boolean hasChanges = false;

    public GuiNpcAdvanced(EntityNPCInterface npc) {
        super(npc, 4);
        PacketClient.sendClient(new MainmenuAdvancedGetPacket());
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = guiTop + 10;
        this.addButton(new GuiNpcButton(3, guiLeft + 85 + 160, y, 52, 20, "selectServer.edit"));
        this.addButton(new GuiNpcButton(8, guiLeft + 85, y, 155, 20, new String[]{"role.none", "role.trader", "role.follower", "role.bank", "role.transporter", "role.mailman", NoppesStringUtils.translate("role.companion", "(WIP)")}, npc.advanced.role.ordinal()));
        getButton(3).setEnabled(npc.advanced.role != EnumRoleType.None && npc.advanced.role != EnumRoleType.Postman);

        this.addButton(new GuiNpcButton(4, guiLeft + 85 + 160, y += 22, 52, 20, "selectServer.edit"));
        this.addButton(new GuiNpcButton(5, guiLeft + 85, y, 155, 20, new String[]{"job.none", "job.bard", "job.healer", "job.guard", "job.itemgiver", "role.follower", "job.spawner", "job.conversation", "job.chunkloader"}, npc.advanced.job.ordinal()));

        getButton(4).setEnabled(npc.advanced.job != EnumJobType.None && npc.advanced.job != EnumJobType.ChunkLoader);

        this.addButton(new GuiNpcButton(7, guiLeft + 15, y += 22, 190, 20, "advanced.lines"));
        this.addButton(new GuiNpcButton(9, guiLeft + 208, y, 190, 20, "menu.factions"));

        this.addButton(new GuiNpcButton(10, guiLeft + 15, y += 22, 190, 20, "dialog.dialogs"));
        this.addButton(new GuiNpcButton(11, guiLeft + 208, y, 190, 20, "advanced.sounds"));

        this.addButton(new GuiNpcButton(12, guiLeft + 15, y += 22, 190, 20, "advanced.night"));
        this.addButton(new GuiNpcButton(13, guiLeft + 208, y, 190, 20, "global.linked"));

        this.addButton(new GuiNpcButton(14, guiLeft + 15, y += 22, 190, 20, "menu.tags"));
        this.addButton(new GuiNpcButton(15, guiLeft + 208, y, 190, 20, "advanced.marks"));

        this.addButton(new GuiNpcButton(16, guiLeft + 15, y += 22, 190, 20, "menu.magics"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 3) {
            save();
            PacketClient.sendClient(new RoleGetPacket());
        }
        if (button.id == 8) {
            hasChanges = true;
            npc.advanced.setRole(button.getValue());

            getButton(3).setEnabled(npc.advanced.role != EnumRoleType.None && npc.advanced.role != EnumRoleType.Postman);
        }
        if (button.id == 4) {
            save();
            PacketClient.sendClient(new JobGetPacket());
        }
        if (button.id == 5) {
            hasChanges = true;
            npc.advanced.setJob(button.getValue());

            getButton(4).setEnabled(npc.advanced.job != EnumJobType.None && npc.advanced.job != EnumJobType.ChunkLoader);
        }
        if (button.id == 9) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCFactionSetup(npc));
        }
        if (button.id == 10) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCDialogNpcOptions(npc, this));
        }
        if (button.id == 11) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCSoundsMenu(npc));
        }
        if (button.id == 7) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCLinesMenu(npc));
        }
        if (button.id == 12) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCNightSetup(npc));
        }
        if (button.id == 13) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCAdvancedLinkedNpc(npc));
        }
        if (button.id == 14) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCTagSetup(npc));
        }
        if (button.id == 15) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCMarks(npc));
        }
        if (button.id == 16) {
            save();
            NoppesUtil.openGUI(player, new GuiNPCMagic(npc));
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("RoleData")) {
            npc.advanced.setRole(compound.getInteger("RoleOrdinal"));
            if (npc.roleInterface != null)
                npc.roleInterface.readFromNBT(compound);

            if (npc.advanced.role == EnumRoleType.Trader)
                NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader);
            else if (npc.advanced.role == EnumRoleType.Follower)
                NoppesUtil.requestOpenGUI(EnumGuiType.SetupFollower);
            else if (npc.advanced.role == EnumRoleType.Bank)
                NoppesUtil.requestOpenGUI(EnumGuiType.SetupBank);
            else if (npc.advanced.role == EnumRoleType.Transporter)
                displayGuiScreen(new GuiNpcTransporter(npc));
            else if (npc.advanced.role == EnumRoleType.Companion)
                displayGuiScreen(new GuiNpcCompanion(npc));
        } else if (compound.hasKey("JobData")) {
            if (npc.jobInterface != null)
                npc.jobInterface.readFromNBT(compound);

            if (npc.advanced.job == EnumJobType.Bard)
                NoppesUtil.openGUI(player, new GuiNpcBard(npc));
            else if (npc.advanced.job == EnumJobType.Healer)
                NoppesUtil.openGUI(player, new GuiNpcHealer(npc));
            else if (npc.advanced.job == EnumJobType.Guard)
                NoppesUtil.openGUI(player, new GuiNpcGuard(npc));
            else if (npc.advanced.job == EnumJobType.ItemGiver)
                NoppesUtil.requestOpenGUI(EnumGuiType.SetupItemGiver);
            else if (npc.advanced.job == EnumJobType.Follower)
                NoppesUtil.openGUI(player, new GuiNpcFollowerJob(npc));
            else if (npc.advanced.job == EnumJobType.Spawner)
                NoppesUtil.openGUI(player, new GuiNpcSpawner(npc));
            else if (npc.advanced.job == EnumJobType.Conversation)
                NoppesUtil.openGUI(player, new GuiNpcConversation(npc));
        } else {
            npc.advanced.readToNBT(compound);
            initGui();
        }
    }

    @Override
    public void save() {
        if (hasChanges) {
            PacketClient.sendClient(new MainmenuAdvancedSavePacket(npc.advanced.writeToNBT(new NBTTagCompound())));
            hasChanges = false;
        }
    }
}
