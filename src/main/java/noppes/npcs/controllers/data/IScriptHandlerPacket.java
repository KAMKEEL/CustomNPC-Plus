package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;

import java.util.List;

public interface IScriptHandlerPacket extends IScriptHandler {

    /**
     * Client-side: request initial script data from the server (typically a {@code ...Packet.Get(...)}).
     */
    void requestData();

    /**
     * Client-side: send a script container packet to the server.
     *
     * @param index {@code >= 0} for a script tab, {@code -1} for handler metadata (language/enabled/console)
     * @param totalCount Total number of scripts for this handler
     * @param nbt The script container NBT (tab content or metadata)
     */
    void sendSavePacket(int index, int totalCount, NBTTagCompound nbt);

    enum GuiDataKind {
        LOAD_COMPLETE,
        METADATA,
        TAB
    }

    class GuiDataResult {
        public final GuiDataKind kind;
        public final int tabIndex;

        public GuiDataResult(GuiDataKind kind, int tabIndex) {
            this.kind = kind;
            this.tabIndex = tabIndex;
        }
    }

    /**
     * Apply one incoming gui-data payload to this handler.
     * <p>
     * Notes:
     * <ul>
     *     <li>{@code Languages} / {@code ScriptConsole} are not consumed here; GuiScriptInterface owns those UI concerns.</li>
     *     <li>Tab payloads are converted to an {@link IScriptUnit} via {@link IScriptUnit#createFromNBT}.</li>
     * </ul>
     *
     * @return A small classification result so GuiScriptInterface can update GUI state.
     */
    default GuiDataResult setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("LoadComplete")) {
            return new GuiDataResult(GuiDataKind.LOAD_COMPLETE, -1);
        }

        if (!compound.hasKey("Tab")) {
            setLanguage(compound.getString("ScriptLanguage"));
            setEnabled(compound.getBoolean("ScriptEnabled"));
            return new GuiDataResult(GuiDataKind.METADATA, -1);
        }

        int tab = compound.getInteger("Tab");
        NBTTagCompound scriptCompound = compound.getCompoundTag("Script");
        IScriptUnit unit = IScriptUnit.createFromNBT(scriptCompound, this);
        replaceScriptUnit(tab, unit);
        return new GuiDataResult(GuiDataKind.TAB, tab);
    }

    /**
     * Emit save packets for all tabs plus one metadata packet.
     * GuiScriptInterface is responsible for updating the active {@link IScriptUnit} with current editor text first.
     */
    default void sync() {
        List<IScriptUnit> containers = getScripts();
        for (int i = 0; i < containers.size(); i++) {
            IScriptUnit container = containers.get(i);
            sendSavePacket(i, containers.size(), container.writeToNBT(new NBTTagCompound()));
        }

        NBTTagCompound scriptData = new NBTTagCompound();
        scriptData.setString("ScriptLanguage", getLanguage());
        scriptData.setBoolean("ScriptEnabled", getEnabled());
        scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(getConsoleText()));
        sendSavePacket(-1, containers.size(), scriptData);
    }
}
