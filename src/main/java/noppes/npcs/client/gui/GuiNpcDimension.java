package noppes.npcs.client.gui;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.DimensionsGetPacket;
import kamkeel.npcs.network.packets.request.feather.DimensionTeleportPacket;
import kamkeel.npcs.network.packets.request.npc.RemoteDeletePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumScrollData;

import java.util.HashMap;
import java.util.Vector;

public class GuiNpcDimension extends GuiNPCInterface implements IScrollData {

    private GuiCustomScroll scroll;
    private HashMap<String, Integer> data = new HashMap<String, Integer>();

    public GuiNpcDimension() {
        super();
        xSize = 256;
        setBackground("menubg.png");
        PacketClient.sendClient(new DimensionsGetPacket());
    }

    public void initGui() {
        super.initGui();
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(165, 208);
        }
        scroll.guiLeft = guiLeft + 4;
        scroll.guiTop = guiTop + 4;
        addScroll(scroll);

        String title = StatCollector.translateToLocal("Dimensions");
        int x = (xSize - this.fontRendererObj.getStringWidth(title)) / 2;

        this.addLabel(new GuiNpcLabel(0, title, guiLeft + x, guiTop - 8));

        this.addButton(new GuiNpcButton(4, guiLeft + 170, guiTop + 72, 82, 20, "remote.tp"));
    }

    @Override
    public void confirmClicked(boolean flag, int i) {
        if (flag) {
            PacketClient.sendClient(new RemoteDeletePacket(data.get(scroll.getSelected())));
        }
        NoppesUtil.openGUI(player, this);
    }

    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;

        if (!data.containsKey(scroll.getSelected()))
            return;

        if (id == 4) {
            PacketClient.sendClient(new DimensionTeleportPacket(data.get(scroll.getSelected())));
            close();
        }
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        scroll.mouseClicked(i, j, k);
    }

    @Override
    public void keyTyped(char c, int i) {
        if (i == 1 || isInventoryKey(i)) {
            close();
        }
    }

    @Override
    public void save() {


    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        scroll.setList(list);
        this.data = data;
    }

    @Override
    public void setSelected(String selected) {
        getButton(3).setDisplayText(selected);
    }

}
