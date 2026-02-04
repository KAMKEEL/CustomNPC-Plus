package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.clone.CloneAllTagsShortPacket;
import kamkeel.npcs.network.packets.request.clone.ClonePreSavePacket;
import kamkeel.npcs.network.packets.request.clone.CloneSavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.CloneFolder;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class GuiNpcMobSpawnerAdd extends GuiNPCInterface implements GuiYesNoCallback, IGuiData, ISubGuiListener {

    private Entity toClone;
    private NBTTagCompound compound;
    private static boolean serverSide = false;
    private static int saveMode = 0; // 0=Tab, 1=Folder
    private static int tab = 1;
    private static String folder = null;
    public boolean isNPC = false;

    // Folder names cache for the selector
    private String[] folderLabels = new String[0];

    // Selected Tags to Add
    public static NBTTagList tagsCompound;
    public static HashSet<String> addTags;
    public static ArrayList<String> allTags = new ArrayList<>();
    public static HashSet<UUID> addTagUUIDs;
    public static HashMap<String, UUID> tagMap = new HashMap<>();

    public GuiNpcMobSpawnerAdd(NBTTagCompound compound) {
        this.toClone = EntityList.createEntityFromNBT(compound, Minecraft.getMinecraft().theWorld);
        this.compound = compound;
        tagsCompound = new NBTTagList();

        if (toClone instanceof EntityNPCInterface) {
            isNPC = true;
            tagsCompound = this.compound.getTagList("TagUUIDs", 8);
        }
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;

        if (addTags == null) {
            addTags = new HashSet<>();
        }
        if (addTagUUIDs == null) {
            addTagUUIDs = new HashSet<>();
        }
        if (isNPC) {
            PacketClient.sendClient(new CloneAllTagsShortPacket());
        }

        buildFolderLabels();
    }

    private void buildFolderLabels() {
        ArrayList<String> names = new ArrayList<>();
        if (ClientCloneController.Instance != null) {
            for (CloneFolder f : ClientCloneController.Instance.getFolderList()) {
                names.add(f.name);
            }
        }
        folderLabels = names.toArray(new String[0]);
    }

    @Override
    public void initGui() {
        super.initGui();
        String name = toClone.getCommandSenderName();
        addLabel(new GuiNpcLabel(0, "Save as", guiLeft + 4, guiTop + 6));
        addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 4, guiTop + 18, 200, 20, name));

        // Tab/Folder toggle
        addButton(new GuiNpcButton(6, guiLeft + 4, guiTop + 45, 50, 20,
            new String[]{"Tab", "Folder"}, saveMode));

        // Destination selector based on saveMode
        if (saveMode == 0) {
            // Tab selector: 1-15
            String[] tabLabels = new String[15];
            for (int i = 0; i < 15; i++) tabLabels[i] = String.valueOf(i + 1);
            int selectedTab = (tab >= 1 && tab <= 15) ? tab - 1 : 0;
            addButton(new GuiButtonBiDirectional(2, guiLeft + 56, guiTop + 45, 90, 20, tabLabels, selectedTab));
        } else {
            // Folder selector
            if (folderLabels.length > 0) {
                int selectedFolder = 0;
                if (folder != null) {
                    for (int i = 0; i < folderLabels.length; i++) {
                        if (folder.equals(folderLabels[i])) {
                            selectedFolder = i;
                            break;
                        }
                    }
                }
                addButton(new GuiButtonBiDirectional(2, guiLeft + 56, guiTop + 45, 90, 20, folderLabels, selectedFolder));
            } else {
                addLabel(new GuiNpcLabel(7, "No folders", guiLeft + 60, guiTop + 51));
            }
        }

        addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 70, 80, 20, "gui.save"));
        addButton(new GuiNpcButton(1, guiLeft + 86, guiTop + 70, 80, 20, "gui.cancel"));

        addButton(new GuiNpcButton(3, guiLeft + 4, guiTop + 95, new String[]{"Client side", "Server side"}, serverSide ? 1 : 0));

        if (isNPC) {
            addButton(new GuiNpcButton(4, guiLeft + 4, guiTop + 120, 99, 20, "cloner.wandTags"));
            addButton(new GuiNpcButton(5, guiLeft + 106, guiTop + 120, 99, 20, "cloner.npcTags"));
            if (addTags.size() > 0) {
                addLabel(new GuiNpcLabel(8, "cloner.wandtagsapplied", guiLeft + 10, guiTop + 160));
            }
        }
    }

    private void updateDestinationFromSelector() {
        GuiNpcButton selector = getButton(2);
        if (selector == null) return;

        int index = selector.getValue();
        if (saveMode == 0) {
            // Tab mode
            tab = index + 1;
            folder = null;
        } else {
            // Folder mode
            if (index >= 0 && index < folderLabels.length) {
                folder = folderLabels[index];
                tab = -1;
            }
        }
    }

    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 0) {
            updateDestinationFromSelector();
            String name = getTextField(0).getText();
            if (name.isEmpty())
                return;

            if (saveMode == 1 && folderLabels.length == 0)
                return;

            if (!serverSide) {
                boolean exists;
                if (saveMode == 1 && folder != null && ClientCloneController.Instance != null) {
                    exists = ClientCloneController.Instance.getCloneData(null, name, folder) != null;
                } else {
                    exists = ClientCloneController.Instance.getCloneData(null, name, tab) != null;
                }
                if (exists)
                    displayGuiScreen(new GuiYesNo(this, "Warning", "You are about to overwrite a clone", 1));
                else
                    confirmClicked(true, 0);
            } else {
                if (saveMode == 1 && folder != null) {
                    PacketClient.sendClient(new ClonePreSavePacket(name, folder));
                } else {
                    PacketClient.sendClient(new ClonePreSavePacket(name, tab));
                }
            }
        }
        if (id == 1) {
            close();
        }
        if (id == 2) {
            updateDestinationFromSelector();
        }
        if (id == 3) {
            serverSide = ((GuiNpcButton) guibutton).getValue() == 1;
        }
        if (id == 4) {
            if (isNPC) {
                this.setSubGui(new SubGuiClonerQuickTags(this));
            }
        }
        if (id == 5) {
            if (isNPC) {
                this.setSubGui(new SubGuiClonerNPCTags((EntityNPCInterface) toClone, this));
            }
        }
        if (id == 6) {
            saveMode = ((GuiNpcButton) guibutton).getValue();
            initGui();
        }
    }


    @Override
    public void confirmClicked(boolean confirm, int id) {
        if (confirm) {
            String name = getTextField(0).getText();
            NBTTagCompound extraTags = new NBTTagCompound();
            if (isNPC) {
                extraTags = setTempTags();
            }
            if (!serverSide) {
                if (isNPC) {
                    compound.setTag("TagUUIDs", tagsCompound);
                }
                if (saveMode == 1 && folder != null && ClientCloneController.Instance != null) {
                    ClientCloneController.Instance.addClone(compound, name, folder, extraTags);
                } else {
                    ClientCloneController.Instance.addClone(compound, name, tab, extraTags);
                }
            } else {
                NBTTagCompound compounder = new NBTTagCompound();
                if (isNPC) {
                    compounder.setTag("TagUUIDs", tagsCompound);
                }
                if (saveMode == 1 && folder != null) {
                    PacketClient.sendClient(new CloneSavePacket(name, folder, extraTags, compounder));
                } else {
                    PacketClient.sendClient(new CloneSavePacket(name, tab, extraTags, compounder));
                }
            }

            close();
        } else
            displayGuiScreen(this);
    }


    @Override
    public void save() {
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("NameExists")) {
            if (compound.getBoolean("NameExists"))
                displayGuiScreen(new GuiYesNo(this, "Warning", "You are about to overwrite a clone", 1));
            else
                confirmClicked(true, 0);
        } else if (compound.hasKey("ShortTags")) {
            NBTTagList validTags = compound.getTagList("ShortTags", 10);
            tagMap.clear();
            allTags.clear();
            addTagUUIDs.clear();
            if (validTags != null) {
                for (int j = 0; j < validTags.tagCount(); j++) {
                    NBTTagCompound tagStructure = validTags.getCompoundTagAt(j);
                    Tag tag = new Tag();
                    tag.readShortNBT(tagStructure);
                    tagMap.put(tag.name, tag.uuid);
                    addTagUUIDs.add(tag.uuid);
                }
                allTags.addAll(tagMap.keySet());
                allTags.sort(String.CASE_INSENSITIVE_ORDER);
            }
            initGui();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        initGui();
    }

    public NBTTagCompound setTempTags() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        NBTTagList nbtTagList = new NBTTagList();
        for (String name : addTags) {
            if (tagMap.containsKey(name)) {
                nbtTagList.appendTag(new NBTTagString(tagMap.get(name).toString()));
            } else {
                addTags.remove(name);
            }
        }
        nbtTagCompound.setTag("TempTagUUIDs", nbtTagList);
        return nbtTagCompound;
    }
}
