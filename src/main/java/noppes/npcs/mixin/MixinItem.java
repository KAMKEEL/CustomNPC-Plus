package noppes.npcs.mixin;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.gui.player.AuctionTooltipHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin to inject auction information into item tooltips when viewing auction GUIs.
 */
@Mixin(Item.class)
public class MixinItem {

    @SideOnly(Side.CLIENT)
    @Inject(method = "addInformation", at = @At("HEAD"))
    public void onAddInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced, CallbackInfo ci) {
        AuctionTooltipHandler.addAuctionInfo(stack, tooltip);
    }
}
