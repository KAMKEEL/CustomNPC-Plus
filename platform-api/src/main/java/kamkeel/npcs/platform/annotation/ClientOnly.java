package kamkeel.npcs.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks code that should only exist on the physical client.
 *
 * This is a documentation-only annotation in core — it does NOT trigger
 * class stripping like Forge's @SideOnly or Fabric's @Environment.
 *
 * Platform modules should use their native side annotations for actual stripping:
 * - Forge 1.7.10/1.12: @SideOnly(Side.CLIENT)
 * - Forge 1.16+: @OnlyIn(Dist.CLIENT)
 * - Fabric: @Environment(EnvType.CLIENT)
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface ClientOnly {
}
