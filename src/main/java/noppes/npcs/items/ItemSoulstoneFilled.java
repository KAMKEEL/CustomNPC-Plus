package noppes.npcs.items;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;

import java.util.List;

public class ItemSoulstoneFilled extends Item {
    public ItemSoulstoneFilled() {
        setMaxStackSize(1);
    }

    @Override
    public Item setUnlocalizedName(String name) {
        super.setUnlocalizedName(name);
        GameRegistry.registerItem(this, name);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bo) {
        if (stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("Entity", 10)) {
            list.add(EnumChatFormatting.RED + "Error");
            return;
        }
        String name = StatCollector.translateToLocal(stack.stackTagCompound.getString("Name"));
        if (stack.stackTagCompound.hasKey("DisplayName"))
            name = stack.stackTagCompound.getString("DisplayName") + " (" + name + ")";
        list.add(EnumChatFormatting.BLUE + name);

        if (stack.stackTagCompound.hasKey("ExtraText")) {
            String text = "";
            String[] split = stack.stackTagCompound.getString("ExtraText").split(",");
            for (String s : split)
                text += StatCollector.translateToLocal(s);
            list.add(text);
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        if (!spawn(player, stack, world, x, y, z))
            return false;

        if (!player.capabilities.isCreativeMode)
            stack.splitStack(1);
        return true;
    }

    public boolean spawn(EntityPlayer player, ItemStack stack, World world, int x, int y, int z) {
        if (world.isRemote)
            return true;
        if (stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("Entity", 10))
            return false;
        NBTTagCompound compound = stack.stackTagCompound.getCompoundTag("Entity");
        Entity entity = EntityList.createEntityFromNBT(compound, world);
        if (entity == null)
            return false;
        entity.setPosition(x + 0.5, y + 1 + 0.2F, z + 0.5);
        entity.dimension = world.provider.dimensionId;
        String sourceName = "DISPENSER";
        if (player != null) {
            sourceName = player.getCommandSenderName();
        }
        if (ConfigDebug.PlayerLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            LogWriter.script(String.format("[%s] %s PLACED ENTITY %s", "SOULSTONE", sourceName, entity));
        }
        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) entity;
            npc.ais.startPos = new int[]{x, y, z};
            npc.setHealth(npc.getMaxHealth());
            npc.setPosition((float) x + 0.5F, npc.getStartYPos(), (float) z + 0.5F);
            npc.advanced.soulStonePlayerName = sourceName;
            npc.advanced.soulStoneInit = true;
            if (npc.advanced.role == EnumRoleType.Companion && player != null) {
                PlayerData data = PlayerDataController.Instance.getPlayerData(player);
                if (data.hasCompanion())
                    return false;
                ((RoleCompanion) npc.roleInterface).setOwner(player);
                data.setCompanion(npc);
            }
            if (npc.advanced.role == EnumRoleType.Follower && player != null) {
                ((RoleFollower) npc.roleInterface).setOwner(player);
            }
        }
        world.spawnEntityInWorld(entity);
        return true;
    }
}
