package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.naturalspawns.NaturalSpawnGetAllPacket;
import kamkeel.npcs.network.packets.request.naturalspawns.NaturalSpawnGetPacket;
import kamkeel.npcs.network.packets.request.naturalspawns.NaturalSpawnRemovePacket;
import kamkeel.npcs.network.packets.request.naturalspawns.NaturalSpawnSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.GuiNpcMobSpawnerSelector;
import noppes.npcs.client.gui.SubGuiNpcBiomes;
import noppes.npcs.client.gui.SubGuiNpcDimensions;
import noppes.npcs.client.gui.SubGuiSpawningOptions;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiHoverText;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class GuiNpcNaturalSpawns extends GuiNPCInterface2 implements IGuiData, IScrollData, ITextfieldListener, ICustomScrollListener, ISliderListener {
    private GuiCustomScroll scrollNaturalSpawns;
    private final GuiCustomScroll spawnEntryScroll = new GuiCustomScroll(this, 20, false);
    private HashMap<String, Integer> data = new HashMap<String, Integer>();
    private String search = "";

    private SpawnData spawn = new SpawnData();

    public GuiNpcNaturalSpawns(EntityNPCInterface npc) {
        super(npc);
        PacketClient.sendClient(new NaturalSpawnGetAllPacket());
    }

    @Override
    public void initGui() {
        super.initGui();
        if (this.scrollNaturalSpawns == null) {
            this.scrollNaturalSpawns = new GuiCustomScroll(this, 0, 0);
            this.scrollNaturalSpawns.setSize(143, 185);
        }
        this.scrollNaturalSpawns.guiLeft = guiLeft + 214;
        this.scrollNaturalSpawns.guiTop = guiTop + 4;
        this.addScroll(this.scrollNaturalSpawns);
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 214, guiTop + 4 + 3 + 185, 143, 20, search));

        this.addButton(new GuiNpcButton(1, guiLeft + 358, guiTop + 38, 58, 20, "gui.add"));
        this.addButton(new GuiNpcButton(2, guiLeft + 358, guiTop + 61, 58, 20, "gui.remove"));

        if (this.spawn.id >= 0)
            showSpawn();
    }

    private void showSpawn() {
        addLabel(new GuiNpcLabel(1, "gui.title", guiLeft + 4, guiTop + 8));
        addTextField(new GuiNpcTextField(1, this, this.fontRendererObj, guiLeft + 60, guiTop + 3, 140, 20, this.spawn.name));

        int y = guiTop + 30;
        addLabel(new GuiNpcLabel(3, "spawning.biomes", guiLeft + 4, y));
        addButton(new GuiNpcButton(3, guiLeft + 40, y - 5, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(11, StatCollector.translateToLocal("Dimensions"), guiLeft + 95, y));
        addButton(new GuiNpcButton(11, guiLeft + 150, y - 5, 50, 20, "selectServer.edit"));

        addLabel(new GuiNpcLabel(4, "spawning.options", guiLeft + 4, y += 22));
        addButton(new GuiNpcButton(4, guiLeft + 90, y - 5, 50, 20, "selectServer.edit"));

        addSlider(new GuiNpcSlider(this, 5, guiLeft + 4, y += 17, 180, 20, (float) this.spawn.itemWeight / 100));
        addExtra(new GuiHoverText(1, "spawning.naturalInfo", guiLeft + 188, y + 5));

        this.spawnEntryScroll.guiLeft = guiLeft + 4;
        this.spawnEntryScroll.guiTop = y + 40;
        this.spawnEntryScroll.setSize(50, 102);
        ArrayList<String> list = new ArrayList<>();
        Set<Integer> keySet = this.spawn.spawnCompounds.keySet();
        for (int i : keySet) {
            list.add(String.valueOf(i));
        }
        this.spawnEntryScroll.setList(list);
        addScroll(this.spawnEntryScroll);

        addButton(new GuiNpcButton(21, guiLeft + 6, y += 20, 20, 20, "+"));
        if (this.spawnEntryScroll.hasSelected()) {
            int selected = Integer.parseInt(this.spawnEntryScroll.getSelected());
            addButton(new GuiNpcButton(22, guiLeft + 32, y, 20, 20, "-"));
            GuiNpcTextField num = new GuiNpcTextField(25, this, guiLeft + 60, y += 10, 30, 20, String.valueOf(selected));
            num.integersOnly = true;
            addTextField(num);
            addButton(new GuiNpcButton(26, guiLeft + 92, y, 100, 20, this.getTitle(this.spawn.spawnCompounds.get(selected))));
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scrollNaturalSpawns.resetScroll();
                scrollNaturalSpawns.setList(getSearchList());
            }
        }
    }

    private List<String> getSearchList() {
        if (search.isEmpty()) {
            return new ArrayList<String>(this.data.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : this.data.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    private String getTitle(NBTTagCompound compound) {
        if (compound != null && compound.hasKey("ClonedName"))
            return compound.getString("ClonedName");
        return "gui.selectnpc";
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 1) {
            save();
            String name = "New";
            while (data.containsKey(name))
                name += "_";

            SpawnData spawn = new SpawnData();
            spawn.name = name;
            PacketClient.sendClient(new NaturalSpawnSavePacket(spawn.writeNBT(new NBTTagCompound())));
            this.spawnEntryScroll.selected = -1;
        }
        if (id == 2) {
            if (data.containsKey(scrollNaturalSpawns.getSelected())) {
                PacketClient.sendClient(new NaturalSpawnRemovePacket(spawn.id));
                spawn = new SpawnData();
                scrollNaturalSpawns.clear();
                this.spawnEntryScroll.selected = -1;
            }
        }
        if (id == 3) {
            setSubGui(new SubGuiNpcBiomes(spawn));
        }
        if (id == 11) {
            setSubGui(new SubGuiNpcDimensions(spawn));
        }
        if (id == 4) {
            setSubGui(new SubGuiSpawningOptions(spawn));
        }
        if (id == 21) {
            int addId = 0;
            if (this.spawnEntryScroll.hasSelected()) {
                int selected = Integer.parseInt(spawnEntryScroll.getSelected());
                ArrayList<Integer> keys = new ArrayList<>(this.spawn.spawnCompounds.keySet());
                int keyIndex = keys.indexOf(selected);
                do {
                    addId = keys.get(keyIndex) + 1;
                    keyIndex++;
                } while (this.spawn.spawnCompounds.containsKey(addId));
            } else if (this.spawn.spawnCompounds.size() > 0) {
                addId = (Integer) this.spawn.spawnCompounds.keySet().toArray()[this.spawn.spawnCompounds.size() - 1] + 1;
            }
            this.spawn.spawnCompounds.put(addId, new NBTTagCompound());
            this.initGui();
        }
        if (id == 22) {
            if (this.spawnEntryScroll.hasSelected()) {
                int selected = Integer.parseInt(spawnEntryScroll.getSelected());
                this.spawn.spawnCompounds.remove(selected);
                this.spawnEntryScroll.selected = -1;
                this.initGui();
            }
        }
        if (id == 26) {
            setSubGui(new GuiNpcMobSpawnerSelector());
        }
    }


    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 1) {
            String name = textfield.getText();
            if (name.isEmpty() || data.containsKey(name)) {
                textfield.setText(spawn.name);
            } else {
                String old = spawn.name;
                data.remove(old);
                spawn.name = name;
                data.put(spawn.name, spawn.id);
                scrollNaturalSpawns.replace(old, spawn.name);
            }
        }
        if (textfield.id == 25 && this.spawnEntryScroll.hasSelected()) {
            int selected = Integer.parseInt(spawnEntryScroll.getSelected());
            if (spawnEntryScroll.getList().contains(String.valueOf(textfield.getInteger()))) {
                textfield.setText(String.valueOf(selected));
                return;
            }
            NBTTagCompound compound = spawn.spawnCompounds.get(selected);
            spawn.spawnCompounds.remove(selected);
            spawn.spawnCompounds.put(textfield.getInteger(), compound);
            spawnEntryScroll.selected = -1;
            initGui();
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        String name = scrollNaturalSpawns.getSelected();
        this.data = data;
        scrollNaturalSpawns.setList(getSearchList());

        if (name != null)
            scrollNaturalSpawns.setSelected(name);
        initGui();
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            save();
            String selected = scrollNaturalSpawns.getSelected();
            spawn = new SpawnData();
            PacketClient.sendClient(new NaturalSpawnGetPacket(data.get(selected)));
        }
        if (guiCustomScroll.id == 20) {
            initGui();
        }
    }

    @Override
    public void save() {
        GuiNpcTextField.unfocus();
        if (spawn.id >= 0)
            PacketClient.sendClient(new NaturalSpawnSavePacket(spawn.writeNBT(new NBTTagCompound())));
    }

    @Override
    public void setSelected(String selected) {
    }

    @Override
    public void closeSubGui(SubGuiInterface gui) {
        super.closeSubGui(gui);
        if (gui instanceof GuiNpcMobSpawnerSelector && this.spawnEntryScroll.hasSelected()) {
            GuiNpcMobSpawnerSelector selector = (GuiNpcMobSpawnerSelector) gui;
            int selected = Integer.parseInt(this.spawnEntryScroll.getSelected());
            this.spawn.spawnCompounds.put(selected, selector.getCompound());
            initGui();
        }
        if (gui instanceof SubGuiNpcBiomes || gui instanceof SubGuiNpcDimensions || gui instanceof SubGuiSpawningOptions) {
            save();
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        spawn.readNBT(compound);
        setSelected(spawn.name);
        initGui();
    }

    @Override
    public void mouseDragged(GuiNpcSlider guiNpcSlider) {
        guiNpcSlider.displayString = StatCollector.translateToLocal("spawning.weightedChance") + ": " + (int) (guiNpcSlider.sliderValue * 100);
    }

    @Override
    public void mousePressed(GuiNpcSlider guiNpcSlider) {
    }

    @Override
    public void mouseReleased(GuiNpcSlider guiNpcSlider) {
        spawn.itemWeight = (int) (guiNpcSlider.sliderValue * 100);
    }
}
