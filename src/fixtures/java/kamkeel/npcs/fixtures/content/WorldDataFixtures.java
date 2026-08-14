package kamkeel.npcs.fixtures.content;

import kamkeel.npcs.fixtures.Fixtures;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The world-level {@code .dat} documents: global, tags, transport, banks and spawns.
 *
 * <p>All five share a shape -- one compound holding a list of entries plus a {@code lastID} -- and
 * each list is built here in a fixed order rather than by iterating the controller's HashMap, so
 * regeneration is a no-op rather than a reshuffle.
 *
 * <p>Every document format below is copied from its controller's {@code getNBT}, named per method.
 */
public final class WorldDataFixtures {

    private WorldDataFixtures() {
    }

    // ------------------------------------------------------------------ global.dat

    /** GlobalDataController.saveData -- a single integer, and the smallest document CNPC+ writes. */
    public static NBTTagCompound global(int itemGiverId) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("itemGiverId", itemGiverId);
        return compound;
    }

    // ------------------------------------------------------------------ tags.dat

    /**
     * Tags. Note {@link Tag#uuid} defaults to {@code randomUUID()}, so every fixture here sets one
     * explicitly -- otherwise the corpus would differ on every run and the reproducibility check
     * would fail for a reason that has nothing to do with the format.
     */
    public static NBTTagCompound tags() {
        List<Tag> tags = new ArrayList<Tag>();

        tags.add(tag(1, "Boss", 0xFF0000, "3f2504e0-4f89-41d3-9a0c-0305e82c3301", false));
        tags.add(tag(2, "Hidden", 0x333333, "3f2504e0-4f89-41d3-9a0c-0305e82c3302", true));
        tags.add(tag(3, "", 0x00FF00, "3f2504e0-4f89-41d3-9a0c-0305e82c3303", false));
        tags.add(tag(4, Text.KATAKANA_FACTION, 0xFFFFFF,
            "3f2504e0-4f89-41d3-9a0c-0305e82c3304", false));
        tags.add(tag(5, "Extremes", -1, "ffffffff-ffff-ffff-ffff-ffffffffffff", true));

        NBTTagList list = new NBTTagList();
        for (Tag tag : tags) {
            NBTTagCompound entry = new NBTTagCompound();
            tag.writeNBT(entry);
            list.appendTag(entry);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("lastID", 5);
        compound.setTag("NPCTags", list);
        return compound;
    }

    private static Tag tag(int id, String name, int color, String uuid, boolean hidden) {
        Tag tag = new Tag();
        tag.id = id;
        tag.name = name;
        tag.color = color;
        tag.uuid = UUID.fromString(uuid);
        tag.hideTag = hidden;
        return tag;
    }

    // ------------------------------------------------------------------ transport.dat

    /**
     * Transport locations, grouped into categories.
     *
     * <p>The one place CustomNPC+ stores a dimension as a bare {@code int} alongside a position --
     * which is the signature that stopped being expressible after 1.16, so it is worth having on
     * disk in both a vanilla dimension and a modded id.
     */
    public static NBTTagCompound transport() {
        List<TransportCategory> categories = new ArrayList<TransportCategory>();

        TransportCategory overworld = category(1, "Overworld");
        overworld.locations.put(1, location(1, "Spawn Plaza", 0.5D, 64.0D, 0.5D, 0, 0));
        overworld.locations.put(2, location(2, "Northern Watchtower", -1204.5D, 96.0D, 3311.5D, 1, 0));
        overworld.locations.put(3, location(3, "Negative Coordinates",
            -30000000.0D, -64.0D, -30000000.0D, 2, 0));
        categories.add(overworld);

        TransportCategory other = category(2, "Other Dimensions");
        other.locations.put(4, location(4, "Nether Hub", 12.0D, 70.0D, -8.0D, 0, -1));
        other.locations.put(5, location(5, "The End", 100.0D, 50.0D, 0.0D, 0, 1));
        // A modded dimension id, which is the case a numeric-to-key translation table must either
        // cover or refuse rather than silently map to the overworld.
        other.locations.put(6, location(6, "Modded Dimension", 0.0D, 0.0D, 0.0D, 0, 7265));
        categories.add(other);

        TransportCategory empty = category(3, "Empty Category");
        categories.add(empty);

        NBTTagList list = new NBTTagList();
        for (TransportCategory category : categories) {
            NBTTagCompound entry = new NBTTagCompound();
            category.writeNBT(entry);
            list.appendTag(entry);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("lastID", 6);
        compound.setTag("NPCTransportCategories", list);
        return compound;
    }

    private static TransportCategory category(int id, String title) {
        TransportCategory category = new TransportCategory();
        category.id = id;
        category.title = title;
        return category;
    }

    private static TransportLocation location(int id, String name, double x, double y, double z,
                                              int type, int dimension) {
        TransportLocation location = new TransportLocation();
        location.id = id;
        location.name = name;
        location.posX = x;
        location.posY = y;
        location.posZ = z;
        location.type = type;
        location.dimension = dimension;
        return location;
    }

    // ------------------------------------------------------------------ bank.dat

    /** BankController.getNBT -- note the key is "Data" and there is no lastID. */
    public static NBTTagCompound banks() {
        List<Bank> banks = new ArrayList<Bank>();

        Bank basic = bank(1, "Village Bank", 2, 9);
        basic.currencyInventory.items.put(0, Fixtures.item("minecraft:emerald", 1));
        basic.upgradeInventory.items.put(0, Fixtures.item("minecraft:gold_ingot", 8));
        for (int slot = 0; slot < 9; slot++) {
            basic.slotTypes.put(slot, slot % 3);
        }
        banks.add(basic);

        Bank bare = bank(2, "", 1, 1);
        banks.add(bare);

        Bank big = bank(3, "Vault", 6, 54);
        big.currencyInventory.items.put(0, Fixtures.namedItem("minecraft:diamond", 64, 0, "Deposit"));
        for (int slot = 0; slot < 54; slot++) {
            big.slotTypes.put(slot, slot % 4);
        }
        banks.add(big);

        NBTTagList list = new NBTTagList();
        for (Bank bank : banks) {
            NBTTagCompound entry = new NBTTagCompound();
            bank.writeEntityToNBT(entry);
            list.appendTag(entry);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("Data", list);
        return compound;
    }

    private static Bank bank(int id, String name, int startSlots, int maxSlots) {
        Bank bank = new Bank();
        bank.id = id;
        bank.name = name;
        bank.startSlots = startSlots;
        bank.maxSlots = maxSlots;
        return bank;
    }

    // ------------------------------------------------------------------ spawns.dat

    /**
     * Natural spawn entries.
     *
     * <p>The interesting shape here is {@code SpawnCompound<n>}: the per-variant NPC data is
     * written as a numbered key rather than a list, so a reader has to probe for
     * {@code SpawnCompound0}, {@code SpawnCompound1}, ... rather than iterate. The dimension set
     * is written only when non-empty, so the key is absent on most entries.
     */
    public static NBTTagCompound spawns() {
        List<SpawnData> spawns = new ArrayList<SpawnData>();

        SpawnData plains = spawn(1, "Plains Wanderer", 10);
        plains.biomes.addAll(Arrays.asList("Plains", "Sunflower Plains", "Forest"));
        plains.spawnCompounds.put(0, npcStub("Wanderer"));
        spawns.add(plains);

        SpawnData variants = spawn(2, "Cave Dweller", 3);
        variants.biomes.add("Extreme Hills");
        variants.dimensions.addAll(Arrays.asList(0, -1, 7265));
        variants.monsterSpawning = true;
        variants.animalSpawning = false;
        variants.airSpawning = true;
        variants.liquidSpawning = true;
        variants.spawnHeightMin = 5;
        variants.spawnHeightMax = 40;
        variants.maxAlive = 12;
        variants.cooldownTicks = 600;
        variants.attemptsPerCycle = 4;
        // Three numbered compounds, which is the shape worth pinning.
        variants.spawnCompounds.put(0, npcStub("Dweller A"));
        variants.spawnCompounds.put(1, npcStub("Dweller B"));
        variants.spawnCompounds.put(2, npcStub("Dweller C"));
        spawns.add(variants);

        SpawnData bare = spawn(3, "", 0);
        spawns.add(bare);

        NBTTagList list = new NBTTagList();
        for (SpawnData spawn : spawns) {
            list.appendTag(spawn.writeNBT(new NBTTagCompound()));
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("lastID", 3);
        compound.setTag("NPCSpawnData", list);
        return compound;
    }

    private static SpawnData spawn(int id, String name, int weight) {
        SpawnData spawn = new SpawnData();
        spawn.id = id;
        spawn.name = name;
        spawn.itemWeight = weight;
        return spawn;
    }

    /**
     * A stand-in for a stored NPC.
     *
     * <p>Deliberately not a real {@code EntityCustomNpc}: constructing one needs a World, and a
     * spawn entry stores whatever compound it was given without inspecting it. A fixture with a
     * full NPC in it belongs with the NPC system, which is not covered here yet -- so this says
     * what it is rather than pretending.
     */
    private static NBTTagCompound npcStub(String name) {
        NBTTagCompound stub = new NBTTagCompound();
        stub.setString("Name", name);
        stub.setString("FixtureNote", "placeholder -- not a serialized EntityCustomNpc");
        return stub;
    }
}
