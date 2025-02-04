package noppes.npcs.controllers;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.IMagic;
import noppes.npcs.controllers.data.Magic;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class MagicController {
    public HashMap<Integer, Magic> magicSync = new HashMap<Integer, Magic>();
    public HashMap<Integer, Magic> magics;

    private static MagicController instance;

    private int lastUsedID = 0;

    public MagicController() {
        instance = this;
        magics = new HashMap<Integer, Magic>();
    }

    public static MagicController getInstance() {
        return instance;
    }

    public Magic getMagic(int magic) {
        return magics.get(magic);
    }

    public void load() {
        magics = new HashMap<Integer, Magic>();
        lastUsedID = 0;
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }
        try {
            File file = new File(saveDir, "magic.dat");
            if (file.exists()) {
                loadMagicFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(saveDir, "magic.dat_old");
                if (file.exists()) {
                    loadMagicFile(file);
                }
            } catch (Exception ee) { }
        }
        magics.clear();
        if (true) {
            // Create default Pokémon types
            Magic normal   = new Magic(0, "Normal", 0xA8A878);
            normal.setIconItem(new ItemStack(net.minecraft.init.Items.paper));  // using paper for Normal

            Magic fire     = new Magic(1, "Fire", 0xF08030);
            fire.setIconItem(new ItemStack(net.minecraft.init.Items.flint));    // flint for Fire

            Magic water    = new Magic(2, "Water", 0x6890F0);
            water.setIconItem(new ItemStack(net.minecraft.init.Items.water_bucket)); // water bucket

            Magic electric = new Magic(3, "Electric", 0xF8D030);
            electric.setIconItem(new ItemStack(net.minecraft.init.Items.redstone)); // redstone

            Magic grass    = new Magic(4, "Grass", 0x78C850);
            grass.setIconItem(new ItemStack(net.minecraft.init.Items.wheat_seeds));  // wheat seeds

            Magic ice      = new Magic(5, "Ice", 0x98D8D8);
            ice.setIconItem(new ItemStack(net.minecraft.init.Items.snowball));       // snowball for Ice

            Magic fighting = new Magic(6, "Fighting", 0xC03028);
            fighting.setIconItem(new ItemStack(net.minecraft.init.Items.iron_sword));  // sword for Fighting

            Magic poison   = new Magic(7, "Poison", 0xA040A0);
            poison.setIconItem(new ItemStack(net.minecraft.init.Items.potionitem));    // potion item for Poison

            Magic ground   = new Magic(8, "Ground", 0xE0C068);
            ground.setIconItem(new ItemStack(Blocks.dirt));          // dirt for Ground

            Magic flying   = new Magic(9, "Flying", 0xA890F0);
            flying.setIconItem(new ItemStack(net.minecraft.init.Items.feather));       // feather for Flying

            Magic psychic  = new Magic(10, "Psychic", 0xF85888);
            psychic.setIconItem(new ItemStack(net.minecraft.init.Items.ender_pearl));  // ender pearl for Psychic

            Magic bug      = new Magic(11, "Bug", 0xA8B820);
            bug.setIconItem(new ItemStack(net.minecraft.init.Items.spider_eye));       // spider eye for Bug

            Magic rock     = new Magic(12, "Rock", 0xB8A038);
            rock.setIconItem(new ItemStack(Blocks.cobblestone));     // cobblestone for Rock

            Magic ghost    = new Magic(13, "Ghost", 0x705898);
            ghost.setIconItem(new ItemStack(net.minecraft.init.Items.skull));          // skull for Ghost

            Magic dragon   = new Magic(14, "Dragon", 0x7038F8);
            dragon.setIconItem(new ItemStack(Blocks.dragon_egg));    // dragon egg

            Magic dark     = new Magic(15, "Dark", 0x705848);
            dark.setIconItem(new ItemStack(net.minecraft.init.Items.coal));            // coal for Dark

            Magic steel    = new Magic(16, "Steel", 0xB8B8D0);
            steel.setIconItem(new ItemStack(net.minecraft.init.Items.iron_ingot));     // iron ingot for Steel

            Magic fairy    = new Magic(17, "Fairy", 0xEE99AC);
            fairy.setIconItem(new ItemStack(net.minecraft.init.Items.sugar));          // sugar for Fairy

            // Now set weaknesses (using a multiplier of 2.0f to indicate “double damage”).
            // The following is based on a common Pokémon type chart.
            normal.weaknesses.put(fighting.id, 2.0f);

            fire.weaknesses.put(water.id, 2.0f);
            fire.weaknesses.put(ground.id, 2.0f);
            fire.weaknesses.put(rock.id, 2.0f);

            water.weaknesses.put(electric.id, 2.0f);
            water.weaknesses.put(grass.id, 2.0f);

            electric.weaknesses.put(ground.id, 2.0f);

            grass.weaknesses.put(fire.id, 2.0f);
            grass.weaknesses.put(ice.id, 2.0f);
            grass.weaknesses.put(poison.id, 2.0f);
            grass.weaknesses.put(flying.id, 2.0f);
            grass.weaknesses.put(bug.id, 2.0f);

            ice.weaknesses.put(fire.id, 2.0f);
            ice.weaknesses.put(fighting.id, 2.0f);
            ice.weaknesses.put(rock.id, 2.0f);
            ice.weaknesses.put(steel.id, 2.0f);

            fighting.weaknesses.put(flying.id, 2.0f);
            fighting.weaknesses.put(psychic.id, 2.0f);
            fighting.weaknesses.put(fairy.id, 2.0f);

            poison.weaknesses.put(ground.id, 2.0f);
            poison.weaknesses.put(psychic.id, 2.0f);

            ground.weaknesses.put(water.id, 2.0f);
            ground.weaknesses.put(grass.id, 2.0f);
            ground.weaknesses.put(ice.id, 2.0f);

            flying.weaknesses.put(electric.id, 2.0f);
            flying.weaknesses.put(ice.id, 2.0f);
            flying.weaknesses.put(rock.id, 2.0f);

            psychic.weaknesses.put(bug.id, 2.0f);
            psychic.weaknesses.put(ghost.id, 2.0f);
            psychic.weaknesses.put(dark.id, 2.0f);

            bug.weaknesses.put(fire.id, 2.0f);
            bug.weaknesses.put(flying.id, 2.0f);
            bug.weaknesses.put(rock.id, 2.0f);

            rock.weaknesses.put(water.id, 2.0f);
            rock.weaknesses.put(grass.id, 2.0f);
            rock.weaknesses.put(fighting.id, 2.0f);
            rock.weaknesses.put(ground.id, 2.0f);
            rock.weaknesses.put(steel.id, 2.0f);

            ghost.weaknesses.put(ghost.id, 2.0f);
            ghost.weaknesses.put(dark.id, 2.0f);

            dragon.weaknesses.put(ice.id, 2.0f);
            dragon.weaknesses.put(dragon.id, 2.0f);
            dragon.weaknesses.put(fairy.id, 2.0f);

            dark.weaknesses.put(fighting.id, 2.0f);
            dark.weaknesses.put(bug.id, 2.0f);
            dark.weaknesses.put(fairy.id, 2.0f);

            steel.weaknesses.put(fire.id, 2.0f);
            steel.weaknesses.put(fighting.id, 2.0f);
            steel.weaknesses.put(ground.id, 2.0f);

            fairy.weaknesses.put(poison.id, 2.0f);
            fairy.weaknesses.put(steel.id, 2.0f);

            // Register the types.
            magics.put(normal.id, normal);
            magics.put(fire.id, fire);
            magics.put(water.id, water);
            magics.put(electric.id, electric);
            magics.put(grass.id, grass);
            magics.put(ice.id, ice);
            magics.put(fighting.id, fighting);
            magics.put(poison.id, poison);
            magics.put(ground.id, ground);
            magics.put(flying.id, flying);
            magics.put(psychic.id, psychic);
            magics.put(bug.id, bug);
            magics.put(rock.id, rock);
            magics.put(ghost.id, ghost);
            magics.put(dragon.id, dragon);
            magics.put(dark.id, dark);
            magics.put(steel.id, steel);
            magics.put(fairy.id, fairy);
        }

        if (false) {
            // Create default magics
            Magic earth = new Magic(0, "Earth", 0x00DD00, 1, 1);
            earth.setIconItem(new ItemStack(CustomItems.earthElement));

            Magic water = new Magic(1, "Water", 0xF2DD00, 1, 2);
            water.setIconItem(new ItemStack(CustomItems.waterElement));

            Magic fire    = new Magic(2, "Fire", 0xDD0000, 1, 3);
            fire.setIconItem(new ItemStack(CustomItems.spellFire));

            Magic air   = new Magic(3, "Air", 0xDD0000, 1, 0);
            air.setIconItem(new ItemStack(CustomItems.airElement));

            Magic dark   = new Magic(4, "Dark", 0xDD0000, 0, 3);
            dark.setIconItem(new ItemStack(CustomItems.spellDark));

            Magic holy   = new Magic(5, "Holy", 0xDD0000, 0, 1);
            holy.setIconItem(new ItemStack(CustomItems.spellHoly));

            Magic nature   = new Magic(6, "Nature", 0xDD0000, 0, 0);
            nature.setIconItem(new ItemStack(CustomItems.spellNature));

            Magic arcane   = new Magic(7, "Arcane", 0xDD0000, 0, 2);
            arcane.setIconItem(new ItemStack(CustomItems.spellArcane));

            // Insiders
            earth.weaknesses.put(air.id, 0.60f);
            water.weaknesses.put(earth.id, 0.60f);
            fire.weaknesses.put(water.id, 0.60f);
            air.weaknesses.put(fire.id, 0.60f);

            // Outsiders
            dark.weaknesses.put(nature.id, 0.60f);
            nature.weaknesses.put(holy.id, 0.60f);
            holy.weaknesses.put(arcane.id, 0.60f);
            arcane.weaknesses.put(dark.id, 0.60f);

            // Cross Interactions
            earth.weaknesses.put(nature.id, 0.30f);
            water.weaknesses.put(holy.id, 0.30f);
            fire.weaknesses.put(arcane.id, 0.30f);
            air.weaknesses.put(dark.id, 0.30f);
            dark.weaknesses.put(fire.id, 0.30f);
            nature.weaknesses.put(air.id, 0.30f);
            holy.weaknesses.put(earth.id, 0.30f);
            arcane.weaknesses.put(water.id, 0.30f);

            // Add them to the registry
            magics.put(earth.id, earth);
            magics.put(water.id, water);
            magics.put(fire.id, fire);
            magics.put(air.id, air);
            magics.put(dark.id, dark);
            magics.put(holy.id, holy);
            magics.put(nature.id, nature);
            magics.put(arcane.id, arcane);
        }
        printAllMagicInteractions();
    }

    private void loadMagicFile(File file) throws IOException {
        DataInputStream stream = new DataInputStream(
            new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)))
        );
        loadMagic(stream);
        stream.close();
    }

    public void loadMagic(DataInputStream stream) throws IOException {
        HashMap<Integer, Magic> magic = new HashMap<Integer, Magic>();
        NBTTagCompound nbttagcompound1 = CompressedStreamTools.read(stream);
        lastUsedID = nbttagcompound1.getInteger("lastID");
        NBTTagList list = nbttagcompound1.getTagList("NPCMagic", 10); // using "NPCMagic" as tag

        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
                Magic loadMagic = new Magic();
                loadMagic.readNBT(nbttagcompound);
                magic.put(loadMagic.id, loadMagic);
            }
        }
        this.magics = magic;
    }

    public NBTTagCompound getNBT() {
        NBTTagList list = new NBTTagList();
        for (int slot : magics.keySet()) {
            Magic mag = magics.get(slot);
            NBTTagCompound nbtMagic = new NBTTagCompound();
            mag.writeNBT(nbtMagic);
            list.appendTag(nbtMagic);
        }
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setInteger("lastID", lastUsedID);
        nbttagcompound.setTag("NPCMagic", list); // use same tag as loadMagic
        return nbttagcompound;
    }

    public void saveFactions() {
        try {
            File saveDir = CustomNpcs.getWorldSaveDirectory();
            File file = new File(saveDir, "magic.dat_new");
            File file1 = new File(saveDir, "magic.dat_old");
            File file2 = new File(saveDir, "magic.dat");
            CompressedStreamTools.writeCompressed(getNBT(), new FileOutputStream(file));
            if (file1.exists()) {
                file1.delete();
            }
            file2.renameTo(file1);
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public Magic get(int magicID) {
        return magics.get(magicID);
    }

    public List<IMagic> list() {
        return new ArrayList<IMagic>(this.magics.values());
    }

    public void saveMagic(Magic mag) {
        if (mag.id < 0) {
            mag.id = getUnusedId();
            while (hasName(mag.name))
                mag.name += "_";
        } else {
            Magic existing = magics.get(mag.id);
            if (existing != null && !existing.name.equals(mag.name))
                while (hasName(mag.name))
                    mag.name += "_";
        }
        magics.remove(mag.id);
        magics.put(mag.id, mag);

        NBTTagCompound facCompound = new NBTTagCompound();
        mag.writeNBT(facCompound);
        // Server.sendToAll(EnumPacketClient.SYNC_UPDATE, SyncType.MAGIC, facCompound);
        saveFactions();
    }

    public int getUnusedId() {
        if (lastUsedID == 0) {
            for (int catid : magics.keySet())
                if (catid > lastUsedID)
                    lastUsedID = catid;
        }
        lastUsedID++;
        return lastUsedID;
    }

    public boolean hasName(String newName) {
        if (newName.trim().isEmpty())
            return true;
        for (Magic mag : magics.values())
            if (mag.name.equals(newName))
                return true;
        return false;
    }

    public Magic getMagicFromName(String magicName) {
        for (Map.Entry<Integer, Magic> entryMag : MagicController.getInstance().magics.entrySet()) {
            if (entryMag.getValue().name.equalsIgnoreCase(magicName)) {
                return entryMag.getValue();
            }
        }
        return null;
    }

    public String[] getNames() {
        String[] names = new String[magics.size()];
        int i = 0;
        for (Magic mag : magics.values()) {
            names[i] = mag.name.toLowerCase();
            i++;
        }
        return names;
    }

    // --- New functions for applying magic NBT stats and calculating damage ---

    /**
     * Applies magic-type damage to an item. A weapon/item can have multiple magic damage values.
     * The damage is stored under a compound tag "MagicDamage" where each key is the magic ID.
     */
    public static void addMagicDamageToItem(ItemStack item, int magicId, float damage) {
        if (item == null) return;
        if (item.stackTagCompound == null)
            item.stackTagCompound = new NBTTagCompound();
        NBTTagCompound compound = item.stackTagCompound;
        NBTTagCompound magicDamage;
        if (compound.hasKey("MagicDamage")) {
            magicDamage = compound.getCompoundTag("MagicDamage");
        } else {
            magicDamage = new NBTTagCompound();
        }
        magicDamage.setFloat(String.valueOf(magicId), damage);
        compound.setTag("MagicDamage", magicDamage);
    }

    /**
     * Applies magic resistance to an armor piece.
     * The resistance is stored under a compound tag "MagicResistances" with the magic ID as key.
     */
    public static void addMagicResistanceToArmor(ItemStack armor, int magicId, float resistance) {
        if (armor == null) return;
        if (armor.stackTagCompound == null)
            armor.stackTagCompound = new NBTTagCompound();
        NBTTagCompound compound = armor.stackTagCompound;
        NBTTagCompound magicResist;
        if (compound.hasKey("MagicResistances")) {
            magicResist = compound.getCompoundTag("MagicResistances");
        } else {
            magicResist = new NBTTagCompound();
        }
        magicResist.setFloat(String.valueOf(magicId), resistance);
        compound.setTag("MagicResistances", magicResist);
    }

    /**
     * Given a collection of armor pieces, sums the resistance value for the specified magic ID.
     */
    public static float calculateTotalMagicResistance(int magicId, Iterable<ItemStack> armors) {
        float totalResistance = 0.0F;
        for (ItemStack armor : armors) {
            if (armor != null && armor.hasTagCompound() && armor.stackTagCompound.hasKey("MagicResistances")) {
                NBTTagCompound magicResist = armor.stackTagCompound.getCompoundTag("MagicResistances");
                if (magicResist.hasKey(String.valueOf(magicId))) {
                    totalResistance += magicResist.getFloat(String.valueOf(magicId));
                }
            }
        }
        return totalResistance;
    }

    /**
     * Calculates the final magic damage after applying armor resistances.
     * For a given magic type, the resistance is applied.
     *
     * @param magicId    the magic type
     * @param baseDamage the base damage value
     * @param armors     an Iterable collection of armor ItemStacks
     * @return the final damage after reduction
     */
    public static float calculateFinalMagicDamage(int magicId, float baseDamage, Iterable<ItemStack> armors) {
        if (getInstance().getMagic(magicId) == null) {
            // If the magic type is not defined, treat it as non-magical.
            return baseDamage;
        }
        float resistance = calculateTotalMagicResistance(magicId, armors);
        // Resistance is assumed to be a fractional reduction (e.g., 0.2 = 20% reduction).
        return baseDamage * (1.0F - resistance);
    }

    /**
     * Extracts magic damage values from a weapon ItemStack.
     * It looks for a compound tag "CNPCMagic" first; if not found, it falls back to "MagicDamage".
     * Returns a mapping of magic ID to its damage value.
     */
    public static Map<Integer, Float> getMagicDamageFromItem(ItemStack item) {
        Map<Integer, Float> damageMap = new HashMap<Integer, Float>();
        if (item == null || item.stackTagCompound == null) return damageMap;
        NBTTagCompound compound = item.stackTagCompound;
        NBTTagCompound damageCompound = null;
        if (compound.hasKey("CNPCMagic")) {
            NBTTagCompound magicCompound = compound.getCompoundTag("CNPCMagic");
            if (magicCompound.hasKey("MagicDamage")) {
                damageCompound = magicCompound.getCompoundTag("MagicDamage");
            }
        } else if (compound.hasKey("MagicDamage")) {
            damageCompound = compound.getCompoundTag("MagicDamage");
        }
        if (damageCompound != null) {
            Set<String> keys = damageCompound.func_150296_c();
            for (String key : keys) {
                try {
                    int magicId = Integer.parseInt(key);
                    float dmg = damageCompound.getFloat(key);
                    damageMap.put(magicId, dmg);
                } catch (NumberFormatException e) {
                    // Ignore non-integer keys.
                }
            }
        }
        return damageMap;
    }

    /**
     * Extracts magic resistances from an armor ItemStack.
     * It checks for a "CNPCMagic" container first; if not present, it falls back to "MagicResistances".
     * Returns a mapping of magic ID to its resistance value.
     */
    public static Map<Integer, Float> getMagicResistancesFromItem(ItemStack item) {
        Map<Integer, Float> resistMap = new HashMap<Integer, Float>();
        if (item == null || item.stackTagCompound == null) return resistMap;
        NBTTagCompound compound = item.stackTagCompound;
        NBTTagCompound resistCompound = null;
        if (compound.hasKey("CNPCMagic")) {
            NBTTagCompound magicCompound = compound.getCompoundTag("CNPCMagic");
            if (magicCompound.hasKey("MagicResistances")) {
                resistCompound = magicCompound.getCompoundTag("MagicResistances");
            }
        } else if (compound.hasKey("MagicResistances")) {
            resistCompound = compound.getCompoundTag("MagicResistances");
        }
        if (resistCompound != null) {
            Set<String> keys = resistCompound.func_150296_c();
            for (String key : keys) {
                try {
                    int magicId = Integer.parseInt(key);
                    float res = resistCompound.getFloat(key);
                    resistMap.put(magicId, res);
                } catch (NumberFormatException e) {
                    // Ignore non-integer keys.
                }
            }
        }
        return resistMap;
    }

    /**
     * Validates whether an item (weapon or armor) contains any magic-related NBT.
     */
    public static boolean hasMagicStats(ItemStack item) {
        if (item == null || item.stackTagCompound == null) return false;
        NBTTagCompound compound = item.stackTagCompound;
        return compound.hasKey("CNPCMagic") || compound.hasKey("MagicDamage") || compound.hasKey("MagicResistances");
    }

    /**
     * Calculates a damage breakdown for each magic type from a weapon when applied against a set of armors.
     * This function will ignore any magic with ID 0 (neutral), so only magic damage is returned.
     * Returns a map where keys are magic IDs (non-zero) and values are the final damage for that type.
     */
    public static Map<Integer, Float> calculateDamageBreakdown(ItemStack weapon, Iterable<ItemStack> armors) {
        Map<Integer, Float> breakdown = new HashMap<Integer, Float>();
        Map<Integer, Float> weaponDamage = getMagicDamageFromItem(weapon);
        for (Map.Entry<Integer, Float> entry : weaponDamage.entrySet()) {
            int magicId = entry.getKey();
            // Skip magic ID 0 (neutral)
            if (magicId == 0) continue;
            float dmg = entry.getValue();
            float finalDmg = calculateFinalMagicDamage(magicId, dmg, armors);
            breakdown.put(magicId, finalDmg);
        }
        return breakdown;
    }

    /**
     * Calculates the total final magic damage of a weapon after applying the resistances of a set of armor pieces.
     * This sums only the magic interactions (magic IDs other than 0).
     */
    public static float calculateTotalDamage(ItemStack weapon, Iterable<ItemStack> armors) {
        float totalDamage = 0.0F;
        Map<Integer, Float> breakdown = calculateDamageBreakdown(weapon, armors);
        for (Float dmg : breakdown.values()) {
            totalDamage += dmg;
        }
        return totalDamage;
    }

    /**
     * Prints all magic interactions (weaknesses) in the registry.
     * For each magic (defender) that has weaknesses, prints the corresponding attacker and the extra damage percentage.
     * Format: "Magic ID: [attacker id], Name: "[attacker name]" does [percentage*100]% extra damage to Magic ID: [defender id], Name: "[defender name]".
     */
    public static void printAllMagicInteractions() {
        MagicController mc = getInstance();
        for (Magic defender : mc.magics.values()) {
            for (Map.Entry<Integer, Float> entry : defender.weaknesses.entrySet()) {
                int attackerId = entry.getKey();
                float percentage = entry.getValue();
                Magic attacker = mc.getMagic(attackerId);
                if (attacker != null) {
                    System.out.println("Magic ID: " + attacker.id + ", Name: \"" + attacker.name + "\" does "
                        + (percentage * 100) + "% extra damage to Magic ID: " + defender.id + ", Name: \"" + defender.name + "\"");
                }
            }
        }
    }
}
