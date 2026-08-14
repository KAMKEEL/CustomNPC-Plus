package kamkeel.npcs.fixtures;

import cpw.mods.fml.common.Loader;
import kamkeel.npcs.addon.DBCAddon;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.NoppesUtilServer;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Shared machinery for the fixture generator: bringing Minecraft far enough up to serialize, and
 * building the item stacks the content refers to.
 *
 * <p>Nothing here decides what a fixture contains. That lives in the world builders.
 */
public final class Fixtures {

    /**
     * A fixed instant, used everywhere a fixture wants a timestamp.
     *
     * <p>2021-03-14T01:59:26Z. Any constant would do; what matters is that it is a constant, so
     * two runs of the generator produce the same bytes and a diff means something changed.
     */
    public static final long EPOCH = 1615687166000L;

    private static boolean booted;

    private Fixtures() {
    }

    /**
     * Registers vanilla blocks and items so item stacks can be serialized.
     *
     * <p>{@link Item#itemRegistry} is Forge's {@code FMLControlledNamespacedRegistry}, and
     * {@link noppes.npcs.NoppesUtilServer#writeItem} asks it for a registry name -- so a stack
     * written before this call would be saved as {@code minecraft:air} rather than failing, which
     * is the quiet kind of wrong.
     */
    public static void bootMinecraft() {
        if (booted) {
            return;
        }
        installBareLoader();
        Bootstrap.func_151354_b();
        // PlayerData.getNBT ends with DBCAddon.instance.writeToNBT(...), and the field is only set
        // by the constructor. DBCAddon is a shell whose methods a companion mod replaces by mixin,
        // so every method here is a no-op -- constructing one costs nothing and is what stops a
        // player record from throwing on its last line. Without the addon installed this is
        // exactly the state a real server is in.
        new DBCAddon();
        booted = true;
        verifyRegistry();
    }

    /**
     * Makes {@code Loader.instance()} answerable without launching FML.
     *
     * <p>Registering an item goes {@code FMLControlledNamespacedRegistry.addObject} ->
     * {@code GameData.register} -> {@code GameData.addPrefix}, and addPrefix asks
     * {@code Loader.instance().activeModContainer()} which namespace to use. Reaching that
     * normally constructs a {@code Loader}, whose constructor builds a {@code ModClassLoader} and
     * casts its parent to LaunchWrapper's {@code LaunchClassLoader} -- which fails outside a
     * LaunchWrapper launch, and is where this generator first stopped.
     *
     * <p>So the singleton is filled with an allocated-but-unconstructed Loader. Its
     * {@code modController} is null, {@code activeModContainer()} returns null, and addPrefix
     * takes its own documented branch: <i>"no mod container, assume minecraft"</i>. That is
     * exactly the branch real vanilla registration takes, because no mod is active while
     * {@code Item.registerItems()} runs.
     *
     * <p>Nothing about the naming rule is reimplemented here -- Forge still computes it. What this
     * removes is only the classloader assumption. {@link #verifyRegistry()} then checks the result
     * against what real CustomNPC+ save files contain, so the shortcut is measured rather than
     * trusted. On the registration path {@code Loader} is touched at that one call and nowhere
     * else; the other uses in GameData are world remapping and mod-declared substitutions.
     */
    private static void installBareLoader() {
        try {
            Field instance = Loader.class.getDeclaredField("instance");
            instance.setAccessible(true);
            if (instance.get(null) != null) {
                return;
            }
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);
            instance.set(null, unsafe.allocateInstance(Loader.class));
        } catch (Exception e) {
            throw new IllegalStateException(
                "Could not install a bare FML Loader. Without it the item registry cannot be "
                    + "populated outside a LaunchWrapper launch.", e);
        }
    }

    /**
     * Checks that registration produced the names real save files contain.
     *
     * <p>{@code minecraft:diamond}, not {@code diamond}: the prefix comes from Forge, and a
     * generator that quietly lost it would emit fixtures that every reader tested against them
     * would then agree with. The expected values here were read out of the CustomNPC+ worlds in
     * {@code run/saves/}, not from the code that produces them.
     */
    private static void verifyRegistry() {
        String diamond = Item.itemRegistry.getNameForObject(Items.diamond);
        if (!"minecraft:diamond".equals(diamond)) {
            throw new IllegalStateException("Item registry produced '" + diamond
                + "' where real CustomNPC+ saves contain 'minecraft:diamond'. Item stacks in these "
                + "fixtures would not match anything the mod writes.");
        }
        NBTTagCompound probe = new NBTTagCompound();
        NoppesUtilServer.writeItem(new ItemStack(Items.golden_apple, 2, 1), probe);
        if (!"minecraft:golden_apple".equals(probe.getString("id"))
            || probe.getByte("Count") != 2
            || probe.getShort("Damage") != 1) {
            throw new IllegalStateException(
                "NoppesUtilServer.writeItem produced " + probe + ", which is not an item stack.");
        }
    }

    /** A stack of a vanilla item, by registry name, so the fixture reads as what it is. */
    public static ItemStack item(String registryName, int count, int damage) {
        Item item = (Item) Item.itemRegistry.getObject(registryName);
        if (item == null) {
            throw new IllegalArgumentException("No such item: " + registryName);
        }
        return new ItemStack(item, count, damage);
    }

    public static ItemStack item(String registryName, int count) {
        return item(registryName, count, 0);
    }

    /**
     * A stack carrying custom NBT, which is the case that matters most to Scribe: item state above
     * (id, damage) is what the Flattening and Data Components rearranged.
     */
    public static ItemStack namedItem(String registryName, int count, int damage, String displayName) {
        ItemStack stack = item(registryName, count, damage);
        NBTTagCompound display = new NBTTagCompound();
        display.setString("Name", displayName);
        NBTTagList lore = new NBTTagList();
        lore.appendTag(new NBTTagString("Written by the Scribe fixture generator"));
        display.setTag("Lore", lore);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("display", display);
        tag.setBoolean("Unbreakable", true);
        tag.setInteger("RepairCost", 7);
        stack.stackTagCompound = tag;
        return stack;
    }

    /**
     * A stack whose identity is a pre-Flattening variant -- damage as a variant selector rather
     * than durability. Scribe converts these through a table that throws on a gap, so a fixture
     * holding one is the only way to find out which entries the table is still missing.
     */
    public static ItemStack variant(String registryName, int count, int variantDamage) {
        return item(registryName, count, variantDamage);
    }
}
