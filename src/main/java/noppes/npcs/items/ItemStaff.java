package noppes.npcs.items;

import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.entity.EntityAbilityOrb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumNpcToolMaterial;
import noppes.npcs.enchants.EnchantInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.IProjectileCallback;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ItemStaff extends ItemNpcInterface implements IProjectileCallback {

    private EnumNpcToolMaterial material;

    protected OrbColor color;

    public ItemStaff(int par1, EnumNpcToolMaterial material) {
        super(par1);
        this.material = material;
        this.color = OrbColor.get(material.ordinal());;
        setCreativeTab(CustomItems.tabWeapon);
    }

    public void renderSpecial() {
        GL11.glScalef(1f, 1.14f, 1f);
        GL11.glTranslatef(0.14f, -0.3f, 0.08f);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int par4) {

        if (world.isRemote)
            return;

        if (stack.stackTagCompound == null)
            return;

        Entity entity = ((WorldServer) world)
            .getEntityByID(stack.stackTagCompound.getInteger("MagicProjectile"));

        if (!(entity instanceof EntityAbilityOrb))
            return;

        EntityAbilityOrb orb = (EntityAbilityOrb) entity;

        orb.startMoving(null);

        world.playSoundAtEntity(
            player,
            "customnpcs:magic.shot",
            1.0F,
            1.0F
        );
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        int tick = getMaxItemUseDuration(stack) - count;

        if (player.worldObj.isRemote) {
            spawnParticle(stack, player);
            return;
        }

        int chargeTime = 20 + material.getHarvestLevel() * 8;

        if (tick == chargeTime) {

            if (!player.capabilities.isCreativeMode && !hasInfinite(stack)) {
                if (!hasItem(player, CustomItems.mana))
                    return;
                consumeItem(player, CustomItems.mana);
            }

            player.worldObj.playSoundAtEntity(
                player,
                "customnpcs:magic.charge",
                1.0F,
                1.0F
            );

            if (stack.stackTagCompound == null) {
                stack.stackTagCompound = new NBTTagCompound();
            }

            int damage = 6 + material.getDamageVsEntity() + player.worldObj.rand.nextInt(4);
            damage += damage * EnchantInterface.getLevel(EnchantInterface.Damage, stack) * 0.5f;

            EnergyCombatData combat = new EnergyCombatData();
            combat.damage = damage;
            combat.explosive = true;

            EnergyDisplayData colorData = new EnergyDisplayData(getOrbColor(stack, false), getOrbColor(stack, true));
            EnergyLightningData lightning = new EnergyLightningData();
            EnergyLifespanData lifespan = new EnergyLifespanData(100, 72000);
            EnergyHomingData homing = new EnergyHomingData();

            homing.speed = 0.5f;
            homing.homingStrength = 0.35f;
            homing.homingRange = 20f;

            EntityAbilityOrb orb = new EntityAbilityOrb(
                player.worldObj, player, null,
                player.posX, player.posY + player.getEyeHeight(), player.posZ,
                1.0f, colorData, combat, homing, lightning, lifespan
            );

            orb.setupCharging(
                new EnergyAnchorData(AnchorPoint.FRONT, 0, -1, 0),
                chargeTime
            );

            player.worldObj.spawnEntityInWorld(orb);
            stack.stackTagCompound.setInteger("MagicProjectile", orb.getEntityId());
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.bow;
    }

    public int getOrbColor(ItemStack stack, boolean outer) {
        if (color == OrbColor.GENERIC) {
            float[] color = EntitySheep.fleeceColorTable[stack.getItemDamage()];
            return new Color(color[0], color[1], color[2]).getRGB();
        }

        return outer ? color.outer : color.inner;
    }

    public ItemStack getProjectile(ItemStack stack) {
        if (stack.getItem() == CustomItems.staffWood) {
            return new ItemStack(CustomItems.spellNature);
        }
        if (stack.getItem() == CustomItems.staffStone || stack.getItem() == CustomItems.staffDemonic) {
            return new ItemStack(CustomItems.spellDark);
        }
        if (stack.getItem() == CustomItems.staffIron || stack.getItem() == CustomItems.staffMithril) {
            return new ItemStack(CustomItems.spellHoly);
        }
        if (stack.getItem() == CustomItems.staffBronze) {
            return new ItemStack(CustomItems.spellLightning);
        }
        if (stack.getItem() == CustomItems.staffGold) {
            return new ItemStack(CustomItems.spellFire);
        }
        if (stack.getItem() == CustomItems.staffDiamond || stack.getItem() == CustomItems.staffFrost) {
            return new ItemStack(CustomItems.spellIce);
        }
        if (stack.getItem() == CustomItems.staffEmerald) {
            return new ItemStack(CustomItems.spellArcane);
        }
        return new ItemStack(CustomItems.orb, 1, stack.getItemDamage());
    }

    public void spawnParticle(ItemStack stack, EntityPlayer player) {
        if (stack.getItem() == CustomItems.staffWood) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 5, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 12, 2);
        } else if (stack.getItem() == CustomItems.staffStone || stack.getItem() == CustomItems.staffDemonic) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x563357, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x432744, 2);
        } else if (stack.getItem() == CustomItems.staffBronze) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x83F7F6, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x5CF0FF, 2);
        } else if (stack.getItem() == CustomItems.staffIron || stack.getItem() == CustomItems.staffMithril) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0xFCFFC9, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0xEFFF97, 2);
        } else if (stack.getItem() == CustomItems.staffGold) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 1, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 14, 2);
        } else if (stack.getItem() == CustomItems.staffDiamond || stack.getItem() == CustomItems.staffFrost) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x94DFED, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0x44B6FF, 2);
        } else if (stack.getItem() == CustomItems.staffEmerald) {
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0xFFC3E7, 2);
            CustomNpcs.proxy.spawnParticle(player, "Spell", 0xFB92FF, 2);
        }
    }

    @Override
    public int getItemEnchantability() {
        return this.material.getEnchantability();
    }

    @Override
    public boolean isItemTool(ItemStack par1ItemStack) {
        return true;
    }

    @Override
    public boolean onImpact(EntityProjectile entityProjectile, EntityLivingBase entity, ItemStack itemstack) {
        int confusion = EnchantInterface.getLevel(EnchantInterface.Confusion, itemstack);
        if (confusion > 0) {
            if (entity.getRNG().nextInt(4) > confusion)
                entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 100));
        }
        int poison = EnchantInterface.getLevel(EnchantInterface.Poison, itemstack);
        if (poison > 0) {
            if (entity.getRNG().nextInt(4) > poison)
                entity.addPotionEffect(new PotionEffect(Potion.poison.id, 100));
        }
        return false;
    }

    public boolean hasInfinite(ItemStack stack) {
        return EnchantInterface.getLevel(EnchantInterface.Infinite, stack) > 0;
    }

    public enum OrbColor {
        WOOD(0x7cc818, 0x644a32),
        STONE(0x532d5b, 0x515151),
        BRONZE(0x83F7F6, 0x5CF0FF),
        IRON(0xf7fac5, 0xeafa94),
        DIA(0x44B6FF, 0x94DFED),
        GOLD(0xb3011e, 0xd47c32),
        EMERALD(0xdd6df7, 0xcf1dfa),
        DEMONIC(0xb8144a, 0x422643),
        FROST(0xffffff, 0xaccbfe),
        MITHRIL(0xf7fac5, 0xffffff),
        GENERIC(0xFFFFFF, 0x88FFFF);

        public final int inner;
        public final int outer;

        OrbColor(int inner, int outer) {
            this.inner = inner;
            this.outer = outer;
        }

        public static OrbColor get(int id) {
            if (id >= OrbColor.values().length || id < 0) return GENERIC;

            return OrbColor.values()[id];
        }
    }
}
