package io.github.frostzie.nodex.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.tree.CommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.Locale;
import java.util.Map;

// Taken from Firmament
@Mixin(value = CommandNode.class, remap = false)
public class CaseInsensitiveCommandMapMixin<S> {
    @WrapOperation(
            method = "getRelevantNodes",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"),
            remap = false
    )
    public Object modifyCommandLookup(Map map, Object text, Operation<Object> op) {
        var original = op.call(map, text);
        if (original == null && text instanceof String) {
            return map.get(((String) text).toLowerCase(Locale.ROOT));
        }
        return original;
    }
}