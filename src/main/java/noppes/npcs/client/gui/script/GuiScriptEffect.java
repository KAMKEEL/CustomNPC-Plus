package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.EffectScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.global.GuiNPCManageEffects;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.EffectScript;

public class GuiScriptEffect extends GuiScriptInterface {

    public final CustomEffect effect;
    private final EffectScript scriptHandler;

    public GuiScriptEffect(GuiNPCManageEffects parent, CustomEffect effect) {
        super();
        this.parent = parent;
        this.effect = effect;
        this.scriptHandler = new EffectScript();
        this.handler = this.scriptHandler;
        this.singleContainer = true;

        this.hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(IScriptHookHandler.CONTEXT_EFFECT));

        EffectScriptPacket.Get(effect.id);
    }

    @Override
    protected ScriptContext getScriptContext() {
        return ScriptContext.GLOBAL;
    }

    protected void setHandlerContainer(ScriptContainer container) {
        this.scriptHandler.container = container;
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        setGuiDataWithOldContainer(compound);
    }

    protected void sendSavePacket(int index, int totalCount, NBTTagCompound scriptNBT) {
        EffectScriptPacket.Save(effect.id, index, totalCount, scriptNBT);
    }

    @Override
    public void save() {
        saveWithPackets();
    }
}
