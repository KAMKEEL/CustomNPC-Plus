package noppes.npcs.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import noppes.npcs.*;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityCustomNpc;

public class ItemNbtBook extends Item {

    public ItemNbtBook() {
        maxStackSize = 1;
        setCreativeTab(CustomItems.tab);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer player) {
        if (!par2World.isRemote)
            return par1ItemStack;
        else if (CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.TOOL_NBT_BOOK)) {
            CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.NbtBook, player);
        } else
            player.addChatMessage(new ChatComponentTranslation("availability.permission"));
        return par1ItemStack;
    }

    public void blockEvent(PlayerInteractEvent event) {
        NBTTagCompound data = new NBTTagCompound();
        TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);
        if(tile != null) {
            tile.writeToNBT(data);
        }

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("Data", data);
        GuiDataPacket.sendGuiData((EntityPlayerMP) event.entityPlayer, compound);
    }

    public void entityEvent(EntityInteractEvent event) {
        if(event.target instanceof EntityPlayer) {
            return;
        }

        NBTTagCompound data = new NBTTagCompound();
        event.target.writeToNBTOptional(data);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("EntityId", event.target.getEntityId());
        compound.setTag("Data", data);
        GuiDataPacket.sendGuiData((EntityPlayerMP) event.entityPlayer, compound);
    }

    @Override
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
        return 0x8B4513;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = Items.book.getIconFromDamage(0);
    }

    @Override
    public Item setUnlocalizedName(String name) {
        GameRegistry.registerItem(this, name);
        return super.setUnlocalizedName(name);
    }
}
