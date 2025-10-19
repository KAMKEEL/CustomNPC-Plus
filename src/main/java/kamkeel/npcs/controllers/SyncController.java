package kamkeel.npcs.controllers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.addon.DBCAddon;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumSyncAction;
import kamkeel.npcs.network.enums.EnumSyncType;
import kamkeel.npcs.network.packets.data.LoginPacket;
import kamkeel.npcs.network.packets.data.large.SyncEffectPacket;
import kamkeel.npcs.network.packets.data.large.SyncPacket;
import kamkeel.npcs.network.packets.request.party.PartyInfoPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.MagicController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.EffectKey;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.Magic;
import noppes.npcs.controllers.data.MagicCycle;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerEffect;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.controllers.data.RecipeCarpentry;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SyncController {

    private static final EnumSyncType[] LOGIN_SYNC_TYPES = new EnumSyncType[]{
        EnumSyncType.FACTION,
        EnumSyncType.DIALOG_CATEGORY,
        EnumSyncType.QUEST_CATEGORY,
        EnumSyncType.WORKBENCH_RECIPES,
        EnumSyncType.CARPENTRY_RECIPES,
        EnumSyncType.ANVIL_RECIPES,
        EnumSyncType.CUSTOM_EFFECTS,
        EnumSyncType.MAGIC,
        EnumSyncType.MAGIC_CYCLE
    };

    private static final EnumMap<EnumSyncType, SyncCacheEntry> CACHE_ENTRIES = new EnumMap<>(EnumSyncType.class);
    private static final ConcurrentHashMap<UUID, PlayerSyncState> PLAYER_SYNC_STATE = new ConcurrentHashMap<>();

    static {
        registerCache(EnumSyncType.FACTION, SyncController::factionsNBT);
        registerCache(EnumSyncType.DIALOG_CATEGORY, SyncController::dialogCategoriesNBT);
        registerCache(EnumSyncType.QUEST_CATEGORY, SyncController::questCategoriesNBT);
        registerCache(EnumSyncType.WORKBENCH_RECIPES, SyncController::workbenchNBT);
        registerCache(EnumSyncType.CARPENTRY_RECIPES, SyncController::carpentryNBT);
        registerCache(EnumSyncType.ANVIL_RECIPES, SyncController::anvilNBT);
        registerCache(EnumSyncType.CUSTOM_EFFECTS, SyncController::customEffectsNBT);
        registerCache(EnumSyncType.MAGIC, SyncController::magicsNBT);
        registerCache(EnumSyncType.MAGIC_CYCLE, SyncController::magicCyclesNBT);
    }

    public static void syncPlayer(EntityPlayerMP player) {

        // Login Packet
        PacketHandler.Instance.sendToPlayer(new LoginPacket(), player);

        PlayerSyncState state = PLAYER_SYNC_STATE.computeIfAbsent(player.getUniqueID(), PlayerSyncState::new);

        for (EnumSyncType type : LOGIN_SYNC_TYPES) {
            SyncCacheEntry entry = CACHE_ENTRIES.get(type);
            if (entry == null) {
                continue;
            }

            int currentRevision = entry.getRevisionValue();
            int lastRevision = state.getRevision(type);
            if (lastRevision == currentRevision) {
                continue;
            }

            CachedSyncPayload payload = entry.getPayload(type);
            if (payload == null) {
                continue;
            }

            if (lastRevision != payload.getRevision()) {
                PacketHandler.Instance.sendToPlayer(new SyncPacket(type, payload), player);
                state.updateRevision(type, payload.getRevision());
            }
        }

        DBCAddon.instance.syncPlayer(player);
        syncPlayerData(player, false);

        // Send party information to the player on login
        PartyInfoPacket.sendPartyData(player);
    }

    public static NBTTagCompound workbenchNBT() {
        RecipeController controller = RecipeController.Instance;
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for (RecipeCarpentry recipe : controller.globalRecipes.values()) {
            list.appendTag(recipe.writeNBT(false));
        }
        compound.setTag("recipes", list);
        return compound;
    }

    public static NBTTagCompound carpentryNBT() {
        RecipeController controller = RecipeController.Instance;
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for (RecipeCarpentry recipe : controller.carpentryRecipes.values()) {
            list.appendTag(recipe.writeNBT(false));
        }
        compound.setTag("recipes", list);
        return compound;
    }

    public static NBTTagCompound anvilNBT() {
        RecipeController controller = RecipeController.Instance;
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for (RecipeAnvil recipe : controller.anvilRecipes.values()) {
            list.appendTag(recipe.writeNBT(false));
        }
        compound.setTag("recipes", list);
        return compound;
    }

    public static NBTTagCompound factionsNBT() {
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for (Faction faction : FactionController.getInstance().factions.values()) {
            NBTTagCompound factioNBT = new NBTTagCompound();
            faction.writeNBT(factioNBT);
            list.appendTag(factioNBT);
        }
        compound.setTag("Factions", list);
        return compound;
    }

    public static NBTTagCompound dialogCategoriesNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList categoryList = new NBTTagList();
        for (DialogCategory category : DialogController.Instance.categories.values()) {
            NBTTagCompound questCompound = new NBTTagCompound();
            NBTTagList dialogList = new NBTTagList();
            for (int dialogID : category.dialogs.keySet()) {
                Dialog quest = category.dialogs.get(dialogID);
                dialogList.appendTag(quest.writeToNBT(new NBTTagCompound()));
            }
            questCompound.setTag("Data", dialogList);
            questCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
            categoryList.appendTag(questCompound);
        }
        compound.setTag("DialogCategories", categoryList);
        return compound;
    }

    public static NBTTagCompound questCategoriesNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList categoryList = new NBTTagList();
        for (QuestCategory category : QuestController.Instance.categories.values()) {
            NBTTagCompound questCompound = new NBTTagCompound();
            ;
            NBTTagList questList = new NBTTagList();
            for (int questID : category.quests.keySet()) {
                Quest quest = category.quests.get(questID);
                questList.appendTag(quest.writeToNBT(new NBTTagCompound()));
            }
            questCompound.setTag("Data", questList);
            questCompound.setTag("CatNBT", category.writeSmallNBT(new NBTTagCompound()));
            categoryList.appendTag(questCompound);
        }
        compound.setTag("QuestCategories", categoryList);
        return compound;
    }

    public static NBTTagCompound customEffectsNBT() {
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for (CustomEffect effect : CustomEffectController.getInstance().getCustomEffects().values()) {
            list.appendTag(effect.writeToNBT(false));
        }
        compound.setTag("Data", list);
        return compound;
    }

    public static NBTTagCompound magicsNBT() {
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for (Magic magic : MagicController.getInstance().magics.values()) {
            NBTTagCompound magicCompound = new NBTTagCompound();
            magic.writeNBT(magicCompound);
            list.appendTag(magicCompound);
        }
        compound.setTag("Data", list);
        return compound;
    }

    public static NBTTagCompound magicCyclesNBT() {
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for (MagicCycle cycle : MagicController.getInstance().cycles.values()) {
            NBTTagCompound cycleCompound = new NBTTagCompound();
            cycle.writeNBT(cycleCompound);
            list.appendTag(cycleCompound);
        }
        compound.setTag("Data", list);
        return compound;
    }

    public static void syncPlayerData(EntityPlayerMP player, boolean update) {
        PlayerData data = PlayerData.get(player);
        if (data != null) {
            if (update) {
                PacketHandler.Instance.sendToPlayer(new SyncPacket(
                        EnumSyncType.PLAYERDATA,
                        EnumSyncAction.UPDATE,
                        -1,
                        data.getSyncNBT())
                    , player);
            } else {
                PacketHandler.Instance.sendToPlayer(new SyncPacket(
                    EnumSyncType.PLAYERDATA,
                    EnumSyncAction.RELOAD,
                    -1,
                    data.getSyncNBTFull()), player);
            }
        }
    }

    public static void syncRemove(EnumSyncType enumSyncType, int id) {
        Map<EnumSyncType, Integer> revisions = invalidateCaches(enumSyncType);
        PacketHandler.Instance.sendToAll(new SyncPacket(
            enumSyncType,
            EnumSyncAction.REMOVE,
            id,
            new NBTTagCompound()
        ));
        updateAllPlayerRevisions(revisions);
    }

    public static void syncAllDialogs() {
        CachedSyncPayload payload = rebuildNow(EnumSyncType.DIALOG_CATEGORY);
        PacketHandler.Instance.sendToAll(new SyncPacket(EnumSyncType.DIALOG_CATEGORY, payload));
        updateAllPlayerRevisions(EnumSyncType.DIALOG_CATEGORY, payload.getRevision());
    }

    public static void syncAllQuests() {
        CachedSyncPayload payload = rebuildNow(EnumSyncType.QUEST_CATEGORY);
        PacketHandler.Instance.sendToAll(new SyncPacket(EnumSyncType.QUEST_CATEGORY, payload));
        updateAllPlayerRevisions(EnumSyncType.QUEST_CATEGORY, payload.getRevision());
    }

    public static void syncAllWorkbenchRecipes() {
        CachedSyncPayload payload = rebuildNow(EnumSyncType.WORKBENCH_RECIPES);
        PacketHandler.Instance.sendToAll(new SyncPacket(EnumSyncType.WORKBENCH_RECIPES, payload));
        updateAllPlayerRevisions(EnumSyncType.WORKBENCH_RECIPES, payload.getRevision());
    }

    public static void syncAllCarpentryRecipes() {
        CachedSyncPayload payload = rebuildNow(EnumSyncType.CARPENTRY_RECIPES);
        PacketHandler.Instance.sendToAll(new SyncPacket(EnumSyncType.CARPENTRY_RECIPES, payload));
        updateAllPlayerRevisions(EnumSyncType.CARPENTRY_RECIPES, payload.getRevision());
    }

    public static void syncAllAnvilRecipes() {
        CachedSyncPayload payload = rebuildNow(EnumSyncType.ANVIL_RECIPES);
        PacketHandler.Instance.sendToAll(new SyncPacket(EnumSyncType.ANVIL_RECIPES, payload));
        updateAllPlayerRevisions(EnumSyncType.ANVIL_RECIPES, payload.getRevision());
    }

    public static void syncAllCustomEffects() {
        CachedSyncPayload payload = rebuildNow(EnumSyncType.CUSTOM_EFFECTS);
        PacketHandler.Instance.sendToAll(new SyncPacket(EnumSyncType.CUSTOM_EFFECTS, payload));
        updateAllPlayerRevisions(EnumSyncType.CUSTOM_EFFECTS, payload.getRevision());
    }

    @SideOnly(Side.CLIENT)
    public static void clientSync(EnumSyncType enumSyncType, NBTTagCompound fullCompound) {
        switch (enumSyncType) {
            case FACTION: {
                NBTTagList list = fullCompound.getTagList("Factions", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    Faction faction = new Faction();
                    faction.readNBT(list.getCompoundTagAt(i));
                    FactionController.getInstance().factionsSync.put(faction.id, faction);
                }
                FactionController.getInstance().factions = FactionController.getInstance().factionsSync;
                FactionController.getInstance().factionsSync = new HashMap<Integer, Faction>();
                break;
            }
            case DIALOG_CATEGORY: {
                if (!fullCompound.hasNoTags()) {
                    NBTTagList categories = fullCompound.getTagList("DialogCategories", 10);
                    for (int j = 0; j < fullCompound.getTagList("DialogCategories", 10).tagCount(); j++) {
                        NBTTagCompound categoryCompound = categories.getCompoundTagAt(j);
                        if (categoryCompound.hasNoTags())
                            continue;

                        DialogCategory category = new DialogCategory();
                        category.readSmallNBT(categoryCompound.getCompoundTag("CatNBT"));
                        NBTTagList dialogList = categoryCompound.getTagList("Data", 10);
                        if (DialogController.Instance.categoriesSync.containsKey(category.id)) {
                            category = DialogController.Instance.categoriesSync.get(category.id);
                            category.readSmallNBT(categoryCompound.getCompoundTag("CatNBT"));
                        }
                        for (int i = 0; i < dialogList.tagCount(); i++) {
                            Dialog dialog = new Dialog();
                            dialog.readNBT(dialogList.getCompoundTagAt(i));
                            dialog.category = category;
                            category.dialogs.put(dialog.id, dialog);
                        }
                        DialogController.Instance.categoriesSync.put(category.id, category);
                    }
                }

                HashMap<Integer, Dialog> dialogs = new HashMap<Integer, Dialog>();
                for (DialogCategory category : DialogController.Instance.categoriesSync.values()) {
                    for (Dialog dialog : category.dialogs.values()) {
                        dialogs.put(dialog.id, dialog);
                    }
                }

                DialogController.Instance.categories = DialogController.Instance.categoriesSync;
                DialogController.Instance.dialogs = dialogs;
                DialogController.Instance.categoriesSync = new HashMap<Integer, DialogCategory>();
                break;
            }
            case QUEST_CATEGORY: {
                if (!fullCompound.hasNoTags()) {
                    NBTTagList categories = fullCompound.getTagList("QuestCategories", 10);
                    for (int j = 0; j < fullCompound.getTagList("QuestCategories", 10).tagCount(); j++) {
                        NBTTagCompound categoryCompound = categories.getCompoundTagAt(j);
                        if (categoryCompound.hasNoTags())
                            continue;

                        QuestCategory category = new QuestCategory();
                        category.readSmallNBT(categoryCompound.getCompoundTag("CatNBT"));
                        NBTTagList questList = categoryCompound.getTagList("Data", 10);
                        if (QuestController.Instance.categoriesSync.containsKey(category.id)) {
                            category = QuestController.Instance.categoriesSync.get(category.id);
                            category.readSmallNBT(categoryCompound.getCompoundTag("CatNBT"));
                        }
                        for (int i = 0; i < questList.tagCount(); i++) {
                            Quest quest = new Quest();
                            quest.readNBT(questList.getCompoundTagAt(i));
                            quest.category = category;
                            category.quests.put(quest.id, quest);
                        }
                        QuestController.Instance.categoriesSync.put(category.id, category);
                    }
                }

                HashMap<Integer, Quest> quests = new HashMap<Integer, Quest>();
                for (QuestCategory category : QuestController.Instance.categoriesSync.values()) {
                    for (Quest quest : category.quests.values()) {
                        quests.put(quest.id, quest);
                    }
                }

                QuestController.Instance.categories = QuestController.Instance.categoriesSync;
                QuestController.Instance.quests = quests;
                QuestController.Instance.categoriesSync = new HashMap<Integer, QuestCategory>();
                break;
            }
            case PLAYERDATA: {
                ClientCacheHandler.playerData.setSyncNBTFull(fullCompound);
                break;
            }
            case MAGIC: {
                NBTTagList list = fullCompound.getTagList("Data", 10);
                MagicController mc = MagicController.getInstance();
                mc.magicSync.clear();
                for (int i = 0; i < list.tagCount(); i++) {
                    Magic magic = new Magic();
                    magic.readNBT(list.getCompoundTagAt(i));
                    mc.magicSync.put(magic.id, magic);
                }

                mc.magics.clear();
                mc.magics.putAll(mc.magicSync);
                mc.magicSync.clear();
                break;
            }
            case MAGIC_CYCLE: {
                NBTTagList list = fullCompound.getTagList("Data", 10);
                MagicController mc = MagicController.getInstance();
                mc.cyclesSync.clear();
                for (int i = 0; i < list.tagCount(); i++) {
                    MagicCycle cycle = new MagicCycle();
                    cycle.readNBT(list.getCompoundTagAt(i));
                    mc.cyclesSync.put(cycle.id, cycle);
                }

                mc.cycles.clear();
                mc.cycles.putAll(mc.cyclesSync);
                mc.cyclesSync.clear();
                break;
            }
            case WORKBENCH_RECIPES: {
                NBTTagList list = fullCompound.getTagList("recipes", 10);
                if (list == null)
                    return;

                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound recipeCompound = list.getCompoundTagAt(i);
                    RecipeCarpentry recipe = RecipeCarpentry.create(recipeCompound);
                    recipe.readNBT(recipeCompound);
                    RecipeController.syncRecipes.put(recipe.id, recipe);
                }

                RecipeController.reloadGlobalRecipes(RecipeController.syncRecipes);
                RecipeController.syncRecipes = new HashMap<Integer, RecipeCarpentry>();
                break;
            }
            case CARPENTRY_RECIPES: {
                NBTTagList list = fullCompound.getTagList("recipes", 10);
                if (list == null)
                    return;

                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound recipeCompound = list.getCompoundTagAt(i);
                    RecipeCarpentry recipe = RecipeCarpentry.create(recipeCompound);
                    recipe.readNBT(recipeCompound);
                    RecipeController.syncRecipes.put(recipe.id, recipe);
                }

                RecipeController.Instance.carpentryRecipes = RecipeController.syncRecipes;
                RecipeController.syncRecipes = new HashMap<Integer, RecipeCarpentry>();
                break;
            }
            case ANVIL_RECIPES: {
                NBTTagList list = fullCompound.getTagList("recipes", 10);
                if (list == null)
                    return;

                for (int i = 0; i < list.tagCount(); i++) {
                    RecipeAnvil recipe = new RecipeAnvil();
                    recipe.readNBT(list.getCompoundTagAt(i));
                    RecipeController.syncAnvilRecipes.put(recipe.id, recipe);
                }

                RecipeController.Instance.anvilRecipes = RecipeController.syncAnvilRecipes;
                RecipeController.syncAnvilRecipes = new HashMap<Integer, RecipeAnvil>();
                break;
            }
            case CUSTOM_EFFECTS: {
                NBTTagList list = fullCompound.getTagList("Data", 10);
                CustomEffectController ce = CustomEffectController.getInstance();
                ce.customEffectsSync.clear();
                for (int i = 0; i < list.tagCount(); i++) {
                    CustomEffect effect = new CustomEffect();
                    effect.readFromNBT(list.getCompoundTagAt(i));
                    ClientCacheHandler.getImageData(effect.icon);
                    ce.customEffectsSync.put(effect.id, effect);
                }

                ce.getCustomEffects().clear();
                ce.getCustomEffects().putAll(ce.customEffectsSync);
                ce.customEffectsSync.clear();
                break;
            }
        }
    }

    /**
     * Update Related Tasks
     */
    @SideOnly(Side.CLIENT)
    public static void clientUpdate(EnumSyncType enumSyncType, int category_id, NBTTagCompound compound) {
        switch (enumSyncType) {
            case FACTION: {
                Faction faction = new Faction();
                faction.readNBT(compound);
                FactionController.getInstance().factions.put(faction.id, faction);
                break;
            }
            case DIALOG: {
                DialogCategory category = DialogController.Instance.categories.get(category_id);
                Dialog dialog = new Dialog();
                dialog.category = category;
                dialog.readNBT(compound);
                DialogController.Instance.dialogs.put(dialog.id, dialog);
                category.dialogs.put(dialog.id, dialog);
                break;
            }
            case QUEST: {
                QuestCategory category = QuestController.Instance.categories.get(category_id);
                Quest quest = new Quest();
                quest.category = category;
                quest.readNBT(compound);
                QuestController.Instance.quests.put(quest.id, quest);
                category.quests.put(quest.id, quest);
                break;
            }
            case QUEST_CATEGORY: {
                QuestCategory category = new QuestCategory();
                category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                NBTTagList list = compound.getTagList("Data", 10);
                if (QuestController.Instance.categoriesSync.containsKey(category.id)) {
                    category = QuestController.Instance.categoriesSync.get(category.id);
                    category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                }
                for (int i = 0; i < list.tagCount(); i++) {
                    Quest quest = new Quest();
                    quest.readNBT(list.getCompoundTagAt(i));
                    quest.category = category;
                    category.quests.put(quest.id, quest);
                }
                QuestController.Instance.categories.put(category.id, category);
                break;
            }
            case DIALOG_CATEGORY: {
                DialogCategory category = new DialogCategory();
                category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                NBTTagList list = compound.getTagList("Data", 10);
                if (DialogController.Instance.categoriesSync.containsKey(category.id)) {
                    category = DialogController.Instance.categoriesSync.get(category.id);
                    category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                }
                for (int i = 0; i < list.tagCount(); i++) {
                    Dialog dialog = new Dialog();
                    dialog.readNBT(list.getCompoundTagAt(i));
                    dialog.category = category;
                    category.dialogs.put(dialog.id, dialog);
                }
                DialogController.Instance.categories.put(category.id, category);
                break;
            }
            case MAGIC_CYCLE: {
                MagicCycle cycle = new MagicCycle();
                cycle.readNBT(compound);
                MagicController.getInstance().cycles.put(cycle.id, cycle);
                break;
            }
            case MAGIC: {
                Magic magic = new Magic();
                magic.readNBT(compound);
                MagicController.getInstance().magics.put(magic.id, magic);
                break;
            }
            case PLAYERDATA: {
                ClientCacheHandler.playerData.setSyncNBT(compound);
                break;
            }
            case CUSTOM_EFFECTS: {
                CustomEffect effect = new CustomEffect();
                effect.readFromNBT(compound);
                ClientCacheHandler.getImageData(effect.icon);

                CustomEffectController.Instance.getCustomEffects().put(effect.id, effect);
                break;
            }
        }
    }

    public static void syncUpdate(EnumSyncType type, int cat, NBTTagCompound compound) {
        Map<EnumSyncType, Integer> revisions = invalidateCaches(type);
        PacketHandler.Instance.sendToAll(new SyncPacket(
            type,
            EnumSyncAction.UPDATE,
            cat,
            compound
        ));
        updateAllPlayerRevisions(revisions);
    }


    public static NBTTagCompound updateQuestCat(QuestCategory questCategory) {
        NBTTagCompound questCompound = new NBTTagCompound();
        NBTTagList questList = new NBTTagList();
        for (int questID : questCategory.quests.keySet()) {
            Quest quest = questCategory.quests.get(questID);
            questList.appendTag(quest.writeToNBT(new NBTTagCompound()));
        }
        questCompound.setTag("Data", questList);
        questCompound.setTag("CatNBT", questCategory.writeSmallNBT(new NBTTagCompound()));
        return questCompound;
    }

    public static NBTTagCompound updateDialogCat(DialogCategory dialogCategory) {
        NBTTagCompound dialogCompound = new NBTTagCompound();
        NBTTagList dialogList = new NBTTagList();
        for (int questID : dialogCategory.dialogs.keySet()) {
            Dialog dialog = dialogCategory.dialogs.get(questID);
            dialogList.appendTag(dialog.writeToNBT(new NBTTagCompound()));
        }
        dialogCompound.setTag("Data", dialogList);
        dialogCompound.setTag("CatNBT", dialogCategory.writeSmallNBT(new NBTTagCompound()));
        return dialogCompound;
    }

    @SideOnly(Side.CLIENT)
    public static void clientSyncRemove(EnumSyncType enumSyncType, int id) {
        switch (enumSyncType) {
            case FACTION:
                FactionController.getInstance().factions.remove(id);
                break;
            case DIALOG:
                Dialog dialog = DialogController.Instance.dialogs.remove(id);
                if (dialog != null) {
                    dialog.category.dialogs.remove(id);
                }
                break;
            case DIALOG_CATEGORY:
                DialogCategory dialogCategory = DialogController.Instance.categories.remove(id);
                if (dialogCategory != null) {
                    DialogController.Instance.dialogs.keySet().removeAll(dialogCategory.dialogs.keySet());
                }
                break;
            case QUEST:
                Quest quest = QuestController.Instance.quests.remove(id);
                if (quest != null) {
                    quest.category.quests.remove(id);
                }
                break;
            case QUEST_CATEGORY:
                QuestCategory questCategory = QuestController.Instance.categories.remove(id);
                if (questCategory != null) {
                    QuestController.Instance.quests.keySet().removeAll(questCategory.quests.keySet());
                }
                break;
            case MAGIC:
                for (MagicCycle cycle : MagicController.getInstance().cycles.values()) {
                    cycle.associations.remove(id);
                }
                MagicController.getInstance().cycles.remove(id);
                break;
            case MAGIC_CYCLE:
                MagicController.getInstance().cycles.remove(id);
                break;
            case CUSTOM_EFFECTS:
                CustomEffectController.Instance.getCustomEffects().remove(id);
                break;
        }
    }

    public static void syncEffects(EntityPlayerMP playerMP) {
        ConcurrentHashMap<EffectKey, PlayerEffect> playerEffects = CustomEffectController.getInstance().getPlayerEffects(playerMP);
        PlayerData playerData = PlayerData.get(playerMP);
        playerData.effectData.setEffects(playerEffects);

        NBTTagCompound compound = playerData.getPlayerEffects();
        PacketHandler.Instance.sendToPlayer(new SyncEffectPacket(compound), playerMP);
    }

    @SideOnly(Side.CLIENT)
    public static void clientSyncEffects(NBTTagCompound compound) {
        PlayerData playerData = PlayerData.get(Minecraft.getMinecraft().thePlayer);
        if (playerData != null) {
            playerData.setPlayerEffects(compound);
        }
    }

    private static void registerCache(EnumSyncType type, Supplier<NBTTagCompound> supplier) {
        CACHE_ENTRIES.put(type, new SyncCacheEntry(type, supplier));
    }

    private static CachedSyncPayload rebuildNow(EnumSyncType type) {
        SyncCacheEntry entry = CACHE_ENTRIES.get(type);
        if (entry == null) {
            return null;
        }
        entry.invalidate();
        return entry.getPayload(type);
    }

    public static Map<EnumSyncType, Integer> invalidateCaches(EnumSyncType type) {
        EnumSet<EnumSyncType> targets = getInvalidationTargets(type);
        EnumMap<EnumSyncType, Integer> revisions = new EnumMap<>(EnumSyncType.class);
        for (EnumSyncType target : targets) {
            SyncCacheEntry entry = CACHE_ENTRIES.get(target);
            if (entry != null) {
                revisions.put(target, entry.invalidate());
            }
        }
        return revisions;
    }

    private static EnumSet<EnumSyncType> getInvalidationTargets(EnumSyncType type) {
        switch (type) {
            case DIALOG:
            case DIALOG_CATEGORY:
                return EnumSet.of(EnumSyncType.DIALOG_CATEGORY);
            case QUEST:
            case QUEST_CATEGORY:
                return EnumSet.of(EnumSyncType.QUEST_CATEGORY);
            case FACTION:
                return EnumSet.of(EnumSyncType.FACTION);
            case MAGIC:
                return EnumSet.of(EnumSyncType.MAGIC);
            case MAGIC_CYCLE:
                return EnumSet.of(EnumSyncType.MAGIC_CYCLE);
            case WORKBENCH_RECIPES:
                return EnumSet.of(EnumSyncType.WORKBENCH_RECIPES);
            case CARPENTRY_RECIPES:
                return EnumSet.of(EnumSyncType.CARPENTRY_RECIPES);
            case ANVIL_RECIPES:
                return EnumSet.of(EnumSyncType.ANVIL_RECIPES);
            case CUSTOM_EFFECTS:
                return EnumSet.of(EnumSyncType.CUSTOM_EFFECTS);
            default:
                return EnumSet.noneOf(EnumSyncType.class);
        }
    }

    private static void updateAllPlayerRevisions(EnumSyncType type, int revision) {
        if (revision < 0) {
            return;
        }
        for (PlayerSyncState state : PLAYER_SYNC_STATE.values()) {
            state.updateRevision(type, revision);
        }
    }

    private static void updateAllPlayerRevisions(Map<EnumSyncType, Integer> revisions) {
        if (revisions.isEmpty()) {
            return;
        }
        for (PlayerSyncState state : PLAYER_SYNC_STATE.values()) {
            for (Map.Entry<EnumSyncType, Integer> entry : revisions.entrySet()) {
                state.updateRevision(entry.getKey(), entry.getValue());
            }
        }
    }

    public static final class CachedSyncPayload {
        private final int revision;
        private final byte[] payload;
        private final byte[][] chunks;

        private CachedSyncPayload(int revision, byte[] payload, byte[][] chunks) {
            this.revision = revision;
            this.payload = payload;
            this.chunks = chunks;
        }

        public int getRevision() {
            return revision;
        }

        public byte[] getPayload() {
            return payload;
        }

        public byte[][] getChunks() {
            return chunks;
        }
    }

    private static final class SyncCacheEntry {
        private final Supplier<NBTTagCompound> supplier;
        private volatile CachedSyncPayload payload;
        private int revision = 0;
        private boolean dirty = true;

        private SyncCacheEntry(EnumSyncType type, Supplier<NBTTagCompound> supplier) {
            this.supplier = supplier;
        }

        private synchronized CachedSyncPayload getPayload(EnumSyncType requestedType) {
            if (!dirty && payload != null) {
                return payload;
            }

            NBTTagCompound data = supplier.get();
            ByteBuf buffer = Unpooled.buffer();
            byte[] bytes;
            try {
                buffer.writeInt(requestedType.ordinal());
                buffer.writeInt(EnumSyncAction.RELOAD.ordinal());
                buffer.writeInt(-1);
                ByteBufUtils.writeBigNBT(buffer, data);

                bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
            } finally {
                buffer.release();
            }

            byte[][] chunks = splitIntoChunks(bytes);
            payload = new CachedSyncPayload(revision, bytes, chunks);
            dirty = false;
            return payload;
        }

        private synchronized int invalidate() {
            dirty = true;
            payload = null;
            revision++;
            return revision;
        }

        private synchronized int getRevisionValue() {
            return revision;
        }

        private static byte[][] splitIntoChunks(byte[] payload) {
            int totalSize = payload.length;
            int chunkCount = (totalSize + LargeAbstractPacket.CHUNK_SIZE - 1) / LargeAbstractPacket.CHUNK_SIZE;
            if (chunkCount <= 0) {
                chunkCount = 1;
            }
            byte[][] chunks = new byte[chunkCount][];
            for (int i = 0; i < chunkCount; i++) {
                int offset = i * LargeAbstractPacket.CHUNK_SIZE;
                int length = Math.min(LargeAbstractPacket.CHUNK_SIZE, totalSize - offset);
                byte[] chunk = new byte[length];
                System.arraycopy(payload, offset, chunk, 0, length);
                chunks[i] = chunk;
            }
            return chunks;
        }
    }

    private static final class PlayerSyncState {
        private final EnumMap<EnumSyncType, Integer> revisions = new EnumMap<>(EnumSyncType.class);

        private PlayerSyncState(UUID ignored) {
        }

        private synchronized int getRevision(EnumSyncType type) {
            Integer value = revisions.get(type);
            return value == null ? -1 : value;
        }

        private synchronized void updateRevision(EnumSyncType type, int revision) {
            revisions.put(type, revision);
        }
    }
}
