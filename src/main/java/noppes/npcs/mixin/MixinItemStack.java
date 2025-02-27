package noppes.npcs.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.attribute.ItemAttributeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

import static noppes.npcs.attribute.ItemAttributeHelper.RPGItemAttributes;

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
        if (hasTagCompound() && getTagCompound().hasKey(RPGItemAttributes)) {
            cir.setReturnValue(ItemAttributeHelper.getToolTip(tooltip, getTagCompound()));
        }
    }
}
