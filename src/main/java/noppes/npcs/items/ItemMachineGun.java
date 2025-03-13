package noppes.npcs.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.config.ConfigItem;
import noppes.npcs.entity.EntityProjectile;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ItemMachineGun extends ItemNpcInterface {

    public ItemMachineGun(int par1) {
        super(par1);
        this.setMaxDamage(80);
        setCreativeTab(CustomItems.tabWeapon);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("ShotsLeft")) {
            list.add(StatCollector.translateToLocalFormatted("item.npcMachineGun.info") + ": " +
                stack.stackTagCompound.getInteger("ShotsLeft"));
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        if (player.worldObj.isRemote) { // Skip if on client side
            return;
        }

        int ticks = getMaxItemUseDuration(stack) - count; // How many ticks you’ve been holding
        ensureNBT(stack); // Make sure the item has its data tags

        int shotsLeft = stack.stackTagCompound.getInteger("ShotsLeft");
        boolean isReloading = stack.stackTagCompound.getBoolean("IsReloading");

        if (!isReloading && shotsLeft > 0) {
            if (ticks >= ConfigItem.MachineGunTickSpeed && ticks % ConfigItem.MachineGunTickSpeed == 0) {
                fireShot(stack, player); // Fire a shot (decrements ShotsLeft)
                shotsLeft = stack.stackTagCompound.getInteger("ShotsLeft");
                if (shotsLeft <= 0 && hasItem(player, CustomItems.bulletBlack)) {
                    stack.stackTagCompound.setBoolean("IsReloading", true);
                }
            }
        } else if (isReloading && shotsLeft < ConfigItem.MachineGunAmmo && hasItem(player, CustomItems.bulletBlack)) {
            if (ticks % ConfigItem.MachineGunTickSpeed - 1 == 0) {
                reloadShot(stack, player); // Add a shot and consume a bullet
                player.worldObj.playSoundAtEntity(player, "customnpcs:gun.ak47.load", 1.0F, 1.0F); // Play reload sound
            }
        } else if (ticks % ConfigItem.MachineGunTickSpeed == 0) {
            // No shots or bullets left, play empty sound
            player.worldObj.playSoundAtEntity(player, "customnpcs:gun.empty", 1.0F, 1.0F);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World par2World, EntityPlayer player, int count) {
        if (stack.stackTagCompound == null) {
            return;
        }
        // Stop reloading when you let go
        stack.stackTagCompound.setBoolean("IsReloading", false);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isUsingItem()) {
            return stack; // Avoid glitches from spamming
        }

        ensureNBT(stack); // Set up data tags if missing
        int shotsLeft = stack.stackTagCompound.getInteger("ShotsLeft");
        stack.stackTagCompound.setBoolean("IsReloading", shotsLeft == 0 && hasItem(player, CustomItems.bulletBlack));

        player.setItemInUse(stack, this.getMaxItemUseDuration(stack)); // Start the hold action
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        if (stack.stackTagCompound == null || !stack.stackTagCompound.getBoolean("IsReloading")) {
            return EnumAction.bow; // Firing animation
        }
        return EnumAction.block; // Reloading animation
    }

    @Override
    public void renderSpecial() {
        GL11.glRotatef(-6, 0, 0, 1f);
        GL11.glScalef(0.8f, 0.7f, 0.7f);
        GL11.glTranslatef(0.2f, 0.0f, 0.2F);
    }

    private void ensureNBT(ItemStack stack) {
        if (stack.stackTagCompound == null) {
            stack.stackTagCompound = new NBTTagCompound();
        }
        if (!stack.stackTagCompound.hasKey("ShotsLeft")) {
            stack.stackTagCompound.setInteger("ShotsLeft", 0);
        }
        if (!stack.stackTagCompound.hasKey("IsReloading")) {
            stack.stackTagCompound.setBoolean("IsReloading", false);
        }
    }

    private void fireShot(ItemStack stack, EntityPlayer player) {
        if (!ConfigItem.GunsEnabled && !player.capabilities.isCreativeMode) {
            return;
        }
        int shotsLeft = stack.stackTagCompound.getInteger("ShotsLeft");
        if (shotsLeft <= 0) {
            return;
        }

        EntityProjectile projectile = new EntityProjectile(player.worldObj, player,
            new ItemStack(CustomItems.bulletBlack, 1, 0), false);
        projectile.damage = 4;
        projectile.setSpeed(40);
        projectile.shoot(2);
        player.worldObj.playSoundAtEntity(player, "customnpcs:gun.pistol.shot", 0.9F,
            itemRand.nextFloat() * 0.3F + 0.8F);
        player.worldObj.spawnEntityInWorld(projectile);

        stack.stackTagCompound.setInteger("ShotsLeft", shotsLeft - 1);
        if (!player.capabilities.isCreativeMode) {
            consumeItem(player, CustomItems.bulletBlack);
        }
    }

    private void reloadShot(ItemStack stack, EntityPlayer player) {
        int shotsLeft = stack.stackTagCompound.getInteger("ShotsLeft");
        if (shotsLeft < ConfigItem.MachineGunAmmo && hasItem(player, CustomItems.bulletBlack)) {
            stack.stackTagCompound.setInteger("ShotsLeft", shotsLeft + 1); // Add one shot
            if (!player.capabilities.isCreativeMode) {
                consumeItem(player, CustomItems.bulletBlack); // Use up a bullet
            }
        }
    }
}
