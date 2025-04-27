package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ICustomEffect;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.scripted.event.player.PlayerEvent;

import java.util.function.Consumer;

public class CustomEffect implements ICustomEffect {
    public int id = -1;

    public String name = "";
    public boolean lossOnDeath = true;
    public int length = 30;

    // Must be a multiple of 10
    public int everyXTick = 20;

    public String icon = "";
    public int iconX = 0, iconY = 0;

    /**
     * Experimental script stuff.
     */
    public Consumer<PlayerEvent.EffectEvent.Added> onAddedConsumer;
    public Consumer<PlayerEvent.EffectEvent.Ticked> onTickConsumer;
    public Consumer<PlayerEvent.EffectEvent.Removed> onRemovedConsumer;
    public String menuName = "§aNEW EFFECT";
    public int width = 16, height = 16;

    public int index = 0;

    public CustomEffect() {
    }

    public CustomEffect(int id) {
        this.id = id;
    }

    public CustomEffect(int id, String name) {
        this(id);
        this.name = name;
    }

    @Override
    public int getID() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setMenuName(String name) {
        if (name != null && !name.isEmpty())
            this.menuName = name.replaceAll("&", "§");
    }

    @Override
    public String getMenuName() {
        return menuName;
    }

    @Override
    public void setName(String name) {
        if (name != null && !name.isEmpty())
            this.name = name;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public int getEveryXTick() {
        return everyXTick;
    }

    @Override
    public void setEveryXTick(int everyXTick) {
        if (everyXTick < 10)
            everyXTick = 10;
        int remainder = everyXTick % 10;
        if (remainder >= 5)
            everyXTick += 10 - remainder;
        else
            everyXTick -= remainder;
        this.everyXTick = everyXTick;
    }

    @Override
    public int getIconX() {
        return iconX;
    }

    @Override
    public void setIconX(int iconX) {
        this.iconX = iconX;
    }

    @Override
    public int getIconY() {
        return iconY;
    }

    @Override
    public void setIconY(int iconY) {
        this.iconY = iconY;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public boolean isLossOnDeath() {
        return lossOnDeath;
    }

    @Override
    public void setLossOnDeath(boolean lossOnDeath) {
        this.lossOnDeath = lossOnDeath;
    }

    @Override
    public ICustomEffect save() {
        return CustomEffectController.getInstance().saveEffect(this);
    }

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public int getIndex() {
        return this.index;
    }


    public void onAdded(Consumer<PlayerEvent.EffectEvent.Added> function) {
        onAddedConsumer = function;
    }

    public void onTick(Consumer<PlayerEvent.EffectEvent.Ticked> function) {
        onTickConsumer = function;
    }

    public void onRemoved(Consumer<PlayerEvent.EffectEvent.Removed> function) {
        onRemovedConsumer = function;
    }

    public void onAdded(EntityPlayer player, PlayerEffect playerEffect) {
        IPlayer iPlayer = NoppesUtilServer.getIPlayer(player);
        if(playerEffect.index == 0){
            PlayerEvent.EffectEvent.Added event = new PlayerEvent.EffectEvent.Added(iPlayer, playerEffect);

            if (onAddedConsumer != null)
                onAddedConsumer.accept(event);

            EffectScript script = getScriptHandler();
            if (script == null) {
                return;
            }

            script.callScript(EffectScript.ScriptType.OnEffectAdd, event);
        }

        EventHooks.onEffectAdded(iPlayer, playerEffect);
    }

    public void onTick(EntityPlayer player, PlayerEffect playerEffect) {
        IPlayer iPlayer = NoppesUtilServer.getIPlayer(player);
        if(playerEffect.index == 0){
            PlayerEvent.EffectEvent.Ticked event = new PlayerEvent.EffectEvent.Ticked(iPlayer, playerEffect);

            if (onTickConsumer != null) {
                onTickConsumer.accept(event);
            }

            EffectScript script = getScriptHandler();
            if (script == null) {
                return;
            }

            script.callScript(EffectScript.ScriptType.OnEffectTick, event);
        }

        EventHooks.onEffectTick(iPlayer, playerEffect);
    }

    public void onRemoved(EntityPlayer player, PlayerEffect playerEffect, PlayerEvent.EffectEvent.ExpirationType type) {
        IPlayer iPlayer = NoppesUtilServer.getIPlayer(player);

        if(playerEffect.index == 0){
            PlayerEvent.EffectEvent.Removed event = new PlayerEvent.EffectEvent.Removed(iPlayer, playerEffect, type);

            if (onRemovedConsumer != null) {
                onRemovedConsumer.accept(event);
            }

            EffectScript script = getScriptHandler();
            if (script == null) {
                return;
            }

            script.callScript(EffectScript.ScriptType.OnEffectRemove, event);
        }

        EventHooks.onEffectRemove(iPlayer, playerEffect, type);
    }

    public NBTTagCompound writeToNBT(boolean saveScripts) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("ID", id);
        compound.setString("name", name);
        compound.setString("menuName", menuName);
        compound.setInteger("length", length);
        compound.setInteger("everyXTick", everyXTick);
        compound.setInteger("iconX", iconX);
        compound.setInteger("iconY", iconY);
        compound.setInteger("iconWidth", width);
        compound.setInteger("iconHeight", height);
        compound.setString("icon", icon);
        compound.setBoolean("lossOnDeath", lossOnDeath);

//        if (saveScripts && scriptContainer != null) {
        if (saveScripts) {
            NBTTagCompound scriptData = new NBTTagCompound();
            EffectScript handler = getScriptHandler();
            if (handler != null)
                handler.writeToNBT(scriptData);
            compound.setTag("ScriptData", scriptData);
        }

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("ID"))
            id = compound.getInteger("ID");
        else
            id = CustomEffectController.Instance.getUnusedId();
        name = compound.getString("name");

        if (compound.hasKey("menuName", Constants.NBT.TAG_STRING))
            menuName = compound.getString("menuName");
        else
            menuName = name;

        length = compound.getInteger("length");
        everyXTick = compound.getInteger("everyXTick");
        iconX = compound.getInteger("iconX");
        iconY = compound.getInteger("iconY");

        if (compound.hasKey("iconWidth", Constants.NBT.TAG_INT))
            width = compound.getInteger("iconWidth");
        else
            width = 16;

        if (compound.hasKey("iconHeight", Constants.NBT.TAG_INT))
            height = compound.getInteger("iconHeight");
        else
            height = 16;

        icon = compound.getString("icon");
        lossOnDeath = compound.getBoolean("lossOnDeath");

        if (compound.hasKey("ScriptData", Constants.NBT.TAG_COMPOUND)) {
            EffectScript handler = new EffectScript();
            handler.readFromNBT(compound.getCompoundTag("ScriptData"));
            setScriptHandler(handler);
        }

    }

    public CustomEffect cloneEffect() {
        CustomEffect newEffect = new CustomEffect();
        newEffect.readFromNBT(this.writeToNBT(true));
        newEffect.id = -1;
        return newEffect;
    }

    public EffectScript getScriptHandler() {
        return CustomEffectController.getInstance().customEffectScriptHandlers.get(this.id);
    }

    public void setScriptHandler(EffectScript handler) {
        CustomEffectController.getInstance().customEffectScriptHandlers.put(this.id, handler);
    }

    public EffectScript getOrCreateScriptHandler() {
        EffectScript data = getScriptHandler();
        if (data == null)
            setScriptHandler(data = new EffectScript());
        return data;
    }

    public void runEffect(EntityPlayer player, PlayerEffect playerEffect) {
        if (player.ticksExisted % everyXTick == 0) {
            onTick(player, playerEffect);
        }
    }
}
