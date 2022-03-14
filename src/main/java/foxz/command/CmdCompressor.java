package foxz.command;

import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import me.luizotavio.compressor.ZstdCompressor;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static me.luizotavio.compressor.executor.IOExecutor.IO_EXECUTOR;

@Command(
    name = "compressor",
    desc = "Convert a file to a compressed file or back to json",
    usage = "update [file|all] [json|zstd]"
)
public class CmdCompressor extends ChMcLogger {

    public CmdCompressor(Object sender) {
        super(sender);
    }

    @SubCommand(
        name = "update",
        desc = "Convert all files in the current directory to compressed files",
        permissions = {OpOnly.class}
    )
    public boolean onConvert(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        if (args.length == 0) {
            sendmessage("Usage: /noppes compressor update [<filename>|all] [json|zstd]");
            return true;
        }
        if (args.length == 1) {
            sendmessage("Please specify the type of compression to use");
            return true;
        }

        String type;

        if (args[1].equalsIgnoreCase("json")) {
            type = "json";
        } else if (args[1].equalsIgnoreCase("zstd")) {
            type = "zstd";
        } else {
            sendmessage("Please specify the type of compression to use");
            return true;
        }

        File directory = PlayerDataController.instance.getSaveDir();

        if (args[0].equalsIgnoreCase("all")) {
            sendmessage("Converting all files in the current directory to " + type + " files");

            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }

                if (file.getName().endsWith(".json") && type.equalsIgnoreCase("zstd")) {
                    submit(() -> {
                        try {
                            NBTTagCompound compound = NBTJsonUtil.LoadFile(file);

                            File compressed = new File(file.getParentFile(), file.getName().replace(".json", ".zstd"));

                            byte[] bytes = ZstdCompressor.writeCompound(compound);

                            Files.write(
                                compressed.toPath(),
                                bytes,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE
                            );
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    });
                }

                if (file.getName().endsWith(".zstd") && type.equalsIgnoreCase("json")) {
                    submit(() -> {
                        try {
                            NBTTagCompound compound = ZstdCompressor.readCompressCompound(
                                new RandomAccessFile(file, "r")
                            );

                            File compressed = new File(file.getParentFile(), file.getName().replace(".zstd", ".json"));

                            NBTJsonUtil.SaveFile(compressed, compound);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            sendmessage("Done! All files were converted.");
        } else {
            File file = new File(directory, args[0] + ".json");

            if (!file.exists()) {
                file = new File(directory, args[0] + ".zstd");
            }

            if (!file.exists()) {
                sendmessage("File not found");
                return true;
            }

            File finalFile = file;

            if (file.getName().endsWith(".json")) {
                if (type.equalsIgnoreCase("json")) {
                    sendmessage("File is already in json format");
                    return true;
                }

                submit(() -> {
                    try {
                        NBTTagCompound compound = NBTJsonUtil.LoadFile(finalFile);

                        File compressed = new File(finalFile.getParentFile(), finalFile.getName().replace(".json", ".zstd"));

                        byte[] bytes = ZstdCompressor.writeCompound(compound);

                        Files.write(
                            compressed.toPath(),
                            bytes,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE
                        );

                        sendmessage("File compressed to zstd!");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
            }

            if (file.getName().endsWith(".zstd")) {
                if (type.equalsIgnoreCase("zstd")) {
                    sendmessage("File is already in zstd format");
                    return true;
                }

                submit(() -> {
                    try {
                        NBTTagCompound compound = ZstdCompressor.readCompressCompound(
                            new RandomAccessFile(finalFile, "r")
                        );

                        if (compound == null) {
                            return;
                        }

                        File compressed = new File(finalFile.getParentFile(), finalFile.getName().replace(".zstd", ".json"));

                        NBTJsonUtil.SaveFile(compressed, compound);

                        sendmessage("File converted to json!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        return true;
    }

    private void submit(Runnable runnable) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture.runAsync(runnable, IO_EXECUTOR).get(5, TimeUnit.MINUTES);
    }


}
