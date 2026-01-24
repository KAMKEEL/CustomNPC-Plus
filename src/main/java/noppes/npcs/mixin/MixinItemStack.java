package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.util.AttributeItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.player.GuiAuctionInterface;
import noppes.npcs.client.gui.player.GuiAuctionListing;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.containers.ContainerAuctionListing;
import noppes.npcs.controllers.data.AuctionListing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static kamkeel.npcs.util.AttributeItemUtil.TAG_RPGCORE;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract boolean hasTagCompound();

    @Shadow
    public abstract NBTTagCompound getTagCompound();

    @Shadow
    public NBTTagCompound stackTagCompound;

    @Inject(method = "getTooltip", at = @At("TAIL"), cancellable = true)
    public void getAttributeTooltip(EntityPlayer player, boolean advanced, CallbackInfoReturnable<List<String>> cir) {
        List<String> tooltip = cir.getReturnValue();
        if (hasTagCompound() && getTagCompound().hasKey(TAG_RPGCORE)) {
            cir.setReturnValue(AttributeItemUtil.getToolTip(tooltip, getTagCompound()));
        }
    }
}
