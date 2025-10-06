package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.quest.QuestOpenGuiPacket;
import kamkeel.npcs.network.packets.request.quest.QuestSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeDialog;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeKill;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeLocation;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeManual;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.gui.util.SubGuiNpcCooldownPicker;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.Quest;

public class SubGuiNpcQuest extends SubGuiInterface implements ISubGuiListener, GuiSelectionListener, ITextfieldListener {

    public int questCategoryID;
    public Quest quest;
    private final GuiNPCManageQuest parent;
    private boolean questlogTA = false;

    public SubGuiNpcQuest(GuiNPCManageQuest parent, Quest quest, int catId) {
        this.parent = parent;
        this.quest = quest;
        this.questCategoryID = catId;
        setBackground("menubg.png");
        xSize = 360;
        ySize = 216;
    }

    public void initGui() {
        super.initGui();

        addLabel(new GuiNpcLabel(1, "gui.title", guiLeft + 4, guiTop + 8));
        addTextField(new GuiNpcTextField(1, this, this.fontRendererObj, guiLeft + 36, guiTop + 3, 200, 20, quest.title));

        addLabel(new GuiNpcLabel(0, "ID", guiLeft + 238, guiTop + 4));
        addLabel(new GuiNpcLabel(2, quest.id + "", guiLeft + 238, guiTop + 14));

        addLabel(new GuiNpcLabel(3, "quest.completedtext", guiLeft + 7, guiTop + 33));
        addButton(new GuiNpcButton(3, guiLeft + 120, guiTop + 28, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(4, "quest.questlogtext", guiLeft + 7, guiTop + 57));
        addButton(new GuiNpcButton(4, guiLeft + 120, guiTop + 52, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(5, "quest.reward", guiLeft + 7, guiTop + 81));
        addButton(new GuiNpcButton(5, guiLeft + 120, guiTop + 76, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(10, "faction.options", guiLeft + 180, guiTop + 33));
        addButton(new GuiNpcButton(10, guiLeft + 303, guiTop + 28, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(11, "advMode.command", guiLeft + 180, guiTop + 57));
        addButton(new GuiNpcButton(11, guiLeft + 303, guiTop + 52, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(6, "gui.type", guiLeft + 180, guiTop + 81));
        addButton(new GuiNpcButton(6, guiLeft + 240, guiTop + 76, 60, 20, new String[]{"quest.item", "quest.dialog", "quest.kill", "quest.location", "quest.areakill", "quest.manual"}, quest.type.ordinal()));
        addButton(new GuiNpcButton(7, guiLeft + 303, guiTop + 76, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(17, "party.options", guiLeft + 180, guiTop + 135));
        addButton(new GuiNpcButton(18, guiLeft + 303, guiTop + 130, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(33, "quest.profile", guiLeft + 180, guiTop + 159));
        addButton(new GuiNpcButton(33, guiLeft + 303, guiTop + 154, 50, 20, "selectServer.edit"));

        this.addButton(new GuiNpcButton(9, guiLeft + 7, guiTop + 102, 90, 20, new String[]{"quest.npc", "quest.instant"}, quest.completion.ordinal()));
        if (quest.completerNpc.isEmpty() && npc != null)
            quest.completerNpc = npc.display.name;
        this.addTextField(new GuiNpcTextField(2, this, this.fontRendererObj, guiLeft + 104, guiTop + 102, 154, 20, quest.completerNpc));
        this.getTextField(2).enabled = quest.completion == EnumQuestCompletion.Npc;

        addButton(new GuiNpcButton(15, guiLeft + 4, guiTop + 130, 144, 20, "quest.next"));
        addButton(new GuiNpcButton(12, guiLeft + 150, guiTop + 130, 20, 20, "X"));
        if (!parent.nextQuestName.isEmpty())
            getButton(15).setDisplayText(parent.nextQuestName);

        addButton(new GuiNpcButton(13, guiLeft + 4, guiTop + 154, 144, 20, "mailbox.setup"));
        addButton(new GuiNpcButton(14, guiLeft + 150, guiTop + 154, 20, 20, "X"));
        if (!quest.mail.subject.isEmpty())
            getButton(13).setDisplayText(quest.mail.subject);

        addLabel(new GuiNpcLabel(8, "quest.repeatable", guiLeft + 7, guiTop + 178 + 5));
        this.addButton(new GuiNpcButton(8, guiLeft + 100, guiTop + 178, 70, 20,
            new String[]{"gui.no", "gui.yes", "quest.mcdaily", "quest.mcweekly", "quest.rldaily", "quest.rlweekly", "quest.mccustom", "quest.rlcustom"}, quest.repeat.ordinal()));
        if (quest.repeat == EnumQuestRepeat.MCCUSTOM || quest.repeat == EnumQuestRepeat.RLCUSTOM) {
            addButton(new GuiNpcButton(19, guiLeft + 175, guiTop + 178, 90, 20, "Set Cooldown"));
        }
        addButton(new GuiNpcButton(16, guiLeft + 303, guiTop + 192, 50, 20, "gui.done"));

        if (!quest.mail.subject.isEmpty())
            getButton(13).setDisplayText(quest.mail.subject);
    }

    public void buttonEvent(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if (button.id == 3 && quest.id >= 0) {
            questlogTA = false;
            setSubGui(new SubGuiNpcTextArea(quest.completeText));
        }
        if (button.id == 4 && quest.id >= 0) {
            questlogTA = true;
            setSubGui(new SubGuiNpcTextArea(quest.logText));
        }
        if (button.id == 5 && quest.id >= 0) {
            PacketClient.sendClient(new QuestOpenGuiPacket(EnumGuiType.QuestReward, quest.writeToNBT(new NBTTagCompound())));
        }
        if (button.id == 6 && quest.id >= 0) {
            quest.setType(EnumQuestType.values()[button.getValue()]);
        }
        if (button.id == 7) {
            if (quest.type == EnumQuestType.Item)
                PacketClient.sendClient(new QuestOpenGuiPacket(EnumGuiType.QuestItem, quest.writeToNBT(new NBTTagCompound())));

            if (quest.type == EnumQuestType.Dialog)
                setSubGui(new GuiNpcQuestTypeDialog(npc, quest, this));

            if (quest.type == EnumQuestType.Kill)
                setSubGui(new GuiNpcQuestTypeKill(npc, quest, this));

            if (quest.type == EnumQuestType.Location)
                setSubGui(new GuiNpcQuestTypeLocation(npc, quest, this));

            if (quest.type == EnumQuestType.AreaKill)
                setSubGui(new GuiNpcQuestTypeKill(npc, quest, this));

            if (quest.type == EnumQuestType.Manual)
                setSubGui(new GuiNpcQuestTypeManual(npc, quest, this));
        }
        if (button.id == 8) {
            quest.repeat = EnumQuestRepeat.values()[button.getValue()];
            initGui();
        }
        if (button.id == 9) {
            quest.completion = EnumQuestCompletion.values()[button.getValue()];
            this.getTextField(2).enabled = quest.completion == EnumQuestCompletion.Npc;
        }
        if (button.id == 10) {
            setSubGui(new SubGuiNpcFactionOptions(quest.factionOptions));
        }
        if (button.id == 11) {
            setSubGui(new SubGuiNpcCommand(quest.command));
        }
        if (button.id == 12 && quest.id >= 0) {
            quest.nextQuestid = -1;
            parent.nextQuestName = "";
            initGui();
        }
        if (button.id == 13) {
            setSubGui(new SubGuiMailmanSendSetup(quest.mail, getParent()));
        }
        if (button.id == 14) {
            quest.mail = new PlayerMail();
            initGui();
        }
        if (button.id == 15 && quest.id >= 0) {
            setSubGui(new GuiQuestSelection(quest.nextQuestid));
        }
        if (button.id == 18) {
            setSubGui(new SubGuiNpcPartyOptions(quest.partyOptions));
        }
        if (button.id == 19) {
            boolean isMCCustom = (quest.repeat == EnumQuestRepeat.MCCUSTOM);
            setSubGui(new SubGuiNpcCooldownPicker(isMCCustom, quest.customCooldown));
        }
        if (button.id == 33) {
            setSubGui(new SubGuiNpcProfileOptions(quest.profileOptions));
        }
        if (button.id == 16) {
            close();
        }
    }


    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        if (guiNpcTextField.id == 1) {
            if (quest.id < 0)
                guiNpcTextField.setText("");
            else {
                String name = guiNpcTextField.getText();
                if (name.isEmpty() || this.parent.questData.containsKey(name)) {
                    guiNpcTextField.setText(quest.title);
                } else if (quest.id >= 0) {
                    String old = quest.title;
                    this.parent.questData.remove(old);
                    quest.title = name;
                    this.parent.questData.put(quest.title, quest.id);
                    this.parent.questScroll.replace(old, quest.title);
                }
            }
        }
        if (guiNpcTextField.id == 2) {
            quest.completerNpc = guiNpcTextField.getText();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiNpcTextArea) {
            SubGuiNpcTextArea gui = (SubGuiNpcTextArea) subgui;
            if (questlogTA)
                quest.logText = gui.text;
            else
                quest.completeText = gui.text;
        } else if (subgui instanceof SubGuiNpcCommand) {
            SubGuiNpcCommand sub = (SubGuiNpcCommand) subgui;
            quest.command = sub.command;
            initGui();

        } else if (subgui instanceof SubGuiNpcCooldownPicker) {
            SubGuiNpcCooldownPicker cooldownGui = (SubGuiNpcCooldownPicker) subgui;
            quest.customCooldown = cooldownGui.cooldownValue;
            initGui();
        } else {
            initGui();
        }
    }

    @Override
    public void selected(int id, String name) {
        quest.nextQuestid = id;
        quest.nextQuestTitle = name;
        parent.nextQuestName = name;
        initGui();
        PacketClient.sendClient(new QuestSavePacket(this.questCategoryID, quest.writeToNBT(new NBTTagCompound()), false));
    }

    public void save() {
        PacketClient.sendClient(new QuestSavePacket(this.questCategoryID, quest.writeToNBT(new NBTTagCompound()), false));
    }
}
