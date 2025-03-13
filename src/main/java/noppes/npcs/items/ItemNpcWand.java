package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityCustomNpc;

public class ItemNpcWand extends Item {

    public ItemNpcWand() {
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer player) {
        if (!par2World.isRemote)
            return par1ItemStack;
        else if (CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.NPC_GUI)) {
            CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.NpcRemote, player);
        } else
            player.addChatMessage(new ChatComponentTranslation("availability.permission"));
        return par1ItemStack;
    }

    @Override
    public boolean onItemUse(ItemStack par1ItemStack, final EntityPlayer player, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
        if (par3World.isRemote)
            return false;
        if (ConfigMain.OpsOnly && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile())) {
            player.addChatMessage(new ChatComponentTranslation("availability.permission"));
            return false;
        } else if (CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.NPC_CREATE)) {
            EntityCustomNpc npc = new EntityCustomNpc(par3World);
            npc.ais.startPos = new int[]{par4, par5, par6};

            npc.setLocationAndAngles((float) par4 + 0.5F, npc.getStartYPos(), (float) par6 + 0.5F, player.rotationYaw, player.rotationPitch);

            par3World.spawnEntityInWorld(npc);
            npc.setHealth(npc.getMaxHealth());

            NoppesUtilServer.sendOpenGuiNoDelay(player, EnumGuiType.MainMenuDisplay, npc);
            return true;
        } else
            player.addChatMessage(new ChatComponentTranslation("availability.permission"));
        return false;
    }

    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        return 0x8B4513;
    }

    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = Items.iron_hoe.getIconFromDamage(0);
    }

    @Override
    public Item setUnlocalizedName(String name) {
        GameRegistry.registerItem(this, name);
        return super.setUnlocalizedName(name);
    }
}
