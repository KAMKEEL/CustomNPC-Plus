package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.item.ItemScriptErrorPacket;
import kamkeel.npcs.network.packets.request.script.item.ItemScriptPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptHookController;
import noppes.npcs.scripted.item.ScriptCustomItem;

import java.util.*;

public class GuiScriptItem extends GuiScriptInterface {
    private ScriptCustomItem item;
    public static Map<Long, String> consoleText = new HashMap<>();

    public GuiScriptItem() {
        this.hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(IScriptHookHandler.CONTEXT_ITEM));

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

    @Override
    protected ScriptContext getScriptContext() {
        return ScriptContext.ITEM;
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
