package me.luizotavio.compressor;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.NBTJsonUtil;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class CompressorTest {

    public static final int MAX_ENTRIES = 1000;

    public static void main(String[] args) throws JsonException, IOException {
        Logger logger = Logger.getLogger("CompressorTest");

        logger.info("Starting CompressorTest with " + MAX_ENTRIES + " entries");

        NBTTagCompound compound = new NBTTagCompound();
        File file = new File("test.json");

        fill(compound);

        long start = System.currentTimeMillis();

        logger.info("Writing the data into JSON...");

        NBTJsonUtil.SaveFile(file, compound);

        long end = System.currentTimeMillis();

        logger.info("Done! Took " + (end - start) + "ms");
        logger.info("Reading the data from JSON...");

        start = System.currentTimeMillis();

        NBTJsonUtil.LoadFile(new File("test.json"));

        end = System.currentTimeMillis();

        logger.info("Done! Took " + (end - start) + "ms");
        logger.info("Now, we will compress the data into zstd...");

        start = System.currentTimeMillis();

        byte[] bytes = ZstdCompressor.writeCompound(compound);

        end = System.currentTimeMillis();

        logger.info("Done! Took " + (end - start) + "ms");
        logger.info("Now, we will decompress the data from zstd...");

        start = System.currentTimeMillis();

        ZstdCompressor.readCompressed(
            new DataInputStream(
                new ByteArrayInputStream(bytes)
            )
        );

        end = System.currentTimeMillis();

        logger.info("Done! Took " + (end - start) + "ms");

        file.deleteOnExit();
    }

    private static void fill(NBTTagCompound compound) {
        for (int i = 0; i < MAX_ENTRIES; i++) {
            compound.setInteger("" + i, i);
        }
    }

}
