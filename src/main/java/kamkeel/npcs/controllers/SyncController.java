package kamkeel.npcs.controllers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.addon.DBCAddon;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumSyncAction;
import kamkeel.npcs.network.enums.EnumSyncType;
import kamkeel.npcs.network.packets.data.LoginPacket;
import kamkeel.npcs.network.packets.data.large.SyncEffectPacket;
import kamkeel.npcs.network.packets.data.large.SyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SyncController {

    public static void syncPlayer(EntityPlayerMP player){

        // Login Packet
        PacketHandler.Instance.sendToPlayer(new LoginPacket(), player);

        // 1) Factions
        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.FACTION,
            EnumSyncAction.RELOAD,
            -1,
            factionsNBT()
        ), player);

        // 2) Dialog Categories
        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.DIALOG_CATEGORY,
            EnumSyncAction.RELOAD,
            -1,
            dialogCategoriesNBT()
        ), player);

        // 3) Quest Categories
        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.QUEST_CATEGORY,
            EnumSyncAction.RELOAD,
            -1,
            questCategoriesNBT()
        ), player);

        // 4) Workbench Recipes
        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.WORKBENCH_RECIPES,
            EnumSyncAction.RELOAD,
            -1,
            workbenchNBT()
        ), player);

        // 5) Carpentry Recipes
        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.CARPENTRY_RECIPES,
            EnumSyncAction.RELOAD,
            -1,
            carpentryNBT()
        ), player);

        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.ANVIL_RECIPES,
            EnumSyncAction.RELOAD,
            -1,
            anvilNBT()
        ), player);

        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.CUSTOM_EFFECTS,
            EnumSyncAction.RELOAD,
            -1,
            customEffectsNBT()
        ), player);

        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.MAGIC,
            EnumSyncAction.RELOAD,
            -1,
            magicsNBT()
        ), player);

        PacketHandler.Instance.sendToPlayer(new SyncPacket(
            EnumSyncType.MAGIC_CYCLE,
            EnumSyncAction.RELOAD,
            -1,
            magicCyclesNBT()
        ), player);

        DBCAddon.instance.syncPlayer(player);
        syncPlayerData(player, false);
    }

    public static NBTTagCompound workbenchNBT(){
        RecipeController controller = RecipeController.Instance;
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for(RecipeCarpentry recipe : controller.globalRecipes.values()){
            list.appendTag(recipe.writeNBT());
        }
        compound.setTag("recipes", list);
        return compound;
    }

    public static NBTTagCompound carpentryNBT(){
        RecipeController controller = RecipeController.Instance;
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for(RecipeCarpentry recipe : controller.carpentryRecipes.values()){
            list.appendTag(recipe.writeNBT());
        }
        compound.setTag("recipes", list);
        return compound;
    }

    public static NBTTagCompound anvilNBT(){
        RecipeController controller = RecipeController.Instance;
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for(RecipeAnvil recipe : controller.anvilRecipes.values()){
            list.appendTag(recipe.writeNBT());
        }
        compound.setTag("recipes", list);
        return compound;
    }

    public static NBTTagCompound factionsNBT(){
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for(Faction faction : FactionController.getInstance().factions.values()){
            NBTTagCompound factioNBT = new NBTTagCompound();
            faction.writeNBT(factioNBT);
            list.appendTag(factioNBT);
        }
        compound.setTag("Factions", list);
        return compound;
    }

    public static NBTTagCompound dialogCategoriesNBT(){
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList categoryList = new NBTTagList();
        for(DialogCategory category : DialogController.Instance.categories.values()){
            NBTTagCompound questCompound = new NBTTagCompound();
            NBTTagList dialogList = new NBTTagList();
            for(int dialogID : category.dialogs.keySet()){
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

    public static NBTTagCompound questCategoriesNBT(){
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList categoryList = new NBTTagList();
        for(QuestCategory category : QuestController.Instance.categories.values()){
            NBTTagCompound questCompound = new NBTTagCompound();;
            NBTTagList questList = new NBTTagList();
            for(int questID : category.quests.keySet()){
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

    public static NBTTagCompound customEffectsNBT(){
        NBTTagList list = new NBTTagList();
        NBTTagCompound compound = new NBTTagCompound();
        for (CustomEffect effect : CustomEffectController.getInstance().getCustomEffects().values()) {
            list.appendTag(effect.writeToNBT(false));
        }
        compound.setTag("Data", list);
        return compound;
    }

    public static NBTTagCompound magicsNBT(){
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

    public static NBTTagCompound magicCyclesNBT(){
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

    public static void syncPlayerData(EntityPlayerMP player, boolean update){
        PlayerData data = PlayerData.get(player);
        if(data != null){
            if(update){
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

    public static void syncRemove(EnumSyncType enumSyncType, int id){
        PacketHandler.Instance.sendToAll(new SyncPacket(
            enumSyncType,
            EnumSyncAction.REMOVE,
            id,
            new NBTTagCompound()
        ));
    }

    public static void syncAllDialogs() {
        PacketHandler.Instance.sendToAll(new SyncPacket(
            EnumSyncType.DIALOG_CATEGORY,
            EnumSyncAction.RELOAD,
            -1,
            dialogCategoriesNBT()
        ));
    }

    public static void syncAllQuests() {
        PacketHandler.Instance.sendToAll(new SyncPacket(
            EnumSyncType.QUEST_CATEGORY,
            EnumSyncAction.RELOAD,
            -1,
            questCategoriesNBT()
        ));
    }

    public static void syncAllWorkbenchRecipes() {
        PacketHandler.Instance.sendToAll(new SyncPacket(
            EnumSyncType.WORKBENCH_RECIPES,
            EnumSyncAction.RELOAD,
            -1,
            workbenchNBT()
        ));
    }

    public static void syncAllCarpentryRecipes() {
        PacketHandler.Instance.sendToAll(new SyncPacket(
            EnumSyncType.CARPENTRY_RECIPES,
            EnumSyncAction.RELOAD,
            -1,
            carpentryNBT()
        ));
    }

    public static void syncAllAnvilRecipes() {
        PacketHandler.Instance.sendToAll(new SyncPacket(
            EnumSyncType.ANVIL_RECIPES,
            EnumSyncAction.RELOAD,
            -1,
            anvilNBT()
        ));
    }

    public static void syncAllCustomEffects() {
        PacketHandler.Instance.sendToAll(new SyncPacket(
            EnumSyncType.CUSTOM_EFFECTS,
            EnumSyncAction.RELOAD,
            -1,
            customEffectsNBT()
        ));
    }

    @SideOnly(Side.CLIENT)
    public static void clientSync(EnumSyncType enumSyncType, NBTTagCompound fullCompound) {
        switch (enumSyncType){
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
                if(!fullCompound.hasNoTags()){
                    NBTTagList categories = fullCompound.getTagList("DialogCategories", 10);
                    for(int j = 0; j < fullCompound.getTagList("DialogCategories", 10).tagCount(); j++) {
                        NBTTagCompound categoryCompound = categories.getCompoundTagAt(j);
                        if(categoryCompound.hasNoTags())
                            continue;

                        DialogCategory category = new DialogCategory();
                        category.readSmallNBT(categoryCompound.getCompoundTag("CatNBT"));
                        NBTTagList dialogList = categoryCompound.getTagList("Data", 10);
                        if(DialogController.Instance.categoriesSync.containsKey(category.id)){
                            category = DialogController.Instance.categoriesSync.get(category.id);
                            category.readSmallNBT(categoryCompound.getCompoundTag("CatNBT"));
                        }
                        for(int i = 0; i < dialogList.tagCount(); i++)
                        {
                            Dialog dialog = new Dialog();
                            dialog.readNBT(dialogList.getCompoundTagAt(i));
                            dialog.category = category;
                            category.dialogs.put(dialog.id, dialog);
                        }
                        DialogController.Instance.categoriesSync.put(category.id, category);
                    }
                }

                HashMap<Integer, Dialog> dialogs = new HashMap<Integer, Dialog>();
                for(DialogCategory category : DialogController.Instance.categoriesSync.values()){
                    for(Dialog dialog : category.dialogs.values()){
                        dialogs.put(dialog.id, dialog);
                    }
                }

                DialogController.Instance.categories = DialogController.Instance.categoriesSync;
                DialogController.Instance.dialogs = dialogs;
                DialogController.Instance.categoriesSync = new HashMap<Integer, DialogCategory>();
                break;
            }
            case QUEST_CATEGORY: {
                if(!fullCompound.hasNoTags()){
                    NBTTagList categories = fullCompound.getTagList("QuestCategories", 10);
                    for(int j = 0; j < fullCompound.getTagList("QuestCategories", 10).tagCount(); j++) {
                        NBTTagCompound categoryCompound = categories.getCompoundTagAt(j);
                        if(categoryCompound.hasNoTags())
                            continue;

                        QuestCategory category = new QuestCategory();
                        category.readSmallNBT(categoryCompound.getCompoundTag("CatNBT"));
                        NBTTagList questList = categoryCompound.getTagList("Data", 10);
                        if(QuestController.Instance.categoriesSync.containsKey(category.id)){
                            category = QuestController.Instance.categoriesSync.get(category.id);
                            category.readSmallNBT(categoryCompound.getCompoundTag("CatNBT"));
                        }
                        for(int i = 0; i < questList.tagCount(); i++)
                        {
                            Quest quest = new Quest();
                            quest.readNBT(questList.getCompoundTagAt(i));
                            quest.category = category;
                            category.quests.put(quest.id, quest);
                        }
                        QuestController.Instance.categoriesSync.put(category.id, category);
                    }
                }

                HashMap<Integer,Quest> quests = new HashMap<Integer, Quest>();
                for(QuestCategory category : QuestController.Instance.categoriesSync.values()){
                    for(Quest quest : category.quests.values()){
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
                if(list == null)
                    return;

                for(int i = 0; i < list.tagCount(); i++)
                {
                    RecipeCarpentry recipe = RecipeCarpentry.read(list.getCompoundTagAt(i));
                    RecipeController.syncRecipes.put(recipe.id,recipe);
                }

                RecipeController.reloadGlobalRecipes(RecipeController.syncRecipes);
                RecipeController.syncRecipes = new HashMap<Integer, RecipeCarpentry>();
                break;
            }
            case CARPENTRY_RECIPES: {
                NBTTagList list = fullCompound.getTagList("recipes", 10);
                if(list == null)
                    return;

                for(int i = 0; i < list.tagCount(); i++)
                {
                    RecipeCarpentry recipe = RecipeCarpentry.read(list.getCompoundTagAt(i));
                    RecipeController.syncRecipes.put(recipe.id,recipe);
                }

                RecipeController.Instance.carpentryRecipes = RecipeController.syncRecipes;
                RecipeController.syncRecipes = new HashMap<Integer, RecipeCarpentry>();
                break;
            }
            case ANVIL_RECIPES: {
                NBTTagList list = fullCompound.getTagList("recipes", 10);
                if(list == null)
                    return;

                for(int i = 0; i < list.tagCount(); i++)
                {
                    RecipeAnvil recipe = RecipeAnvil.read(list.getCompoundTagAt(i));
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
        switch (enumSyncType){
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
                if(QuestController.Instance.categoriesSync.containsKey(category.id)){
                    category = QuestController.Instance.categoriesSync.get(category.id);
                    category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                }
                for(int i = 0; i < list.tagCount(); i++)
                {
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
                if(DialogController.Instance.categoriesSync.containsKey(category.id)){
                    category = DialogController.Instance.categoriesSync.get(category.id);
                    category.readSmallNBT(compound.getCompoundTag("CatNBT"));
                }
                for(int i = 0; i < list.tagCount(); i++)
                {
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
        PacketHandler.Instance.sendToAll(new SyncPacket(
            type,
            EnumSyncAction.UPDATE,
            cat,
            compound
        ));
    }


    public static NBTTagCompound updateQuestCat(QuestCategory questCategory) {
        NBTTagCompound questCompound = new NBTTagCompound();
        NBTTagList questList = new NBTTagList();
        for(int questID : questCategory.quests.keySet()){
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
        for(int questID : dialogCategory.dialogs.keySet()){
            Dialog dialog = dialogCategory.dialogs.get(questID);
            dialogList.appendTag(dialog.writeToNBT(new NBTTagCompound()));
        }
        dialogCompound.setTag("Data", dialogList);
        dialogCompound.setTag("CatNBT", dialogCategory.writeSmallNBT(new NBTTagCompound()));
        return dialogCompound;
    }

    @SideOnly(Side.CLIENT)
    public static void clientSyncRemove(EnumSyncType enumSyncType, int id) {
        switch (enumSyncType){
            case FACTION:
                FactionController.getInstance().factions.remove(id);
                break;
            case DIALOG:
                Dialog dialog = DialogController.Instance.dialogs.remove(id);
                if(dialog != null) {
                    dialog.category.dialogs.remove(id);
                }
                break;
            case DIALOG_CATEGORY:
                DialogCategory dialogCategory = DialogController.Instance.categories.remove(id);
                if(dialogCategory != null) {
                    DialogController.Instance.dialogs.keySet().removeAll(dialogCategory.dialogs.keySet());
                }
                break;
            case QUEST:
                Quest quest = QuestController.Instance.quests.remove(id);
                if(quest != null) {
                    quest.category.quests.remove(id);
                }
                break;
            case QUEST_CATEGORY:
                QuestCategory questCategory = QuestController.Instance.categories.remove(id);
                if(questCategory != null) {
                    QuestController.Instance.quests.keySet().removeAll(questCategory.quests.keySet());
                }
                break;
            case MAGIC:
                for(MagicCycle cycle : MagicController.getInstance().cycles.values()){
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

    public static void syncEffects(EntityPlayerMP playerMP){
        ConcurrentHashMap<EffectKey, PlayerEffect> playerEffects = CustomEffectController.getInstance().getPlayerEffects(playerMP);
        PlayerData playerData = PlayerData.get(playerMP);
        playerData.effectData.setEffects(playerEffects);

        NBTTagCompound compound = playerData.getPlayerEffects();
        PacketHandler.Instance.sendToPlayer(new SyncEffectPacket(compound), playerMP);
    }

    @SideOnly(Side.CLIENT)
    public static void clientSyncEffects(NBTTagCompound compound) {
        PlayerData playerData = PlayerData.get(Minecraft.getMinecraft().thePlayer);
        if(playerData != null){
            playerData.setPlayerEffects(compound);
        }
    }
}
