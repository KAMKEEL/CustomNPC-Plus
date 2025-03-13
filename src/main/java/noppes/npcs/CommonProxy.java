package noppes.npcs;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import noppes.npcs.api.IWorld;
import noppes.npcs.blocks.tiles.TileNpcContainer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.*;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class CommonProxy implements IGuiHandler {
    public boolean newVersionAvailable = false;
    public int revision = 1;

    public void load() {
    }

    public PlayerData getPlayerData(EntityPlayer player) {
        return null;
    }

    public AnimationData getClientAnimationData(Entity entity) {
        return null;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID > EnumGuiType.values().length)
            return null;
        EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
        EnumGuiType gui = EnumGuiType.values()[ID];
        return getContainer(gui, player, x, y, z, npc);
    }

    public Container getContainer(EnumGuiType gui, EntityPlayer player, int x, int y, int z, EntityNPCInterface npc) {
        if (gui == EnumGuiType.CustomGui)
            return new ContainerCustomGui(new InventoryBasic("", false, x));

        if (gui == EnumGuiType.MainMenuInv)
            return new ContainerNPCInv(npc, player);

        if (gui == EnumGuiType.PlayerBankSmall)
            return new ContainerNPCBankSmall(player, x, y);

        if (gui == EnumGuiType.PlayerBankUnlock)
            return new ContainerNPCBankUnlock(player, x, y);

        if (gui == EnumGuiType.PlayerBankUprade)
            return new ContainerNPCBankUpgrade(player, x, y);

        if (gui == EnumGuiType.PlayerBankLarge)
            return new ContainerNPCBankLarge(player, x, y);

        if (gui == EnumGuiType.PlayerFollowerHire)
            return new ContainerNPCFollowerHire(npc, player);

        if (gui == EnumGuiType.PlayerFollower)
            return new ContainerNPCFollower(npc, player);

        if (gui == EnumGuiType.PlayerTrader)
            return new ContainerNPCTrader(npc, player);

        if (gui == EnumGuiType.PlayerCarpentryBench)
            return new ContainerCarpentryBench(player.inventory, player.worldObj, x, y, z);

        if (gui == EnumGuiType.PlayerAnvil)
            return new ContainerAnvilRepair(player.inventory, player.worldObj, x, y, z);

        if (gui == EnumGuiType.SetupItemGiver)
            return new ContainerNpcItemGiver(npc, player);

        if (gui == EnumGuiType.SetupTrader)
            return new ContainerNPCTraderSetup(npc, player);

        if (gui == EnumGuiType.SetupFollower)
            return new ContainerNPCFollowerSetup(npc, player);

        if (gui == EnumGuiType.QuestReward)
            return new ContainerNpcQuestReward(player);

        if (gui == EnumGuiType.QuestItem)
            return new ContainerNpcQuestTypeItem(player);

        if (gui == EnumGuiType.ManageRecipes)
            return new ContainerManageRecipes(player, x);

        if (gui == EnumGuiType.ManageBanks)
            return new ContainerManageBanks(player);

        if (gui == EnumGuiType.MerchantAdd)
            return new ContainerMerchantAdd(player, ServerEventsHandler.Merchant, player.worldObj);

        if (gui == EnumGuiType.Crate)
            return new ContainerCrate(player.inventory, (TileNpcContainer) player.worldObj.getTileEntity(x, y, z));

        if (gui == EnumGuiType.PlayerMailman)
            return new ContainerMail(player, x == 1, y == 1);

        if (gui == EnumGuiType.CompanionInv)
            return new ContainerNPCCompanion(npc, player);

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

        return null;
    }

    public void openGui(EntityNPCInterface npc, EnumGuiType gui) {

    }

    public void openGui(EntityNPCInterface npc, EnumGuiType gui, int x, int y, int z) {

    }


    public void openGui(int i, int j, int k, EnumGuiType gui, EntityPlayer player) {

    }

    public void openGui(EntityPlayer player, Object guiscreen) {


    }

    public void spawnParticle(EntityLivingBase player, String string, Object... ob) {


    }

    public FakePlayer getCommandPlayer(IWorld world) {
        return null;
    }

    public boolean hasClient() {
        return false;
    }

    public EntityPlayer getPlayer() {
        return null;
    }

    public void registerItem(Item item) {


    }

    public ModelBiped getSkirtModel() {
        return null;
    }

    public void spawnParticle(String particle, double x, double y, double z,
                              double motionX, double motionY, double motionZ, float scale) {

    }

    public void generateBigSmokeParticles(World world, int x, int y, int z, boolean signalFire) {

    }

    public String getAchievementDesc(Achievement achievement) {
        return "";
    }

    public boolean isGUIOpen() {
        return false;
    }
}
