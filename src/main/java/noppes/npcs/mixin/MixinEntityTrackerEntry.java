package noppes.npcs.mixin;

import cpw.mods.fml.common.network.internal.FMLMessage;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.common.registry.EntityRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

/**
 * Fixes a race condition in Forge's FMLNetworkHandler.getEntitySpawningPacket.
 *
 * Forge uses a single static shared EmbeddedChannel to serialize all mod entity
 * spawn packets. The channel's generatePacketFrom method does a non-atomic
 * write-then-poll, so concurrent calls can swap, lose, or misdeliver packets.
 *
 * This mixin bypasses the shared channel by serializing directly into a fresh
 * ByteBuf per call, eliminating the race condition entirely.
 */
@Mixin(EntityTrackerEntry.class)
public abstract class MixinEntityTrackerEntry {
    private static final Logger LOGGER = LogManager.getLogger("CustomNPCs-SpawnFix");
    private static Method toBytesMethod;

    static {
        try {
            toBytesMethod = FMLMessage.EntitySpawnMessage.class.getDeclaredMethod("toBytes", ByteBuf.class);
            toBytesMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LOGGER.fatal("Failed to find FMLMessage.EntitySpawnMessage.toBytes - entity spawn fix will not work", e);
        }
    }

    @Shadow
    public Entity myEntity;

    @Redirect(
        method = "func_151260_c",
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/common/network/internal/FMLNetworkHandler;getEntitySpawningPacket(Lnet/minecraft/entity/Entity;)Lnet/minecraft/network/Packet;"
        ),
        remap = false
    )
    private Packet cnpcplus$getEntitySpawningPacket(Entity entity) {
        if (toBytesMethod == null) {
            return null;
        }

        EntityRegistry.EntityRegistration er = EntityRegistry.instance().lookupModSpawn(entity.getClass(), false);
        if (er == null || er.usesVanillaSpawning()) {
            return null;
        }

        FMLMessage.EntitySpawnMessage message = new FMLMessage.EntitySpawnMessage(er, entity, er.getContainer());
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(2);
        try {
            toBytesMethod.invoke(message, buf);
        } catch (Throwable e) {
            LOGGER.warn("Failed to serialize entity spawn packet for {}", entity.getClass().getName(), e);
            buf.release();
            return null;
        }
        return new FMLProxyPacket(buf, "FML");
    }
}
