package noppes.npcs.client.gui.roles;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.bank.BanksGetPacket;
import kamkeel.npcs.network.packets.request.role.RoleSavePacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleBank;

import java.util.HashMap;
import java.util.Vector;


public class GuiNpcBankSetup extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener {
    private GuiCustomScroll scroll;
    private HashMap<String, Integer> data = new HashMap<String, Integer>();
    private final RoleBank role;

    public GuiNpcBankSetup(EntityNPCInterface npc) {
        super(npc);
        PacketClient.sendClient(new BanksGetPacket());
        role = (RoleBank) npc.roleInterface;
    }

    public void initGui() {
        super.initGui();
        if (scroll == null)
            scroll = new GuiCustomScroll(this, 0);
        scroll.setSize(200, 152);
        scroll.guiLeft = guiLeft + 85;
        scroll.guiTop = guiTop + 20;
        addScroll(scroll);
    }

    protected void actionPerformed(GuiButton guibutton) {
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        String name = null;
        Bank bank = role.getBank();
        if (bank != null)
            name = bank.name;
        this.data = data;
        scroll.setList(list);

        if (name != null)
            setSelected(name);
    }

    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if (k == 0 && scroll != null)
            scroll.mouseClicked(i, j, k);
    }

    @Override
    public void setSelected(String selected) {
        scroll.setSelected(selected);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            role.bankId = data.get(scroll.getSelected());
            save();
        }
    }

    public void save() {
        PacketClient.sendClient(new RoleSavePacket(role.writeToNBT(new NBTTagCompound())));
    }
}
