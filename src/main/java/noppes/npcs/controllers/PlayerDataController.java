package noppes.npcs.controllers;

import com.github.luben.zstd.Zstd;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.luizotavio.zstd.NBTReaderUtil;
import me.luizotavio.zstd.NBTWriterUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerDataController {
    public static PlayerDataController instance;

    public static ExecutorService executorService = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder()
      .setNameFormat("CNPC Thread %d")
      .build()
    );

    public PlayerDataController() {
        instance = this;
    }

    public File getSaveDir() {
        try {
            File file = new File(CustomNpcs.getWorldSaveDirectory(), "playerdata");
            if (!file.exists())
                file.mkdir();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public NBTTagCompound loadPlayerData(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            File data = new File(getSaveDir(), uniqueId + ".dat");

            if (data.exists()) {
                try {
                    return from(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return new NBTTagCompound();
        }, executorService).join();
    }

    public void savePlayerData(NBTTagCompound compound, UUID uuid) {
        executorService.submit(() -> {
            File file = new File(getSaveDir(), uuid + ".dat");

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file))) {
                byte[] uncompressed = NBTWriterUtil.writeCompound(compound),
                  compressed = Zstd.compress(uncompressed);

                outputStream.writeInt(compressed.length);
                outputStream.writeInt(uncompressed.length);

                outputStream.write(compressed);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public PlayerBankData getBankData(EntityPlayer player, int bankId) {
        Bank bank = BankController.getInstance().getBank(bankId);
        PlayerBankData data = getPlayerData(player).bankData;
        if (!data.hasBank(bank.id)) {
            data.loadNew(bank.id);
        }
        return data;
    }

    public PlayerData getPlayerData(EntityPlayer player) {
        PlayerData data = (PlayerData) player.getExtendedProperties("CustomNpcsData");
        if (data == null) {
            player.registerExtendedProperties("CustomNpcsData", data = new PlayerData());
            data.player = player;
            data.loadNBTData(null);
        }
        data.player = player;
        return data;
    }

    public String hasPlayer(String username) {
        for (String name : getUsernameData().keySet()) {
            if (name.equalsIgnoreCase(username))
                return name;
        }

        return "";
    }

    public PlayerData getDataFromUsername(String username) {
        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
        PlayerData data = null;
        if (player == null) {
            Map<String, NBTTagCompound> map = getUsernameData();
            for (String name : map.keySet()) {
                if (name.equalsIgnoreCase(username)) {
                    data = new PlayerData();
                    data.setNBT(map.get(name));
                    break;
                }
            }
        } else
            data = getPlayerData(player);

        return data;
    }

    public void addPlayerMessage(String username, PlayerMail mail) {
        mail.time = System.currentTimeMillis();

        PlayerData data = getDataFromUsername(username);
        data.mailData.playermail.add(mail.copy());

        savePlayerData(data.getNBT(), data.player.getPersistentID());
    }

    public Map<String, NBTTagCompound> getUsernameData() {
        Map<String, NBTTagCompound> map = new HashMap<>();

        try {
            for (File file : getSaveDir().listFiles()) {
                if (file.isDirectory()) continue;

                NBTTagCompound compound = from(file);

                map.put(
                  compound.getString("PlayerName"),
                  compound
                );
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return map;
    }

    private NBTTagCompound from(File file) throws IOException {
        try (DataInputStream outputStream = new DataInputStream(new FileInputStream(file))) {
            return NBTReaderUtil.readCompressCompound(outputStream);
        }
    }

    public boolean hasMail(EntityPlayer player) {
        return getPlayerData(player).mailData.hasMail();
    }
}
