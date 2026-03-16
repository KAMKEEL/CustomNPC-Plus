package noppes.npcs.controllers.data;


import java.util.function.Consumer;
import java.util.HashSet;
import java.util.UUID;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ICustomEffect;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.TagController;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;

public class CustomEffect implements ICustomEffect {
    public int id = -1;

    public String name = "";
    public boolean lossOnDeath = true;
    public int length = 30;

    // Must be a multiple of 10
    public int everyXTick = 20;

    public String icon = "";
    public int iconX = 0, iconY = 0;

    public String menuName = "§aNEW EFFECT";
    public int width = 16, height = 16;

    // Animation
    public boolean animated = false;
    public int frameCount = 1;
    public int frametime = 2;

    public int index = 0;

    public HashSet<UUID> tagUUIDs = new HashSet<>();

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
    public boolean isAnimated() {
        return animated;
    }

    @Override
    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    @Override
    public int getFrameCount() {
        return frameCount;
    }

    @Override
    public void setFrameCount(int frameCount) {
        this.frameCount = Math.max(1, frameCount);
    }

    @Override
    public int getFrameTime() {
        return frametime;
    }

    @Override
    public void setFrameTime(int frametime) {
        this.frametime = Math.max(1, frametime);
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

    public void onAdded(IPlayer player, PlayerEffect playerEffect) {
        IPlayer iPlayer = NoppesUtilServer.getIPlayer(player);
        if (playerEffect.index == 0) {
            PlayerEvent.EffectEvent.Added event = new PlayerEvent.EffectEvent.Added(iPlayer, playerEffect);
            EffectScript script = getScriptHandler();
            if (script == null) {
                return;
            }

            script.callScript(EnumScriptType.ON_EFFECT_ADD, event);
        }

        EventHooks.onEffectAdded(iPlayer, playerEffect);
    }

    public void onTick(IPlayer player, PlayerEffect playerEffect) {
        IPlayer iPlayer = NoppesUtilServer.getIPlayer(player);
        if (playerEffect.index == 0) {
            PlayerEvent.EffectEvent.Ticked event = new PlayerEvent.EffectEvent.Ticked(iPlayer, playerEffect);
            EffectScript script = getScriptHandler();
            if (script == null) {
                return;
            }

            script.callScript(EnumScriptType.ON_EFFECT_TICK, event);
        }

        EventHooks.onEffectTick(iPlayer, playerEffect);
    }

    public void onRemoved(IPlayer player, PlayerEffect playerEffect, PlayerEvent.EffectEvent.ExpirationType type) {
        IPlayer iPlayer = NoppesUtilServer.getIPlayer(player);

        if (playerEffect.index == 0) {
            PlayerEvent.EffectEvent.Removed event = new PlayerEvent.EffectEvent.Removed(iPlayer, playerEffect, type);
            EffectScript script = getScriptHandler();
            if (script == null) {
                return;
            }

            script.callScript(EnumScriptType.ON_EFFECT_REMOVE, event);
        }

        EventHooks.onEffectRemove(iPlayer, playerEffect, type);
    }

    public INbt writeToNBT(boolean saveScripts) {
        INbt compound = new INbt();
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
        compound.setBoolean("iconAnimated", animated);
        compound.setInteger("iconFrameCount", frameCount);
        compound.setInteger("iconFrameTime", frametime);

        TagController.writeTagUUIDs(compound, "TagUUIDs", tagUUIDs);

        if (saveScripts) {
            INbt scriptData = new INbt();
            EffectScript handler = getScriptHandler();
            if (handler != null)
                handler.writeToNBT(scriptData);
            compound.setTag("ScriptData", scriptData);
        }

        return compound;
    }

    public void readFromNBT(INbt compound) {
        if (compound.hasKey("ID"))
            id = compound.getInteger("ID");
        else
            id = CustomEffectController.Instance.getUnusedId();
        name = compound.getString("name");

        if (compound.hasKey("menuName", NbtConstants.TAG_STRING))
            menuName = compound.getString("menuName");
        else
            menuName = name;

        length = compound.getInteger("length");
        everyXTick = compound.getInteger("everyXTick");
        iconX = compound.getInteger("iconX");
        iconY = compound.getInteger("iconY");

        if (compound.hasKey("iconWidth", NbtConstants.TAG_INT))
            width = compound.getInteger("iconWidth");
        else
            width = 16;

        if (compound.hasKey("iconHeight", NbtConstants.TAG_INT))
            height = compound.getInteger("iconHeight");
        else
            height = 16;

        icon = compound.getString("icon");
        lossOnDeath = compound.getBoolean("lossOnDeath");

        if (compound.hasKey("iconAnimated"))
            animated = compound.getBoolean("iconAnimated");
        if (compound.hasKey("iconFrameCount"))
            frameCount = Math.max(1, compound.getInteger("iconFrameCount"));
        if (compound.hasKey("iconFrameTime"))
            frametime = Math.max(1, compound.getInteger("iconFrameTime"));

        tagUUIDs = TagController.readTagUUIDs(compound, "TagUUIDs");

        if (compound.hasKey("ScriptData", NbtConstants.TAG_COMPOUND)) {
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

    public void runEffect(IPlayer player, PlayerEffect playerEffect) {
        if (player.ticksExisted % everyXTick == 0) {
            onTick(player, playerEffect);
        }
    }
}
