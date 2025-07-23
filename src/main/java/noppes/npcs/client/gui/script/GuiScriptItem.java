package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.item.ItemScriptErrorPacket;
import kamkeel.npcs.network.packets.request.script.item.ItemScriptPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.NBTTags;
import noppes.npcs.scripted.item.ScriptCustomItem;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GuiScriptItem extends GuiScriptInterface {
    private ScriptCustomItem item;
    public static Map<Long, String> consoleText = new HashMap<>();

    public GuiScriptItem() {
        hookList.add("init");
        hookList.add("tick");
        hookList.add("tossed");
        hookList.add("pickedUp");
        hookList.add("spawn");
        hookList.add("interact");
        hookList.add("rightClick");
        hookList.add("attack");
        hookList.add("startItem");
        hookList.add("usingItem");
        hookList.add("stopItem");
        hookList.add("finishItem");

        this.handler = this.item = new ScriptCustomItem(new ItemStack(CustomItems.scripted_item));
        ItemScriptErrorPacket.Get();
        ItemScriptPacket.Get();
    }

    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("ItemScriptConsole")) {
            consoleText = NBTTags.GetLongStringMap(compound.getTagList("ItemScriptConsole", 10));
            initGui();
        } else {
            this.item.setMCNbt(compound);
            this.item.loadScriptData();
            super.setGuiData(compound);
            loaded = true;
        }
    }

    public void save() {
        if (loaded) {
            super.save();
            ItemScriptPacket.Save(this.item.getMCNbt());
        }
    }

    protected String getConsoleText() {
        Map<Long, String> map = consoleText;
        StringBuilder builder = new StringBuilder();
        Iterator var3 = map.entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<Long, String> entry = (Map.Entry) var3.next();
            builder.insert(0, new Date((Long) entry.getKey()) + (String) entry.getValue() + "\n");
        }

        return builder.toString();
    }

    public void confirmClicked(boolean flag, int i) {
        if (i == 102) {
            if (activeTab <= 0) {
                consoleText = new HashMap<>();
                ItemScriptErrorPacket.Clear();
            }
        }
        super.confirmClicked(flag, i);
    }
}
